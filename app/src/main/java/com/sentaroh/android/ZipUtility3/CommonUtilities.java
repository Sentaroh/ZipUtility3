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
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.media.MediaScannerConnection;
import android.net.wifi.WifiManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;

import com.sentaroh.android.Utilities3.Dialog.CommonDialog;
import com.sentaroh.android.Utilities3.NotifyEvent;
import com.sentaroh.android.Utilities3.SafFile3;
import com.sentaroh.android.Utilities3.SystemInfo;
import com.sentaroh.android.Utilities3.ThreadCtrl;
import com.sentaroh.android.ZipUtility3.Log.LogUtil;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;

import static com.sentaroh.android.ZipUtility3.Constants.DEFAULT_PREFS_FILENAME;
import static com.sentaroh.android.ZipUtility3.Constants.WORK_DIRECTORY;

public final class CommonUtilities {
	private Context mContext=null;

   	private LogUtil mLog=null;
   	
   	private GlobalParameters mGp=null;
   	
   	@SuppressWarnings("unused")
	private String mLogIdent="";

    private CommonDialog mCommonDlg=null;

	public CommonUtilities(Context c, String li, GlobalParameters gp, CommonDialog cd) {
		mContext=c;// ContextはApplicationContext
		mLog=new LogUtil(c, li);
		mLogIdent=li;
        mGp=gp;
        mCommonDlg=cd;
	}

	final public SharedPreferences getPrefMgr() {
    	return getPrefMgr(mContext);
    }

//	static public void scanMediaFile(GlobalParameters gp, CommonUtilities util, String fp) {
//		MediaScannerConnection.scanFile(gp.appContext, new String[]{fp}, null, null);
//		if (gp.settingDebugLevel>=2) util.addDebugMsg(2, "I","Media scanner invoked, name="+fp);
//	};
//
//	static public void scanLocalFile(GlobalParameters gp, CommonUtilities util, ThreadCtrl tc, File scan_item) {
//		if (!tc.isEnabled()) return;
//		if (scan_item.exists()) {
//			if (scan_item.isDirectory()) {
//				File[] scan_list=scan_item.listFiles();
//				if (scan_list!=null && scan_list.length>0) {
//					for(File child_item:scan_list) {
//						if (!tc.isEnabled()) return;
//						scanLocalFile(gp, util, tc, child_item);
//					}
//				}
//			} else {
////				if (queryMediaStoreFile(gp.appContext,scan_item.getPath())<1)
//					scanMediaFile(gp, util, scan_item.getPath());
//			}
//		}
//	};
	
	final static public void getAllFileInDirectory(GlobalParameters gp, CommonUtilities util,
                                                   ArrayList<SafFile3> fl, SafFile3 lf, boolean process_sub_directories) {
		if (lf.isDirectory()) {
			SafFile3[] cfl=lf.listFiles();
			if (cfl!=null && cfl.length>0) {
				for(SafFile3 cf:cfl) {
					if (cf.isDirectory()) {
						if (!cf.getName().equals(".thumbnails")) {
							if (process_sub_directories) 
								getAllFileInDirectory(gp, util, fl, cf, process_sub_directories);
						}
					} else {
						fl.add(cf);
					}
				}
			}
		} else {
			fl.add(lf);
		}
	};
	
	static public boolean deleteLocalFile(SafFile3 del_item) {
		boolean result=false;
		if (del_item.exists()) {
			if (del_item.isDirectory()) {
				SafFile3[] del_list=del_item.listFiles();
				if (del_list!=null && del_list.length>0) {
					for(SafFile3 child_item:del_list) {
						result=deleteLocalFile(child_item);
						if (!result) break;
					}
					if (result) result=del_item.delete(); 
				} else {
					result=del_item.delete(); 
				}
			} else {
				result=del_item.delete();
			}
		}
		return result;
	};

	public static void setSpinnerBackground(Context c, Spinner spinner, boolean theme_is_light) {
		if (theme_is_light) spinner.setBackground(c.getDrawable(R.drawable.spinner_color_background_light));
		else spinner.setBackground(c.getDrawable(R.drawable.spinner_color_background));
	};

	@SuppressWarnings("deprecation")
	@SuppressLint("InlinedApi")
	final static public SharedPreferences getPrefMgr(Context c) {
    	return c.getSharedPreferences(DEFAULT_PREFS_FILENAME, Context.MODE_PRIVATE| Context.MODE_MULTI_PROCESS);
    }

	final public void setLogId(String li) {
		mLog.setLogId(li);
	};
	
	public static void printStackTraceElement(CommonUtilities ut, StackTraceElement[] ste) {
		for (int i=0;i<ste.length;i++) {
			ut.addLogMsg("E","",ste[i].toString());	
		}
	};

	public static String formatStackTraceInfo(Exception e) {
        final StringWriter sw = new StringWriter();
        final PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        pw.flush();
        pw.close();
        return sw.toString();
    }

	@SuppressLint("DefaultLocale")
	final static public String getFileExtention(String fp) {
		String fid="";
		if (fp.lastIndexOf(".") > 0) {
			fid = fp.substring(fp.lastIndexOf(".") + 1).toLowerCase();
		}
		return fid;
	};
	
