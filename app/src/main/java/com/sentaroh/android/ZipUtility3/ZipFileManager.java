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

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager.LayoutParams;
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
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
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
import com.sentaroh.android.Utilities3.Dialog.ProgressSpinDialogFragment;
import com.sentaroh.android.Utilities3.LocalMountPoint;
import com.sentaroh.android.Utilities3.MiscUtil;
import com.sentaroh.android.Utilities3.NotifyEvent;
import com.sentaroh.android.Utilities3.NotifyEvent.NotifyEventListener;
import com.sentaroh.android.Utilities3.SafFile3;
import com.sentaroh.android.Utilities3.SafManager3;
import com.sentaroh.android.Utilities3.StringUtil;
import com.sentaroh.android.Utilities3.ThreadCtrl;
import com.sentaroh.android.Utilities3.Widget.CustomSpinnerAdapter;
import com.sentaroh.android.Utilities3.Widget.NonWordwrapTextView;
import com.sentaroh.android.Utilities3.Zip.BufferedZipFile3;
import com.sentaroh.android.Utilities3.Zip.HeaderReader;
import com.sentaroh.android.Utilities3.Zip.SeekableInputStream;
import com.sentaroh.android.Utilities3.Zip.ZipFileListItem;
import com.sentaroh.android.Utilities3.Zip.ZipUtil;


import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.io.inputstream.ZipInputStream;
import net.lingala.zip4j.model.FileHeader;
import net.lingala.zip4j.model.LocalFileHeader;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.model.enums.AesKeyStrength;
import net.lingala.zip4j.model.enums.CompressionLevel;
import net.lingala.zip4j.model.enums.CompressionMethod;
import net.lingala.zip4j.model.enums.EncryptionMethod;

import org.apache.commons.compress.compressors.CompressorInputStream;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.apache.commons.compress.compressors.deflate64.Deflate64CompressorInputStream;
import org.apache.commons.compress.compressors.lzma.LZMACompressorInputStream;
import org.tukaani.xz.LZMA2InputStream;
import org.tukaani.xz.LZMAInputStream;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Externalizable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.StreamCorruptedException;
import java.nio.Buffer;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.InputMismatchException;
import java.util.Locale;
import java.util.regex.Pattern;

import static com.sentaroh.android.Utilities3.SafManager3.SCOPED_STORAGE_SDK;
import static com.sentaroh.android.ZipUtility3.Constants.*;

@SuppressLint("ClickableViewAccessibility")
public class ZipFileManager {

	private GlobalParameters mGp=null;
	
	private FragmentManager mFragmentManager=null;
	private CommonDialog mCommonDlg=null;
	
	private Context mContext;
	private ActivityMain mActivity=null;
	@SuppressWarnings("unused")
	private String mLastMsgText="";

	private ArrayList<ZipFileListItem> mZipFileList=null;
	private ListView mTreeFilelistView=null;
	private CustomTreeFilelistAdapter mTreeFilelistAdapter=null;
	
	private Handler mUiHandler=null;
	private String mCurrentFilePath="";
	private long mCurrentFileLastModified=0;
	private long mCurrentFileLength=0;
	private String mMainPassword="";

	private boolean mCurretnFileIsReadOnly =false;

    private ArrayList<ZipFileViewerItem> zipFileViewerList=new ArrayList<ZipFileViewerItem>();
    private Spinner mZipFileSpinner=null;

    private Button mFileListUp, mFileListTop;
	private NonWordwrapTextView mCurrentDirectory;
	private TextView mFileEmpty, mFileInfo;
	private Spinner mEncodingSpinner;
	private LinearLayout mMainDialogView=null;
	
	private CommonUtilities mUtil=null;
	
	private LinearLayout mMainView=null;
	private LinearLayout mDialogProgressSpinView=null;
	private TextView mDialogProgressSpinMsg1=null;
	private TextView mDialogProgressSpinMsg2=null;
	private Button mDialogProgressSpinCancel=null;

	private LinearLayout mDialogMessageView=null;
//	private TextView mDialogMessageTitle=null;
//	private TextView mDialogMessageBody=null;
//	private Button mDialogMessageOk=null;
//	private Button mDialogMessageClose=null;
//	private Button mDialogMessageCancel=null;

	private LinearLayout mDialogConfirmView=null;
	private TextView mDialogConfirmMsg=null;
	private Button mDialogConfirmCancel=null;
	
	private Button mDialogConfirmYes=null;
	private Button mDialogConfirmYesAll=null;
	private Button mDialogConfirmNo=null;
	private Button mDialogConfirmNoAll=null;
	
	private LinearLayout mContextButton=null;

	private ImageButton mContextButtonCopy=null;
	private ImageButton mContextButtonCut=null;
	private ImageButton mContextButtonPaste=null;
	private ImageButton mContextButtonExtract=null;
	private ImageButton mContextButtonOpen=null;
	private ImageButton mContextButtonNew=null;
    private ImageButton mContextButtonDelete=null;
    private ImageButton mContextButtonSelectAll=null;
    private ImageButton mContextButtonUnselectAll=null;

	private LinearLayout mContextButtonCopyView=null;
	private LinearLayout mContextButtonCutView=null;
	private LinearLayout mContextButtonPasteView=null;
	private LinearLayout mContextButtonExtractView=null;
	private LinearLayout mContextButtonOpenView=null;
	private LinearLayout mContextButtonNewView=null;
    private LinearLayout mContextButtonDeleteView=null;
    private LinearLayout mContextButtonSelectAllView=null;
    private LinearLayout mContextButtonUnselectAllView=null;

	public ZipFileManager(GlobalParameters gp, ActivityMain a, FragmentManager fm, LinearLayout mv) {
		mGp=gp;
        mActivity=a;
		mCommonDlg=new CommonDialog(a, fm);
        mUiHandler=new Handler();
        mContext=a.getApplicationContext();
        mFragmentManager=fm;
        mUtil=new CommonUtilities(mContext, "ZipFolder", gp, mCommonDlg);

        mEncodingDesired=mContext.getString(R.string.msgs_zip_parm_zip_encoding_auto);
        
        mMainView=mv;
        initViewWidget();

		mTreeFilelistAdapter=new CustomTreeFilelistAdapter(mActivity, false, true);
		mTreeFilelistView.setAdapter(mTreeFilelistAdapter);

        hideTreeFileListView();
	};

    public void reInitView() {
        ArrayList<TreeFilelistItem> fl=mTreeFilelistAdapter.getDataList();
        int v_pos_fv=0, v_pos_top=0;
        v_pos_fv=mTreeFilelistView.getFirstVisiblePosition();
        if (mTreeFilelistView.getChildAt(0)!=null) v_pos_top=mTreeFilelistView.getChildAt(0).getTop();

        mTreeFilelistAdapter=new CustomTreeFilelistAdapter(mActivity, false, true);

        mTreeFilelistAdapter.setDataList(fl);
        mTreeFilelistView.setAdapter(mTreeFilelistAdapter);
        mTreeFilelistView.setSelectionFromTop(v_pos_fv, v_pos_top);
        mTreeFilelistAdapter.notifyDataSetChanged();
    }

    public boolean isFileListSortAscendant() {
		if (mTreeFilelistAdapter!=null) return mTreeFilelistAdapter.isSortAscendant();
		else return true;
	};

	private void initViewWidget() {
		
		mContextButton=(LinearLayout)mMainView.findViewById(R.id.context_view_zip_file);
		
		mZipFileSpinner=(Spinner)mMainView.findViewById(R.id.zip_file_zip_file_spinner);
		CommonUtilities.setSpinnerBackground(mActivity, mZipFileSpinner, mGp.themeIsLight);
		ZipFileSelectorAdapter adapter=new ZipFileSelectorAdapter(mActivity, android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(R.layout.spinner_dropdown_single_choice);
        mZipFileSpinner.setPrompt(mContext.getString(R.string.msgs_zip_zip_select_file));
//		mZipFileSpinner.setPrompt(mContext.getString(R.string.msgs_main_sync_profile_dlg_sync_folder_type_prompt));
		mZipFileSpinner.setAdapter(adapter);
		
		mEncodingSpinner=(Spinner)mMainView.findViewById(R.id.zip_file_encoding);
		CommonUtilities.setSpinnerBackground(mActivity, mEncodingSpinner, mGp.themeIsLight);
		final CustomSpinnerAdapter enc_adapter=
				new CustomSpinnerAdapter(mActivity, android.R.layout.simple_spinner_item);
		enc_adapter.setDropDownViewResource(android.R.layout.select_dialog_singlechoice);
		enc_adapter.add(mContext.getString(R.string.msgs_zip_parm_zip_encoding_auto));
		for(String item:ENCODING_NAME_LIST) enc_adapter.add(item);
		mEncodingSpinner.setAdapter(enc_adapter);
		mEncodingSpinner.setSelection(0);
		
		mMainDialogView=(LinearLayout)mMainView.findViewById(R.id.main_dialog_view);
		mMainDialogView.setVisibility(LinearLayout.VISIBLE);

        mTreeFilelistView=(ListView)mMainView.findViewById(R.id.zip_file_list);
        mFileEmpty=(TextView)mMainView.findViewById(R.id.zip_file_empty);
        mFileEmpty.setVisibility(TextView.GONE);
        mTreeFilelistView.setVisibility(ListView.VISIBLE);
        
        mFileInfo=(TextView)mMainView.findViewById(R.id.zip_file_info);
        
        mFileListUp=(Button)mMainView.findViewById(R.id.zip_file_up_btn);
        if (mGp.themeIsLight) mFileListUp.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_16_go_up_dark, 0, 0, 0);
        else mFileListUp.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_16_go_up_light, 0, 0, 0);
        mFileListTop=(Button)mMainView.findViewById(R.id.zip_file_top_btn);
        if (mGp.themeIsLight) mFileListTop.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_16_go_top_dark, 0, 0, 0);
        else mFileListTop.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_16_go_top_light, 0, 0, 0);

        mCurrentDirectory=(NonWordwrapTextView)mMainView.findViewById(R.id.zip_file_filepath);
//        mCurrentDirectory.setTextColor(mGp.themeColorList.text_color_primary);


        mDialogProgressSpinView=(LinearLayout)mMainView.findViewById(R.id.main_dialog_progress_spin_view);
        mDialogProgressSpinView.setVisibility(LinearLayout.GONE);
        mDialogProgressSpinMsg1=(TextView)mMainView.findViewById(R.id.main_dialog_progress_spin_syncprof);
        mDialogProgressSpinMsg1.setVisibility(TextView.GONE);
        mDialogProgressSpinMsg2=(TextView)mMainView.findViewById(R.id.main_dialog_progress_spin_syncmsg);
        mDialogProgressSpinCancel=(Button)mMainView.findViewById(R.id.main_dialog_progress_spin_btn_cancel);

        mDialogMessageView=(LinearLayout)mMainView.findViewById(R.id.main_dialog_message_view);
        mDialogMessageView.setVisibility(LinearLayout.GONE);
