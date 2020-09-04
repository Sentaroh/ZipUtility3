package com.sentaroh.android.ZipUtility3;
/*
The MIT License (MIT)
Copyright (c) 2011-2019 Sentaroh

Permission is hereby granted, free of charge, to any person obtaining a copy of
this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights to use,
copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software,
and to permit persons to whom the Software is furnished to do so, subject to
the following conditions:

The above copyright notice and this permission notice shall be included in all copies or
substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
OTHER DEALINGS IN THE SOFTWARE.

*/

import android.app.Activity;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager.LayoutParams;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.core.content.FileProvider;
import androidx.fragment.app.FragmentManager;

import com.sentaroh.android.Utilities3.CallBackListener;
import com.sentaroh.android.Utilities3.ContextButton.ContextButtonUtil;
import com.sentaroh.android.Utilities3.ContextMenu.CustomContextMenu;
import com.sentaroh.android.Utilities3.ContextMenu.CustomContextMenuItem.CustomContextMenuOnClickListener;
import com.sentaroh.android.Utilities3.Dialog.CommonDialog;
import com.sentaroh.android.Utilities3.Dialog.CommonFileSelector2;
import com.sentaroh.android.Utilities3.Dialog.MessageDialogFragment;
import com.sentaroh.android.Utilities3.Dialog.ProgressSpinDialogFragment;
import com.sentaroh.android.Utilities3.MiscUtil;
import com.sentaroh.android.Utilities3.NotifyEvent;
import com.sentaroh.android.Utilities3.NotifyEvent.NotifyEventListener;
import com.sentaroh.android.Utilities3.SafFile3;
import com.sentaroh.android.Utilities3.SafStorage3;
import com.sentaroh.android.Utilities3.ThemeUtil;
import com.sentaroh.android.Utilities3.ThreadCtrl;
import com.sentaroh.android.Utilities3.Widget.CustomSpinnerAdapter;
import com.sentaroh.android.Utilities3.Widget.NonWordwrapTextView;
import com.sentaroh.android.Utilities3.Zip.BufferedZipFile3;
import com.sentaroh.android.Utilities3.Zip.ZipUtil;


import net.lingala.zip4j.model.FileHeader;
import net.lingala.zip4j.model.enums.CompressionMethod;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.regex.Pattern;

import static com.sentaroh.android.ZipUtility3.Constants.ENCODING_NAME_UTF8;
import static com.sentaroh.android.ZipUtility3.Constants.IO_AREA_SIZE;
import static com.sentaroh.android.ZipUtility3.Constants.MIME_TYPE_TEXT;
import static com.sentaroh.android.ZipUtility3.Constants.MIME_TYPE_ZIP;

public class LocalFileManager {
    private GlobalParameters mGp = null;

    private FragmentManager mFragmentManager = null;
    private CommonDialog mCommonDlg = null;

    private Context mContext;
    private ActivityMain mActivity = null;

    private ListView mTreeFilelistView = null;
    private CustomTreeFilelistAdapter mTreeFilelistAdapter = null;
    private TextView mLocalViewMsg = null;

    private View mMainView = null;
    private Handler mUiHandler = null;
    private String mMainFilePath = "";
    private String mMainStoragePath = "";

    private Button mFileListUp, mFileListTop;
    private NonWordwrapTextView mCurrentDirectory;
    private TextView mFileEmpty;
    private LinearLayout mMainDialogView = null;

    private CommonUtilities mUtil = null;
//	public void setOptions(boolean debug, int mln) {
//		mGlblParms.debugEnabled=debug;
//	}

    public LocalFileManager(GlobalParameters gp, ActivityMain a, FragmentManager fm, LinearLayout mv) {
        mGp = gp;
        mActivity = a;
        mCommonDlg = new CommonDialog(a, fm);
        mUiHandler = new Handler();
        mContext = a;

        mFragmentManager = fm;
        mMainStoragePath=mMainFilePath = "/storage/emulated/0";//mGp.internalRootDirectory;
        mUtil = new CommonUtilities(mContext, "LocalFolder", gp, mCommonDlg);

        mMainView = mv;
        initViewWidget();
        mTreeFilelistAdapter = new CustomTreeFilelistAdapter(mActivity, false, true);

        mLocalStorageSelector.setEnabled(false);
        createFileList(mMainFilePath, null);
        mUiHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mLocalStorageSelector.setEnabled(true);
            }
        },200);

    }

    public void reInitView() {
        ArrayList<TreeFilelistItem> fl = mTreeFilelistAdapter.getDataList();
        int v_pos_fv = 0, v_pos_top = 0;
        v_pos_fv = mTreeFilelistView.getFirstVisiblePosition();
        if (mTreeFilelistView.getChildAt(0) != null)
            v_pos_top = mTreeFilelistView.getChildAt(0).getTop();

        mTreeFilelistAdapter = new CustomTreeFilelistAdapter(mActivity, false, true);

        mTreeFilelistAdapter.setDataList(fl);
        mTreeFilelistView.setAdapter(mTreeFilelistAdapter);
        mTreeFilelistView.setSelectionFromTop(v_pos_fv, v_pos_top);
        mTreeFilelistAdapter.notifyDataSetChanged();
    }

    public boolean isFileListSortAscendant() {
        if (mTreeFilelistAdapter != null) return mTreeFilelistAdapter.isSortAscendant();
        else return true;
    }

    public void refreshFileList() {
        mUtil.addDebugMsg(1, "I", CommonUtilities.getExecutedMethodName() + " entered");

        final String curr_dir = mCurrentDirectory.getText().toString();

        final int pos_x = mTreeFilelistView.getFirstVisiblePosition();
        final int pos_y = mTreeFilelistView.getChildAt(0) == null ? 0 : mTreeFilelistView.getChildAt(0).getTop();
        final ArrayList<TreeFilelistItem> prev_fl = mTreeFilelistAdapter.getDataList();
        final boolean prev_selected = mTreeFilelistAdapter.isItemSelected();

        NotifyEvent ntfy=new NotifyEvent(mContext);
        ntfy.setListener(new NotifyEvent.NotifyEventListener() {
            @Override
            public void positiveResponse(Context context, Object[] objects) {
                ArrayList<TreeFilelistItem> tfl=(ArrayList<TreeFilelistItem>)objects[0];
                if (prev_selected) {
                    ArrayList<TreeFilelistItem> new_fl = mTreeFilelistAdapter.getDataList();
                    for (TreeFilelistItem prev_item : prev_fl) {
                        if (prev_item.isChecked()) {
                            for (TreeFilelistItem new_item : new_fl) {
                                if (prev_item.getName().equals(new_item.getName())) {
                                    new_item.setChecked(true);
                                    break;
                                }
                            }
                        }
                    }
                    mTreeFilelistAdapter.notifyDataSetChanged();
                }
                mTreeFilelistView.setSelectionFromTop(pos_x, pos_y);

                if (isCopyCutDestValid(curr_dir)) {
                    mContextButtonPasteView.setVisibility(ImageButton.VISIBLE);
                } else {
                    mContextButtonPasteView.setVisibility(ImageButton.INVISIBLE);
                }

            }
            @Override
            public void negativeResponse(Context context, Object[] objects) {}
        });
        createFileList(curr_dir, ntfy);
    }

    public void refreshFileList(boolean refresh_file_list) {
        mUtil.addDebugMsg(1, "I", CommonUtilities.getExecutedMethodName() + " entered");
        if (refresh_file_list) {
            refreshFileList();
        } else {
            final String curr_dir = mCurrentDirectory.getText().toString();

            if (isCopyCutDestValid(curr_dir)) {
                mContextButtonPasteView.setVisibility(ImageButton.VISIBLE);
            } else {
                mContextButtonPasteView.setVisibility(ImageButton.INVISIBLE);
            }
        }
    }

    private Spinner mLocalStorageSelector;

    private LinearLayout mDialogProgressSpinView = null;
    private TextView mDialogProgressSpinMsg2 = null;
    private Button mDialogProgressSpinCancel = null;

    private LinearLayout mDialogMessageView = null;
    private TextView mDialogMessageTitle = null;
    private TextView mDialogMessageBody = null;
    private Button mDialogMessageOk = null;
    private Button mDialogMessageClose = null;
    private Button mDialogMessageCancel = null;

    private LinearLayout mDialogConfirmView = null;
    private TextView mDialogConfirmMsg = null;
    private TextView mDialogConfirmTitle = null;
    private TextView mDialogConfirmFilePath = null;
    private TextView mDialogConfirmFileDateTime = null;

    private Button mDialogConfirmCancel = null;

    private Button mDialogConfirmYes = null;
    private Button mDialogConfirmYesAll = null;
    private Button mDialogConfirmNo = null;
    private Button mDialogConfirmNoAll = null;

    private LinearLayout mContextButtonView = null;

    private ImageButton mContextButtonPaste = null;
    private LinearLayout mContextButtonPasteView = null;
    private ImageButton mContextButtonCopy = null;
    private LinearLayout mContextButtonCopyView = null;
    private ImageButton mContextButtonCut = null;
    private LinearLayout mContextButtonCutView = null;

    private LinearLayout mContextButtonCreateView = null;
    private ImageButton mContextButtonCreate = null;
    private LinearLayout mContextButtonShareView = null;
    private ImageButton mContextButtonShare = null;

    private LinearLayout mContextButtonRenameView = null;
    private ImageButton mContextButtonRename = null;

    private ImageButton mContextButtonArchive = null;
    private LinearLayout mContextButtonArchiveView = null;

    private ImageButton mContextButtonDelete = null;
    private LinearLayout mContextButtonDeleteView = null;
    private ImageButton mContextButtonSelectAll = null;
    private LinearLayout mContextButtonSelectAllView = null;
    private ImageButton mContextButtonUnselectAll = null;
    private LinearLayout mContextButtonUnselectAllView = null;

    private void invalidateLocalFileView() {
    }

    public void showLocalFileView(boolean show) {
        LinearLayout lv = (LinearLayout) mMainView.findViewById(R.id.local_file_view);
        if (show) lv.setVisibility(LinearLayout.VISIBLE);
        else lv.setVisibility(LinearLayout.INVISIBLE);
    }

    private void initViewWidget() {
        mUtil.addDebugMsg(1, "I", CommonUtilities.getExecutedMethodName() + " entered");
        mContextButtonView = (LinearLayout) mMainView.findViewById(R.id.context_view_local_file);

        LinearLayout lv = (LinearLayout) mMainView.findViewById(R.id.local_file_view);
        lv.setVisibility(LinearLayout.VISIBLE);

        mMainDialogView = (LinearLayout) mMainView.findViewById(R.id.main_dialog_view);
        mMainDialogView.setVisibility(LinearLayout.VISIBLE);

        mTreeFilelistView = (ListView) mMainView.findViewById(R.id.local_file_list);
        mFileEmpty = (TextView) mMainView.findViewById(R.id.local_file_empty);
        mFileEmpty.setVisibility(TextView.GONE);
        mTreeFilelistView.setVisibility(ListView.VISIBLE);

        mLocalViewMsg = (TextView) mMainView.findViewById(R.id.local_file_msg);

        mLocalStorageSelector = (Spinner) mMainView.findViewById(R.id.local_file_storage_spinner);
        CommonUtilities.setSpinnerBackground(mActivity, mLocalStorageSelector, mGp.themeIsLight);
        setLocalStorageSelector(false);

        mFileListUp = (Button) mMainView.findViewById(R.id.local_file_up_btn);
        if (mGp.themeIsLight)
            mFileListUp.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_16_go_up_dark, 0, 0, 0);
        else
            mFileListUp.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_16_go_up_light, 0, 0, 0);
        mFileListTop = (Button) mMainView.findViewById(R.id.local_file_top_btn);
        if (mGp.themeIsLight)
            mFileListTop.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_16_go_top_dark, 0, 0, 0);
        else
            mFileListTop.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_16_go_top_light, 0, 0, 0);
        mCurrentDirectory = (NonWordwrapTextView) mMainView.findViewById(R.id.local_file_filepath);
        mCurrentDirectory.setText(SafFile3.SAF_FILE_PRIMARY_STORAGE_PREFIX);