	final static public String getExecutedMethodName() {
		String name = Thread.currentThread().getStackTrace()[3].getMethodName();
		return name;
	}

	final public void resetLogReceiver() {
		mLog.resetLogReceiver();
	};

	final public void flushLog() {
		mLog.flushLog();
	};

	final public void rotateLogFile() {
		mLog.rotateLogFile();
	};

    final public void deleteLogFile() {
    	mLog.deleteLogFile();
	};

	public String buildPrintMsg(String cat, String... msg) {
		return mLog.buildPrintLogMsg(cat, msg);
	};
	
	final public void addLogMsg(String cat, String... msg) {
		mLog.addLogMsg(cat, msg); 
	};
	final public void addDebugMsg(int lvl, String cat, String... msg) {
		mLog.addDebugMsg(lvl, cat, msg);
	};

	final public boolean isLogFileExists() {
		boolean result = false;
		result=mLog.isLogFileExists();
		addDebugMsg(3,"I","Log file exists="+result);
		return result;
	};

    final public int getSettingLogLevel() {
        return mLog.getLogLevel();
    };

    final public boolean getSettingsLogOption() {
		boolean result = false;
		result=getPrefMgr().getBoolean(mContext.getString(R.string.settings_log_option), false);
		addDebugMsg(2,"I","LogOption="+result);
		return result;
	};

	static public long getSettingsParmSaveDate(Context c, String dir, String fn) {
		File lf=new File(dir+"/"+fn);
		long result=0;
		if (lf.exists()) {
			result=lf.lastModified();
		} else {
			result=-1;
		}
		return result;
	};
	
	public boolean isDebuggable() {
        PackageManager manager = mContext.getPackageManager();
        ApplicationInfo appInfo = null;
        try {
            appInfo = manager.getApplicationInfo(mContext.getPackageName(), 0);
        } catch (NameNotFoundException e) {
            return false;
        }
        if ((appInfo.flags & ApplicationInfo.FLAG_DEBUGGABLE) == ApplicationInfo.FLAG_DEBUGGABLE)
            return true;
        return false;
    };
	
	public void initAppSpecificExternalDirectory(Context c) {
//		if (Build.VERSION.SDK_INT>=19) {
//			c.getExternalFilesDirs(null);
//		} else {
//		}
//		ContextCompat.getExternalFilesDirs(c, null);
        c.getExternalFilesDirs(null);
	};
	
	public boolean isWifiActive() { 
		boolean ret=false;
		WifiManager mWifi =(WifiManager)mContext.getSystemService(Context.WIFI_SERVICE);
		if (mWifi.isWifiEnabled()) ret=true;
		addDebugMsg(2,"I","isWifiActive WifiEnabled="+ret);
		return ret;
	};

	public String getConnectedWifiSsid() {
		String ret="";
		WifiManager mWifi =(WifiManager)mContext.getSystemService(Context.WIFI_SERVICE);
		String ssid="";
		if (mWifi.isWifiEnabled()) {
			ssid=mWifi.getConnectionInfo().getSSID();
			if (ssid!=null && 
					!ssid.equals("0x") &&
					!ssid.equals("<unknown ssid>") &&
					!ssid.equals("")) ret=ssid;
//			Log.v("","ssid="+ssid);
		}
		addDebugMsg(2,"I","getConnectedWifiSsid WifiEnabled="+mWifi.isWifiEnabled()+
				", SSID="+ssid+", result="+ret);
		return ret;
	};