//        mDialogMessageTitle=(TextView)mMainView.findViewById(R.id.main_dialog_message_title);
//        mDialogMessageBody=(TextView)mMainView.findViewById(R.id.main_dialog_message_body);
//        mDialogMessageClose=(Button)mMainView.findViewById(R.id.main_dialog_message_close_btn);
//        mDialogMessageCancel=(Button)mMainView.findViewById(R.id.main_dialog_message_cancel_btn);
//        mDialogMessageOk=(Button)mMainView.findViewById(R.id.main_dialog_message_ok_btn);

        mDialogConfirmView=(LinearLayout)mMainView.findViewById(R.id.main_dialog_confirm_view);
        mDialogConfirmView.setVisibility(LinearLayout.GONE);
        mDialogConfirmMsg=(TextView)mMainView.findViewById(R.id.main_dialog_confirm_msg);
        mDialogConfirmCancel=(Button)mMainView.findViewById(R.id.main_dialog_confirm_sync_cancel);
        mDialogConfirmNo=(Button)mMainView.findViewById(R.id.copy_delete_confirm_no);
        mDialogConfirmNoAll=(Button)mMainView.findViewById(R.id.copy_delete_confirm_noall);
        mDialogConfirmYes=(Button)mMainView.findViewById(R.id.copy_delete_confirm_yes);
        mDialogConfirmYesAll=(Button)mMainView.findViewById(R.id.copy_delete_confirm_yesall);
        
    	mContextButtonCopy=(ImageButton)mMainView.findViewById(R.id.context_button_copy);
    	mContextButtonCut=(ImageButton)mMainView.findViewById(R.id.context_button_cut);
    	mContextButtonPaste=(ImageButton)mMainView.findViewById(R.id.context_button_paste);
    	mContextButtonExtract=(ImageButton)mMainView.findViewById(R.id.context_button_extract);
    	mContextButtonOpen=(ImageButton)mMainView.findViewById(R.id.context_button_open);
    	mContextButtonNew=(ImageButton)mMainView.findViewById(R.id.context_button_clear);
    	mContextButtonDelete=(ImageButton)mMainView.findViewById(R.id.context_button_delete);
        mContextButtonSelectAll=(ImageButton)mMainView.findViewById(R.id.context_button_select_all);
        mContextButtonUnselectAll=(ImageButton)mMainView.findViewById(R.id.context_button_unselect_all);
        
    	mContextButtonCopyView=(LinearLayout)mMainView.findViewById(R.id.context_button_copy_view);
    	mContextButtonCutView=(LinearLayout)mMainView.findViewById(R.id.context_button_cut_view);
    	mContextButtonPasteView=(LinearLayout)mMainView.findViewById(R.id.context_button_paste_view);
    	mContextButtonExtractView=(LinearLayout)mMainView.findViewById(R.id.context_button_extract_view);
    	mContextButtonOpenView=(LinearLayout)mMainView.findViewById(R.id.context_button_open_view);
    	mContextButtonNewView=(LinearLayout)mMainView.findViewById(R.id.context_button_clear_view);
    	mContextButtonDeleteView=(LinearLayout)mMainView.findViewById(R.id.context_button_delete_view);
        mContextButtonSelectAllView=(LinearLayout)mMainView.findViewById(R.id.context_button_select_all_view);
        mContextButtonUnselectAllView=(LinearLayout)mMainView.findViewById(R.id.context_button_unselect_all_view);
        
        setContextButtonListener();
	};

	private void saveZipFileViewerItem(String fp, Bundle bd) {
		mUtil.addDebugMsg(2, "I", CommonUtilities.getExecutedMethodName()+" entered, fp="+fp);
		for(ZipFileViewerItem fvi:zipFileViewerList) {
			if (fvi.file_path.equals(fp)) {
				fvi.saved_data=bd;
				mUtil.addDebugMsg(2, "I", CommonUtilities.getExecutedMethodName()+" file viewer item saved, fp="+fp);
				return;
			}
		}
		ZipFileViewerItem fvi=new ZipFileViewerItem();
		fvi.file_path=fp;
		fvi.saved_data=bd;
		zipFileViewerList.add(fvi);
		Collections.sort(zipFileViewerList, new Comparator<ZipFileViewerItem>(){
			@Override
			public int compare(ZipFileViewerItem lhs, ZipFileViewerItem rhs) {
				return lhs.file_path.compareToIgnoreCase(rhs.file_path);
			}
		});
        CustomSpinnerAdapter adapter=(CustomSpinnerAdapter)mZipFileSpinner.getAdapter();
		adapter.clear();
		for(ZipFileViewerItem zfi:zipFileViewerList) {
			adapter.add(zfi.file_path);
		}
		mZipFileSpinner.setSelection(adapter.getPosition(fp));
		adapter.notifyDataSetChanged();
		mUtil.addDebugMsg(2, "I", CommonUtilities.getExecutedMethodName()+" file viewer item added, pos="+adapter.getPosition(fp)+", fp="+fp);
	};
	
	private ZipFileViewerItem getZipFileViewerItem(String fp) {
		for(ZipFileViewerItem fvi:zipFileViewerList) {
			if (fvi.file_path.equals(fp)) {
				mUtil.addDebugMsg(2, "I", CommonUtilities.getExecutedMethodName()+" found, fp="+fp);
				return fvi;
			}
		}
		mUtil.addDebugMsg(2, "I", CommonUtilities.getExecutedMethodName()+" not found, fp="+fp);
		return null;
	};

	private void addZipFileViewerItem(boolean temp_file, String fp) {
		ZipFileViewerItem n_fvi=new ZipFileViewerItem();
		n_fvi.file_path=fp;
//		n_fvi.temporary_file=temp_file;
		n_fvi.read_only_file=temp_file;
		zipFileViewerList.add(n_fvi);
		Collections.sort(zipFileViewerList, new Comparator<ZipFileViewerItem>(){
			@Override
			public int compare(ZipFileViewerItem lhs, ZipFileViewerItem rhs) {
				return lhs.file_path.compareToIgnoreCase(rhs.file_path);
			}
		});
		mUtil.addDebugMsg(2, "I", CommonUtilities.getExecutedMethodName()+" file viewer item added, fp="+fp);
	};
	
	private void refreshZipFileSpinner(SafFile3 fp) {
        CustomSpinnerAdapter adapter=(CustomSpinnerAdapter)mZipFileSpinner.getAdapter();
		adapter.clear();
		for(ZipFileViewerItem zfi:zipFileViewerList) {
			adapter.add(zfi.file_path);
		}
		mZipFileSpinner.setSelection(adapter.getPosition(fp.getPath()), false);
		adapter.notifyDataSetChanged();
	};
	
	public void cleanZipFileManager() {
		zipFileViewerList=null;
		mZipFileList=null;
		mTreeFilelistAdapter.setDataList(new ArrayList<TreeFilelistItem>());
		mCommonDlg=null;
        mUiHandler=null;
        mContext=null;
        mFragmentManager=null;
        mUtil=null;
	};

	public void saveZipFile() {
        NotifyEvent ntfy_select_dest=new NotifyEvent(mContext);
        ntfy_select_dest.setListener(new NotifyEventListener() {
            @Override
            public void positiveResponse(Context context, Object[] objects) {
                Uri uri=(Uri)objects[0];
                SafFile3 out_file=new SafFile3(mContext, uri);
                NotifyEvent ntfy_confirm=new NotifyEvent(mContext);
                ntfy_confirm.setListener(new NotifyEventListener() {
                    @Override
                    public void positiveResponse(Context context, Object[] objects) {
                        SafFile3 in_file=new SafFile3(mContext, mCurrentFilePath);
                        setUiDisabled();
                        showDialogProgress();
                        putProgressMessage(mContext.getString(R.string.msgs_zip_write_zip_file_writing));
                        final ThreadCtrl tc=new ThreadCtrl();
                        mDialogProgressSpinCancel.setEnabled(true);
                        mDialogProgressSpinMsg1.setVisibility(TextView.GONE);
                        mDialogProgressSpinCancel.setOnClickListener(new OnClickListener(){
                            @Override
                            public void onClick(View v) {
                                confirmCancel(tc,mDialogProgressSpinCancel);
                            }
                        });
                        Thread th=new Thread() {
                            @Override
                            public void run() {
                                try {
                                    File tmp=new File(out_file.getAppDirectoryCache()+"/"+out_file.getName());
                                    InputStream is=in_file.getInputStream();
                                    OutputStream os=new FileOutputStream(tmp);
                                    int rc=0;
                                    byte[] buff=new byte[1024*1024*2];
                                    while((rc=is.read(buff))>0) {
                                        if (!tc.isEnabled()) {
                                            is.close();
                                            os.flush();
                                            os.close();
                                            tmp.delete();
                                            break;
                                        }
                                        os.write(buff, 0, rc);
                                    }
                                    if (tc.isEnabled()) {
                                        is.close();
                                        os.flush();
                                        os.close();
                                        if (Build.VERSION.SDK_INT>=SCOPED_STORAGE_SDK) {
                                            SafFile3 tmp_sf=new SafFile3(mContext, tmp.getPath());
                                            if (out_file.exists()) out_file.delete();
                                            tmp_sf.moveTo(out_file);
                                        } else {
                                            if (out_file.getPath().startsWith(SafFile3.SAF_FILE_PRIMARY_STORAGE_PREFIX)) {
//                                                File out_of_file=new File(out_file.getPath());
                                                if (out_file.exists()) out_file.delete();
                                                tmp.renameTo(out_file.getFile());
                                            } else {
                                                SafFile3 tmp_sf=new SafFile3(mContext, tmp.getPath());
                                                if (out_file.exists()) out_file.delete();
                                                tmp_sf.moveTo(out_file);
                                            }
                                        }
                                    } else {
                                        mCommonDlg.showCommonDialog(false, "W",
                                                mContext.getString(R.string.msgs_zip_write_zip_file_canelled), out_file.getPath(), null);
                                    }
                                    mUiHandler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            setUiEnabled();
                                            hideDialog();
                                        }
                                    });
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        };
                        th.start();
                    }

                    @Override
                    public void negativeResponse(Context context, Object[] objects) {

                    }
                });
                if (out_file.exists()) {
                    mCommonDlg.showCommonDialog(true, "W",
                            mContext.getString(R.string.msgs_zip_write_zip_file_confirm_override), out_file.getPath(), ntfy_confirm);
                } else {
                    ntfy_confirm.notifyToListener(true, null);
                }
            }

            @Override
            public void negativeResponse(Context context, Object[] objects) {

            }
        });
        boolean include_root=false;
        boolean scoped_storage_mode=mGp.safMgr.isScopedStorageMode();
        CommonFileSelector2 fsdf=
                CommonFileSelector2.newInstance(scoped_storage_mode, true, false, CommonFileSelector2.DIALOG_SELECT_CATEGORY_FILE,
                        true, false, SafFile3.SAF_FILE_PRIMARY_UUID, "", "", "Select destination file");
        fsdf.showDialog(false, mActivity.getSupportFragmentManager(), fsdf, ntfy_select_dest);

    }

	public void showZipFile(boolean read_only, SafFile3 in_file) {
		if (!isUiEnabled()) return;
		Bundle bd=new Bundle();
//        mCurretnFileIsReadOnly =read_only;
		if (in_file!=null) {
		    if (!read_only) {
		        if (Build.VERSION.SDK_INT>=SCOPED_STORAGE_SDK) {
		            if (in_file.getUuid().equals(SafFile3.SAF_FILE_UNKNOWN_UUID)) {
                        mCurretnFileIsReadOnly =false;
                    } else {
                        if (!SafManager3.isUuidRegistered(mContext, in_file.getUuid())) mCurretnFileIsReadOnly =true;
                    }
                } else {
                    if (in_file.getUuid().equals(SafFile3.SAF_FILE_UNKNOWN_UUID)) {
                        mCurretnFileIsReadOnly =false;
                    } else if (!in_file.getUuid().equals(SafFile3.SAF_FILE_PRIMARY_UUID)) {
                        if (!SafManager3.isUuidRegistered(mContext, in_file.getUuid())) mCurretnFileIsReadOnly =true;
                    }
                }
            }
			String fid=CommonUtilities.getFileExtention(in_file.getName());
			if (fid.equals("gz") || fid.equals("tar") || fid.equals("tgz")) {
				mCommonDlg.showCommonDialog(false, "W", mContext.getString(R.string.msgs_zip_open_file_not_supported_file), "", null);
			} else {
				String s_fp=saveViewContents(bd);
				mUtil.addDebugMsg(2, "I", CommonUtilities.getExecutedMethodName()+" saved file path="+s_fp);
				mZipFileSpinner.setOnItemSelectedListener(null);
				if (!s_fp.equals("")) {
					saveZipFileViewerItem(s_fp, bd);
					ZipFileViewerItem fvi= getZipFileViewerItem(in_file.getPath());
					if (fvi!=null) {
					    mCurretnFileIsReadOnly=fvi.read_only_file;
						refreshZipFileSpinner(in_file);
						restoreViewContents(in_file.getPath(), fvi.saved_data);
					} else {
                        mCurretnFileIsReadOnly=read_only;
						addZipFileViewerItem(read_only, in_file.getPath());
						mCurrentFilePath=in_file.getPath();
						mCurrentDirectory.setText("/");
						refreshFileList(true);
						refreshZipFileSpinner(in_file);
					}
				} else {
                    mCurretnFileIsReadOnly=read_only;
					addZipFileViewerItem(read_only, in_file.getPath());
					mCurrentFilePath=in_file.getPath();
					mCurrentDirectory.setText("/");
					refreshFileList(true);
					refreshZipFileSpinner(in_file);
				}
				mZipFileSpinner.setOnItemSelectedListener(new OnItemSelectedListener(){
					@Override
					public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
						String n_fp=mZipFileSpinner.getSelectedItem().toString();
                        ZipFileViewerItem fvi= getZipFileViewerItem(n_fp);
                        if (fvi!=null) {
//                            mCurretnFileIsReadOnly=fvi.read_only_file;
                            showZipFile(fvi.read_only_file, new SafFile3(mContext, n_fp));
                        } else {
                            showZipFile(false, new SafFile3(mContext, n_fp));
                        }
					}
					@Override
					public void onNothingSelected(AdapterView<?> parent) {}
				});
			}
		} else {
		}
	};

	class ZipFileViewerItem {
		public String file_path="";
//        public boolean temporary_file=false;
        public Bundle saved_data=null;
        public boolean read_only_file=false;
	};

	class SavedViewData implements Externalizable {
		private static final long serialVersionUID = 1L;
		public ArrayList<ZipFileListItem> zfl=new ArrayList<ZipFileListItem>();
		public ArrayList<TreeFilelistItem> tfl=new ArrayList<TreeFilelistItem>();
		public int tree_list_pos_x=0, tree_list_pos_y=0;
		public String curr_dir="", encoding_desired="", encoding_selected="";
		public int encoding_spinner_pos=0;
		public long file_last_modified=0;
		public long file_length=0;
        public boolean temporary_file=true;
		public boolean sort_ascendant=true;
		public boolean sort_key_name=true;
		public boolean sort_key_size=false;
		public boolean sort_key_time=false;
		
		public SavedViewData() {};
		
		@Override
		public void readExternal(ObjectInput input) throws IOException,
                ClassNotFoundException {
			int tc=input.readInt();
			zfl=new ArrayList<ZipFileListItem>();
			if (tc>0) for(int i=0;i<tc;i++) zfl.add((ZipFileListItem)input.readObject());
			tc=input.readInt();
			tfl=new ArrayList<TreeFilelistItem>();
			if (tc>0) for(int i=0;i<tc;i++) tfl.add((TreeFilelistItem)input.readObject());
			tree_list_pos_x=input.readInt();
			tree_list_pos_y=input.readInt();
			curr_dir=input.readUTF();
			encoding_desired=input.readUTF();
			encoding_selected=input.readUTF();
			encoding_spinner_pos=input.readInt();
			file_last_modified=input.readLong();
			file_length=input.readLong();
			
			sort_ascendant=input.readBoolean();
			sort_key_name=input.readBoolean();
			sort_key_size=input.readBoolean();
			sort_key_time=input.readBoolean();

            temporary_file=input.readBoolean();
		}
		
		@Override
		public void writeExternal(ObjectOutput output) throws IOException {
			output.writeInt(zfl.size());
			for(ZipFileListItem zi:zfl) output.writeObject(zi);
			output.writeInt(tfl.size());
			for(TreeFilelistItem ti:tfl) output.writeObject(ti);
			output.writeInt(tree_list_pos_x);
			output.writeInt(tree_list_pos_y);
			output.writeUTF(curr_dir);
			output.writeUTF(encoding_desired);
			output.writeUTF(encoding_selected);
			output.writeInt(encoding_spinner_pos);
			output.writeLong(file_last_modified);
			output.writeLong(file_length);
			
			output.writeBoolean(sort_ascendant);
			output.writeBoolean(sort_key_name);
			output.writeBoolean(sort_key_size);
			output.writeBoolean(sort_key_time);

			output.writeBoolean(temporary_file);
		}
	};
	
	private static final String SAVE_VIEW_CONTENT_KEY="saved_data";
	public String saveViewContents(Bundle bd) {
		SavedViewData sv=new SavedViewData();
		ByteArrayOutputStream bos=new ByteArrayOutputStream();
		ObjectOutputStream oos;
		try {
			oos = new ObjectOutputStream(bos);

			if (mZipFileList!=null) sv.zfl=mZipFileList;
			if (mTreeFilelistAdapter!=null) sv.tfl=mTreeFilelistAdapter.getDataList();
			if (mTreeFilelistView!=null) {
			    sv.tree_list_pos_x=mTreeFilelistView.getFirstVisiblePosition();
			    sv.tree_list_pos_y=mTreeFilelistView.getChildAt(0)==null?0:mTreeFilelistView.getChildAt(0).getTop();
			}
			sv.curr_dir=mCurrentDirectory.getText().toString();
			sv.encoding_desired=mEncodingDesired;
			sv.encoding_selected=mEncodingSelected;
			sv.encoding_spinner_pos=mEncodingSpinner.getSelectedItemPosition();
			sv.file_last_modified=mCurrentFileLastModified;
			sv.file_length=mCurrentFileLength;

			if (mTreeFilelistAdapter!=null) {
				sv.sort_ascendant=mTreeFilelistAdapter.isSortAscendant();
				sv.sort_key_name=mTreeFilelistAdapter.isSortKeyName();
				sv.sort_key_size=mTreeFilelistAdapter.isSortKeySize();
				sv.sort_key_time=mTreeFilelistAdapter.isSortKeyTime();
			}

			sv.temporary_file= mCurretnFileIsReadOnly;

			sv.writeExternal(oos);
			oos.flush();
			oos.close();
			byte[] ba=bos.toByteArray();
			bd.putByteArray(SAVE_VIEW_CONTENT_KEY, ba);
		} catch (IOException e) {
			e.printStackTrace();
			mUtil.addLogMsg("I", e.getMessage());
			CommonUtilities.printStackTraceElement(mUtil, e.getStackTrace());
		}
		return mCurrentFilePath;
	};

	public void restoreViewContents(String fp, Bundle bd) {
		byte[] ba=bd.getByteArray(SAVE_VIEW_CONTENT_KEY);
		ByteArrayInputStream bis=new ByteArrayInputStream(ba);
		SavedViewData sv=new SavedViewData();
		sv.encoding_desired=mContext.getString(R.string.msgs_zip_parm_zip_encoding_auto);
		sv.encoding_selected=ENCODING_NAME_UTF8;
		try {
			ObjectInputStream ois=new ObjectInputStream(bis);
			sv.readExternal(ois);
			ois.close();
		} catch (StreamCorruptedException e) {
			mUtil.addLogMsg("I", e.getMessage());
			CommonUtilities.printStackTraceElement(mUtil, e.getStackTrace());
		} catch (IOException e) {
			mUtil.addLogMsg("I", e.getMessage());
			CommonUtilities.printStackTraceElement(mUtil, e.getStackTrace());
		} catch (ClassNotFoundException e) {
			mUtil.addLogMsg("I", e.getMessage());
			CommonUtilities.printStackTraceElement(mUtil, e.getStackTrace());
		}
		mCurrentFilePath=fp;
		mCurrentDirectory.setText(sv.curr_dir);
		mEncodingDesired=sv.encoding_desired;
		mEncodingSelected=sv.encoding_selected;
		mEncodingSpinner.setSelection(sv.encoding_spinner_pos);
		mCurrentFileLastModified=sv.file_last_modified;
		mCurrentFileLength=sv.file_length;
        mCurretnFileIsReadOnly =sv.temporary_file;

		if (sv.sort_ascendant) mTreeFilelistAdapter.setSortAscendant();
		else mTreeFilelistAdapter.setSortDescendant();
		if (sv.sort_key_name) mTreeFilelistAdapter.setSortKeyName();
		else if (sv.sort_key_size) mTreeFilelistAdapter.setSortKeySize();
		else if (sv.sort_key_time) mTreeFilelistAdapter.setSortKeyTime();

		if (isZipFileChanged()) {
			refreshFileList();
		} else {
			String cdir=mCurrentDirectory.getText().toString();
			String target_dir="";
			if (cdir.length()>0) target_dir=cdir.substring(1);
			mZipFileList=sv.zfl;
			mTreeFilelistAdapter.setDataList(sv.tfl);
			refreshFileListView(target_dir, fp);
		}
		mTreeFilelistAdapter.notifyDataSetChanged();
		if (mTreeFilelistAdapter.getCount()>0) mTreeFilelistView.setSelectionFromTop(sv.tree_list_pos_x, sv.tree_list_pos_y);
		mActivity.refreshOptionMenu();
	};
	
	private String mFindKey="*";
	private AdapterSearchFileList mAdapterSearchFileList=null;
	private int mSearchListPositionX=0;
	private int mSearchListPositionY=0;
	private String mSearchRootDir="";