//        mCurrentDirectory.setDebugEnable(true);
//        mCurrentDirectory.setTextColor(mGp.themeColorList.text_color_primary);

        mDialogProgressSpinView = (LinearLayout) mMainView.findViewById(R.id.main_dialog_progress_spin_view);
        mDialogProgressSpinView.setVisibility(LinearLayout.GONE);
        mDialogProgressSpinMsg2 = (TextView) mMainView.findViewById(R.id.main_dialog_progress_spin_syncmsg);
        mDialogProgressSpinCancel = (Button) mMainView.findViewById(R.id.main_dialog_progress_spin_btn_cancel);

        mDialogConfirmView = (LinearLayout) mMainView.findViewById(R.id.main_dialog_confirm_view);
        mDialogConfirmView.setVisibility(LinearLayout.GONE);
        mDialogConfirmTitle = (TextView) mMainView.findViewById(R.id.main_dialog_confirm_title);
        mDialogConfirmMsg = (TextView) mMainView.findViewById(R.id.main_dialog_confirm_msg);
        mDialogConfirmFilePath = (TextView) mMainView.findViewById(R.id.main_dialog_confirm_file_path);
        mDialogConfirmFileDateTime = (TextView) mMainView.findViewById(R.id.main_dialog_confirm_file_date_time);
        mDialogConfirmCancel = (Button) mMainView.findViewById(R.id.main_dialog_confirm_sync_cancel);
        mDialogConfirmNo = (Button) mMainView.findViewById(R.id.copy_delete_confirm_no);
        mDialogConfirmNoAll = (Button) mMainView.findViewById(R.id.copy_delete_confirm_noall);
        mDialogConfirmYes = (Button) mMainView.findViewById(R.id.copy_delete_confirm_yes);
        mDialogConfirmYesAll = (Button) mMainView.findViewById(R.id.copy_delete_confirm_yesall);

        mContextButtonCreate = (ImageButton) mMainView.findViewById(R.id.context_button_clear);
        mContextButtonCreateView = (LinearLayout) mMainView.findViewById(R.id.context_button_clear_view);
        mContextButtonShare = (ImageButton) mMainView.findViewById(R.id.context_button_share);
        if (ThemeUtil.isLightThemeUsed(mActivity)) mContextButtonShare.setImageResource(R.drawable.context_button_share_dark);
        mContextButtonShareView = (LinearLayout) mMainView.findViewById(R.id.context_button_share_view);
        mContextButtonRename = (ImageButton) mMainView.findViewById(R.id.context_button_rename);
        mContextButtonRenameView = (LinearLayout) mMainView.findViewById(R.id.context_button_rename_view);
        mContextButtonPaste = (ImageButton) mMainView.findViewById(R.id.context_button_paste);
        mContextButtonPasteView = (LinearLayout) mMainView.findViewById(R.id.context_button_paste_view);
        mContextButtonCopy = (ImageButton) mMainView.findViewById(R.id.context_button_copy);
        mContextButtonCopyView = (LinearLayout) mMainView.findViewById(R.id.context_button_copy_view);
        mContextButtonCut = (ImageButton) mMainView.findViewById(R.id.context_button_cut);
        mContextButtonCutView = (LinearLayout) mMainView.findViewById(R.id.context_button_cut_view);

        mContextButtonArchive = (ImageButton) mMainView.findViewById(R.id.context_button_archive);
        mContextButtonArchiveView = (LinearLayout) mMainView.findViewById(R.id.context_button_archive_view);

        mContextButtonDelete = (ImageButton) mMainView.findViewById(R.id.context_button_delete);
        mContextButtonDeleteView = (LinearLayout) mMainView.findViewById(R.id.context_button_delete_view);
        mContextButtonSelectAll = (ImageButton) mMainView.findViewById(R.id.context_button_select_all);
        mContextButtonSelectAllView = (LinearLayout) mMainView.findViewById(R.id.context_button_select_all_view);
        mContextButtonUnselectAll = (ImageButton) mMainView.findViewById(R.id.context_button_unselect_all);
        mContextButtonUnselectAllView = (LinearLayout) mMainView.findViewById(R.id.context_button_unselect_all_view);
        mContextButtonUnselectAllView.setVisibility(LinearLayout.INVISIBLE);

        mContextButtonCreateView.setVisibility(ImageButton.INVISIBLE);
        mContextButtonShareView.setVisibility(ImageButton.INVISIBLE);
        mContextButtonRenameView.setVisibility(ImageButton.INVISIBLE);
        mContextButtonCopyView.setVisibility(ImageButton.INVISIBLE);
        mContextButtonCutView.setVisibility(ImageButton.INVISIBLE);
        mContextButtonPasteView.setVisibility(ImageButton.INVISIBLE);
        mContextButtonDeleteView.setVisibility(ImageButton.INVISIBLE);
        mContextButtonArchiveView.setVisibility(ImageButton.INVISIBLE);

        setContextButtonListener();
    }

    private void setContextButtonEnabled(final ImageButton btn, boolean enabled) {
        mUtil.addDebugMsg(1, "I", CommonUtilities.getExecutedMethodName() + " entered, enabled=" + enabled);
        if (enabled) {
            btn.postDelayed(new Runnable() {
                @Override
                public void run() {
                    btn.setEnabled(true);
                }
            }, 1000);
        } else {
            btn.setEnabled(false);
        }
    }

    private void setContextButtonSelectUnselectVisibility() {
        if (mTreeFilelistAdapter.getCount() > 0) {
            if (mTreeFilelistAdapter.isAllItemSelected())
                mContextButtonSelectAllView.setVisibility(ImageButton.INVISIBLE);
            else mContextButtonSelectAllView.setVisibility(ImageButton.VISIBLE);
            if (mTreeFilelistAdapter.getSelectedItemCount() == 0)
                mContextButtonUnselectAllView.setVisibility(ImageButton.INVISIBLE);
            else mContextButtonUnselectAllView.setVisibility(ImageButton.VISIBLE);
        } else {
            mContextButtonSelectAllView.setVisibility(ImageButton.INVISIBLE);
            mContextButtonUnselectAllView.setVisibility(ImageButton.INVISIBLE);
        }
    }


    private void setContextButtonListener() {
        mUtil.addDebugMsg(1, "I", CommonUtilities.getExecutedMethodName() + " entered");
        mContextButtonCreate.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isUiEnabled()) {
                    setContextButtonEnabled(mContextButtonCreate, false);
                    createItem(mTreeFilelistAdapter, mMainFilePath);
                    setContextButtonEnabled(mContextButtonCreate, true);
                }
            }
        });
        ContextButtonUtil.setButtonLabelListener(mActivity, mContextButtonCreate, mContext.getString(R.string.msgs_zip_cont_label_create));

        mContextButtonShare.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isUiEnabled()) {
                    setContextButtonEnabled(mContextButtonShare, false);
                    if (mTreeFilelistAdapter.isItemSelected()) shareItem(mTreeFilelistAdapter);
                    setContextButtonEnabled(mContextButtonShare, true);
                }
            }
        });
        ContextButtonUtil.setButtonLabelListener(mActivity, mContextButtonShare, mContext.getString(R.string.msgs_zip_cont_label_share));

        mContextButtonRename.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isUiEnabled()) {
                    setContextButtonEnabled(mContextButtonRename, false);
                    if (mTreeFilelistAdapter.isItemSelected()) renameItem(mTreeFilelistAdapter);
                    setContextButtonEnabled(mContextButtonRename, true);
                }
            }
        });
        ContextButtonUtil.setButtonLabelListener(mActivity, mContextButtonRename, mContext.getString(R.string.msgs_zip_cont_label_rename));

        mContextButtonArchive.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isUiEnabled()) {
                    setContextButtonEnabled(mContextButtonArchive, false);
                    if (mTreeFilelistAdapter.isItemSelected()) prepareZipSelectedItem(mTreeFilelistAdapter);
                    setContextButtonEnabled(mContextButtonArchive, true);
                }
            }
        });
        ContextButtonUtil.setButtonLabelListener(mActivity, mContextButtonArchive, mContext.getString(R.string.msgs_zip_cont_label_compress));

        mContextButtonPaste.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isUiEnabled()) {
                    setContextButtonEnabled(mContextButtonPaste, false);
                    pasteItem();
                    setContextButtonEnabled(mContextButtonPaste, true);
                }
            }
        });
        ContextButtonUtil.setButtonLabelListener(mActivity, mContextButtonPaste, mContext.getString(R.string.msgs_zip_cont_label_paste));

        mContextButtonCopy.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isUiEnabled()) {
                    setContextButtonEnabled(mContextButtonCopy, false);
                    if (mTreeFilelistAdapter.isItemSelected()) copyItem(mTreeFilelistAdapter);
                    setContextButtonEnabled(mContextButtonCopy, true);
                }
            }
        });
        ContextButtonUtil.setButtonLabelListener(mActivity, mContextButtonCopy, mContext.getString(R.string.msgs_zip_cont_label_copy));

        mContextButtonCut.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isUiEnabled()) {
                    setContextButtonEnabled(mContextButtonCut, false);
                    if (mTreeFilelistAdapter.isItemSelected()) cutItem(mTreeFilelistAdapter);
                    setContextButtonEnabled(mContextButtonCut, true);
                }
            }
        });
        ContextButtonUtil.setButtonLabelListener(mActivity, mContextButtonCut, mContext.getString(R.string.msgs_zip_cont_label_cut));

        mContextButtonDelete.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isUiEnabled()) {
                    setContextButtonEnabled(mContextButtonDelete, false);
                    if (mTreeFilelistAdapter.isItemSelected()) confirmDelete(mTreeFilelistAdapter);
                    setContextButtonEnabled(mContextButtonDelete, true);
                }
            }
        });
        ContextButtonUtil.setButtonLabelListener(mActivity, mContextButtonDelete, mContext.getString(R.string.msgs_zip_cont_label_delete));

        mContextButtonSelectAll.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isUiEnabled()) {
                    setContextButtonEnabled(mContextButtonSelectAll, false);
                    ArrayList<TreeFilelistItem> tfl = mTreeFilelistAdapter.getDataList();
                    for (TreeFilelistItem tfli : tfl) {
                        tfli.setChecked(true);
                    }
                    mTreeFilelistAdapter.notifyDataSetChanged();
                    setContextButtonEnabled(mContextButtonSelectAll, true);
                }
            }
        });
        ContextButtonUtil.setButtonLabelListener(mActivity, mContextButtonSelectAll, mContext.getString(R.string.msgs_zip_cont_label_select_all));

        mContextButtonUnselectAll.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isUiEnabled()) {
                    setContextButtonEnabled(mContextButtonUnselectAll, false);
                    mTreeFilelistAdapter.setAllItemUnchecked();
                    mTreeFilelistAdapter.notifyDataSetChanged();
                    setContextButtonEnabled(mContextButtonUnselectAll, true);
                }
            }
        });
        ContextButtonUtil.setButtonLabelListener(mActivity, mContextButtonUnselectAll, mContext.getString(R.string.msgs_zip_cont_label_unselect_all));
    }

    static private void setCheckedTextView(final CheckedTextView ctv) {
        ctv.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                ctv.toggle();
            }
        });
    }

    public void sortFileList() {
        CommonUtilities.sortFileList(mActivity, mGp, mTreeFilelistAdapter, null);
    }

    final static public void getAllPictureFileInDirectory(ArrayList<File> fl, File lf, boolean process_sub_directories) {
        if (lf.exists()) {
            if (lf.isDirectory()) {
                File[] cfl = lf.listFiles();
                if (cfl != null && cfl.length > 0) {
                    for (File cf : cfl) {
                        if (cf.isDirectory()) {
                            if (process_sub_directories)
                                getAllPictureFileInDirectory(fl, cf, process_sub_directories);
                        } else {
                            fl.add(cf);
                        }
                    }
                }
            } else {
                fl.add(lf);
            }
        }
    }

    private void shareItem(CustomTreeFilelistAdapter tfa) {
        mUtil.addDebugMsg(1,"I",CommonUtilities.getExecutedMethodName()+" entered");
        ArrayList<TreeFilelistItem> tfl = tfa.getDataList();
        ArrayList<String> fpl = new ArrayList<String>();
        for (int i = 0; i < tfl.size(); i++) {
            if (tfl.get(i).isChecked()) {
                if (!tfl.get(i).isDirectory()) {
                    fpl.add(tfl.get(i).getPath() + "/" + tfl.get(i).getName());
                } else {
                    ArrayList<File> fl = new ArrayList<File>();
                    File cf = new File(tfl.get(i).getPath() + "/" + tfl.get(i).getName());
                    getAllPictureFileInDirectory(fl, cf, true);

                    for (File item : fl) {
                        fpl.add(item.getAbsolutePath());
                    }
                }
            }
        }
        if (fpl.size() >= 100) {
            mCommonDlg.showCommonDialog(false, "E",
                    mContext.getString(R.string.msgs_local_file_share_file_max_file_count_reached), "", null);
            mUtil.addDebugMsg(1,"I",mContext.getString(R.string.msgs_local_file_share_file_max_file_count_reached));
        } else {
            if (fpl.size() > 1) {
                Intent intent = new Intent();
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.setAction(Intent.ACTION_SEND_MULTIPLE);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
//                intent.setType("image/*"); /* This example is sharing jpeg images. */

                ArrayList<Uri> files = new ArrayList<Uri>();

                for (String path : fpl) {
                    File file = new File(path);
                    Uri uri = null;
                    if (Build.VERSION.SDK_INT >= 24) uri = FileProvider.getUriForFile(mContext, BuildConfig.APPLICATION_ID + ".provider", new File(path));
                    else Uri.fromFile(file);
//                    Uri.fromFile(file);
                    files.add(uri);
                }
                intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, files);

                intent.setType("*/*");

                try {
                    mContext.startActivity(intent);
                } catch (Exception e) {
                    mCommonDlg.showCommonDialog(false, "E", "startActivity() failed at shareItem() for multiple item. message=" + e.getMessage(), "", null);
                    mUtil.addDebugMsg(1,"E","startActivity() failed at shareItem() for multiple item. message=" + e.getMessage());
                }
            } else if (fpl.size() == 1) {
                File lf = new File(fpl.get(0));
                String mt=getMimeTypeFromFileExtention(mGp, lf.getName());
                Intent intent = new Intent(android.content.Intent.ACTION_SEND);
                Uri uri = null;
                uri = Uri.parse("file://" + fpl.get(0));
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra(Intent.EXTRA_STREAM, uri);
                if (mt!=null) intent.setType(mt);
                else intent.setType("*/*");
                try {
                    mContext.startActivity(intent);
                } catch (Exception e) {
                    mCommonDlg.showCommonDialog(false, "E", "startActivity() failed at shareItem() for share item. message=" + e.getMessage(), "", null);
                    mUtil.addDebugMsg(1,"E","startActivity() failed at shareItem() for share item. message=" + e.getMessage());
                }
            } else {
                mCommonDlg.showCommonDialog(false, "E",
                        mContext.getString(R.string.msgs_local_file_share_file_no_file_selected), "", null);
                mUtil.addDebugMsg(1,"E",mContext.getString(R.string.msgs_local_file_share_file_no_file_selected));
            }
        }
    }

    static public String getMimeTypeFromFileExtention(GlobalParameters gp, String fn) {
        String fid="", mt=null;
        if (fn.lastIndexOf(".") > 0) {
            fid = fn.substring(fn.lastIndexOf(".") + 1, fn.length());
            fid=fid.toLowerCase();
        }
        if (!gp.settingOpenAsTextFileType.equals("")) {
            String[] ft_array=gp.settingOpenAsTextFileType.split(";");
            for(String ft_item:ft_array) {
                String ft=ft_item.trim();
                if (fid.equals(ft)) {
                    mt=MIME_TYPE_TEXT;
                    break;
                }
            }
        }
        if (mt==null) mt= MimeTypeMap.getSingleton().getMimeTypeFromExtension(fid);
        return mt;
    }

    private String mFindKey = "*";
    private AdapterSearchFileList mAdapterSearchFileList = null;
    private int mSearchListPositionX = 0;
    private int mSearchListPositionY = 0;
    private String mSearchRootDir = "";

    public void searchFile() {
        final Dialog dialog = new Dialog(mActivity, mGp.applicationTheme);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setSoftInputMode(LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        dialog.setContentView(R.layout.search_file_dlg);
        final LinearLayout dlg_view = (LinearLayout) dialog.findViewById(R.id.search_file_dlg_view);
//        dlg_view.setBackgroundResource(R.drawable.dialog_border_dark);

        final LinearLayout title_view = (LinearLayout) dialog.findViewById(R.id.search_file_dlg_title_view);
        title_view.setBackgroundColor(mGp.themeColorList.title_background_color);
        final TextView dlg_title = (TextView) dialog.findViewById(R.id.search_file_dlg_title);
        dlg_title.setTextColor(mGp.themeColorList.title_text_color);

        final ImageButton ib_sort = (ImageButton) dialog.findViewById(R.id.search_file_dlg_sort_btn);
//        ib_sort.setBackgroundColor(mGp.themeColorList.dialog_title_background_color);
//		final TextView dlg_msg = (TextView) dialog.findViewById(R.id.search_file_dlg_msg);
        final CheckedTextView dlg_hidden = (CheckedTextView) dialog.findViewById(R.id.search_file_dlg_search_hidden_item);
        CommonUtilities.setCheckedTextView(dlg_hidden);
        final CheckedTextView dlg_case_sensitive = (CheckedTextView) dialog.findViewById(R.id.search_file_dlg_search_case_sensitive);
        CommonUtilities.setCheckedTextView(dlg_case_sensitive);

        final Button btnOk = (Button) dialog.findViewById(R.id.search_file_dlg_ok_btn);
        final Button btnCancel = (Button) dialog.findViewById(R.id.search_file_dlg_cancel_btn);
        final EditText et_search_key = (EditText) dialog.findViewById(R.id.search_file_dlg_search_key);
        final ListView lv_search_result = (ListView) dialog.findViewById(R.id.search_file_dlg_search_result);

        final TextView searcgh_info = (TextView) dialog.findViewById(R.id.search_file_dlg_search_info);

        ib_sort.setBackgroundColor(Color.argb(255,32,32,32));
        if (mAdapterSearchFileList == null) {
            mAdapterSearchFileList = new AdapterSearchFileList(mActivity);
            lv_search_result.setAdapter(mAdapterSearchFileList);
        } else {
            if (!mMainFilePath.equals(mSearchRootDir)) {
                mAdapterSearchFileList = new AdapterSearchFileList(mActivity);
                lv_search_result.setAdapter(mAdapterSearchFileList);
            } else {
                lv_search_result.setAdapter(mAdapterSearchFileList);
                lv_search_result.setSelectionFromTop(mSearchListPositionX, mSearchListPositionY);
                if (mAdapterSearchFileList.isSortAscendant()) ib_sort.setImageResource(R.drawable.ic_128_sort_asc_gray);
                else ib_sort.setImageResource(R.drawable.ic_128_sort_dsc_gray);
                long s_size = 0;
                for (TreeFilelistItem tfi : mAdapterSearchFileList.getDataList())
                    s_size += tfi.getLength();
                String msg = mContext.getString(R.string.msgs_search_file_dlg_search_result);
                searcgh_info.setText(String.format(msg, mAdapterSearchFileList.getDataList().size(), s_size));
            }
        }

        CommonDialog.setDlgBoxSizeLimit(dialog, true);

        ib_sort.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                final CustomTreeFilelistAdapter tfa = new CustomTreeFilelistAdapter(mActivity, false, false);
                NotifyEvent ntfy_sort = new NotifyEvent(mContext);
                ntfy_sort.setListener(new NotifyEventListener() {
                    @Override
                    public void positiveResponse(Context c, Object[] o) {
                        if (tfa.isSortAscendant()) mAdapterSearchFileList.setSortAscendant();
                        else mAdapterSearchFileList.setSortDescendant();
                        if (tfa.isSortKeyName()) mAdapterSearchFileList.setSortKeyName();
                        else if (tfa.isSortKeySize()) mAdapterSearchFileList.setSortKeySize();
                        else if (tfa.isSortKeyTime()) mAdapterSearchFileList.setSortKeyTime();
                        mAdapterSearchFileList.sort();
                        mAdapterSearchFileList.notifyDataSetChanged();
                        if (mAdapterSearchFileList.isSortAscendant()) ib_sort.setImageResource(R.drawable.ic_128_sort_asc_gray);
                        else ib_sort.setImageResource(R.drawable.ic_128_sort_dsc_gray);
                    }

                    @Override
                    public void negativeResponse(Context c, Object[] o) {
                    }
                });
                if (mAdapterSearchFileList.isSortAscendant()) tfa.setSortAscendant();
                else tfa.setSortDescendant();
                if (mAdapterSearchFileList.isSortKeyName()) tfa.setSortKeyName();
                else if (mAdapterSearchFileList.isSortKeySize()) tfa.setSortKeySize();
                else if (mAdapterSearchFileList.isSortKeyTime()) tfa.setSortKeyTime();
                CommonUtilities.sortFileList(mActivity, mGp, tfa, ntfy_sort);
            }
        });

        et_search_key.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() > 0) CommonDialog.setButtonEnabled(mActivity, btnOk, true);
                else CommonDialog.setButtonEnabled(mActivity, btnOk, false);
            }
        });

        lv_search_result.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                TreeFilelistItem tfi = mAdapterSearchFileList.getItem(position);
                openSppecificDirectory(tfi.getPath(), tfi.getName());
                mSearchListPositionX = lv_search_result.getFirstVisiblePosition();
                mSearchListPositionY = lv_search_result.getChildAt(0) == null ? 0 : lv_search_result.getChildAt(0).getTop();
                btnCancel.performClick();
            }
        });

        lv_search_result.setOnItemLongClickListener(new OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                final CustomTreeFilelistAdapter n_tfa = new CustomTreeFilelistAdapter(mActivity, false, false, false);
                ArrayList<TreeFilelistItem> n_tfl = new ArrayList<TreeFilelistItem>();
                final TreeFilelistItem n_tfli = mAdapterSearchFileList.getItem(position).clone();
                n_tfli.setChecked(true);
                n_tfl.add(n_tfli);
                n_tfa.setDataList(n_tfl);

                final CustomContextMenu mCcMenu = new CustomContextMenu(mContext.getResources(), mFragmentManager);

                mCcMenu.addMenuItem(mContext.getString(R.string.msgs_main_local_file_ccmenu_top), R.drawable.context_button_top)
                        .setOnClickListener(new CustomContextMenuOnClickListener() {
                            @Override
                            public void onClick(CharSequence menuTitle) {
                                lv_search_result.setSelection(0);
                            }
                        });
                mCcMenu.addMenuItem(mContext.getString(R.string.msgs_main_local_file_ccmenu_bottom), R.drawable.context_button_bottom)
                        .setOnClickListener(new CustomContextMenuOnClickListener() {
                            @Override
                            public void onClick(CharSequence menuTitle) {
                                lv_search_result.setSelection(lv_search_result.getCount());
                            }
                        });

                mCcMenu.addMenuItem(mContext.getString(R.string.msgs_main_local_file_ccmenu_open_file) + "(" + (n_tfl.get(0)).getName() + ")")
                        .setOnClickListener(new CustomContextMenuOnClickListener() {
                            @Override
                            public void onClick(CharSequence menuTitle) {
                                invokeBrowser(n_tfli.getPath(), n_tfli.getName(), "");
                                btnCancel.performClick();
                            }
                        });
                mCcMenu.addMenuItem(mContext.getString(R.string.msgs_main_local_file_ccmenu_force_zip) + "(" + (n_tfl.get(0)).getName() + ")", R.drawable.ic_32_file_zip)
                        .setOnClickListener(new CustomContextMenuOnClickListener() {
                            @Override
                            public void onClick(CharSequence menuTitle) {
                                invokeBrowser(n_tfli.getPath(), n_tfli.getName(), MIME_TYPE_ZIP);
                                btnCancel.performClick();
                            }
                        });
                mCcMenu.addMenuItem(mContext.getString(R.string.msgs_main_local_file_ccmenu_force_text) + "(" + (n_tfl.get(0)).getName() + ")", R.drawable.cc_sheet)
                        .setOnClickListener(new CustomContextMenuOnClickListener() {
                            @Override
                            public void onClick(CharSequence menuTitle) {
                                invokeBrowser(n_tfli.getPath(), n_tfli.getName(), MIME_TYPE_TEXT);
                                btnCancel.performClick();
                            }
                        });
                mCcMenu.createMenu();

                mSearchListPositionX = lv_search_result.getFirstVisiblePosition();
                mSearchListPositionY = lv_search_result.getChildAt(0) == null ? 0 : lv_search_result.getChildAt(0).getTop();

                return true;
            }
        });

        //OK button
        et_search_key.setText(mFindKey);
        btnOk.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mFindKey = et_search_key.getText().toString();
                final ArrayList<TreeFilelistItem> s_tfl = new ArrayList<TreeFilelistItem>();
                int flags = 0;//Pattern.CASE_INSENSITIVE;// | Pattern..MULTILINE;
                if (!dlg_case_sensitive.isChecked()) flags = Pattern.CASE_INSENSITIVE;
                final Pattern s_key = Pattern.compile("(" + MiscUtil.convertRegExp(mFindKey) + ")", flags);
                final ThreadCtrl tc = new ThreadCtrl();
                final ProgressSpinDialogFragment psd = ProgressSpinDialogFragment.newInstance(
                        mContext.getString(R.string.msgs_search_file_dlg_searching), "",
                        mContext.getString(R.string.msgs_common_dialog_cancel),
                        mContext.getString(R.string.msgs_common_dialog_canceling));

                NotifyEvent ntfy = new NotifyEvent(mContext);
                ntfy.setListener(new NotifyEventListener() {
                    @Override
                    public void positiveResponse(Context c, Object[] o) {
                    }

                    @Override
                    public void negativeResponse(Context c, Object[] o) {
                        tc.setDisabled();
                        if (checkCancel(tc)) psd.dismissAllowingStateLoss();
                    }
                });
                psd.showDialog(mFragmentManager, psd, ntfy, true);
                Thread th = new Thread() {
                    @Override
                    public void run() {
                        SafFile3 sf=new SafFile3(mContext, mMainFilePath);
                        ContentProviderClient cpc=sf.getContentProviderClient();
                        try {
                            buildFileListBySearchKey(cpc, tc, dlg_hidden.isChecked(), psd, s_tfl, s_key, sf);
                        } finally {
                            if (cpc!=null) cpc.release();
                        }
                        psd.dismissAllowingStateLoss();
                        if (checkCancel(tc)) {
                            mCommonDlg.showCommonDialog(false, "W",
                                    mContext.getString(R.string.msgs_search_file_dlg_search_cancelled), "", null);
                        } else {
                            mAdapterSearchFileList.setDataList(s_tfl);
                            mSearchRootDir = mLocalStorageSelector.getSelectedItem().toString();
                            mUiHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    long s_size = 0;
                                    for (TreeFilelistItem tfi : mAdapterSearchFileList.getDataList())
                                        s_size += tfi.getLength();
                                    String msg = mContext.getString(R.string.msgs_search_file_dlg_search_result);
                                    searcgh_info.setText(String.format(msg, mAdapterSearchFileList.getDataList().size(), s_size));
                                    mAdapterSearchFileList.notifyDataSetChanged();
                                }
                            });
                        }
                    }
                };
                th.setPriority(Thread.MIN_PRIORITY);
                th.start();
            }
        });
        btnCancel.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();

    }

    private void buildFileListBySearchKey(ContentProviderClient cpc, final ThreadCtrl tc, boolean search_hidden_item,
                                          ProgressSpinDialogFragment psd,
                                          ArrayList<TreeFilelistItem> s_tfl, Pattern s_key, SafFile3 s_file) {
//		mUtil.addDebugMsg(1, "I", CommonUtilities.getExecutedMethodName()+" entered");
        boolean ignore = false;
        if (s_file.isHidden()) {
            if (search_hidden_item) {
                ignore = false;
            } else {
                ignore = true;
            }
        } else {
            ignore = false;
        }
        if (!ignore) {
            if (s_file.isDirectory(cpc)) {
                psd.updateMsgText(s_file.getPath());
                SafFile3[] fl = s_file.listFiles(cpc);
                if (fl != null) {
                    for (SafFile3 item : fl) {
                        if (checkCancel(tc)) break;
                        buildFileListBySearchKey(cpc, tc, search_hidden_item, psd, s_tfl, s_key, item);
                    }
                }
                if (checkCancel(tc)) return;
            } else {
                if (checkCancel(tc)) return;
                if (s_key.matcher(s_file.getName()).matches()) {
                    TreeFilelistItem tfli = createSafApiFileListItem(cpc, s_file);
                    s_tfl.add(tfli);
                }
            }
        }
    }

    private void createItem(CustomTreeFilelistAdapter tfa, final String c_dir) {
        mUtil.addDebugMsg(1,"I",CommonUtilities.getExecutedMethodName()+" entered");
        final Dialog dialog = new Dialog(mActivity, mGp.applicationTheme);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.single_item_input_dlg);
        final LinearLayout title_view = (LinearLayout) dialog.findViewById(R.id.single_item_input_title_view);
        title_view.setBackgroundColor(mGp.themeColorList.title_background_color);
        final TextView dlg_title = (TextView) dialog.findViewById(R.id.single_item_input_title);
        dlg_title.setTextColor(mGp.themeColorList.title_text_color);
        dlg_title.setText(mContext.getString(R.string.msgs_file_select_edit_dlg_create));
        final TextView dlg_msg = (TextView) dialog.findViewById(R.id.single_item_input_msg);
        final TextView dlg_cmp = (TextView) dialog.findViewById(R.id.single_item_input_name);
        final Button btnOk = (Button) dialog.findViewById(R.id.single_item_input_ok_btn);
        final Button btnCancel = (Button) dialog.findViewById(R.id.single_item_input_cancel_btn);
        final EditText etDir = (EditText) dialog.findViewById(R.id.single_item_input_dir);
        final CheckedTextView create_type = (CheckedTextView) dialog.findViewById(R.id.single_item_input_type);
        create_type.setVisibility(CheckedTextView.VISIBLE);
        setCheckedTextView(create_type);
        create_type.setText(mContext.getString(R.string.msgs_file_select_edit_dlg_dir_create_file));
        create_type.setChecked(false);
        final String t_dir = c_dir.endsWith("/") ? c_dir : c_dir + "/";
        dlg_cmp.setText(mContext.getString(R.string.msgs_file_select_edit_parent_directory) + t_dir);
        CommonDialog.setDlgBoxSizeCompactWithInput(dialog);
        etDir.setText("");
        CommonDialog.setButtonEnabled(mActivity, btnOk, false);
        etDir.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() > 0) {
                    File lf = new File(t_dir + s.toString());
//					Log.v("","fp="+lf.getPath());
                    if (lf.exists()) {
                        CommonDialog.setButtonEnabled(mActivity, btnOk, false);
                        dlg_msg.setText(mContext.getString(R.string.msgs_single_item_input_dlg_duplicate_dir));
                    } else {
                        CommonDialog.setButtonEnabled(mActivity, btnOk, true);
                        dlg_msg.setText("");
                    }
                }
            }
        });

        //OK button
        btnOk.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