    static public void setCheckedTextView(final CheckedTextView ctv) {
		ctv.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				ctv.toggle();
			}
		});
	}

	static public FileManagerDirectoryListItem getDirectoryItem(ArrayList<FileManagerDirectoryListItem> mDirectoryList,
                                                                String fp) {
		for(FileManagerDirectoryListItem dli:mDirectoryList) {
			if (fp.equals(dli.file_path)) return dli;
		}
		return null;
	};
	static public void addDirectoryItem(ArrayList<FileManagerDirectoryListItem> mDirectoryList, FileManagerDirectoryListItem item) {
		mDirectoryList.add(item);
	};
	static public void removeDirectoryItem(ArrayList<FileManagerDirectoryListItem> mDirectoryList, FileManagerDirectoryListItem item) {
		mDirectoryList.remove(item);
	};
	static public void clearDirectoryItem(ArrayList<FileManagerDirectoryListItem> mDirectoryList, String base_path) {
		ArrayList<FileManagerDirectoryListItem> dl=new ArrayList<FileManagerDirectoryListItem>();
		for(FileManagerDirectoryListItem dli:mDirectoryList) {
			if (dli.file_path.startsWith(base_path)) dl.add(dli);
		}
		for(FileManagerDirectoryListItem item:dl) {
			mDirectoryList.remove(item);
		}
	}

	static public void sortFileList(final ActivityMain mActivity, final GlobalParameters mGp,
                                    final CustomTreeFilelistAdapter tfa, final NotifyEvent p_ntfy) {
		if (tfa==null) return;
		final Dialog dialog = new Dialog(mActivity, mGp.applicationTheme);
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialog.setContentView(R.layout.select_sort_dlg);
		
		final LinearLayout dlg_view = (LinearLayout) dialog.findViewById(R.id.select_sort_dlg_view);
//		dlg_view.setBackgroundResource(R.drawable.dialog_border_dark);
		
		final LinearLayout title_view = (LinearLayout) dialog.findViewById(R.id.select_sort_dlg_title_view);
		title_view.setBackgroundColor(mGp.themeColorList.title_background_color);
		final TextView dlg_title = (TextView) dialog.findViewById(R.id.select_sort_dlg_title);
		dlg_title.setTextColor(mGp.themeColorList.title_text_color);

		final Button btnCancel = (Button) dialog.findViewById(R.id.select_sort_dlg_cancel_btn);
		final Button btnOk = (Button) dialog.findViewById(R.id.select_sort_dlg_ok_btn);
		
		CommonDialog.setDlgBoxSizeLimit(dialog, false);
		
//		final RadioGroup rg_sort_order=(RadioGroup)dialog.findViewById(R.id.select_sort_dlg_rg_sort_order);
		final RadioButton rb_sort_order_asc=(RadioButton)dialog.findViewById(R.id.select_sort_dlg_rb_sort_order_asc);
		final RadioButton rb_sort_order_dsc=(RadioButton)dialog.findViewById(R.id.select_sort_dlg_rb_sort_order_dsc);

//		final RadioGroup rg_sort_key=(RadioGroup)dialog.findViewById(R.id.select_sort_dlg_rg_sort_key);
		final RadioButton rb_sort_key_name=(RadioButton)dialog.findViewById(R.id.select_sort_dlg_rb_sort_key_name);
		final RadioButton rb_sort_key_size=(RadioButton)dialog.findViewById(R.id.select_sort_dlg_rb_sort_key_size);
		final RadioButton rb_sort_key_time=(RadioButton)dialog.findViewById(R.id.select_sort_dlg_rb_sort_key_time);
		
		if (tfa.isSortAscendant()) {
			rb_sort_order_asc.setChecked(true);
		} else {
			rb_sort_order_dsc.setChecked(true);
		}
		if (tfa.isSortKeyName()) {
			rb_sort_key_name.setChecked(true);
		} else if (tfa.isSortKeySize()) {
			rb_sort_key_size.setChecked(true);
		} else if (tfa.isSortKeyTime()) {
			rb_sort_key_time.setChecked(true);
		}
		
//		rg_sort_order.setOnCheckedChangeListener(new OnCheckedChangeListener(){
//			@Override
//			public void onCheckedChanged(RadioGroup group, int checkedId) {
//				performSortFileList(dialog,tfa);
//			}
//		});
//		
//		rg_sort_order.setOnCheckedChangeListener(new OnCheckedChangeListener(){
//			@Override
//			public void onCheckedChanged(RadioGroup group, int checkedId) {
//				performSortFileList(dialog,tfa);
//			}
//		});
		
		// Ok�{�^���̎w��
		btnOk.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				performSortFileList(mActivity, dialog, tfa);
				if (p_ntfy!=null) p_ntfy.notifyToListener(true, null);
				mActivity.refreshOptionMenu();
				dialog.dismiss();
			}
		});

		// CANCEL�{�^���̎w��
		btnCancel.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				dialog.dismiss();
			}
		});

		dialog.show();
	}
	
	static private void performSortFileList(ActivityMain mActivity, Dialog dialog, CustomTreeFilelistAdapter tfa) {
//		final RadioGroup rg_sort_order=(RadioGroup)dialog.findViewById(R.id.select_sort_dlg_rg_sort_order);
		final RadioButton rb_sort_order_asc=(RadioButton)dialog.findViewById(R.id.select_sort_dlg_rb_sort_order_asc);
//		final RadioButton rb_sort_order_dsc=(RadioButton)dialog.findViewById(R.id.select_sort_dlg_rb_sort_order_dsc);

//		final RadioGroup rg_sort_key=(RadioGroup)dialog.findViewById(R.id.select_sort_dlg_rg_sort_key);
		final RadioButton rb_sort_key_name=(RadioButton)dialog.findViewById(R.id.select_sort_dlg_rb_sort_key_name);
		final RadioButton rb_sort_key_size=(RadioButton)dialog.findViewById(R.id.select_sort_dlg_rb_sort_key_size);
		final RadioButton rb_sort_key_time=(RadioButton)dialog.findViewById(R.id.select_sort_dlg_rb_sort_key_time);
		
		if(rb_sort_key_name.isChecked()) tfa.setSortKeyName();
		else if(rb_sort_key_size.isChecked()) tfa.setSortKeySize();
		else if(rb_sort_key_time.isChecked()) tfa.setSortKeyTime();
		
		if (rb_sort_order_asc.isChecked()) tfa.setSortAscendant();
		else tfa.setSortDescendant();
		tfa.sort();
		tfa.notifyDataSetChanged();
		mActivity.refreshOptionMenu();
	}

}