//	private int mSearchSortKey=0;
	public boolean isZipFileLoaded() {
		return mZipFileList!=null?true:false;
	};
	
	public void sortFileList() {
		CommonUtilities.sortFileList(mActivity, mGp, mTreeFilelistAdapter, null);	
	};
	
	public void searchFile() {
		if (mTreeFilelistAdapter==null) return;
		
		final Dialog dialog = new Dialog(mActivity, mGp.applicationTheme);
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialog.getWindow().setSoftInputMode(LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
		dialog.setContentView(R.layout.search_file_dlg);
		final LinearLayout dlg_view = (LinearLayout) dialog.findViewById(R.id.search_file_dlg_view);
//		dlg_view.setBackgroundResource(R.drawable.dialog_border_dark);
		final LinearLayout title_view = (LinearLayout) dialog.findViewById(R.id.search_file_dlg_title_view);
		title_view.setBackgroundColor(mGp.themeColorList.title_background_color);

		final TextView dlg_title = (TextView) dialog.findViewById(R.id.search_file_dlg_title);
		dlg_title.setTextColor(mGp.themeColorList.title_text_color);

		final ImageButton ib_sort=(ImageButton) dialog.findViewById(R.id.search_file_dlg_sort_btn);
//		ib_sort.setBackgroundColor(mGp.themeColorList.dialog_title_background_color);
		final CheckedTextView dlg_hidden = (CheckedTextView) dialog.findViewById(R.id.search_file_dlg_search_hidden_item);
		dlg_hidden.setVisibility(CheckedTextView.GONE);

		final CheckedTextView dlg_case_sensitive = (CheckedTextView) dialog.findViewById(R.id.search_file_dlg_search_case_sensitive);
		CommonUtilities.setCheckedTextView(dlg_case_sensitive);

//		final TextView dlg_msg = (TextView) dialog.findViewById(R.id.search_file_dlg_msg);
		final Button btnOk = (Button) dialog.findViewById(R.id.search_file_dlg_ok_btn);
		final Button btnCancel = (Button) dialog.findViewById(R.id.search_file_dlg_cancel_btn);
		final EditText et_search_key=(EditText) dialog.findViewById(R.id.search_file_dlg_search_key);
		final ListView lv_search_result=(ListView) dialog.findViewById(R.id.search_file_dlg_search_result);

		final TextView searcgh_info=(TextView) dialog.findViewById(R.id.search_file_dlg_search_info);

		if (mAdapterSearchFileList==null) {
			mAdapterSearchFileList=new AdapterSearchFileList(mActivity);
			lv_search_result.setAdapter(mAdapterSearchFileList);
			searcgh_info.setText("");
		} else {
			if (!mCurrentFilePath.equals(mSearchRootDir)) {
				mAdapterSearchFileList=new AdapterSearchFileList(mActivity);
				lv_search_result.setAdapter(mAdapterSearchFileList);
			} else {
				lv_search_result.setAdapter(mAdapterSearchFileList);
				lv_search_result.setSelectionFromTop(mSearchListPositionX, mSearchListPositionY);
				if (mAdapterSearchFileList.isSortAscendant()) ib_sort.setImageResource(R.drawable.ic_128_sort_asc_gray);
				else ib_sort.setImageResource(R.drawable.ic_128_sort_dsc_gray);
				long s_size=0;
				for(TreeFilelistItem tfi:mAdapterSearchFileList.getDataList()) s_size+=tfi.getLength();
				String msg=mContext.getString(R.string.msgs_search_file_dlg_search_result);
				searcgh_info.setText(String.format(msg,mAdapterSearchFileList.getDataList().size(),s_size));
			}
		}

		CommonDialog.setDlgBoxSizeLimit(dialog, true);

		ib_sort.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				final CustomTreeFilelistAdapter tfa=new CustomTreeFilelistAdapter(mActivity,false,false);
				NotifyEvent ntfy_sort=new NotifyEvent(mContext);
				ntfy_sort.setListener(new NotifyEventListener(){
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

		lv_search_result.setOnItemLongClickListener(new OnItemLongClickListener(){
			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
				final CustomTreeFilelistAdapter n_tfa=new CustomTreeFilelistAdapter(mActivity, false, false, false);
				ArrayList<TreeFilelistItem> n_tfl=new ArrayList<TreeFilelistItem>();
				final TreeFilelistItem n_tfli=mAdapterSearchFileList.getItem(position).clone();
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

				mCcMenu.addMenuItem(mContext.getString(R.string.msgs_main_local_file_ccmenu_open_file)+"("+(n_tfl.get(0)).getName()+")")
			  		.setOnClickListener(new CustomContextMenuOnClickListener() {
					@Override
					public void onClick(CharSequence menuTitle) {
						invokeBrowser(n_tfli.isZipEncrypted(), n_tfli, n_tfli.getPath(), n_tfli.getName(), "");
						btnCancel.performClick();
					}
			  	});
				mCcMenu.addMenuItem(mContext.getString(R.string.msgs_main_local_file_ccmenu_force_zip)+"("+(n_tfl.get(0)).getName()+")",R.drawable.ic_32_file_zip)
		  			.setOnClickListener(new CustomContextMenuOnClickListener() {
					@Override
					public void onClick(CharSequence menuTitle) {
						invokeBrowser(n_tfli.isZipEncrypted(), n_tfli, n_tfli.getPath(), n_tfli.getName(), MIME_TYPE_ZIP);
						btnCancel.performClick();
					}
		  		});
				mCcMenu.addMenuItem(mContext.getString(R.string.msgs_main_local_file_ccmenu_force_text)+"("+(n_tfl.get(0)).getName()+")",R.drawable.cc_sheet)
		  			.setOnClickListener(new CustomContextMenuOnClickListener() {
					@Override
					public void onClick(CharSequence menuTitle) {
						invokeBrowser(n_tfli.isZipEncrypted(), n_tfli, n_tfli.getPath(), n_tfli.getName(), MIME_TYPE_TEXT);
						btnCancel.performClick();
					}
		  		});
				mCcMenu.createMenu();

				mSearchListPositionX=lv_search_result.getFirstVisiblePosition();
				mSearchListPositionY=lv_search_result.getChildAt(0)==null?0:lv_search_result.getChildAt(0).getTop();

				return true;
			}
		});

		et_search_key.addTextChangedListener(new TextWatcher(){
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {}
			@Override
			public void afterTextChanged(Editable s) {
				if (s.length()>0) CommonDialog.setButtonEnabled(mActivity, btnOk, true);
				else CommonDialog.setButtonEnabled(mActivity, btnOk, false);
			}
		});

		lv_search_result.setOnItemClickListener(new OnItemClickListener(){
			@SuppressLint("DefaultLocale")
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				TreeFilelistItem tfi=mAdapterSearchFileList.getItem(position);
				openSppecificDirectory(tfi.getPath(), tfi.getName());
				mSearchListPositionX=lv_search_result.getFirstVisiblePosition();
				mSearchListPositionY=lv_search_result.getChildAt(0)==null?0:lv_search_result.getChildAt(0).getTop();
				btnCancel.performClick();
//				String fid=CommonUtilities.getFileExtention(tfi.getName());
//				String mt=MimeTypeMap.getSingleton().getMimeTypeFromExtension(fid);
//				invokeBrowser(tfi.getPath(), tfi.getName(), "");
////				Log.v("","mt="+mt);
//				if (mt!=null && mt.startsWith("application/zip")) {
//					btnCancel.performClick();
//				}
			}
		});

		et_search_key.setText(mFindKey);
		//OK button
//		btnOk.setEnabled(false);
		btnOk.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				mFindKey=et_search_key.getText().toString();
				final ArrayList<TreeFilelistItem> s_tfl=new ArrayList<TreeFilelistItem>();
				int flags = 0;//Pattern.CASE_INSENSITIVE;// | Pattern..MULTILINE;
				if (!dlg_case_sensitive.isChecked()) flags= Pattern.CASE_INSENSITIVE;
				final Pattern s_key= Pattern.compile("(" + MiscUtil.convertRegExp(mFindKey) + ")", flags);
				final ThreadCtrl tc=new ThreadCtrl();
				final ProgressSpinDialogFragment psd=ProgressSpinDialogFragment.newInstance(
						mContext.getString(R.string.msgs_search_file_dlg_searching), "",
						mContext.getString(R.string.msgs_common_dialog_cancel),
						mContext.getString(R.string.msgs_common_dialog_canceling));

				NotifyEvent ntfy=new NotifyEvent(mContext);
				ntfy.setListener(new NotifyEventListener(){
					@Override
					public void positiveResponse(Context c, Object[] o) {}
					@Override
					public void negativeResponse(Context c, Object[] o) {
						tc.setDisabled();
						if (!tc.isEnabled()) psd.dismissAllowingStateLoss();
					}
				});
				psd.showDialog(mFragmentManager, psd, ntfy,true);
				Thread th=new Thread(){
					@Override
					public void run() {
						buildFileListBySearchKey(tc, psd, s_tfl, s_key);
						psd.dismissAllowingStateLoss();
						if (!tc.isEnabled()) {
							mCommonDlg.showCommonDialog(false, "W",
									mContext.getString(R.string.msgs_search_file_dlg_search_cancelled), "", null);
						} else {
//							mCommonDlg.showCommonDialog(false, "W",
//									String.format(mContext.getString(R.string.msgs_search_file_dlg_search_found), s_tfl.size()), "", null);
							mAdapterSearchFileList.setDataList(s_tfl);
							mSearchRootDir=mCurrentFilePath;
							mUiHandler.post(new Runnable(){
								@Override
								public void run() {
									long s_size=0;
									for(TreeFilelistItem tfi:mAdapterSearchFileList.getDataList()) s_size+=tfi.getLength();
									String msg=mContext.getString(R.string.msgs_search_file_dlg_search_result);
									searcgh_info.setText(String.format(msg,mAdapterSearchFileList.getDataList().size(),s_size));
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
		// CANCEL�{�^���̎w��
		btnCancel.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
//				saveSearchResultList(mAdapterSearchFileList.getDataList());
				dialog.dismiss();
			}
		});

		dialog.show();
	};

//	private void saveSearchResultList(ArrayList<TreeFilelistItem>fl) {
//		try {
//			FileOutputStream fos=new FileOutputStream(mGp.applicationCacheDirectory+"/ZipSearchList");
//			BufferedOutputStream bos=new BufferedOutputStream(fos,1024*1024*4);
//			ObjectOutputStream oos=new ObjectOutputStream(bos);
//			oos.writeObject(fl);
//			oos.flush();
//			oos.close();
//		} catch (FileNotFoundException e) {
//			e.printStackTrace();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//	};
//
//	@SuppressWarnings("unchecked")
//	private ArrayList<TreeFilelistItem> loadSearchResultList() {
//		ArrayList<TreeFilelistItem>fl=null;
//		try {
//			FileInputStream fis=new FileInputStream(mGp.applicationCacheDirectory+"/ZipSearchList");
//			BufferedInputStream bis=new BufferedInputStream(fis,1024*1024*4);
//			ObjectInputStream ois=new ObjectInputStream(bis);
//			fl=(ArrayList<TreeFilelistItem>) ois.readObject();
//			ois.close();
//		} catch (FileNotFoundException e) {
//			e.printStackTrace();
//		} catch (IOException e) {
//			e.printStackTrace();
//		} catch (ClassNotFoundException e) {
//			e.printStackTrace();
//		}
//		return fl;
//	};


	private void buildFileListBySearchKey(final ThreadCtrl tc, ProgressSpinDialogFragment psd,
                                          ArrayList<TreeFilelistItem> s_tfl, Pattern s_key) {
		if (isZipFileLoaded()) {
			int list_size=mZipFileList.size();
			int progress=0, proc_count=0;
			int prev_prog=0;
			for(ZipFileListItem zfli:mZipFileList) {
				if (!zfli.isDirectory()) {

					String fn=zfli.getFileName().lastIndexOf("/")>=0?zfli.getFileName().substring(zfli.getFileName().lastIndexOf("/")+1):zfli.getFileName();
					if (s_key.matcher(fn).matches()) {
						TreeFilelistItem tfli=createNewFileListItem(zfli);
						s_tfl.add(tfli);
					}
				}
				proc_count++;
				progress=(proc_count*100)/list_size;
				if (prev_prog!=progress) {
					prev_prog=progress;
					psd.updateMsgText(String.format(
							mContext.getString(R.string.msgs_search_file_dlg_search_progress), progress));
				}
			}
		}
	};

	public void refreshFileList() {
		refreshFileList(false);
	};
	public void refreshFileList(boolean force) {
		if (!isUiEnabled()) return;
		if (!mCurrentFilePath.equals("")) {
			SafFile3 lf=new SafFile3(mContext, mCurrentFilePath);
			if (!lf.exists()) {
				mCommonDlg.showCommonDialog(false, "W",
						String.format(mContext.getString(R.string.msgs_zip_zip_file_was_not_found),mCurrentFilePath),
						"", null);
			}
		}
		if (isZipFileChanged() || force) {
			final String cdir=mCurrentDirectory.getText().toString();
//			if (mCurrentFilePath.startsWith(mGp.externalRootDirectory)) {
//				if (mGp.safMgr.getSdcardRootSafFile()==null) mActivity.startSdcardPicker();
//			}
			ArrayList<TreeFilelistItem> p_tfl=null;
			if (mTreeFilelistAdapter!=null) p_tfl=mTreeFilelistAdapter.getDataList();
			if (cdir.length()>0) createFileList(mCurrentFilePath,null,cdir.substring(1));
			else createFileList(mCurrentFilePath,null,"");
			ArrayList<TreeFilelistItem> n_tfl=null;
			if (mTreeFilelistAdapter!=null) n_tfl=mTreeFilelistAdapter.getDataList();
			if (p_tfl!=null && n_tfl!=null) {
				for(TreeFilelistItem n_tfli:n_tfl) {
					for(TreeFilelistItem p_tfli:p_tfl) {
						if (n_tfli.getName().equals(p_tfli.getName())) {
							n_tfli.setChecked(p_tfli.isChecked());
							break;
						}
					}
				}
				mTreeFilelistAdapter.setDataList(n_tfl);
			}
//		} else {
		}
		setContextCopyCutPasteButton(mTreeFilelistAdapter);
	};

	private boolean isZipFileChanged() {
	    if (mCurrentFilePath.equals("")) return false;
	    boolean result=false;
		SafFile3 lf=new SafFile3(mContext, mCurrentFilePath);
//		mUtil.addDebugMsg(2, "I", "File exists="+lf.exists()+", savedLastMod="+mCurrentFileLastModified+", savedLength="+mCurrentFileLength+
//				", lastMod="+lf.lastModified()+", length="+lf.length());
        if (mCurrentFileLastModified!=lf.lastModified() || mCurrentFileLength!=lf.length()) result=true;
        mCurrentFileLastModified=lf.lastModified();
        mCurrentFileLength=lf.length();
		return result;
	};

	private void setContextCopyCutPasteButton(CustomTreeFilelistAdapter tfa) {
		if (!mCurrentFilePath.equals("") && !mCurretnFileIsReadOnly) {
			if (mGp.copyCutList.size()>0) {
				if (tfa.isItemSelected()) {
					mContextButtonPasteView.setVisibility(LinearLayout.INVISIBLE);
				} else {
					String c_dir=mCurrentDirectory.getText().length()==0?"":mCurrentDirectory.getText().toString().substring(1);
					String zip_path=mZipFileSpinner.getSelectedItem()==null?mCurrentFilePath:mZipFileSpinner.getSelectedItem().toString();
					if (isCopyCutDestValid(zip_path, c_dir)) mContextButtonPasteView.setVisibility(LinearLayout.VISIBLE);
					else mContextButtonPasteView.setVisibility(LinearLayout.INVISIBLE);
				}
			} else {
				mContextButtonPasteView.setVisibility(LinearLayout.INVISIBLE);
			}
			if (tfa.isItemSelected()) {
				mContextButtonCopyView.setVisibility(LinearLayout.VISIBLE);
				mContextButtonCutView.setVisibility(LinearLayout.VISIBLE);
			} else {
				mContextButtonCopyView.setVisibility(LinearLayout.INVISIBLE);
				mContextButtonCutView.setVisibility(LinearLayout.INVISIBLE);
			}
		} else {
			mContextButtonPasteView.setVisibility(LinearLayout.INVISIBLE);
			mContextButtonCopyView.setVisibility(LinearLayout.INVISIBLE);
			mContextButtonCutView.setVisibility(LinearLayout.INVISIBLE);
		}
	};

	private void refreshFileListView(String target_dir, String fp) {
		if (mZipFileList!=null && mZipFileList.size()>0) {
			mCurrentDirectory.setText("/"+target_dir);
			mCurrentDirectory.setVisibility(TextView.VISIBLE);
			setZipTreeFileListener();
			mTreeFilelistView.setVisibility(ListView.VISIBLE);
			mFileEmpty.setVisibility(TextView.GONE);
			mContextButton.setVisibility(ListView.VISIBLE);
			mFileListUp.setVisibility(Button.VISIBLE);
			mFileListTop.setVisibility(Button.VISIBLE);
			if (target_dir.equals("")) {
				//Root
				setTopUpButtonEnabled(false);
			} else {
				setTopUpButtonEnabled(true);
			}

			if (mTreeFilelistAdapter.isItemSelected() && !mCurretnFileIsReadOnly) mContextButtonDeleteView.setVisibility(LinearLayout.VISIBLE);
	        else mContextButtonDeleteView.setVisibility(LinearLayout.INVISIBLE);
	        mContextButtonExtractView.setVisibility(LinearLayout.VISIBLE);
			if (mTreeFilelistAdapter.isAllItemSelected()) mContextButtonSelectAllView.setVisibility(ImageButton.INVISIBLE);
			else mContextButtonSelectAllView.setVisibility(ImageButton.VISIBLE);
			if (mTreeFilelistAdapter.getSelectedItemCount()==0) mContextButtonUnselectAllView.setVisibility(ImageButton.INVISIBLE);
			else mContextButtonUnselectAllView.setVisibility(ImageButton.VISIBLE);

			setContextCopyCutPasteButton(mTreeFilelistAdapter);
		} else {
			mTreeFilelistView.setVisibility(ListView.GONE);
			mFileEmpty.setVisibility(TextView.VISIBLE);
			mFileEmpty.setText(R.string.msgs_zip_zip_folder_empty);
			mCurrentDirectory.setVisibility(TextView.GONE);
			mFileListUp.setVisibility(Button.GONE);
			mFileListTop.setVisibility(Button.GONE);

	    	mContextButtonNewView.setVisibility(LinearLayout.VISIBLE);
	    	mContextButtonOpenView.setVisibility(LinearLayout.VISIBLE);
	    	mContextButtonDeleteView.setVisibility(LinearLayout.INVISIBLE);
	    	mContextButtonExtractView.setVisibility(LinearLayout.INVISIBLE);
	    	mContextButtonSelectAllView.setVisibility(LinearLayout.INVISIBLE);
	    	mContextButtonUnselectAllView.setVisibility(LinearLayout.INVISIBLE);
			setContextCopyCutPasteButton(mTreeFilelistAdapter);
		}
		mZipFileSpinner.setVisibility(TextView.VISIBLE);
		mEncodingSpinner.setVisibility(Spinner.VISIBLE);
		mFileInfo.setVisibility(TextView.VISIBLE);

		SafFile3 lf=new SafFile3(mContext, fp);
		String tfs=MiscUtil.convertFileSize(lf.length());
		int size=mZipFileList==null?0:mZipFileList.size();
		String info= String.format(mContext.getString(R.string.msgs_zip_zip_file_info),tfs, size, mEncodingSelected);
        String read_only= mCurretnFileIsReadOnly ?mContext.getString(R.string.msgs_zip_zip_file_info_temporary):"";
        if (mCurretnFileIsReadOnly) {
            SpannableStringBuilder sb=new SpannableStringBuilder(read_only+" "+info);
//        BackgroundColorSpan bg_span = new BackgroundColorSpan(mHighlightBackgrounColor);
            ForegroundColorSpan fg_span = new ForegroundColorSpan(mGp.themeColorList.text_color_error);
//        sb.setSpan(bg_span, mt.start(), mt.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            sb.setSpan(fg_span, 0, read_only.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            mFileInfo.setText(sb);
        } else {
            mFileInfo.setText(info);
        }
	};

	private void createFileList(final String fp, final NotifyEvent p_ntfy, final String target_dir) {
		mUtil.addDebugMsg(1, "I", "createFileList entered, fp="+fp+", target="+target_dir);
        setUiDisabled();
        showDialogProgress();
        final ThreadCtrl tc=new ThreadCtrl();
        mDialogProgressSpinCancel.setEnabled(true);
        mDialogProgressSpinMsg1.setVisibility(TextView.GONE);
        mDialogProgressSpinCancel.setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View v) {
                confirmCancel(tc,mDialogProgressSpinCancel);
            }
        });
        putProgressMessage(mContext.getString(R.string.msgs_zip_file_create_file_list_title));
        mFileEmpty.setText("");//.setVisibility(TextView.GONE);
        Thread th=new Thread() {
            @Override
            public void run() {
                SafFile3 lf=new SafFile3(mContext, fp);
                if (!lf.exists()) {
                    mCurrentFileLastModified=0;
                    mCurrentFileLength=0;
                } else {
                    mCurrentFileLastModified=lf.lastModified();
                    mCurrentFileLength=lf.length();
                }
                mUtil.addDebugMsg(2, "I", "createFileList begin");
                final NotifyEvent ntfy_create_file_list=new NotifyEvent(mContext);
                ntfy_create_file_list.setListener(new NotifyEventListener(){
                    @Override
                    public void positiveResponse(Context c, Object[] o) {
                        mUiHandler.post(new Runnable(){
                            @Override
                            public void run() {
                                if (mZipFileList!=null){// && mZipFileList.size()>0) {
                                    ArrayList<TreeFilelistItem> tfl=createTreeFileList(mZipFileList, target_dir);
                                    mTreeFilelistAdapter.setDataList(tfl);
                                }
                                mUtil.addDebugMsg(2, "I", "createFileList Tree file list adapetr created");
                                refreshFileListView(target_dir, fp);
                                setUiEnabled();
                                if (p_ntfy!=null) p_ntfy.notifyToListener(true, null);
                                mUtil.addDebugMsg(2, "I", "createFileList end");
                            }
                        });
                    }
                    @Override
                    public void negativeResponse(Context c, Object[] o) {
                        mUiHandler.post(new Runnable(){
                            @Override
                            public void run() {
                                if (mFileInfo.getVisibility()!= TextView.VISIBLE)
                                    mFileEmpty.setText(R.string.msgs_zip_folder_not_specified);
                                setUiEnabled();
                                mUtil.addDebugMsg(1, "I", "createFileList end");
                            }
                        });
                    }
                });

                String detect_encoding=null;
                if (mEncodingDesired.equals(mContext.getString(R.string.msgs_zip_parm_zip_encoding_auto))) {
                    try {
                        detect_encoding= ZipUtil.detectFileNameEncoding(mContext, fp);
                    } catch(Exception e) {
//                            mUtil.addDebugMsg(1, "I", "error="+e.getMessage());
//					        e.printStackTrace();
                    }
                    if (detect_encoding==null) {
                        mEncodingSelected=mGp.settingZipDefaultEncoding;
                    } else mEncodingSelected=detect_encoding;
                    mUtil.addDebugMsg(1, "I", "createFileList Auto detected encoding="+detect_encoding);
                } else {
                    mEncodingSelected=mEncodingDesired;
                }
                mUtil.addDebugMsg(1, "I", "createFileList desired="+mEncodingDesired+", selected="+mEncodingSelected);
                try {
                    mZipFileList=ZipUtil.buildZipFileList(mContext, fp, mEncodingSelected);
                } catch(Exception e) {
                    mCommonDlg.showCommonDialog(false, "E", "ZIP file", "ZIP file list creation error, error="+e.getMessage(), null);
                    mZipFileList=null;
//                        mUtil.addDebugMsg(1, "I", "error="+e.getMessage());
//                        e.printStackTrace();
                }
                mUtil.addDebugMsg(2, "I", "createFileList Zip file list created");
                if (tc.isEnabled()) {
                    ntfy_create_file_list.notifyToListener(true, null);
                } else {
                    ntfy_create_file_list.notifyToListener(false, null);
                }
            }
        };
        th.start();
	};

	private void hideTreeFileListView() {
        mTreeFilelistView.setVisibility(ListView.GONE);
        mFileEmpty.setVisibility(TextView.VISIBLE);
        mFileEmpty.setText(R.string.msgs_zip_folder_not_specified);
        mCurrentDirectory.setVisibility(TextView.GONE);
        mFileListUp.setVisibility(Button.GONE);
        mFileListUp.setEnabled(false);
        mFileListTop.setVisibility(Button.GONE);
        mEncodingSpinner.setVisibility(Spinner.INVISIBLE);
        mZipFileSpinner.setVisibility(TextView.INVISIBLE);

        mContextButtonCopyView.setVisibility(LinearLayout.INVISIBLE);
        mContextButtonCutView.setVisibility(LinearLayout.INVISIBLE);
        mContextButtonPasteView.setVisibility(LinearLayout.INVISIBLE);
        mContextButtonExtractView.setVisibility(LinearLayout.INVISIBLE);
        mContextButtonDeleteView.setVisibility(LinearLayout.INVISIBLE);
        mContextButtonSelectAllView.setVisibility(LinearLayout.INVISIBLE);
        mContextButtonUnselectAllView.setVisibility(LinearLayout.INVISIBLE);

        mFileInfo.setVisibility(TextView.INVISIBLE);
    }

//	private void saveZipFileList(ArrayList<ZipFileListItem>fl, String fp) {
//		File lf=new File(mGp.applicationCacheDirectory+"/ZipSearchList");
//		try {
//			FileOutputStream fos=new FileOutputStream(lf);
//			BufferedOutputStream bos=new BufferedOutputStream(fos,1024*1024*4);
//			ObjectOutputStream oos=new ObjectOutputStream(bos);
//			oos.writeObject(fl);
//			oos.flush();
//			oos.close();
//		} catch (FileNotFoundException e) {
//			e.printStackTrace();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//	};

	private ArrayList<TreeFilelistItem> createTreeFileList(ArrayList<ZipFileListItem> zip_file_list, String target_dir) {
		ArrayList<TreeFilelistItem> tfl=new ArrayList<TreeFilelistItem>();
		for (ZipFileListItem zfli:mZipFileList) {
			if (zfli.getParentDirectory().equals(target_dir)) {
				TreeFilelistItem tfli=createNewFileListItem(zfli);
				if (tfli.isDirectory()) {
					int sub_dc=0;
					String sub_dir=tfli.getPath()+"/"+tfli.getName();
					if (sub_dir.startsWith("/")) sub_dir=sub_dir.substring(1);

					boolean sub_dir_found=false;
					for (ZipFileListItem s_zfli:mZipFileList) {
						if (s_zfli.getParentDirectory().equals(sub_dir)) {
							sub_dc++;
							sub_dir_found=true;
						} else {
							if (sub_dir_found) break;
						}
					}
					tfli.setSubDirItemCount(sub_dc);
				}
				tfl.add(tfli);
//				tfli.dump("");
			}
		}
		Collections.sort(tfl);
		return tfl;
	};

	@SuppressLint("DefaultLocale")
	private TreeFilelistItem createNewFileListItem(ZipFileListItem zfli) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.getDefault());
		String tfs=MiscUtil.convertFileSize(zfli.getFileLength());
		TreeFilelistItem tfi=null;
		if (zfli.isDirectory()) {
			tfi=new TreeFilelistItem(zfli.getFileName(),
					true, -1, zfli.getLastModifiedTime(),
					false, true, true,
					false, zfli.getParentDirectory(),0);
			tfi.setZipEncrypted(false);
			tfi.setZipFileName(zfli.getPath());
			tfi.setZipFileCompressionMethod(zfli.getCompressionMethod());
            tfi.setZipFileEncryptionMethod(zfli.getEncryptionMethod());
			tfi.setZipFileCompressedSize(zfli.getCompressedFileLength());
            tfi.setZipFileItem(true);
		} else {
			tfi=new TreeFilelistItem(zfli.getFileName(),
					false, zfli.getFileLength(), zfli.getLastModifiedTime(),
					false, true, true,
					false, zfli.getParentDirectory(),0);
			tfi.setZipEncrypted(zfli.isEncrypted());
			tfi.setZipFileName(zfli.getPath());
			tfi.setZipFileCompressionMethod(zfli.getCompressionMethod());
            tfi.setZipFileEncryptionMethod(zfli.getEncryptionMethod());
			tfi.setZipFileCompressedSize(zfli.getCompressedFileLength());
            tfi.setZipFileItem(true);
//            tfi.dump("zfli");
//			Log.v("","ft="+ft+", mt="+mt);
		}
		return tfi;
	};

    private void setContextButtonEnabled(final ImageButton btn, boolean enabled) {
    	if (enabled) {
        	btn.postDelayed(new Runnable(){
    			@Override
    			public void run() {
    				btn.setEnabled(true);
    			}
        	}, 1000);
    	} else {
    		btn.setEnabled(false);
    	}
    };

	private void setContextButtonListener() {
        mContextButtonExtract.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				if (isUiEnabled()) {
					setContextButtonEnabled(mContextButtonExtract, false);
                    extractDlg(mTreeFilelistAdapter);
					setContextButtonEnabled(mContextButtonExtract, true);
				}
			}
        });
        ContextButtonUtil.setButtonLabelListener(mContext, mContextButtonExtract,mContext.getString(R.string.msgs_zip_cont_label_extract));

        mContextButtonCopy.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				if (isUiEnabled()) {
					setContextButtonEnabled(mContextButtonCopy, false);
                    if (mTreeFilelistAdapter.isItemSelected()) copyItem(mTreeFilelistAdapter);
					setContextButtonEnabled(mContextButtonCopy, true);
				}
			}
        });
        ContextButtonUtil.setButtonLabelListener(mContext, mContextButtonCopy, mContext.getString(R.string.msgs_zip_cont_label_copy));

        mContextButtonCut.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				if (isUiEnabled()) {
					setContextButtonEnabled(mContextButtonCut, false);
                    if (mTreeFilelistAdapter.isItemSelected()) cutItem(mTreeFilelistAdapter);
					setContextButtonEnabled(mContextButtonCut, true);
				}
			}
        });
        ContextButtonUtil.setButtonLabelListener(mContext, mContextButtonCut, mContext.getString(R.string.msgs_zip_cont_label_cut));

        mContextButtonPaste.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				if (isUiEnabled()) {
					setContextButtonEnabled(mContextButtonPaste, false);
					pasteItem();
					setContextButtonEnabled(mContextButtonPaste, true);
				}
			}
        });
        ContextButtonUtil.setButtonLabelListener(mContext, mContextButtonPaste, mContext.getString(R.string.msgs_zip_cont_label_paste));

        mContextButtonOpen.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				if (isUiEnabled()) {
					setContextButtonEnabled(mContextButtonOpen, false);
					openZipFileDialog();
					setContextButtonEnabled(mContextButtonOpen, true);
				}
			}
        });
        ContextButtonUtil.setButtonLabelListener(mContext, mContextButtonOpen,mContext.getString(R.string.msgs_zip_cont_label_open));

        mContextButtonNew.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				if (isUiEnabled()) {
                    setContextButtonEnabled(mContextButtonNew, false);
                    createNewZipFileDialog();
                    setContextButtonEnabled(mContextButtonNew, true);
				}
			}
        });
        ContextButtonUtil.setButtonLabelListener(mContext, mContextButtonNew,mContext.getString(R.string.msgs_zip_cont_label_new));

        mContextButtonDelete.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				if (isUiEnabled()) {
					setContextButtonEnabled(mContextButtonDelete, false);
                    if (mTreeFilelistAdapter.isItemSelected()) confirmDelete(mTreeFilelistAdapter);
					setContextButtonEnabled(mContextButtonDelete, true);
				}
			}
        });
        ContextButtonUtil.setButtonLabelListener(mContext, mContextButtonDelete,mContext.getString(R.string.msgs_zip_cont_label_delete));

        mContextButtonSelectAll.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				if (isUiEnabled()) {
					setContextButtonEnabled(mContextButtonSelectAll,false);
					ArrayList<TreeFilelistItem> tfl=mTreeFilelistAdapter.getDataList();
					for(TreeFilelistItem tfli:tfl) {
						tfli.setChecked(true);
					}
					mTreeFilelistAdapter.notifyDataSetChanged();
					setContextButtonEnabled(mContextButtonSelectAll,true);
				}
			}
        });
        ContextButtonUtil.setButtonLabelListener(mContext, mContextButtonSelectAll,mContext.getString(R.string.msgs_zip_cont_label_select_all));

        mContextButtonUnselectAll.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				if (isUiEnabled()) {
					setContextButtonEnabled(mContextButtonUnselectAll,false);
					mTreeFilelistAdapter.setAllItemUnchecked();
					mTreeFilelistAdapter.notifyDataSetChanged();
					setContextButtonEnabled(mContextButtonUnselectAll,true);
				}
			}
        });
        ContextButtonUtil.setButtonLabelListener(mContext, mContextButtonUnselectAll,mContext.getString(R.string.msgs_zip_cont_label_unselect_all));
	};

	private void copyItem(CustomTreeFilelistAdapter tfa) {
		if (tfa.isItemSelected()) {
			mGp.copyCutModeIsCut=false;
			mGp.copyCutFilePath=mCurrentFilePath;
			mGp.copyCutCurrentDirectory=mCurrentDirectory.getText().equals("/")?"":mCurrentDirectory.getText().toString().substring(1);
			mGp.copyCutEncoding=mEncodingSelected;
			mGp.copyCutType=GlobalParameters.COPY_CUT_FROM_ZIP;
			mGp.copyCutList.clear();
			String c_list="", sep="";
			for(TreeFilelistItem tfl:tfa.getDataList()) {
				if(tfl.isChecked()) {
					mGp.copyCutList.add(tfl);
					c_list+=sep+tfl.getPath().replace(mZipFileSpinner.getSelectedItem().toString(), "")+"/"+tfl.getName();
					sep=", ";
                    tfl.setChecked(false);
				}
			}
			tfa.notifyDataSetChanged();
			String from=mGp.copyCutType.equals(GlobalParameters.COPY_CUT_FROM_LOCAL)?"Local":"ZIP";
			mGp.copyCutItemInfo.setText(mContext.getString(R.string.msgs_zip_cont_header_copy)+" "+from+":"+c_list);
			mGp.copyCutItemInfo.setVisibility(TextView.VISIBLE);
			mGp.copyCutItemClear.setVisibility(Button.VISIBLE);
			mContextButtonPasteView.setVisibility(ImageButton.INVISIBLE);
		}
	};

	private void cutItem(CustomTreeFilelistAdapter tfa) {
		if (tfa.isItemSelected()) {
			mGp.copyCutModeIsCut=true;
			mGp.copyCutFilePath=mCurrentFilePath;
			mGp.copyCutCurrentDirectory=mCurrentDirectory.getText().equals("/")?"":mCurrentDirectory.getText().toString().substring(1);
			mGp.copyCutEncoding=mEncodingSelected;
			mGp.copyCutType=GlobalParameters.COPY_CUT_FROM_ZIP;
			mGp.copyCutList.clear();
			String c_list="", sep="";
			for(TreeFilelistItem tfl:tfa.getDataList()) {
				if(tfl.isChecked()) {
					mGp.copyCutList.add(tfl);
					c_list+=sep+tfl.getPath().replace(mZipFileSpinner.getSelectedItem().toString(), "")+"/"+tfl.getName();
					sep=", ";
                    tfl.setChecked(false);
				}
			}
			tfa.notifyDataSetChanged();
			String from=mGp.copyCutType.equals(GlobalParameters.COPY_CUT_FROM_LOCAL)?"Local":"ZIP";
			mGp.copyCutItemInfo.setText(mContext.getString(R.string.msgs_zip_cont_header_cut)+" "+from+":"+c_list);
			mGp.copyCutItemInfo.setVisibility(TextView.VISIBLE);
			mGp.copyCutItemClear.setVisibility(Button.VISIBLE);
			mContextButtonPasteView.setVisibility(ImageButton.INVISIBLE);
		}
	};

	private boolean isCopyCutDestValid(String zip_file_path, String fp) {
		boolean enabled=true;
		if (mGp.copyCutList.size()>0) {
			if (mGp.copyCutType.equals(GlobalParameters.COPY_CUT_FROM_LOCAL)) {
//				for(TreeFilelistItem tfli:mGp.copyCutList) {
//					if (!tfli.isDirectory()) {
//						String zfp=tfli.getPath()+"/"+tfli.getName();
//						if (zfp.equals(zip_file_path)) {
//							enabled=false;
//							break;
//						}
//					}
//				}
			} else if (mGp.copyCutType.equals(GlobalParameters.COPY_CUT_FROM_ZIP)) {
				if (!mGp.copyCutFilePath.equals(zip_file_path)) enabled=true;
				else {
					String curr_dir=fp.equals("")?"":fp;
//					Log.v("","size="+mGp.copyCutList.size());
					for(TreeFilelistItem s_item:mGp.copyCutList) {
						String sel_path="";
						if (s_item.isDirectory()){
							sel_path=s_item.getPath().equals("")?s_item.getName():s_item.getPath()+"/"+s_item.getName();
						} else {
							sel_path=s_item.getPath();
						}
						String[] item_array=sel_path.equals("")?new String[]{"/"}:sel_path.substring(1).split("/");
						String[] cdir_array=curr_dir.equals("/")?new String[]{""}:curr_dir.split("/");
//						Log.v("","item length="+item_array.length+", cdir length="+cdir_array.length);
						if (item_array.length>1) {
							if (cdir_array.length!=0){
								if (sel_path.equals(curr_dir)) enabled=false;
								if (s_item.isDirectory()) {
									if (sel_path.equals(curr_dir)) enabled=false;
									else {
										if (!curr_dir.equals("")) if (sel_path.startsWith(curr_dir)) enabled=false;
									}
								} else {
								}
							}
						} else {
							if (s_item.isDirectory()) {
								if (curr_dir.equals("")) {
									enabled=false;
								} else {
									if (curr_dir.startsWith(sel_path)) enabled=false;
								}
							} else {
								if (curr_dir.equals(sel_path)) enabled=false;
							}
						}
//						if (enabled) Log.v("","enabled  name="+sel_path+", c="+curr_dir);
//						else Log.v("","disabled name="+sel_path+", c="+curr_dir);
						if (!enabled) break;
					}
				}
			}
		} else {
			enabled=false;
		}
		mUtil.addDebugMsg(1, "I", CommonUtilities.getExecutedMethodName()+" exit, enabled="+enabled+", Zip path="+zip_file_path+", fp="+fp);
		return enabled;
	};

	private void pasteItem() {
		final String[] add_item=new String[mGp.copyCutList.size()];
		int cnt=0;
		for(TreeFilelistItem tfli:mGp.copyCutList) {
			add_item[cnt]=tfli.getPath()+"/"+tfli.getName();
			cnt++;
		}
		if (mGp.copyCutType.equals(GlobalParameters.COPY_CUT_FROM_LOCAL)) {
			if (mGp.copyCutModeIsCut) {
				NotifyEvent ntfy_move=new NotifyEvent(mContext);
				ntfy_move.setListener(new NotifyEventListener(){
					@Override
					public void positiveResponse(Context c, Object[] o) {
						for(String item:add_item) {
							SafFile3 lf=new SafFile3(mContext, item);
                            CommonUtilities.deleteLocalFile(lf);
						}
//						mCommonDlg.showCommonDialog(false, "I", mContext.getString(R.string.msgs_zip_move_file_completed), "", null);
						showToast(mActivity, mContext.getString(R.string.msgs_zip_move_file_completed));
						mUiHandler.post(new Runnable(){
							@Override
							public void run() {
								mGp.copyCutItemClear.performClick();
							}
						});
					}
					@Override
					public void negativeResponse(Context c, Object[] o) {}
				});
				confirmAddItemFromLocal(add_item, mContext.getString(R.string.msgs_zip_move_file_confirm), ntfy_move);//false, ntfy_move);
			} else {
				confirmAddItemFromLocal(add_item, mContext.getString(R.string.msgs_zip_add_file_confirm), null);//true, null);
			}
		} else {
//			mCommonDlg.showCommonDialog(false, "W", mContext.getString(R.string.msgs_zip_copy_cut_not_supported), "", null);
			if (mGp.copyCutModeIsCut) {
				NotifyEvent ntfy_move=new NotifyEvent(mContext);
				ntfy_move.setListener(new NotifyEventListener(){
					@Override
					public void positiveResponse(Context c, Object[] o) {
						mUiHandler.post(new Runnable(){
							@Override
							public void run() {
								CustomTreeFilelistAdapter tfa=new CustomTreeFilelistAdapter(mActivity, true, false);
								tfa.setDataList(mGp.copyCutList);
								NotifyEvent ntfy_move_comp=new NotifyEvent(mContext);
								ntfy_move_comp.setListener(new NotifyEventListener(){
									@Override
									public void positiveResponse(Context c, Object[] o) {
//										mCommonDlg.showCommonDialog(false, "W", mContext.getString(R.string.msgs_zip_move_file_completed), "", null);
                                        showToast(mActivity, mContext.getString(R.string.msgs_zip_move_file_completed));
										mUiHandler.post(new Runnable(){
											@Override
											public void run() {
												deleteCopyPasteWorkFile();
												mGp.copyCutItemClear.performClick();
											}
										});
									}
									@Override
									public void negativeResponse(Context c, Object[] o) {}
								});
								deleteZipFileItem(mGp.copyCutFilePath, mGp.copyCutEncoding, tfa, "", ntfy_move_comp);//, false);
							}
						});
					}
					@Override
					public void negativeResponse(Context c, Object[] o) {}
				});
				confirmCopyItemFromZip(add_item, mContext.getString(R.string.msgs_zip_move_file_confirm), ntfy_move);//false, ntfy_move);
			} else {
				confirmCopyItemFromZip(add_item, mContext.getString(R.string.msgs_zip_add_file_confirm), null);//true, null);
			}
		}
	};

	private void createNewZipFileDialog() {
		boolean enableCreate=true;
		String title=mContext.getString(R.string.msgs_zip_create_new_zip_file_title);
		String filename="/newZip.zip";
		String lurl=LocalMountPoint.getExternalStorageDir();
		String ldir="";

		NotifyEvent ntfy_select_file=new NotifyEvent(mContext);
		ntfy_select_file.setListener(new NotifyEventListener(){
			@Override
			public void positiveResponse(Context c, Object[] o) {
                final String fp=(String)o[1];
				final SafFile3 lf=new SafFile3(mContext, fp);
				if (!lf.isDirectory()) {
					NotifyEvent ntfy_comfirm_override=new NotifyEvent(mContext);
					ntfy_comfirm_override.setListener(new NotifyEventListener(){
						@Override
						public void positiveResponse(Context c, Object[] o) {
                            lf.deleteIfExists();
                            try {
                                lf.createNewFile();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
							showZipFile(false, lf);
						}
						@Override
						public void negativeResponse(Context c, Object[] o) {}
					});
					if (lf.exists()) {
						mCommonDlg.showCommonDialog(true, "W",
								String.format(mContext.getString(R.string.msgs_zip_create_new_zip_file_already_exists), lf.getName()),
								"", ntfy_comfirm_override);
					} else {
						ntfy_comfirm_override.notifyToListener(true, null);
					}
				} else {
					mCommonDlg.showCommonDialog(false, "E",
							mContext.getString(R.string.msgs_zip_create_new_zip_file_dir_can_not_used), "", null);
				}
			}
			@Override
			public void negativeResponse(Context c, Object[] o) {
			}
		});
        boolean include_root=false;
        boolean scoped_storage_mode=mGp.safMgr.isScopedStorageMode();
        CommonFileSelector2 fsdf=
                CommonFileSelector2.newInstance(scoped_storage_mode, true, false, CommonFileSelector2.DIALOG_SELECT_CATEGORY_FILE,
                        true, false, SafFile3.SAF_FILE_PRIMARY_UUID, "", "", title);
        fsdf.showDialog(false, mActivity.getSupportFragmentManager(), fsdf, ntfy_select_file);
	};

	private void openZipFileDialog() {
		boolean enableCreate=false;
		String title=mContext.getString(R.string.msgs_main_select_file);
		String filename="";
		String lurl=LocalMountPoint.getExternalStorageDir();
		String ldir="";

		NotifyEvent ntfy=new NotifyEvent(mContext);
		ntfy.setListener(new NotifyEventListener(){
			@Override
			public void positiveResponse(Context c, Object[] o) {
				String in_path=(String)o[1];
                SafFile3 lf=new SafFile3(mContext, in_path);
                if (lf.isDirectory()) {
                    mCommonDlg.showCommonDialog(false, "W",
                            mContext.getString(R.string.msgs_zip_create_new_zip_file_dir_can_not_used), "", null);
                } else {
                    if (lf.exists()) showZipFile(false, lf);
                    else {
                        mCommonDlg.showCommonDialog(false, "W",
                                mContext.getString(R.string.msgs_zip_create_new_zip_file_not_exists), "", null);
                    }
                }
			}
			@Override
			public void negativeResponse(Context c, Object[] o) {
			}
		});
        boolean include_root=false;
        boolean scoped_storage_mode=mGp.safMgr.isScopedStorageMode();
        CommonFileSelector2 fsdf=
                CommonFileSelector2.newInstance(scoped_storage_mode, true, false, CommonFileSelector2.DIALOG_SELECT_CATEGORY_FILE,
                        true, true, SafFile3.SAF_FILE_PRIMARY_UUID, "", "", title);
        fsdf.showDialog(false, mActivity.getSupportFragmentManager(), fsdf, ntfy);
	};

	private void confirmCancel(final ThreadCtrl tc, final Button cancel) {
		NotifyEvent ntfy=new NotifyEvent(mContext);
		ntfy.setListener(new NotifyEventListener(){
			@Override
			public void positiveResponse(Context c, Object[] o) {
				tc.setDisabled();
				cancel.setEnabled(false);
			}
			@Override
			public void negativeResponse(Context c, Object[] o) {}
		});
		mCommonDlg.showCommonDialog(true, "W",
				mContext.getString(R.string.msgs_main_confirm_cancel), "", ntfy);
	};

	@SuppressWarnings("unused")
	private void addItemDlg() {
		NotifyEvent ntfy=new NotifyEvent(mContext);
		ntfy.setListener(new NotifyEventListener(){
			@Override
			public void positiveResponse(Context c, Object[] o) {
				final String[] add_item=(String[])o[1];
				confirmAddItemFromLocal(add_item, mContext.getString(R.string.msgs_zip_add_file_confirm), null);//true, null);
			}
			@Override
			public void negativeResponse(Context c, Object[] o) {
			}
		});

        boolean include_root=false;
        boolean scoped_storage_mode=mGp.safMgr.isScopedStorageMode();
        CommonFileSelector2 fsdf=
                CommonFileSelector2.newInstance(scoped_storage_mode, true, false, CommonFileSelector2.DIALOG_SELECT_CATEGORY_FILE,
                        true, true, SafFile3.SAF_FILE_PRIMARY_UUID, "", "", mContext.getString(R.string.msgs_zip_add_select_add_item));
        fsdf.showDialog(false, mActivity.getSupportFragmentManager(), fsdf, ntfy);

	};

	private void deleteCopyPasteWorkFile() {
		CommonUtilities.deleteLocalFile(new SafFile3(mContext, getCopyPasteWorkFilePath()));
	};
	private String getCopyPasteWorkFilePath() {
		return mContext.getExternalCacheDirs()[0].getPath()+"/zuw";
	}

	private void confirmCopyItemFromZip(final String[] add_item, String conf_msg, //final boolean comp_msg_required,
                                        final NotifyEvent p_ntfy) {
		String w_sel_list="", sep="";
		for(String sel_item:add_item) {
			w_sel_list+=sep+sel_item;
			sep=", ";
		}
		final String sel_list=w_sel_list;
		NotifyEvent ntfy_confirm=new NotifyEvent(mContext);
		ntfy_confirm.setListener(new NotifyEventListener(){
			@Override
			public void positiveResponse(Context c, final Object[] o) {
				CustomTreeFilelistAdapter tfa=new CustomTreeFilelistAdapter(mActivity, true, false);
				tfa.setDataList(mGp.copyCutList);
				deleteCopyPasteWorkFile();

				NotifyEvent ntfy_extract=new NotifyEvent(mContext);
				ntfy_extract.setListener(new NotifyEventListener(){
					@Override
					public void positiveResponse(Context c, Object[] o) {
						final NotifyEvent ntfy_zip_parm=new NotifyEvent(mContext);
						ntfy_zip_parm.setListener(new NotifyEventListener(){
							@Override
							public void positiveResponse(Context c, final Object[] o) {
								CustomZipParameters zp=(CustomZipParameters)o[0];
								String zip_curr_dir="";
								if (mCurrentDirectory.getText().toString().equals("") ||
										mCurrentDirectory.getText().toString().equals("/")) zip_curr_dir="";
								else zip_curr_dir=mCurrentDirectory.getText().toString().substring(1);
								String base_dir=getCopyPasteWorkFilePath();
								File[] fl=new File(base_dir).listFiles();
								String[] t_add_item=new String[fl.length];
								int cnt=0;
								for(File item:fl) {
									t_add_item[cnt]=item.getPath();
									cnt++;
								}
								addSelectedItem(t_add_item, zp, p_ntfy, base_dir, zip_curr_dir);//comp_msg_required, p_ntfy, base_dir, zip_curr_dir);
							}
							@Override
							public void negativeResponse(Context c, Object[] o) {
								deleteCopyPasteWorkFile();
							}
						});
						mUiHandler.post(new Runnable(){
							@Override
							public void run() {
								getZipParmDlg(mUtil, mActivity, mGp, mEncodingSelected, "", mCurrentFilePath, ntfy_zip_parm);
							}
						});
					}
					@Override
					public void negativeResponse(Context c, Object[] o) {
					}

				});
				prepareExtractMultipleItem(mGp.copyCutFilePath, mGp.copyCutEncoding,
						tfa, mGp.copyCutCurrentDirectory,
						getCopyPasteWorkFilePath(), sel_list, ntfy_extract, false, false);
			}
			@Override
			public void negativeResponse(Context c, Object[] o) {
			}
		});
		mCommonDlg.showCommonDialog(true, "W", conf_msg, sel_list, ntfy_confirm);

	};

	private void confirmAddItemFromLocal(final String[] add_item, String conf_msg, //final boolean comp_msg_required,
                                         final NotifyEvent p_ntfy) {
		String w_sel_list="", sep="";
		for(String sel_item:add_item) {
			w_sel_list+=sep+sel_item;
			sep=", ";
		}
		final String sel_list=w_sel_list;
		NotifyEvent ntfy_confirm=new NotifyEvent(mContext);
		ntfy_confirm.setListener(new NotifyEventListener(){
			@Override
			public void positiveResponse(Context c, final Object[] o) {
				putProgressMessage(mContext.getString(R.string.msgs_zip_add_file_starting));
				NotifyEvent ntfy_zip_parm=new NotifyEvent(mContext);
				ntfy_zip_parm.setListener(new NotifyEventListener(){
					@Override
					public void positiveResponse(Context c, final Object[] o) {
						CustomZipParameters zp=(CustomZipParameters)o[0];
						String zip_curr_dir="";
						if (mCurrentDirectory.getText().toString().equals("") ||
								mCurrentDirectory.getText().toString().equals("/")) zip_curr_dir="";
						else zip_curr_dir=mCurrentDirectory.getText().toString().substring(1);
//						String base_dir=add_item[0].startsWith(mGp.internalRootDirectory)?mGp.internalRootDirectory:mGp.externalRootDirectory;
						String parent_dir=add_item[0].lastIndexOf("/")>0?add_item[0].substring(0,add_item[0].lastIndexOf("/")):add_item[0];
//						Log.v("","name="+add_item[0]+", base="+parent_dir);
						addSelectedItem(add_item, zp, p_ntfy, parent_dir, zip_curr_dir);//comp_msg_required, p_ntfy, parent_dir, zip_curr_dir);
					}
					@Override
					public void negativeResponse(Context c, Object[] o) {
					}
				});
				getZipParmDlg(mUtil, mActivity, mGp, mEncodingSelected, "", mCurrentFilePath, ntfy_zip_parm);
			}
			@Override
			public void negativeResponse(Context c, Object[] o) {
			}
		});
		mCommonDlg.showCommonDialog(true, "W", conf_msg, sel_list, ntfy_confirm);

	};

	static public void getAllItemInLocalDirectory(ArrayList<SafFile3> sel_item_list, SafFile3 sf) {
	    File lf=new File(sf.getPath());
	    if (lf.canRead()) getFileApiAllItemInLocalDirectory(sf.getContext(), sel_item_list, lf);
	    else getSafApiAllItemInLocalDirectory(sel_item_list, sf);
	};

    static public void getSafApiAllItemInLocalDirectory(ArrayList<SafFile3> sel_item_list, SafFile3 lf) {
        if (lf.isDirectory()) {
            SafFile3[] fl=lf.listFiles();
            if (fl!=null && fl.length>0) {
                for(SafFile3 cf:fl) {
                    if (cf.isDirectory()) {
                        sel_item_list.add(cf);
                        getSafApiAllItemInLocalDirectory(sel_item_list, cf);
                    } else {
                        sel_item_list.add(cf);
                    }
                }
            }
        } else {
            sel_item_list.add(lf);
        }
    };

    static public void getFileApiAllItemInLocalDirectory(Context c, ArrayList<SafFile3> sel_item_list, File lf) {
        if (lf.isDirectory()) {
            File[] fl=lf.listFiles();
            if (fl!=null && fl.length>0) {
                for(File cf:fl) {
                    if (cf.isDirectory()) {
                        sel_item_list.add(new SafFile3(c, cf.getPath()));
                        getFileApiAllItemInLocalDirectory(c, sel_item_list, cf);
                    } else {
                        sel_item_list.add(new SafFile3(c, cf.getPath()));
                    }
                }
            }
        } else {
            sel_item_list.add(new SafFile3(c, lf.getPath()));
        }
    };

    private void closeUiDialogView(int delay_time) {
        mUiHandler.postDelayed(new Runnable(){
            @Override
            public void run() {
                refreshFileList(true);
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

    private void addSelectedItem(final String[] add_item, final CustomZipParameters zp, //final boolean comp_msg_required,
                                 final NotifyEvent p_ntfy, final String zip_base, final String zip_curr_dir) {
		setUiDisabled();
		showDialogProgress();
		final ThreadCtrl tc=new ThreadCtrl();
		mDialogProgressSpinMsg1.setVisibility(TextView.GONE);
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
                putProgressMessage(mContext.getString(R.string.msgs_zip_add_file_starting));
                SafFile3 in_zip=new SafFile3(mContext, mCurrentFilePath);
                SafFile3 out_temp = new SafFile3(mContext, mCurrentFilePath+".tmp");
                String zip_file_name = mCurrentFilePath.substring(mCurrentFilePath.lastIndexOf("/"));
                try {
                    out_temp.createNewFile();
                    BufferedZipFile3 bzf = new BufferedZipFile3(mContext, in_zip, out_temp,
                            ZipUtil.DEFAULT_ZIP_FILENAME_ENCODING, out_temp.getAppDirectoryCache());
                    bzf.setNoCompressExtentionList(mGp.settingNoCompressFileType);
                    bzf.setPassword(zp.getPassword());
                    for (String item : add_item) {
                        if (item != null) {
                            SafFile3 add_file = new SafFile3(mContext, item);
                            ZipParameters n_zp = new ZipParameters(zp);
                            n_zp.setDefaultFolderPath(zip_base+"/");
                            ArrayList<SafFile3> sel_list = new ArrayList<SafFile3>();
                            if (add_file.isDirectory()) sel_list.add(add_file);
                            getAllItemInLocalDirectory(sel_list, add_file);
                            for (SafFile3 sel_file : sel_list) {
                                try {
                                    CallBackListener cbl=new CallBackListener() {
                                        @Override
                                        public boolean onCallBack(Context context, Object o, Object[] objects) {
                                            if (!tc.isEnabled()) {
                                                bzf.abort();
                                            } else {
                                                int prog=(Integer)o;
                                                putProgressMessage(mContext.getString(R.string.msgs_zip_add_file_adding, sel_file.getPath(), prog));
                                            }
                                            return true;
                                        }
                                    };
                                    String abs_input_file_path=sel_file.getPath().replace(n_zp.getDefaultFolderPath(), "");
                                    String file_name_in_zip=zip_curr_dir.equals("")?abs_input_file_path:zip_curr_dir+"/"+abs_input_file_path;
                                    if (sel_file.isDirectory()) n_zp.setFileNameInZip(file_name_in_zip+"/");
                                    else n_zp.setFileNameInZip(file_name_in_zip);
                                    bzf.addItem(sel_file.getPath(), n_zp, cbl);
                                    if (!tc.isEnabled()) {
                                        mCommonDlg.showCommonDialog(false, "W", String.format(mContext.getString(R.string.msgs_zip_add_file_cancelled), sel_file.getPath()), "", null);
                                        try {bzf.destroy();} catch (Exception e) {}
                                        deleteBufferedZipWork(mGp, mUtil, mCurrentFilePath, out_temp.getPath());
                                        closeUiDialogView(500);
                                        return;
                                    }
                                    putProgressMessage(String.format(mContext.getString(R.string.msgs_zip_add_file_added), sel_file.getPath()));
                                } catch (ZipException e) {
                                    tc.setThreadMessage(e.getMessage());
                                    mUtil.addLogMsg("I", String.format(mContext.getString(R.string.msgs_zip_add_file_failed), sel_file.getPath()));
                                    mCommonDlg.showCommonDialog(false, "E", String.format(mContext.getString(R.string.msgs_zip_add_file_failed), sel_file.getPath()),
                                            tc.getThreadMessage(), null);
                                    closeUiDialogView(500);
                                    return;
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                    try {
//                        disableCancelButton();
                        CallBackListener cbl=getZipProgressCallbackListener(tc, bzf, mContext.getString(R.string.msgs_zip_zip_file_being_updated));
                        if (bzf.isAborted() || bzf.close(cbl)) {
                            if (!bzf.isAborted()) renameBufferedZipFile(mGp, mUtil, mCurrentFilePath, out_temp.getPath(), zip_file_name);
                            else {
                                mCommonDlg.showCommonDialog(false, "W", mContext.getString(R.string.msgs_zip_write_zip_file_canelled), "", null);
                                try {bzf.destroy();} catch (Exception e) {}
                                deleteBufferedZipWork(mGp, mUtil, mCurrentFilePath, out_temp.getPath());
                                closeUiDialogView(500);
                                return;
                            }
                        }

                        if (p_ntfy != null) p_ntfy.notifyToListener(true, new Object[]{add_item});
//                        String w_sel_list = "", sep = "";
//                        for (String sel_item : add_item) {
//                            w_sel_list += sep + sel_item;
//                            sep = ", ";
//                        }
//                        if (comp_msg_required) {
//                            mCommonDlg.showCommonDialog(false, "I",
//                                    mContext.getString(R.string.msgs_zip_add_file_completed), w_sel_list, null);
                            mUtil.addLogMsg("I", mContext.getString(R.string.msgs_zip_add_file_completed));
                            showToast(mActivity, mContext.getString(R.string.msgs_zip_add_file_completed));
                            deleteCopyPasteWorkFile();
                            closeUiDialogView(500);

                            if (p_ntfy==null) {
                                mUiHandler.postDelayed(new Runnable(){
                                    @Override
                                    public void run() {
                                        refreshFileList();
                                    }
                                },500);
                            }
//                        }
                    } catch (ZipException e) {
                        tc.setThreadMessage(e.getMessage());
                        mUtil.addLogMsg("I", mContext.getString(R.string.msgs_zip_add_file_close_failed));
                        mCommonDlg.showCommonDialog(false, "E", mContext.getString(R.string.msgs_zip_add_file_close_failed),
                                tc.getThreadMessage(), null);
                        closeUiDialogView(500);
                    } catch (Exception e) {
                        e.printStackTrace();
                        tc.setThreadMessage(e.getMessage());
                        mUtil.addLogMsg("I", mContext.getString(R.string.msgs_zip_add_file_close_failed));
                        mCommonDlg.showCommonDialog(false, "E", mContext.getString(R.string.msgs_zip_add_file_close_failed),
                                tc.getThreadMessage(), null);
                        closeUiDialogView(500);
                    }
                } catch (Exception e) {
                    out_temp.deleteIfExists();
                    mUtil.addLogMsg("E", mContext.getString(R.string.msgs_zip_zip_file_creation_failed));
                    mCommonDlg.showCommonDialog(false, "E",mContext.getString(R.string.msgs_zip_zip_file_creation_failed), e.getMessage(), null);
                    closeUiDialogView(500);
                }
            }
		};
		th.start();
	};

    private CallBackListener getZipProgressCallbackListener(final ThreadCtrl tc, final BufferedZipFile3 bzf, final String msg_txt) {
        CallBackListener cbl=new CallBackListener() {
            @Override
            public boolean onCallBack(Context context, Object o, Object[] objects) {
                if (!tc.isEnabled()) {
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

	static public void getZipParmDlg(CommonUtilities mUtil, Activity mActivity, final GlobalParameters mGp,
                                     final String selected_encoding, final String pswd, final String fp, final NotifyEvent p_ntfy) {
		int zip_comp_method=0, zip_enc_method=0;
		final CustomZipFile zf=createZipFile(mGp.appContext, fp, selected_encoding);
		mUtil.addDebugMsg(1, "I", "getZipParm comp_method="+zip_comp_method+", enc_method="+zip_enc_method);

		final Dialog dialog = new Dialog(mActivity, mGp.applicationTheme);
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialog.setCanceledOnTouchOutside(false);
		dialog.setContentView(R.layout.zip_parm_dlg);

		LinearLayout ll_dlg_view=(LinearLayout) dialog.findViewById(R.id.zip_parm_dlg_view);
//		ll_dlg_view.setBackgroundColor(mGp.themeColorList.dialog_msg_background_color);
//		ll_dlg_view.setBackgroundResource(R.drawable.dialog_border_dark);

    	LinearLayout title_view=(LinearLayout)dialog.findViewById(R.id.zip_parm_dlg_title_view);
    	title_view.setBackgroundColor(mGp.themeColorList.title_background_color);
    	TextView dlg_title=(TextView)dialog.findViewById(R.id.zip_parm_dlg_title);
    	dlg_title.setTextColor(mGp.themeColorList.title_text_color);

    	final TextView dlg_msg=(TextView)dialog.findViewById(R.id.zip_parm_dlg_msg);
    	dlg_msg.setVisibility(TextView.VISIBLE);
    	final EditText dlg_pswd=(EditText)dialog.findViewById(R.id.zip_parm_dlg_enc_password);
    	final EditText dlg_conf=(EditText)dialog.findViewById(R.id.zip_parm_dlg_enc_confirm);

    	final Spinner dlg_comp_level=(Spinner)dialog.findViewById(R.id.zip_parm_dlg_comp_level);

    	final RadioGroup dlg_rg_enc=(RadioGroup)dialog.findViewById(R.id.zip_parm_dlg_enc_type_rg);
    	final RadioButton dlg_rb_none=(RadioButton)dialog.findViewById(R.id.zip_parm_dlg_enc_type_rb_none);
    	final RadioButton dlg_rb_std=(RadioButton)dialog.findViewById(R.id.zip_parm_dlg_enc_type_rb_standard);
    	final RadioButton dlg_rb_aes128=(RadioButton)dialog.findViewById(R.id.zip_parm_dlg_enc_type_rb_aes128);
    	final RadioButton dlg_rb_aes256=(RadioButton)dialog.findViewById(R.id.zip_parm_dlg_enc_type_rb_aes256);

    	final Button dlg_cancel=(Button)dialog.findViewById(R.id.zip_parm_dlg_cancel_btn);
    	final Button dlg_ok=(Button)dialog.findViewById(R.id.zip_parm_dlg_ok_btn);

    	CommonDialog.setDlgBoxSizeLimit(dialog, true);

    	setZipCompLevelSpinner(mGp, mActivity, dlg_comp_level);

    	dlg_rg_enc.setOnCheckedChangeListener(new OnCheckedChangeListener(){
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				if(checkedId==dlg_rb_none.getId()) {
		    		dlg_pswd.setVisibility(EditText.GONE);
		    		dlg_conf.setVisibility(EditText.GONE);
				} else if(checkedId==dlg_rb_std.getId()) {
		    		dlg_pswd.setVisibility(EditText.VISIBLE);
		    		dlg_conf.setVisibility(EditText.VISIBLE);
				} else if(checkedId==dlg_rb_aes128.getId()) {
		    		dlg_pswd.setVisibility(EditText.VISIBLE);
		    		dlg_conf.setVisibility(EditText.VISIBLE);
				} else if(checkedId==dlg_rb_aes256.getId()) {
		    		dlg_pswd.setVisibility(EditText.VISIBLE);
		    		dlg_conf.setVisibility(EditText.VISIBLE);
				}
				checkZipParmValidation(mGp, dialog, fp, zf);
			}
    	});

		dlg_rb_none.setEnabled(true);
		dlg_rb_none.setChecked(true);
		dlg_pswd.setVisibility(EditText.GONE);
		dlg_conf.setVisibility(EditText.GONE);

    	dlg_pswd.setText(pswd);

    	checkZipParmValidation(mGp, dialog, fp, zf);
    	dlg_pswd.addTextChangedListener(new TextWatcher(){
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
			}
			@Override
			public void afterTextChanged(Editable s) {
				checkZipParmValidation(mGp, dialog, fp, zf);
			}
    	});
    	dlg_conf.addTextChangedListener(new TextWatcher(){
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
			}
			@Override
			public void afterTextChanged(Editable s) {
				checkZipParmValidation(mGp, dialog, fp, zf);
			}
    	});

		dlg_ok.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				dialog.dismiss();
				CustomZipParameters zp=new CustomZipParameters();
				String comp_level=dlg_comp_level.getSelectedItem().toString();
//				int deflate_level=-1;
				if (comp_level.equals(mGp.appContext.getString(R.string.msgs_zip_parm_zip_comp_level_fastest))) {
					zp.setCompressionLevel(CompressionLevel.FASTEST);
				} else if (comp_level.equals(mGp.appContext.getString(R.string.msgs_zip_parm_zip_comp_level_fast))) {
					zp.setCompressionLevel(CompressionLevel.FAST);
				} else if (comp_level.equals(mGp.appContext.getString(R.string.msgs_zip_parm_zip_comp_level_normal))) {
					zp.setCompressionLevel(CompressionLevel.NORMAL);
				} else if (comp_level.equals(mGp.appContext.getString(R.string.msgs_zip_parm_zip_comp_level_maximum))) {
					zp.setCompressionLevel(CompressionLevel.MAXIMUM);
//				} else if (comp_level.equals(mGp.appContext.getString(R.string.msgs_zip_parm_zip_comp_level_ultra))) {
//					zp.setCompressionLevel(Zip4jConstants.DEFLATE_LEVEL_ULTRA);
				}
				String pswd=dlg_pswd.getText().toString();
				if (dlg_rb_none.isChecked()) {
					zp.setCompressionMethod(CompressionMethod.DEFLATE);
				} else if (dlg_rb_std.isChecked()) {
					zp.setCompressionMethod(CompressionMethod.DEFLATE);
					zp.setEncryptionMethod(EncryptionMethod.ZIP_STANDARD);
					zp.setEncryptFiles(true);
					zp.setPassword(pswd);
				} else if (dlg_rb_aes128.isChecked()) {
					zp.setCompressionMethod(CompressionMethod.DEFLATE);
					zp.setEncryptionMethod(EncryptionMethod.AES);
					zp.setAesKeyStrength(AesKeyStrength.KEY_STRENGTH_128);
					zp.setEncryptFiles(true);
					zp.setPassword(pswd);
				} else if (dlg_rb_aes256.isChecked()) {
					zp.setCompressionMethod(CompressionMethod.DEFLATE);
                    zp.setEncryptionMethod(EncryptionMethod.AES);
                    zp.setAesKeyStrength(AesKeyStrength.KEY_STRENGTH_256);
					zp.setEncryptFiles(true);
					zp.setPassword(pswd);
				}
				p_ntfy.notifyToListener(true, new Object[]{zp});
			}
		});

		dlg_cancel.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				dialog.dismiss();
				p_ntfy.notifyToListener(false, null);
			}
		});
    	dialog.show();
	};

	private static void checkZipParmValidation(GlobalParameters mGp, Dialog dialog, String fp,
                                               CustomZipFile zf) {
    	final EditText dlg_pswd=(EditText)dialog.findViewById(R.id.zip_parm_dlg_enc_password);
    	final EditText dlg_conf=(EditText)dialog.findViewById(R.id.zip_parm_dlg_enc_confirm);
    	final TextView dlg_msg=(TextView)dialog.findViewById(R.id.zip_parm_dlg_msg);

//    	final Spinner dlg_comp_level=(Spinner)dialog.findViewById(R.id.zip_parm_dlg_comp_level);

    	final RadioButton dlg_rb_none=(RadioButton)dialog.findViewById(R.id.zip_parm_dlg_enc_type_rb_none);
//    	final RadioButton dlg_rb_std=(RadioButton)dialog.findViewById(R.id.zip_parm_dlg_enc_type_rb_standard);
//    	final RadioButton dlg_rb_aes128=(RadioButton)dialog.findViewById(R.id.zip_parm_dlg_enc_type_rb_aes128);
//    	final RadioButton dlg_rb_aes256=(RadioButton)dialog.findViewById(R.id.zip_parm_dlg_enc_type_rb_aes256);
    	final Button dlg_ok=(Button)dialog.findViewById(R.id.zip_parm_dlg_ok_btn);

    	if (dlg_rb_none.isChecked()) {
			dlg_msg.setText("");
			dlg_ok.setEnabled(true);;
    	} else {
			if (dlg_pswd.getText().length()>0) {
				if (dlg_pswd.getText().toString().equals(dlg_conf.getText().toString())) {
					dlg_msg.setText("");
					dlg_ok.setEnabled(true);;
				} else {
					dlg_msg.setText(mGp.appContext.getString(R.string.msgs_zip_parm_confirm_pswd_unmatched));
					dlg_ok.setEnabled(false);;
				}
				dlg_conf.setEnabled(true);
			} else {
				dlg_ok.setEnabled(false);;
				dlg_msg.setText(mGp.appContext.getString(R.string.msgs_zip_parm_pswd_not_specified));
				dlg_conf.setEnabled(false);
			}
    	}
	};

	private static void setZipCompLevelSpinner(GlobalParameters mGp, Activity mActivity, Spinner spinner) {
		CommonUtilities.setSpinnerBackground(mActivity, spinner, mGp.themeIsLight);
		final CustomSpinnerAdapter adapter=
				new CustomSpinnerAdapter(mActivity, android.R.layout.simple_spinner_item);
		adapter.setSpinner(spinner);
		adapter.add(mGp.appContext.getString(R.string.msgs_zip_parm_zip_comp_level_fastest));
		adapter.add(mGp.appContext.getString(R.string.msgs_zip_parm_zip_comp_level_fast));
		adapter.add(mGp.appContext.getString(R.string.msgs_zip_parm_zip_comp_level_normal));
		adapter.add(mGp.appContext.getString(R.string.msgs_zip_parm_zip_comp_level_maximum));
		adapter.setDropDownViewResource(android.R.layout.select_dialog_singlechoice);
		spinner.setPrompt(mGp.appContext.getString(R.string.msgs_zip_parm_zip_comp_level_promopt));
		spinner.setAdapter(adapter);

		spinner.setSelection(2);
	};

	private void extractDlg(final CustomTreeFilelistAdapter tfa) {
		NotifyEvent ntfy=new NotifyEvent(mContext);
		ntfy.setListener(new NotifyEventListener(){
			@Override
			public void positiveResponse(Context c, Object[] o) {
//				final String dest_path=((String)o[0]).endsWith("/")?((String)o[0]).substring(0,((String)o[0]).length()-1):((String)o[0]);
                final String dest_path=((String)o[1]);
				String w_conf_list="";
				String sep="";
				for (TreeFilelistItem item:tfa.getDataList()) {
					if (item.isChecked() || !tfa.isItemSelected()) {
						 w_conf_list+=sep+item.getZipFileName();
						sep="\n";
					}
				}
				final String conf_list=w_conf_list;
				NotifyEvent ntfy_confirm=new NotifyEvent(mContext);
				ntfy_confirm.setListener(new NotifyEventListener(){
					@Override
					public void positiveResponse(Context c, Object[] o) {
						String t_cd=mCurrentDirectory.getText().equals("/")?"":mCurrentDirectory.getText().toString().substring(1);
						prepareExtractMultipleItem(mCurrentFilePath, mEncodingSelected,
								tfa, t_cd, dest_path, conf_list, null, true, true);
					}
					@Override
					public void negativeResponse(Context c, Object[] o) {
					}
				});
				mCommonDlg.showCommonDialog(true, "W",
						String.format(mContext.getString(R.string.msgs_zip_extract_file_confirm_extract),dest_path),
						conf_list, ntfy_confirm);
			}
			@Override
			public void negativeResponse(Context c, Object[] o) {
			}
		});
        boolean include_root=false;
        boolean scoped_storage_mode=mGp.safMgr.isScopedStorageMode();
        CommonFileSelector2 fsdf=
                CommonFileSelector2.newInstance(scoped_storage_mode, true, false, CommonFileSelector2.DIALOG_SELECT_CATEGORY_DIRECTORY,
                        true, true, SafFile3.SAF_FILE_PRIMARY_UUID, "", "", mContext.getString(R.string.msgs_zip_extract_select_dest_directory));
        fsdf.showDialog(false, mActivity.getSupportFragmentManager(), fsdf, ntfy);
	};

	private void prepareExtractMultipleItem(final String zip_file_path, final String zip_file_encoding,
                                            final CustomTreeFilelistAdapter tfa, final String zip_curr_dir,
                                            final String dest_path, final String conf_list, final NotifyEvent p_ntfy,
                                            final boolean comp_msg_required, final boolean scan_media) {
		mConfirmResponse=0;

		setUiDisabled();
		showDialogProgress();
		final ThreadCtrl tc=new ThreadCtrl();
		mDialogProgressSpinMsg1.setVisibility(TextView.GONE);
		mDialogProgressSpinCancel.setEnabled(true);
		mDialogProgressSpinCancel.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				confirmCancel(tc,mDialogProgressSpinCancel);
			}
		});
		Thread th=new Thread(){
			@SuppressWarnings("unchecked")
			@Override
			public void run() {
				mUtil.addDebugMsg(1, "I", "Extract started");
				putProgressMessage(mContext.getString(R.string.msgs_zip_extract_file_started));

				final CustomZipFile zf=createZipFile(mContext, zip_file_path, zip_file_encoding);
				ArrayList<FileHeader> zf_fhl=null;
				StackTraceElement[] ste=null;
				try {
                    zf_fhl=(ArrayList<FileHeader>) zf.getFileHeaders();
                } catch(Exception e) {
				    e.printStackTrace();
                }
				if (zf_fhl!=null) {
                    ArrayList<FileHeader> sel_fhl=new ArrayList<FileHeader>();
                    for(FileHeader fh_item:zf_fhl) {
                        for(TreeFilelistItem sel_tfli:tfa.getDataList()) {
                            if (sel_tfli.isChecked() || !tfa.isItemSelected()) {
                                if (sel_tfli.isDirectory()) {
                                    if (fh_item.getFileName().startsWith(sel_tfli.getZipFileName()+"/")) {
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
                    }
                    ArrayList<FileHeader> ext_fhl=new ArrayList<FileHeader>();
                    extractMultipleItem(tc, dest_path, zf, zip_curr_dir, sel_fhl, ext_fhl, conf_list, p_ntfy, comp_msg_required, scan_media);

                    mUiHandler.post(new Runnable(){
                        @Override
                        public void run() {
                            mTreeFilelistAdapter.setAllItemUnchecked();
                            mTreeFilelistAdapter.notifyDataSetChanged();
                        }
                    });

                    mUtil.addDebugMsg(1, "I", "Extract exited");
                } else {
				    String ste_info="";
				    for(StackTraceElement element:ste) ste_info+=element.toString()+"\n";
                    mCommonDlg.showCommonDialog(false, "E",
                            mContext.getString(R.string.msgs_zip_extract_file_end_with_error)+"\n"+ste_info, conf_list, null);
                    mUtil.addDebugMsg(1, "I", "Extract exited with error"+"\n"+ste_info);
                }
			}
		};
		th.setName("extract");
		th.setPriority(Thread.MIN_PRIORITY);
		th.start();
	};

	private boolean isExtractEnded(final ThreadCtrl tc, final String dest_path, final CustomZipFile zf,
                                   final ArrayList<FileHeader> selected_fh_list, final ArrayList<FileHeader> extracted_fh_list,
                                   final String conf_list, final NotifyEvent p_ntfy, final boolean comp_msg_required) {
		if ((selected_fh_list.size()==0)) {
			mUtil.addDebugMsg(2, "I", CommonUtilities.getExecutedMethodName()+" Extract ended");
			if (tc.isEnabled() && comp_msg_required) {
//				mCommonDlg.showCommonDialog(false, "I",
//						mContext.getString(R.string.msgs_zip_extract_file_completed), conf_list, null);
                showToast(mActivity, mContext.getString(R.string.msgs_zip_extract_file_completed));
			}
//			if (!comp_msg_required)
			mUiHandler.post(new Runnable(){
				@Override
				public void run() {
					setUiEnabled();
					hideDialog();
				}
			});
			if (p_ntfy!=null) p_ntfy.notifyToListener(true, null);
			return true;
		}
		return false;
	};

	private boolean extractMultipleItem(final ThreadCtrl tc, final String dest_path, final CustomZipFile zf, final String zip_curr_dir,
                                        final ArrayList<FileHeader> selected_fh_list, final ArrayList<FileHeader> extracted_fh_list,
                                        final String conf_list, final NotifyEvent p_ntfy, final boolean comp_msg_required, final boolean scan_media) {

		mUtil.addDebugMsg(2, "I", CommonUtilities.getExecutedMethodName()+" entered, size="+selected_fh_list.size());
		try {
			while(selected_fh_list.size()>0) {
				final FileHeader fh_item=selected_fh_list.get(0);
				selected_fh_list.remove(0);
				extracted_fh_list.add(fh_item);
				if (fh_item.isDirectory()) {
                    String fp=dest_path+"/"+fh_item.getFileName().replace(zip_curr_dir,"");
                    SafFile3 sf=new SafFile3(mContext, fp);
                    if (!sf.exists()) sf.mkdirs();
				} else {
					final NotifyEvent ntfy_pswd=new NotifyEvent(mContext);
					ntfy_pswd.setListener(new NotifyEventListener(){
						@Override
						public void positiveResponse(Context c, Object[] o) {
							mMainPassword=(String)o[0];
							extractSelectedItem(tc, dest_path, zf, zip_curr_dir, selected_fh_list, extracted_fh_list, fh_item,
									conf_list, true, p_ntfy, comp_msg_required, scan_media);
						}
						@Override
						public void negativeResponse(Context c, Object[] o) {
							mUiHandler.post(new Runnable(){
								@Override
								public void run() {
									setUiEnabled();
									hideDialog();
								}
							});
							if (p_ntfy!=null) p_ntfy.notifyToListener(false, null);
						}
					});

//					Log.v("","name="+fh_item.getFileName()+", m="+zip_curr_dir);
					if (fh_item.isEncrypted()) {
						if (!mMainPassword.isEmpty()) {
							zf.setPassword(mMainPassword);
							if (!isCorrectZipFilePassword(zf, fh_item, mMainPassword)) {
								mUiHandler.post(new Runnable(){
									@Override
									public void run() {
                                        if (isSupportedCompressionMethod(fh_item)) getZipPasswordDlg(mActivity, mGp, mMainPassword, zf, fh_item, ntfy_pswd, true);
                                        else {
                                            mCommonDlg.showCommonDialog(false, "E", "Extract error", "Unsupported compression method. code="+
                                                    getCompressionMethodName(fh_item), null);
                                        }
									}
								});
								break;
							} else {
								boolean rc=extractSelectedItem(tc, dest_path, zf, zip_curr_dir, selected_fh_list, extracted_fh_list, fh_item,
										conf_list, false, p_ntfy, comp_msg_required, scan_media);
								if (!rc) break;
							}
						} else {
							mUiHandler.post(new Runnable(){
								@Override
								public void run() {
									if (isSupportedCompressionMethod(fh_item)) getZipPasswordDlg(mActivity, mGp, mMainPassword, zf, fh_item, ntfy_pswd, true);
                                    else {
                                        CompressionMethod cm=getCompressionMethod(fh_item);
                                        mCommonDlg.showCommonDialog(false, "E", "Extract error", "Unsupported compression method. code="+
                                                getCompressionMethodName(fh_item), null);
                                    }
								}
							});
							break;
						}
					} else {
						boolean rc=extractSelectedItem(tc, dest_path, zf, zip_curr_dir, selected_fh_list, extracted_fh_list, fh_item,
								conf_list, false, p_ntfy, comp_msg_required, scan_media);
						if (!rc) break;
					}
				}
				isExtractEnded(tc, dest_path, zf, selected_fh_list, extracted_fh_list, conf_list, p_ntfy, comp_msg_required);
				if (!tc.isEnabled()) break;
			}
		} catch(Exception e) {
			mUtil.addLogMsg("I", e.getMessage());
			CommonUtilities.printStackTraceElement(mUtil, e.getStackTrace());
			return false;
		}
		return true;
	};

	private boolean extractSelectedItem(final ThreadCtrl tc, final String dest_path, final CustomZipFile zf, final String zip_curr_dir,
                                        final ArrayList<FileHeader> selected_fh_list, final ArrayList<FileHeader> extracted_fh_list,
                                        FileHeader fh_item, String conf_list, boolean call_child, final NotifyEvent p_ntfy,
                                        final boolean comp_msg_required, final boolean scan_media) {
		String dir="", fn=fh_item.getFileName();
		boolean result=true;
		if (fh_item.getFileName().lastIndexOf("/")>0) {
			dir=fh_item.getFileName().substring(0,fh_item.getFileName().lastIndexOf("/")).replace(zip_curr_dir,"");
			fn=fh_item.getFileName().substring(fh_item.getFileName().lastIndexOf("/")+1);
		}
		if (confirmReplace(tc, dest_path+"/"+dir, fn)) {
			if (extractSpecificFile(tc, zf, fh_item.getFileName(), dest_path+"/"+dir, fn, scan_media)) {
				if (tc.isEnabled()) {
					putProgressMessage(String.format(mContext.getString(R.string.msgs_zip_extract_file_was_extracted), fh_item.getFileName()));
					mUtil.addLogMsg("I", String.format(mContext.getString(R.string.msgs_zip_extract_file_was_extracted), fh_item.getFileName()));
					if (call_child && !isExtractEnded(tc, dest_path, zf, selected_fh_list, extracted_fh_list, conf_list, p_ntfy,comp_msg_required))
						extractMultipleItem(tc, dest_path, zf, zip_curr_dir, selected_fh_list, extracted_fh_list, conf_list, p_ntfy,comp_msg_required, scan_media);
				} else {
					result=false;
					mUiHandler.post(new Runnable(){
						@Override
						public void run() {
							setUiEnabled();
							hideDialog();
						}
					});
					if (p_ntfy!=null) p_ntfy.notifyToListener(false, null);
				}
			} else {
				result=false;
				mCommonDlg.showCommonDialog(false, "E", mContext.getString(R.string.msgs_zip_extract_file_was_failed),
						tc.getThreadMessage(), null);
				mUiHandler.post(new Runnable(){
					@Override
					public void run() {
						setUiEnabled();
						hideDialog();
					}
				});
				if (p_ntfy!=null) p_ntfy.notifyToListener(false, null);
			}
		} else {
			//Reject replace request
			if (tc.isEnabled()) {
				putProgressMessage(
						mContext.getString(R.string.msgs_zip_extract_file_was_not_replaced)+dest_path+"/"+dir+"/"+fn);
				mUtil.addLogMsg("I",
						mContext.getString(R.string.msgs_zip_extract_file_was_not_replaced)+dest_path+"/"+dir+"/"+fn);
				if (call_child && !isExtractEnded(tc, dest_path, zf, selected_fh_list, extracted_fh_list, conf_list, p_ntfy,comp_msg_required))
					extractMultipleItem(tc, dest_path, zf, zip_curr_dir, selected_fh_list, extracted_fh_list, conf_list, p_ntfy,comp_msg_required, scan_media);
			}
		}
		if (!tc.isEnabled()) {
			result=false;
			mCommonDlg.showCommonDialog(false, "W",
					mContext.getString(R.string.msgs_zip_extract_file_was_cancelled), "", null);
			mUiHandler.post(new Runnable(){
				@Override
				public void run() {
					setUiEnabled();
					hideDialog();
				}
			});
			if (p_ntfy!=null) p_ntfy.notifyToListener(false, null);
		}
		return result;
	};

	static final private int CONFIRM_RESPONSE_CANCEL=-99;
	static final private int CONFIRM_RESPONSE_YES=1;
	static final private int CONFIRM_RESPONSE_YESALL=2;
	static final private int CONFIRM_RESPONSE_NO=-1;
	static final private int CONFIRM_RESPONSE_NOALL=-2;
	private int mConfirmResponse=0;
	private boolean confirmReplace(final ThreadCtrl tc, final String dest_dir, final String dest_name) {
		final String w_path=dest_dir.endsWith("/")?dest_dir+dest_name:dest_dir+"/"+dest_name;
		File lf=new File(w_path);
//		Log.v("","name="+lf.getPath()+", exists="+lf.exists());
		boolean result=false;
		mUtil.addDebugMsg(1, "I", CommonUtilities.getExecutedMethodName()+" entered, response="+mConfirmResponse);
		if (lf.exists()) {
			if (mConfirmResponse!=CONFIRM_RESPONSE_YESALL && mConfirmResponse!=CONFIRM_RESPONSE_NOALL) {
//				Log.v("","show");
				mUiHandler.post(new Runnable(){
					@Override
					public void run() {
						mUtil.addDebugMsg(1, "I", CommonUtilities.getExecutedMethodName()+" show confirm");
						mDialogProgressSpinView.setVisibility(LinearLayout.GONE);
						mDialogConfirmView.setVisibility(LinearLayout.VISIBLE);
						mDialogConfirmCancel.setOnClickListener(new OnClickListener(){
							@Override
							public void onClick(View v) {
								mDialogProgressSpinView.setVisibility(LinearLayout.VISIBLE);
								mDialogConfirmView.setVisibility(LinearLayout.GONE);
								mConfirmResponse=CONFIRM_RESPONSE_CANCEL;
								tc.setDisabled();
								synchronized(tc) {tc.notify();}
							}
						});

						mDialogConfirmYes.setOnClickListener(new OnClickListener(){
							@Override
							public void onClick(View v) {
								mDialogProgressSpinView.setVisibility(LinearLayout.VISIBLE);
								mDialogConfirmView.setVisibility(LinearLayout.GONE);
								mConfirmResponse=CONFIRM_RESPONSE_YES;
								synchronized(tc) {tc.notify();}
							}
						});
						mDialogConfirmYesAll.setOnClickListener(new OnClickListener(){
							@Override
							public void onClick(View v) {
								mDialogProgressSpinView.setVisibility(LinearLayout.VISIBLE);
								mDialogConfirmView.setVisibility(LinearLayout.GONE);
								mConfirmResponse=CONFIRM_RESPONSE_YESALL;
								synchronized(tc) {tc.notify();}
							}
						});
						mDialogConfirmNo.setOnClickListener(new OnClickListener(){
							@Override
							public void onClick(View v) {
								mDialogProgressSpinView.setVisibility(LinearLayout.VISIBLE);
								mDialogConfirmView.setVisibility(LinearLayout.GONE);
								mConfirmResponse=CONFIRM_RESPONSE_NO;
								synchronized(tc) {tc.notify();}
							}
						});
						mDialogConfirmNoAll.setOnClickListener(new OnClickListener(){
							@Override
							public void onClick(View v) {
								mDialogProgressSpinView.setVisibility(LinearLayout.VISIBLE);
								mDialogConfirmView.setVisibility(LinearLayout.GONE);
								mConfirmResponse=CONFIRM_RESPONSE_NOALL;
								synchronized(tc) {tc.notify();}
							}
						});
						mDialogConfirmMsg.setText(
								String.format(mContext.getString(R.string.msgs_zip_extract_file_confirm_replace_copy), w_path));
					}
				});

				synchronized(tc) {
					try {
						tc.wait();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}

				if (mConfirmResponse==CONFIRM_RESPONSE_CANCEL) {
				} else if (mConfirmResponse==CONFIRM_RESPONSE_YES) {
					result=true;
				} else if (mConfirmResponse==CONFIRM_RESPONSE_YESALL) {
					result=true;
				} else if (mConfirmResponse==CONFIRM_RESPONSE_NO) {
				} else if (mConfirmResponse==CONFIRM_RESPONSE_NOALL) {
				}
				return result;
			} else {
				if (mConfirmResponse==CONFIRM_RESPONSE_YESALL) {
					result=true;
				}
				return result;
			}
		} else {
			result=true;
		}
		mUtil.addDebugMsg(1, "I", CommonUtilities.getExecutedMethodName()+" result="+result+", response="+mConfirmResponse);
		return result;
	};

	static public void getZipPasswordDlg(Activity mActivity, GlobalParameters mGp, String mMainPassword,
                                         final CustomZipFile zf, final FileHeader fh, final NotifyEvent p_ntfy,
                                         final boolean thread_resp) {
		final Dialog dialog = new Dialog(mActivity, mGp.applicationTheme);
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialog.setCanceledOnTouchOutside(false);
		dialog.setContentView(R.layout.password_prompt_dlg);

		LinearLayout ll_dlg_view=(LinearLayout) dialog.findViewById(R.id.password_prompt_dlg_view);
//		ll_dlg_view.setBackgroundColor(mGp.themeColorList.dialog_msg_background_color);
//		ll_dlg_view.setBackgroundResource(R.drawable.dialog_border_dark);

    	LinearLayout title_view=(LinearLayout)dialog.findViewById(R.id.password_prompt_dlg_title_view);
    	title_view.setBackgroundColor(mGp.themeColorList.title_background_color);
    	TextView dlg_title=(TextView)dialog.findViewById(R.id.password_prompt_dlg_title);
    	dlg_title.setTextColor(mGp.themeColorList.title_text_color);
    	dlg_title.setText(R.string.msgs_zip_extract_zip_password_title);
    	final TextView dlg_msg=(TextView)dialog.findViewById(R.id.password_prompt_dlg_msg);
    	dlg_msg.setVisibility(TextView.VISIBLE);
    	final TextView dlg_filename=(TextView)dialog.findViewById(R.id.password_prompt_dlg_itemtext);
    	if (fh!=null) {
    		dlg_filename.setText(fh.getFileName());
    	} else {
    		dlg_filename.setText("");
    	}

    	final EditText dlg_pswd=(EditText)dialog.findViewById(R.id.password_prompt_dlg_itemname);

    	final Button dlg_cancel=(Button)dialog.findViewById(R.id.password_prompt_dlg_cancel_btn);
    	final Button dlg_ok=(Button)dialog.findViewById(R.id.password_prompt_dlg_ok_btn);
    	dlg_ok.setVisibility(Button.VISIBLE);

    	dlg_pswd.setVisibility(EditText.VISIBLE);

    	CommonDialog.setDlgBoxSizeLimit(dialog, true);

    	dlg_pswd.setText(mMainPassword);
        dlg_pswd.setEnabled(true);
    	verifyZipPassword(mActivity, zf, fh, mMainPassword, dlg_ok, dlg_msg);
    	dlg_pswd.addTextChangedListener(new TextWatcher(){
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
			}
			@Override
			public void afterTextChanged(Editable s) {
			    if (s.toString().length()>0) {
//			        verifyZipPassword(mActivity, zf, fh, s.toString(), dlg_ok, dlg_msg);
                    CommonDialog.setViewEnabled(mActivity, dlg_ok, true);
                    dlg_msg.setText("");
                } else {
			        CommonDialog.setViewEnabled(mActivity, dlg_ok, false);
                }
			}
    	});

		dlg_ok.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
			    if (isCorrectZipFilePassword(zf, fh, dlg_pswd.getText().toString())) {
                    dialog.dismiss();
                    if (thread_resp) {
                        Thread th=new Thread(){
                            @Override
                            public void run() {
                                p_ntfy.notifyToListener(true, new Object[]{dlg_pswd.getText().toString()});
                            }
                        };
                        th.start();
                    } else {
                        p_ntfy.notifyToListener(true, new Object[]{dlg_pswd.getText().toString()});
                    }
                } else {
                    dlg_msg.setText(R.string.msgs_zip_extract_zip_password_wrong);
                }
			}
		});

		dlg_cancel.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				dialog.dismiss();
				if (thread_resp) {
					Thread th=new Thread(){
						@Override
						public void run() {
							p_ntfy.notifyToListener(false, null);
						}
					};
					th.start();
				} else {
					p_ntfy.notifyToListener(false, null);
				}
			}
		});
    	dialog.show();
	};

	private static void verifyZipPassword(final Activity a, final CustomZipFile zf, final FileHeader fh, String pswd, Button dlg_ok, TextView dlg_msg) {
		if (pswd.length()>0) {
			if (isCorrectZipFilePassword(zf, fh, pswd)) {
			    CommonDialog.setViewEnabled(a, dlg_ok, true);
				dlg_msg.setText("");
			} else {
                CommonDialog.setViewEnabled(a, dlg_ok, false);
				dlg_msg.setText(R.string.msgs_zip_extract_zip_password_wrong);
			}
		} else {
            CommonDialog.setViewEnabled(a, dlg_ok, false);
			dlg_msg.setText(R.string.msgs_zip_extract_zip_password_not_specified);
		}
	}

	public static CustomZipFile createZipFile(Context c, String fp, String select_encoding) {
		CustomZipFile zf=null;
//		File lf=new File(fp);
        zf = new CustomZipFile(c, new SafFile3(c, fp), select_encoding);
		return zf;
	};

	public static boolean isCorrectZipFilePassword(final CustomZipFile zf, final FileHeader fh, String pswd) {
		if (zf==null || fh==null) return true;
		boolean result=false;
		if (pswd==null) return result;
		try {
			if (fh.isEncrypted()) {
				zf.setPassword(pswd);
				InputStream is=zf.getInputStream(fh);
				byte[] buff=new byte[512];
				@SuppressWarnings("unused")
				int rc=is.read(buff);
				result=true;
//				is.close();
			}
		} catch (ZipException e) {
//			e.printStackTrace();
		} catch (Exception e) {
//			e.printStackTrace();
		}
		return result;
	};

	@SuppressWarnings("unchecked")
	private ArrayList<FileHeader> buildSelectedFileHeaderList(CustomZipFile zf, CustomTreeFilelistAdapter tfa) {
		ArrayList<FileHeader> sel_fh=new ArrayList<FileHeader>();
		ArrayList<FileHeader> zf_list=null;
        try {
            zf_list = (ArrayList<FileHeader>) zf.getFileHeaders();
        } catch(Exception e) {
            e.printStackTrace();
        }
		if (zf_list!=null) {
			if (tfa.isItemSelected()) {
				for(FileHeader fh:zf_list) {
					for (TreeFilelistItem tfli:tfa.getDataList()) {
						if (tfli.isChecked()) {
							if (tfli.isDirectory()) {
								if (fh.getFileName().startsWith(tfli.getZipFileName()+"/")) {
									sel_fh.add(fh);
									break;
								}
							} else {
								if (fh.getFileName().equals(tfli.getZipFileName())) {
									sel_fh.add(fh);
									break;
								}
							}
						}
					}
				}
			} else {

			}
		}
		return sel_fh;
	};

	private void confirmDelete(final CustomTreeFilelistAdapter tfa) {
		String w_conf_list="";
		String sep="";
		for (TreeFilelistItem tfli:tfa.getDataList()) {
//			Log.v("","sel name="+tfli.getName());
			if (tfli.isChecked()) {
				w_conf_list+=sep+tfli.getZipFileName();
				sep="\n";
			}
		}
		final String conf_list=w_conf_list;
		NotifyEvent ntfy=new NotifyEvent(mContext);
		ntfy.setListener(new NotifyEventListener(){
			@Override
			public void positiveResponse(Context c, Object[] o) {
				deleteZipFileItem(mCurrentFilePath, mEncodingSelected, tfa, conf_list, null);//, true);
			}
			@Override
			public void negativeResponse(Context c, Object[] o) {}
		});
		mCommonDlg.showCommonDialog(true, "W",
				mContext.getString(R.string.msgs_zip_delete_confirm_delete), conf_list, ntfy);
	};

	private void deleteZipFileItem(final String zip_file_path, final String zip_encoding,
                                   final CustomTreeFilelistAdapter tfa, final String conf_list, final NotifyEvent p_ntfy){//}, final boolean com_msg_required) {
		setUiDisabled();
		showDialogProgress();
		final ThreadCtrl tc=new ThreadCtrl();
		mDialogProgressSpinCancel.setEnabled(true);
		mDialogProgressSpinMsg1.setVisibility(TextView.GONE);
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
				SafFile3 out_temp=new SafFile3(mContext, zip_file_path+".tmp");
				String zip_file_name=zip_file_path.substring(zip_file_path.lastIndexOf("/"));
				String error_msg="";
                putProgressMessage(mContext.getString(R.string.msgs_zip_delete_file_has_been_started));
                CustomZipFile zf=createZipFile(mContext, zip_file_path, zip_encoding);
                try {
                    BufferedZipFile3 bzf=new BufferedZipFile3(mContext, zip_file_path, out_temp.getPath(), ZipUtil.DEFAULT_ZIP_FILENAME_ENCODING, "");
                    ArrayList<FileHeader> sel_fh=buildSelectedFileHeaderList(zf, tfa);
                    String msg=mContext.getString(R.string.msgs_zip_delete_file_was_deleted);
                    for(FileHeader fh:sel_fh) {
                        if (!tc.isEnabled()) {
                            mCommonDlg.showCommonDialog(false, "I",
                                    String.format(mContext.getString(R.string.msgs_zip_delete_file_was_cancelled),fh.getFileName()), "", null);
                            try {
                                bzf.destroy();
                                deleteBufferedZipWork(mGp, mUtil, zip_file_path, out_temp.getPath());
                            } catch(ZipException e) {}
                            break;
                        } else {
                            bzf.removeItem(fh);
                            putProgressMessage(String.format(msg,fh.getFileName()));
                            mUtil.addLogMsg("I", String.format(msg,fh.getFileName()));
                        }
                    }
                    if (tc.isEnabled()) {
                        CallBackListener cbl=getZipProgressCallbackListener(tc, bzf, mContext.getString(R.string.msgs_zip_zip_file_being_updated));
                        if (bzf.isAborted() || bzf.close(cbl)) {
                            if (!bzf.isAborted()) {
                                renameBufferedZipFile(mGp, mUtil, zip_file_path, out_temp.getPath(), zip_file_name);
                                showToast(mActivity, mContext.getString(R.string.msgs_zip_delete_file_completed));
                            }
                            else {
                                mCommonDlg.showCommonDialog(false, "W", mContext.getString(R.string.msgs_zip_write_zip_file_canelled), "", null);
                                try {bzf.destroy();} catch (Exception e) {}
                                deleteBufferedZipWork(mGp, mUtil, zip_file_path, out_temp.getPath());
                                closeUiDialogView(500);
                                return;
                            }
                        }

                    }
                } catch (Exception e) {
                    mUtil.addLogMsg("I", e.getMessage());
                    error_msg=e.getMessage();
                    CommonUtilities.printStackTraceElement(mUtil, e.getStackTrace());
                    mCommonDlg.showCommonDialog(false, "I",
                            mContext.getString(R.string.msgs_zip_delete_file_aborted), error_msg, null);
                }
                mUtil.addDebugMsg(1, "I", "Delete ended");

                mUiHandler.postDelayed(new Runnable(){
                    @Override
                    public void run() {
                        setUiEnabled();
                        refreshFileList();
                        if (p_ntfy!=null) p_ntfy.notifyToListener(true, null);
                    }
                },100);
			}
		};
		th.setName("Delete");
		th.start();

	}