//				NotifyEvent
                final String creat_dir = etDir.getText().toString();
                final String n_path = t_dir + creat_dir;
                NotifyEvent ntfy = new NotifyEvent(mContext);
                ntfy.setListener(new NotifyEventListener() {
                    @Override
                    public void positiveResponse(Context c, Object[] o) {
                        boolean rc_create = false;
                        SafFile3 sf=new SafFile3(mContext, n_path);
                        if (create_type.isChecked()) {
                            try {
                                rc_create = sf.createNewFile();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        } else {
                            rc_create=sf.mkdirs();
                        }
                        if (!rc_create) {
                            dlg_msg.setText(String.format(
                                    mContext.getString(R.string.msgs_file_select_edit_dlg_dir_not_created),
                                    etDir.getText()));
                            return;
                        }
                        refreshFileList();
                        dialog.dismiss();
//                        mCommonDlg.showCommonDialog(false, "I",
//                                String.format(mContext.getString(R.string.msgs_file_select_edit_dlg_dir_created), n_path), "", null);
                        showToast(mActivity, mContext.getString(R.string.msgs_file_select_edit_dlg_dir_created, n_path));
                    }

                    @Override
                    public void negativeResponse(Context c, Object[] o) {
                    }
                });
                if (create_type.isChecked())
                    mCommonDlg.showCommonDialog(true, "W", mContext.getString(R.string.msgs_file_select_edit_confirm_create_file), n_path, ntfy);
                else
                    mCommonDlg.showCommonDialog(true, "W", mContext.getString(R.string.msgs_file_select_edit_confirm_create_directory), n_path, ntfy);
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.setCancelable(false);
        dialog.show();

    }

    private void renameItem(CustomTreeFilelistAdapter tfa) {
        mUtil.addDebugMsg(1,"I",CommonUtilities.getExecutedMethodName()+" entered");
        final Dialog dialog = new Dialog(mActivity, mGp.applicationTheme);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.single_item_input_dlg);
        final LinearLayout dlg_view = (LinearLayout) dialog.findViewById(R.id.single_item_input_dlg_view);
