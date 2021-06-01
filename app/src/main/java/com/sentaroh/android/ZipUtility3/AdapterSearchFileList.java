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

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.sentaroh.android.Utilities3.ThemeColorList;
import com.sentaroh.android.Utilities3.ThemeUtil;
import com.sentaroh.android.Utilities3.Zip.ZipFileListItem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class AdapterSearchFileList extends BaseAdapter {
	private Activity mActivity;
	private ArrayList<TreeFilelistItem> mDataItems=null;
	private ThemeColorList mThemeColorList;
	
	public AdapterSearchFileList(Activity c) {
		mActivity = c;
		mDataItems=new ArrayList<TreeFilelistItem>();
		initTextColor();
		
	};
	private void initTextColor() {
		mThemeColorList=ThemeUtil.getThemeColorList(mActivity);
	}
	
	@Override
	public int getCount() {return mDataItems.size();}

	@Override
	public TreeFilelistItem getItem(int arg0) {return mDataItems.get(arg0);}

	@Override
	public long getItemId(int arg0) {return 0;}
	
	@Override
	public boolean isEnabled(int p) {
		return true;
	};

	public ArrayList<TreeFilelistItem> getDataList() {
		return mDataItems;
	};
	
	private boolean mSortAscendant=true;
	public void setSortAscendant() {
		mSortAscendant=true;
	};
	
	public void setSortDescendant() {
		mSortAscendant=false;
	};
	
	public boolean isSortAscendant() {
		return mSortAscendant;
	};

	public boolean isSortKeyName() {
		return mSortKey==SORT_KEY_NAME;
	};

	public boolean isSortKeySize() {
		return mSortKey==SORT_KEY_SIZE;
	};

	public boolean isSortKeyTime() {
		return mSortKey==SORT_KEY_TIME;
	};

	final static private int SORT_KEY_NAME=0;
	final static private int SORT_KEY_TIME=1;
	final static private int SORT_KEY_SIZE=2;
	private int mSortKey=SORT_KEY_NAME;
	public void setSortKeyName() {
		mSortKey=SORT_KEY_NAME;
	};

	public void setSortKeyTime() {
		mSortKey=SORT_KEY_TIME;
	};

	public void setSortKeySize() {
		mSortKey=SORT_KEY_SIZE;
	};
	
	public void setDataList(ArrayList<TreeFilelistItem> p) {
		mDataItems=p;
		sort();
	}

    public void sort() {
        synchronized (mDataItems) {
            if (mSortKey==SORT_KEY_NAME) {
                CustomTreeFilelistAdapter.sortByName(mDataItems,mSortAscendant);
            } else if (mSortKey==SORT_KEY_TIME) {
                CustomTreeFilelistAdapter.sortByTime(mDataItems,mSortAscendant);
            } else if (mSortKey==SORT_KEY_SIZE) {
                CustomTreeFilelistAdapter.sortBySize(mDataItems,mSortAscendant);
            }
        }

    };

//    public void sort() {
//
//		Collections.sort(mDataItems, new Comparator<TreeFilelistItem>(){
//			@Override
//			public int compare(TreeFilelistItem lhs, TreeFilelistItem rhs) {
//				String l_key="", r_key="";
//				if (mSortKey==SORT_KEY_NAME) {
////					l_key=lhs.getName();
////					r_key=rhs.getName();
//					l_key=lhs.getSortKeyName();
//					r_key=rhs.getSortKeyName();
//				} else if (mSortKey==SORT_KEY_TIME) {
//					l_key=lhs.getSortKeyTime();
//					r_key=rhs.getSortKeyTime();
//				} else if (mSortKey==SORT_KEY_SIZE) {
//					l_key=lhs.getSortKeySize();
//					r_key=rhs.getSortKeySize();
//				}
//				if (mSortAscendant) return l_key.compareToIgnoreCase(r_key);
//				else return r_key.compareToIgnoreCase(l_key);
//			}
//		});
//	};

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
	 	final ViewHolder holder;
	 	
        View v = convertView;
        if (v == null) {
            LayoutInflater vi = (LayoutInflater)mActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = vi.inflate(R.layout.search_file_result_list_item, null);
            holder=new ViewHolder();

            holder.result_view=(LinearLayout)v.findViewById(R.id.search_file_result_list_item_view);
        	holder.file_name=(TextView)v.findViewById(R.id.search_file_result_list_item_file_name);
        	holder.directory_name=(TextView)v.findViewById(R.id.search_file_result_list_item_directory_name);
        	holder.file_size=(TextView)v.findViewById(R.id.search_file_result_list_item_file_size);
        	holder.comp_method=(TextView)v.findViewById(R.id.search_file_result_list_item_comp_method);
        	holder.file_date=(TextView)v.findViewById(R.id.search_file_result_list_item_last_modified_date);
        	holder.file_time=(TextView)v.findViewById(R.id.search_file_result_list_item_last_modified_time);
//        	if (ThemeUtil.isLightThemeUsed(mActivity)) {
////    			holder.result_view.setBackgroundColor(mThemeColorList.dialog_msg_background_color);
//        	}

//        	if (normal_text_color==-1) normal_text_color=holder.tv_name.getCurrentTextColor();
//        	Log.v("","n="+String.format("0x%08x",holder.tv_name.getCurrentTextColor()));
        	v.setTag(holder); 
        } else {
     	   holder= (ViewHolder)v.getTag();
        }
        v.setEnabled(true);
        final TreeFilelistItem o = mDataItems.get(position);
        if (o != null) {
        	holder.file_name.setText(o.getName());
        	holder.directory_name.setText(o.getPath());
        	String[] cap1 = new String[3];
        	holder.file_size.setText(String.format("%1$,3d", o.getLength())+ "Byte, ");
        	String comp_method="";
			if (o.getZipFileCompressionMethod()== ZipFileListItem.COMPRESSION_METHOD_DEFLATE) comp_method="DEFLATE, ";
			else if (o.getZipFileCompressionMethod()==ZipFileListItem.COMPRESSION_METHOD_STORE) comp_method="STORE, ";
			if (o.getZipFileEncryptionMethod()==ZipFileListItem.ENCRPTION_METHOD_AES) comp_method="AES, ";
            else if (o.getZipFileEncryptionMethod()==ZipFileListItem.ENCRPTION_METHOD_ZIP) comp_method="ZIP, ";
//			else comp_method+=o.getZipFileCompressionMethod()+", ";
			holder.comp_method.setText(comp_method);
			if (o.getZipFileCompressionMethod()<0) holder.comp_method.setVisibility(TextView.GONE);
			else holder.comp_method.setVisibility(TextView.VISIBLE);

        	holder.file_date.setText(o.getFileLastModDate());
        	holder.file_time.setText(o.getFileLastModTime());

        }
        return v;
	}

	class ViewHolder {
		public LinearLayout result_view;
		public TextView file_name, directory_name, file_size, file_time, file_date, comp_method;
	}
}
