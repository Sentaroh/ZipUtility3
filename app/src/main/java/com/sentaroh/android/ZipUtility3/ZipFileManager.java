package com.sentaroh.android.ZipUtility3;

/*
The MIT License (MIT)
Copyright (c) 2019 Sentaroh

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
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextWatcher;
import android.text.method.PasswordTransformationMethod;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.CompoundButton;
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

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
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
import com.sentaroh.android.Utilities3.Zip.ZipFileListItem;
import com.sentaroh.android.Utilities3.Zip.ZipUtil;


import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.io.inputstream.ZipInputStream;
import net.lingala.zip4j.model.FileHeader;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.model.enums.AesKeyStrength;
import net.lingala.zip4j.model.enums.CompressionLevel;
import net.lingala.zip4j.model.enums.CompressionMethod;
import net.lingala.zip4j.model.enums.EncryptionMethod;

import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.apache.commons.compress.compressors.deflate64.Deflate64CompressorInputStream;
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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Locale;
import java.util.regex.Pattern;

import static com.sentaroh.android.ZipUtility3.Constants.*;

public class ZipFileManager {

	private GlobalParameters mGp=null;
	
	private FragmentManager mFragmentManager=null;

	private ActivityMain mActivity=null;
	private String mLastMsgText="";

	private ArrayList<ZipFileListItem> mZipFileList=null;
	private ListView mTreeFilelistView=null;
	private CustomTreeFilelistAdapter mTreeFilelistAdapter=null;
	
	private Handler mUiHandler=null;
	public String mCurrentFilePath="";
	private long mCurrentFileLastModified=0;
	private long mCurrentFileLength=0;
	private String mMainPassword=null;

	private boolean mCurretnFileIsReadOnly =false;

    private ArrayList<ZipFileViewerItem> zipFileViewerList=new ArrayList<ZipFileViewerItem>();
    private Spinner mZipFileSpinner=null;

    private Button mFileListUp, mFileListTop;
    private ImageButton mFileInfoClose;
	private NonWordwrapTextView mCurrentDirectory;
	private TextView mFileEmpty, mFileInfoText;
	private LinearLayout mFileInfoView=null;
	
	private CommonUtilities mUtil=null;
	
	private LinearLayout mMainView=null;

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
        mUiHandler=new Handler();
        mFragmentManager=fm;
        mUtil=new CommonUtilities(mActivity, "ZipFolder", gp, fm);

        mEncodingDesired=mActivity.getString(R.string.msgs_zip_parm_zip_encoding_auto);

        mMainView=mv;
        initViewWidget();

		mTreeFilelistAdapter=new CustomTreeFilelistAdapter(mActivity, false, true);
        mTreeFilelistAdapter.setSortKey(mGp.zipSortKey);
        if (mGp.zipSortOrderAsc) mTreeFilelistAdapter.setSortAscendant();
        else mTreeFilelistAdapter.setSortDescendant();
        mTreeFilelistView.setAdapter(mTreeFilelistAdapter);

        hideTreeFileListView();
	};

    public void notifyTreeFileListAdapter() {
        if (mTreeFilelistAdapter!=null) mTreeFilelistAdapter.notifyDataSetChanged();
    }

    public void reInitView() {
        ArrayList<TreeFilelistItem> fl=mTreeFilelistAdapter.getDataList();
        int v_pos_fv=0, v_pos_top=0;
        v_pos_fv=mTreeFilelistView.getFirstVisiblePosition();
        if (mTreeFilelistView.getChildAt(0)!=null) v_pos_top=mTreeFilelistView.getChildAt(0).getTop();

        mTreeFilelistAdapter=new CustomTreeFilelistAdapter(mActivity, false, true);
        mTreeFilelistAdapter.setSortKey(mGp.zipSortKey);
        if (mGp.zipSortOrderAsc) mTreeFilelistAdapter.setSortAscendant();
        else mTreeFilelistAdapter.setSortDescendant();

        mTreeFilelistAdapter.setDataList(fl);
        mTreeFilelistAdapter.setZipArchiveFileName(mCurrentFilePath);
        mTreeFilelistView.setAdapter(mTreeFilelistAdapter);
        mTreeFilelistView.setSelectionFromTop(v_pos_fv, v_pos_top);
        mTreeFilelistAdapter.notifyDataSetChanged();
    }

    public boolean isFileListSortAscendant() {
		if (mTreeFilelistAdapter!=null) return mTreeFilelistAdapter.isSortAscendant();
		else return true;
	};

	private void initViewWidget() {
        mUtil.addDebugMsg(1, "I", CommonUtilities.getExecutedMethodName()+" entered");
		mContextButton=(LinearLayout)mMainView.findViewById(R.id.context_view_zip_file);
		
		mZipFileSpinner=(Spinner)mMainView.findViewById(R.id.zip_file_zip_file_spinner);
		CommonUtilities.setSpinnerBackground(mActivity, mZipFileSpinner, mGp.themeIsLight);
		ZipFileSelectorAdapter adapter=new ZipFileSelectorAdapter(mActivity, android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(R.layout.spinner_dropdown_single_choice);
        mZipFileSpinner.setPrompt(mActivity.getString(R.string.msgs_zip_zip_select_file));
//		mZipFileSpinner.setPrompt(mActivity.getString(R.string.msgs_main_sync_profile_dlg_sync_folder_type_prompt));
		mZipFileSpinner.setAdapter(adapter);
		
        mTreeFilelistView=(ListView)mMainView.findViewById(R.id.zip_file_list);
        mFileEmpty=(TextView)mMainView.findViewById(R.id.zip_file_empty);
        mFileEmpty.setVisibility(TextView.GONE);
        mTreeFilelistView.setVisibility(ListView.VISIBLE);
        
        mFileInfoView=(LinearLayout) mMainView.findViewById(R.id.zip_file_info_view);
        mFileInfoText =(TextView)mMainView.findViewById(R.id.zip_file_info_text);
        mFileInfoClose=(ImageButton)mMainView.findViewById(R.id.zip_file_info_close_btn);
//        mFileInfoClose.setVisibility(Button.GONE);

        mFileListUp=(Button)mMainView.findViewById(R.id.zip_file_up_btn);
        if (mGp.themeIsLight) mFileListUp.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_16_go_up_dark, 0, 0, 0);
        else mFileListUp.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_16_go_up_light, 0, 0, 0);
        mFileListTop=(Button)mMainView.findViewById(R.id.zip_file_top_btn);
        if (mGp.themeIsLight) mFileListTop.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_16_go_top_dark, 0, 0, 0);
        else mFileListTop.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_16_go_top_light, 0, 0, 0);

        mCurrentDirectory=(NonWordwrapTextView)mMainView.findViewById(R.id.zip_file_filepath);
        mCurrentDirectory.setWordWrapEnabled(false);
//        mCurrentDirectory.setTextColor(mGp.themeColorList.text_color_primary);

    	mContextButtonCopy=(ImageButton)mMainView.findViewById(R.id.context_button_copy);
    	mContextButtonCut=(ImageButton)mMainView.findViewById(R.id.context_button_cut);
    	mContextButtonPaste=(ImageButton)mMainView.findViewById(R.id.context_button_paste);
    	mContextButtonExtract=(ImageButton)mMainView.findViewById(R.id.context_button_extract);
    	mContextButtonOpen=(ImageButton)mMainView.findViewById(R.id.context_button_open);
    	mContextButtonNew=(ImageButton)mMainView.findViewById(R.id.context_button_add);
    	mContextButtonDelete=(ImageButton)mMainView.findViewById(R.id.context_button_delete);
        mContextButtonSelectAll=(ImageButton)mMainView.findViewById(R.id.context_button_select_all);
        mContextButtonUnselectAll=(ImageButton)mMainView.findViewById(R.id.context_button_unselect_all);
        
    	mContextButtonCopyView=(LinearLayout)mMainView.findViewById(R.id.context_button_copy_view);
    	mContextButtonCutView=(LinearLayout)mMainView.findViewById(R.id.context_button_cut_view);
    	mContextButtonPasteView=(LinearLayout)mMainView.findViewById(R.id.context_button_paste_view);
    	mContextButtonExtractView=(LinearLayout)mMainView.findViewById(R.id.context_button_extract_view);
    	mContextButtonOpenView=(LinearLayout)mMainView.findViewById(R.id.context_button_open_view);
    	mContextButtonNewView=(LinearLayout)mMainView.findViewById(R.id.context_button_add_view);
    	mContextButtonDeleteView=(LinearLayout)mMainView.findViewById(R.id.context_button_delete_view);
        mContextButtonSelectAllView=(LinearLayout)mMainView.findViewById(R.id.context_button_select_all_view);
        mContextButtonUnselectAllView=(LinearLayout)mMainView.findViewById(R.id.context_button_unselect_all_view);

//        mGp.zipCopyCutView=(LinearLayout) mMainView.findViewById(R.id.zip_file_copy_cut_view);
        mGp.zipCopyCutItemClear=(Button)mMainView.findViewById(R.id.zip_file_copy_cut_clear_btn);
        CommonDialog.setViewEnabled(mActivity, mGp.zipCopyCutItemClear, false);
//        mGp.zipCopyCutItemMode =(TextView)mMainView.findViewById(R.id.zip_file_copy_cut_mode);
//        mGp.zipCopyCutItemFrom=(TextView)mMainView.findViewById(R.id.zip_file_copy_cut_from);
        mGp.zipCopyCutItemInfo=(Button)mMainView.findViewById(R.id.zip_file_copy_cut_info_btn);
        CommonDialog.setViewEnabled(mActivity, mGp.zipCopyCutItemInfo, false);
//
//        mGp.zipCopyCutView.setVisibility(LinearLayout.GONE);
//
        mGp.zipCopyCutItemClear.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                CommonDialog.showPopupMessageAsDownAnchorView(mActivity, mGp.zipCopyCutItemClear,
                        mActivity.getString(R.string.msgs_zip_button_header_clear_copy_item), 2);
                return true;
            }
        });
        mGp.zipCopyCutItemClear.setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View v) {
                clearCopyCutItem(true);
            }
        });
        mGp.zipCopyCutItemInfo.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                CommonDialog.showPopupMessageAsDownAnchorView(mActivity, mGp.zipCopyCutItemInfo,
                        mActivity.getString(R.string.msgs_zip_button_header_copy_item_list), 2);
                return true;
            }
        });
        mGp.zipCopyCutItemInfo.setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View v) {
                mActivity.showCopyCutItemList();
            }
        });

        mFileInfoClose.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                String fp=(String)mZipFileSpinner.getSelectedItem();
                ((ZipFileSelectorAdapter)mZipFileSpinner.getAdapter()).remove(fp);

                ZipFileViewerItem remove_item=null;
                for(ZipFileViewerItem item:zipFileViewerList) {
                    if (item.file_path.equals(fp)) {
                        remove_item=item;
                        break;
                    }
                }
                if (remove_item!=null) zipFileViewerList.remove(remove_item);

                ((ZipFileSelectorAdapter)mZipFileSpinner.getAdapter()).notifyDataSetChanged();

                mUtil.addDebugMsg(1, "I", "ZIP file closed. fp="+fp);

                mCurrentFilePath="";

                if (zipFileViewerList.size()!=0) {
                    showZipFile(zipFileViewerList.get(0).read_only_file, new SafFile3(mActivity, zipFileViewerList.get(0).file_path));
                    if (mGp.copyCutFrom.equals(GlobalParameters.COPY_CUT_FROM_ZIP)) {
                        boolean found=false;
                        for(ZipFileViewerItem zf:zipFileViewerList) {
                            if (zf.file_path.equals(mGp.copyCutFilePath)) {
                                found=true;
                                break;
                            }
                        }
                        if (!found) {
                            mActivity.clearCopyCutItem();
                        }
                    }
                } else {
                    hideTreeFileListView();
                    if (mGp.copyCutFrom.equals(GlobalParameters.COPY_CUT_FROM_ZIP)) {
                        mActivity.clearCopyCutItem();
                    }
                }
                mActivity.refreshOptionMenu();
            }
        });

        setContextButtonListener();
	};

    private void clearCopyCutItem(final boolean toast) {
        mUiHandler.post(new Runnable(){
            @Override
            public void run() {
                CommonDialog.setViewEnabled(mActivity, mGp.localCopyCutItemInfo, false);
                CommonDialog.setViewEnabled(mActivity, mGp.localCopyCutItemClear, false);
                mActivity.clearCopyCutItem(toast);
                mContextButtonPasteView.setVisibility(ImageButton.INVISIBLE);
            }
        });
    }


    public void changeZipFileNameEncoding() {
        Dialog dialog=new Dialog(mActivity, mGp.applicationTheme);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.zip_file_name_encoding_dlg);

        final LinearLayout title_view = (LinearLayout) dialog.findViewById(R.id.zip_file_name_encoding_dlg_title_view);
        final TextView title = (TextView) dialog.findViewById(R.id.zip_file_name_encoding_dlg_title);
        title_view.setBackgroundColor(mGp.themeColorList.title_background_color);
        title.setTextColor(mGp.themeColorList.title_text_color);

        final ListView lv_encoding_list=(ListView)dialog.findViewById(R.id.zip_file_name_encoding_dlg_encoding_list);
        ArrayList<EncodeListItem> enc_list=new ArrayList<EncodeListItem>();
        boolean checked=false;
        int sel_pos=0;
        if (mEncodingDesired.equals(mActivity.getString(R.string.msgs_zip_parm_zip_encoding_auto))) checked=true;
        enc_list.add(new EncodeListItem(checked, mActivity.getString(R.string.msgs_zip_parm_zip_encoding_auto)));
        for(String item:ENCODING_NAME_LIST) {
            if (!checked && mEncodingDesired.equals(item)) {
                enc_list.add(new EncodeListItem(true, item));
                sel_pos=enc_list.size()-1;
            } else {
                enc_list.add(new EncodeListItem(item));
            }
        }
        EncodeSelectorAdapter adapter=new EncodeSelectorAdapter(mActivity, R.layout.zip_file_name_encoding_list_item, enc_list);
        lv_encoding_list.setAdapter(adapter);
        lv_encoding_list.setSelection(sel_pos);

        final Button dlg_ok=(Button)dialog.findViewById(R.id.zip_file_name_encoding_dlg_ok_btn);
        final Button dlg_cancel=(Button)dialog.findViewById(R.id.zip_file_name_encoding_dlg_cancel_btn);

        NotifyEvent ntfy_click=new NotifyEvent(mActivity);
        ntfy_click.setListener(new NotifyEventListener() {
            @Override
            public void positiveResponse(Context context, Object[] objects) {
                String sel_name=(String)objects[0];
                if (!mEncodingDesired.equals(sel_name)) CommonDialog.setViewEnabled(mActivity, dlg_ok, true);
                else CommonDialog.setViewEnabled(mActivity, dlg_ok, false);
            }

            @Override
            public void negativeResponse(Context context, Object[] objects) {}
        });
        adapter.setClickListener(ntfy_click);

        CommonDialog.setViewEnabled(mActivity, dlg_ok, false);
        dlg_ok.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                for(EncodeListItem item:enc_list) {
                    if (item.isChecked) {
                        mEncodingDesired=item.encode_name;
                        break;
                    }
                }
                refreshFileList(true);
                dialog.dismiss();
            }
        });

        dlg_cancel.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private class EncodeListItem {
        public boolean isChecked=false;
        public String encode_name=null;
        public EncodeListItem(boolean checked, String name) {
            isChecked=checked;
            encode_name=name;
        }
        public EncodeListItem(String name) {
            encode_name=name;
        }
    }

    private class EncodeSelectorAdapter extends ArrayAdapter<EncodeListItem> {
        private ArrayList<EncodeListItem> encode_list=null;
        private int mLayoutId=0;
        private Context mActivity=null;
        private Drawable mDefaultBackGroundColor=null;
        private ColorStateList mDefaultTextColor=null;

        public EncodeSelectorAdapter(Context context, int id, ArrayList<EncodeListItem> objects) {
            super(context, id, objects);
            mLayoutId=id;
            encode_list=objects;
            mActivity=context;
        };

        private NotifyEvent mNotifyClickListener=null;
        public void setClickListener(NotifyEvent ntfy){
            mNotifyClickListener=ntfy;
        }

        @Override
        final public View getView(final int position, View convertView, final ViewGroup parent) {
            final ViewHolder holder;
            final EncodeListItem item = getItem(position);
            View v = convertView;
            if (v == null) {
                LayoutInflater vi = (LayoutInflater)mActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = vi.inflate(mLayoutId, null);
                holder=new ViewHolder();

                holder.ll_entry=(LinearLayout)v.findViewById(R.id.zip_file_name_encoding_list_item_entry_view);
                holder.tv_name=(TextView)v.findViewById(R.id.zip_file_name_encoding_list_item_entry_name);
                holder.rb_checked=(RadioButton)v.findViewById(R.id.zip_file_name_encoding_list_item_entry_rb);

                if (mDefaultTextColor==null) mDefaultTextColor=holder.tv_name.getTextColors();

                if (mDefaultBackGroundColor==null) mDefaultBackGroundColor=holder.ll_entry.getBackground();

                v.setTag(holder);
            } else {
                holder= (ViewHolder)v.getTag();
            }
            if (item != null) {
                if (item.isChecked) {
                    if (mGp.themeIsLight) {
                        holder.ll_entry.setBackgroundColor(Color.CYAN);
                        holder.tv_name.setTextColor(Color.BLACK);
                    } else {
                        holder.tv_name.setTextColor(Color.BLACK);
                        holder.ll_entry.setBackgroundColor(Color.LTGRAY);
                    }
                } else {
                    holder.tv_name.setTextColor(mDefaultTextColor);
                    holder.ll_entry.setBackground(mDefaultBackGroundColor);
                }



                holder.tv_name.setText(item.encode_name);
                holder.tv_name.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        holder.rb_checked.performClick();
                    }
                });

                holder.rb_checked.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){
                    @Override
                    public void onCheckedChanged(CompoundButton arg0, boolean isChecked) {
                        if (isChecked) {
                            for (int i=0;i<encode_list.size();i++) {
                                encode_list.get(i).isChecked=false;
                            }
                            item.isChecked=isChecked;
                            if (mNotifyClickListener!=null) mNotifyClickListener.notifyToListener(true, new Object[]{item.encode_name});
                            notifyDataSetChanged();
                        }
                    }
                });
                holder.rb_checked.setChecked(item.isChecked);
            }
            return v;
        };

        private class ViewHolder {
            LinearLayout ll_entry;
            TextView tv_name;
            RadioButton rb_checked;
        };

    }

    private void saveZipFileViewerItem(String fp, Bundle bd) {
		mUtil.addDebugMsg(1, "I", CommonUtilities.getExecutedMethodName()+" entered, fp="+fp);
		for(ZipFileViewerItem fvi:zipFileViewerList) {
			if (fvi.file_path.equals(fp)) {
				fvi.saved_data=bd;
				mUtil.addDebugMsg(1, "I", CommonUtilities.getExecutedMethodName()+" file viewer item saved, fp="+fp);
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
		mUtil.addDebugMsg(1, "I", CommonUtilities.getExecutedMethodName()+" file viewer item added, pos="+adapter.getPosition(fp)+", fp="+fp);
	};
	
	private ZipFileViewerItem getZipFileViewerItem(String fp) {
		for(ZipFileViewerItem fvi:zipFileViewerList) {
			if (fvi.file_path.equals(fp)) {
				mUtil.addDebugMsg(1, "I", CommonUtilities.getExecutedMethodName()+" found, fp="+fp);
				return fvi;
			}
		}
		mUtil.addDebugMsg(1, "I", CommonUtilities.getExecutedMethodName()+" not found, fp="+fp);
		return null;
	};

	private void addZipFileViewerItem(boolean temp_file, String fp) {
        if (getZipFileViewerItem(fp)==null) {
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
            mUtil.addDebugMsg(1, "I", CommonUtilities.getExecutedMethodName()+" file viewer item added, fp="+fp);
        } else {
            mUtil.addDebugMsg(1, "I", CommonUtilities.getExecutedMethodName()+" file viewer item already exists, fp="+fp);
        }
	};
	
	private void refreshZipFileSpinner(SafFile3 fp) {
        mUtil.addDebugMsg(1, "I", CommonUtilities.getExecutedMethodName()+" entered");
        CustomSpinnerAdapter adapter=(CustomSpinnerAdapter)mZipFileSpinner.getAdapter();
		adapter.clear();
		for(ZipFileViewerItem zfi:zipFileViewerList) {
			adapter.add(zfi.file_path);
		}
		mZipFileSpinner.setSelection(adapter.getPosition(fp.getPath()), false);
		adapter.notifyDataSetChanged();
	};
	
	public void cleanZipFileManager() {
        mUtil.addDebugMsg(1, "I", CommonUtilities.getExecutedMethodName()+" entered");
		zipFileViewerList=null;
		mZipFileList=null;
		mTreeFilelistAdapter.setDataList(new ArrayList<TreeFilelistItem>());
        mTreeFilelistAdapter.setZipArchiveFileName("");
        mUiHandler=null;
        mActivity=null;
        mFragmentManager=null;
        mUtil=null;
	};

	public void saveZipFile() {
        NotifyEvent ntfy_select_dest=new NotifyEvent(mActivity);
        ntfy_select_dest.setListener(new NotifyEventListener() {
            @Override
            public void positiveResponse(Context context, Object[] objects) {
//                Uri uri=(Uri)objects[0];
//                SafFile3 out_filex=new SafFile3(mActivity, uri);
                String fp=(String)objects[1];
                SafFile3 out_file=new SafFile3(mActivity, fp);
                NotifyEvent ntfy_confirm=new NotifyEvent(mActivity);
                ntfy_confirm.setListener(new NotifyEventListener() {
                    @Override
                    public void positiveResponse(Context context, Object[] objects) {
                        SafFile3 in_file=new SafFile3(mActivity, mCurrentFilePath);
                        setUiDisabled();
                        showDialogProgress();
                        putProgressMessage(mActivity.getString(R.string.msgs_zip_write_zip_file_writing));
                        final ThreadCtrl tc=new ThreadCtrl();
                        mActivity.createDialogCancelListener(tc);
                        Thread th=new Thread() {
                            @Override
                            public void run() {
                                mUtil.addDebugMsg(1, "I", CommonUtilities.getExecutedMethodName()+" entered");
                                SafFile3 tmp=null;
                                try {
                                    boolean cache_available=false;
                                    if (out_file.getAppDirectoryCache()!=null) {
                                        cache_available=true;
                                        tmp=new SafFile3(mActivity, out_file.getAppDirectoryCache()+"/"+out_file.getName());
                                    } else {
                                        tmp=new SafFile3(mActivity, out_file.getPath()+".tmp");
                                    }
                                    tmp.deleteIfExists();
                                    tmp.createNewFile();
                                    InputStream is=in_file.getInputStream();
                                    OutputStream os=tmp.getOutputStream();
                                    copyFile(mActivity, tc, in_file.length(), is, os, new CallBackListener() {
                                        @Override
                                        public void onCallBack(Context context, boolean positive, Object[] o) {
                                            putProgressMessage(mActivity.getString(R.string.msgs_zip_write_zip_file_writing)+" "+(int)o[0]+"%");
                                        }
                                    });
                                    if (!isCancelled(true, tc)) {
                                        if (out_file.getPath().startsWith(mGp.primaryStoragePrefix)) {
//                                                File out_of_file=new File(out_file.getPath());
                                            if (out_file.exists()) out_file.delete();
                                            tmp.renameTo(out_file);
                                        } else {
                                            SafFile3 tmp_sf=new SafFile3(mActivity, tmp.getPath());
                                            if (out_file.exists()) out_file.delete();
                                            if (cache_available) tmp_sf.moveTo(out_file);
                                            else LocalFileManager.renameSafFile(tmp_sf, out_file);//tmp_sf.renameTo(out_file);
                                        }
                                    } else {
                                        tmp.delete();
                                        CommonDialog.showCommonDialog(mFragmentManager, false, "W",
                                                mActivity.getString(R.string.msgs_zip_write_zip_file_canelled), out_file.getPath(), null);
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
                                    if (tmp!=null) tmp.delete();
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
                    CommonDialog.showCommonDialog(mFragmentManager, true, "W",
                            mActivity.getString(R.string.msgs_zip_write_zip_file_confirm_override), out_file.getPath(), ntfy_confirm);
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
        String fn=mCurrentFilePath.substring(mCurrentFilePath.lastIndexOf("/")+1);
        CommonFileSelector2 fsdf=
                CommonFileSelector2.newInstance(scoped_storage_mode, true, false, CommonFileSelector2.DIALOG_SELECT_CATEGORY_FILE,
                        true, false, SafFile3.SAF_FILE_PRIMARY_UUID, "", fn, "Select destination file");
        fsdf.showDialog(false, mActivity.getSupportFragmentManager(), fsdf, ntfy_select_dest);

    }

    public static boolean copyFile(Context c, ThreadCtrl tc, long input_file_size, InputStream is, OutputStream os, CallBackListener cbl) throws IOException {
	    boolean result=false;
        int rc=0;
        long read_size=0;
        long tot_size=input_file_size;
        long progress=0, prev_progress=-1;
        byte[] buff=new byte[1024*1024*2];
        cbl.onCallBack(c, true, new Object[]{0});
        while((rc=is.read(buff))>0) {
            if (isCancelled(true, tc)) {
                is.close();
                os.close();
                return false;
            }
            os.write(buff, 0, rc);
            read_size+=rc;
            progress=(read_size*100)/tot_size;
            if (prev_progress!=progress) {
                prev_progress=progress;
                cbl.onCallBack(c, true, new Object[]{(int)progress});
            }
        }
        result=true;
        is.close();
        os.flush();
        os.close();

        return result;
    }

	public void showZipFile(boolean read_only, SafFile3 in_file) {
        mUtil.addDebugMsg(1, "I", CommonUtilities.getExecutedMethodName()+" entered");
		if (!isUiEnabled()) return;
		Bundle bd=new Bundle();
		if (in_file!=null) {
		    if (!read_only) {
                if (in_file.getUuid().equals(SafFile3.SAF_FILE_UNKNOWN_UUID)) {
                    mCurretnFileIsReadOnly =false;
                } else if (!in_file.getUuid().equals(SafFile3.SAF_FILE_PRIMARY_UUID)) {
                    if (!SafManager3.isUuidRegistered(mActivity, in_file.getUuid())) mCurretnFileIsReadOnly =true;
                }
            }
			String fid=CommonUtilities.getFileExtention(in_file.getName());
			if (fid.equals("gz") || fid.equals("tar") || fid.equals("tgz")) {
				CommonDialog.showCommonDialog(mFragmentManager, false, "W", mActivity.getString(R.string.msgs_zip_open_file_not_supported_file), "", null);
			} else {
				String s_fp=saveViewContents(bd);
				mUtil.addDebugMsg(1, "I", CommonUtilities.getExecutedMethodName()+" saved file path="+s_fp);
				mZipFileSpinner.setOnItemSelectedListener(null);
				if (!s_fp.equals("")) {
					saveZipFileViewerItem(s_fp, bd);
					ZipFileViewerItem fvi= getZipFileViewerItem(in_file.getPath());
					if (fvi!=null) {
					    mCurretnFileIsReadOnly=fvi.read_only_file;
						refreshZipFileSpinner(in_file);
						restoreViewContents(in_file.getPath(), fvi.saved_data);
                        refreshFileList(true);
					} else {
                        mCurretnFileIsReadOnly=read_only;
						addZipFileViewerItem(read_only, in_file.getPath());
						mCurrentFilePath=in_file.getPath();
						mCurrentDirectory.setText("/");
						refreshFileList(true);
						refreshZipFileSpinner(in_file);
					}
				} else {
				    final Handler hndl=new Handler();
				    final Dialog pd=CommonDialog.showProgressSpinIndicator(mActivity);
				    pd.show();
				    Thread th=new Thread() {
				        @Override
                        public void run() {
                            final String err_msg=ZipUtil.isZipFile(mActivity, in_file);
                            hndl.post(new Runnable(){
                                @Override
                                public void run() {
                                    mUiHandler.postDelayed(new Runnable(){
                                        @Override
                                        public void run() {
                                            pd.dismiss();
                                        }
                                    },500);
                                    if (err_msg==null) {
                                        if (!ZipUtil.isSplitArchiveFile(mActivity, in_file)) {
                                            mCurretnFileIsReadOnly=read_only;
                                            addZipFileViewerItem(read_only, in_file.getPath());
                                            mCurrentFilePath=in_file.getPath();
                                            mCurrentDirectory.setText("/");
                                            refreshFileList(true);
                                            refreshZipFileSpinner(in_file);
                                        } else {
                                            CommonDialog.showCommonDialog(mFragmentManager, false, "W", "Split ZIP file can not be used", "File="+in_file.getPath(), null);
                                        }
                                    } else {
                                        CommonDialog.showCommonDialog(mFragmentManager, false, "W", "Invalid ZIP file", "File="+in_file.getPath()+"\n"+err_msg, null);
                                    }
                                }
                            });
                        }
                    };
				    th.start();
				}
				mZipFileSpinner.setOnItemSelectedListener(new OnItemSelectedListener(){
					@Override
					public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        for(int i=0;i<((ZipFileSelectorAdapter)mZipFileSpinner.getAdapter()).getCount();i++) {
                            mUtil.addDebugMsg(1, "I", "selected fp="+((ZipFileSelectorAdapter)mZipFileSpinner.getAdapter()).getItem(i));
                        }

                        String n_fp=mZipFileSpinner.getSelectedItem().toString();
                        mUtil.addDebugMsg(1, "I", "selected new_fp="+n_fp);
                        ZipFileViewerItem fvi= getZipFileViewerItem(n_fp);
                        if (fvi!=null) {
                            showZipFile(fvi.read_only_file, new SafFile3(mActivity, n_fp));
                        } else {
                            showZipFile(false, new SafFile3(mActivity, n_fp));
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
        public boolean file_error_detected=false;
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
        mUtil.addDebugMsg(1, "I", CommonUtilities.getExecutedMethodName()+" entered");
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
			sv.curr_dir=mCurrentDirectory.getOriginalText().toString();
			sv.encoding_desired=mEncodingDesired;
			sv.encoding_selected=mEncodingSelected;
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
        mUtil.addDebugMsg(1, "I", CommonUtilities.getExecutedMethodName()+" entered");
		byte[] ba=bd.getByteArray(SAVE_VIEW_CONTENT_KEY);
		ByteArrayInputStream bis=new ByteArrayInputStream(ba);
		SavedViewData sv=new SavedViewData();
		sv.encoding_desired=mActivity.getString(R.string.msgs_zip_parm_zip_encoding_auto);
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
			String cdir=mCurrentDirectory.getOriginalText().toString();
			String target_dir="";
			if (cdir.length()>0) target_dir=cdir.substring(1);
			mZipFileList=sv.zfl;
			mTreeFilelistAdapter.setDataList(sv.tfl);
            mTreeFilelistAdapter.setZipArchiveFileName(mCurrentFilePath);
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

    private boolean mSearchListSortOrderAsc=true;
    private int mSearchListSortKey=CustomTreeFilelistAdapter.SORT_KEY_NAME;

    public boolean isZipFileLoaded() {
		return mZipFileList!=null?true:false;
	};
	
	public void sortFileList() {
        NotifyEvent ntfy=new NotifyEvent(mActivity);
        ntfy.setListener(new NotifyEventListener() {
            @Override
            public void positiveResponse(Context context, Object[] objects) {
                mGp.saveZipSortParameter(context, mTreeFilelistAdapter);
            }

            @Override
            public void negativeResponse(Context context, Object[] objects) {}
        });
        CommonUtilities.sortFileList(mActivity, mGp, mTreeFilelistAdapter, ntfy);
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
		ib_sort.setBackgroundColor(mGp.themeColorList.title_background_color);
		final CheckedTextView dlg_hidden = (CheckedTextView) dialog.findViewById(R.id.search_file_dlg_search_hidden_item);
		dlg_hidden.setVisibility(CheckedTextView.GONE);

		final CheckedTextView dlg_case_sensitive = (CheckedTextView) dialog.findViewById(R.id.search_file_dlg_search_case_sensitive);
		CommonUtilities.setCheckedTextView(dlg_case_sensitive);

//		final TextView dlg_msg = (TextView) dialog.findViewById(R.id.search_file_dlg_msg);
		final Button btn_search = (Button) dialog.findViewById(R.id.search_file_dlg_ok_btn);
		final Button btnCancel = (Button) dialog.findViewById(R.id.search_file_dlg_cancel_btn);
		final EditText et_search_key=(EditText) dialog.findViewById(R.id.search_file_dlg_search_key);
		final ListView lv_search_result=(ListView) dialog.findViewById(R.id.search_file_dlg_search_result);
        lv_search_result.setVisibility(ListView.GONE);
        final TextView searcgh_info = (TextView) dialog.findViewById(R.id.search_file_dlg_search_info);
        final TextView searcgh_description = (TextView) dialog.findViewById(R.id.search_file_dlg_search_description);

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
				long s_size=0;
				for(TreeFilelistItem tfi:mAdapterSearchFileList.getDataList()) s_size+=tfi.getLength();
				String msg=mActivity.getString(R.string.msgs_search_file_dlg_search_result);
				searcgh_info.setText(String.format(msg,mAdapterSearchFileList.getDataList().size(),s_size));
			}
		}

        if (mSearchListSortOrderAsc) mAdapterSearchFileList.setSortAscendant();
        else mAdapterSearchFileList.setSortDescendant();
        if (mSearchListSortKey==CustomTreeFilelistAdapter.SORT_KEY_NAME) mAdapterSearchFileList.setSortKeyName();
        else if (mSearchListSortKey==CustomTreeFilelistAdapter.SORT_KEY_SIZE) mAdapterSearchFileList.setSortKeySize();
        else if (mSearchListSortKey==CustomTreeFilelistAdapter.SORT_KEY_TIME) mAdapterSearchFileList.setSortKeyTime();

		CommonDialog.setDlgBoxSizeLimit(dialog, true);

        if (mAdapterSearchFileList.isSortAscendant()) ib_sort.setImageResource(R.drawable.ic_128_sort_asc_gray);
        else ib_sort.setImageResource(R.drawable.ic_128_sort_dsc_gray);
		ib_sort.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				final CustomTreeFilelistAdapter tfa=new CustomTreeFilelistAdapter(mActivity,false,false);
				NotifyEvent ntfy_sort=new NotifyEvent(mActivity);
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

                        if (tfa.isSortAscendant()) mSearchListSortOrderAsc=true;
                        else mSearchListSortOrderAsc=false;

                        if (tfa.getSortKey()==CustomTreeFilelistAdapter.SORT_KEY_NAME) mSearchListSortKey=CustomTreeFilelistAdapter.SORT_KEY_NAME;
                        else if (tfa.getSortKey()==CustomTreeFilelistAdapter.SORT_KEY_SIZE) mSearchListSortKey=CustomTreeFilelistAdapter.SORT_KEY_SIZE;
                        else if (tfa.getSortKey()==CustomTreeFilelistAdapter.SORT_KEY_TIME) mSearchListSortKey=CustomTreeFilelistAdapter.SORT_KEY_TIME;
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

		        final CustomContextMenu mCcMenu = new CustomContextMenu(mActivity.getResources(), mFragmentManager);

				mCcMenu.addMenuItem(mActivity.getString(R.string.msgs_main_local_file_ccmenu_top), R.drawable.context_button_top)
			  		.setOnClickListener(new CustomContextMenuOnClickListener() {
					@Override
					public void onClick(CharSequence menuTitle) {
						lv_search_result.setSelection(0);
					}
			  	});
				mCcMenu.addMenuItem(mActivity.getString(R.string.msgs_main_local_file_ccmenu_bottom), R.drawable.context_button_bottom)
			  		.setOnClickListener(new CustomContextMenuOnClickListener() {
					@Override
					public void onClick(CharSequence menuTitle) {
						lv_search_result.setSelection(lv_search_result.getCount());
					}
			  	});

				mCcMenu.addMenuItem(mActivity.getString(R.string.msgs_main_local_file_ccmenu_open_file)+"("+(n_tfl.get(0)).getName()+")")
			  		.setOnClickListener(new CustomContextMenuOnClickListener() {
					@Override
					public void onClick(CharSequence menuTitle) {
						invokeBrowser(n_tfli.isZipEncrypted(), n_tfli, n_tfli.getPath(), n_tfli.getName(), "");
						btnCancel.performClick();
					}
			  	});
				mCcMenu.addMenuItem(mActivity.getString(R.string.msgs_main_local_file_ccmenu_force_zip)+"("+(n_tfl.get(0)).getName()+")",R.drawable.ic_32_file_zip)
		  			.setOnClickListener(new CustomContextMenuOnClickListener() {
					@Override
					public void onClick(CharSequence menuTitle) {
						invokeBrowser(n_tfli.isZipEncrypted(), n_tfli, n_tfli.getPath(), n_tfli.getName(), MIME_TYPE_ZIP);
						btnCancel.performClick();
					}
		  		});
				mCcMenu.addMenuItem(mActivity.getString(R.string.msgs_main_local_file_ccmenu_force_text)+"("+(n_tfl.get(0)).getName()+")",R.drawable.cc_sheet)
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
				if (s.length()>0) CommonDialog.setButtonEnabled(mActivity, btn_search, true);
				else CommonDialog.setButtonEnabled(mActivity, btn_search, false);
			}
		});

		lv_search_result.setOnItemClickListener(new OnItemClickListener(){
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				TreeFilelistItem tfi=mAdapterSearchFileList.getItem(position);
//				openSppecificDirectory(tfi.getPath(), tfi.getName());
//				mSearchListPositionX=lv_search_result.getFirstVisiblePosition();
//				mSearchListPositionY=lv_search_result.getChildAt(0)==null?0:lv_search_result.getChildAt(0).getTop();
//				btnCancel.performClick();
                invokeBrowser(tfi.isZipEncrypted(), tfi, tfi.getPath(), tfi.getName(), "");
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
		btn_search.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
                searcgh_description.setVisibility(TextView.GONE);
                lv_search_result.setVisibility(ListView.VISIBLE);
                mFindKey=et_search_key.getText().toString();
				final ArrayList<TreeFilelistItem> s_tfl=new ArrayList<TreeFilelistItem>();
				int flags = 0;//Pattern.CASE_INSENSITIVE;// | Pattern..MULTILINE;
				if (!dlg_case_sensitive.isChecked()) flags= Pattern.CASE_INSENSITIVE;
				final Pattern s_key= Pattern.compile("(" + MiscUtil.convertRegExp(mFindKey) + ")", flags);
				final ThreadCtrl tc=new ThreadCtrl();
				final ProgressSpinDialogFragment psd=ProgressSpinDialogFragment.newInstance(
						mActivity.getString(R.string.msgs_search_file_dlg_searching), "",
						mActivity.getString(R.string.msgs_common_dialog_cancel),
						mActivity.getString(R.string.msgs_common_dialog_canceling));

				NotifyEvent ntfy=new NotifyEvent(mActivity);
				ntfy.setListener(new NotifyEventListener(){
					@Override
					public void positiveResponse(Context c, Object[] o) {}
					@Override
					public void negativeResponse(Context c, Object[] o) {
						tc.setDisabled();
						if (isCancelled(tc)) psd.dismissAllowingStateLoss();
					}
				});
				psd.showDialog(mFragmentManager, psd, ntfy,true);
				Thread th=new Thread(){
					@Override
					public void run() {
						buildFileListBySearchKey(tc, psd, s_tfl, s_key);
						psd.dismissAllowingStateLoss();
						if (isCancelled(tc)) {
							CommonDialog.showCommonDialog(mFragmentManager, false, "W",
									mActivity.getString(R.string.msgs_search_file_dlg_search_cancelled), "", null);
						} else {
//							CommonDialog.showCommonDialog(mFragmentManager, false, "W",
//									String.format(mActivity.getString(R.string.msgs_search_file_dlg_search_found), s_tfl.size()), "", null);
							mAdapterSearchFileList.setDataList(s_tfl);
							mSearchRootDir=mCurrentFilePath;
							mUiHandler.post(new Runnable(){
								@Override
								public void run() {
									long s_size=0;
									for(TreeFilelistItem tfi:mAdapterSearchFileList.getDataList()) s_size+=tfi.getLength();
									String msg=mActivity.getString(R.string.msgs_search_file_dlg_search_result);
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
		btnCancel.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
//				saveSearchResultList(mAdapterSearchFileList.getDataList());
				dialog.dismiss();
			}
		});

		dialog.show();
	};

	private void buildFileListBySearchKey(final ThreadCtrl tc, ProgressSpinDialogFragment psd,
                                          ArrayList<TreeFilelistItem> s_tfl, Pattern s_key) {
		if (isZipFileLoaded()) {
			int list_size=mZipFileList.size();
			int progress=0, proc_count=0;
			int prev_prog=0;
			for(ZipFileListItem zfli:mZipFileList) {
			    String zip_dir=zfli.getPath().equals("")?"/":"/"+zfli.getPath()+"/";
                String curr_dir=mCurrentDirectory.getOriginalText().toString().equals("/")?"/":mCurrentDirectory.getOriginalText().toString()+"/";
			    if (zip_dir.startsWith(curr_dir)) {
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
                                mActivity.getString(R.string.msgs_search_file_dlg_search_progress), progress));
                    }
                }
			}
		}
	};

	public void refreshFileList() {
		refreshFileList(false);
	};
	public void refreshFileList(boolean force) {
		if (!isUiEnabled()) return;
        mUtil.addDebugMsg(1, "I", CommonUtilities.getExecutedMethodName()+" entered, fp="+mCurrentFilePath);
        if (!mCurrentFilePath.equals("")) {
            ZipFileViewerItem zfv=getZipFileViewerItem(mCurrentFilePath);
			SafFile3 lf=new SafFile3(mActivity, mCurrentFilePath);
			if (!lf.exists()) {
			    if (!zfv.file_error_detected) {
                    CommonDialog.showCommonDialog(mFragmentManager, false, "W",
                            String.format(mActivity.getString(R.string.msgs_zip_zip_file_was_not_found),mCurrentFilePath), "", null);
                    zfv.file_error_detected=true;
                    mZipFileList=new ArrayList<ZipFileListItem>();
                    String cdir=mCurrentDirectory.getOriginalText().toString();
                    if (cdir.length()>0) cdir=cdir.substring(1);
                    refreshFileListView(cdir, mCurrentFilePath);
                    mFileEmpty.setText(mActivity.getString(R.string.msgs_zip_zip_file_was_not_found, mCurrentFilePath));
                    mContextButtonPasteView.setVisibility(LinearLayout.INVISIBLE);
                }
			} else {
			    String zf_err=ZipUtil.isZipFile(mActivity, lf);
			    if (zf_err!=null) {
                    zfv.file_error_detected=true;
                    mZipFileList=new ArrayList<ZipFileListItem>();
                    String cdir=mCurrentDirectory.getOriginalText().toString();
                    if (cdir.length()>0) cdir=cdir.substring(1);
                    refreshFileListView(cdir, mCurrentFilePath);
                    mFileEmpty.setText(zf_err);
                    mContextButtonPasteView.setVisibility(LinearLayout.INVISIBLE);
                    return;
                }
                zfv.file_error_detected=false;
                if (isZipFileChanged() || force) {
                    mUtil.addDebugMsg(1, "I", "refresh entered");
                    String cdir=mCurrentDirectory.getOriginalText().toString();
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
                        mTreeFilelistAdapter.setZipArchiveFileName(mCurrentFilePath);
                    }
                }
                setContextCopyCutPasteButton(mTreeFilelistAdapter);
            }
		}
	};

	private boolean isZipFileChanged() {
	    if (mCurrentFilePath.equals("")) return false;
	    boolean result=false;
		SafFile3 lf=new SafFile3(mActivity, mCurrentFilePath);
//		mUtil.addDebugMsg(1, "I", "File exists="+lf.exists()+", savedLastMod="+mCurrentFileLastModified+", savedLength="+mCurrentFileLength+
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
					String c_dir=mCurrentDirectory.getOriginalText().length()==0?"":mCurrentDirectory.getOriginalText().toString().substring(1);
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
        mUtil.addDebugMsg(1, "I", CommonUtilities.getExecutedMethodName()+" entered, fp="+fp);
		if (mZipFileList!=null && mZipFileList.size()>0) {
			mCurrentDirectory.setText("/"+target_dir);
			mCurrentDirectory.setVisibility(TextView.VISIBLE);
			setZipTreeFileListener();
			mTreeFilelistView.setVisibility(ListView.VISIBLE);
			mFileEmpty.setVisibility(TextView.GONE);
			mFileInfoClose.setVisibility(Button.VISIBLE);
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
            mFileInfoClose.setVisibility(Button.VISIBLE);
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
        mFileInfoView.setVisibility(TextView.VISIBLE);
        mGp.zipCopyCutItemClear.setVisibility(Button.VISIBLE);
        mGp.zipCopyCutItemInfo.setVisibility(Button.VISIBLE);

		SafFile3 lf=new SafFile3(mActivity, fp);
		String tfs=MiscUtil.convertFileSize(lf.length());
		int size=mZipFileList==null?0:mZipFileList.size();
		String info= String.format(mActivity.getString(R.string.msgs_zip_zip_file_info),tfs, size, mEncodingSelected);
        String read_only= mCurretnFileIsReadOnly ?mActivity.getString(R.string.msgs_zip_zip_file_info_temporary):"";
        if (mCurretnFileIsReadOnly) {
            SpannableStringBuilder sb=new SpannableStringBuilder(read_only+" "+info);
//        BackgroundColorSpan bg_span = new BackgroundColorSpan(mHighlightBackgrounColor);
            ForegroundColorSpan fg_span = new ForegroundColorSpan(mGp.themeColorList.text_color_error);
//        sb.setSpan(bg_span, mt.start(), mt.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            sb.setSpan(fg_span, 0, read_only.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            mFileInfoText.setText(sb);
        } else {
            mFileInfoText.setText(info);
        }
	};

	private void createFileList(final String fp, final NotifyEvent p_ntfy, final String target_dir) {
		mUtil.addDebugMsg(1, "I", "createFileList entered, fp="+fp+", target="+target_dir);
        setUiDisabled();
        final Dialog pd=CommonDialog.showProgressSpinIndicator(mActivity);
        final ThreadCtrl tc=new ThreadCtrl();
        pd.show();
        mFileEmpty.setText("");//.setVisibility(TextView.GONE);
        Thread th=new Thread() {
            @Override
            public void run() {
                SafFile3 lf=new SafFile3(mActivity, fp);
                if (!lf.exists()) {
                    mCurrentFileLastModified=0;
                    mCurrentFileLength=0;
                } else {
                    mCurrentFileLastModified=lf.lastModified();
                    mCurrentFileLength=lf.length();
                }
                mUtil.addDebugMsg(1, "I", "createFileList begin");
                final NotifyEvent ntfy_create_file_list=new NotifyEvent(mActivity);
                ntfy_create_file_list.setListener(new NotifyEventListener(){
                    @Override
                    public void positiveResponse(Context c, Object[] o) {
                        mUiHandler.post(new Runnable(){
                            @Override
                            public void run() {
                                if (mZipFileList!=null){// && mZipFileList.size()>0) {
                                    ArrayList<TreeFilelistItem> tfl=createTreeFileList(mZipFileList, target_dir);
                                    mTreeFilelistAdapter.setDataList(tfl);
                                    mTreeFilelistAdapter.setZipArchiveFileName(mCurrentFilePath);
                                }
                                mUtil.addDebugMsg(1, "I", "createFileList Tree file list adapetr created");
                                refreshFileListView(target_dir, fp);
                                setUiEnabled();
                                if (p_ntfy!=null) p_ntfy.notifyToListener(true, null);
                                mUtil.addDebugMsg(1, "I", "createFileList end");
                                pd.dismiss();
                            }
                        });
                    }
                    @Override
                    public void negativeResponse(Context c, Object[] o) {
                        mUiHandler.post(new Runnable(){
                            @Override
                            public void run() {
                                if (mFileInfoView.getVisibility()!= TextView.VISIBLE)
                                    mFileEmpty.setText(R.string.msgs_zip_folder_not_specified);
                                setUiEnabled();
                                mUtil.addDebugMsg(1, "I", "createFileList end");
                                pd.dismiss();
                            }
                        });
                    }
                });
                String detect_encoding=null;
                if (mEncodingDesired.equals(mActivity.getString(R.string.msgs_zip_parm_zip_encoding_auto))) {
                    try {
                        detect_encoding= ZipUtil.detectFileNameEncoding(mActivity, fp);
                    } catch(Exception e) {
    			        e.printStackTrace();
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
                    mZipFileList=ZipUtil.buildZipFileList(mActivity, fp, mEncodingSelected);
                } catch(Exception e) {
                    CommonDialog.showCommonDialog(mFragmentManager, false, "E", "ZIP file", "ZIP file list creation error, error="+e.getMessage(), null);
                    mZipFileList=null;
                    e.printStackTrace();
                }
                mUtil.addDebugMsg(1, "I", "createFileList Zip file list created");
                if (!isCancelled(tc)) {
                    ntfy_create_file_list.notifyToListener(true, null);
                } else {
                    ntfy_create_file_list.notifyToListener(false, null);
                }
                pd.dismiss();
            }
        };
        th.start();
	};

	private void hideTreeFileListView() {
        mTreeFilelistView.setVisibility(ListView.GONE);
        mFileEmpty.setVisibility(TextView.VISIBLE);
        mFileEmpty.setText(R.string.msgs_zip_folder_not_specified);
        mFileInfoClose.setVisibility(Button.GONE);
        mCurrentDirectory.setVisibility(TextView.GONE);
        mFileListUp.setVisibility(Button.GONE);
        mFileListUp.setEnabled(false);
        mFileListTop.setVisibility(Button.GONE);
        mZipFileSpinner.setVisibility(TextView.INVISIBLE);
        mGp.zipCopyCutItemInfo.setVisibility(Button.GONE);
        mGp.zipCopyCutItemClear.setVisibility(Button.GONE);

        mContextButtonCopyView.setVisibility(LinearLayout.INVISIBLE);
        mContextButtonCutView.setVisibility(LinearLayout.INVISIBLE);
        mContextButtonPasteView.setVisibility(LinearLayout.INVISIBLE);
        mContextButtonExtractView.setVisibility(LinearLayout.INVISIBLE);
        mContextButtonDeleteView.setVisibility(LinearLayout.INVISIBLE);
        mContextButtonSelectAllView.setVisibility(LinearLayout.INVISIBLE);
        mContextButtonUnselectAllView.setVisibility(LinearLayout.INVISIBLE);

        mFileInfoView.setVisibility(TextView.GONE);
    }

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
        ContextButtonUtil.setButtonLabelListener(mActivity, mContextButtonExtract,mActivity.getString(R.string.msgs_zip_cont_label_extract));

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
        ContextButtonUtil.setButtonLabelListener(mActivity, mContextButtonCopy, mActivity.getString(R.string.msgs_zip_cont_label_copy));

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
        ContextButtonUtil.setButtonLabelListener(mActivity, mContextButtonCut, mActivity.getString(R.string.msgs_zip_cont_label_cut));

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
        ContextButtonUtil.setButtonLabelListener(mActivity, mContextButtonPaste, mActivity.getString(R.string.msgs_zip_cont_label_paste));

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
        ContextButtonUtil.setButtonLabelListener(mActivity, mContextButtonOpen,mActivity.getString(R.string.msgs_zip_cont_label_open));

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
        ContextButtonUtil.setButtonLabelListener(mActivity, mContextButtonNew,mActivity.getString(R.string.msgs_zip_cont_label_new));

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
        ContextButtonUtil.setButtonLabelListener(mActivity, mContextButtonDelete,mActivity.getString(R.string.msgs_zip_cont_label_delete));

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
        ContextButtonUtil.setButtonLabelListener(mActivity, mContextButtonSelectAll,mActivity.getString(R.string.msgs_zip_cont_label_select_all));

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
        ContextButtonUtil.setButtonLabelListener(mActivity, mContextButtonUnselectAll,mActivity.getString(R.string.msgs_zip_cont_label_unselect_all));
	};

	private void copyItem(CustomTreeFilelistAdapter tfa) {
		if (tfa.isItemSelected()) {
			mGp.copyCutModeIsCut=false;
			mGp.copyCutFilePath=mCurrentFilePath;
			mGp.copyCutCurrentDirectory=mCurrentDirectory.getOriginalText().equals("/")?"":mCurrentDirectory.getOriginalText().toString().substring(1);
			mGp.copyCutEncoding=mEncodingSelected;
			mGp.copyCutFrom =GlobalParameters.COPY_CUT_FROM_ZIP;
			mGp.copyCutList.clear();
			for(TreeFilelistItem tfl:tfa.getDataList()) {
				if(tfl.isChecked()) {
					mGp.copyCutList.add(tfl);
                    tfl.setChecked(false);
				}
			}
			tfa.notifyDataSetChanged();
			mActivity.setCopyCutItemView();
			CommonDialog.setViewEnabled(mActivity, mGp.localCopyCutItemClear, true);
            CommonDialog.setViewEnabled(mActivity, mGp.localCopyCutItemInfo, true);
            mGp.localCopyCutItemInfo.setText(mActivity.getString(R.string.msgs_zip_cont_header_copy));
            mGp.zipCopyCutItemInfo.setText(mActivity.getString(R.string.msgs_zip_cont_header_copy));
		}
	};

	private void cutItem(CustomTreeFilelistAdapter tfa) {
		if (tfa.isItemSelected()) {
			mGp.copyCutModeIsCut=true;
			mGp.copyCutFilePath=mCurrentFilePath;
			mGp.copyCutCurrentDirectory=mCurrentDirectory.getOriginalText().equals("/")?"":mCurrentDirectory.getOriginalText().toString().substring(1);
			mGp.copyCutEncoding=mEncodingSelected;
			mGp.copyCutFrom =GlobalParameters.COPY_CUT_FROM_ZIP;
			mGp.copyCutList.clear();
			for(TreeFilelistItem tfl:tfa.getDataList()) {
				if(tfl.isChecked()) {
					mGp.copyCutList.add(tfl);
                    tfl.setChecked(false);
				}
			}
			tfa.notifyDataSetChanged();
            mActivity.setCopyCutItemView();
            CommonDialog.setViewEnabled(mActivity, mGp.localCopyCutItemClear, true);
            CommonDialog.setViewEnabled(mActivity, mGp.localCopyCutItemInfo, true);
		}
	};

	private boolean isCopyCutDestValid(String zip_file_path, String fp) {
		boolean enabled=true;
		if (mGp.copyCutList.size()>0) {
			if (mGp.copyCutFrom.equals(GlobalParameters.COPY_CUT_FROM_LOCAL)) {
			    //NOP
			} else if (mGp.copyCutFrom.equals(GlobalParameters.COPY_CUT_FROM_ZIP)) {
			    enabled=false;
				if (!mGp.copyCutFilePath.equals(zip_file_path)) enabled=true;
				else {
					String curr_dir=fp.equals("")?"":fp;
					for(TreeFilelistItem s_item:mGp.copyCutList) {
						String sel_path="";
						if (s_item.isDirectory()){
							sel_path=s_item.getPath().equals("")?s_item.getName():s_item.getPath()+"/"+s_item.getName();
						} else {
							sel_path=s_item.getPath();
						}
						String[] item_array=sel_path.equals("")?new String[]{"/"}:sel_path.substring(1).split("/");
						String[] cdir_array=curr_dir.equals("/")?new String[]{""}:curr_dir.split("/");
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
								else enabled=true;
							}
						}
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
		if (mGp.copyCutFrom.equals(GlobalParameters.COPY_CUT_FROM_LOCAL)) {
            if (mGp.copyCutModeIsCut) {
                NotifyEvent ntfy_move=new NotifyEvent(mActivity);
                ntfy_move.setListener(new NotifyEventListener(){
                    @Override
                    public void positiveResponse(Context c, Object[] o) {
                        mUiHandler.post(new Runnable(){
                            @Override
                            public void run() {
                                setUiEnabled();
                            }
                        });
                    }
                    @Override
                    public void negativeResponse(Context c, Object[] o) {}
                });
                prepareMoveCopyLocalToZip(add_item, ntfy_move, true);
            } else {
                prepareMoveCopyLocalToZip(add_item, null, false);
            }
        } else {
            CustomZipFile from_zf=new CustomZipFile(mActivity, new SafFile3(mActivity, mGp.copyCutFilePath), "UTF-8");
            CustomZipFile to_zf=new CustomZipFile(mActivity, new SafFile3(mActivity, mCurrentFilePath), "UTF-8");

            ArrayList<FileHeader>sel_list=buildExtractZipItemList(from_zf, mGp.copyCutList);
            try {
                final ThreadCtrl tc=new ThreadCtrl();
                ArrayList<FileHeader> to_zf_file_header=to_zf.getFileHeaders();
                setUiDisabled();
                showDialogProgress();
                mActivity.createDialogCancelListener(tc);
                Thread th=new Thread() {
                    @Override
                    public void run() {
                        if (mGp.copyCutFilePath.equals(mCurrentFilePath)) {
                            String parent_dir=mCurrentDirectory.getOriginalText().toString().substring(1);
                            moveCopyZipToZipSameZip(tc, sel_list, from_zf, to_zf, parent_dir, mGp.copyCutModeIsCut);
                        } else {
                            final String from_parent_dir=add_item[0].lastIndexOf("/")>0?add_item[0].substring(0,add_item[0].lastIndexOf("/")):add_item[0];
//                            final String from_parent_dir=from_parent_dir_temp.startsWith("/")?from_parent_dir_temp.substring(1):from_parent_dir_temp;
                            String to_parent_dir=mCurrentDirectory.getOriginalText().toString().substring(1);
                            moveCopyZipToZipSeparateZip(tc, sel_list, from_zf, to_zf, from_parent_dir, to_parent_dir, mGp.copyCutModeIsCut);
                        }
                    }
                };
                th.start();
            } catch(Exception e){
                e.printStackTrace();
                CommonDialog.showCommonDialog(mActivity.getSupportFragmentManager(), false, "E",
                        "Error", e.getMessage()+"\n"+MiscUtil.getStackTraceString(e), null);
            }
        }
	};

    private void moveCopyZipToZipSeparateZip(final ThreadCtrl tc, ArrayList<FileHeader>sel_list, CustomZipFile from_zf,
                                             CustomZipFile to_zf, final String from_parent_dir, final String to_parent_dir, final boolean move) {
        try {
            String msg="";
            if (move) msg=mActivity.getString(R.string.msgs_zip_file_manager_zip_to_zip_move_started);
            else msg=mActivity.getString(R.string.msgs_zip_file_manager_zip_to_zip_copy_started);
            putProgressMessage(msg);

            boolean result=false;
            String to_zip_temp_path=mCurrentFilePath+".tmp";
            BufferedZipFile3 to_bz=new BufferedZipFile3(mActivity, mCurrentFilePath, to_zip_temp_path, "UTF-8");
            String from_zip_temp_path=mGp.copyCutFilePath+".tmp";
            BufferedZipFile3 from_bz=new BufferedZipFile3(mActivity, mGp.copyCutFilePath, from_zip_temp_path, "UTF-8");

            CallBackListener to_bz_add_cbl=getZipProgressCallbackListenerWithPath(tc, to_bz,
                    mActivity.getString(R.string.msgs_zip_file_manager_zip_to_zip_file_copying));
            for(FileHeader edfh_item:sel_list) {
                String from_fn=edfh_item.getFileName().replace(from_parent_dir+"/", "");
                String to_fn_temp=to_parent_dir+"/"+from_fn;//edfh_item.getFileName();
                String to_fn=to_fn_temp.startsWith("/")?to_fn_temp.substring(1):to_fn_temp;
                boolean already_exists=to_zf.getFileHeader(to_fn)==null?false:true;
                if (edfh_item.isDirectory()) {
                    if (!already_exists) {
                        ZipParameters zp=new ZipParameters();
                        zp.setEntrySize(edfh_item.getUncompressedSize());
                        zp.setLastModifiedFileTime(ZipUtil.dosToJavaTme((int)edfh_item.getLastModifiedTime()));
                        zp.setFileNameInZip(to_fn);//edfh_item.file_header.getFileName());
                        zp.setCompressionMethod(edfh_item.getCompressionMethod());
                        tc.setExtraDataObject(new Object[]{edfh_item.getFileName()});
                        to_bz.mkDir(zp);
                        result=true;
                    }
                    if (move) {
                        from_bz.removeItem(edfh_item);
                        putProgressMessage(mActivity.getString(R.string.msgs_zip_local_file_move_moved, to_fn));
                    }
                } else {
                    if (already_exists && !edfh_item.isDirectory()) {
                        msg=mActivity.getString(R.string.msgs_zip_extract_file_confirm_replace_copy);
                        boolean replace_granted= mActivity.confirmReplace(tc, "", msg, to_fn);
                        if (isCancelled(true, tc)) {
                            msg=mActivity.getString(R.string.msgs_zip_file_manager_zip_to_zip_file_copy_cancelled, to_fn);
                            CommonDialog.showCommonDialog(mFragmentManager, false, "W", msg, "", null);
                            mUtil.addLogMsg("W", msg);
                            deleteBufferedZipFile(to_bz, from_bz);
                            closeUiDialogView(500);
                            return;
                        }
                        if (!replace_granted) {
                            putProgressMessage(mActivity.getString(R.string.msgs_zip_extract_file_was_not_replaced, to_fn));
                            mUtil.addLogMsg("I", mActivity.getString(R.string.msgs_zip_extract_file_was_not_replaced, to_fn));
                            break;
                        }
                    }

                    ZipParameters out_zp=new ZipParameters();
                    InputStream is=null;
                    if (edfh_item.isEncrypted()) {
                        verifyAndUpdatePasword(tc, from_zf, edfh_item);
                        from_zf.setPassword(mMainPassword);
                        is=getZipInputStream(null, from_zf, edfh_item, mUtil);
                        out_zp.setEncryptionMethod(edfh_item.getEncryptionMethod());
                        out_zp.setEncryptFiles(true);
                        out_zp.setPassword(mMainPassword);
                    } else {
                        is=getZipInputStream(null, from_zf, edfh_item, mUtil);
                    }

                    out_zp.setEntrySize(edfh_item.getUncompressedSize());
                    out_zp.setLastModifiedFileTime(ZipUtil.dosToJavaTme((int)edfh_item.getLastModifiedTime()));
                    out_zp.setFileNameInZip(to_fn);//edfh_item.file_header.getFileName());

                    if (edfh_item.getCompressionMethod()==CompressionMethod.DEFLATE) {
                        out_zp.setCompressionMethod(edfh_item.getCompressionMethod());
                    } else if (edfh_item.getCompressionMethod()==CompressionMethod.STORE) {
                        out_zp.setCompressionMethod(edfh_item.getCompressionMethod());
                    }

                    tc.setExtraDataObject(new Object[]{edfh_item.getFileName()});
                    result=to_bz.addItem(is, out_zp, to_bz_add_cbl);
                    is.close();

                    if (isCancelled(true, tc)) {
                        msg=mActivity.getString(R.string.msgs_zip_file_manager_zip_to_zip_file_move_cancelled, to_fn);
                        CommonDialog.showCommonDialog(mFragmentManager, false, "W", msg, "", null);
                        mUtil.addLogMsg("W", msg);
                        deleteBufferedZipFile(to_bz, from_bz);
                        closeUiDialogView(500);
                        return;
                    }

                    if (!result) {
                        msg=mActivity.getString(R.string.msgs_zip_file_manager_zip_to_zip_file_copy_failed, to_fn);
                        CommonDialog.showCommonDialog(mFragmentManager, false, "W", msg, "", null);
                        mUtil.addLogMsg("W", msg);
                        deleteBufferedZipFile(to_bz, from_bz);
                        closeUiDialogView(500);
                        return;
                    } else {
                        if (move) {
                            from_bz.removeItem(edfh_item);
                            msg=mActivity.getString(R.string.msgs_zip_file_manager_zip_to_zip_file_move_completed, to_fn);
                            putProgressMessage(msg);
                            mUtil.addLogMsg("I", msg);
                            mActivity.showSnackbar(mActivity, msg);
                        } else {
                            msg=mActivity.getString(R.string.msgs_zip_file_manager_zip_to_zip_file_copy_completed, to_fn);
                            putProgressMessage(msg);
                            mUtil.addLogMsg("I", msg);
                            mActivity.showSnackbar(mActivity, msg);
                        }
                    }
                }
            }

            if (result) {
//                closeZipToZipBufferedZipFile(tc, from_bz, to_bz, move);
                if (move) {//Cut mode
                    tc.setExtraDataObject(new Object[]{mCurrentFilePath});
                    CallBackListener to_bz_cbl=getZipProgressCallbackListenerWithPath(tc, to_bz,
                            mActivity.getString(R.string.msgs_zip_file_manager_zip_to_zip_zip_output_updating));
                    result=to_bz.close(to_bz_cbl);
                    if (isCancelled(tc)) {
                        deleteBufferedZipFile(to_bz, from_bz);
                        msg=mActivity.getString(R.string.msgs_zip_file_manager_zip_to_zip_zip_output_cancelled, mCurrentFilePath);
                        CommonDialog.showCommonDialog(mFragmentManager, false, "W", msg, "", null);
                        mUtil.addLogMsg("W", msg);
                        return;
                    } else {
                        if (!result) {
                            deleteBufferedZipFile(to_bz, from_bz);
                            msg=mActivity.getString(R.string.msgs_zip_file_manager_zip_to_zip_zip_output_failed, mCurrentFilePath);
                            CommonDialog.showCommonDialog(mFragmentManager, false, "W", msg, "", null);
                            mUtil.addLogMsg("W", msg);
                            return;
                        } else {
                            tc.setExtraDataObject(new Object[]{mGp.copyCutFilePath});
                            CallBackListener from_bz_close_cbl=getZipProgressCallbackListenerWithPath(tc, from_bz,
                                    mActivity.getString(R.string.msgs_zip_file_manager_zip_to_zip_zip_input_updating));
                            result=from_bz.close(from_bz_close_cbl);
                            if (isCancelled(tc)) {
                                deleteBufferedZipFile(to_bz, from_bz);
                                msg=mActivity.getString(R.string.msgs_zip_file_manager_zip_to_zip_zip_output_cancelled, mGp.copyCutFilePath);
                                CommonDialog.showCommonDialog(mFragmentManager, false, "W", msg, "", null);
                                mUtil.addLogMsg("W", msg);
                                return;
                            } else {
                                if (!result) {
                                    deleteBufferedZipFile(to_bz, from_bz);
                                    msg=mActivity.getString(R.string.msgs_zip_file_manager_zip_to_zip_zip_output_failed, mCurrentFilePath);
                                    CommonDialog.showCommonDialog(mFragmentManager, false, "W", msg, "", null);
                                    mUtil.addLogMsg("W", msg);
                                    return;
                                } else {
                                    SafFile3 from_zip=new SafFile3(mActivity, mGp.copyCutFilePath);
                                    from_zip.delete();
                                    result=from_bz.getOutputZipFile().renameTo(from_zip);

                                    SafFile3 to_zip=new SafFile3(mActivity, mCurrentFilePath);
                                    to_zip.delete();
                                    result=to_bz.getOutputZipFile().renameTo(to_zip);

                                    mActivity.showSnackbar(mActivity, mActivity.getString(R.string.msgs_zip_file_manager_zip_to_zip_move_completed));
                                }
                            }
                        }
                    }
                } else {//Copy mode
                    tc.setExtraDataObject(new Object[]{mCurrentFilePath});
                    CallBackListener to_bz_cbl=getZipProgressCallbackListenerWithPath(tc, to_bz, mActivity.getString(R.string.msgs_zip_file_manager_zip_to_zip_zip_output_updating));
                    result=to_bz.close(to_bz_cbl);
                    if (isCancelled(tc)) {
                        deleteBufferedZipFile(to_bz, from_bz);
                        msg=mActivity.getString(R.string.msgs_zip_file_manager_zip_to_zip_zip_output_cancelled, mCurrentFilePath);
                        CommonDialog.showCommonDialog(mFragmentManager, false, "W", msg, "", null);
                        mUtil.addLogMsg("W", msg);
                        return;
                    } else {
                        if (!result) {
                            deleteBufferedZipFile(to_bz, from_bz);
                            msg=mActivity.getString(R.string.msgs_zip_file_manager_zip_to_zip_zip_output_failed, mCurrentFilePath);
                            CommonDialog.showCommonDialog(mFragmentManager, false, "W", msg, "", null);
                            mUtil.addLogMsg("W", msg);
                            return;
                        } else {
                            SafFile3 to_zip=new SafFile3(mActivity, mCurrentFilePath);
                            to_zip.delete();
                            result=to_bz.getOutputZipFile().renameTo(to_zip);
                            if (result) {
                                mActivity.showSnackbar(mActivity, mActivity.getString(R.string.msgs_zip_file_manager_zip_to_zip_copy_completed));
                            } else {
                                msg=mActivity.getString(R.string.msgs_zip_file_manager_zip_to_zip_file_rename_cancelled, mCurrentFilePath);
                                CommonDialog.showCommonDialog(mFragmentManager, false, "W", msg, "", null);
                                mUtil.addLogMsg("W", msg);
                                return;
                            }
                        }
                    }
                }

                if (move) clearCopyCutItem(false);
            } else {
                clearCopyCutItem(false);
            }

            mUiHandler.post(new Runnable(){
                @Override
                public void run() {
                    setUiEnabled();
                    closeUiDialogView(100);
                }
            });

        } catch(Exception e) {
            e.printStackTrace();
            CommonDialog.showCommonDialog(mActivity.getSupportFragmentManager(), false, "E",
                    "Error", e.getMessage()+"\n"+MiscUtil.getStackTraceString(e), null);
        }
    }

    private void moveCopyZipToZipSameZip(final ThreadCtrl tc, ArrayList<FileHeader>sel_list, CustomZipFile from_zf,
                                             CustomZipFile to_zf, final String parent_dir, final boolean move) {
        try {
            String msg="";
            if (move) msg=mActivity.getString(R.string.msgs_zip_file_manager_zip_to_zip_move_started);
            else msg=mActivity.getString(R.string.msgs_zip_file_manager_zip_to_zip_copy_started);
            putProgressMessage(msg);

            boolean result=false;
            String to_zip_temp_path=mCurrentFilePath+".tmp";
            BufferedZipFile3 to_bz=new BufferedZipFile3(mActivity, mCurrentFilePath, to_zip_temp_path, "UTF-8");

            ArrayList<FileHeader>remove_item_list=new ArrayList<FileHeader>();

            CallBackListener to_bz_add_cbl=getZipProgressCallbackListenerWithPath(tc, to_bz,
                    mActivity.getString(R.string.msgs_zip_file_manager_zip_to_zip_file_copying));
            for(FileHeader edfh_item:sel_list) {
//                String fn=edfh_item.getFileName().replace(parent_dir+"/", "");
                String fn=parent_dir+"/"+edfh_item.getFileName();
                boolean already_exists=to_zf.getFileHeader(fn)==null?false:true;
                if (edfh_item.isDirectory()) {
                    if (!already_exists) {
                        ZipParameters zp=new ZipParameters();
                        zp.setEntrySize(edfh_item.getUncompressedSize());
                        zp.setLastModifiedFileTime(ZipUtil.dosToJavaTme((int)edfh_item.getLastModifiedTime()));
                        zp.setFileNameInZip(fn);//edfh_item.file_header.getFileName());
                        zp.setCompressionMethod(edfh_item.getCompressionMethod());
                        tc.setExtraDataObject(new Object[]{fn});
                        to_bz.mkDir(zp);
                        result=true;
                    }
                    if (move) {
                        remove_item_list.add(edfh_item);
                        putProgressMessage(mActivity.getString(R.string.msgs_zip_local_file_move_moved, fn));
                    }
                } else {
                    if (already_exists && !edfh_item.isDirectory()) {
                        msg=mActivity.getString(R.string.msgs_zip_extract_file_confirm_replace_copy);
                        boolean replace_granted= mActivity.confirmReplace(tc, "", msg, fn);
                        if (isCancelled(true, tc)) {
                            msg=mActivity.getString(R.string.msgs_zip_file_manager_zip_to_zip_file_copy_cancelled, fn);
                            CommonDialog.showCommonDialog(mFragmentManager, false, "W", msg, "", null);
                            mUtil.addLogMsg("W", msg);
                            try {to_bz.destroy();} catch(Exception e){};
                            closeUiDialogView(500);
                            return;
                        }
                        if (!replace_granted) {
                            putProgressMessage(mActivity.getString(R.string.msgs_zip_extract_file_was_not_replaced, fn));
                            mUtil.addLogMsg("I", mActivity.getString(R.string.msgs_zip_extract_file_was_not_replaced, fn));
                            break;
                        }
                    }

                    ZipParameters out_zp=new ZipParameters();
                    InputStream is=null;
                    if (edfh_item.isEncrypted()) {
                        verifyAndUpdatePasword(tc, from_zf, edfh_item);
                        from_zf.setPassword(mMainPassword);
                        is=getZipInputStream(null, from_zf, edfh_item, mUtil);
                        out_zp.setEncryptionMethod(edfh_item.getEncryptionMethod());
                        out_zp.setEncryptFiles(true);
                        out_zp.setPassword(mMainPassword);
                    } else {
                        is=getZipInputStream(null, from_zf, edfh_item, mUtil);
                    }

                    out_zp.setEntrySize(edfh_item.getUncompressedSize());
                    out_zp.setLastModifiedFileTime(ZipUtil.dosToJavaTme((int)edfh_item.getLastModifiedTime()));
                    out_zp.setFileNameInZip(fn);//edfh_item.file_header.getFileName());

                    if (edfh_item.getCompressionMethod()==CompressionMethod.DEFLATE) {
                        out_zp.setCompressionMethod(edfh_item.getCompressionMethod());
                    } else if (edfh_item.getCompressionMethod()==CompressionMethod.STORE) {
                        out_zp.setCompressionMethod(edfh_item.getCompressionMethod());
                    }

                    remove_item_list.add(edfh_item);

                    tc.setExtraDataObject(new Object[]{fn});
                    result=to_bz.addItem(is, out_zp, to_bz_add_cbl);
                    is.close();

                    if (isCancelled(true, tc)) {
                        msg=mActivity.getString(R.string.msgs_zip_file_manager_zip_to_zip_file_move_cancelled, fn);
                        CommonDialog.showCommonDialog(mFragmentManager, false, "W", msg, "", null);
                        mUtil.addLogMsg("W", msg);
                        try {to_bz.destroy();} catch(Exception e){};
                        closeUiDialogView(500);
                        return;
                    }

                    if (!result) {
                        msg=mActivity.getString(R.string.msgs_zip_file_manager_zip_to_zip_file_copy_failed, fn);
                        CommonDialog.showCommonDialog(mFragmentManager, false, "W", msg, "", null);
                        mUtil.addLogMsg("W", msg);
                        try {to_bz.destroy();} catch(Exception e){};
                        closeUiDialogView(500);
                        return;
                    } else {
                        if (move) {
                            msg=mActivity.getString(R.string.msgs_zip_file_manager_zip_to_zip_file_move_completed, fn);
                            putProgressMessage(msg);
                            mUtil.addLogMsg("I", msg);
                            mActivity.showSnackbar(mActivity, msg);
                        } else {
                            msg=mActivity.getString(R.string.msgs_zip_file_manager_zip_to_zip_file_copy_completed, fn);
                            putProgressMessage(msg);
                            mUtil.addLogMsg("I", msg);
                            mActivity.showSnackbar(mActivity, msg);
                        }
                    }
                }
            }

            if (result) {
//                closeZipToZipBufferedZipFile(tc, from_bz, to_bz, move);
                if (move) {//Cut mode
                    for(FileHeader rem_item:remove_item_list) {
                        to_bz.removeItem(rem_item);
                    }

                    tc.setExtraDataObject(new Object[]{mCurrentFilePath});
                    CallBackListener to_bz_cbl=getZipProgressCallbackListenerWithPath(tc, to_bz,
                            mActivity.getString(R.string.msgs_zip_file_manager_zip_to_zip_zip_output_updating));
                    result=to_bz.close(to_bz_cbl);
                    if (isCancelled(tc)) {
                        try {to_bz.destroy();} catch(Exception e){};
                        msg=mActivity.getString(R.string.msgs_zip_file_manager_zip_to_zip_zip_output_cancelled, mCurrentFilePath);
                        CommonDialog.showCommonDialog(mFragmentManager, false, "W", msg, "", null);
                        mUtil.addLogMsg("W", msg);
                        return;
                    } else {
                        if (!result) {
                            try {to_bz.destroy();} catch(Exception e){};
                            msg=mActivity.getString(R.string.msgs_zip_file_manager_zip_to_zip_zip_output_failed, mCurrentFilePath);
                            CommonDialog.showCommonDialog(mFragmentManager, false, "W", msg, "", null);
                            mUtil.addLogMsg("W", msg);
                            return;
                        } else {
                            SafFile3 to_zip=new SafFile3(mActivity, mCurrentFilePath);
                            to_zip.delete();
                            result=to_bz.getOutputZipFile().renameTo(to_zip);
                            if (result) {
                                mActivity.showSnackbar(mActivity, mActivity.getString(R.string.msgs_zip_file_manager_zip_to_zip_move_completed));
                            } else {
                                msg=mActivity.getString(R.string.msgs_zip_file_manager_zip_to_zip_file_rename_cancelled, mCurrentFilePath);
                                CommonDialog.showCommonDialog(mFragmentManager, false, "W", msg, "", null);
                                mUtil.addLogMsg("W", msg);
                                return;
                            }
                        }
                    }
                } else {//Copy mode
                    tc.setExtraDataObject(new Object[]{mCurrentFilePath});
                    CallBackListener to_bz_cbl=getZipProgressCallbackListenerWithPath(tc, to_bz, mActivity.getString(R.string.msgs_zip_file_manager_zip_to_zip_zip_output_updating));
                    result=to_bz.close(to_bz_cbl);
                    if (isCancelled(tc)) {
                        try {to_bz.destroy();} catch(Exception e){};
                        msg=mActivity.getString(R.string.msgs_zip_file_manager_zip_to_zip_zip_output_cancelled, mCurrentFilePath);
                        CommonDialog.showCommonDialog(mFragmentManager, false, "W", msg, "", null);
                        mUtil.addLogMsg("W", msg);
                        return;
                    } else {
                        if (!result) {
                            try {to_bz.destroy();} catch(Exception e){};
                            msg=mActivity.getString(R.string.msgs_zip_file_manager_zip_to_zip_zip_output_failed, mCurrentFilePath);
                            CommonDialog.showCommonDialog(mFragmentManager, false, "W", msg, "", null);
                            mUtil.addLogMsg("W", msg);
                            return;
                        } else {
                            SafFile3 to_zip=new SafFile3(mActivity, mCurrentFilePath);
                            to_zip.delete();
                            result=to_bz.getOutputZipFile().renameTo(to_zip);
                            if (result) {
                                mActivity.showSnackbar(mActivity, mActivity.getString(R.string.msgs_zip_file_manager_zip_to_zip_copy_completed));
                            } else {
                                msg=mActivity.getString(R.string.msgs_zip_file_manager_zip_to_zip_file_rename_cancelled, mCurrentFilePath);
                                CommonDialog.showCommonDialog(mFragmentManager, false, "W", msg, "", null);
                                mUtil.addLogMsg("W", msg);
                                return;
                            }
                        }
                    }
                }

                if (move) clearCopyCutItem(false);
            } else {
                clearCopyCutItem(false);
            }

            mUiHandler.post(new Runnable(){
                @Override
                public void run() {
                    setUiEnabled();
                    closeUiDialogView(100);
                }
            });

        } catch(Exception e) {
            e.printStackTrace();
            CommonDialog.showCommonDialog(mActivity.getSupportFragmentManager(), false, "E",
                    "Error", e.getMessage()+"\n"+MiscUtil.getStackTraceString(e), null);
        }
    }

    private void verifyAndUpdatePasword(ThreadCtrl tc, CustomZipFile from_zf, FileHeader edfh_item) throws Exception {
        final NotifyEvent ntfy_password_verify=new NotifyEvent(mActivity);
        ntfy_password_verify.setListener(new NotifyEventListener(){
            @Override
            public void positiveResponse(Context c, Object[] o) {
                mMainPassword=(String)o[0];
                notifyResponse(tc, edfh_item.getFileName());
            }
            @Override
            public void negativeResponse(Context c, Object[] o) {
                tc.setDisabled();
                notifyResponse(tc, edfh_item.getFileName());
            }
        });

        if (edfh_item.isEncrypted()) {
            if (mMainPassword!=null) {
                from_zf.setPassword(mMainPassword);
                if (!isCorrectZipFilePassword(from_zf, edfh_item, mMainPassword)) {
                    mUiHandler.post(new Runnable(){
                        @Override
                        public void run() {
                            getZipPasswordDlg(mActivity, mGp, mMainPassword, from_zf, edfh_item, ntfy_password_verify, true);
                        }
                    });
                    waitResponse(tc, edfh_item.getFileName());
                }
            } else {
                mUiHandler.post(new Runnable(){
                    @Override
                    public void run() {
                        getZipPasswordDlg(mActivity, mGp, mMainPassword, from_zf, edfh_item, ntfy_password_verify, true);
                    }
                });
                waitResponse(tc, edfh_item.getFileName());
            }
        }
    }
    
    private void deleteBufferedZipFile(BufferedZipFile3 to_bz, BufferedZipFile3 from_bz) {
	    try {to_bz.destroy();} catch(Exception e){};
        try {from_bz.destroy();} catch(Exception e){};
    }

	public ArrayList<FileHeader> buildExtractZipItemList(CustomZipFile from_zf, ArrayList<TreeFilelistItem>tfa_list) {
        mUtil.addDebugMsg(1, "I", "buildExtractZipItemList entered");
        CustomTreeFilelistAdapter tfa=new CustomTreeFilelistAdapter(mActivity, true, false);
        tfa.setDataList(tfa_list);

        ArrayList<FileHeader> zf_fhl=null;
        ArrayList<FileHeader> sel_fhl=new ArrayList<FileHeader>();
        try {
            zf_fhl=(ArrayList<FileHeader>) from_zf.getFileHeaders();
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
            mUtil.addDebugMsg(1, "I", "buildExtractZipItemList exited, size="+sel_fhl.size());

        } catch(Exception e) {
            e.printStackTrace();
            String ste_info="";
            for(StackTraceElement element:e.getStackTrace()) ste_info+=element.toString()+"\n";
            CommonDialog.showCommonDialog(mFragmentManager, false, "E",
                    mActivity.getString(R.string.msgs_zip_extract_file_end_with_error), ste_info, null);
            mUtil.addDebugMsg(1, "I", "buildExtractZipItemList exited with error"+"\n"+ste_info);
        }

        return sel_fhl;
    }

	private void createNewZipFileDialog() {
		boolean enableCreate=true;
		String title=mActivity.getString(R.string.msgs_zip_create_new_zip_file_title);
		String filename="/newZip.zip";
		String lurl=LocalMountPoint.getExternalStorageDir();
		String ldir="";

		NotifyEvent ntfy_select_file=new NotifyEvent(mActivity);
		ntfy_select_file.setListener(new NotifyEventListener(){
			@Override
			public void positiveResponse(Context c, Object[] o) {
                final String fp=(String)o[1];
				final SafFile3 lf=new SafFile3(mActivity, fp);
				if (!lf.isDirectory()) {
					NotifyEvent ntfy_comfirm_override=new NotifyEvent(mActivity);
					ntfy_comfirm_override.setListener(new NotifyEventListener(){
						@Override
						public void positiveResponse(Context c, Object[] o) {
                            lf.deleteIfExists();
                            try {
                                lf.createNewFile();
                                ZipUtil.writeEmptyZipHeader(lf);
                                showZipFile(false, lf);
                            } catch (Exception e) {
                                e.printStackTrace();
                                CommonDialog.showCommonDialog(mFragmentManager, false, "E", "ZIP file creation error", e.getMessage(), null);
                            }
						}
						@Override
						public void negativeResponse(Context c, Object[] o) {}
					});
					if (lf.exists()) {
						CommonDialog.showCommonDialog(mFragmentManager, true, "W",
								String.format(mActivity.getString(R.string.msgs_zip_create_new_zip_file_already_exists), lf.getName()),
								"", ntfy_comfirm_override);
					} else {
						ntfy_comfirm_override.notifyToListener(true, null);
					}
				} else {
					CommonDialog.showCommonDialog(mFragmentManager, false, "E",
							mActivity.getString(R.string.msgs_zip_create_new_zip_file_dir_can_not_used), "", null);
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
                        true, false, SafFile3.SAF_FILE_PRIMARY_UUID, "", filename, title);
        fsdf.showDialog(false, mActivity.getSupportFragmentManager(), fsdf, ntfy_select_file);
	};

	private void openZipFileDialog() {
		boolean enableCreate=false;
		String title=mActivity.getString(R.string.msgs_main_select_file);
		String filename="";
		String lurl=LocalMountPoint.getExternalStorageDir();
		String ldir="";

		NotifyEvent ntfy=new NotifyEvent(mActivity);
		ntfy.setListener(new NotifyEventListener(){
			@Override
			public void positiveResponse(Context c, Object[] o) {
				String in_path=(String)o[1];
                SafFile3 lf=new SafFile3(mActivity, in_path);
                if (lf.isDirectory()) {
                    CommonDialog.showCommonDialog(mFragmentManager, false, "W",
                            mActivity.getString(R.string.msgs_zip_create_new_zip_file_dir_can_not_used), "", null);
                } else {
                    if (lf.exists()) {
                        String err_msg=ZipUtil.isZipFile(mActivity, lf);
                        if (err_msg==null) {
                            showZipFile(false, lf);
                        } else {
                            CommonDialog.showCommonDialog(mFragmentManager, false, "W", "Invalid ZIP file", "File="+lf.getPath()+"\n"+err_msg, null);
                        }
                    } else {
                        CommonDialog.showCommonDialog(mFragmentManager, false, "W",
                                mActivity.getString(R.string.msgs_zip_create_new_zip_file_not_exists), "", null);
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

    static public boolean isCancelled(boolean wait, final ThreadCtrl tc) {
        return ActivityMain.isCancelled(wait, tc);
    }

    static public void setCancelled(final ThreadCtrl tc) {
        ActivityMain.setCancelled(tc);
    }

    static public boolean isCancelled(final ThreadCtrl tc) {
        return ActivityMain.isCancelled(false, tc);
    }

	private boolean isZipItemAlreadyExists(String zip_entry_name, SafFile3 in_zip, String encoding) {
	    boolean result=false;
        try {
            ArrayList<FileHeader>fhl=ZipUtil.getFileHeaders(mActivity, in_zip, encoding);
            FileHeader fh=ZipUtil.getFileHeader(fhl, zip_entry_name);
            if (fh!=null) result=true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

	private void prepareMoveCopyLocalToZip(final String[] add_item, final NotifyEvent p_ntfy, boolean move) {
        putProgressMessage(mActivity.getString(R.string.msgs_zip_add_file_starting));
        NotifyEvent ntfy_zip_parm=new NotifyEvent(mActivity);
        ntfy_zip_parm.setListener(new NotifyEventListener(){
            @Override
            public void positiveResponse(Context c, final Object[] o) {
                ZipParameters zp=(ZipParameters)o[0];
                String zip_curr_dir="";
                if (mCurrentDirectory.getOriginalText().toString().equals("") || mCurrentDirectory.getOriginalText().toString().equals("/")) zip_curr_dir="";
                else zip_curr_dir=mCurrentDirectory.getOriginalText().toString().substring(1);
                String parent_dir=add_item[0].lastIndexOf("/")>0?add_item[0].substring(0,add_item[0].lastIndexOf("/")):add_item[0];
                NotifyEvent notify_move_copy_ended=new NotifyEvent(mActivity);
                notify_move_copy_ended.setListener(new NotifyEventListener() {
                    @Override
                    public void positiveResponse(Context context, Object[] objects) {
                        if (p_ntfy!=null) {
                            p_ntfy.notifyToListener(true, objects);
                        } else {
                            closeUiDialogView(500);
                        }
                    }

                    @Override
                    public void negativeResponse(Context context, Object[] objects) {
                        closeUiDialogView(500);
                    }
                });
                moveCopyLocalToZip(add_item, zp, notify_move_copy_ended, parent_dir, zip_curr_dir, move);
            }
            @Override
            public void negativeResponse(Context c, Object[] o) {
                setUiEnabled();
                closeUiDialogView(500);
            }
        });
        getZipParmDlg(mUtil, mActivity, mGp, mEncodingSelected, "", mCurrentFilePath, ntfy_zip_parm);

	};

	static public void getAllItemInLocalDirectory(ArrayList<SafFile3> sel_item_list, SafFile3 sf) {
	    File lf=new File(sf.getPath());
	    if (lf.canRead()) {
	        if (lf.isDirectory()) sel_item_list.add(sf);//for ZIP file directory entry
	        getFileApiAllItemInLocalDirectory(sf.getContext(), sel_item_list, lf);
        } else {
            if (sf.isDirectory()) sel_item_list.add(sf);//for ZIP file directory entry
	        getSafApiAllItemInLocalDirectory(sel_item_list, sf);
        }
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

    private void moveCopyLocalToZip(final String[] add_item, final ZipParameters zp, //final boolean comp_msg_required,
                                    final NotifyEvent p_ntfy, final String zip_base, final String zip_curr_dir, final boolean move) {
		setUiDisabled();
		showDialogProgress();
		final ThreadCtrl tc=new ThreadCtrl();
		mActivity.createDialogCancelListener(tc);
		Thread th=new Thread(){
			@Override
			public void run() {
			    String msg="";
			    if (move) msg=mActivity.getString(R.string.msgs_zip_file_manager_local_to_zip_move_started);
			    else msg=mActivity.getString(R.string.msgs_zip_file_manager_local_to_zip_copy_started);
                putProgressMessage(msg);
                SafFile3 in_zip=new SafFile3(mActivity, mCurrentFilePath);
                SafFile3 out_temp = new SafFile3(mActivity, mCurrentFilePath+".tmp");
                String zip_file_name = mCurrentFilePath.substring(mCurrentFilePath.lastIndexOf("/"));
                try {
                    out_temp.createNewFile();
                    BufferedZipFile3 bzf = new BufferedZipFile3(mActivity, in_zip, out_temp, ZipUtil.DEFAULT_ZIP_FILENAME_ENCODING);
                    bzf.setNoCompressExtentionList(mGp.settingNoCompressFileType);
                    mActivity.clearConfirmReplaceResponse();
                    for (String item : add_item) {
                        SafFile3 add_file = new SafFile3(mActivity, item);
                        ZipParameters n_zp = new ZipParameters(zp);
                        n_zp.setDefaultFolderPath(zip_base+"/");
                        ArrayList<SafFile3> sel_list = new ArrayList<SafFile3>();
                        getAllItemInLocalDirectory(sel_list, add_file);
                        for (SafFile3 sel_file : sel_list) {
                            try {
                                tc.setExtraDataObject(new Object[]{sel_file.getPath()});
                                CallBackListener cbl=null;
                                cbl=getZipProgressCallbackListenerWithPath(tc, bzf, mActivity.getString(R.string.msgs_zip_file_manager_local_to_zip_file_copying));

                                boolean replace_granted=true;
                                String fp=sel_file.getPath().replace(n_zp.getDefaultFolderPath(), "");
                                String abs_input_file_path=sel_file.getPath().replace(n_zp.getDefaultFolderPath(), "");
                                String file_name_in_zip=zip_curr_dir.equals("")?abs_input_file_path:zip_curr_dir+"/"+abs_input_file_path;
                                if (sel_file.isDirectory()) n_zp.setFileNameInZip(file_name_in_zip+"/");
                                else n_zp.setFileNameInZip(file_name_in_zip);
                                boolean exists=bzf.exists(n_zp.getFileNameInZip());
                                if (exists) {
                                    msg=mActivity.getString(R.string.msgs_zip_extract_file_confirm_replace_copy);
                                    replace_granted= mActivity.confirmReplace(tc, "", msg, (sel_file.getPath()).replaceAll("//","/"));
                                }
                                if (replace_granted) {
                                    bzf.addItem(sel_file.getPath(), n_zp, cbl);
                                    if (isCancelled(true, tc)) {
                                        CommonDialog.showCommonDialog(mFragmentManager, false, "W", mActivity.getString(R.string.msgs_zip_file_manager_local_to_zip_file_copy_cancelled, sel_file.getPath()), "", null);
                                        try {bzf.destroy();} catch (Exception e) {}
                                        closeUiDialogView(500);
                                        return;
                                    }
                                    msg=mActivity.getString(R.string.msgs_zip_file_manager_local_to_zip_file_copy_completed, sel_file.getPath());
                                    putProgressMessage(msg);
                                    mUtil.addLogMsg("I",msg);

                                } else {
                                    if (!isCancelled(true, tc)) {
                                        msg=mActivity.getString(R.string.msgs_zip_file_manager_local_to_zip_file_replace_rejected, sel_file.getPath());
                                        putProgressMessage(msg);
                                        mUtil.addLogMsg("I", msg);
                                    } else {
                                        if (move) msg=mActivity.getString(R.string.msgs_zip_file_manager_local_to_zip_file_move_cancelled, sel_file.getPath());
                                        else msg=mActivity.getString(R.string.msgs_zip_file_manager_local_to_zip_file_copy_cancelled, sel_file.getPath());
                                        CommonDialog.showCommonDialog(mFragmentManager, false, "W", msg, "", null);
                                        mUtil.addLogMsg("I", msg);
                                        try {bzf.destroy();} catch (Exception e) {}
                                        closeUiDialogView(500);
                                        return;
                                    }
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                                tc.setThreadMessage(e.getMessage());
                                msg=mActivity.getString(R.string.msgs_zip_file_manager_local_to_zip_file_error_occured, sel_file.getPath());
                                mUtil.addLogMsg("I", msg);
                                CommonDialog.showCommonDialog(mFragmentManager, false, "E", msg, tc.getThreadMessage(), null);
                                closeUiDialogView(500);
                                return;
                            }
                        }
                    }
                    try {
                        CallBackListener cbl=getZipProgressCallbackListener(tc, bzf, mActivity.getString(R.string.msgs_zip_zip_file_being_updated));
                        if (bzf.isAborted() || bzf.close(cbl)) {
                            if (!bzf.isAborted()) renameBufferedZipFile(mGp, mUtil, mCurrentFilePath, out_temp.getPath(), zip_file_name);
                            else {
                                CommonDialog.showCommonDialog(mFragmentManager, false, "W", mActivity.getString(R.string.msgs_zip_write_zip_file_canelled), "", null);
                                try {bzf.destroy();} catch (Exception e) {}
                                closeUiDialogView(500);
                                return;
                            }
                        }

                        if (move) {
                            for(String item:add_item) {
                                SafFile3 lf=new SafFile3(mActivity, item);
                                deleteMovedLocalFile(lf);
                            }
                            mUiHandler.postDelayed(new Runnable(){
                                @Override
                                public void run() {
                                    mActivity.clearCopyCutItem(false);
                                }
                            },50);
                        }

                        if (p_ntfy != null) p_ntfy.notifyToListener(true, null);

                        if (move) msg=mActivity.getString(R.string.msgs_zip_file_manager_local_to_zip_move_completed);
                        else msg=mActivity.getString(R.string.msgs_zip_file_manager_local_to_zip_copy_completed);
                        mUtil.addLogMsg("I", msg);

                        mActivity.showSnackbar(mActivity, msg);
                        closeUiDialogView(500);

                        mUiHandler.postDelayed(new Runnable(){
                            @Override
                            public void run() {
                                refreshFileList(true);
                            }
                        },50);
                    } catch (Exception e) {
                        e.printStackTrace();
                        msg=mActivity.getString(R.string.msgs_zip_file_manager_local_to_zip_update_failed);
                        tc.setThreadMessage(e.getMessage()+"\n"+MiscUtil.getStackTraceString(e));
                        mUtil.addLogMsg("I", msg+"\n"+e.getMessage()+"\n"+MiscUtil.getStackTraceString(e));
                        CommonDialog.showCommonDialog(mFragmentManager, false, "E", msg, tc.getThreadMessage(), null);
                        closeUiDialogView(500);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    out_temp.deleteIfExists();
                    msg=mActivity.getString(R.string.msgs_zip_file_manager_local_to_zip_create_failed);
                    mUtil.addLogMsg("E", msg+"\n"+MiscUtil.getStackTraceString(e));
                    CommonDialog.showCommonDialog(mFragmentManager, false, "E", msg, e.getMessage()+"\n"+MiscUtil.getStackTraceString(e), null);
                    closeUiDialogView(500);
                }
            }
		};
		th.start();
	};

    private boolean deleteMovedLocalFile(SafFile3 del_item) {
        boolean result=false;
        if (del_item.exists()) {
            if (del_item.isDirectory()) {
                SafFile3[] del_list=del_item.listFiles();
                if (del_list!=null && del_list.length>0) {
                    for(SafFile3 child_item:del_list) {
                        result= deleteMovedLocalFile(child_item);
                        if (!result) break;
                    }
                    if (result) {
                        result=del_item.delete();
                        String msg=mActivity.getString(R.string.msgs_zip_file_manager_local_to_zip_delete_moved_directory, del_item.getPath());
                        putProgressMessage(msg);
                        mUtil.addLogMsg("I", msg);
                    }
                } else {
                    result=del_item.delete();
                    String msg=mActivity.getString(R.string.msgs_zip_file_manager_local_to_zip_delete_moved_directory, del_item.getPath());
                    putProgressMessage(msg);
                    mUtil.addLogMsg("I", msg);
                }
            } else {
                result=del_item.delete();
                ActivityMain.scanMediaFile(mGp, mUtil, del_item.getPath());
                String msg=mActivity.getString(R.string.msgs_zip_file_manager_local_to_zip_delete_moved_file, del_item.getPath());
                putProgressMessage(msg);
                mUtil.addLogMsg("I", msg);
            }
        }
        return result;
    };

    private CallBackListener getZipProgressCallbackListener(final ThreadCtrl tc, final BufferedZipFile3 bzf, final String msg_txt) {
        CallBackListener cbl=new CallBackListener() {
            @Override
            public void onCallBack(Context c, boolean positive, Object[] o2) {
                if (isCancelled(tc)) {
                    bzf.abort();
                } else {
                    int prog=(Integer)o2[0];
                    putProgressMessage(msg_txt+" "+prog+"%");
                }
            }
        };
        return cbl;
    }

    public CallBackListener getZipProgressCallbackListenerWithPath(final ThreadCtrl tc, final BufferedZipFile3 bzf, final String msg_txt) {
        CallBackListener cbl=new CallBackListener() {
            @Override
            public void onCallBack(Context c, boolean positive, Object[] o2) {
                if (isCancelled(tc)) {
                    bzf.abort();
                } else {
                    int prog=(Integer)o2[0];
                    putProgressMessage(String.format(msg_txt, (String)tc.getExtraDataObject()[0] , prog));
                }
            }
        };
        return cbl;
    }

    static public void getZipParmDlg(CommonUtilities mUtil, final Activity mActivity, final GlobalParameters mGp,
                                     final String selected_encoding, final String pswd, final String fp, final NotifyEvent p_ntfy) {
		final CustomZipFile zf=createZipFile(mGp.appContext, fp, selected_encoding);
		mUtil.addDebugMsg(1, "I", "getZipParm entered");

		final Dialog dialog = new Dialog(mActivity);
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
    	final TextInputLayout ll_dlg_pswd=(TextInputLayout)dialog.findViewById(R.id.zip_parm_dlg_enc_password_view);
    	final TextInputEditText dlg_pswd=(TextInputEditText)dialog.findViewById(R.id.zip_parm_dlg_enc_password);
        final TextInputLayout ll_dlg_conf=(TextInputLayout)dialog.findViewById(R.id.zip_parm_dlg_enc_confirm_view);
    	final TextInputEditText dlg_conf=(TextInputEditText)dialog.findViewById(R.id.zip_parm_dlg_enc_confirm);

        final RadioGroup dlg_rg_comp=(RadioGroup)dialog.findViewById(R.id.zip_parm_dlg_comp_level_rg);
        final RadioButton dlg_rb_comp_fastest=(RadioButton)dialog.findViewById(R.id.zip_parm_dlg_comp_level_rb_fastest);
        final RadioButton dlg_rb_comp_normal=(RadioButton)dialog.findViewById(R.id.zip_parm_dlg_comp_level_rb_normal);
        final RadioButton dlg_rb_comp_maximum=(RadioButton)dialog.findViewById(R.id.zip_parm_dlg_comp_level_rb_maximum);

        final RadioGroup dlg_rg_enc=(RadioGroup)dialog.findViewById(R.id.zip_parm_dlg_enc_type_rg);
    	final RadioButton dlg_rb_none=(RadioButton)dialog.findViewById(R.id.zip_parm_dlg_enc_type_rb_none);
    	final RadioButton dlg_rb_std=(RadioButton)dialog.findViewById(R.id.zip_parm_dlg_enc_type_rb_standard);
    	final RadioButton dlg_rb_aes128=(RadioButton)dialog.findViewById(R.id.zip_parm_dlg_enc_type_rb_aes128);
    	final RadioButton dlg_rb_aes256=(RadioButton)dialog.findViewById(R.id.zip_parm_dlg_enc_type_rb_aes256);

    	final Button dlg_cancel=(Button)dialog.findViewById(R.id.zip_parm_dlg_cancel_btn);
    	final Button dlg_ok=(Button)dialog.findViewById(R.id.zip_parm_dlg_ok_btn);

    	CommonDialog.setDlgBoxSizeLimit(dialog, false);

        dlg_rg_comp.setOnCheckedChangeListener(new OnCheckedChangeListener(){
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if(checkedId==dlg_rb_comp_fastest.getId()) {
                    ll_dlg_pswd.setVisibility(EditText.GONE);
                    ll_dlg_conf.setVisibility(EditText.GONE);
                } else if(checkedId==dlg_rb_comp_normal.getId()) {
                    ll_dlg_pswd.setVisibility(EditText.VISIBLE);
                    ll_dlg_conf.setVisibility(EditText.VISIBLE);
                } else if(checkedId==dlg_rb_comp_maximum.getId()) {
                    ll_dlg_pswd.setVisibility(EditText.VISIBLE);
                    ll_dlg_conf.setVisibility(EditText.VISIBLE);
                }
            }
        });

        dlg_rg_enc.setOnCheckedChangeListener(new OnCheckedChangeListener(){
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				if(checkedId==dlg_rb_none.getId()) {
                    ll_dlg_pswd.setVisibility(EditText.GONE);
                    ll_dlg_conf.setVisibility(EditText.GONE);
				} else if(checkedId==dlg_rb_std.getId()) {
                    ll_dlg_pswd.setVisibility(EditText.VISIBLE);
                    ll_dlg_conf.setVisibility(EditText.VISIBLE);
				} else if(checkedId==dlg_rb_aes128.getId()) {
                    ll_dlg_pswd.setVisibility(EditText.VISIBLE);
                    ll_dlg_conf.setVisibility(EditText.VISIBLE);
				} else if(checkedId==dlg_rb_aes256.getId()) {
		    		ll_dlg_pswd.setVisibility(EditText.VISIBLE);
                    ll_dlg_conf.setVisibility(EditText.VISIBLE);
				}
				checkZipParmValidation(mGp, dialog, fp, zf);
			}
    	});

		dlg_rb_none.setEnabled(true);
		dlg_rb_none.setChecked(true);
		ll_dlg_pswd.setVisibility(EditText.GONE);
		ll_dlg_conf.setVisibility(EditText.GONE);

    	dlg_pswd.setText(pswd);

    	ll_dlg_pswd.setPasswordVisibilityToggleEnabled(true);
    	ll_dlg_pswd.setEndIconOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (dlg_pswd.getTransformationMethod()!=null) {
                    dlg_pswd.setTransformationMethod(null);
                    ll_dlg_conf.setVisibility(TextInputLayout.GONE);
                } else {
                    dlg_pswd.setTransformationMethod(new PasswordTransformationMethod());
                    ll_dlg_conf.setVisibility(TextInputLayout.VISIBLE);
                }
                checkZipParmValidation(mGp, dialog, fp, zf);
            }
        });

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
				ZipParameters zp=new ZipParameters();

                if(dlg_rb_comp_fastest.isChecked()) {
                    zp.setCompressionLevel(CompressionLevel.FASTEST);
                } else if(dlg_rb_comp_normal.isChecked()) {
                    zp.setCompressionLevel(CompressionLevel.NORMAL);
                } else if(dlg_rb_comp_maximum.isChecked()) {
                    zp.setCompressionLevel(CompressionLevel.MAXIMUM);
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
        final TextInputLayout ll_dlg_pswd=(TextInputLayout)dialog.findViewById(R.id.zip_parm_dlg_enc_password_view);
        final TextInputEditText dlg_pswd=(TextInputEditText)dialog.findViewById(R.id.zip_parm_dlg_enc_password);
        final TextInputLayout ll_dlg_conf=(TextInputLayout)dialog.findViewById(R.id.zip_parm_dlg_enc_confirm_view);
        final TextInputEditText dlg_conf=(TextInputEditText)dialog.findViewById(R.id.zip_parm_dlg_enc_confirm);
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
    	    if (ll_dlg_conf.getVisibility()==TextInputLayout.VISIBLE) {
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
            } else {
                if (dlg_pswd.getText().length()>0) {
                    dlg_msg.setText("");
                    dlg_ok.setEnabled(true);
                } else {
                    dlg_ok.setEnabled(false);
                    dlg_msg.setText(mGp.appContext.getString(R.string.msgs_zip_parm_pswd_not_specified));
                }
            }
    	}
	};

	private void extractDlg(final CustomTreeFilelistAdapter tfa) {
		NotifyEvent ntfy=new NotifyEvent(mActivity);
		ntfy.setListener(new NotifyEventListener(){
			@Override
			public void positiveResponse(Context c, Object[] o) {
                final String dest_path=((String)o[1]);
                String t_cd=mCurrentDirectory.getOriginalText().equals("/")?"":mCurrentDirectory.getOriginalText().toString().substring(1);
                prepareMoveCopyZipToLocal(mCurrentFilePath, mEncodingSelected, tfa.getDataList(), t_cd, dest_path, null);
			}
			@Override
			public void negativeResponse(Context c, Object[] o) {
			}
		});
		String uuid=SafFile3.getUuidFromFilePath(mCurrentFilePath);
        boolean include_root=false;
        boolean scoped_storage_mode=mGp.safMgr.isScopedStorageMode();
        CommonFileSelector2 fsdf=
                CommonFileSelector2.newInstance(scoped_storage_mode, true, false, CommonFileSelector2.DIALOG_SELECT_CATEGORY_DIRECTORY,
                        true, true, uuid, "", "", mActivity.getString(R.string.msgs_zip_extract_select_dest_directory));
        fsdf.showDialog(false, mActivity.getSupportFragmentManager(), fsdf, ntfy);
	};

	public void prepareMoveCopyZipToLocal(String zip_path, String encoding, ArrayList<TreeFilelistItem>tfa_list, String zip_curr_dir, String dest_dir, NotifyEvent p_ntfy) {
        CustomZipFile from_zf=new CustomZipFile(mActivity, new SafFile3(mActivity, zip_path), encoding);

        ArrayList<FileHeader>sel_list=buildExtractZipItemList(from_zf, tfa_list);
        final ThreadCtrl tc=new ThreadCtrl();
        setUiDisabled();
        showDialogProgress();
        mActivity.createDialogCancelListener(tc);
        Thread th=new Thread() {
            @Override
            public void run() {
                SafFile3 sf=new SafFile3(mActivity, dest_dir);
                moveCopyZipToLocal(tc, sel_list, from_zf, sf, mGp.copyCutModeIsCut, p_ntfy);
            }
        };
        th.start();

    }

    public void moveCopyZipToLocal(final ThreadCtrl tc, ArrayList<FileHeader>sel_list, CustomZipFile from_zf,
                                   final SafFile3 to_dir, final boolean move, final NotifyEvent p_ntfy) {
        try {
            String msg="";
            if (move) msg=mActivity.getString(R.string.msgs_zip_file_manager_zip_to_local_move_started);
            else msg=mActivity.getString(R.string.msgs_zip_file_manager_zip_to_local_copy_started);
            putProgressMessage(msg);

            boolean result=false;
            String from_zip_temp_path=from_zf.getSafFile().getPath()+".tmp";
            BufferedZipFile3 from_bz=new BufferedZipFile3(mActivity, from_zf.getSafFile().getPath(), from_zip_temp_path, "UTF-8");

            boolean zip_item_deleted=false;
            CallBackListener to_bz_add_cbl=getZipProgressCallbackListenerWithPath(tc, from_bz, mActivity.getString(R.string.msgs_zip_file_manager_zip_to_local_copying));
            for(FileHeader edfh_item:sel_list) {
                if (!edfh_item.isDirectory()) {
                    String fn=edfh_item.getFileName();
                    String to_file_temp_path=to_dir.getAppDirectoryCache()+"/"+System.currentTimeMillis();
                    String to_file_dest_path=to_dir.getPath()+"/"+fn;
                    File to_file_temp=new File(to_file_temp_path);
                    SafFile3 to_file_dest=new SafFile3(mActivity, to_file_dest_path);
                    boolean already_exists=to_file_dest.exists();
                    if (already_exists) {
                        msg=mActivity.getString(R.string.msgs_zip_extract_file_confirm_replace_copy);
                        boolean replace_granted= mActivity.confirmReplace(tc, "", msg, fn);
                        if (isCancelled(true, tc)) {
                            msg=mActivity.getString(R.string.msgs_zip_file_manager_zip_to_local_copy_cancelled, fn);
                            CommonDialog.showCommonDialog(mFragmentManager, false, "W", msg, "", null);
                            mUtil.addLogMsg("W", msg);
                            try {from_bz.destroy();} catch (Exception e){};
                            closeUiDialogView(500);
                            return;
                        }
                        if (!replace_granted) {
                            putProgressMessage(mActivity.getString(R.string.msgs_zip_extract_file_was_not_replaced, fn));
                            mUtil.addLogMsg("I", mActivity.getString(R.string.msgs_zip_extract_file_was_not_replaced, fn));
                            break;
                        }
                    }

                    if (edfh_item.isEncrypted()) {
                        verifyAndUpdatePasword(tc, from_zf, edfh_item);
                        from_zf.setPassword(mMainPassword);
                    }
                    InputStream is=getZipInputStream(null, from_zf, edfh_item, mUtil);

                    FileOutputStream fos=new FileOutputStream(to_file_temp);
                    result=copyFile(mActivity, tc, edfh_item.getUncompressedSize(), is, fos, new CallBackListener() {
                        @Override
                        public void onCallBack(Context context, boolean positive, Object[] o) {
                            int prog=(int)o[0];
                            putProgressMessage(mActivity.getString(R.string.msgs_zip_file_manager_zip_to_local_copying, edfh_item.getFileName(), prog));
                        }
                    });

                    if (isCancelled(true, tc)) {
                        msg=mActivity.getString(R.string.msgs_zip_file_manager_zip_to_local_move_cancelled, fn);
                        CommonDialog.showCommonDialog(mFragmentManager, false, "W", msg, "", null);
                        mUtil.addLogMsg("W", msg);
                        try {from_bz.destroy();} catch (Exception e){};
                        closeUiDialogView(500);
                        return;
                    }

                    if (!result) {
                        msg=mActivity.getString(R.string.msgs_zip_file_manager_zip_to_local_copy_failed, fn);
                        CommonDialog.showCommonDialog(mFragmentManager, false, "W", msg, "", null);
                        mUtil.addLogMsg("W", msg);
                        try {from_bz.destroy();} catch (Exception e){};
                        closeUiDialogView(500);
                        return;
                    } else {
                        SafFile3 temp=new SafFile3(mActivity, to_file_temp_path);
                        to_file_dest.deleteIfExists();
                        boolean rename_result=temp.renameTo(to_file_dest);
                        if (rename_result) {
                            ActivityMain.scanMediaFile(mGp, mUtil, to_file_dest_path);
                            if (move) {
                                from_bz.removeItem(edfh_item);
                                msg=mActivity.getString(R.string.msgs_zip_file_manager_zip_to_local_file_move_completed, fn);
                                putProgressMessage(msg);
                                mUtil.addLogMsg("I", msg);
                                zip_item_deleted=true;
                            } else {
                                msg=mActivity.getString(R.string.msgs_zip_file_manager_zip_to_local_file_copy_completed, fn);
                                putProgressMessage(msg);
                                mUtil.addLogMsg("I", msg);
                            }
                        } else {
                            msg=mActivity.getString(R.string.msgs_zip_file_manager_zip_to_local_file_rename_failed, fn);
                            putProgressMessage(msg);
                            mUtil.addLogMsg("I", msg);
                            try {from_bz.destroy();} catch (Exception e){};
                            closeUiDialogView(500);
                            return;
                        }
                    }
                }
            }

            if (result) {
                if (move) {
                    tc.setExtraDataObject(new Object[]{mGp.copyCutFilePath});
                    CallBackListener from_bz_close_cbl=getZipProgressCallbackListenerWithPath(tc, from_bz, mActivity.getString(R.string.msgs_zip_file_manager_zip_to_local_zip_input_zip_updating));
                    result=from_bz.close(from_bz_close_cbl);
                    if (isCancelled(tc)) {
                        try {from_bz.destroy();} catch (Exception e){};
                        msg=mActivity.getString(R.string.msgs_zip_file_manager_zip_to_local_zip_update_cancelled, mGp.copyCutFilePath);
                        CommonDialog.showCommonDialog(mFragmentManager, false, "W", msg, "", null);
                        mUtil.addLogMsg("W", msg);
                        return;
                    } else {
                        if (!result) {
                            try {from_bz.destroy();} catch (Exception e){};
                            msg=mActivity.getString(R.string.msgs_zip_file_manager_zip_to_local_zip_update_failed, mCurrentFilePath);
                            CommonDialog.showCommonDialog(mFragmentManager, false, "W", msg, "", null);
                            mUtil.addLogMsg("W", msg);
                            return;
                        } else {
                            SafFile3 from_zip=new SafFile3(mActivity, mGp.copyCutFilePath);
                            from_zip.delete();
                            result=from_bz.getOutputZipFile().renameTo(from_zip);
                            if (result) {
                                msg=mActivity.getString(R.string.msgs_zip_file_manager_zip_to_local_move_completed);
                                mActivity.showSnackbar(mActivity, msg);
                            } else {
                                msg=mActivity.getString(R.string.msgs_zip_file_manager_zip_to_local_file_rename_failed, mGp.copyCutFilePath);
                                CommonDialog.showCommonDialog(mFragmentManager, false, "W", msg, "", null);
                                mUtil.addLogMsg("W", msg);
                                return;
                            }
                        }
                    }
                    clearCopyCutItem(false);
                } else {
                    msg=mActivity.getString(R.string.msgs_zip_file_manager_zip_to_local_copy_completed);
                    mActivity.showSnackbar(mActivity, msg);
                }
            }

            mUiHandler.post(new Runnable(){
                @Override
                public void run() {
                    p_ntfy.notifyToListener(true, null);
                    setUiEnabled();
                    closeUiDialogView(100);
                }
            });

        } catch(Exception e) {
            e.printStackTrace();
            CommonDialog.showCommonDialog(mActivity.getSupportFragmentManager(), false, "E",
                    "Error", e.getMessage()+"\n"+MiscUtil.getStackTraceString(e), null);
        }
    }

	public void waitResponse(Object o, String id) throws InterruptedException {
	    synchronized (o) {
            mUtil.addDebugMsg(1, "I", "wait issued, id="+id);
	        o.wait();
        }
    }

    public void notifyResponse(Object o, String id) {
        synchronized (o) {
            mUtil.addDebugMsg(1, "I", "notify issued, id="+id);
            o.notify();
        }
    }

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

    	final TextInputLayout ll_dlg_pswd=(TextInputLayout)dialog.findViewById(R.id.password_prompt_dlg_itemname_view);
    	final TextInputEditText dlg_pswd=(TextInputEditText)dialog.findViewById(R.id.password_prompt_dlg_itemname);

    	final Button dlg_cancel=(Button)dialog.findViewById(R.id.password_prompt_dlg_cancel_btn);
    	final Button dlg_ok=(Button)dialog.findViewById(R.id.password_prompt_dlg_ok_btn);
    	dlg_ok.setVisibility(Button.VISIBLE);

    	dlg_pswd.setVisibility(EditText.VISIBLE);

    	CommonDialog.setDlgBoxSizeLimit(dialog, true);

        if (mMainPassword!=null) {
            dlg_pswd.setText(mMainPassword);
            if (isCorrectZipFilePassword(zf, fh, dlg_pswd.getText().toString())) {
                CommonDialog.setViewEnabled(mActivity, dlg_ok, true);
                dlg_msg.setText("");
            } else {
                CommonDialog.setViewEnabled(mActivity, dlg_ok, false);
                dlg_msg.setText(R.string.msgs_zip_extract_zip_password_wrong);
            }
        } else {
            CommonDialog.setViewEnabled(mActivity, dlg_ok, false);
            dlg_msg.setText(R.string.msgs_zip_extract_zip_password_not_specified);
        }

        ThreadCtrl tc=new ThreadCtrl();
        ll_dlg_pswd.setEndIconOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                tc.setDisabled();
                if (dlg_pswd.getTransformationMethod()!=null) {
                    dlg_pswd.setTransformationMethod(null);
                } else {
                    dlg_pswd.setTransformationMethod(new PasswordTransformationMethod());
                }
                tc.setEnabled();
            }
        });

        dlg_pswd.addTextChangedListener(new TextWatcher(){
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
			}
			@Override
			public void afterTextChanged(Editable s) {
			    if (tc.isEnabled()) {
                    if (s.toString().length()>0) {
                        CommonDialog.setViewEnabled(mActivity, dlg_ok, true);
                        dlg_msg.setText("");
                    } else {
                        CommonDialog.setViewEnabled(mActivity, dlg_ok, false);
                        dlg_msg.setText(R.string.msgs_zip_extract_zip_password_not_specified);
                    }
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

    public ArrayList<ZipFileViewerItem> getZipFileViewerList() {
	    return zipFileViewerList;
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
				int rc=is.read(buff);
				result=true;
				is.close();
			}
		} catch (ZipException e) {
//			e.printStackTrace();
		} catch (Exception e) {
//			e.printStackTrace();
		}
		return result;
	};

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
							    String n1=fh.getFileName();
                                String n2=tfli.getZipFileName();
								if (fh.getFileName().startsWith(tfli.getZipFileName()+"/")) {
									sel_fh.add(fh);
									break;
                                }
							} else {
                                String n1=fh.getFileName();
                                String n2=tfli.getZipFileName();
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
			if (tfli.isChecked()) {
				w_conf_list+=sep+tfli.getZipFileName();
				sep="\n";
			}
		}
		final String conf_list=w_conf_list;
		NotifyEvent ntfy=new NotifyEvent(mActivity);
		ntfy.setListener(new NotifyEventListener(){
			@Override
			public void positiveResponse(Context c, Object[] o) {
				deleteZipFileItem(mCurrentFilePath, mEncodingSelected, tfa, conf_list, null);//, true);
			}
			@Override
			public void negativeResponse(Context c, Object[] o) {}
		});
		CommonDialog.showCommonDialog(mFragmentManager, true, "W",
				mActivity.getString(R.string.msgs_zip_delete_confirm_delete_zip), conf_list, ntfy);
	};

	private void deleteZipFileItem(final String zip_file_path, final String zip_encoding,
                                   final CustomTreeFilelistAdapter tfa, final String conf_list, final NotifyEvent p_ntfy){//}, final boolean com_msg_required) {
		setUiDisabled();
		showDialogProgress();
		final ThreadCtrl tc=new ThreadCtrl();
		mActivity.createDialogCancelListener(tc);
		Thread th=new Thread(){
			@Override
			public void run() {
				mUtil.addDebugMsg(1, "I", "Delete started");
				SafFile3 out_temp=new SafFile3(mActivity, zip_file_path+".tmp");
				String zip_file_name=zip_file_path.substring(zip_file_path.lastIndexOf("/"));
				String error_msg="";
                putProgressMessage(mActivity.getString(R.string.msgs_zip_delete_file_has_been_started));
                CustomZipFile zf=createZipFile(mActivity, zip_file_path, zip_encoding);
                try {
                    BufferedZipFile3 bzf=new BufferedZipFile3(mActivity, zip_file_path, out_temp.getPath(), ZipUtil.DEFAULT_ZIP_FILENAME_ENCODING);
                    ArrayList<FileHeader> sel_fh=buildSelectedFileHeaderList(zf, tfa);
                    String msg=mActivity.getString(R.string.msgs_zip_delete_file_was_deleted);
                    for(FileHeader fh:sel_fh) {
                        if (isCancelled(true, tc)) {
                            CommonDialog.showCommonDialog(mFragmentManager, false, "I",
                                    String.format(mActivity.getString(R.string.msgs_zip_delete_file_was_cancelled),fh.getFileName()), "", null);
                            try {bzf.destroy();} catch(ZipException e) {}
                            break;
                        } else {
                            bzf.removeItem(fh);
                            putProgressMessage(String.format(msg,fh.getFileName()));
                            mUtil.addLogMsg("I", String.format(msg,fh.getFileName()));
                        }
                    }
                    if (!isCancelled(true, tc)) {
                        CallBackListener cbl=getZipProgressCallbackListener(tc, bzf, mActivity.getString(R.string.msgs_zip_zip_file_being_updated));
                        if (bzf.isAborted() || bzf.close(cbl)) {
                            if (!bzf.isAborted()) {
                                renameBufferedZipFile(mGp, mUtil, zip_file_path, out_temp.getPath(), zip_file_name);
                                mActivity.showSnackbar(mActivity, mActivity.getString(R.string.msgs_zip_delete_file_completed));
                            }
                            else {
                                CommonDialog.showCommonDialog(mFragmentManager, false, "W", mActivity.getString(R.string.msgs_zip_write_zip_file_canelled), "", null);
                                try {bzf.destroy();} catch (Exception e) {}
                                out_temp.deleteIfExists();
                                closeUiDialogView(500);
                                return;
                            }
                        }

                    }
                } catch (Exception e) {
                    mUtil.addLogMsg("I", e.getMessage());
                    error_msg=e.getMessage();
                    CommonUtilities.printStackTraceElement(mUtil, e.getStackTrace());
                    CommonDialog.showCommonDialog(mFragmentManager, false, "I",
                            mActivity.getString(R.string.msgs_zip_delete_file_aborted), error_msg, null);
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

    static public void renameBufferedZipFile(GlobalParameters gp, CommonUtilities util, String dest_path, String out_path, String zip_file_name) {
        util.addDebugMsg(1,"I","renameBufferedZipFile entered");
        long b_time= System.currentTimeMillis();
        SafFile3 df=new SafFile3(gp.appContext, dest_path);
        SafFile3 of=new SafFile3(gp.appContext, out_path);
        df.deleteIfExists();
        LocalFileManager.renameSafFile(of, df);
        util.addDebugMsg(1,"I","renameBufferedZipFile elapsed time="+(System.currentTimeMillis()-b_time));
    }

    private void putProgressMessage(final String msg) {
        mActivity.putProgressMessage(msg);
    }

    final private void refreshOptionMenu() {
		mActivity.invalidateOptionsMenu();
	};

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

	private boolean isUiEnabled() {
		return mActivity.isUiEnabled();
	};

	public void showDialogProgress() {
	    mActivity.showDialogProgress();
    };

    private void hideDialog() {
        mActivity.hideDialog();
	};

	public void setContextButtonPasteEnabled(boolean enabled) {
		if (enabled) mContextButtonPasteView.setVisibility(LinearLayout.VISIBLE);
		else mContextButtonPasteView.setVisibility(LinearLayout.INVISIBLE);
	};

	private String mEncodingDesired="";
	private String mEncodingSelected=ENCODING_NAME_UTF8;
	private void setZipTreeFileListener() {
		mContextButtonDeleteView.setVisibility(ImageButton.INVISIBLE);
		NotifyEvent ntfy_cb=new NotifyEvent(mActivity);
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
					String curr_dir=mCurrentDirectory.getOriginalText().toString().substring(1);
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
                    mTreeFilelistAdapter.setZipArchiveFileName(mCurrentFilePath);
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
                FileManagerDirectoryListItem dli=mDirectoryList.get(0);
				CommonUtilities.clearDirectoryItem(mDirectoryList, mZipFileSpinner.getSelectedItem().toString()+"/");
				String dir="";
				ArrayList<TreeFilelistItem> tfl=createTreeFileList(mZipFileList, dir);
				mTreeFilelistAdapter.setDataList(tfl);
                mTreeFilelistAdapter.setZipArchiveFileName(mCurrentFilePath);
				mCurrentDirectory.setText("/");
				setTopUpButtonEnabled(false);
                mTreeFilelistView.setSelectionFromTop(dli.pos_x, dli.pos_y);
//				mTreeFilelistView.setSelectionFromTop(0, 0);
				setContextCopyCutPasteButton(mTreeFilelistAdapter);
			}
        });

        mFileListUp.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				if (!isUiEnabled()) return;
				String dir=mCurrentDirectory.getOriginalText().toString();
				if (!dir.equals("/")) {
					FileManagerDirectoryListItem dli=
							CommonUtilities.getDirectoryItem(mDirectoryList, mZipFileSpinner.getSelectedItem().toString()+dir);

					CommonUtilities.removeDirectoryItem(mDirectoryList, dli);
					String n_dir=dir.lastIndexOf("/")>0?dir.substring(1,dir.lastIndexOf("/")):"";
					FileManagerDirectoryListItem p_dli=
							CommonUtilities.getDirectoryItem(mDirectoryList, mZipFileSpinner.getSelectedItem().toString()+"/"+n_dir);
					ArrayList<TreeFilelistItem> tfl=createTreeFileList(mZipFileList, n_dir);
					mTreeFilelistAdapter.setDataList(tfl);
                    mTreeFilelistAdapter.setZipArchiveFileName(mCurrentFilePath);
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
                    mTreeFilelistAdapter.setZipArchiveFileName(mCurrentFilePath);
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
        final CustomContextMenu mCcMenu = new CustomContextMenu(mActivity.getResources(), mFragmentManager);
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

        if (sel_count==1) {
            mCcMenu.addMenuItem(mActivity.getString(R.string.msgs_main_local_file_ccmenu_property)+"("+(n_tfl.get(0)).getName()+")",R.drawable.dialog_information)
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
                                String prop= String.format(mActivity.getString(R.string.msgs_zip_zip_item_property_directory),
                                        "/"+tfi.getZipFileName(),
                                        item_cnt, item_comp_size, item_uncomp_size, comp_ratio);
                                CommonDialog.showCommonDialog(mFragmentManager, false, "I", mActivity.getString(R.string.msgs_main_local_file_ccmenu_property), prop, null);
                            } else {
                                ZipFileListItem zfli=getZipFileListItem(tfi.getZipFileName());
                                if (zfli!=null) {
                                    String comp_method=ZipUtil.getCompressionMethodName(zfli.getCompressionMethod());
                                    String enc_method="None";
                                    if (zfli.getEncryptionMethod()==ZipFileListItem.ENCRPTION_METHOD_AES) enc_method="AES";
                                    else if (zfli.getEncryptionMethod()==ZipFileListItem.ENCRPTION_METHOD_ZIP) enc_method="ZIP";
                                    long comp_size=zfli.getCompressedFileLength();
                                    long uncomp_size=zfli.getFileLength();
                                    long comp_ratio=0;
                                    if (uncomp_size!=0) comp_ratio=(comp_size*100)/uncomp_size;
                                    long last_mod=zfli.getLastModifiedTime();
                                    String enc_yes_no=zfli.isEncrypted()?
                                            mActivity.getString(R.string.msgs_zip_zip_item_property_encrypted_yes):
                                            mActivity.getString(R.string.msgs_zip_zip_item_property_encrypted_no);
                                    String prop= String.format(mActivity.getString(R.string.msgs_zip_zip_item_property_file),
                                            "/"+zfli.getPath(),
                                            StringUtil.convDateTimeTo_YearMonthDayHourMinSec(last_mod),
                                            comp_method, enc_method, comp_size, uncomp_size, comp_ratio);
                                    CommonDialog.showCommonDialog(mFragmentManager, false, "I", mActivity.getString(R.string.msgs_main_local_file_ccmenu_property), prop, null);
                                }
                            }

                        }
                    });
        }

        if (!tfi.isChecked()) {
            mCcMenu.addMenuItem(mActivity.getString(R.string.msgs_main_local_file_ccmenu_select)+"("+sel_list+")",R.drawable.menu_active)
                    .setOnClickListener(new CustomContextMenuOnClickListener() {
                @Override
                public void onClick(CharSequence menuTitle) {
                    tfi.setChecked(true);
                    mTreeFilelistAdapter.notifyDataSetChanged();
                }
            });
        }

        mCcMenu.addMenuItem(mActivity.getString(R.string.msgs_main_local_file_ccmenu_top),R.drawable.context_button_top)
                .setOnClickListener(new CustomContextMenuOnClickListener() {
            @Override
            public void onClick(CharSequence menuTitle) {
                mTreeFilelistView.setSelection(0);
            }
        });

        mCcMenu.addMenuItem(mActivity.getString(R.string.msgs_main_local_file_ccmenu_bottom),R.drawable.context_button_bottom)
                .setOnClickListener(new CustomContextMenuOnClickListener() {
            @Override
            public void onClick(CharSequence menuTitle) {
                mTreeFilelistView.setSelection(mTreeFilelistAdapter.getCount()-1);
            }
        });

		if (tfi.isDirectory() && sel_count==1) {
			mCcMenu.addMenuItem(mActivity.getString(R.string.msgs_main_local_file_ccmenu_open_directory)+
					"("+(n_tfl.get(0)).getName()+")",R.drawable.cc_folder)
	  			.setOnClickListener(new CustomContextMenuOnClickListener() {
				@Override
				public void onClick(CharSequence menuTitle) {
					String curr_dir=mCurrentDirectory.getOriginalText().toString().substring(1);
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
                    mTreeFilelistAdapter.setZipArchiveFileName(mCurrentFilePath);
					mCurrentDirectory.setText("/"+dir);
					if (tfl.size()>0) mTreeFilelistView.setSelection(0);
					setTopUpButtonEnabled(true);
					setContextCopyCutPasteButton(mTreeFilelistAdapter);
				}
	  		});
		}

		mCcMenu.addMenuItem(mActivity.getString(R.string.msgs_main_local_file_ccmenu_delete)+"("+sel_list+")",R.drawable.context_button_trash)
			.setOnClickListener(new CustomContextMenuOnClickListener() {
			@Override
			public void onClick(CharSequence menuTitle) {
				confirmDelete(tfa);
			}
		});

		mCcMenu.addMenuItem(mActivity.getString(R.string.msgs_main_local_file_ccmenu_extract)+"("+sel_list+")",R.drawable.context_button_extract)
			.setOnClickListener(new CustomContextMenuOnClickListener() {
			@Override
			public void onClick(CharSequence menuTitle) {
				extractDlg(tfa);
			}
		});
		if (isCopyCutDestValid(mCurrentFilePath, "")) {
			mCcMenu.addMenuItem(mActivity.getString(R.string.msgs_main_local_file_ccmenu_paste),R.drawable.context_button_paste)
		  		.setOnClickListener(new CustomContextMenuOnClickListener() {
				@Override
				public void onClick(CharSequence menuTitle) {
					pasteItem();
				}
		  	});
		}
		mCcMenu.addMenuItem(mActivity.getString(R.string.msgs_main_local_file_ccmenu_copy)+"("+sel_list+")",R.drawable.context_button_copy)
	  		.setOnClickListener(new CustomContextMenuOnClickListener() {
			@Override
			public void onClick(CharSequence menuTitle) {
				copyItem(tfa);
			}
	  	});

		mCcMenu.addMenuItem(mActivity.getString(R.string.msgs_main_local_file_ccmenu_cut)+"("+sel_list+")",R.drawable.context_button_cut)
	  		.setOnClickListener(new CustomContextMenuOnClickListener() {
			@Override
			public void onClick(CharSequence menuTitle) {
				cutItem(tfa);
			}
	  	});

		if (sel_count==1) {
			if (!tfi.isDirectory()) {
				mCcMenu.addMenuItem(mActivity.getString(R.string.msgs_main_local_file_ccmenu_force_zip)+"("+(n_tfl.get(0)).getName()+")",R.drawable.context_button_archive)
			  		.setOnClickListener(new CustomContextMenuOnClickListener() {
					@Override
					public void onClick(CharSequence menuTitle) {
						invokeBrowser(tfi.isZipEncrypted(), tfi, tfi.getPath(), tfi.getName(), MIME_TYPE_ZIP);
					}
			  	});
				mCcMenu.addMenuItem(mActivity.getString(R.string.msgs_main_local_file_ccmenu_force_text)+"("+(n_tfl.get(0)).getName()+")", R.drawable.cc_sheet)
		  			.setOnClickListener(new CustomContextMenuOnClickListener() {
					@Override
					public void onClick(CharSequence menuTitle) {
						invokeBrowser(tfi.isZipEncrypted(), tfi, tfi.getPath(), tfi.getName(), MIME_TYPE_TEXT);
					}
		  		});
			}
		}

        mCcMenu.addMenuItem(mActivity.getString(R.string.msgs_zip_zip_item_change_encoding))
                .setOnClickListener(new CustomContextMenuOnClickListener() {
                    @Override
                    public void onClick(CharSequence menuTitle) {
                        changeZipFileNameEncoding();
                    }
                });

        mCcMenu.createMenu();
	};

	private static InputStream buildBzip2InputStream(CustomZipFile zf, FileHeader fh, CommonUtilities cu) throws Exception {
        if (cu.getSettingLogLevel()>=2) {
            InputStream wis = (InputStream) zf.getInputStream(fh);
            byte[] buff=new byte[100];
            int rc=wis.read(buff);
            cu.addDebugMsg(1,"I","BZIP2 Compressed data (100 bytes from the beginning):\n"+StringUtil.getDumpFormatHexString(buff, 0, rc));
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
            cu.addDebugMsg(1,"I","DEFLATE64 Compressed data (100 bytes from the beginning):\n"+StringUtil.getDumpFormatHexString(buff, 0, rc));
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
            cu.addDebugMsg(1,"I","LZMA Compressed data (100 bytes from the beginning):\n"+StringUtil.getDumpFormatHexString(buff, 0, rc));
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
            cu.addDebugMsg(1,"I", "lzma_ver=0x"+StringUtil.getHexString(lzma_ver, 0, 2)+", lzma_prop_size="+lzma_prop_size+
                    ", propCode="+String.format("0x%h", props)+", uncomp_size="+fh.getUncompressedSize()+", dict_size="+dict_size);

        return new LZMAInputStream(bis, fh.getUncompressedSize(), props, dict_size);
    }

    public static InputStream getZipInputStream(ThreadCtrl tc, CustomZipFile zf, FileHeader fh, CommonUtilities cu) throws Exception {
        InputStream is=null;
        CompressionMethod cm=ZipUtil.getCompressionMethod(fh);
        if (!ZipUtil.isSupportedCompressionMethod(fh)) {
            throw new ZipException("Unsupported compression method. code="+ZipUtil.getCompressionMethodName(fh));
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

    public boolean copyZipItemToFile(ThreadCtrl tc, CustomZipFile zf, FileHeader fh, String zip_file_name,
                                     String dest_path, String dest_file_name) {
		boolean result=false;
		long b_time=System.currentTimeMillis();
		try {
			if (!isCancelled(true, tc)) {
                InputStream is=getZipInputStream(tc, zf, fh, mUtil);

				String dest_fpath=dest_path.endsWith("/")?dest_path+dest_file_name:dest_path+"/"+dest_file_name;
				SafFile3 out_dir_sf=new SafFile3(mActivity, dest_path);
                if (!out_dir_sf.exists()) out_dir_sf.mkdirs();
                final String msg_text=mActivity.getString(R.string.msgs_zip_extract_file_extracting);
                CallBackListener cbl=new CallBackListener() {
                    @Override
                    public void onCallBack(Context c, boolean positive, Object[] o2) {
                        putProgressMessage(String.format(msg_text, zip_file_name, (int)o2[0]));
                    }
                };
				if (out_dir_sf.getAppDirectoryCache()==null) {
                    SafFile3 out_file_work=new SafFile3(mActivity, dest_fpath+".tmp");
                    out_file_work.deleteIfExists();
                    out_file_work.createNewFile();
                    OutputStream os=out_file_work.getOutputStream();
                    copyFile(mActivity, tc, fh.getUncompressedSize(), is, os, cbl);
                    if (isCancelled(true, tc)) out_file_work.deleteIfExists();
                    else {
                        SafFile3 out_file_sf=new SafFile3(mActivity, dest_fpath);
                        boolean rc_rename=out_file_work.renameTo(out_file_sf);
                        if (!rc_rename && !out_file_work.exists() && out_file_sf.exists()) {
                            result=true;
                            ActivityMain.scanMediaFile(mGp, mUtil, out_file_sf.getPath());
                        }
                    }
                } else {
				    String work_fpath=out_dir_sf.getAppDirectoryCache()+"/"+dest_file_name;//System.currentTimeMillis();
                    SafFile3 out_file_work=new SafFile3(mActivity, work_fpath);
                    File out_os_file=new File(work_fpath);
                    OutputStream os=new FileOutputStream(out_os_file);
                    copyFile(mActivity, tc, fh.getUncompressedSize(), is, os, cbl);
                    if (isCancelled(true, tc)) out_file_work.deleteIfExists();
                    else {
                        SafFile3 out_file_sf=new SafFile3(mActivity, dest_fpath);
                        out_os_file.setLastModified(ZipUtil.dosToJavaTme((int) fh.getLastModifiedTime()));
                        out_file_sf.deleteIfExists();
                        out_file_work.moveTo(out_file_sf);
                        result=true;
                        ActivityMain.scanMediaFile(mGp, mUtil, out_file_sf.getPath());
                    }
                }
			}
		} catch (Exception e) {
			mUtil.addLogMsg("I", e.getMessage());
			CommonUtilities.printStackTraceElement(mUtil, e.getStackTrace());
			tc.setThreadMessage(e.getMessage());
            result=false;
		}
		mUtil.addDebugMsg(1,"I",
				"copyZipItemToFile result="+result+", zip file name="+zip_file_name+", dest="+dest_path+
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
		final String work_dir=mActivity.getExternalCacheDirs()[0].getPath()+"/"+WORK_DIRECTORY;
//		String fid=CommonUtilities.getFileExtention(f_name);
		String w_mt=LocalFileManager.getMimeTypeFromFileExtention(mGp, f_name);
		final String mt=mime_type.equals("")?w_mt:mime_type;
        try {
            final CustomZipFile zf=createZipFile(mActivity, mCurrentFilePath, mEncodingSelected);
            final String e_name=p_dir.equals("")?f_name:p_dir+"/"+f_name;
            final FileHeader fh=zf.getFileHeader(e_name);
            NotifyEvent ntfy_pswd=new NotifyEvent(mActivity);
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
                    mActivity.createDialogCancelListener(tc);
                    setUiDisabled();
                    showDialogProgress();
                    Thread th=new Thread(){
                        @Override
                        public void run() {
                            putProgressMessage(String.format(mActivity.getString(R.string.msgs_zip_specific_extract_file_extracting),f_name));
                            File ef=new File(work_dir+"/"+f_name);
                            boolean extract_rc=true;
                            if (ef.exists()) {
                                if (ef.lastModified()!=tfli.getLastModified() || ef.length()!=tfli.getLength()) {
                                    extract_rc= copyZipItemToFile(tc, zf, fh, e_name, work_dir, f_name);
                                    ef.setLastModified(tfli.getLastModified());
                                }
                            } else {
                                extract_rc= copyZipItemToFile(tc, zf, fh, e_name, work_dir, f_name);
                                ef.setLastModified(tfli.getLastModified());
                            }
                            final boolean rc=extract_rc;
                            mUiHandler.post(new Runnable(){
                                @Override
                                public void run() {
                                    setUiEnabled();
                                    if (rc && !isCancelled(tc)) {
                                        SafFile3 sf=null;
                                        try {
                                            String fp=(work_dir+"/"+f_name).replaceAll("//","/");
                                            sf=new SafFile3(mActivity, fp);
                                            Intent intent = new Intent(Intent.ACTION_VIEW);
                                            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION |Intent.FLAG_GRANT_WRITE_URI_PERMISSION);// | Intent.FLAG_ACTIVITY_NEW_TASK);
                                            Uri uri=null;
                                            if (sf.isSafFile()) {
                                                uri=sf.getUri();
                                            } else {
                                                uri= FileProvider.getUriForFile(mActivity, PACKAGE_NAME+".provider", new File(fp));
                                            }
                                            if (mt==null) intent.setDataAndType(uri, "*/*");
                                            else intent.setDataAndType(uri, mt);
                                            mActivity.startActivity(intent);
                                        } catch(ActivityNotFoundException e) {
                                            CommonDialog.showCommonDialog(mFragmentManager, false,"E",
                                                    String.format(mActivity.getString(R.string.msgs_zip_specific_extract_file_viewer_not_found),f_name,mt),"",null);
                                            if (sf!=null) sf.deleteIfExists();
                                        }
                                    } else {
                                        if (!isCancelled(tc))
                                            CommonDialog.showCommonDialog(mFragmentManager, false, "E", mActivity.getString(R.string.msgs_zip_extract_file_was_failed),
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
                if (ZipUtil.isSupportedCompressionMethod(fh)) getZipPasswordDlg(mActivity, mGp, mMainPassword, zf, fh, ntfy_pswd, false);
                else {
                    CommonDialog.showCommonDialog(mFragmentManager, false, "E", mActivity.getString(R.string.msgs_zip_extract_file_error),
                            "Unsupported compression method. code="+ ZipUtil.getCompressionMethodName(fh), null);
                }
            } else {
                ntfy_pswd.notifyToListener(true, null);
            }
        } catch (Exception e) {
            mUtil.addLogMsg("I", e.getMessage());
            CommonUtilities.printStackTraceElement(mUtil, e.getStackTrace());
        }

	};

}