//        dlg_view.setBackgroundResource(R.drawable.dialog_border_dark);

        final LinearLayout title_view = (LinearLayout) dialog.findViewById(R.id.single_item_input_title_view);
        title_view.setBackgroundColor(mGp.themeColorList.title_background_color);
        final TextView dlg_title = (TextView) dialog.findViewById(R.id.single_item_input_title);
        dlg_title.setTextColor(mGp.themeColorList.title_text_color);
        dlg_title.setText(mContext.getString(R.string.msgs_zip_local_file_rename_title));
        final TextView dlg_msg = (TextView) dialog.findViewById(R.id.single_item_input_msg);
        final TextView dlg_cmp = (TextView) dialog.findViewById(R.id.single_item_input_name);
        final Button btnOk = (Button) dialog.findViewById(R.id.single_item_input_ok_btn);
        final Button btnCancel = (Button) dialog.findViewById(R.id.single_item_input_cancel_btn);
        final EditText etDir = (EditText) dialog.findViewById(R.id.single_item_input_dir);
        dlg_cmp.setVisibility(TextView.GONE);

        TreeFilelistItem w_tfli = null;
        for (TreeFilelistItem item : tfa.getDataList()) {
            if (item.isChecked()) {
                w_tfli = item;
                break;
            }
        }
        final TreeFilelistItem tfli = w_tfli;

        CommonDialog.setDlgBoxSizeCompactWithInput(dialog);
        CommonDialog.setButtonEnabled(mActivity, btnOk, false);
        etDir.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() > 0) {
                    File lf = new File(tfli.getPath() + "/" + s.toString());
//					Log.v("","fp="+lf.getPath());
                    if (lf.exists()) {
                        CommonDialog.setButtonEnabled(mActivity, btnOk, false);
                        dlg_msg.setText(mContext.getString(R.string.msgs_single_item_input_dlg_duplicate_dir));
                    } else {
                        CommonDialog.setButtonEnabled(mActivity, btnOk, true);
                        dlg_msg.setText("");
                    }
                }
            }

        });
        etDir.setText(tfli.getName());

        //OK button
        btnOk.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                final String new_name = tfli.getPath() + "/" + etDir.getText().toString();
                final String current_name = tfli.getPath() + "/" + tfli.getName();
                NotifyEvent ntfy = new NotifyEvent(mContext);
                ntfy.setListener(new NotifyEventListener() {
                    @Override
                    public void positiveResponse(Context c, Object[] o) {
                        setUiDisabled();
                        Thread th = new Thread() {
                            @Override
                            public void run() {
                                mUtil.addDebugMsg(1, "I", "Rename started");
                                boolean rc_rename = false;
                                String e_msg_tmp="";
                                SafFile3 cf=new SafFile3(mContext, current_name);
                                SafFile3 nf=new SafFile3(mContext, new_name);
                                try {
                                    rc_rename=renameSafFile(cf, nf);
                                } catch(Exception e) {
                                    e.printStackTrace();
                                    e_msg_tmp=MiscUtil.getStackTraceString(e);
                                }
                                if (!rc_rename) {
                                    final String e_msg=e_msg_tmp;
                                    mUiHandler.post(new Runnable(){
                                        @Override
                                        public void run() {
                                            mCommonDlg.showCommonDialog(false, "I",
                                                    String.format(mContext.getString(R.string.msgs_zip_local_file_rename_failed), new_name), e_msg, null);
                                            setUiEnabled();
                                        }
                                    });
                                    return;
                                }
                                scanMediaFile(mGp, mUtil, nf.getPath());
                                mUtil.addDebugMsg(1, "I", "Rename ended");

                                mUiHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        showToast(mActivity, mContext.getString(R.string.msgs_zip_local_file_rename_completed, new_name));
                                        mGp.copyCutList.clear();
                                        mGp.copyCutType = GlobalParameters.COPY_CUT_FROM_LOCAL;
                                        mGp.copyCutItemInfo.setVisibility(TextView.GONE);
                                        mGp.copyCutItemClear.setVisibility(Button.GONE);
                                        mContextButtonPasteView.setVisibility(ImageButton.INVISIBLE);
                                        refreshFileList();
                                        setUiEnabled();
                                    }
                                });
                            }
                        };
                        th.start();
                    }

                    @Override
                    public void negativeResponse(Context c, Object[] o) {
                    }
                });
                mCommonDlg.showCommonDialog(true, "W", mContext.getString(R.string.msgs_zip_local_file_rename_confirm_title),
                        current_name, ntfy);
                dialog.dismiss();
            }
        });
        btnCancel.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.show();

    }

    private void copyItem(CustomTreeFilelistAdapter tfa) {
        if (tfa.isItemSelected()) {
            mGp.copyCutModeIsCut = false;
            mGp.copyCutList.clear();
            mGp.copyCutType = GlobalParameters.COPY_CUT_FROM_LOCAL;
            mGp.copyCutFilePath = mMainFilePath;
            mGp.copyCutCurrentDirectory = mCurrentDirectory.getText().equals("/") ? "" : (mCurrentDirectory.getText().length() == 0 ? "" : mCurrentDirectory.getText().toString().substring(1));
            String c_list = "", sep = "";
            for (TreeFilelistItem tfl : tfa.getDataList()) {
                if (tfl.isChecked()) {
                    mGp.copyCutList.add(tfl);
                    c_list += sep + tfl.getPath().replace(mLocalStorageSelector.getSelectedItem().toString(), "") + "/" + tfl.getName();
                    sep = ", ";
                    tfl.setChecked(false);
                }
            }
            tfa.notifyDataSetChanged();
            String from = mGp.copyCutType.equals(GlobalParameters.COPY_CUT_FROM_LOCAL) ? "Local" : "ZIP";
            mGp.copyCutItemInfo.setText(mContext.getString(R.string.msgs_zip_cont_header_copy) + " " + from + ":" + c_list);
            mGp.copyCutItemInfo.setVisibility(TextView.VISIBLE);
            mGp.copyCutItemClear.setVisibility(Button.VISIBLE);
            mContextButtonPasteView.setVisibility(ImageButton.INVISIBLE);
            mNotifyCheckBoxChanged.notifyToListener(false, null);
            mUtil.addDebugMsg(1,"I","copyItem copy="+mGp.copyCutItemInfo.getText().toString());
        }
    }

    private void cutItem(CustomTreeFilelistAdapter tfa) {
        if (tfa.isItemSelected()) {
            mGp.copyCutModeIsCut = true;
            mGp.copyCutList.clear();
            mGp.copyCutType = GlobalParameters.COPY_CUT_FROM_LOCAL;
            mGp.copyCutFilePath = mMainFilePath;
//			mGp.copyCutCurrentDirectory=mCurrentDirectory.getText().equals("/")?"":mCurrentDirectory.getText().substring(1);
            mGp.copyCutCurrentDirectory = mCurrentDirectory.getText().equals("/") ? "" : (mCurrentDirectory.getText().length() == 0 ? "" : mCurrentDirectory.getText().toString().substring(1));
            String c_list = "", sep = "";
            for (TreeFilelistItem tfl : tfa.getDataList()) {
                if (tfl.isChecked()) {
                    mGp.copyCutList.add(tfl);
                    c_list += sep + tfl.getPath().replace(mLocalStorageSelector.getSelectedItem().toString(), "") + "/" + tfl.getName();
                    sep = ", ";
                    tfl.setChecked(false);
                }
            }
            tfa.notifyDataSetChanged();
            String from = mGp.copyCutType.equals(GlobalParameters.COPY_CUT_FROM_LOCAL) ? "Local" : "ZIP";
            mGp.copyCutItemInfo.setText(mContext.getString(R.string.msgs_zip_cont_header_cut) + " " + from + ":" + c_list);
            mGp.copyCutItemInfo.setVisibility(TextView.VISIBLE);
            mGp.copyCutItemClear.setVisibility(Button.VISIBLE);
            mNotifyCheckBoxChanged.notifyToListener(false, null);
            mUtil.addDebugMsg(1,"I","copyItem cut="+mGp.copyCutItemInfo.getText().toString());
        }
        mContextButtonPasteView.setVisibility(ImageButton.INVISIBLE);
    }

    private boolean isCopyCutDestValid(String fp) {
        boolean enabled = true;
        if (mGp.copyCutList.size() > 0) {
            if (mGp.copyCutType.equals(GlobalParameters.COPY_CUT_FROM_ZIP)) enabled = true;
            else {
                for (TreeFilelistItem s_item : mGp.copyCutList) {
                    if (s_item.getPath().startsWith(fp)) {
                        enabled = false;
                        break;
                    }
                }
            }
        } else {
            enabled = false;
        }
        mUtil.addDebugMsg(1, "I", CommonUtilities.getExecutedMethodName() + " exit, enabled=" + enabled);
        return enabled;
    }

    static public boolean checkCancelAndWait(final ThreadCtrl tc) {
        tc.writeLockWait();
        return checkCancel(tc);
    }

    static public boolean checkCancel(final ThreadCtrl tc) {
        if (!tc.isEnabled()) {
            return true;
        }
        return false;
    }
    static public void confirmCancel(ActivityMain a, final ThreadCtrl tc, final Button cancel) {
        NotifyEvent ntfy = new NotifyEvent(a);
        ntfy.setListener(new NotifyEventListener() {
            @Override
            public void positiveResponse(Context c, Object[] o) {
                tc.setDisabled();
                cancel.setEnabled(false);
                tc.writeLockRelease();
            }

            @Override
            public void negativeResponse(Context c, Object[] o) {
                tc.writeLockRelease();
            }
        });
        MessageDialogFragment cdf =MessageDialogFragment.newInstance(true, "W", a.getString(R.string.msgs_main_confirm_cancel), "");
        cdf.showDialog(a.getSupportFragmentManager(),cdf,ntfy);

        tc.writeLockAcuire();
    }

    public void confirmCancel(final ThreadCtrl tc, final Button cancel) {
        confirmCancel(mActivity, tc, cancel);
    }

    private void confirmMove() {
        mConfirmResponse=0;
        if (mGp.copyCutType.equals(GlobalParameters.COPY_CUT_FROM_LOCAL)) confirmMoveFromLocal();
        else if (mGp.copyCutType.equals(GlobalParameters.COPY_CUT_FROM_ZIP)) confirmMoveFromZip();
    }

    private void confirmMoveFromZip() {
        final String to_dir = mMainFilePath;
        String w_conf_list = "", w_override_list="";
        String sep = "";
        for (TreeFilelistItem item : mGp.copyCutList) {
            w_conf_list += sep + item.getZipFileName();
            SafFile3 sf=new SafFile3(mContext, to_dir+"/"+item.getName());
            if (sf.exists()) w_override_list+=sep+item.getName();
            sep = "\n";
        }
        final String conf_list = w_conf_list;
        NotifyEvent ntfy_confirm = new NotifyEvent(mContext);
        ntfy_confirm.setListener(new NotifyEventListener() {
            @Override
            public void positiveResponse(Context c, Object[] o) {
                prepareExtractMultipleItem(to_dir, conf_list, true);
            }

            @Override
            public void negativeResponse(Context c, Object[] o) {
            }
        });
        mCommonDlg.showCommonDialog(true, "W", mContext.getString(R.string.msgs_zip_local_file_move_confirm_title), w_conf_list, ntfy_confirm);
    }

    private void confirmMoveFromLocal() {
        final String to_dir = mMainFilePath;
        NotifyEvent ntfy = new NotifyEvent(mContext);
        ntfy.setListener(new NotifyEventListener() {
            @Override
            public void positiveResponse(Context c, Object[] o) {
                setUiDisabled();
                showDialogProgress();
                final ThreadCtrl tc = new ThreadCtrl();
                mDialogProgressSpinCancel.setEnabled(true);
                mDialogProgressSpinCancel.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        confirmCancel(tc, mDialogProgressSpinCancel);
                    }
                });
                Thread th = new Thread() {
                    @Override
                    public void run() {
                        mUtil.addDebugMsg(1, "I", "Move started");
                        String moved_item = "", moved_sep = "";
                        boolean rc =false;
                        for (TreeFilelistItem tfl : mGp.copyCutList) {
                            SafFile3 from_file = new SafFile3(mContext, tfl.getPath() + "/" + tfl.getName());
                            SafFile3 to_file=new SafFile3(mContext, to_dir+"/"+tfl.getName());
                            rc = moveCopyLocalToLocal(true, tc, from_file, (to_dir + "/" + tfl.getName()).replace("//", "/"));
                            if (rc) {
                                moved_item += moved_sep + from_file;
                                moved_sep = ", ";
                            } else {
                                if (!checkCancelAndWait(tc)) {
                                    String msg = String.format(mContext.getString(R.string.msgs_zip_local_file_move_failed), tfl.getName());
                                    mUtil.addLogMsg("I", msg);
                                    mCommonDlg.showCommonDialog(false, "W", msg, "", null);
                                    break;
                                }
                            }
                        }
                        if (rc) {
                            showToast(mActivity, mContext.getString(R.string.msgs_zip_local_file_move_completed));
                        }
                        mUtil.addDebugMsg(1, "I", "Move ended");
                        mUiHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                mGp.copyCutList.clear();
                                mGp.copyCutType = GlobalParameters.COPY_CUT_FROM_LOCAL;
                                mGp.copyCutItemInfo.setVisibility(TextView.GONE);
                                mGp.copyCutItemClear.setVisibility(Button.GONE);
                                mContextButtonPasteView.setVisibility(ImageButton.INVISIBLE);
                                refreshFileList();
                                setUiEnabled();
                            }
                        });
                    }
                };
                th.start();
            }

            @Override
            public void negativeResponse(Context c, Object[] o) {
            }
        });
        String override_list = "", sep_override = "", move_list="", sep_move="";
        for (TreeFilelistItem tfl : mGp.copyCutList) {
            SafFile3 sf=new SafFile3(mContext, to_dir+"/"+tfl.getName());
            if (sf.exists()) {
                override_list += sep_override + tfl.getName();
                sep_override = ",";
            }
            move_list+=sep_move+tfl.getName();
        }
        mCommonDlg.showCommonDialog(true, "W", mContext.getString(R.string.msgs_zip_local_file_move_confirm_title), move_list, ntfy);
    }

    private boolean moveCopyLocalToLocal(boolean move, ThreadCtrl tc, SafFile3 from_file, String to_path) {
        mUtil.addDebugMsg(1, "I", CommonUtilities.getExecutedMethodName() + " from=" + from_file.getPath() + ", to=" + to_path);
        boolean result = false;
        if (from_file.isDirectory()) {
            if (checkCancelAndWait(tc)) {
                String msg = String.format(mContext.getString(R.string.msgs_zip_local_file_move_cancelled), to_path);
                mUtil.addLogMsg("I", msg);
                mCommonDlg.showCommonDialog(false, "W", msg, "", null);
                return false;
            }
            SafFile3[] fl = from_file.listFiles();
            if (fl != null && fl.length > 0) {
                for (SafFile3 item : fl) {
                    result = moveCopyLocalToLocal(move, tc, item, to_path + "/" + item.getName());
                    if (!result) break;
                    result = true;
                }
                if (move && result) {
                    SafFile3 out_sf = new SafFile3(mContext, from_file.getPath());
                    out_sf.deleteIfExists();
                }
            } else {
                SafFile3 out_sf = new SafFile3(mContext, from_file.getPath());
                out_sf.deleteIfExists();
                result = true;
            }
            SafFile3 lf = new SafFile3(mContext, to_path);
            lf.mkdirs();
        } else {
            SafFile3 lf=new SafFile3(mContext, to_path);
            boolean replace_granted=true;
            if (lf.exists()) {
                String title=move?"Move":"Copy";
                String msg=mContext.getString(R.string.msgs_zip_extract_file_confirm_replace_copy);
                replace_granted=confirmReplace(tc, title, msg, to_path);
            }
            if (replace_granted) {
                result=moveCopyFileLocalToLocal(move, tc, from_file, to_path);
            } else {
                //Reject replace request
                if (checkCancel(tc)) {
                    putProgressMessage(mContext.getString(R.string.msgs_zip_extract_file_was_not_replaced) + to_path);
                    mUtil.addLogMsg("I", mContext.getString(R.string.msgs_zip_extract_file_was_not_replaced) + to_path);
                    result = true;
                } else {
                    result = false;
                }
            }
        }
        return result;
    }

    private boolean moveCopyFileLocalToLocal(boolean move, ThreadCtrl tc, SafFile3 from_file, String to_path) {
        SafFile3 out_file = new SafFile3(mContext, to_path);
        if (out_file.getAppDirectoryCache()==null) return moveCopyFileLocalToLocalByRename(move, tc, from_file, to_path);
        else return moveCopyFileLocalToLocalByMove(move, tc, from_file, to_path);
    }

    private boolean moveCopyFileLocalToLocalByMove(boolean move, ThreadCtrl tc, SafFile3 from_file, String to_path) {
        boolean result=false;
        try {
            SafFile3 out_file = new SafFile3(mContext, to_path);
            OutputStream temp_out_stream=null;
            String tmp_file_name=out_file.getAppDirectoryCache()+"/"+from_file.getName();
            SafFile3 temp_out_file = new SafFile3(mContext, tmp_file_name);
            File temp_os_file=new File(tmp_file_name);
            OutputStream fos =new FileOutputStream(temp_os_file);
            InputStream fis = from_file.getInputStream();

            String msg_in_prog="",msg_comp="",msg_cancelled="";
            if (move) {
                msg_in_prog=mContext.getString(R.string.msgs_zip_local_file_move_moving);
                msg_comp=mContext.getString(R.string.msgs_zip_local_file_move_moved);
                msg_cancelled=mContext.getString(R.string.msgs_zip_local_file_move_cancelled);
            } else {
                msg_in_prog=mContext.getString(R.string.msgs_zip_local_file_copy_copying);
                msg_comp=mContext.getString(R.string.msgs_zip_local_file_copy_copied);
                msg_cancelled=mContext.getString(R.string.msgs_zip_local_file_copy_cancelled);
            }

            result= writeBackZipFile(fis, fos, tc, from_file, to_path, msg_in_prog);

            if (checkCancelAndWait(tc)) {
                out_file.deleteIfExists();
                String msg = String.format(msg_cancelled, to_path);
                mUtil.addLogMsg("I", msg);
                mCommonDlg.showCommonDialog(false, "W", msg, "", null);
                result = false;
            } else {
                result = true;
                SafFile3 d_lf =out_file.getParentFile();
                d_lf.mkdirs();

                long in_last_mod = from_file.lastModified();
                temp_os_file.setLastModified(in_last_mod);

                out_file.deleteIfExists();
                result=temp_out_file.moveTo(out_file);
                scanMediaFile(mGp, mUtil, out_file.getPath());

                if (move && result) from_file.deleteIfExists();
                String msg = String.format(msg_comp, to_path);
                mUtil.addLogMsg("I", msg);
                putProgressMessage(msg);
            }
        } catch(Exception e) {
            e.printStackTrace();
            tc.setThreadMessage(MiscUtil.getStackTraceString(e));
        }
        return result;
    }

    private boolean writeBackZipFile(InputStream fis, OutputStream fos, ThreadCtrl tc, SafFile3 from_file, String to_path, String msg_in_prog) throws Exception {
        boolean result=true;
        byte[] buff = new byte[IO_AREA_SIZE];
        int rc = fis.read(buff);
        long file_size = from_file.length();
        long progress = 0, tot_rc = 0;
        while (rc > 0) {
            if (checkCancelAndWait(tc)) {
                result=false;
                break;
            } else {
                fos.write(buff, 0, rc);
                tot_rc += rc;
                progress = ((tot_rc * 100) / file_size);
                String msg = String.format(msg_in_prog, to_path, progress);
                putProgressMessage(msg);
                rc = fis.read(buff);
            }
        }
        fos.flush();
        fos.close();
        fis.close();
        return result;
    }

    private boolean moveCopyFileLocalToLocalByRename(boolean move, ThreadCtrl tc, SafFile3 from_file, String to_path) {
        boolean result=false;
        try {
            SafFile3 out_file = new SafFile3(mContext, to_path);
            SafFile3 d_lf =out_file.getParentFile();
            d_lf.mkdirs();
            OutputStream temp_out_stream=null;
            String tmp_file_name=to_path+".work.tmp";
            SafFile3 temp_out_file = new SafFile3(mContext, tmp_file_name);
            temp_out_file.createNewFile();
            OutputStream fos =temp_out_file.getOutputStream();
            InputStream fis = from_file.getInputStream();

            String msg_in_prog="",msg_comp="",msg_cancelled="";
            if (move) {
                msg_in_prog=mContext.getString(R.string.msgs_zip_local_file_move_moving);
                msg_comp=mContext.getString(R.string.msgs_zip_local_file_move_moved);
                msg_cancelled=mContext.getString(R.string.msgs_zip_local_file_move_cancelled);
            } else {
                msg_in_prog=mContext.getString(R.string.msgs_zip_local_file_copy_copying);
                msg_comp=mContext.getString(R.string.msgs_zip_local_file_copy_copied);
                msg_cancelled=mContext.getString(R.string.msgs_zip_local_file_copy_cancelled);
            }
            result= writeBackZipFile(fis, fos, tc, from_file, to_path, msg_in_prog);
            if (checkCancel(tc)) {
                temp_out_file.deleteIfExists();
                String msg = String.format(msg_cancelled, to_path);
                mUtil.addLogMsg("I", msg);
                mCommonDlg.showCommonDialog(false, "W", msg, "", null);
                result = false;
            } else {
                result = true;
                out_file.deleteIfExists();
                try {
                    result=renameSafFile(temp_out_file, out_file);
                    scanMediaFile(mGp, mUtil, out_file.getPath());
                } catch(Exception e) {
                    e.printStackTrace();
                    mUtil.addLogMsg("I", "rename error occured, error="+MiscUtil.getStackTraceString(e));
                    putProgressMessage("rename error occured, error="+e.getMessage());
                    tc.setThreadMessage(MiscUtil.getStackTraceString(e));
                    return false;
                }

                if (move && result) from_file.deleteIfExists();
                String msg = String.format(msg_comp, to_path);
                mUtil.addLogMsg("I", msg);
                putProgressMessage(msg);
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    private void pasteItem() {
        if (mGp.copyCutList.size() > 0) {
            if (mGp.copyCutModeIsCut) confirmMove();
            else confirmCopy();
        }
    }

    private void confirmCopy() {
        mConfirmResponse=0;
        if (mGp.copyCutType.equals(GlobalParameters.COPY_CUT_FROM_LOCAL)) confirmCopyFromLocal();
        else if (mGp.copyCutType.equals(GlobalParameters.COPY_CUT_FROM_ZIP)) confirmCopyFromZip();
    }

    private void confirmCopyFromZip() {
        mUtil.addDebugMsg(1,"I",CommonUtilities.getExecutedMethodName()+" entered");
        final String to_dir = mMainFilePath;
        String w_conf_list = "";
        String w_override_list = "";
        String sep = "";
        for (TreeFilelistItem item : mGp.copyCutList) {
            w_conf_list += sep + item.getZipFileName();
            SafFile3 sf=new SafFile3(mContext, to_dir+"/"+item.getName());
            if (sf.exists()) w_override_list+=sep+item.getName();
            sep = "\n";
        }
        final String conf_list = w_conf_list;
        NotifyEvent ntfy_confirm = new NotifyEvent(mContext);
        ntfy_confirm.setListener(new NotifyEventListener() {
            @Override
            public void positiveResponse(Context c, Object[] o) {
                prepareExtractMultipleItem(to_dir, conf_list, false);
            }

            @Override
            public void negativeResponse(Context c, Object[] o) {
            }
        });
        mCommonDlg.showCommonDialog(true, "W",
                String.format(mContext.getString(R.string.msgs_zip_local_file_copy_confirm_title), to_dir), w_conf_list, ntfy_confirm);
    }

    private void prepareExtractMultipleItem(final String dest_path, final String conf_list, final boolean move_mode) {
        mConfirmResponse = 0;

        setUiDisabled();
        showDialogProgress();
        final ThreadCtrl tc = new ThreadCtrl();
        mDialogProgressSpinCancel.setEnabled(true);
        mDialogProgressSpinCancel.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                confirmCancel(tc, mDialogProgressSpinCancel);
            }
        });
        Thread th = new Thread() {
            @Override
            public void run() {
                mUtil.addDebugMsg(1, "I", "Extract started");
                putProgressMessage(mContext.getString(R.string.msgs_zip_extract_file_started));

                final CustomZipFile zf = ZipFileManager.createZipFile(mContext, mGp.copyCutFilePath, mGp.copyCutEncoding);
                ArrayList<FileHeader> zf_fhl = null;
                try {
                    zf_fhl = (ArrayList<FileHeader>) zf.getFileHeaders();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                ArrayList<FileHeader> sel_fhl = new ArrayList<FileHeader>();
                for (FileHeader fh_item : zf_fhl) {
                    for (TreeFilelistItem sel_tfli : mGp.copyCutList) {
                        if (sel_tfli.isDirectory()) {
                            if (fh_item.getFileName().startsWith(sel_tfli.getZipFileName() + "/")) {
                                sel_fhl.add(fh_item);
                                break;
                            }
                        } else {
                            if (sel_tfli.getZipFileName().equals(fh_item.getFileName())) {
                                sel_fhl.add(fh_item);
                                break;
                            }
                        }
                    }
                }
                ArrayList<FileHeader> ext_fhl = new ArrayList<FileHeader>();
                extractSelectedZipItem(tc, dest_path, zf, sel_fhl, ext_fhl, move_mode);
                mUtil.addDebugMsg(1, "I", "Extract exited");
            }
        };
        th.setName("extract");
        th.start();
    }

    private String mMainPassword = "";

    private boolean extractSelectedZipItem(final ThreadCtrl tc, final String dest_path, final CustomZipFile zf,
                                           final ArrayList<FileHeader> selected_fh_list, final ArrayList<FileHeader> extracted_fh_list,
                                           final boolean move_mode) {

        mUtil.addDebugMsg(2, "I", CommonUtilities.getExecutedMethodName() + " entered, size=" + selected_fh_list.size()+
                ", dest_path="+dest_path);
        ThreadCtrl tc_extract=new ThreadCtrl();
        try {
            while (selected_fh_list.size() > 0) {
                final FileHeader fh_item = selected_fh_list.get(0);
                mUtil.addDebugMsg(1, "I", "extract name="+fh_item.getFileName());
                selected_fh_list.remove(0);
                extracted_fh_list.add(fh_item);
                if (fh_item.isDirectory()) {
                    String fp=dest_path + "/" + fh_item.getFileName().replace(mGp.copyCutCurrentDirectory, "");
                    SafFile3 sf=new SafFile3(mContext, fp);
                    if (!sf.exists()) sf.mkdirs();
                } else {
					if (checkCancelAndWait(tc)) return true;
                    final NotifyEvent ntfy_pswd = new NotifyEvent(mContext);
                    ntfy_pswd.setListener(new NotifyEventListener() {
                        @Override
                        public void positiveResponse(Context c, Object[] o) {
                            if (o!=null && o[0]!=null) mMainPassword=(String)o[0];
                            boolean rc=extractSingleZipItem(tc, dest_path, zf, selected_fh_list, extracted_fh_list, fh_item);
                            if (!rc) {
                                tc_extract.setDisabled();
                                mUiHandler.post(new Runnable(){
                                    @Override
                                    public void run() {
                                        setUiEnabled();
                                        hideDialog();
                                        notifyResponse(tc_extract, fh_item.getFileName());
                                    }
                                });
                            } else {
                                mUiHandler.post(new Runnable(){
                                    @Override
                                    public void run() {
                                        notifyResponse(tc_extract, fh_item.getFileName());
                                    }
                                });
                            }
                        }
                        @Override
                        public void negativeResponse(Context c, Object[] o) {
                            tc_extract.setDisabled();
                            mUiHandler.post(new Runnable(){
                                @Override
                                public void run() {
                                    setUiEnabled();
                                    hideDialog();
                                    notifyResponse(tc_extract, fh_item.getFileName());
                                }
                            });
                        }
                    });

                    if (ZipUtil.isSupportedCompressionMethod(fh_item)) {
                        if (fh_item.isEncrypted()) {
                            if (!mMainPassword.isEmpty()) {
                                zf.setPassword(mMainPassword);
                                if (!ZipFileManager.isCorrectZipFilePassword(zf, fh_item, mMainPassword)) {
                                    mUiHandler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            ZipFileManager.getZipPasswordDlg(mActivity, mGp, mMainPassword, zf, fh_item, ntfy_pswd, true);
                                        }
                                    });
                                    waitResponse(tc_extract, fh_item.getFileName());
                                } else {
                                    ntfy_pswd.notifyToListener(true, null);
                                }
                            } else {
                                mUiHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        ZipFileManager.getZipPasswordDlg(mActivity, mGp, mMainPassword, zf, fh_item, ntfy_pswd, true);
                                    }
                                });
                                waitResponse(tc_extract, fh_item.getFileName());
                            }
                        } else {
                            ntfy_pswd.notifyToListener(true, null);
//                            waitResponse(tc_extract, fh_item.getFileName());
                        }
                    } else {
                        tc_extract.setDisabled();
                        CompressionMethod cm=ZipUtil.getCompressionMethod(fh_item);
                        mCommonDlg.showCommonDialog(false, "E", mContext.getString(R.string.msgs_zip_extract_file_error),
                                "Unsupported compression method. code="+ ZipUtil.getCompressionMethodName(fh_item), null);
                        notifyResponse(tc_extract, fh_item.getFileName());
                    }
                }
            }

            if (move_mode) {
                try {//Delete ZIP item for Move mode
                    SafFile3 out_temp=new SafFile3(mContext, zf.getSafFile().getPath()+".tmp");
                    out_temp.createNewFile();
                    BufferedZipFile3 bzf = new BufferedZipFile3(mContext, zf.getSafFile(), out_temp, mGp.copyCutEncoding);
                    String msg = mContext.getString(R.string.msgs_zip_delete_file_was_deleted);
                    for (FileHeader fh : extracted_fh_list) {
                        if (checkCancelAndWait(tc)) {
                            mCommonDlg.showCommonDialog(false, "I",
                                    String.format(mContext.getString(R.string.msgs_zip_delete_file_was_cancelled), fh.getFileName()), "", null);
                            break;
                        }
                        bzf.removeItem(fh);
                        putProgressMessage(String.format(msg, fh.getFileName()));
                    }
                    clearCopyCutItem();
                    if (!checkCancelAndWait(tc)) {
                        try {
                            CallBackListener cbl=getZipProgressCallbackListener(tc, bzf, mContext.getString(R.string.msgs_zip_zip_file_being_updated));
                            bzf.close(cbl);
                            zf.getSafFile().deleteIfExists();
                            renameSafFile(out_temp, zf.getSafFile());
                            scanMediaFile(mGp, mUtil, zf.getSafFile().getPath());
                            showToast(mActivity, mContext.getString(R.string.msgs_zip_move_file_completed));
                        } catch (Exception e) {
                            e.printStackTrace();
                            String e_msg = "Exception occured";
                            mUtil.addLogMsg("E", e_msg + ", " + e.getMessage());
                            mCommonDlg.showCommonDialog(false, "E", e_msg, e.getMessage(), null);
                            out_temp.deleteIfExists();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    String e_msg = "Exception occured";
                    mUtil.addLogMsg("E", e_msg + ", " + e.getMessage());
                    mCommonDlg.showCommonDialog(false, "E", e_msg, e.getMessage(), null);
                }
            }
            mUiHandler.post(new Runnable(){
                @Override
                public void run() {
                    setUiEnabled();
                    hideDialog();
                    refreshFileList(true);
                }
            });

        } catch (Exception e) {
            mUtil.addLogMsg("I", e.getMessage());
            CommonUtilities.printStackTraceElement(mUtil, e.getStackTrace());
            return false;
        }
        return true;
    }

    private boolean extractSelectedZipItemX(final ThreadCtrl tc, final String dest_path, final CustomZipFile zf,
                                           final ArrayList<FileHeader> selected_fh_list, final ArrayList<FileHeader> extracted_fh_list,
                                           final boolean move_mode) {

        mUtil.addDebugMsg(2, "I", CommonUtilities.getExecutedMethodName() + " entered, size=" + selected_fh_list.size()+
                ", dest_path="+dest_path);
        ThreadCtrl tc_extract=new ThreadCtrl();
        try {
            while (selected_fh_list.size() > 0) {
                final FileHeader fh_item = selected_fh_list.get(0);
                mUtil.addDebugMsg(1, "I", "extract name="+fh_item.getFileName());
                selected_fh_list.remove(0);
                extracted_fh_list.add(fh_item);
                if (fh_item.isDirectory()) {
                    String fp=dest_path + "/" + fh_item.getFileName().replace(mGp.copyCutCurrentDirectory, "");
                    SafFile3 sf=new SafFile3(mContext, fp);
                    if (!sf.exists()) sf.mkdirs();
                } else {
                    if (checkCancelAndWait(tc)) return true;
                    final NotifyEvent ntfy_pswd = new NotifyEvent(mContext);
                    ntfy_pswd.setListener(new NotifyEventListener() {
                        @Override
                        public void positiveResponse(Context c, Object[] o) {
                            if (o!=null && o[0]!=null) mMainPassword=(String)o[0];
                            boolean rc=extractSingleZipItem(tc, dest_path, zf, selected_fh_list, extracted_fh_list, fh_item);
                            if (!rc) {
                                tc_extract.setDisabled();
                                mUiHandler.post(new Runnable(){
                                    @Override
                                    public void run() {
//                                        Thread.dumpStack();
                                        setUiEnabled();
                                        hideDialog();
                                        notifyResponse(tc_extract, fh_item.getFileName());
                                    }
                                });
                            } else {
                                mUiHandler.post(new Runnable(){
                                    @Override
                                    public void run() {
//                                        Thread.dumpStack();
                                        notifyResponse(tc_extract, fh_item.getFileName());
                                    }
                                });
                            }
                        }
                        @Override
                        public void negativeResponse(Context c, Object[] o) {
                            tc_extract.setDisabled();
                            mUiHandler.post(new Runnable(){
                                @Override
                                public void run() {
//                                    Thread.dumpStack();
                                    setUiEnabled();
                                    hideDialog();
                                    notifyResponse(tc_extract, fh_item.getFileName());
                                }
                            });
                        }
                    });

                    if (ZipUtil.isSupportedCompressionMethod(fh_item)) {
                        if (fh_item.isEncrypted()) {
                            if (!mMainPassword.isEmpty()) {
                                zf.setPassword(mMainPassword);
                                if (!ZipFileManager.isCorrectZipFilePassword(zf, fh_item, mMainPassword)) {
                                    mUiHandler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            ZipFileManager.getZipPasswordDlg(mActivity, mGp, mMainPassword, zf, fh_item, ntfy_pswd, true);
                                        }
                                    });
                                    waitResponse(tc_extract, fh_item.getFileName());
                                } else {
                                    ntfy_pswd.notifyToListener(true, null);
//                                    waitResponse(tc_extract, fh_item.getFileName());
                                }
                            } else {
                                mUiHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        ZipFileManager.getZipPasswordDlg(mActivity, mGp, mMainPassword, zf, fh_item, ntfy_pswd, true);
                                    }
                                });
                                waitResponse(tc_extract, fh_item.getFileName());
                            }
                        } else {
                            ntfy_pswd.notifyToListener(true, null);
//                            waitResponse(tc_extract, fh_item.getFileName());
                        }
                    } else {
                        tc_extract.setDisabled();
                        CompressionMethod cm=ZipUtil.getCompressionMethod(fh_item);
                        mCommonDlg.showCommonDialog(false, "E", mContext.getString(R.string.msgs_zip_extract_file_error),
                                "Unsupported compression method. code="+ ZipUtil.getCompressionMethodName(fh_item), null);
                        notifyResponse(tc_extract, fh_item.getFileName());
                    }
                }
            }

            if (move_mode) {
                try {//Delete ZIP item for Move mode
                    SafFile3 out_temp=new SafFile3(mContext, zf.getSafFile().getPath()+".tmp");
                    out_temp.createNewFile();
                    BufferedZipFile3 bzf = new BufferedZipFile3(mContext, zf.getSafFile(), out_temp, mGp.copyCutEncoding);
                    String msg = mContext.getString(R.string.msgs_zip_delete_file_was_deleted);
                    for (FileHeader fh : extracted_fh_list) {
                        if (checkCancelAndWait(tc)) {
                            mCommonDlg.showCommonDialog(false, "I",
                                    String.format(mContext.getString(R.string.msgs_zip_delete_file_was_cancelled), fh.getFileName()), "", null);
                            break;
                        }
                        bzf.removeItem(fh);
                        putProgressMessage(String.format(msg, fh.getFileName()));
                    }
                    clearCopyCutItem();
                    if (!checkCancelAndWait(tc)) {
                        try {
                            CallBackListener cbl=getZipProgressCallbackListener(tc, bzf, mContext.getString(R.string.msgs_zip_zip_file_being_updated));
                            bzf.close(cbl);
                            zf.getSafFile().deleteIfExists();
                            renameSafFile(out_temp, zf.getSafFile());
                            scanMediaFile(mGp, mUtil, zf.getSafFile().getPath());
                            showToast(mActivity, mContext.getString(R.string.msgs_zip_move_file_completed));
                        } catch (Exception e) {
                            e.printStackTrace();
                            String e_msg = "Exception occured";
                            mUtil.addLogMsg("E", e_msg + ", " + e.getMessage());
                            mCommonDlg.showCommonDialog(false, "E", e_msg, e.getMessage(), null);
                            out_temp.deleteIfExists();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    String e_msg = "Exception occured";
                    mUtil.addLogMsg("E", e_msg + ", " + e.getMessage());
                    mCommonDlg.showCommonDialog(false, "E", e_msg, e.getMessage(), null);
                }
            }
            mUiHandler.post(new Runnable(){
                @Override
                public void run() {
                    setUiEnabled();
                    hideDialog();
                    refreshFileList(true);
                }
            });

        } catch (Exception e) {
            mUtil.addLogMsg("I", e.getMessage());
            CommonUtilities.printStackTraceElement(mUtil, e.getStackTrace());
            return false;
        }
        return true;
    }

    public static boolean renameSafFile(SafFile3 from, SafFile3 to) {
        boolean result=from.renameTo(to);
        if (!from.getPath().startsWith(SafFile3.SAF_FILE_PRIMARY_STORAGE_PREFIX)) {
            if (!result && !from.exists() && to.exists()) {
                result=true;
            }
        }
        return result;
    }

    private void clearCopyCutItem() {
        mUiHandler.post(new Runnable(){
            @Override
            public void run() {
                mGp.copyCutList.clear();
                mGp.copyCutType = GlobalParameters.COPY_CUT_FROM_LOCAL;
                mGp.copyCutItemInfo.setVisibility(TextView.GONE);
                mGp.copyCutItemClear.setVisibility(Button.GONE);
                mContextButtonPasteView.setVisibility(ImageButton.INVISIBLE);
            }
        });
    }

    private void waitResponse(Object o, String id) throws InterruptedException {
        mActivity.getZipFileManager().waitResponse(o, id);
    }

    private void notifyResponse(Object o, String id) {
        mActivity.getZipFileManager().notifyResponse(o, id);
    }

    private boolean extractSingleZipItem(final ThreadCtrl tc, final String dest_path, final CustomZipFile zf,
                                         final ArrayList<FileHeader> selected_fh_list, final ArrayList<FileHeader> extracted_fh_list,
                                         FileHeader fh_item) {
        String dir = "", fn = fh_item.getFileName();
        boolean result = true;
        if (fh_item.getFileName().lastIndexOf("/") > 0) {
            dir = "/"+fh_item.getFileName().substring(0, fh_item.getFileName().lastIndexOf("/")).replace(mGp.copyCutCurrentDirectory, "");
            fn = fh_item.getFileName().substring(fh_item.getFileName().lastIndexOf("/") + 1);
        }
        String file_path=(dest_path + dir + "/" + fn).replaceAll("//","/").replaceAll("//","/");
        File lf=new File(file_path);
        boolean replace_granted=true;
        if (lf.exists() && !lf.isDirectory()) {
            String msg=mContext.getString(R.string.msgs_zip_extract_file_confirm_replace_copy);
            replace_granted=confirmReplace(tc, "Extract from ZIP", msg, file_path);
        }
        if (replace_granted) {
            if (copyZipItemToFile(tc, zf, fh_item, fh_item.getFileName(), dest_path + dir, fn)) {
                if (!checkCancelAndWait(tc)) {
                    putProgressMessage(String.format(mContext.getString(R.string.msgs_zip_extract_file_was_extracted), fh_item.getFileName()));
                    mUtil.addLogMsg("I", String.format(mContext.getString(R.string.msgs_zip_extract_file_was_extracted), fh_item.getFileName()));
                } else {
                    result = false;
                    mUiHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            refreshFileList();
                            setUiEnabled();
                            hideDialog();
                        }
                    });
                }
            } else {
                result = false;
                mCommonDlg.showCommonDialog(false, "E", mContext.getString(R.string.msgs_zip_extract_file_was_failed), tc.getThreadMessage(), null);
                mUiHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        refreshFileList();
                        setUiEnabled();
                        hideDialog();
                    }
                });
            }
        } else {
            //Reject replace request
            if (!checkCancelAndWait(tc)) {
                putProgressMessage(mContext.getString(R.string.msgs_zip_extract_file_was_not_replaced) + dest_path + "/" + dir + "/" + fn);
                mUtil.addLogMsg("I", mContext.getString(R.string.msgs_zip_extract_file_was_not_replaced) + dest_path + "/" + dir + "/" + fn);
                mUiHandler.post(new Runnable() {
                    @Override
                    public void run() {
//                        setUiEnabled();
                        hideDialog();
                    }
                });
            }
        }
        if (checkCancelAndWait(tc)) {
            result = false;
            mCommonDlg.showCommonDialog(false, "W", mContext.getString(R.string.msgs_zip_extract_file_was_cancelled), "", null);
            mUiHandler.post(new Runnable() {
                @Override
                public void run() {
                    setUiEnabled();
                    closeUiDialogView(50);
//                    hideDialog();
                }
            });
        }
        return result;
    }

    private boolean copyZipItemToFile(ThreadCtrl tc, CustomZipFile zf, FileHeader fh, String zip_file_name,
                                      String dest_path, String dest_file_name) {
        boolean result = false;
        result=mActivity.getZipFileManager().copyZipItemToFile(tc, zf, fh, zip_file_name, dest_path, dest_file_name, mDialogProgressSpinMsg2);
        return result;
    }

    private void confirmCopyFromLocal() {
        mUtil.addDebugMsg(1,"I",CommonUtilities.getExecutedMethodName()+" entered");
        final String to_dir = mMainFilePath;
        NotifyEvent ntfy = new NotifyEvent(mContext);
        ntfy.setListener(new NotifyEventListener() {
            @Override
            public void positiveResponse(Context c, Object[] o) {
                setUiDisabled();
                showDialogProgress();
                final ThreadCtrl tc = new ThreadCtrl();
                mDialogProgressSpinCancel.setEnabled(true);
                mDialogProgressSpinCancel.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        confirmCancel(tc, mDialogProgressSpinCancel);
                    }
                });
                Thread th = new Thread() {
                    @Override
                    public void run() {
                        mUtil.addDebugMsg(1, "I", "Copy started");
                        String copied_item = "", copied_sep = "";
                        boolean rc =false;
                        for (TreeFilelistItem tfl : mGp.copyCutList) {
                            SafFile3 from_file = new SafFile3(mContext, tfl.getPath() + "/" + tfl.getName());
                            rc = moveCopyLocalToLocal(false, tc, from_file, to_dir + "/" + tfl.getName());
                            if (rc) {
                                String msg = String.format(mContext.getString(R.string.msgs_zip_local_file_copy_copied), tfl.getName());
                                mUtil.addLogMsg("I", msg);
                                putProgressMessage(msg);
                                copied_item += copied_sep + from_file.getName();
                            } else {
                                if (!checkCancelAndWait(tc)) {
                                    String msg = String.format(mContext.getString(R.string.msgs_zip_local_file_copy_failed), tfl.getName());
                                    mUtil.addLogMsg("I", msg);
                                    mCommonDlg.showCommonDialog(false, "W", msg, tc.getThreadMessage(), null);
                                    break;
                                }
                            }
                        }
                        if (rc) {
                            showToast(mActivity, mContext.getString(R.string.msgs_zip_local_file_copy_completed));
                        }
                        mUtil.addDebugMsg(1, "I", "Copy ended");
                        mUiHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                refreshFileList();
                                setUiEnabled();
                            }
                        });
                    }
                };
                th.start();
            }

            @Override
            public void negativeResponse(Context c, Object[] o) {
            }
        });
