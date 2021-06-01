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
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;

import com.sentaroh.android.Utilities3.NotifyEvent;
import com.sentaroh.android.Utilities3.ThemeColorList;
import com.sentaroh.android.Utilities3.ThemeUtil;
import com.sentaroh.android.Utilities3.Widget.NonWordwrapTextView;
import com.sentaroh.android.Utilities3.Zip.ZipUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class CustomTreeFilelistAdapter extends BaseAdapter {
    private static Logger log= LoggerFactory.getLogger(CustomTreeFilelistAdapter.class);

	private Activity mActivity;
//	private ArrayList<Integer>mShowItems=new ArrayList<Integer>();
	private ArrayList<TreeFilelistItem> mDataItems=null;
	private boolean mSingleSelectMode=false;
	private boolean mShowLastModified=true;
	private boolean mSetColor=true;
	private int[] mIconImage= new int[] {R.drawable.cc_expanded,
			R.drawable.cc_expanded,
			R.drawable.cc_folder,
			R.drawable.cc_sheet,
			R.drawable.cc_blank};
	
	private ThemeColorList mThemeColorList;
	
	private NotifyEvent mNotifyExpand=null;

	private boolean mCheckBoxEnabled=true;
	
	public void setCheckBoxEnabled(boolean p) {mCheckBoxEnabled=p;}
	public boolean isCheckBoxEnabled() {return mCheckBoxEnabled;}

	private GlobalParameters mGp=null;
	
	public CustomTreeFilelistAdapter(Activity c) {
		mActivity = c;
		mDataItems=new ArrayList<TreeFilelistItem>();
		initTextColor();
		
	};

	public CustomTreeFilelistAdapter(Activity c,
                                     boolean singleSelectMode, boolean showLastModified) {
		mActivity = c;
		this.mSingleSelectMode=singleSelectMode;
		this.mShowLastModified=showLastModified;
		mDataItems=new ArrayList<TreeFilelistItem>();
		initTextColor();
	};

	public CustomTreeFilelistAdapter(Activity c,
                                     boolean singleSelectMode, boolean showLastModified, boolean set_color) {
		mActivity = c;
		this.mSingleSelectMode=singleSelectMode;
		this.mShowLastModified=showLastModified;
		mDataItems=new ArrayList<TreeFilelistItem>();
		mSetColor=set_color;
		initTextColor();
	};

	private void initTextColor() {
	    mGp=GlobalWorkArea.getGlobalParameters(mActivity);
		mThemeColorList=mGp.themeColorList;
	}

	private String mZipArchiveFileName="";
	public void setZipArchiveFileName(String archive_name) {mZipArchiveFileName=archive_name;}

	@Override
	public int getCount() {return mDataItems.size();}

	@Override
	public TreeFilelistItem getItem(int arg0) {return mDataItems.get(arg0);}

	@Override
	public long getItemId(int arg0) {return arg0;}

    synchronized public ArrayList<TreeFilelistItem> getDataList() {return mDataItems;}
	
	public void setDataList(ArrayList<TreeFilelistItem> fl) {
        synchronized (mDataItems) {
            mDataItems=fl;
            sort();
        }
	};

	public void setShowLastModified(boolean p) {
		mShowLastModified=p;
	};

	public void setSingleSelectMode(boolean p) {
		mSingleSelectMode=p;
	};

//	public void setItemIsSelected(int pos) {
//		if (mSingleSelectMode) {
//			setAllItemUnchecked();
//			mDataItems.get(pos).setChecked(true);
//		} else {
//			mDataItems.get(pos).setChecked(!mDataItems.get(pos).isChecked());
//		}
//        notifyDataSetChanged();
//	};

//	public void setItemIsUnselected(int pos) {
//		mDataItems.get(pos).setChecked(false);
//        notifyDataSetChanged();
//	};

	public boolean isItemSelected() {
		boolean result=false;
		
		for (int i=0;i<mDataItems.size();i++) {
			if (mDataItems.get(i).isChecked()) {
				result=true;
				break;
			}
		}
		return result;
	};

	public boolean isAllItemSelected() {
		boolean result=true;
		
		for (int i=0;i<mDataItems.size();i++) {
			if (!mDataItems.get(i).isChecked()) {
				result=false;
				break;
			}
		}
		return result;
	};

	public int getSelectedItemCount() {
		int result=0;
		
		for (int i=0;i<mDataItems.size();i++) {
			if (mDataItems.get(i).isChecked()) {
				result++;
			}
		}
		return result;
	};

	public void setAllItemUnchecked() {
		for (int i=0;i<mDataItems.size();i++) 
			mDataItems.get(i).setChecked(false); 
	};

	public void setAllItemChecked(boolean checked) {
		for (int i=0;i<mDataItems.size();i++)
		    if (TreeFilelistItem.isSelectableItem(mDataItems.get(i))) mDataItems.get(i).setChecked(checked);
	};

	public boolean isSingleSelectMode() {
		return mSingleSelectMode;
	};

    synchronized public void removeItem(int dc) {
		mDataItems.remove(dc);
		notifyDataSetChanged();
	};

    synchronized public void removeItem(TreeFilelistItem fi) {
		mDataItems.remove(fi);
		notifyDataSetChanged();
	};

    synchronized public void replaceItem(int i, TreeFilelistItem fi) {
		mDataItems.set(i,fi);
		notifyDataSetChanged();
	};

    synchronized public void addItem(TreeFilelistItem fi) {
        synchronized (mDataItems) {
            mDataItems.add(fi);
            notifyDataSetChanged();
        }
	};

    synchronized public void insertItem(int i, TreeFilelistItem fi) {
        synchronized (mDataItems) {
            mDataItems.add(i,fi);
            notifyDataSetChanged();
        }
	};

	private boolean mSortAscendant=true;
    synchronized public void setSortAscendant() {
		mSortAscendant=true;
	};

    synchronized public void setSortDescendant() {
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

	public int getSortKey() {
	    return mSortKey;
    }

    public void setSortKey(int key_value) {
	    Thread.dumpStack();
        mSortKey=key_value;
    }

    final static public int SORT_KEY_NAME=0;
	final static public int SORT_KEY_TIME=1;
	final static public int SORT_KEY_SIZE=2;
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

    public void sort() {
        synchronized (mDataItems) {
            if (mSortKey==SORT_KEY_NAME) {
                sortByName(mDataItems,mSortAscendant);
            } else if (mSortKey==SORT_KEY_TIME) {
                sortByTime(mDataItems,mSortAscendant);
            } else if (mSortKey==SORT_KEY_SIZE) {
                sortBySize(mDataItems,mSortAscendant);
            }
            notifyDataSetChanged();
        }

	};

//    @Override
//    public void notifyDataSetChanged() {
//        synchronized (mDataItems) {
//            log.info("notifyDataSetChanged entered");
//            super.notifyDataSetChanged();
//        }
//
//    };

    public static void sortByName(ArrayList<TreeFilelistItem> data_list, final boolean orderAsc) {
        ArrayList<TreeFilelistItem>dir_list=new ArrayList<TreeFilelistItem>();
        ArrayList<TreeFilelistItem>file_list=new ArrayList<TreeFilelistItem>();
        for(TreeFilelistItem item:data_list) {
            if (item.isDirectory()) dir_list.add(item);
            else file_list.add(item);
        }
		if (orderAsc) {
			Collections.sort(dir_list, new Comparator<TreeFilelistItem>(){
				@Override
				public int compare(TreeFilelistItem lhs, TreeFilelistItem rhs) {
                    return lhs.getName().compareToIgnoreCase(rhs.getName());
				}
			});
            Collections.sort(file_list, new Comparator<TreeFilelistItem>(){
                @Override
                public int compare(TreeFilelistItem lhs, TreeFilelistItem rhs) {
                    return lhs.getName().compareToIgnoreCase(rhs.getName());
                }
            });
		} else {
            Collections.sort(dir_list, new Comparator<TreeFilelistItem>(){
                @Override
                public int compare(TreeFilelistItem lhs, TreeFilelistItem rhs) {
                    return rhs.getName().compareToIgnoreCase(lhs.getName());
                }
            });
            Collections.sort(file_list, new Comparator<TreeFilelistItem>(){
                @Override
                public int compare(TreeFilelistItem lhs, TreeFilelistItem rhs) {
                    return rhs.getName().compareToIgnoreCase(lhs.getName());
                }
            });
		}
        data_list.clear();
        data_list.addAll(dir_list);
        data_list.addAll(file_list);
	};

	public static void sortBySize(ArrayList<TreeFilelistItem> list, final boolean orderAsc) {
		if (orderAsc) {
			Collections.sort(list, new Comparator<TreeFilelistItem>(){
				@Override
				public int compare(TreeFilelistItem lhs, TreeFilelistItem rhs) {
				    long diff=lhs.getLength()-rhs.getLength();
				    if (diff==0) return 0;
				    else if (diff>0) return 1;
				    else return -1;
//					return lhs.getSortKeySize().compareToIgnoreCase(rhs.getSortKeySize());
				}
			});
		} else {
			Collections.sort(list, new Comparator<TreeFilelistItem>(){
				@Override
				public int compare(TreeFilelistItem lhs, TreeFilelistItem rhs) {
                    long diff=rhs.getLength()-lhs.getLength();
                    if (diff==0) return 0;
                    else if (diff>0) return 1;
                    else return -1;
//					return rhs.getSortKeySize().compareToIgnoreCase(lhs.getSortKeySize());
				}
			});
		}
	};

	public static void sortByTime(ArrayList<TreeFilelistItem> list, final boolean orderAsc) {
		if (orderAsc) {
			Collections.sort(list, new Comparator<TreeFilelistItem>(){
				@Override
				public int compare(TreeFilelistItem lhs, TreeFilelistItem rhs) {
                    long diff=lhs.getLastModified()-rhs.getLastModified();
                    if (diff==0) return 0;
                    else if (diff>0) return 1;
                    else return -1;
//					return lhs.getSortKeyTime().compareToIgnoreCase(rhs.getSortKeyTime());
				}
			});
		} else {
			Collections.sort(list, new Comparator<TreeFilelistItem>(){
				@Override
				public int compare(TreeFilelistItem lhs, TreeFilelistItem rhs) {
                    long diff=rhs.getLastModified()-lhs.getLastModified();
                    if (diff==0) return 0;
                    else if (diff>0) return 1;
                    else return -1;
//					return rhs.getSortKeyTime().compareToIgnoreCase(lhs.getSortKeyTime());
				}
			});
		}
	};

	private NotifyEvent cb_ntfy=null;
	public void setCbCheckListener(NotifyEvent ntfy) {
		cb_ntfy=ntfy;
	}

	public void unsetCbCheckListener() {
		cb_ntfy=null;
	}

	private boolean enableListener=true;
	
	@Override
	public boolean isEnabled(int p) {
	    if (getItem(p).canRead()) {
            if (p<getCount()) return getItem(p).isEnableItem();
            else return false;
        } else {
            return false;
        }
	}

	final public static int TEXT_COLOR_CUT_LIGHT=Color.RED;
    final public static int TEXT_COLOR_CUT_NORMAL=Color.RED;
    final public static int TEXT_COLOR_COPY_LIGHT=0xdd0000ff;
    final public static int TEXT_COLOR_COPY_NORMAL=0xdd00ff00;

	private Drawable mDefaultBgColor=null;
	private ColorStateList mDefaultTextColor=null;
	@Override
    public View getView(int position, View convertView, ViewGroup parent) {
		 	final ViewHolder holder;
		 	
            View v = convertView;
            if (v == null) {
                LayoutInflater vi = (LayoutInflater) mActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = vi.inflate(R.layout.custom_tree_file_list_item, null);
                holder=new ViewHolder();

            	holder.cb_cb1=(CheckBox)v.findViewById(R.id.tree_file_list_checkbox);
            	holder.rb_rb1=(RadioButton)v.findViewById(R.id.tree_file_list_radiobtn);
            	holder.ll_expand_view=(LinearLayout)v.findViewById(R.id.tree_file_list_expand_view);
            	holder.tv_spacer=(TextView)v.findViewById(R.id.tree_file_list_spacer);
            	holder.iv_expand=(ImageView)v.findViewById(R.id.tree_file_list_expand);
            	holder.ll_expand_view.setVisibility(LinearLayout.GONE);
            	holder.iv_image1=(ImageView)v.findViewById(R.id.tree_file_list_icon);
            	holder.tv_name=(NonWordwrapTextView)v.findViewById(R.id.tree_file_list_name);
            	holder.tv_size=(TextView)v.findViewById(R.id.tree_file_list_size);
                holder.tv_count=(TextView)v.findViewById(R.id.tree_file_list_count);
                holder.tv_comp_info=(TextView)v.findViewById(R.id.tree_file_list_zip_comp_info);
            	holder.tv_moddate=(TextView)v.findViewById(R.id.tree_file_list_date);
            	holder.tv_modtime=(TextView)v.findViewById(R.id.tree_file_list_time);
        		holder.ll_view=(LinearLayout)v.findViewById(R.id.tree_file_list_view);
            	
        		holder.ll_select_view=(LinearLayout)v.findViewById(R.id.tree_file_list_select_view);
        		holder.ll_date_time_view=(LinearLayout)v.findViewById(R.id.tree_file_list_date_time_view);
                if (mDefaultBgColor==null) mDefaultBgColor=holder.ll_view.getBackground();
                if (mDefaultTextColor==null) mDefaultTextColor=holder.tv_name.getTextColors();
            	if (ThemeUtil.isLightThemeUsed(mActivity)) {

            		if (mSetColor) {
//                    	holder.tv_spacer.setBackgroundColor(mThemeColorList.dialog_msg_background_color);
//                    	holder.iv_expand.setBackgroundColor(mThemeColorList.dialog_msg_background_color);
//                    	holder.iv_image1.setBackgroundColor(mThemeColorList.dialog_msg_background_color);
//                    	holder.tv_name.setBackgroundColor(mThemeColorList.dialog_msg_background_color);
//                    	holder.tv_size.setBackgroundColor(mThemeColorList.dialog_msg_background_color);
//                    	holder.tv_moddate.setBackgroundColor(mThemeColorList.dialog_msg_background_color);
//                    	holder.tv_modtime.setBackgroundColor(mThemeColorList.dialog_msg_background_color);
//
//                    	holder.ll_date_time_view.setBackgroundColor(mThemeColorList.dialog_msg_background_color);
                		holder.ll_view.setBackgroundDrawable(mDefaultBgColor);
            		}

            	}
            	v.setTag(holder);
            } else {
         	   holder= (ViewHolder)v.getTag();
            }
            v.setEnabled(true);
            final TreeFilelistItem o = mDataItems.get(position);
            if (o != null) {
                boolean selectable=TreeFilelistItem.isSelectableItem(o);
                setCheckBoxEnabled(selectable);
            	if (o.isEnableItem()) {
	            	holder.cb_cb1.setEnabled(true);
            		holder.rb_rb1.setEnabled(true);
	            	holder.tv_spacer.setEnabled(true);
	            	holder.iv_expand.setEnabled(true);
	            	holder.iv_image1.setEnabled(true);
	            	holder.tv_name.setEnabled(true);
	            	holder.tv_size.setEnabled(true);
            	} else {
            		holder.cb_cb1.setEnabled(false);
            		holder.rb_rb1.setEnabled(true);
	            	holder.tv_spacer.setEnabled(false);
	            	holder.iv_expand.setEnabled(false);
	            	holder.iv_image1.setEnabled(false);
	            	holder.tv_name.setEnabled(false);
	            	holder.tv_size.setEnabled(false);
            	}
        		if (mSingleSelectMode) {
        			holder.cb_cb1.setVisibility(CheckBox.GONE);
        			holder.rb_rb1.setVisibility(RadioButton.VISIBLE);
        		} else {
        			holder.cb_cb1.setVisibility(CheckBox.VISIBLE);
        			holder.rb_rb1.setVisibility(RadioButton.GONE);
        		}

                holder.tv_spacer.setWidth(o.getListLevel()*30);
                holder.tv_name.setTextColor(mDefaultTextColor);
                holder.tv_name.setText(o.getName());
                holder.tv_size.setVisibility(TextView.VISIBLE);
                holder.tv_count.setVisibility(TextView.VISIBLE);
                if (o.getLength()!=-1) {
                    holder.tv_size.setText(o.getFileSize());
                    holder.tv_count.setVisibility(TextView.VISIBLE);
                } else {
//                	    holder.tv_size.setText(String.format("%3d Item",o.getSubDirItemCount()));
                    holder.tv_size.setText(mActivity.getString(R.string.msgs_file_list_size_calculating));
//                        holder.tv_count.setVisibility(TextView.GONE);
                }
                if (o.isDirectory()) {
                    if (!o.isZipFileItem()) holder.tv_size.setVisibility(TextView.VISIBLE);
                    else holder.tv_size.setVisibility(TextView.GONE);
                } else {
                    holder.tv_count.setVisibility(TextView.GONE);
                }
                if (mShowLastModified) {
                    holder.tv_moddate.setText(o.getFileLastModDate());
                    holder.tv_modtime.setText(o.getFileLastModTime());
                    if (o.isDirectory()) holder.tv_count.setText(String.format("%3d Item",o.getSubDirItemCount()));
                } else {
                    holder.tv_moddate.setVisibility(TextView.GONE);
                    holder.tv_modtime.setVisibility(TextView.GONE);
                }
                int disabled_color=mThemeColorList.theme_is_light?0xffa0a0a0:Color.GRAY;

                if (!selectable) {
                    holder.cb_cb1.setVisibility(CheckBox.INVISIBLE);
                    holder.rb_rb1.setVisibility(RadioButton.GONE);
                }
                if (!o.isHidden()) {
                    if (o.isEnableItem()) {
                        if (o.canRead()) {
                            if (o.isZipEncrypted()) {
                                holder.tv_name.setTextColor(mThemeColorList.text_color_warning);//normal_text_color);
                                holder.tv_size.setTextColor(mThemeColorList.text_color_warning);//normal_text_color);
                                holder.tv_count.setTextColor(mThemeColorList.text_color_warning);//Color.GRAY);
                                holder.tv_comp_info.setTextColor(mThemeColorList.text_color_warning);//Color.GRAY);
                                holder.tv_moddate.setTextColor(mThemeColorList.text_color_warning);//normal_text_color);
                                holder.tv_modtime.setTextColor(mThemeColorList.text_color_warning);//normal_text_color);
                            } else {
                                holder.tv_name.setTextColor(mDefaultTextColor);//normal_text_color);
                                holder.tv_size.setTextColor(mDefaultTextColor);//normal_text_color);
                                holder.tv_count.setTextColor(mDefaultTextColor);//Color.GRAY);
                                holder.tv_comp_info.setTextColor(mDefaultTextColor);//Color.GRAY);
                                holder.tv_moddate.setTextColor(mDefaultTextColor);//normal_text_color);
                                holder.tv_modtime.setTextColor(mDefaultTextColor);//normal_text_color);
                            }
                        } else {
                            holder.tv_name.setTextColor(disabled_color);//Color.GRAY);
                            holder.tv_size.setTextColor(disabled_color);//Color.GRAY);
                            holder.tv_count.setTextColor(disabled_color);//Color.GRAY);
                            holder.tv_comp_info.setTextColor(disabled_color);//Color.GRAY);
                            holder.tv_moddate.setTextColor(disabled_color);//Color.GRAY);
                            holder.tv_modtime.setTextColor(disabled_color);//Color.GRAY);
                            holder.cb_cb1.setVisibility(CheckBox.INVISIBLE);
                            holder.rb_rb1.setVisibility(RadioButton.GONE);
                        }
                    } else {
                        holder.tv_name.setTextColor(disabled_color);//Color.GRAY);
                        holder.tv_size.setTextColor(disabled_color);//Color.GRAY);
                        holder.tv_count.setTextColor(disabled_color);//Color.GRAY);
                        holder.tv_comp_info.setTextColor(disabled_color);//Color.GRAY);
                        holder.tv_moddate.setTextColor(disabled_color);//Color.GRAY);
                        holder.tv_modtime.setTextColor(disabled_color);//Color.GRAY);
                    }
                } else {
                    holder.tv_name.setTextColor(disabled_color);//Color.GRAY);
                    holder.tv_size.setTextColor(disabled_color);//Color.GRAY);
                    holder.tv_count.setTextColor(disabled_color);//Color.GRAY);
                    holder.tv_comp_info.setTextColor(disabled_color);//Color.GRAY);
                    holder.tv_moddate.setTextColor(disabled_color);//Color.GRAY);
                    holder.tv_modtime.setTextColor(disabled_color);//Color.GRAY);
                }
                if (mGp.copyCutList.size()>0) {
                    if (mZipArchiveFileName.equals("")) {
                        for(TreeFilelistItem item:mGp.copyCutList) {
                            if (item.isDirectory()) {
                                if ((o.getPath()+"/"+o.getName()+"/").startsWith(item.getPath()+"/"+item.getName()+"/")) {
                                    if (mGp.themeIsLight) {
                                        if (mGp.copyCutModeIsCut) holder.tv_name.setTextColor(TEXT_COLOR_CUT_LIGHT);
                                        else holder.tv_name.setTextColor(TEXT_COLOR_COPY_LIGHT);//Color.CYAN);
                                    } else {
                                        if (mGp.copyCutModeIsCut) holder.tv_name.setTextColor(TEXT_COLOR_CUT_LIGHT);
                                        else holder.tv_name.setTextColor(TEXT_COLOR_COPY_NORMAL);//Color.GREEN);
                                    }
                                }
                            } else {
                                if ((o.getPath()+"/"+o.getName()).equals(item.getPath()+"/"+item.getName())) {
                                    if (mGp.themeIsLight) {
                                        if (mGp.copyCutModeIsCut) holder.tv_name.setTextColor(TEXT_COLOR_CUT_LIGHT);
                                        else holder.tv_name.setTextColor(TEXT_COLOR_COPY_LIGHT);//Color.CYAN);
                                    } else {
                                        if (mGp.copyCutModeIsCut) holder.tv_name.setTextColor(TEXT_COLOR_CUT_LIGHT);
                                        else holder.tv_name.setTextColor(TEXT_COLOR_COPY_NORMAL);//Color.GREEN);
                                    }
                                }
                            }
                        }
                    } else {
                        if (mZipArchiveFileName.equals(mGp.copyCutFilePath)) {
                            String o_fp="";
                            if (o.isDirectory()) o_fp=o.getPath().equals("")?o.getName()+"/":o.getPath()+"/"+o.getName()+"/";
                            else o_fp=o.getPath().equals("")?o.getName():o.getPath()+"/"+o.getName();
                            for(TreeFilelistItem item:mGp.copyCutList) {
                                if (item.isDirectory()) {
                                    String item_fp=item.getPath().equals("")?item.getName()+"/":item.getPath()+"/"+item.getName()+"/";
                                    if (o_fp.startsWith(item_fp)) {
                                        if (mGp.themeIsLight) {
                                            if (mGp.copyCutModeIsCut) holder.tv_name.setTextColor(Color.RED);
                                            else holder.tv_name.setTextColor(0xdd0000ff);//Color.CYAN);
                                        } else {
                                            if (mGp.copyCutModeIsCut) holder.tv_name.setTextColor(Color.RED);
                                            else holder.tv_name.setTextColor(0xdd00ff00);//Color.GREEN);
                                        }
                                    }
                                } else {
                                    if ((o.getPath()+"/"+o.getName()).equals(item.getPath()+"/"+item.getName())) {
                                        if (mGp.themeIsLight) {
                                            if (mGp.copyCutModeIsCut) holder.tv_name.setTextColor(Color.RED);
                                            else holder.tv_name.setTextColor(0xdd0000ff);//Color.CYAN);
                                        } else {
                                            if (mGp.copyCutModeIsCut) holder.tv_name.setTextColor(Color.RED);
                                            else holder.tv_name.setTextColor(0xdd00ff00);//Color.GREEN);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                if(o.isDirectory()) {
                    holder.tv_comp_info.setText("");
                    if (o.getLength()!=-1) holder.tv_count.setVisibility(TextView.VISIBLE);
                    if (o.getSubDirItemCount()>0) {
                        if (o.isEnableItem()) {
                            if (o.isChildListExpanded())
                                holder.iv_expand.setImageResource(mIconImage[0]);//expanded
                            else holder.iv_expand.setImageResource(mIconImage[1]);//collapsed
                        } else holder.iv_expand.setImageResource(mIconImage[4]); //blank
                    } else {
                        holder.iv_expand.setImageResource(mIconImage[4]); //blank
                    }
                    holder.iv_image1.setImageResource(mIconImage[2]); //folder
                } else {
                    if (o.isZipFileItem()) {
                        String cm= ZipUtil.getCompressionMethodName(o.getZipFileCompressionMethod());
                        String comp_ratio="";
                        if (o.getZipFileCompressionMethod()!=TreeFilelistItem.ZIP_COMPRESSION_METHOD_STORE) {
                            if (o.getLength()>0) comp_ratio=(o.getZipFileCompressedSize()*100)/o.getLength()+"%";
                        }
                        holder.tv_comp_info.setText(comp_ratio+" "+cm);
                    } else {
                        holder.tv_comp_info.setText("");
                    }
                    holder.tv_count.setVisibility(TextView.GONE);
                    if (o.getMimeType().startsWith("image")) {
                        holder.iv_image1.setImageResource(R.drawable.ic_32_file_image);
                    } else if (o.getMimeType().startsWith("audio")) {
                        holder.iv_image1.setImageResource(R.drawable.ic_32_file_music);
                    } else if (o.getMimeType().startsWith("video")) {
                        holder.iv_image1.setImageResource(R.drawable.ic_32_file_movie);
                    } else {
                        if (o.getFileExtention().equalsIgnoreCase("zip")) {
                            holder.iv_image1.setImageResource(R.drawable.ic_32_file_zip);
//               				} else if (o.getFileExtention().equals("doc") || o.getFileExtention().equals("docx")) {
//               					holder.iv_image1.setImageResource(R.drawable.ic_32_file_doc);
//               				} else if (o.getFileExtention().equals("xls") || o.getFileExtention().equals("xlsx")) {
//               					holder.iv_image1.setImageResource(R.drawable.ic_32_file_xls);
//               				} else if (o.getFileExtention().equals("ppt") || o.getFileExtention().equals("pptx")) {
//               					holder.iv_image1.setImageResource(R.drawable.ic_32_file_ppt);
                        } else if (o.getFileExtention().equalsIgnoreCase("pdf")) {
                            holder.iv_image1.setImageResource(R.drawable.ic_32_file_pdf);
                        } else {
                            holder.iv_image1.setImageResource(mIconImage[3]); //sheet
                        }
                    }
                    holder.iv_expand.setImageResource(mIconImage[4]); //blank
                }

            	if (o.isChecked()) {
            		if (ThemeUtil.isLightThemeUsed(mActivity)) holder.ll_view.setBackgroundColor(Color.CYAN);
            		else holder.ll_view.setBackgroundColor(Color.GRAY);
                    holder.tv_name.setTextColor(mDefaultTextColor);//normal_text_color);
                    holder.tv_size.setTextColor(mDefaultTextColor);//normal_text_color);
                    holder.tv_count.setTextColor(mDefaultTextColor);//Color.GRAY);
                    holder.tv_comp_info.setTextColor(mDefaultTextColor);//Color.GRAY);
                    holder.tv_moddate.setTextColor(mDefaultTextColor);//normal_text_color);
                    holder.tv_modtime.setTextColor(mDefaultTextColor);//normal_text_color);
            	} else {
            		holder.ll_view.setBackgroundDrawable(mDefaultBgColor);
            	}
               	final int p = position;
               	if (mNotifyExpand!=null) {
               		holder.iv_expand.setOnClickListener(new OnClickListener() {
    					@Override
    					public void onClick(View v) {
    						mNotifyExpand.notifyToListener(true, new Object[]{p});
    					}
       				});
               		holder.ll_expand_view.setOnClickListener(new OnClickListener() {
    					@Override
    					public void onClick(View v) {
    						mNotifyExpand.notifyToListener(true, new Object[]{p});
    					}
       				});
               	}

               	holder.cb_cb1.setEnabled(isCheckBoxEnabled());
           		holder.cb_cb1.setOnCheckedChangeListener(new OnCheckedChangeListener() {
					public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
						setButton(o,p,isChecked);
						notifyDataSetChanged();
  					}
   				});
                holder.rb_rb1.setEnabled(isCheckBoxEnabled());
           		holder.rb_rb1.setOnCheckedChangeListener(new OnCheckedChangeListener() {
					public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
						setButton(o,p,isChecked);
						notifyDataSetChanged();
  					}
   				});
           		if (mSingleSelectMode) {
           			if (o.isChecked()) {
    					TreeFilelistItem fi;
    					for (int i=0;i<mDataItems.size();i++) {
    						fi=mDataItems.get(i);
    						if (TreeFilelistItem.isSelectableItem(fi)) {
                                if (fi.isChecked() && p!=i) {
                                    fi.setChecked(false);
                                }
                            }
    					}
           			}
           			holder.rb_rb1.setChecked(mDataItems.get(position).isChecked());
           		} else {
           		    holder.cb_cb1.setChecked(mDataItems.get(position).isChecked());
                }

            }
            return v;
    };

    private void setButton(TreeFilelistItem o,int p, boolean isChecked) {
		if (enableListener) {
			enableListener=false;
				if (mSingleSelectMode) {
					if (isChecked) {
						TreeFilelistItem fi;
						for (int i=0;i<mDataItems.size();i++) {
							fi=mDataItems.get(i);
							if (fi.isChecked() && p!=i) {
								fi.setChecked(false);
//								replaceDataItem(i,fi);
							}
						}
					}
				}else{ 
				}
				enableListener=true;
		}
		boolean c_chk=o.isChecked();
		o.setChecked(isChecked);

		if (cb_ntfy!=null) cb_ntfy.notifyToListener(isChecked, new Object[]{p, c_chk});

    };
    
	static class ViewHolder {
		 NonWordwrapTextView tv_name;
         TextView tv_moddate, tv_modtime, tv_size, tv_spacer, tv_count, tv_comp_info;
		 ImageView iv_image1;
		 ImageView iv_expand;
		 LinearLayout ll_view, ll_date_time_view, ll_expand_view, ll_select_view;
		 CheckBox cb_cb1;
		 RadioButton rb_rb1;
	}
}