//    static public BufferedZipFile3 createBufferedZipFile(GlobalParameters gp, CommonUtilities util, String dest_path, String out_path) {
//        BufferedZipFile3 bzf=null;
//        util.addDebugMsg(1,"I","createBufferedZipFile entered");
//        long b_time= System.currentTimeMillis();
//        SafFile3 out=new SafFile3(gp.appContext, out_path);
//        SafFile3 dest=dest_path==null?null:new SafFile3(gp.appContext, dest_path);
//        try {
//            if (dest!=null) if (!dest.exists()) dest.createNewFile();
//            if (!out.exists()) out.createNewFile();
//            bzf=new BufferedZipFile3(gp.appContext, dest, out, ZipUtil.DEFAULT_ZIP_FILENAME_ENCODING, out.getAppDirectoryCache());
//        } catch (Exception e) {
//            e.printStackTrace();
//            util.addLogMsg("E","createBufferedZipFile failed. msg="+e.getMessage());
//            util.addLogMsg("E","dest="+dest_path+", out="+out_path);
//            try {if (out!=null && out.exists()) out.delete();} catch(Exception e2){};
//        }
//        util.addDebugMsg(1,"I","createBufferedZipFile elapsed time="+(System.currentTimeMillis()-b_time));
//        return bzf;
//    }

    static public void renameBufferedZipFile(GlobalParameters gp, CommonUtilities util, String dest_path, String out_path, String zip_file_name) {
        util.addDebugMsg(1,"I","renameBufferedZipFile entered");
        long b_time= System.currentTimeMillis();
        SafFile3 df=new SafFile3(gp.appContext, dest_path);
        SafFile3 of=new SafFile3(gp.appContext, out_path);
        df.deleteIfExists();
        of.renameTo(df);
        util.addDebugMsg(1,"I","renameBufferedZipFile elapsed time="+(System.currentTimeMillis()-b_time));
    }

    static public void deleteBufferedZipWork(GlobalParameters gp, CommonUtilities util, String dest_path, String out_path) {
        SafFile3 of=new SafFile3(gp.appContext, out_path);
        of.deleteIfExists();
    }

	private void putProgressMessage(final String msg) {
		mUiHandler.post(new Runnable(){
			@Override
			public void run() {
				mDialogProgressSpinMsg2.setText(msg);
			}
		});
	}

	final private void refreshOptionMenu() {
		mActivity.invalidateOptionsMenu();
	};

	private void setUiEnabled() {
		mActivity.setUiEnabled();
		hideDialog();
		mZipFileSpinner.setEnabled(true);
		mEncodingSpinner.setEnabled(true);
		refreshOptionMenu();
	};

	private void setUiDisabled() {
		mActivity.setUiDisabled();
		mZipFileSpinner.setEnabled(false);
		mEncodingSpinner.setEnabled(false);
		refreshOptionMenu();
	};

	private boolean isUiEnabled() {
		return mActivity.isUiEnabled();
	};

	public void showDialogProgress() {
		mDialogProgressSpinView.setVisibility(LinearLayout.VISIBLE);
        mMainDialogView.bringToFront();
        mMainDialogView.setBackgroundColor(mGp.themeColorList.text_background_color);
    };

    private void hideDialog() {
		mDialogProgressSpinView.setVisibility(LinearLayout.GONE);
		mDialogConfirmView.setVisibility(LinearLayout.GONE);
	};

	private void openSppecificDirectory(String dir_name, String file_name) {
		ArrayList<TreeFilelistItem> tfl=createTreeFileList(mZipFileList, dir_name);
		mTreeFilelistAdapter.setDataList(tfl);
		mCurrentDirectory.setText("/"+dir_name);
		int sel_pos=0;
		if (tfl.size()>0) {
			if (!file_name.equals("")) {
				for(int i=0;i<tfl.size();i++) {
					TreeFilelistItem tfli=tfl.get(i);
					if (tfli.getName().equals(file_name)) {
						sel_pos=i;
//						tfli.setChecked(true);
						break;
					}
				}
			}
			mTreeFilelistView.setSelection(sel_pos);
		}
		setTopUpButtonEnabled(true);
	};

	public void setContextButtonPasteEnabled(boolean enabled) {
		if (enabled) mContextButtonPasteView.setVisibility(LinearLayout.VISIBLE);
		else mContextButtonPasteView.setVisibility(LinearLayout.INVISIBLE);
	};

	private String mEncodingDesired="";
	private String mEncodingSelected=ENCODING_NAME_UTF8;
	private void setZipTreeFileListener() {
		mEncodingSpinner.setOnItemSelectedListener(new OnItemSelectedListener(){
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				String sel_encoding=mEncodingSpinner.getSelectedItem().toString();
				if (!mEncodingDesired.equals(sel_encoding)) {
					mEncodingDesired=sel_encoding;
					refreshFileList(true);
				}
			}
			@Override
			public void onNothingSelected(AdapterView<?> parent) {
			}
		});

		mContextButtonDeleteView.setVisibility(ImageButton.INVISIBLE);
		NotifyEvent ntfy_cb=new NotifyEvent(mContext);
		ntfy_cb.setListener(new NotifyEventListener(){
			@Override
			public void positiveResponse(Context c, Object[] o) {
				if (!isUiEnabled()) return;
                if (mCurretnFileIsReadOnly) mContextButtonDeleteView.setVisibility(ImageButton.INVISIBLE);
                else mContextButtonDeleteView.setVisibility(ImageButton.VISIBLE);
				if (mTreeFilelistAdapter.isAllItemSelected()) mContextButtonSelectAllView.setVisibility(ImageButton.INVISIBLE);
				else mContextButtonSelectAllView.setVisibility(ImageButton.VISIBLE);
				if (mTreeFilelistAdapter.getSelectedItemCount()==0) mContextButtonUnselectAllView.setVisibility(ImageButton.INVISIBLE);
				else mContextButtonUnselectAllView.setVisibility(ImageButton.VISIBLE);

				setContextCopyCutPasteButton(mTreeFilelistAdapter);

				mContextButtonOpenView.setVisibility(ImageButton.INVISIBLE);
				mContextButtonNewView.setVisibility(ImageButton.INVISIBLE);
			}
			@Override
			public void negativeResponse(Context c, Object[] o) {
				if (!isUiEnabled()) return;
				if (mTreeFilelistAdapter.isItemSelected()) {
				    if (mCurretnFileIsReadOnly) mContextButtonDeleteView.setVisibility(ImageButton.INVISIBLE);
				    else mContextButtonDeleteView.setVisibility(ImageButton.VISIBLE);
					mContextButtonOpenView.setVisibility(ImageButton.INVISIBLE);
					mContextButtonNewView.setVisibility(ImageButton.INVISIBLE);
					mContextButtonPasteView.setVisibility(ImageButton.INVISIBLE);
					mContextButtonCopyView.setVisibility(ImageButton.VISIBLE);
				} else {
					mContextButtonDeleteView.setVisibility(ImageButton.INVISIBLE);
					mContextButtonOpenView.setVisibility(ImageButton.VISIBLE);
					mContextButtonNewView.setVisibility(ImageButton.VISIBLE);
					mContextButtonPasteView.setVisibility(ImageButton.VISIBLE);
					mContextButtonCopyView.setVisibility(ImageButton.INVISIBLE);

				}
				setContextCopyCutPasteButton(mTreeFilelistAdapter);
				if (mTreeFilelistAdapter.isAllItemSelected()) mContextButtonSelectAllView.setVisibility(ImageButton.INVISIBLE);
				else mContextButtonSelectAllView.setVisibility(ImageButton.VISIBLE);
				if (mTreeFilelistAdapter.getSelectedItemCount()==0) mContextButtonUnselectAllView.setVisibility(ImageButton.INVISIBLE);
				else mContextButtonUnselectAllView.setVisibility(ImageButton.VISIBLE);
			}
		});
		mTreeFilelistAdapter.setCbCheckListener(ntfy_cb);
        mTreeFilelistView.setOnItemClickListener(new OnItemClickListener(){
        	public void onItemClick(AdapterView<?> items, View view, int idx, long id) {
        		if (!isUiEnabled()) return;
//	    		final int pos=mTreeFilelistAdapter.getItem(idx);
	    		final TreeFilelistItem tfi=mTreeFilelistAdapter.getItem(idx);
				if (tfi.getName().startsWith("---")) return;
				if (!mTreeFilelistAdapter.isItemSelected() && tfi.isDirectory()) {
					String curr_dir=mCurrentDirectory.getText().toString().substring(1);
					FileManagerDirectoryListItem dli=
							CommonUtilities.getDirectoryItem(mDirectoryList, mZipFileSpinner.getSelectedItem().toString()+"/"+curr_dir);
					if (dli==null) {
						dli=new FileManagerDirectoryListItem();
						dli.file_path=mZipFileSpinner.getSelectedItem().toString()+"/"+curr_dir;
						mDirectoryList.add(dli);
					}
					dli.file_list=mTreeFilelistAdapter.getDataList();
					dli.pos_x=mTreeFilelistView.getFirstVisiblePosition();
					dli.pos_y=mTreeFilelistView.getChildAt(0)==null?0:mTreeFilelistView.getChildAt(0).getTop();
//					Log.v("","saved="+zipFileSpinner.getSelectedItem().toString()+"/"+curr_dir);
//					Log.v("","x="+dli.pos_x+", y="+dli.pos_y);
					String dir=tfi.getPath().equals("")?tfi.getName():tfi.getPath()+"/"+tfi.getName();
					ArrayList<TreeFilelistItem> tfl=createTreeFileList(mZipFileList, dir);
					mTreeFilelistAdapter.setDataList(tfl);
					mCurrentDirectory.setText("/"+dir);
					if (tfl.size()>0) {
						mTreeFilelistView.setSelection(0);
						mContextButtonSelectAllView.setVisibility(LinearLayout.VISIBLE);
					} else mContextButtonSelectAllView.setVisibility(LinearLayout.INVISIBLE);
					setTopUpButtonEnabled(true);
				} else {
					if (mTreeFilelistAdapter.isItemSelected()) {
						tfi.setChecked(!tfi.isChecked());
						mTreeFilelistAdapter.notifyDataSetChanged();
					} else {
						invokeBrowser(tfi.isZipEncrypted(), tfi, tfi.getPath(), tfi.getName(), "");
					}
				}
				setContextCopyCutPasteButton(mTreeFilelistAdapter);
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
				CommonUtilities.clearDirectoryItem(mDirectoryList, mZipFileSpinner.getSelectedItem().toString()+"/");
				String dir="";
				ArrayList<TreeFilelistItem> tfl=createTreeFileList(mZipFileList, dir);
				mTreeFilelistAdapter.setDataList(tfl);
				mCurrentDirectory.setText("/");
				setTopUpButtonEnabled(false);
				mTreeFilelistView.setSelectionFromTop(0, 0);
				setContextCopyCutPasteButton(mTreeFilelistAdapter);
			}
        });

        mFileListUp.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				if (!isUiEnabled()) return;
				String dir=mCurrentDirectory.getText().toString();
				if (!dir.equals("/")) {
					FileManagerDirectoryListItem dli=
							CommonUtilities.getDirectoryItem(mDirectoryList, mZipFileSpinner.getSelectedItem().toString()+dir);

					CommonUtilities.removeDirectoryItem(mDirectoryList, dli);
					String n_dir=dir.lastIndexOf("/")>0?dir.substring(1,dir.lastIndexOf("/")):"";
					FileManagerDirectoryListItem p_dli=
							CommonUtilities.getDirectoryItem(mDirectoryList, mZipFileSpinner.getSelectedItem().toString()+"/"+n_dir);
//					Log.v("","loaded="+zipFileSpinner.getSelectedItem().toString()+"/"+n_dir);
//					Log.v("","x="+p_dli.pos_x+", y="+p_dli.pos_y);
//					Log.v("","dir="+dir+", idx="+dir.lastIndexOf("/"));
					ArrayList<TreeFilelistItem> tfl=createTreeFileList(mZipFileList, n_dir);
					mTreeFilelistAdapter.setDataList(tfl);
					mCurrentDirectory.setText("/"+n_dir);

					if (n_dir.equals("")) {
						setTopUpButtonEnabled(false);
					}
					if (tfl.size()>0) {
//						mTreeFilelistView.setSelection(0);
						if (p_dli!=null) mTreeFilelistView.setSelectionFromTop(p_dli.pos_x, p_dli.pos_y);
						mContextButtonSelectAllView.setVisibility(LinearLayout.VISIBLE);
					}
				} else {
					FileManagerDirectoryListItem p_dli=
							CommonUtilities.getDirectoryItem(mDirectoryList, mZipFileSpinner.getSelectedItem().toString()+"/");
					CommonUtilities.clearDirectoryItem(mDirectoryList, mZipFileSpinner.getSelectedItem().toString()+"/");
//					Log.v("","s1");
					ArrayList<TreeFilelistItem> tfl=createTreeFileList(mZipFileList, "");
					mTreeFilelistAdapter.setDataList(tfl);
					mCurrentDirectory.setText("/");
					setTopUpButtonEnabled(false);

					if (tfl.size()>0) {
//						mTreeFilelistView.setSelection(0);
						if (p_dli!=null) mTreeFilelistView.setSelectionFromTop(p_dli.pos_x, p_dli.pos_y);
						mContextButtonSelectAllView.setVisibility(LinearLayout.VISIBLE);
					} else mContextButtonSelectAllView.setVisibility(LinearLayout.INVISIBLE);
				}
				setContextCopyCutPasteButton(mTreeFilelistAdapter);
			}
        });
	};

	public boolean isUpButtonEnabled() {
		return mFileListUp.isEnabled();
	};

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
	};

	private void setTopUpButtonEnabled(boolean p) {
		mFileListUp.setEnabled(p);
		mFileListTop.setEnabled(p);
		if (p) {
			mFileListUp.setAlpha(1);
			mFileListTop.setAlpha(1);
		} else {
			mFileListUp.setAlpha(0.4f);
			mFileListTop.setAlpha(0.4f);
		}
	};

	private ZipFileListItem getZipFileListItem(String zfp) {
		for(ZipFileListItem zfli:mZipFileList) {
			if (zfli.getPath().equals(zfp)) {
				return zfli;
			}
		}
		return null;
	};

	private ArrayList<FileManagerDirectoryListItem> mDirectoryList=new ArrayList<FileManagerDirectoryListItem>();

	private void showContextMenu(final TreeFilelistItem tfi) {
        final CustomContextMenu mCcMenu = new CustomContextMenu(mContext.getResources(), mFragmentManager);
		String sel_list="",sep="";
		final CustomTreeFilelistAdapter tfa=new CustomTreeFilelistAdapter(mActivity, false, false, false);
		final ArrayList<TreeFilelistItem> n_tfl=new ArrayList<TreeFilelistItem>();
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

		if (!tfi.isChecked()) {
            mCcMenu.addMenuItem(mContext.getString(R.string.msgs_main_local_file_ccmenu_select)+"("+sel_list+")",R.drawable.menu_active)
                    .setOnClickListener(new CustomContextMenuOnClickListener() {
                @Override
                public void onClick(CharSequence menuTitle) {
                    tfi.setChecked(true);
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

        if (sel_count==1) {
			mCcMenu.addMenuItem(mContext.getString(R.string.msgs_main_local_file_ccmenu_property)+"("+(n_tfl.get(0)).getName()+")",R.drawable.dialog_information)
		  		.setOnClickListener(new CustomContextMenuOnClickListener() {
				@Override
				public void onClick(CharSequence menuTitle) {
					if (tfi.isDirectory()) {
						long item_cnt=-1, item_comp_size=0, item_uncomp_size=0;
						for(ZipFileListItem zfli:mZipFileList) {
							if (zfli.getPath().startsWith(tfi.getZipFileName())) {
								item_cnt++;
								item_uncomp_size+=zfli.getFileLength();
								item_comp_size+=zfli.getCompressedFileLength();
							}
						}
                        long comp_ratio=0;
                        if (item_uncomp_size!=0) comp_ratio=(item_comp_size*100)/item_uncomp_size;
						String prop= String.format(mContext.getString(R.string.msgs_zip_zip_item_property_directory),
								 item_cnt, item_comp_size, item_uncomp_size, comp_ratio);
						mCommonDlg.showCommonDialog(false, "I", "/"+tfi.getZipFileName()+"/", prop, null);
					} else {
						ZipFileListItem zfli=getZipFileListItem(tfi.getZipFileName());
						if (zfli!=null) {
							String comp_method=getCompressionMethodName(zfli.getCompressionMethod());
							String enc_method="None";
							if (zfli.getEncryptionMethod()==ZipFileListItem.ENCRPTION_METHOD_AES) enc_method="AES";
							else if (zfli.getEncryptionMethod()==ZipFileListItem.ENCRPTION_METHOD_ZIP) enc_method="ZIP";
							long comp_size=zfli.getCompressedFileLength();
							long uncomp_size=zfli.getFileLength();
							long comp_ratio=0;
							if (uncomp_size!=0) comp_ratio=(comp_size*100)/uncomp_size;
							long last_mod=zfli.getLastModifiedTime();
							String enc_yes_no=zfli.isEncrypted()?
									mContext.getString(R.string.msgs_zip_zip_item_property_encrypted_yes):
									mContext.getString(R.string.msgs_zip_zip_item_property_encrypted_no);
							String prop= String.format(mContext.getString(R.string.msgs_zip_zip_item_property_file),
									 StringUtil.convDateTimeTo_YearMonthDayHourMinSec(last_mod),
									 comp_method, enc_method, comp_size, uncomp_size, comp_ratio);
							mCommonDlg.showCommonDialog(false, "I", "/"+zfli.getFileName(), prop, null);
						}
					}

				}
		  	});
		}

		if (tfi.isDirectory() && sel_count==1) {
			mCcMenu.addMenuItem(mContext.getString(R.string.msgs_main_local_file_ccmenu_open_directory)+
					"("+(n_tfl.get(0)).getName()+")",R.drawable.cc_folder)
	  			.setOnClickListener(new CustomContextMenuOnClickListener() {
				@Override
				public void onClick(CharSequence menuTitle) {
					String curr_dir=mCurrentDirectory.getText().toString().substring(1);
					FileManagerDirectoryListItem dli=
							CommonUtilities.getDirectoryItem(mDirectoryList, mZipFileSpinner.getSelectedItem().toString()+"/"+curr_dir);
					if (dli==null) {
						dli=new FileManagerDirectoryListItem();
						dli.file_path=mZipFileSpinner.getSelectedItem().toString()+"/"+curr_dir;
						mDirectoryList.add(dli);
					}
					dli.file_list=mTreeFilelistAdapter.getDataList();
					dli.pos_x=mTreeFilelistView.getFirstVisiblePosition();
					dli.pos_y=mTreeFilelistView.getChildAt(0)==null?0:mTreeFilelistView.getChildAt(0).getTop();

					String dir=tfi.getPath().equals("")?tfi.getName():tfi.getPath()+"/"+tfi.getName();
					ArrayList<TreeFilelistItem> tfl=createTreeFileList(mZipFileList, dir);
					mTreeFilelistAdapter.setDataList(tfl);
					mCurrentDirectory.setText("/"+dir);
					if (tfl.size()>0) mTreeFilelistView.setSelection(0);
					setTopUpButtonEnabled(true);
					setContextCopyCutPasteButton(mTreeFilelistAdapter);
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

		mCcMenu.addMenuItem(mContext.getString(R.string.msgs_main_local_file_ccmenu_extract)+"("+sel_list+")",R.drawable.context_button_extract)
			.setOnClickListener(new CustomContextMenuOnClickListener() {
			@Override
			public void onClick(CharSequence menuTitle) {
				extractDlg(tfa);
			}
		});
		if (isCopyCutDestValid(mCurrentFilePath, "")) {
			mCcMenu.addMenuItem(mContext.getString(R.string.msgs_main_local_file_ccmenu_paste),R.drawable.context_button_paste)
		  		.setOnClickListener(new CustomContextMenuOnClickListener() {
				@Override
				public void onClick(CharSequence menuTitle) {
					pasteItem();
				}
		  	});
		}
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
//		if (mGp.copyCutList.size()>0 &&
//				isCopyCutDestValid(mZipFileSpinner.getSelectedItem().toString(),mZipFileCurrentDirectory.getText().toString().substring(1))) {
//			mCcMenu.addMenuItem(mContext.getString(R.string.msgs_main_local_file_ccmenu_paste),R.drawable.context_button_paste)
//		  		.setOnClickListener(new CustomContextMenuOnClickListener() {
//				@Override
//				public void onClick(CharSequence menuTitle) {
//					if (mGp.copyCutModeIsCut) confirmMove();
//					else confirmCopy();
//				}
//		  	});
//		}

		if (sel_count==1) {
			if (!tfi.isDirectory()) {
				mCcMenu.addMenuItem(mContext.getString(R.string.msgs_main_local_file_ccmenu_force_zip)+"("+(n_tfl.get(0)).getName()+")",R.drawable.context_button_archive)
			  		.setOnClickListener(new CustomContextMenuOnClickListener() {
					@Override
					public void onClick(CharSequence menuTitle) {
						invokeBrowser(tfi.isZipEncrypted(), tfi, tfi.getPath(), tfi.getName(), MIME_TYPE_ZIP);
					}
			  	});
				mCcMenu.addMenuItem(mContext.getString(R.string.msgs_main_local_file_ccmenu_force_text)+"("+(n_tfl.get(0)).getName()+")", R.drawable.cc_sheet)
		  			.setOnClickListener(new CustomContextMenuOnClickListener() {
					@Override
					public void onClick(CharSequence menuTitle) {
						invokeBrowser(tfi.isZipEncrypted(), tfi, tfi.getPath(), tfi.getName(), MIME_TYPE_TEXT);
					}
		  		});
			}
		}
		mCcMenu.createMenu();
	};

	private static InputStream buildBzip2InputStream(CustomZipFile zf, FileHeader fh, CommonUtilities cu) throws Exception {
        if (cu.getSettingLogLevel()>=2) {
            InputStream wis = (InputStream) zf.getInputStream(fh);
            byte[] buff=new byte[100];
            int rc=wis.read(buff);
            cu.addDebugMsg(2,"I","BZIP2 Compressed data (100 bytes from the beginning):\n"+StringUtil.getDumpFormatHexString(buff, 0, rc));
            wis.close();
        }

        final InputStream zis = (InputStream) zf.getInputStream(fh);
        BufferedInputStream bis = new BufferedInputStream(zis, IO_AREA_SIZE * 8);
        return new BZip2CompressorInputStream(bis);
    }

    private static InputStream buildDEFLATE64InputStream(CustomZipFile zf, FileHeader fh, CommonUtilities cu) throws Exception {
        if (cu.getSettingLogLevel()>=2) {
            InputStream wis = (InputStream) zf.getInputStream(fh);
            byte[] buff=new byte[100];
            int rc=wis.read(buff);
            cu.addDebugMsg(2,"I","DEFLATE64 Compressed data (100 bytes from the beginning):\n"+StringUtil.getDumpFormatHexString(buff, 0, rc));
            wis.close();
        }

        final ZipInputStream zis = zf.getInputStream(fh);
        BufferedInputStream bis = new BufferedInputStream(zis, IO_AREA_SIZE * 8);
        return new Deflate64CompressorInputStream(zis);
    }

    private static InputStream buildLZMAInputStream(CustomZipFile zf, FileHeader fh, CommonUtilities cu) throws Exception {
        if (cu.getSettingLogLevel()>=2) {
            InputStream wis = (InputStream) zf.getInputStream(fh);
            byte[] buff=new byte[100];
            int rc=wis.read(buff);
            cu.addDebugMsg(2,"I","LZMA Compressed data (100 bytes from the beginning):\n"+StringUtil.getDumpFormatHexString(buff, 0, rc));
            wis.close();
        }

        InputStream tis = (InputStream) zf.getInputStream(fh);
        BufferedInputStream bis = new BufferedInputStream(tis, IO_AREA_SIZE * 8);
        int rc=0;
        byte[] buff=new byte[100];
        // Read Header
        byte[] lzma_ver=new byte[2];
        rc=bis.read(lzma_ver);
        rc=bis.read(buff, 0, 2);
        int lzma_prop_size=getIntFromLittleEndian(buff, 0, rc);

        // Read prop byte
        byte props=(byte)bis.read();

        // Read dictionary size is an unsigned 32-bit little endian integer.
        rc=bis.read(buff, 0, 4);
        int dict_size = getIntFromLittleEndian(buff, 0, rc);
        if (cu.getSettingLogLevel()>=2)
            cu.addDebugMsg(2,"I", "lzma_ver=0x"+StringUtil.getHexString(lzma_ver, 0, 2)+", lzma_prop_size="+lzma_prop_size+
                    ", propCode="+String.format("0x%h", props)+", uncomp_size="+fh.getUncompressedSize()+", dict_size="+dict_size);

        return new LZMAInputStream(bis, fh.getUncompressedSize(), props, dict_size);
    }

    public static InputStream getZipInputStream(ThreadCtrl tc, CustomZipFile zf, FileHeader fh, CommonUtilities cu) throws Exception {
        InputStream is=null;
        CompressionMethod cm=getCompressionMethod(fh);
        if (!isSupportedCompressionMethod(fh)) {
            throw new ZipException("Unsupported compression method. code="+getCompressionMethodName(fh));
        } else {
            if (cm==CompressionMethod.BZIP2) {
                is=buildBzip2InputStream(zf, fh, cu);
            } else if (cm==CompressionMethod.LZMA) {
                is=buildLZMAInputStream(zf, fh, cu);
            } else if (cm==CompressionMethod.DEFLATE64) {
                is=buildDEFLATE64InputStream(zf, fh, cu);
            } else {// STORE/DEFLATE/AE-x
                is=zf.getInputStream(fh);
            }
        }
        return is;
    }

    public boolean extractSpecificFile(ThreadCtrl tc, CustomZipFile zf, String zip_file_name,
                                        String dest_path, String dest_file_name, boolean scan_media) {
		boolean result=false;
		long b_time=System.currentTimeMillis();
		try {
			if (tc.isEnabled()) {
                FileHeader fh=zf.getFileHeader(zip_file_name);
                InputStream is=getZipInputStream(tc, zf, fh, mUtil);

				String w_path=dest_path.endsWith("/")?dest_path+dest_file_name:dest_path+"/"+dest_file_name;
				SafFile3 out_dir_sf=new SafFile3(mContext, dest_path);
				if (!out_dir_sf.exists()) out_dir_sf.mkdirs();
                SafFile3 out_file_sf=new SafFile3(mContext, w_path);
                out_file_sf.deleteIfExists();
                out_file_sf.createNewFile();
				OutputStream os=out_file_sf.getOutputStream();

				long fsz=fh.getUncompressedSize();
				long frc=0;
				byte[] buff=new byte[IO_AREA_SIZE];
				int rc=0;
				String msg_txt=mContext.getString(R.string.msgs_zip_extract_file_extracting);
				boolean prog_enable=fsz>(1024*1024);
				int prog_value=0, prev_prog_value=-1;
				while((rc=is.read(buff))>0) {
//				    mUtil.addDebugMsg(1,"I","size="+rc);
					if (!tc.isEnabled()) break;
					os.write(buff,0,rc);
					frc+=rc;
					prog_value=(int)((frc*100)/(fsz));
					if (prog_enable && prev_prog_value!=prog_value) {
					    prev_prog_value=prog_value;
					    putProgressMessage(String.format(msg_txt, zip_file_name, prog_value));
                    }
				}
				os.flush();
				os.close();
				is.close();

				if (!tc.isEnabled()) out_file_sf.delete();
			}
			result=true;
		} catch (Exception e) {
			mUtil.addLogMsg("I", e.getMessage());
			CommonUtilities.printStackTraceElement(mUtil, e.getStackTrace());
			tc.setThreadMessage(e.getMessage());
		}
		mUtil.addDebugMsg(1,"I",
				"extractSpecificFile result="+result+", zip file name="+zip_file_name+", dest="+dest_path+
                        ", dest file name="+dest_file_name+", elapsed="+(System.currentTimeMillis()-b_time));
		return result;
	};

	private static int getIntFromLittleEndian(byte[] buff, int offset, int length) {
        int dict_size = 0;
        for (int i = 0; i < length; ++i) {
            dict_size |= buff[i] << (8 * i);
        }
	    return dict_size;
    }

	private void invokeBrowser(boolean encrypted, final TreeFilelistItem tfli,
                               final String p_dir, final String f_name, String mime_type) {
		final String work_dir=mContext.getExternalCacheDirs()[0].getPath()+"/"+WORK_DIRECTORY;
//		String fid=CommonUtilities.getFileExtention(f_name);
		String w_mt=LocalFileManager.getMimeTypeFromFileExtention(mGp, f_name);
		final String mt=mime_type.equals("")?w_mt:mime_type;
        try {
            final CustomZipFile zf=createZipFile(mContext, mCurrentFilePath, mEncodingSelected);
            final String e_name=p_dir.equals("")?f_name:p_dir+"/"+f_name;
            NotifyEvent ntfy_pswd=new NotifyEvent(mContext);
            ntfy_pswd.setListener(new NotifyEventListener(){
                @Override
                public void positiveResponse(Context c, Object[] o) {
                    String pswd="";
                    if (o!=null && o[0]!=null) pswd=(String)o[0];
                    if (!pswd.equals("")) {
                        zf.setPassword(pswd);
                        mMainPassword=pswd;
                    }
                    final ThreadCtrl tc=new ThreadCtrl();
                    mDialogProgressSpinCancel.setEnabled(true);
                    mDialogProgressSpinCancel.setOnClickListener(new OnClickListener(){
                        @Override
                        public void onClick(View v) {
                            confirmCancel(tc,mDialogProgressSpinCancel);
                        }
                    });
                    setUiDisabled();
                    showDialogProgress();
                    Thread th=new Thread(){
                        @Override
                        public void run() {
                            putProgressMessage(String.format(mContext.getString(R.string.msgs_zip_specific_extract_file_extracting),f_name));
                            File ef=new File(work_dir+"/"+f_name);
                            boolean extract_rc=true;
                            if (ef.exists()) {
                                if (ef.lastModified()!=tfli.getLastModified() || ef.length()!=tfli.getLength()) {
                                    extract_rc=extractSpecificFile(tc, zf, e_name, work_dir, f_name, false);
                                    ef.setLastModified(tfli.getLastModified());
                                }
                            } else {
                                extract_rc=extractSpecificFile(tc, zf, e_name, work_dir, f_name, false);
                                ef.setLastModified(tfli.getLastModified());
                            }
                            final boolean rc=extract_rc;
                            mUiHandler.post(new Runnable(){
                                @Override
                                public void run() {
                                    setUiEnabled();
                                    if (rc && tc.isEnabled()) {
                                        SafFile3 sf=null;
                                        try {
                                            String fp=(work_dir+"/"+f_name).replaceAll("//","/");
                                            sf=new SafFile3(mContext, fp);
                                            Intent intent = new Intent(android.content.Intent.ACTION_VIEW);
                                            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION |Intent.FLAG_GRANT_WRITE_URI_PERMISSION);// | Intent.FLAG_ACTIVITY_NEW_TASK);
                                            Uri uri=null;
                                            if (sf.isSafFile()) {
                                                uri=sf.getUri();
                                            } else {
                                                uri= FileProvider.getUriForFile(mContext, PACKAGE_NAME+".provider", new File(fp));
                                            }
                                            if (mt==null) intent.setDataAndType(uri, "*/*");
                                            else intent.setDataAndType(uri, mt);
                                            mActivity.startActivity(intent);
                                        } catch(ActivityNotFoundException e) {
                                            mCommonDlg.showCommonDialog(false,"E",
                                                    String.format(mContext.getString(R.string.msgs_zip_specific_extract_file_viewer_not_found),f_name,mt),"",null);
                                            if (sf!=null) sf.deleteIfExists();
                                        }
                                    } else {
                                        if (tc.isEnabled())
                                            mCommonDlg.showCommonDialog(false, "E", mContext.getString(R.string.msgs_zip_extract_file_was_failed),
                                                    f_name+"\n"+tc.getThreadMessage(), null);
                                    }
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
            if (encrypted) {
                FileHeader fh=zf.getFileHeader(e_name);
                if (isSupportedCompressionMethod(fh)) getZipPasswordDlg(mActivity, mGp, mMainPassword, zf, fh, ntfy_pswd, false);
                else {
                    mCommonDlg.showCommonDialog(false, "E", "Extract error", "Unsupported compression method. code="+
                            getCompressionMethodName(fh), null);
                }
            } else {
                ntfy_pswd.notifyToListener(true, null);
            }
        } catch (Exception e) {
            mUtil.addLogMsg("I", e.getMessage());
            CommonUtilities.printStackTraceElement(mUtil, e.getStackTrace());
        }
//		if (mt != null) {
//		} else {
//			mCommonDlg.showCommonDialog(false,"E",
//					String.format(mContext.getString(R.string.msgs_zip_specific_extract_mime_type_not_found),f_name),"",null);
//		}

	};

	private static boolean isSupportedCompressionMethod(FileHeader fh) {
	    boolean result=false;
        CompressionMethod cm=getCompressionMethod(fh);

        if (cm==CompressionMethod.STORE || cm==CompressionMethod.DEFLATE || cm==CompressionMethod.AES_INTERNAL_ONLY ||
                cm==CompressionMethod.BZIP2 || cm==CompressionMethod.LZMA
                || cm==CompressionMethod.DEFLATE64
                ) {
	        result=true;
        }

	    return result;
    }

    private static CompressionMethod getCompressionMethod(FileHeader fh) {
        CompressionMethod cm=fh.getCompressionMethod();
        if (fh.getCompressionMethod()==CompressionMethod.AES_INTERNAL_ONLY) cm=fh.getAesExtraDataRecord().getCompressionMethod();
	    return cm;
    }

    public static String getCompressionMethodName(FileHeader fh) {
	    CompressionMethod cm=getCompressionMethod(fh);
        return getCompressionMethodName(cm.getCode());
    }

    public static String getCompressionMethodName(int code) {
        String method_name="Unknown("+String.valueOf(code)+")";
        if (code==CompressionMethod.STORE.getCode()) method_name="STORE";
        else if (code==CompressionMethod.COMP_FACTOR1.getCode()) method_name="REDUCE1";
        else if (code==CompressionMethod.COMP_FACTOR2.getCode()) method_name="REDUCE2";
        else if (code==CompressionMethod.COMP_FACTOR3.getCode()) method_name="REDUCE3";
        else if (code==CompressionMethod.COMP_FACTOR4.getCode()) method_name="REDUCE4";
        else if (code==CompressionMethod.DEFLATE.getCode()) method_name="DEFLATE";
        else if (code==CompressionMethod.DEFLATE64.getCode()) method_name="DEFLATE64";
        else if (code==CompressionMethod.AES_INTERNAL_ONLY.getCode()) method_name="AE-x";
        else if (code==CompressionMethod.BZIP2.getCode()) method_name="BZIP2";
        else if (code==CompressionMethod.IBM_CMPSC.getCode()) method_name="IBM_CMPSC";
        else if (code==CompressionMethod.IBM_LZ77.getCode()) method_name="IBM_LZ77";
        else if (code==CompressionMethod.IBM_TERE.getCode()) method_name="IBM_TERSE";
        else if (code==CompressionMethod.JPEG.getCode()) method_name="JPEG";
        else if (code==CompressionMethod.WAVPACK.getCode()) method_name="WavPack";
        else if (code==CompressionMethod.LZMA.getCode()) method_name="LZMA";
        else if (code==CompressionMethod.PKWARE_IMPLODING.getCode()) method_name="PKWARE_IMPLOD";
        else if (code==CompressionMethod.IMPLOD.getCode()) method_name="IMPLOD";
        else if (code==CompressionMethod.PPMD.getCode()) method_name="PPMD";
        else if (code==CompressionMethod.SHRUNK.getCode()) method_name="SHRUNK";
        return method_name;
    }

    private void showToast(Activity a, String msg) {
        mUiHandler.post(new Runnable() {
            @Override
            public void run() {
                CommonDialog.showToastLong(a, msg);
            }
        });
    }

}