//        ntfy.notifyToListener(true, null);
        String c_list = "", sep = "", copy_list="", copy_sep="";
        for (TreeFilelistItem tfl : mGp.copyCutList) {
            SafFile3 sf=new SafFile3(mContext, to_dir+"/"+tfl.getName());
            if (sf.exists()) {
                c_list += sep + tfl.getName();
                sep = ",";
            }
            copy_list+=copy_sep+tfl.getName();
            copy_sep=",";
        }
        mCommonDlg.showCommonDialog(true, "W", mContext.getString(R.string.msgs_zip_local_file_copy_confirm_title), copy_list, ntfy);
    }

    private void showToast(Activity a, String msg) {
        Thread.dumpStack();
        mUiHandler.post(new Runnable() {
            @Override
            public void run() {
                CommonDialog.showToastLong(a, msg);
            }
        });
    }

    static final public int CONFIRM_RESPONSE_CANCEL = -99;
    static final public int CONFIRM_RESPONSE_YES = 1;
    static final public int CONFIRM_RESPONSE_YESALL = 2;
    static final public int CONFIRM_RESPONSE_NO = -1;
    static final public int CONFIRM_RESPONSE_NOALL = -2;
    private int mConfirmResponse = 0;

    private boolean confirmReplace(final ThreadCtrl tc, final String title, final String message, final String dest_path) {
//        Thread.dumpStack();
        if (mConfirmResponse != CONFIRM_RESPONSE_YESALL && mConfirmResponse != CONFIRM_RESPONSE_NOALL) {
            mUiHandler.post(new Runnable() {
                @Override
                public void run() {
                    mDialogProgressSpinView.setVisibility(LinearLayout.GONE);
                    mDialogConfirmView.setVisibility(LinearLayout.VISIBLE);
                    mDialogConfirmCancel.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mDialogProgressSpinView.setVisibility(LinearLayout.VISIBLE);
                            mDialogConfirmView.setVisibility(LinearLayout.GONE);
                            mConfirmResponse = CONFIRM_RESPONSE_CANCEL;
                            tc.setDisabled();
                            synchronized (tc) {
                                tc.notify();
                            }
                        }
                    });

                    mDialogConfirmYes.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mDialogProgressSpinView.setVisibility(LinearLayout.VISIBLE);
                            mDialogConfirmView.setVisibility(LinearLayout.GONE);
                            mConfirmResponse = CONFIRM_RESPONSE_YES;
                            synchronized (tc) {
                                tc.notify();
                            }
                        }
                    });
                    mDialogConfirmYesAll.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mDialogProgressSpinView.setVisibility(LinearLayout.VISIBLE);
                            mDialogConfirmView.setVisibility(LinearLayout.GONE);
                            mConfirmResponse = CONFIRM_RESPONSE_YESALL;
                            synchronized (tc) {
                                tc.notify();
                            }
                        }
                    });
                    mDialogConfirmNo.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mDialogProgressSpinView.setVisibility(LinearLayout.VISIBLE);
                            mDialogConfirmView.setVisibility(LinearLayout.GONE);
                            mConfirmResponse = CONFIRM_RESPONSE_NO;
                            synchronized (tc) {
                                tc.notify();
                            }
                        }
                    });
                    mDialogConfirmNoAll.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mDialogProgressSpinView.setVisibility(LinearLayout.VISIBLE);
                            mDialogConfirmView.setVisibility(LinearLayout.GONE);
                            mConfirmResponse = CONFIRM_RESPONSE_NOALL;
                            synchronized (tc) {
                                tc.notify();
                            }
                        }
                    });
                    mDialogConfirmMsg.setText(message);
                    mDialogConfirmMsg.requestLayout();
                    mDialogConfirmFilePath.setText(dest_path);
                    mDialogConfirmFilePath.requestLayout();
                }
            });

            synchronized (tc) {
                try {
                    tc.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            boolean result = false;
            if (mConfirmResponse == CONFIRM_RESPONSE_CANCEL) {
            } else if (mConfirmResponse == CONFIRM_RESPONSE_YES) {
                result = true;
            } else if (mConfirmResponse == CONFIRM_RESPONSE_YESALL) {
                result = true;
            } else if (mConfirmResponse == CONFIRM_RESPONSE_NO) {
            } else if (mConfirmResponse == CONFIRM_RESPONSE_NOALL) {
            }
            return result;
        } else {
            boolean result = false;
            if (mConfirmResponse == CONFIRM_RESPONSE_YESALL) {
                result = true;
            }
            return result;
        }
    }

    private boolean deleteLocalItem(ThreadCtrl tc, String fp) {
        if (checkCancelAndWait(tc)) return false;
        boolean result = true;
        SafFile3 lf = new SafFile3(mContext, fp);
        if (lf.exists()) {
            if (lf.isDirectory()) {
                SafFile3[] file_list = lf.listFiles();
                if (file_list != null) {
                    for (SafFile3 item : file_list) {
                        if (checkCancelAndWait(tc)) return false;
                        if (item.isDirectory()) deleteLocalItem(tc, item.getPath());
                        else {
                            result=item.delete();
                            if (result) {
                                putProgressMessage(
                                        String.format(mContext.getString(R.string.msgs_zip_delete_file_was_deleted),
                                                item.getPath()));
                                mUtil.addLogMsg("I",
                                        String.format(mContext.getString(R.string.msgs_zip_delete_file_was_deleted),
                                                item.getPath()));
                            }
                        }
                        if (!result) break;
                    }
                }
                if (result) {
                    SafFile3 del_sf = new SafFile3(mContext,fp);
                    result = del_sf.delete();
                    if (result) {
                        putProgressMessage(String.format(mContext.getString(R.string.msgs_zip_delete_file_was_deleted), lf.getPath()));
                        mUtil.addLogMsg("I", String.format(mContext.getString(R.string.msgs_zip_delete_file_was_deleted), lf.getPath()));
                    }
                }
            } else {
                if (checkCancelAndWait(tc)) return false;
                SafFile3 del_sf = new SafFile3(mContext,fp);
                result = del_sf.delete();
                if (result) {
                    putProgressMessage(String.format(mContext.getString(R.string.msgs_zip_delete_file_was_deleted), lf.getPath()));
                    mUtil.addLogMsg("I", String.format(mContext.getString(R.string.msgs_zip_delete_file_was_deleted), lf.getPath()));
                }
            }
        }
        return result;
    }

	private void confirmDelete(final CustomTreeFilelistAdapter tfa) {
		String conf_list="";
		String sep="";
		StringBuilder sb=new StringBuilder(1024*1024);
		for (TreeFilelistItem tfli:tfa.getDataList()) {
			if (tfli.isChecked()) {
				sb.append(sep).append(tfli.getPath()+"/"+tfli.getName());
				sep="\n";
			}
		}
		conf_list=sb.toString();
		NotifyEvent ntfy=new NotifyEvent(mContext);
		ntfy.setListener(new NotifyEventListener(){
			@Override
			public void positiveResponse(Context c, Object[] o) {
				setUiDisabled();
				showDialogProgress();
				final ThreadCtrl tc=new ThreadCtrl();
				mDialogProgressSpinCancel.setEnabled(true);
				mDialogProgressSpinCancel.setOnClickListener(new OnClickListener(){
					@Override
					public void onClick(View v) {
						confirmCancel(tc,mDialogProgressSpinCancel);
					}
				});
				Thread th=new Thread(){
					@Override
					public void run() {
						mUtil.addDebugMsg(1, "I", "Delete started");
						boolean process_abrted=false;
						for (TreeFilelistItem tfli:tfa.getDataList()) {
							if (tfli.isChecked()) {
								if (!deleteLocalItem(tc, tfli.getPath()+"/"+tfli.getName())) {
									if (checkCancelAndWait(tc)) {
										String msg= String.format(mContext.getString(R.string.msgs_zip_delete_file_was_cancelled),tfli.getName());
										mCommonDlg.showCommonDialog(false, "W", msg, "", null);
									} else {
										String msg= String.format(mContext.getString(R.string.msgs_zip_delete_file_was_failed),tfli.getName());
										mCommonDlg.showCommonDialog(false, "W", msg, "", null);
									}
									process_abrted=true;
									break;
								} else {
//									deleted_item+=deleted_sep+tfli.getPath()+"/"+tfli.getName();
//									deleted_sep=", ";
								}
							}
						}
						if (!process_abrted) {
                            showToast(mActivity, mContext.getString(R.string.msgs_zip_delete_file_completed));
						}
						mUtil.addDebugMsg(1, "I", "Delete ended");

						mUiHandler.post(new Runnable(){
							@Override
							public void run() {
                                clearCopyCutItem();
                                mTreeFilelistAdapter.setAllItemUnchecked();
								refreshFileList();
								setUiEnabled();
							}
						});
					}
				};
				th.start();
			}
			@Override
			public void negativeResponse(Context c, Object[] o) {}
		});
		mCommonDlg.showCommonDialog(true, "W", mContext.getString(R.string.msgs_zip_delete_confirm_delete),
				conf_list, ntfy);
	}

	private void putProgressMessage(final String msg) {
        putProgressMessage(mDialogProgressSpinMsg2, msg);
	}

    private void putProgressMessage(final TextView tv, final String msg) {
        mUiHandler.post(new Runnable(){
            @Override
            public void run() {
                tv.setText(msg);
                tv.requestLayout();
            }
        });
    }

	final private void refreshOptionMenu() {
		mActivity.invalidateOptionsMenu();
	}

	private void setUiEnabled() {
		mActivity.setUiEnabled();
		mTreeFilelistAdapter.setCheckBoxEnabled(isUiEnabled());
		mTreeFilelistAdapter.notifyDataSetChanged();
		hideDialog();
		refreshOptionMenu();
//		Thread.dumpStack();
	}

	private void setUiDisabled() {
		mActivity.setUiDisabled();
		mTreeFilelistAdapter.setCheckBoxEnabled(false);
		mTreeFilelistAdapter.notifyDataSetChanged();
		refreshOptionMenu();
//		Thread.dumpStack();
	}



	public void setContextButtonPasteEnabled(boolean enabled) {
		if (enabled) mContextButtonPasteView.setVisibility(LinearLayout.VISIBLE);
		else mContextButtonPasteView.setVisibility(LinearLayout.INVISIBLE);
	}

	private boolean isUiEnabled() {
		return mActivity.isUiEnabled();
	}

	private void showDialogProgress() {
		mDialogProgressSpinView.setVisibility(LinearLayout.VISIBLE);
        mMainDialogView.bringToFront();
        mMainDialogView.setBackgroundColor(mGp.themeColorList.text_background_color);

//		Thread.dumpStack();
	}

	private void hideDialog() {
		mDialogProgressSpinView.setVisibility(LinearLayout.GONE);
		mDialogConfirmView.setVisibility(LinearLayout.GONE);
//		Thread.dumpStack();
	}

	class SavedViewContent {
		public int pos_x=0, pos_y=0;
		public String curr_dir="/";
		public ArrayList<TreeFilelistItem> tree_list=null;
		public String main_storage_path="";

		public boolean sort_ascendant=true;
		public boolean sort_key_name=true;
		public boolean sort_key_size=false;
		public boolean sort_key_time=false;
	};

	private ArrayList<FileManagerDirectoryListItem> mDirectoryList=new ArrayList<FileManagerDirectoryListItem>();

	private void openSppecificDirectory(final String dir_name, final String file_name) {
		String curr_dir= mLocalStorageSelector.getSelectedItem().toString()+mCurrentDirectory.getText().toString();
		if (mCurrentDirectory.getText().toString().equals("/")) curr_dir= mLocalStorageSelector.getSelectedItem().toString();
		FileManagerDirectoryListItem dli=CommonUtilities.getDirectoryItem(mDirectoryList, curr_dir);
		if (dli==null) {
			dli=new FileManagerDirectoryListItem();
			dli.file_path=curr_dir;
			mDirectoryList.add(dli);
		}
		dli.file_list=mTreeFilelistAdapter.getDataList();
		dli.pos_x=mTreeFilelistView.getFirstVisiblePosition();
		dli.pos_y=mTreeFilelistView.getChildAt(0)==null?0:mTreeFilelistView.getChildAt(0).getTop();

        NotifyEvent ntfy=new NotifyEvent(mContext);
        ntfy.setListener(new NotifyEventListener() {
            @Override
            public void positiveResponse(Context context, Object[] objects) {
                ArrayList<TreeFilelistItem> tfl=(ArrayList<TreeFilelistItem>)objects[0];
                mTreeFilelistAdapter.setDataList(tfl);
                if (tfl.size()>0) mTreeFilelistView.setSelection(0);
                setCurrentDirectoryText(dir_name.replace(mLocalStorageSelector.getSelectedItem().toString(), ""));
                setTopUpButtonEnabled(true);

                if (mTreeFilelistAdapter.getCount()==0) {
                    mTreeFilelistView.setVisibility(ListView.GONE);
                    mFileEmpty.setVisibility(TextView.VISIBLE);
                    mFileEmpty.setText(R.string.msgs_zip_local_folder_empty);
                } else {
                    mFileEmpty.setVisibility(TextView.GONE);
                    mTreeFilelistView.setVisibility(ListView.VISIBLE);

                    int sel_pos=0;
                    if (tfl.size()>0) {
                        if (!file_name.equals("")) {
                            for(int i=0;i<tfl.size();i++) {
                                TreeFilelistItem tfli=tfl.get(i);
                                if (tfli.getName().equals(file_name)) {
                                    sel_pos=i;
//							tfli.setChecked(false);
                                    break;
                                }
                            }
                        }
                        mTreeFilelistView.setSelection(sel_pos);
                    }

                }
                if (isCopyCutDestValid(dir_name)) {
                    mContextButtonPasteView.setVisibility(ImageButton.VISIBLE);
                } else {
                    mContextButtonPasteView.setVisibility(ImageButton.INVISIBLE);
                }
                mContextButtonCopyView.setVisibility(ImageButton.INVISIBLE);
                mContextButtonCutView.setVisibility(ImageButton.INVISIBLE);
            }

            @Override
            public void negativeResponse(Context context, Object[] objects) {

            }
        });
        createTreeFileList(dir_name, ntfy);
	}

    private void setContextButtonShareVisibility() {
        mContextButtonShareView.setVisibility(ImageButton.INVISIBLE);
    	if (mTreeFilelistAdapter.isItemSelected()) {
    	    boolean dir_selected=false;
    	    for(int i=0;i<mTreeFilelistAdapter.getCount();i++) {
    	        TreeFilelistItem tfli=mTreeFilelistAdapter.getItem(i);
    	        if (tfli.isChecked()) {
    	            if (tfli.isDirectory()) {
    	                dir_selected=true;
    	                break;
                    }
                }
            }
    	    if (!dir_selected) {
                for(int i=0;i<mTreeFilelistAdapter.getCount();i++) {
                    TreeFilelistItem tfli=mTreeFilelistAdapter.getItem(i);
                    if (tfli.isChecked()) {
                        mContextButtonShareView.setVisibility(ImageButton.VISIBLE);
                        break;
                    }
                }
            }
    	}
    }

	private NotifyEvent mNotifyCheckBoxChanged=null;
    private ArrayList<SavedViewContent>mLocalStorageList=new ArrayList<SavedViewContent>();

    private SafStorage3 getSafStorageByDescription(String desc) {
        ArrayList<SafStorage3>svl=mGp.safMgr.getSafStorageList();
        for(SafStorage3 sli:svl) {
            if (sli.description.equals(desc)) {
                return sli;
            }
        }
        return null;
    }

    public Spinner getLocalStorageSelector() {
        return mLocalStorageSelector;
    }

    public void setLocalStorageSelector(boolean animate) {
        CustomSpinnerAdapter stg_adapter = new CustomSpinnerAdapter(mActivity, android.R.layout.simple_spinner_item);
        stg_adapter.setDropDownViewResource(android.R.layout.select_dialog_singlechoice);

        ArrayList<SafStorage3>svl=mGp.safMgr.getSafStorageList();
        for(SafStorage3 sli:svl) {
            stg_adapter.add(sli.description);
        }
        mLocalStorageSelector.setPrompt(mContext.getString(R.string.msgs_main_local_mount_point_select_local_storage_title));
        mLocalStorageSelector.setAdapter(stg_adapter);
        mLocalStorageSelector.setSelection(0, animate);
    }

    public boolean refreshLocalStorageSelector() {
        int prev_sc=mLocalStorageSelector.getSelectedItemPosition();
        String prev_stg_name=(String)mLocalStorageSelector.getSelectedItem();

        int sel_no=mLocalStorageSelector.getSelectedItemPosition();

        CustomSpinnerAdapter adapter = (CustomSpinnerAdapter) mLocalStorageSelector.getAdapter();
        ArrayList<SafStorage3>svl=mGp.safMgr.getSafStorageList();
        boolean changed=false;
        if (svl.size()==adapter.getCount()) {
            for(int i=0;i>adapter.getCount();i++) {
                String a_item=adapter.getItem(i);
                if (!a_item.equals(svl.get(i))) {
                    changed=true;
                    break;
                }
            }
        } else {
            changed=true;
        }
        adapter.clear();
        for (int i = 0; i<svl.size(); i++) {
            adapter.add(svl.get(i).description);
        }
        boolean found=false;
        for (int i = 0; i<svl.size(); i++) {
            if (svl.get(i).description.equals(prev_stg_name)) {
                found=true;
                sel_no=i;
            }
        }
        if (found) mLocalStorageSelector.setSelection(sel_no, false);
        else mLocalStorageSelector.setSelection(0);

        return !found;
    }

    private void setTreeFileListener() {
		mUtil.addDebugMsg(1, "I", CommonUtilities.getExecutedMethodName()+" entered");
		mLocalStorageSelector.setOnItemSelectedListener(new OnItemSelectedListener(){
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mUtil.addDebugMsg(1, "I", "LocalStorageSelector onItemSelected entered, pos="+position+", item="+(String)mLocalStorageSelector.getSelectedItem());
			    if (!mLocalStorageSelector.isEnabled()) return;
				String n_sn= mLocalStorageSelector.getSelectedItem().toString();
				String n_fp=getSafStorageByDescription(n_sn).saf_file.getPath();
				SavedViewContent svc=null;
				for(SavedViewContent item:mLocalStorageList) {
				    if (item.main_storage_path.equals(mMainStoragePath)) {
				        svc=item;
				        break;
                    }
                }
				if (svc==null) {
				    svc=new SavedViewContent();
				    mLocalStorageList.add(svc);
                }
                svc.curr_dir=mMainFilePath;
				svc.main_storage_path= mMainStoragePath;
                svc.tree_list=mTreeFilelistAdapter.getDataList();
                svc.pos_x=mTreeFilelistView.getFirstVisiblePosition();
                svc.pos_y=mTreeFilelistView.getChildAt(0)==null?0:mTreeFilelistView.getChildAt(0).getTop();
//                svc.curr_dir=mCurrentDirectory.getText().toString();
                svc.sort_ascendant=mTreeFilelistAdapter.isSortAscendant();
                svc.sort_key_name=mTreeFilelistAdapter.isSortKeyName();
                svc.sort_key_size=mTreeFilelistAdapter.isSortKeySize();
                svc.sort_key_time=mTreeFilelistAdapter.isSortKeyTime();

                SavedViewContent new_svc=null;
                for(SavedViewContent item:mLocalStorageList) {
                    if (item.main_storage_path.equals(n_fp)) {
                        new_svc=item;
                        break;
                    }
                }
                if (new_svc==null) {
                    new_svc=new SavedViewContent();
                    new_svc.curr_dir=n_fp;
                    new_svc.main_storage_path=n_fp;
                    mLocalStorageList.add(new_svc);
                }

                ArrayList<TreeFilelistItem> prev_tfl=null;
				int pos_x=0, pos_y=0;
				boolean sort_asc=true, sort_name=true, sort_size=false, sort_time=false;

                prev_tfl=new_svc.tree_list;
                pos_x=new_svc.pos_x;
                pos_y=new_svc.pos_y;
                sort_asc=new_svc.sort_ascendant;
                sort_name=new_svc.sort_key_name;
                sort_size=new_svc.sort_key_size;
                sort_time=new_svc.sort_key_time;

                mMainFilePath=new_svc.curr_dir;
                mMainStoragePath=new_svc.main_storage_path;

                String curr_dir=new_svc.curr_dir;

			    if (sort_asc) mTreeFilelistAdapter.setSortAscendant();
			    else mTreeFilelistAdapter.setSortDescendant();
			    if (sort_name) mTreeFilelistAdapter.setSortKeyName();
			    else if (sort_size) mTreeFilelistAdapter.setSortKeySize();
			    else if (sort_time) mTreeFilelistAdapter.setSortKeyTime();

			    final String f_curr_dir=curr_dir;
			    final ArrayList<TreeFilelistItem> f_prev_tfl=prev_tfl;
			    final int f_pos_x=pos_x;
                final int f_pos_y=pos_y;

			    NotifyEvent ntfy=new NotifyEvent(mContext);
			    ntfy.setListener(new NotifyEventListener() {
                    @Override
                    public void positiveResponse(Context context, Object[] objects) {
                        ArrayList<TreeFilelistItem> tfl=(ArrayList<TreeFilelistItem>)objects[0];
                        mTreeFilelistView.setSelectionFromTop(f_pos_x, f_pos_y);
                        mMainFilePath=f_curr_dir;
                        if (f_prev_tfl!=null) {
                            for(TreeFilelistItem prev_item:f_prev_tfl) {
                                for(TreeFilelistItem new_item:mTreeFilelistAdapter.getDataList()) {
                                    if (prev_item.getPath().equals(new_item.getPath()) && prev_item.getName().equals(new_item.getName())) {
                                        new_item.setChecked(prev_item.isChecked());
                                    }
                                }
                            }
                        }
                        if (mTreeFilelistAdapter.isItemSelected()) {
                            mContextButtonCopyView.setVisibility(ImageButton.VISIBLE);
                            mContextButtonCutView.setVisibility(ImageButton.VISIBLE);
                        }
                        if (mGp.copyCutList.size()>0) {
                            mGp.copyCutItemInfo.setVisibility(TextView.VISIBLE);
                            mGp.copyCutItemClear.setVisibility(Button.VISIBLE);
                            if (isCopyCutDestValid(f_curr_dir))
                                mContextButtonPasteView.setVisibility(ImageButton.VISIBLE);
                        }
                        mActivity.refreshOptionMenu();
                    }

                    @Override
                    public void negativeResponse(Context context, Object[] objects) {

                    }
                });
			    createFileList(curr_dir, ntfy);
			}
			@Override
			public void onNothingSelected(AdapterView<?> parent) {
			}
		});

		mContextButtonCreateView.setVisibility(ImageButton.VISIBLE);
		mContextButtonShareView.setVisibility(ImageButton.INVISIBLE);
		mContextButtonRenameView.setVisibility(ImageButton.INVISIBLE);
		mContextButtonCopyView.setVisibility(ImageButton.INVISIBLE);
		mContextButtonCutView.setVisibility(ImageButton.INVISIBLE);
		mContextButtonPasteView.setVisibility(ImageButton.INVISIBLE);
		mContextButtonDeleteView.setVisibility(ImageButton.INVISIBLE);
		mContextButtonArchiveView.setVisibility(ImageButton.INVISIBLE);
		mNotifyCheckBoxChanged=new NotifyEvent(mContext);
		mNotifyCheckBoxChanged.setListener(new NotifyEventListener(){
			@Override
			public void positiveResponse(Context c, Object[] o) {
				setContextButtonShareVisibility();
				mContextButtonCreateView.setVisibility(ImageButton.INVISIBLE);
				if (mTreeFilelistAdapter.getSelectedItemCount()==1) {
				    mContextButtonRenameView.setVisibility(ImageButton.VISIBLE);
                } else {
				    mContextButtonRenameView.setVisibility(ImageButton.INVISIBLE);
                }
                mContextButtonDeleteView.setVisibility(ImageButton.VISIBLE);
				mContextButtonArchiveView.setVisibility(ImageButton.VISIBLE);
				mContextButtonCopyView.setVisibility(ImageButton.VISIBLE);
                mContextButtonCutView.setVisibility(ImageButton.VISIBLE);
                mContextButtonPasteView.setVisibility(ImageButton.INVISIBLE);
				setContextButtonSelectUnselectVisibility();
			}
			@Override
			public void negativeResponse(Context c, Object[] o) {
				if (mTreeFilelistAdapter.isItemSelected()) {
                    mContextButtonCreateView.setVisibility(ImageButton.INVISIBLE);
					setContextButtonShareVisibility();
					if (mTreeFilelistAdapter.getSelectedItemCount()==1) {
                        mContextButtonRenameView.setVisibility(ImageButton.VISIBLE);
					} else {
						mContextButtonRenameView.setVisibility(ImageButton.INVISIBLE);
					}
					mContextButtonPasteView.setVisibility(ImageButton.INVISIBLE);
                    mContextButtonDeleteView.setVisibility(ImageButton.VISIBLE);
					mContextButtonArchiveView.setVisibility(ImageButton.VISIBLE);
					mContextButtonCopyView.setVisibility(ImageButton.VISIBLE);
                    mContextButtonCutView.setVisibility(ImageButton.VISIBLE);
				} else {
					String dir=mMainFilePath;
					if (isCopyCutDestValid(dir)) {
                        mContextButtonPasteView.setVisibility(ImageButton.VISIBLE);
					} else {
						mContextButtonPasteView.setVisibility(ImageButton.INVISIBLE);
					}
                    mContextButtonCreateView.setVisibility(ImageButton.VISIBLE);
					setContextButtonShareVisibility();
					mContextButtonRenameView.setVisibility(ImageButton.INVISIBLE);
					mContextButtonDeleteView.setVisibility(ImageButton.INVISIBLE);
					mContextButtonArchiveView.setVisibility(ImageButton.INVISIBLE);
					mContextButtonCopyView.setVisibility(ImageButton.INVISIBLE);
					mContextButtonCutView.setVisibility(ImageButton.INVISIBLE);
				}
				setContextButtonSelectUnselectVisibility();
			}
		});
		mTreeFilelistAdapter.setCbCheckListener(mNotifyCheckBoxChanged);
        mTreeFilelistView.setOnItemClickListener(new OnItemClickListener(){
        	public void onItemClick(AdapterView<?> items, View view, int idx, long id) {
        		if (!isUiEnabled()) return;
	    		final TreeFilelistItem tfi=mTreeFilelistAdapter.getItem(idx);
				if (!mTreeFilelistAdapter.isItemSelected() && tfi.isDirectory()) {
					String curr_dir=mCurrentDirectory.getText().toString();
					FileManagerDirectoryListItem dli=CommonUtilities.getDirectoryItem(mDirectoryList, curr_dir);
					if (dli==null) {
						dli=new FileManagerDirectoryListItem();
						dli.file_path=curr_dir;
						mDirectoryList.add(dli);
					}
					dli.file_list=mTreeFilelistAdapter.getDataList();
					dli.pos_x=mTreeFilelistView.getFirstVisiblePosition();
					dli.pos_y=mTreeFilelistView.getChildAt(0)==null?0:mTreeFilelistView.getChildAt(0).getTop();

                    final String dir=mMainFilePath+"/"+tfi.getName();
                    NotifyEvent ntfy=new NotifyEvent(mContext);
                    ntfy.setListener(new NotifyEventListener() {
                        @Override
                        public void positiveResponse(Context context, Object[] objects) {
                            ArrayList<TreeFilelistItem> tfl=(ArrayList<TreeFilelistItem>)objects[0];
                            mTreeFilelistAdapter.setDataList(tfl);
                            if (tfl.size()>0) {
                                mTreeFilelistView.setSelection(0);
                            }
                            setContextButtonSelectUnselectVisibility();
                            setCurrentDirectoryText(dir.replace(mLocalStorageSelector.getSelectedItem().toString(), ""));
                            mMainFilePath=dir;
                            setTopUpButtonEnabled(true);

                            if (mTreeFilelistAdapter.getCount()==0) {
                                mTreeFilelistView.setVisibility(ListView.GONE);
                                mFileEmpty.setVisibility(TextView.VISIBLE);
                                mFileEmpty.setText(R.string.msgs_zip_local_folder_empty);
                            } else {
                                mFileEmpty.setVisibility(TextView.GONE);
                                mTreeFilelistView.setVisibility(ListView.VISIBLE);
                            }
                            if (isCopyCutDestValid(dir)) {
                                mContextButtonPasteView.setVisibility(ImageButton.VISIBLE);
                            } else {
                                mContextButtonPasteView.setVisibility(ImageButton.INVISIBLE);
                            }
                            mContextButtonCopyView.setVisibility(ImageButton.INVISIBLE);
                            mContextButtonCutView.setVisibility(ImageButton.INVISIBLE);
                        }

                        @Override
                        public void negativeResponse(Context context, Object[] objects) {

                        }
                    });
                    createTreeFileList(dir, ntfy);
				} else {
					if (mTreeFilelistAdapter.isItemSelected()) {
						tfi.setChecked(!tfi.isChecked());
						mTreeFilelistAdapter.notifyDataSetChanged();
					} else {
						invokeBrowser(tfi.getPath(), tfi.getName(), "");
					}
				}
			}
        });

        mTreeFilelistView.setOnItemLongClickListener(new OnItemLongClickListener(){
			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        		if (!isUiEnabled()) return true;
//	    		final int pos=mTreeFilelistAdapter.getItem(position);
	    		final TreeFilelistItem tfi=mTreeFilelistAdapter.getItem(position);
				if (tfi.getName().startsWith("---")) return true;
				showContextMenu(tfi);
				return true;
			}
        });
        mFileListTop.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				if (!isUiEnabled()) return;
				mMainFilePath=mMainStoragePath;
				final FileManagerDirectoryListItem p_dli=CommonUtilities.getDirectoryItem(mDirectoryList, mMainFilePath);
				CommonUtilities.clearDirectoryItem(mDirectoryList, mMainFilePath);
				String dir="";
                NotifyEvent ntfy=new NotifyEvent(mContext);
                ntfy.setListener(new NotifyEventListener() {
                    @Override
                    public void positiveResponse(Context context, Object[] objects) {
                        ArrayList<TreeFilelistItem> tfl=(ArrayList<TreeFilelistItem>)objects[0];
                        mTreeFilelistAdapter.setDataList(tfl);
                        mCurrentDirectory.setText(mMainFilePath);
                        invalidateLocalFileView();
//                        mCurrentDirectory.invalidate();
//                        mCurrentDirectory.requestLayout();
                        if (tfl.size()>0) mTreeFilelistView.setSelection(0);
                        setTopUpButtonEnabled(false);
                        if (mTreeFilelistAdapter.getCount()==0) {
                            mTreeFilelistView.setVisibility(ListView.GONE);
                            mFileEmpty.setVisibility(TextView.VISIBLE);
                            mFileEmpty.setText(R.string.msgs_zip_local_folder_empty);
                        } else {
                            mFileEmpty.setVisibility(TextView.GONE);
                            mTreeFilelistView.setVisibility(ListView.VISIBLE);
                            if (p_dli!=null) mTreeFilelistView.setSelectionFromTop(p_dli.pos_x, p_dli.pos_y);
                        }
                        if (isCopyCutDestValid(mMainFilePath)) {
                            mContextButtonPasteView.setVisibility(ImageButton.VISIBLE);
                        } else {
                            mContextButtonPasteView.setVisibility(ImageButton.INVISIBLE);
                        }
                        mContextButtonCopyView.setVisibility(ImageButton.INVISIBLE);
                        mContextButtonCutView.setVisibility(ImageButton.INVISIBLE);
                    }

                    @Override
                    public void negativeResponse(Context context, Object[] objects) {

                    }
                });
                createTreeFileList(mMainFilePath, ntfy);
			}
        });

        mFileListUp.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				if (!isUiEnabled()) return;
				if (!mMainFilePath.equals("")) {
					if (mMainFilePath.lastIndexOf("/")>0) {
						FileManagerDirectoryListItem dli=CommonUtilities.getDirectoryItem(mDirectoryList, mMainFilePath);
						CommonUtilities.removeDirectoryItem(mDirectoryList, dli);
						final String n_dir=mMainFilePath.substring(0,mMainFilePath.lastIndexOf("/"));
						final FileManagerDirectoryListItem p_dli=CommonUtilities.getDirectoryItem(mDirectoryList, n_dir);
                        NotifyEvent ntfy=new NotifyEvent(mContext);
                        ntfy.setListener(new NotifyEventListener() {
                            @Override
                            public void positiveResponse(Context context, Object[] objects) {
                                mTreeFilelistView.setAdapter(null);
                                ArrayList<TreeFilelistItem> tfl=(ArrayList<TreeFilelistItem>)objects[0];
                                mTreeFilelistAdapter.setDataList(tfl);
                                mTreeFilelistView.setAdapter(mTreeFilelistAdapter);
                                mMainFilePath=n_dir;
                                if (!mMainStoragePath.equals(n_dir)) {
                                    setCurrentDirectoryText(n_dir);
                                    if (tfl.size()>0) {
                                        if (p_dli!=null) {
                                            mTreeFilelistView.setSelectionFromTop(p_dli.pos_x, p_dli.pos_y);
                                        }
                                    }
                                } else {
                                    mCurrentDirectory.setText(n_dir);
                                    invalidateLocalFileView();
                                    if (tfl.size()>0) {
                                        if (p_dli!=null) {
                                            mTreeFilelistView.setSelectionFromTop(p_dli.pos_x, p_dli.pos_y);
                                        }
                                    }
                                    setTopUpButtonEnabled(false);
                                }
                                if (mTreeFilelistAdapter.getCount()==0) {
                                    mTreeFilelistView.setVisibility(ListView.GONE);
                                    mFileEmpty.setVisibility(TextView.VISIBLE);
                                    mFileEmpty.setText(R.string.msgs_zip_local_folder_empty);
                                } else {
                                    mFileEmpty.setVisibility(TextView.GONE);
                                    mTreeFilelistView.setVisibility(ListView.VISIBLE);
                                }
                                if (isCopyCutDestValid(n_dir)) {
                                    mContextButtonPasteView.setVisibility(ImageButton.VISIBLE);
                                } else {
                                    mContextButtonPasteView.setVisibility(ImageButton.INVISIBLE);
                                }
                                mContextButtonCopyView.setVisibility(ImageButton.INVISIBLE);
                                mContextButtonCutView.setVisibility(ImageButton.INVISIBLE);
                            }

                            @Override
                            public void negativeResponse(Context context, Object[] objects) {

                            }
                        });
                        createTreeFileList(n_dir, ntfy);
					}
				}
			}
        });
	}

	public boolean isUpButtonEnabled() {
		return mFileListUp.isEnabled();
	}

    public boolean isFileListSelected() {
        return mTreeFilelistAdapter.isItemSelected();
    }

    public void setFileListAllItemUnselected() {
        mTreeFilelistAdapter.setAllItemUnchecked();
        mTreeFilelistAdapter.notifyDataSetChanged();
    }

    public void performClickUpButton() {
		mFileListUp.setSoundEffectsEnabled(false);
		mFileListUp.performClick();
		mFileListUp.setSoundEffectsEnabled(true);
	}

	private void setTopUpButtonEnabled(boolean p) {
//        Thread.dumpStack();
		mFileListUp.setEnabled(p);
		mFileListTop.setEnabled(p);
		if (p) {
			mFileListUp.setAlpha(1);
			mFileListTop.setAlpha(1);
		} else {
			mFileListUp.setAlpha(0.4f);
			mFileListTop.setAlpha(0.4f);
		}
	}

	private int mInfoDirectoryCount=0;
	private int mInfoFileCount=0;
	private long mInfoFileSize=0;
    private void getDirectorySize(SafFile3 file) {
        if (file.getUuid().equals(SafFile3.SAF_FILE_PRIMARY_UUID)) {
            File lf=new File(file.getPath());
            getFileApiDirectorySize(lf);
        } else {
            File lf=new File(file.getPath());
            if (lf.canRead()) getFileApiDirectorySize(lf);
            else {
                ContentProviderClient cpc=file.getContentProviderClient();
                try {
                    getSafApiDirectorySize(cpc, file);
                } finally {
                    cpc.release();
                }
            }
        }
    };

    private void getSafApiDirectorySize(ContentProviderClient cpc, SafFile3 file) {
		if (file.isDirectory(cpc)) {
			mInfoDirectoryCount++;
			SafFile3[] cl=file.listFiles(cpc);
			if (cl!=null) {
				for(SafFile3 cf:cl) {
					getSafApiDirectorySize(cpc, cf);
				}
			}
		} else {
			mInfoFileCount++;
			mInfoFileSize+=file.length(cpc);
		}
	};

    private void getFileApiDirectorySize(File file) {
        if (file.isDirectory()) {
            mInfoDirectoryCount++;
            File[] cl=file.listFiles();
            if (cl!=null) {
                for(File cf:cl) {
                    getFileApiDirectorySize(cf);
                }
            }
        } else {
            mInfoFileCount++;
            mInfoFileSize+=file.length();
        }
    };

    private void showContextMenu(final TreeFilelistItem tfi) {
        final CustomContextMenu mCcMenu = new CustomContextMenu(mContext.getResources(), mFragmentManager);
		String sel_list="",sep="";
		final CustomTreeFilelistAdapter tfa=new CustomTreeFilelistAdapter(mActivity, false, false, false);
		ArrayList<TreeFilelistItem> n_tfl=new ArrayList<TreeFilelistItem>();
		int sel_count=0;
		if (mTreeFilelistAdapter.isItemSelected()) {
			for(TreeFilelistItem s_tfi:mTreeFilelistAdapter.getDataList()) {
				if (s_tfi.isChecked()) {
					n_tfl.add(s_tfi.clone());
					sel_list+=sep+s_tfi.getName();
					sep=",";
					sel_count++;
				}
			}
		} else {
			TreeFilelistItem n_tfi=tfi.clone();
			n_tfi.setChecked(true);
			n_tfl.add(n_tfi);
			sel_list=n_tfi.getName();
			sel_count++;
		}
		tfa.setDataList(n_tfl);

        if (tfi.isDirectory() && sel_count==1) {
            mCcMenu.addMenuItem(mContext.getString(R.string.msgs_main_local_file_ccmenu_property)+
                    "("+(n_tfl.get(0)).getName()+")",R.drawable.dialog_information)
                    .setOnClickListener(new CustomContextMenuOnClickListener() {
                        @Override
                        public void onClick(CharSequence menuTitle) {
                            final ThreadCtrl tc=new ThreadCtrl();
                            final ProgressSpinDialogFragment psd=ProgressSpinDialogFragment.newInstance(
                                    mContext.getString(R.string.msgs_search_file_dlg_searching),
                                    mContext.getString(R.string.msgs_search_file_dlg_sort_wait),
                                    mContext.getString(R.string.msgs_common_dialog_cancel),
                                    mContext.getString(R.string.msgs_common_dialog_canceling));

                            NotifyEvent ntfy=new NotifyEvent(mContext);
                            ntfy.setListener(new NotifyEventListener(){
                                @Override
                                public void positiveResponse(Context c, Object[] o) {}
                                @Override
                                public void negativeResponse(Context c, Object[] o) {
                                    tc.setDisabled();
                                    if (checkCancel(tc)) psd.dismissAllowingStateLoss();
                                }
                            });
                            psd.showDialog(mFragmentManager, psd, ntfy,true);
                            Thread th=new Thread(){
                                @Override
                                public void run() {
                                    SafFile3 lf=new SafFile3(mContext, tfi.getPath()+"/"+tfi.getName());
                                    mInfoDirectoryCount=-1;
                                    mInfoFileCount=0;
                                    mInfoFileSize=0;
                                    getDirectorySize(lf);
                                    psd.dismissAllowingStateLoss();
                                    String msg=mContext.getString(R.string.msgs_local_file_item_property_directory);
                                    mCommonDlg.showCommonDialog(false, "I", tfi.getName(),
                                            String.format(msg, mInfoDirectoryCount, mInfoFileCount, mInfoFileSize), null);
                                }
                            };
                            th.start();
                        }
                    });
        }

        if (!tfi.isChecked()) {
            mCcMenu.addMenuItem(mContext.getString(R.string.msgs_main_local_file_ccmenu_select) + "(" + sel_list + ")", R.drawable.menu_active)
                    .setOnClickListener(new CustomContextMenuOnClickListener() {
                        @Override
                        public void onClick(CharSequence menuTitle) {
                            tfi.setChecked(!tfi.isChecked());
                            mTreeFilelistAdapter.notifyDataSetChanged();
                        }
                    });
        }

        mCcMenu.addMenuItem(mContext.getString(R.string.msgs_main_local_file_ccmenu_top),R.drawable.context_button_top)
                .setOnClickListener(new CustomContextMenuOnClickListener() {
            @Override
            public void onClick(CharSequence menuTitle) {
                mTreeFilelistView.setSelection(0);
            }
        });

        mCcMenu.addMenuItem(mContext.getString(R.string.msgs_main_local_file_ccmenu_bottom),R.drawable.context_button_bottom)
                .setOnClickListener(new CustomContextMenuOnClickListener() {
            @Override
            public void onClick(CharSequence menuTitle) {
                mTreeFilelistView.setSelection(mTreeFilelistAdapter.getCount()-1);
            }
        });

        if (tfi.isDirectory() && sel_count==1) {
			mCcMenu.addMenuItem(mContext.getString(R.string.msgs_main_local_file_ccmenu_open_directory)+
					"("+(n_tfl.get(0)).getName()+")",R.drawable.cc_folder)
	  			.setOnClickListener(new CustomContextMenuOnClickListener() {
				@Override
				public void onClick(CharSequence menuTitle) {
					String curr_dir=mCurrentDirectory.getText().toString();
//					if (mCurrentDirectory.getText().toString().equals("/")) curr_dir= mLocalStorageSelector.getSelectedItem().toString();
					FileManagerDirectoryListItem dli=CommonUtilities.getDirectoryItem(mDirectoryList, curr_dir);
					if (dli==null) {
						dli=new FileManagerDirectoryListItem();
						dli.file_path=curr_dir;
						mDirectoryList.add(dli);
					}
					dli.file_list=mTreeFilelistAdapter.getDataList();
					dli.pos_x=mTreeFilelistView.getFirstVisiblePosition();
					dli.pos_y=mTreeFilelistView.getChildAt(0)==null?0:mTreeFilelistView.getChildAt(0).getTop();

					final String dir=tfi.getPath().equals("")?tfi.getName():tfi.getPath()+"/"+tfi.getName();

                    NotifyEvent ntfy=new NotifyEvent(mContext);
                    ntfy.setListener(new NotifyEventListener() {
                        @Override
                        public void positiveResponse(Context context, Object[] objects) {
                            ArrayList<TreeFilelistItem> tfl=(ArrayList<TreeFilelistItem>)objects[0];
                            mTreeFilelistAdapter.setDataList(tfl);
                            if (tfl.size()>0) mTreeFilelistView.setSelection(0);
//                            setCurrentDirectoryText(dir.replace(mLocalStorageSelector.getSelectedItem().toString(), ""));
                            setCurrentDirectoryText(dir);
                            mMainFilePath=dir;
                            mContextButtonArchiveView.setVisibility(ImageButton.INVISIBLE);
                            mContextButtonDeleteView.setVisibility(ImageButton.INVISIBLE);
                            setTopUpButtonEnabled(true);

                            if (mTreeFilelistAdapter.getCount()==0) {
                                mTreeFilelistView.setVisibility(ListView.GONE);
                                mFileEmpty.setVisibility(TextView.VISIBLE);
                                mFileEmpty.setText(R.string.msgs_zip_local_folder_empty);
                            } else {
                                mFileEmpty.setVisibility(TextView.GONE);
                                mTreeFilelistView.setVisibility(ListView.VISIBLE);
                            }
                            if (isCopyCutDestValid(dir)) {
                                mContextButtonPasteView.setVisibility(ImageButton.VISIBLE);
                            } else {
                                mContextButtonPasteView.setVisibility(ImageButton.INVISIBLE);
                            }
                            mContextButtonCopyView.setVisibility(ImageButton.INVISIBLE);
                            mContextButtonCutView.setVisibility(ImageButton.INVISIBLE);
                        }

                        @Override
                        public void negativeResponse(Context context, Object[] objects) {

                        }
                    });
                    createTreeFileList(dir, ntfy);
				}
	  		});
		}
		if (sel_count==1) {
			mCcMenu.addMenuItem(mContext.getString(R.string.msgs_main_local_file_ccmenu_rename)+"("+sel_list+")",R.drawable.context_button_rename)
		  		.setOnClickListener(new CustomContextMenuOnClickListener() {
				@Override
				public void onClick(CharSequence menuTitle) {
					renameItem(tfa);
				}
		  	});
		}
		mCcMenu.addMenuItem(mContext.getString(R.string.msgs_main_local_file_ccmenu_delete)+"("+sel_list+")",R.drawable.context_button_trash)
	  		.setOnClickListener(new CustomContextMenuOnClickListener() {
			@Override
			public void onClick(CharSequence menuTitle) {
				confirmDelete(tfa);
			}
	  	});

		mCcMenu.addMenuItem(mContext.getString(R.string.msgs_main_local_file_ccmenu_compress)+"("+sel_list+")",R.drawable.context_button_archive)
	  		.setOnClickListener(new CustomContextMenuOnClickListener() {
			@Override
			public void onClick(CharSequence menuTitle) {
				prepareZipSelectedItem(tfa);
			}
	  	});
		mCcMenu.addMenuItem(mContext.getString(R.string.msgs_main_local_file_ccmenu_copy)+"("+sel_list+")",R.drawable.context_button_copy)
	  		.setOnClickListener(new CustomContextMenuOnClickListener() {
			@Override
			public void onClick(CharSequence menuTitle) {
				copyItem(tfa);
			}
	  	});
		mCcMenu.addMenuItem(mContext.getString(R.string.msgs_main_local_file_ccmenu_cut)+"("+sel_list+")",R.drawable.context_button_cut)
	  		.setOnClickListener(new CustomContextMenuOnClickListener() {
			@Override
			public void onClick(CharSequence menuTitle) {
				cutItem(tfa);
			}
	  	});
		if (mContextButtonPasteView.getVisibility()== LinearLayout.VISIBLE) {
			mCcMenu.addMenuItem(mContext.getString(R.string.msgs_main_local_file_ccmenu_paste),R.drawable.context_button_paste)
		  		.setOnClickListener(new CustomContextMenuOnClickListener() {
				@Override
				public void onClick(CharSequence menuTitle) {
					pasteItem();
				}
		  	});
		}
		if (!tfi.isDirectory() && sel_count==1) {
			mCcMenu.addMenuItem(mContext.getString(R.string.msgs_main_local_file_ccmenu_force_zip)+"("+(n_tfl.get(0)).getName()+")",R.drawable.context_button_archive)
		  		.setOnClickListener(new CustomContextMenuOnClickListener() {
				@Override
				public void onClick(CharSequence menuTitle) {
					invokeBrowser(tfi.getPath(), tfi.getName(), MIME_TYPE_ZIP);
				}
		  	});
			mCcMenu.addMenuItem(mContext.getString(R.string.msgs_main_local_file_ccmenu_force_text)+"("+(n_tfl.get(0)).getName()+")",R.drawable.cc_sheet)
	  			.setOnClickListener(new CustomContextMenuOnClickListener() {
				@Override
				public void onClick(CharSequence menuTitle) {
					invokeBrowser(tfi.getPath(), tfi.getName(), MIME_TYPE_TEXT);
				}
	  		});
		}
		mCcMenu.createMenu();
	}

	private void prepareZipSelectedItem(final CustomTreeFilelistAdapter tfa) {
		String conf_list="", sep="", w_out_fn="", w_out_dir="";
		for(TreeFilelistItem tfi:tfa.getDataList()) {
			if (tfi.isChecked()) {
				conf_list+=sep+tfi.getName();
				sep=",";
				if (w_out_fn.equals("")) {
					w_out_fn=tfi.getName()+".zip";
					w_out_dir=tfi.getPath();
				}
			}
		}
		final String out_fn=w_out_fn;
		final String out_dir=w_out_dir.replace(mLocalStorageSelector.getSelectedItem().toString(), "");
		final NotifyEvent ntfy_confirm=new NotifyEvent(mContext);
		ntfy_confirm.setListener(new NotifyEventListener(){
			@Override
			public void positiveResponse(Context c, Object[] o) {
				NotifyEvent ntfy_select_dest=new NotifyEvent(mContext);
				ntfy_select_dest.setListener(new NotifyEventListener(){
					@Override
					public void positiveResponse(Context c, Object[] o) {
						final SafFile3 out_fl=new SafFile3(mContext, (String)o[1]);
						NotifyEvent ntfy_create=new NotifyEvent(mContext);
						ntfy_create.setListener(new NotifyEventListener(){
							@Override
							public void positiveResponse(Context c, Object[] o) {
								NotifyEvent ntfy_zip=new NotifyEvent(mContext);
								ntfy_zip.setListener(new NotifyEventListener(){
									@Override
									public void positiveResponse(Context c, Object[] o) {
                                        String dest_dir="", dest_name="";
                                        dest_dir=out_fl.getParentFile().getPath();
                                        dest_name=out_fl.getName();
										CustomZipParameters zp=(CustomZipParameters)o[0];
//										zp.setCompressFileExtentionExcludeList(mGp.settingNoCompressFileType);
										ArrayList<String> sel_list=new ArrayList<String>();
										for(TreeFilelistItem tfi:tfa.getDataList()) {
											if (tfi.isChecked()) {
												sel_list.add(tfi.getPath()+"/"+tfi.getName());
											}
										}
										String[] add_item=new String[sel_list.size()];
										for(int i=0;i<sel_list.size();i++) {
											add_item[i]=sel_list.get(i);
										}
										zipSelectedItem(zp, add_item, dest_dir, dest_name, out_fl);

                                        mTreeFilelistAdapter.setAllItemUnchecked();
                                        mTreeFilelistAdapter.notifyDataSetChanged();
									}
									@Override
									public void negativeResponse(Context c, Object[] o) {
									}

								});
								ZipFileManager.getZipParmDlg(mUtil, mActivity, mGp, ENCODING_NAME_UTF8, "", out_fl.getPath(), ntfy_zip);
							}
							@Override
							public void negativeResponse(Context c, Object[] o) {
							}
						});
						if (out_fl.exists()) {
							mCommonDlg.showCommonDialog(true, "W",
									String.format(mContext.getString(R.string.msgs_zip_create_new_zip_file_already_exists),out_fl.getName()),
									"", ntfy_create);
						} else {
							ntfy_create.notifyToListener(true, null);
						}
					}
					@Override
					public void negativeResponse(Context c, Object[] o) {
					}
				});
				String zip_file_name="";
                for(TreeFilelistItem tfi:tfa.getDataList()) {
                    if (tfi.isChecked()) {
                        zip_file_name="";
                        if (tfi.isDirectory()) zip_file_name=tfi.getName()+".zip";
                        else {
                            if (tfi.getName().lastIndexOf(".")>0) {
                                zip_file_name=tfi.getName().substring(0, tfi.getName().lastIndexOf("."))+".zip";
                            } else {
                                zip_file_name=tfi.getName()+".zip";
                            }
                        }
                        break;
                    }
                }
                String uuid=SafFile3.getUuidFromFilePath(mMainFilePath);
                boolean include_root=false;
                boolean scoped_storage_mode=mGp.safMgr.isScopedStorageMode();
                CommonFileSelector2 fsdf=
                        CommonFileSelector2.newInstance(scoped_storage_mode, true, false, CommonFileSelector2.DIALOG_SELECT_CATEGORY_FILE,
                                true, false, uuid, "", zip_file_name, mContext.getString(R.string.msgs_zip_create_new_zip_file_select_dest));
                fsdf.showDialog(false, mActivity.getSupportFragmentManager(), fsdf, ntfy_select_dest);

			}
			@Override
			public void negativeResponse(Context c, Object[] o) {
			}

		});

		mCommonDlg.showCommonDialog(true, "W",
				mContext.getString(R.string.msgs_zip_create_new_zip_file_confirm), conf_list, ntfy_confirm);
	}

    private void zipSelectedItem(final CustomZipParameters zp, final String[] add_item,
                                 final String dest_dir, final String dest_name, final SafFile3 out_fl) {
		mUtil.addDebugMsg(1, "I", CommonUtilities.getExecutedMethodName()+" entered, dest_dir="+dest_dir+
				", dest_file="+dest_name);
		setUiDisabled();
		showDialogProgress();
		final ThreadCtrl tc=new ThreadCtrl();
		mDialogProgressSpinCancel.setEnabled(true);
		mDialogProgressSpinCancel.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				confirmCancel(tc,mDialogProgressSpinCancel);
			}
		});
		Thread th=new Thread(){
			@Override
			public void run() {
				long b_time= System.currentTimeMillis();
				String dest_path=dest_dir+"/"+dest_name;
                SafFile3 dest_sf=new SafFile3(mContext, dest_path);
                String zip_file_name = dest_name;
                String out_path = "";
                boolean cache_available=false;
                if (dest_sf.getAppDirectoryCache()==null) {
                    out_path =dest_path+".tmp";
                } else {
                    cache_available=true;
                    out_path =dest_sf.getAppDirectoryCache()+"/"+dest_name;
                }
                SafFile3 out_sf=new SafFile3(mContext, out_path);
                SafFile3 processed_file=null;
                BufferedZipFile3 bzf=null;
                try {
                    bzf=new BufferedZipFile3(mContext, null, out_sf, ZipUtil.DEFAULT_ZIP_FILENAME_ENCODING);
                    bzf.setNoCompressExtentionList(mGp.settingNoCompressFileType);
                    bzf.setPassword(zp.getPassword());
                    String base_dir=mMainFilePath;
                    String added_item="", added_sep="";
                    putProgressMessage(mContext.getString(R.string.msgs_local_file_add_file_begin));
                    zp.setDefaultFolderPath(base_dir+"/");
                    for(String item:add_item) {
                        SafFile3 in_file=new SafFile3(mContext, item);
                        ArrayList<SafFile3> sel_list=new ArrayList<SafFile3>();
                        if (in_file.isDirectory()) sel_list.add(in_file);
                        ZipFileManager.getAllItemInLocalDirectory(sel_list, in_file);
                        for(SafFile3 sel_item:sel_list) {
                            processed_file=sel_item;
                            String msg= String.format(mContext.getString(R.string.msgs_local_file_add_file_adding),sel_item.getPath());
                            mUtil.addLogMsg("I", msg);
                            if (sel_item.isDirectory()) zp.setFileNameInZip(sel_item.getPath().replace(zp.getDefaultFolderPath(), "")+"/");
                            else zp.setFileNameInZip(sel_item.getPath().replace(zp.getDefaultFolderPath(), ""));

                            CallBackListener cbl=getZipProgressCallbackListener(tc, bzf, msg);
                            bzf.addItem(sel_item, zp, cbl);
                            if (checkCancelAndWait(tc)) {
                                String msg_txt= String.format(mContext.getString(R.string.msgs_local_file_add_file_cancelled),item);
                                mUtil.addLogMsg("W", msg);
                                mCommonDlg.showCommonDialog(false, "W",msg_txt, "", null);
                                bzf.destroy();
                                out_sf.deleteIfExists();

                                closeUiDialogView(100);
                                break;
                            } else {
                                String msg_txt= String.format(mContext.getString(R.string.msgs_local_file_add_file_added),sel_item.getPath());
                                mUtil.addLogMsg("I", msg_txt);
                                putProgressMessage(msg);
                            }
                        }
                        if (checkCancelAndWait(tc)) break;
                        added_item+=added_sep+item;
                        added_sep=", ";
                    }
                    if (!checkCancelAndWait(tc)) {
                        CallBackListener cbl=getZipProgressCallbackListener(tc, bzf, mContext.getString(R.string.msgs_zip_zip_file_being_updated));
                        if (bzf.close(cbl)) {
                            dest_sf.deleteIfExists();
                            if (cache_available) out_sf.moveTo(dest_sf);
                            else {
                                renameSafFile(out_sf, dest_sf);
                                scanMediaFile(mGp, mUtil, dest_sf.getPath());
                            }
                        }
                        mUtil.addDebugMsg(1, "I", "zipSelectedItem elapsed time="+(System.currentTimeMillis()-b_time));
                        showToast(mActivity, mContext.getString(R.string.msgs_local_file_add_file_completed));
                        closeUiDialogView(100);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    try {if (bzf!=null) bzf.destroy(); } catch (IOException ex) {}
                    if (out_sf!=null) out_sf.deleteIfExists();
                    String fn=processed_file!=null?processed_file.getPath():"none";
                    String msg= String.format(mContext.getString(R.string.msgs_local_file_add_file_failed), fn);
                    mUtil.addLogMsg("E", msg);
                    mCommonDlg.showCommonDialog(false, "E",msg, e.getMessage()+"\n"+CommonUtilities.formatStackTraceInfo(e), null);
                    disableCancelButton();
                    closeUiDialogView(100);
                }
			}
		};
		th.start();
	};

    static public void scanMediaFile(GlobalParameters gp, CommonUtilities util, String fp) {
        MediaScannerConnection.scanFile(gp.appContext, new String[]{fp}, null, null);
        util.addDebugMsg(2, "I","Media scanner invoked, name=",fp);
    };

    private CallBackListener getZipProgressCallbackListener(final ThreadCtrl tc, final BufferedZipFile3 bzf, final String msg_txt) {
        CallBackListener cbl=new CallBackListener() {
            @Override
            public boolean onCallBack(Context context, Object o, Object[] objects) {
                if (checkCancelAndWait(tc)) {
                    bzf.abort();
                } else {
                    int prog=(Integer)o;
                    putProgressMessage(msg_txt+" "+prog+"%");
                }
                return true;
            }
        };
        return cbl;
    }

    private void closeUiDialogView(int delay_time) {
        mUiHandler.postDelayed(new Runnable(){
            @Override
            public void run() {
                refreshFileList();
                setUiEnabled();
            }
        }, delay_time);
    }

    private void disableCancelButton() {
        mUiHandler.post(new Runnable(){
            @Override
            public void run() {
                mDialogProgressSpinCancel.setEnabled(false);
            }
        });
    }


    private boolean invokeBrowser(final String p_dir, final String f_name, String mime_type) {
		boolean result=false;
        SafFile3 sf=new SafFile3(mContext, p_dir+"/"+f_name);
		String fid=CommonUtilities.getFileExtention(f_name);
		String mt="";
		if (!mime_type.equals("")) mt=mime_type;
		else if (fid.equals("gz")) mt=MIME_TYPE_ZIP;
		else if (fid.equals("zip")) mt=MIME_TYPE_ZIP;
        else mt=getMimeTypeFromFileExtention(mGp, f_name);
        if (mt!=null && mt.equals(MIME_TYPE_ZIP)) mActivity.showZipFile(false, sf);
        else {
            try {
                Intent intent = new Intent(android.content.Intent.ACTION_VIEW);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION |Intent.FLAG_GRANT_WRITE_URI_PERMISSION);// | Intent.FLAG_ACTIVITY_NEW_TASK);
                Uri uri=null;
                if (sf.isSafFile()) {
                    uri=sf.getUri();
                } else {
                    uri= FileProvider.getUriForFile(mContext, BuildConfig.APPLICATION_ID + ".provider", new File(sf.getPath()));
                }
//                    uri= FileProvider.getUriForFile(mContext, BuildConfig.APPLICATION_ID + ".provider", new File(sf.getPath()));
                if (mt==null) intent.setDataAndType(uri, "*/*");
                else intent.setDataAndType(uri, mt);
                boolean ex=sf.exists();
                mActivity.startActivity(intent);
                result=true;
            } catch(ActivityNotFoundException e) {
                mCommonDlg.showCommonDialog(false,"E",
                        String.format(mContext.getString(R.string.msgs_zip_specific_extract_file_viewer_not_found),f_name,mt),"",null);
            }
        }

		return result;
	};
	
	private void createFileList(final String fp, final NotifyEvent p_ntfy) {
        mTreeFilelistView.setVisibility(ListView.VISIBLE);
        mContextButtonView.setVisibility(ListView.VISIBLE);
        mFileListUp.setVisibility(Button.VISIBLE);
        mFileListTop.setVisibility(Button.VISIBLE);
        String n_sn= mLocalStorageSelector.getSelectedItem().toString();
        String n_fp=getSafStorageByDescription(n_sn).saf_file.getPath();
        if (fp.equals(n_fp)) {
            setTopUpButtonEnabled(false);
        } else {
            setTopUpButtonEnabled(true);
        }

        NotifyEvent ntfy=new NotifyEvent(mContext);
        ntfy.setListener(new NotifyEventListener() {
            @Override
            public void positiveResponse(Context context, Object[] objects) {
                ArrayList<TreeFilelistItem> tfl=(ArrayList<TreeFilelistItem>)objects[0];
                if (mTreeFilelistAdapter==null) mTreeFilelistAdapter=new CustomTreeFilelistAdapter(mActivity, false, true);
                mTreeFilelistAdapter.setDataList(tfl);
                mTreeFilelistAdapter.setCheckBoxEnabled(isUiEnabled());
                mTreeFilelistAdapter.notifyDataSetChanged();
                mTreeFilelistView.setAdapter(mTreeFilelistAdapter);
                if (fp.equals(mLocalStorageSelector.getSelectedItem().toString())) mCurrentDirectory.setText(mMainFilePath);
                else setCurrentDirectoryText(fp.replace(mLocalStorageSelector.getSelectedItem().toString(), ""));
                invalidateLocalFileView();
                mCurrentDirectory.setVisibility(TextView.VISIBLE);
//                    mTreeFilelistView.setSelectionFromTop(0, 0);
                setTreeFileListener();

                if (tfl.size()==0) {
                    mContextButtonSelectAllView.setVisibility(LinearLayout.INVISIBLE);
                    mContextButtonUnselectAllView.setVisibility(LinearLayout.INVISIBLE);
                    mTreeFilelistView.setVisibility(ListView.GONE);
                    mFileEmpty.setVisibility(TextView.VISIBLE);
                    mFileEmpty.setText(R.string.msgs_zip_local_folder_empty);
                } else {
                    mContextButtonSelectAllView.setVisibility(LinearLayout.VISIBLE);
                    mContextButtonUnselectAllView.setVisibility(LinearLayout.INVISIBLE);
                    mContextButtonCreateView.setVisibility(ImageButton.VISIBLE);
                    mFileEmpty.setVisibility(TextView.GONE);
                    mTreeFilelistView.setVisibility(ListView.VISIBLE);
                }

                if (p_ntfy!=null) p_ntfy.notifyToListener(true, new Object[]{tfl});
            }

            @Override
            public void negativeResponse(Context context, Object[] objects) {

            }
        });
        createTreeFileList(fp, ntfy);
	};

	private void setCurrentDirectoryText(String cd) {
	    mCurrentDirectory.setText(cd.equals("")?"/":cd);
    }

    private void createTreeFileList(final String target_dir, final NotifyEvent p_ntfy) {
	    boolean async=false;
        if (target_dir.startsWith(SafFile3.SAF_FILE_PRIMARY_STORAGE_PREFIX)) createFileApiTreeFileList(target_dir, p_ntfy);
        else {
            File lf=new File(target_dir);
            if (lf.canRead()) createFileApiTreeFileList(target_dir, p_ntfy);
            else {
                createSafApiTreeFileListAsync(target_dir, p_ntfy);
            }
        }
    }

	private void createSafApiTreeFileListSync(final String target_dir, final NotifyEvent p_ntfy) {
//	    Thread.dumpStack();
        if (mActivity.isFinishing()) return;
        mUtil.addDebugMsg(1,"I","createSafApiTreeFileListSync entered, fp="+target_dir);
	    final Dialog pd=CommonDialog.showProgressSpinIndicator(mActivity);
	    pd.show();
	    Thread th=new Thread(){
	        public void run() {
                final ArrayList<TreeFilelistItem> tfl=new ArrayList<TreeFilelistItem>();
                SafFile3 sf=new SafFile3(mContext, target_dir);
                ContentProviderClient cpc=sf.getContentProviderClient();
                try {
                    SafFile3[] fl=sf.listFiles(cpc);
                    mUtil.addDebugMsg(1,"I","createSafApiTreeFileListSync listFiles ended");
                    if (fl!=null) {
                        int item_count=0;
                        for (SafFile3 item:fl) {
                            TreeFilelistItem tfli=createSafApiFileListItem(cpc, item);
                            tfl.add(tfli);
//                            if (tfli.isDirectory()) tfli.setSubDirItemCount(item.getCount(cpc));
                        }
                    } else {
                    }
                } finally {
                    if (cpc!=null) cpc.release();
                }

                mUtil.addDebugMsg(1,"I","createSafApiTreeFileListSync ended");
                mUiHandler.post(new Runnable(){
                    @Override
                    public void run() {
                        if (pd.isShowing()) pd.dismiss();
                        p_ntfy.notifyToListener(true, new Object[]{tfl});
                    }
                });
            }
        };
	    th.start();
	};

    private void createSafApiTreeFileListAsync(final String target_dir, final NotifyEvent p_ntfy) {
//	    Thread.dumpStack();
        if (mActivity.isFinishing()) return;
        mUtil.addDebugMsg(1,"I","createSafApiTreeFileList entered, fp="+target_dir);
        final Dialog pd=CommonDialog.showProgressSpinIndicator(mActivity);
        pd.show();
        Thread th=new Thread(){
            public void run() {
                final ArrayList<TreeFilelistItem> tfl=new ArrayList<TreeFilelistItem>();
                SafFile3 sf=new SafFile3(mContext, target_dir);
                ContentProviderClient cpc=null;
                try {
                    cpc=sf.getContentProviderClient();
                    SafFile3[] fl=sf.listFiles(cpc);
                    mUtil.addDebugMsg(1,"I","createSafApiTreeFileList listFiles ended");
                    if (fl!=null) {
                        if (fl.length>0) {
                            boolean first_notified=false;
                            int item_count=0;
                            for (SafFile3 item:fl) {
                                TreeFilelistItem tfli=createSafApiFileListItem(cpc, item);
//                                if (tfli.isDirectory()) tfli.setSubDirItemCount(item.getCount(cpc));
                                mUiHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        tfl.add(tfli);
                                    }
                                });
                                item_count++;
                                if (!first_notified) {
                                    first_notified=true;
                                    mUiHandler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            p_ntfy.notifyToListener(true, new Object[]{tfl});
                                        }
                                    });
                                } else {
                                    if (item_count>=10) {
                                        item_count=0;
                                        mUiHandler.post(new Runnable() {
                                            @Override
                                            public void run() {
                                                mTreeFilelistAdapter.notifyDataSetChanged();
                                            }
                                        });
                                    }
                                }
                            }
                        } else {
                            mUiHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    p_ntfy.notifyToListener(true, new Object[]{tfl});
                                }
                            });
                        }
                    } else {
                        mUiHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                p_ntfy.notifyToListener(true, new Object[]{tfl});
                            }
                        });
                    }
                    mUtil.addDebugMsg(1,"I","createSafApiTreeFileList ended");
                } catch(Exception e) {
                    mUtil.addDebugMsg(1,"I","createSafApiTreeFileList ended with error. error="+e.getMessage()+"\n"+ MiscUtil.getStackTraceString(e));
                } finally {
                    if (cpc!=null) cpc.release();
                }

                mUiHandler.post(new Runnable(){
                    @Override
                    public void run() {
                        if (pd.isShowing()) pd.dismiss();
                        p_ntfy.notifyToListener(true, new Object[]{tfl});
                        mTreeFilelistAdapter.sort();
//                        p_ntfy.notifyToListener(true, new Object[]{tfl});
                    }
                });
            }
        };
        th.start();
    };

    private void createFileApiTreeFileList(final String target_dir, final NotifyEvent p_ntfy) {
//	    Thread.dumpStack();
        if (mActivity.isFinishing()) return;
        mUtil.addDebugMsg(1,"I","createFileApiTreeFileList entered, fp="+target_dir);
        final Dialog pd=CommonDialog.showProgressSpinIndicator(mActivity);
        pd.show();
        Thread th=new Thread(){
            public void run() {
                final ArrayList<TreeFilelistItem> tfl=new ArrayList<TreeFilelistItem>();
                File sf=new File(target_dir);
                try {
                    File[] fl=sf.listFiles();
                    mUtil.addDebugMsg(1,"I","createFileApiTreeFileList listFiles ended");
                    if (fl!=null) {
                        boolean first_notified=false;
                        for (File item:fl) {
                            TreeFilelistItem tfli= createFileApiFileListItem(item);
                            tfl.add(tfli);
                            if (tfli.isDirectory()) {
                                File[] sub_fl=item.listFiles();
                                if (sub_fl!=null) tfli.setSubDirItemCount(sub_fl.length);
                            }
                        }
                    } else {
                    }
                } finally {
                }

                mUtil.addDebugMsg(1,"I","createFileApiTreeFileList ended");
                mUiHandler.post(new Runnable(){
                    @Override
                    public void run() {
                        if (pd.isShowing()) pd.dismiss();
                        p_ntfy.notifyToListener(true, new Object[]{tfl});
                    }
                });
            }
        };
        th.start();
    };

    private TreeFilelistItem createSafApiFileListItem(final ContentProviderClient cpc, final SafFile3 item) {
//        long b_time=System.currentTimeMillis();
		if (item.isDirectory(cpc)) {
            final TreeFilelistItem tfi=new TreeFilelistItem(item.getName(),
					true, -1, item.lastModified(cpc),
                    false, true, true,
                    item.isHidden(), item.getParent(), 0);
            tfi.setSubDirItemCount(item.getCount(cpc));
//            Thread th=new Thread(){
//                @Override
//                public void run() {
//                    long dir_size= getSafApiAllFileSizeInDirectory(cpc, item, true);
//                    tfi.setLength(dir_size);
//                    mUiHandler.post(new Runnable(){
//                        @Override
//                        public void run(){
//                            mTreeFilelistAdapter.notifyDataSetChanged();
//                        }
//                    }) ;
//                }
//            };
//            th.setPriority(Thread.MIN_PRIORITY);
//            th.start();
//            mUtil.addDebugMsg(1,"I","createSafApiFileListItem elapsed time="+(System.currentTimeMillis()-b_time));
            return tfi;
		} else {
		    long[]lm_len=item.getLastModifiedAndLength(cpc);
//            long[]lm_len=new long[]{0L, 0L};
            TreeFilelistItem tfi=new TreeFilelistItem(item.getName(),
					false, lm_len[1], lm_len[0],
                    false, true, true,
                    item.isHidden(), item.getParent(), 0);
//            mUtil.addDebugMsg(1,"I","createSafApiFileListItem elapsed time="+(System.currentTimeMillis()-b_time));
            return tfi;
		}
	};

    final static public long getSafApiAllFileSizeInDirectory(ContentProviderClient cpc, SafFile3 sd, boolean process_sub_directories) {
        long dir_size=0;
        if (sd.exists(cpc)) {
            if (sd.isDirectory(cpc)) {
                SafFile3[] cfl=sd.listFiles(cpc);
                if (cfl!=null && cfl.length>0) {
                    for(SafFile3 cf:cfl) {
                        if (cf.isDirectory(cpc)) {
                            if (process_sub_directories) dir_size+= getSafApiAllFileSizeInDirectory(cpc, cf, process_sub_directories);
                        } else {
                            dir_size+=cf.length(cpc);
                        }
                    }
                }
            } else {
                dir_size+=sd.length(cpc);
            }
        }
        return dir_size;
    };

    private TreeFilelistItem createFileApiFileListItem(final File lf) {
        if (lf.isDirectory()) {
            final TreeFilelistItem tfi=new TreeFilelistItem(lf.getName(),
                    true, -1, lf.lastModified(),
					false, lf.canRead(), lf.canWrite(),
//                    false, true, true,
                    lf.isHidden(), lf.getParent(),0);
            Thread th=new Thread(){
                @Override
                public void run() {
                    long dir_size= getFileApiAllFileSizeInDirectory(lf, true);
                    tfi.setLength(dir_size);
                    mUiHandler.post(new Runnable(){
                        @Override
                        public void run(){
                            mTreeFilelistAdapter.notifyDataSetChanged();
                        }
                    }) ;
                }
            };
            th.start();
            return tfi;
        } else {
            TreeFilelistItem tfi=new TreeFilelistItem(lf.getName(),
                    false, lf.length(), lf.lastModified(),
//					false, item.canRead(), item.canWrite(cpc),
                    false, true, true,
                    lf.isHidden(), lf.getParent(),0);
            return tfi;
        }
    };

    final static public long getFileApiAllFileSizeInDirectory(File sd, boolean process_sub_directories) {
        long dir_size=0;
        if (sd.exists()) {
            if (sd.isDirectory()) {
                File[] cfl=sd.listFiles();
                if (cfl!=null && cfl.length>0) {
                    for(File cf:cfl) {
                        if (cf.isDirectory()) {
                            if (process_sub_directories) dir_size+= getFileApiAllFileSizeInDirectory(cf, process_sub_directories);
                        } else {
                            dir_size+=cf.length();
                        }
                    }
                }
            } else {
                dir_size+=sd.length();
            }
        }
        return dir_size;
    };


}
