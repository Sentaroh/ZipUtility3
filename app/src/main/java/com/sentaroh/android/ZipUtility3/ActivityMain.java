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

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.StrictMode;
import android.os.storage.StorageManager;
import android.text.InputType;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;

import com.sentaroh.android.Utilities3.AppUncaughtExceptionHandler;
import com.sentaroh.android.Utilities3.Dialog.CommonDialog;
import com.sentaroh.android.Utilities3.MiscUtil;
import com.sentaroh.android.Utilities3.NotifyEvent;
import com.sentaroh.android.Utilities3.NotifyEvent.NotifyEventListener;
import com.sentaroh.android.Utilities3.SafFile3;
import com.sentaroh.android.Utilities3.SafManager3;
import com.sentaroh.android.Utilities3.SystemInfo;
import com.sentaroh.android.Utilities3.ThemeUtil;
import com.sentaroh.android.Utilities3.ThreadCtrl;
import com.sentaroh.android.Utilities3.Widget.CustomTabLayout;
import com.sentaroh.android.Utilities3.Widget.CustomViewPager;
import com.sentaroh.android.Utilities3.Widget.CustomViewPagerAdapter;
import com.sentaroh.android.Utilities3.Widget.NonWordwrapTextView;
import com.sentaroh.android.ZipUtility3.Log.LogManagementFragment;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import static com.sentaroh.android.Utilities3.SafFile3.SAF_FILE_PRIMARY_UUID;
import static com.sentaroh.android.Utilities3.SafFile3.SAF_FILE_UNKNOWN_UUID;
import static com.sentaroh.android.ZipUtility3.Constants.APPLICATION_TAG;
import static com.sentaroh.android.ZipUtility3.GlobalParameters.COPY_CUT_FROM_LOCAL;

@SuppressLint("NewApi")
public class ActivityMain extends AppCompatActivity {
	private static Logger log= LoggerFactory.getLogger(ActivityMain.class);

	private GlobalParameters mGp=null;
	
	private boolean mTerminateApplication=false;
    private final static int START_STATUS_START_NORMAL = 0;
    private final static int START_STATUS_START_COMPLETED = 1;
    private final static int START_STATUS_START_INPROGRESS = 2;
    private int mRestartStatus=START_STATUS_START_NORMAL;
    private boolean mRestartRestoreReqired=false;

	private CommonDialog mCommonDlg=null;

	private FragmentManager mFragmentManager=null;
	
	private Context mContext;
	private ActivityMain mActivity;
	
	private CommonUtilities mUtil=null;

	private ActionBar mActionBar=null;

	private LocalFileManager mLocalFileMgr=null;
	private ZipFileManager mZipFileMgr=null;

	private Handler mUiHandler=null;

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(new GlobalParameters().setNewLocale(base, true));
    }

    @Override
	public void onConfigurationChanged(final Configuration newConfig) {
	    super.onConfigurationChanged(newConfig);
	    if (mUtil!=null) {
	    	mUtil.addDebugMsg(1,"I",CommonUtilities.getExecutedMethodName()+" Entered, " ,
	    			"New orientation="+newConfig.orientation+
	    			", New language=",newConfig.locale.getLanguage());
//	    	if (mLocalFileMgr!=null) mLocalFileMgr.refreshFileList();
	    }
	    if (mLocalFileMgr!=null) mLocalFileMgr.reInitView();
        if (mZipFileMgr!=null) mZipFileMgr.reInitView();
	};

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		mUtil.addDebugMsg(1, "I", "onSaveInstanceState entered");
		
		saveViewContents(outState);
	};  

	private void saveViewContents(Bundle outState) {
	};
	
	@Override
	protected void onRestoreInstanceState(Bundle savedState) {
		super.onRestoreInstanceState(savedState);
		mUtil.addDebugMsg(1, "I", "onRestoreInstanceState entered");
		restoreViewContents(savedState);
        mRestartRestoreReqired=true;
	};

	private void restoreViewContents(Bundle savedState) {
	};
	
	@Override
	protected void onNewIntent(Intent in) {
		super.onNewIntent(in);
		mUtil.addDebugMsg(1, "I", "onNewIntent entered, restartStatus="+mRestartStatus);
		if (mRestartStatus!=START_STATUS_START_COMPLETED) return;
		if (in!=null && in.getData()!=null) showZipFileByIntent(in);
	};



	@Override
    public void onCreate(Bundle savedInstanceState) {
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());

        mContext=ActivityMain.this;
        mActivity=ActivityMain.this;
        mUiHandler=new Handler();
        mFragmentManager=getSupportFragmentManager();
        mRestartStatus=START_STATUS_START_NORMAL;
       	mGp=GlobalWorkArea.getGlobalParameters(mContext);
        setTheme(mGp.applicationTheme);
        mGp.themeColorList= ThemeUtil.getThemeColorList(mActivity);
        super.onCreate(savedInstanceState);

		mActionBar=getSupportActionBar();
		mActionBar.setDisplayShowHomeEnabled(false);
		mActionBar.setHomeButtonEnabled(false);

        mCommonDlg=new CommonDialog(mActivity, mFragmentManager);

        mUtil=new CommonUtilities(mContext, "ZipActivity", mGp, mCommonDlg);
        
        mUtil.addDebugMsg(1, "I", "onCreate entered");

        MyUncaughtExceptionHandler myUncaughtExceptionHandler = new MyUncaughtExceptionHandler();
        myUncaughtExceptionHandler.init(mContext, myUncaughtExceptionHandler);

        putSystemInfo();

        if (mGp.settingFixDeviceOrientationToPortrait) setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        else setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        setContentView(R.layout.main_screen);

        createTabView();

        mGp.copyCutList=new ArrayList<TreeFilelistItem>();
    	mGp.copyCutModeIsCut=false;

        cleanupCacheFile();

    };

    private class MyUncaughtExceptionHandler extends AppUncaughtExceptionHandler {
        @Override
        public void appUniqueProcess(Throwable ex, String strace) {
            log.error("UncaughtException detected, error="+ex);
            log.error(strace);
            mUtil.flushLog();
        }
    };

    private void putSystemInfo() {
        ArrayList<String> sil= SystemInfo.listSystemInfo(mContext, mGp.safMgr);
        for(String item:sil) mUtil.addDebugMsg(1,"I",item);
    }

	@Override
	public void onStart() {
		super.onStart();
		mUtil.addDebugMsg(1, "I", "onStart entered");
	};

	@Override
	public void onRestart() {
		super.onStart();
		mUtil.addDebugMsg(1, "I", "onRestart entered");
	};

	@Override
	public void onResume() {
		super.onResume();
		mUtil.addDebugMsg(1, "I", "onResume entered, restartStatus="+mRestartStatus);

        NotifyEvent ntfy_resume=new NotifyEvent(mContext);
        ntfy_resume.setListener(new NotifyEvent.NotifyEventListener() {
            @Override
            public void positiveResponse(Context context, Object[] objects) {
                processOnResumed();
            }
            @Override
            public void negativeResponse(Context context, Object[] objects) { }
        });
        if (isLegacyStorageAccessGranted()) ntfy_resume.notifyToListener(true, null);
        else {
            if (mRestartStatus==START_STATUS_START_NORMAL) {
                checkLegacyStoragePermissions(ntfy_resume);
                mRestartStatus=START_STATUS_START_INPROGRESS;
            }
        }
    }

	private void processOnResumed() {
        if (mRestartStatus==START_STATUS_START_COMPLETED) {
            if (isUiEnabled()) {
                if (mTabLayout.getSelectedTabName().equals(mContext.getString(R.string.msgs_main_tab_name_local))) mLocalFileMgr.refreshFileList();
                else mZipFileMgr.refreshFileList();
            }
            try {
                mSvcClient.aidlSetActivityInForeground();
            } catch (RemoteException e) {
                e.printStackTrace();
            }

        } else {
            NotifyEvent ntfy=new NotifyEvent(mContext);
            ntfy.setListener(new NotifyEvent.NotifyEventListener(){
                @Override
                public void positiveResponse(Context c, Object[] o) {
                    try {
                        mSvcClient.aidlSetActivityInForeground();
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    if (mLocalFileMgr==null) {
                        mLocalFileMgr=new LocalFileManager(mGp, mActivity, mFragmentManager, mLocalView);
                        mLocalFileMgr.showLocalFileView(false);
                        mZipFileMgr=new ZipFileManager(mGp, mActivity, mFragmentManager, mZipView);
                    }
                    if (!mRestartRestoreReqired) {
                        Intent in=getIntent();
                        if (in!=null && in.getData()!=null) showZipFileByIntent(in);
                        else {
                            mLocalFileMgr.showLocalFileView(true);
                            showAddExternalStorageNotification();
                        }
                    } else {
                        mLocalFileMgr.showLocalFileView(true);
                        if (mGp.activityIsDestroyed) {
                            mCommonDlg.showCommonDialog(false, "W", getString(R.string.msgs_main_restart_by_destroyed),"",null);
                        } else {
                            mCommonDlg.showCommonDialog(false, "W", getString(R.string.msgs_main_restart_by_killed),"",null);
                        }
                    }
                    mRestartStatus=START_STATUS_START_COMPLETED;
                    mGp.activityIsDestroyed=false;
                }
                @Override
                public void negativeResponse(Context c, Object[] o) {
                }

            });
            openService(ntfy);
        }

    }

    private void showAddExternalStorageNotification() {
        mUtil.addDebugMsg(1, "I", CommonUtilities.getExecutedMethodName()+
                " entered, isStoragePermissionRequired="+mGp.safMgr.isStoragePermissionRequired()+
                ", isSupressAddExternalStorageNotification"+mGp.isSupressAddExternalStorageNotification());
        if (mGp.safMgr.isStoragePermissionRequired() && !mGp.isSupressAddExternalStorageNotification()) {
            NotifyEvent ntfy=new NotifyEvent(mContext);
            ntfy.setListener(new NotifyEvent.NotifyEventListener() {
                @Override
                public void positiveResponse(Context context, Object[] objects) {
                    boolean suppress=(boolean)objects[0];
                    if (suppress) {
                        mGp.setSupressAddExternalStorageNotification(mContext, true);
                    }
                    NotifyEvent ntfy_add=new NotifyEvent(mContext);
                    ntfy_add.setListener(new NotifyEvent.NotifyEventListener() {
                        @Override
                        public void positiveResponse(Context context, Object[] objects) {
                        }

                        @Override
                        public void negativeResponse(Context context, Object[] objects) {

                        }
                    });
                    requestLocalStoragePermission(ntfy_add);
                }

                @Override
                public void negativeResponse(Context context, Object[] objects) {
                    boolean suppress=(boolean)objects[0];
                    if (suppress) {
                        mGp.setSupressAddExternalStorageNotification(mContext, true);
                    }
                }
            });
            ArrayList<SafManager3.StorageVolumeInfo>svl=SafManager3.buildStoragePermissionRequiredList(mContext);
            String new_storage="";
            for(SafManager3.StorageVolumeInfo si:svl) {
                new_storage+=si.description+"("+si.uuid+")"+"\n";
            }
            showDialogWithHideOption(mActivity, mGp, mUtil,
                    true, mContext.getString(R.string.msgs_common_dialog_ok),
                    true, mContext.getString(R.string.msgs_common_dialog_close),
                    mContext.getString(R.string.msgs_main_suppress_add_external_storage_notification_title),
                    mContext.getString(R.string.msgs_main_suppress_add_external_storage_notification_msg)+"\n-"+new_storage,
                    mContext.getString(R.string.msgs_main_suppress_add_external_storage_notification_suppress), ntfy);
        }

    }

    static public void showDialogWithHideOption(final Activity activity, final GlobalParameters gp, CommonUtilities cu,
                                                boolean ok_visible, String ok_label, boolean cancel_visible, String cancel_label,
                                                String title_text, String msg_text, String suppress_text,
                                                NotifyEvent p_ntfy) {

        final Context c=activity.getApplicationContext();

        final Dialog dialog = new Dialog(activity, gp.applicationTheme);//, android.R.style.Theme_Black);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.show_warning_message_dlg);

        final LinearLayout title_view = (LinearLayout) dialog.findViewById(R.id.show_warning_message_dlg_title_view);
        final TextView title = (TextView) dialog.findViewById(R.id.show_warning_message_dlg_title);
        title_view.setBackgroundColor(gp.themeColorList.title_background_color);
        title.setText(title_text);
        title.setTextColor(gp.themeColorList.title_text_color);

        ((TextView) dialog.findViewById(R.id.show_warning_message_dlg_msg)).setText(msg_text);

        final Button btnOk = (Button) dialog.findViewById(R.id.show_warning_message_dlg_close);
        btnOk.setText(ok_label);
        btnOk.setVisibility(ok_visible?Button.VISIBLE:Button.GONE);
        final Button btnCancel = (Button) dialog.findViewById(R.id.show_warning_message_dlg_cancel);
        btnCancel.setText(cancel_label);
        btnCancel.setVisibility(cancel_visible?Button.VISIBLE:Button.GONE);
        final CheckedTextView ctvSuppr = (CheckedTextView) dialog.findViewById(R.id.show_warning_message_dlg_ctv_suppress);
        ctvSuppr.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                ((CheckedTextView)v).toggle();
            }
        });
        ctvSuppr.setText(suppress_text);

        CommonDialog.setDlgBoxSizeCompact(dialog);
        ctvSuppr.setChecked(false);

        btnOk.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                dialog.dismiss();
                p_ntfy.notifyToListener(true, new Object[]{ctvSuppr.isChecked()});
            }
        });
        btnCancel.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                dialog.dismiss();
                p_ntfy.notifyToListener(false, new Object[]{ctvSuppr.isChecked()});
            }
        });

        dialog.setOnCancelListener(new Dialog.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface arg0) {
                btnCancel.performClick();
            }
        });
        dialog.show();
    }

	@Override
	public void onPause() {
		super.onPause();
		mUtil.addDebugMsg(1, "I", "onPause entered");
        // Application process is follow
		
	};

	@Override
	public void onStop() {
		super.onStop();
		mUtil.addDebugMsg(1, "I", "onStop entered");
        // Application process is follow
		try {
			if (!isUiEnabled()) mSvcClient.aidlSetActivityInBackground();
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	};

	@Override
	public void onDestroy() {
		super.onDestroy();
		mUtil.addDebugMsg(1, "I", "onDestroy entered");
        // Application process is follow
		closeService();
		if (mTerminateApplication) {
//			mGp.settingExitClean=true;

            cleanupCacheFile();

		} else {
			mGp.activityIsDestroyed=true;
		}
	};
	
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (keyCode) {
			case KeyEvent.KEYCODE_BACK:
				if (isUiEnabled()) {
//					if (mMainTabHost.getCurrentTab()==0) {//Local tab
                    if (mTabLayout.getSelectedTabPosition()==0) {//Local tab
					    if (mLocalFileMgr.isFileListSelected()) {
					        mLocalFileMgr.setFileListAllItemUnselected();
                            return true;
                        } else if (mLocalFileMgr.isUpButtonEnabled()) {
							mLocalFileMgr.performClickUpButton();
							return true;
						}
					} else {//Zip folder
                        if (mZipFileMgr.isFileListSelected()) {
                            mZipFileMgr.setFileListAllItemUnselected();
                            return true;
                        } else if (mZipFileMgr.isUpButtonEnabled()) {
							mZipFileMgr.performClickUpButton();
							return true;
						} else {
//							mZipFileMgr.
						}
					}
					mTerminateApplication=true;
					confirmExit();
				} else {
					Intent in=new Intent();
					in.setAction(Intent.ACTION_MAIN);
					in.addCategory(Intent.CATEGORY_HOME);
					startActivity(in);
				}
				return true;
				// break;
			default:
				return super.onKeyDown(keyCode, event);
				// break;
		}
	};

	public ZipFileManager getZipFileManager() {
	    return mZipFileMgr;
    }

    public LocalFileManager getLocalFileManager() {
        return mLocalFileMgr;
    }

    private void cleanupCacheFile() {
        File[] fl=mContext.getExternalCacheDirs();
        if (fl!=null && fl.length>0) {
            for(File cf:fl) {
                if (cf!=null) {
                    File[] child_list=cf.listFiles();
                    if (child_list!=null) {
                        for(File ch_item:child_list) {
                            if (ch_item!=null) {
                                if (!deleteCacheFile(ch_item)) break;
                            }
                        }
                    }
                }
            }
        } else {
            fl=mContext.getExternalCacheDirs();
        }
    }

    public boolean deleteCacheFile(File del_item) {
        boolean result=true;
        if (del_item.isDirectory()) {
            File[] child_list=del_item.listFiles();
            for(File child_item:child_list) {
                if (child_item!=null) {
                    if (!deleteCacheFile(child_item)) {
                        result=false;
                        break;
                    }
                }
            }
            if (result) result=del_item.delete();
        } else {
            result=del_item.delete();
        }
        return result;
    }

	public void refreshOptionMenu() {
		invalidateOptionsMenu();
	};

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		mUtil.addDebugMsg(1, "I", "onCreateOptionsMenu Entered");
		MenuInflater inflater = mActivity.getMenuInflater();
		inflater.inflate(R.menu.menu_top, menu);
		return true;
	};

    @Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		mUtil.addDebugMsg(2, "I", "onPrepareOptionsMenu Entered");
        super.onPrepareOptionsMenu(menu);

        menu.findItem(R.id.menu_top_save_zip_file).setVisible(false);
//        if (mMainTabHost.getCurrentTabTag().equals(mContext.getString(R.string.msgs_main_tab_name_local))) {
        if (mTabLayout.getSelectedTabName().equals(mActivity.getString(R.string.msgs_main_tab_name_local))) {
            menu.findItem(R.id.menu_top_encoding).setVisible(false);
            menu.findItem(R.id.menu_top_save_zip_file).setVisible(false);
        	if (mLocalFileMgr!=null) {
        		if (mLocalFileMgr.isFileListSortAscendant()) menu.findItem(R.id.menu_top_sort).setIcon(R.drawable.ic_128_sort_asc_gray);
        		else menu.findItem(R.id.menu_top_sort).setIcon(R.drawable.ic_128_sort_dsc_gray);
        	} else {
        		menu.findItem(R.id.menu_top_sort).setIcon(R.drawable.ic_128_sort_asc_gray);
        	}
        } else {
        	if (mZipFileMgr!=null) {
                menu.findItem(R.id.menu_top_encoding).setVisible(true);
        	    if (mZipFileMgr.isZipFileLoaded()) menu.findItem(R.id.menu_top_save_zip_file).setVisible(true);
        	    else menu.findItem(R.id.menu_top_save_zip_file).setVisible(false);
        		if (mZipFileMgr.isFileListSortAscendant()) menu.findItem(R.id.menu_top_sort).setIcon(R.drawable.ic_128_sort_asc_gray);
        		else menu.findItem(R.id.menu_top_sort).setIcon(R.drawable.ic_128_sort_dsc_gray);
        	} else {
                menu.findItem(R.id.menu_top_encoding).setVisible(false);
        		menu.findItem(R.id.menu_top_sort).setIcon(R.drawable.ic_128_sort_asc_gray);
        	}
        }

        if (isUiEnabled()) {
            menu.findItem(R.id.menu_top_find).setVisible(true);
            menu.findItem(R.id.menu_top_refresh).setVisible(true);
            menu.findItem(R.id.menu_top_sort).setVisible(true);
            menu.findItem(R.id.menu_top_log_management).setVisible(true);
            menu.findItem(R.id.menu_top_about).setVisible(true);
            menu.findItem(R.id.menu_top_settings).setVisible(true);
//            menu.findItem(R.id.menu_top_save_zip_file).setVisible(true);
//            CommonDialog.setMenuItemEnabled(mActivity, menu, menu.findItem(R.id.menu_top_find), true);
//            CommonDialog.setMenuItemEnabled(mActivity, menu, menu.findItem(R.id.menu_top_refresh),true);
//            CommonDialog.setMenuItemEnabled(mActivity, menu, menu.findItem(R.id.menu_top_sort), true);
//            CommonDialog.setMenuItemEnabled(mActivity, menu, menu.findItem(R.id.menu_top_log_management), true);
//            CommonDialog.setMenuItemEnabled(mActivity, menu, menu.findItem(R.id.menu_top_about), true);
//            CommonDialog.setMenuItemEnabled(mActivity, menu, menu.findItem(R.id.menu_top_settings), true);
//            CommonDialog.setMenuItemEnabled(mActivity, menu, menu.findItem(R.id.menu_top_save_zip_file), true);
        } else {
            menu.findItem(R.id.menu_top_find).setVisible(false);
            menu.findItem(R.id.menu_top_refresh).setVisible(false);
            menu.findItem(R.id.menu_top_sort).setVisible(false);
            menu.findItem(R.id.menu_top_log_management).setVisible(false);
            menu.findItem(R.id.menu_top_about).setVisible(false);
            menu.findItem(R.id.menu_top_settings).setVisible(false);
            menu.findItem(R.id.menu_top_save_zip_file).setVisible(false);
//            CommonDialog.setMenuItemEnabled(mActivity, menu, menu.findItem(R.id.menu_top_find), false);
//            CommonDialog.setMenuItemEnabled(mActivity, menu, menu.findItem(R.id.menu_top_refresh), false);
//            CommonDialog.setMenuItemEnabled(mActivity, menu, menu.findItem(R.id.menu_top_sort), false);
//            CommonDialog.setMenuItemEnabled(mActivity, menu, menu.findItem(R.id.menu_top_log_management), false);
//            CommonDialog.setMenuItemEnabled(mActivity, menu, menu.findItem(R.id.menu_top_about), false);
//            CommonDialog.setMenuItemEnabled(mActivity, menu, menu.findItem(R.id.menu_top_settings), false);
//            CommonDialog.setMenuItemEnabled(mActivity, menu, menu.findItem(R.id.menu_top_save_zip_file), false);
        }
        if (mGp.safMgr.isStoragePermissionRequired()) menu.findItem(R.id.menu_top_storage_permission).setVisible(true);
        else menu.findItem(R.id.menu_top_storage_permission).setVisible(false);

        return true;
	};
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		mUtil.addDebugMsg(2, "I", "onOptionsItemSelected Entered");
		switch (item.getItemId()) {
			case android.R.id.home:
				return true;
			case R.id.menu_top_find:
//				if (mMainTabHost.getCurrentTabTag().equals(mContext.getString(R.string.msgs_main_tab_name_local))) mLocalFileMgr.searchFile();
//				else mZipFileMgr.searchFile();
                if (mTabLayout.getSelectedTabName().equals(mContext.getString(R.string.msgs_main_tab_name_local))) mLocalFileMgr.searchFile();
                else mZipFileMgr.searchFile();
				return true;
			case R.id.menu_top_refresh:
//				if (mMainTabHost.getCurrentTabTag().equals(mContext.getString(R.string.msgs_main_tab_name_local))) mLocalFileMgr.refreshFileList();
//				else mZipFileMgr.refreshFileList(true);
                if (mTabLayout.getSelectedTabName().equals(mContext.getString(R.string.msgs_main_tab_name_local))) mLocalFileMgr.refreshFileList();
                else mZipFileMgr.refreshFileList(true);
				return true;
			case R.id.menu_top_sort:
//				if (mMainTabHost.getCurrentTabTag().equals(mContext.getString(R.string.msgs_main_tab_name_local))) mLocalFileMgr.sortFileList();
//				else mZipFileMgr.sortFileList();
                if (mTabLayout.getSelectedTabName().equals(mContext.getString(R.string.msgs_main_tab_name_local))) mLocalFileMgr.sortFileList();
                else mZipFileMgr.sortFileList();
				return true;
			case R.id.menu_top_save_zip_file:
			    mZipFileMgr.saveZipFile();
				return true;
            case R.id.menu_top_log_management:
                invokeLogManagement();
                return true;
			case R.id.menu_top_about:
				aboutApplicaion();
				return true;			
			case R.id.menu_top_settings:
				invokeSettingsActivity();
				return true;
            case R.id.menu_top_encoding:
                mZipFileMgr.changeZipFileNameEncoding();
                return true;
            case R.id.menu_top_show_system_information:
                showSystemInfo();
                return true;
			case R.id.menu_top_quit:
				mGp.settingExitClean=true;
				confirmExit();
				return true;
            case R.id.menu_top_storage_permission:
                requestLocalStoragePermission(null);
//                reselectSdcard("");
                return true;
			case R.id.menu_top_kill:
				confirmKill();
				return true;			
		}

		return false;
	};

    private void showZipFileByIntent(final Intent intent) {
        if (intent!=null && intent.getData()!=null) {
            mUtil.addDebugMsg(1,"I","showZipFileByIntent entered, "+"Uri="+intent.getData()+", type="+intent.getType());
            final SafFile3 in_file=new SafFile3(mContext, intent.getData());
            final Handler hndl=new Handler();
            if (in_file.getUuid().equals(SAF_FILE_UNKNOWN_UUID)) {
                NotifyEvent ntfy=new NotifyEvent(mContext);
                ntfy.setListener(new NotifyEventListener() {
                    @Override
                    public void positiveResponse(Context context, Object[] objects) {
                        String cp=(String)objects[0];
                        SafFile3 new_in_file=new SafFile3(mContext, cp);
                        mLocalFileMgr.showLocalFileView(false);
                        showZipFile(true, new_in_file);
                        hndl.post(new Runnable(){
                            @Override
                            public void run() {
                                mLocalFileMgr.showLocalFileView(true);
                            }
                        });
                        refreshOptionMenu();
                    }

                    @Override
                    public void negativeResponse(Context context, Object[] objects) {
                        mLocalFileMgr.showLocalFileView(false);
                        hndl.post(new Runnable(){
                            @Override
                            public void run() {
                                mLocalFileMgr.showLocalFileView(true);
                            }
                        });
                        refreshOptionMenu();
                    }
                });
                createZipWorkFileForUnknownUuid(in_file, ntfy);
            } else {
                if (in_file.getName()!=null) {
                    mLocalFileMgr.showLocalFileView(false);
                    showZipFile(false, in_file);
                    hndl.post(new Runnable(){
                        @Override
                        public void run() {
                            mLocalFileMgr.showLocalFileView(true);
                        }
                    });
                    refreshOptionMenu();
                } else {
                    mCommonDlg.showCommonDialog(false, "E", "ZIP file display error",
                            "Unable to display ZIP file because File name is null, PATH="+in_file.getPath()+", URI="+in_file.getUri(), null);
                    mLocalFileMgr.showLocalFileView(false);
                    hndl.post(new Runnable(){
                        @Override
                        public void run() {
                            mLocalFileMgr.showLocalFileView(true);
                        }
                    });
                    refreshOptionMenu();
                }
            }
        }
    }

    private void createZipWorkFileForUnknownUuid(final SafFile3 in_file, final NotifyEvent p_ntfy) {
        final ThreadCtrl tc=new ThreadCtrl();
        tc.setEnabled();
        final Dialog pd=CommonDialog.showProgressSpinIndicator(mActivity);
        pd.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                tc.setDisabled();
            }
        });
        pd.show();

        String fn="unkonw_file_name";
        if (in_file!=null && in_file.getName()!=null) fn=in_file.getName();
        final File cache_file=new File(mContext.getExternalCacheDir(), fn);
        final Handler hndl=new Handler();
        Thread th=new Thread() {
            @Override
            public void run() {
                FileOutputStream fos=null;
                InputStream is=null;
                try {
                    cache_file.delete();
                    fos=new FileOutputStream(cache_file);
                    is=in_file.getInputStream();
                    byte[] buff=new byte[1024*1024*2];
                    int rc=0;
                    while((rc=is.read(buff))>0) {
                        if (!tc.isEnabled()) {
                            fos.flush();
                            fos.close();
                            is.close();
                            cache_file.delete();
                            mUtil.addDebugMsg(1,"I","showZipFileByIntent create work file cancelled by user");
                            mCommonDlg.showCommonDialog(false, "W", "ZipFile prepare","Work file create cancelled by user",null);
                            p_ntfy.notifyToListener(false, new Object[]{cache_file.getPath()});
                            break;
                        }
                        fos.write(buff, 0, rc);
                    }
                    fos.flush();
                    fos.close();
                    is.close();
                    pd.dismiss();
                    if (tc.isEnabled()) {
                        hndl.post(new Runnable(){
                            @Override
                            public void run() {
                                p_ntfy.notifyToListener(true, new Object[]{cache_file.getPath()});
                            }
                        });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                        try {
                            if (fos!=null) fos.close();
                            if (is!=null) is.close();
                            if (cache_file.exists()) cache_file.delete();
                        } catch (IOException ex) {}
                    mUtil.addDebugMsg(1,"I","showZipFileByIntent create work file failed, error="+e.getMessage());
                    mCommonDlg.showCommonDialog(false, "W", "ZipFile prepare","create work file failed, error="+e.getMessage(),null);
                    p_ntfy.notifyToListener(false, new Object[]{cache_file.getPath()});
                }
            }
        };
        th.start();

    }

    private void showSystemInfo() {
        final Dialog dialog = new Dialog(mActivity, mGp.applicationTheme);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.common_dialog);

        final LinearLayout ll_title=(LinearLayout) dialog.findViewById(R.id.common_dialog_title_view);
        ll_title.setBackgroundColor(mGp.themeColorList.title_background_color);

        final TextView tv_title=(TextView)dialog.findViewById(R.id.common_dialog_title);
        tv_title.setTextColor(mGp.themeColorList.title_text_color);
        final TextView tv_msg_old=(TextView)dialog.findViewById(R.id.common_dialog_msg);
        tv_msg_old.setVisibility(TextView.GONE);
        final NonWordwrapTextView tv_msg=(NonWordwrapTextView)dialog.findViewById(R.id.common_dialog_custom_text_view);
        tv_msg.setVisibility(TextView.VISIBLE);
        final Button btn_copy=(Button)dialog.findViewById(R.id.common_dialog_btn_ok);
        final Button btn_close=(Button)dialog.findViewById(R.id.common_dialog_btn_cancel);
        final Button btn_send=(Button)dialog.findViewById(R.id.common_dialog_extra_button);
        btn_send.setText(mContext.getString(R.string.msgs_info_storage_send_btn_title));
        btn_send.setVisibility(Button.VISIBLE);

        tv_title.setText(mContext.getString(R.string.msgs_menu_list_system_info));
        btn_close.setText(mContext.getString(R.string.msgs_common_dialog_close));
        btn_copy.setText(mContext.getString(R.string.msgs_info_storage_copy_clipboard));

        ArrayList<String> sil= SystemInfo.listSystemInfo(mContext, mGp.safMgr);
        String si_text="";
        for(String si_item:sil) si_text+=si_item+"\n";

        tv_msg.setText(si_text);

        CommonDialog.setDlgBoxSizeLimit(dialog,true);

        btn_copy.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                ClipboardManager cm=(ClipboardManager) mContext.getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData cd=cm.getPrimaryClip();
                cm.setPrimaryClip(ClipData.newPlainText("ZipUtility3 Storage info", tv_msg.getText().toString()));
                Toast.makeText(mContext,
                        mContext.getString(R.string.msgs_info_storage_copy_completed), Toast.LENGTH_LONG).show();
            }
        });

        btn_close.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        btn_send.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                NotifyEvent ntfy=new NotifyEvent(mContext);
                ntfy.setListener(new NotifyEventListener() {
                    @Override
                    public void positiveResponse(Context context, Object[] objects) {
                        String desc=(String)objects[0];
                        Intent intent=new Intent();
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.setAction(Intent.ACTION_SEND);
                        intent.setType("message/rfc822");
//                intent.setType("text/plain");
//                intent.setType("application/zip");

                        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{"gm.developer.fhoshino@gmail.com"});
//                intent.putExtra(Intent.EXTRA_CC, new String[]{"cc@example.com"});
//                intent.putExtra(Intent.EXTRA_BCC, new String[]{"bcc@example.com"});
                        intent.putExtra(Intent.EXTRA_SUBJECT, APPLICATION_TAG+" System Info");
                        intent.putExtra(Intent.EXTRA_TEXT, desc+ "\n\n\n"+tv_msg.getText().toString());
//                intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(lf));
                        mContext.startActivity(intent);
                    }
                    @Override
                    public void negativeResponse(Context context, Object[] objects) {
                    }
                });
                getProblemDescription(ntfy);
            }
        });

        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                btn_close.performClick();
            }
        });

        dialog.show();
    }

    private void getProblemDescription(final NotifyEvent p_ntfy) {
        final Dialog dialog = new Dialog(mActivity, mGp.applicationTheme);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.single_item_input_dlg);

        final TextView tv_title=(TextView)dialog.findViewById(R.id.single_item_input_title);
        tv_title.setText(mContext.getString(R.string.msgs_your_problem_title));
        final TextView tv_msg=(TextView)dialog.findViewById(R.id.single_item_input_msg);
        tv_msg.setVisibility(TextView.GONE);
        final TextView tv_desc=(TextView)dialog.findViewById(R.id.single_item_input_name);
        tv_desc.setText(mContext.getString(R.string.msgs_your_problem_msg));
        final EditText et_msg=(EditText)dialog.findViewById(R.id.single_item_input_dir);
        et_msg.setHint(mContext.getString(R.string.msgs_your_problem_hint));
        et_msg.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        final Button btn_ok=(Button)dialog.findViewById(R.id.single_item_input_ok_btn);
        final Button btn_cancel=(Button)dialog.findViewById(R.id.single_item_input_cancel_btn);

//        btn_cancel.setText(mContext.getString(R.string.msgs_common_dialog_close));

        CommonDialog.setDlgBoxSizeLimit(dialog,true);

        btn_ok.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                NotifyEvent ntfy_desc=new NotifyEvent(mContext);
                ntfy_desc.setListener(new NotifyEventListener() {
                    @Override
                    public void positiveResponse(Context context, Object[] objects) {
                        p_ntfy.notifyToListener(true, new Object[]{et_msg.getText().toString()});
                        dialog.dismiss();
                    }

                    @Override
                    public void negativeResponse(Context context, Object[] objects) {
                    }
                });
                if (et_msg.getText().length()<=10) {
                    mCommonDlg.showCommonDialog(false, "W", mContext.getString(R.string.msgs_your_problem_no_desc),"",null);
                } else {
                    ntfy_desc.notifyToListener(true, null);
                }
            }
        });

        btn_cancel.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                btn_cancel.performClick();
            }
        });

        dialog.show();
    }

	public void showZipFile(boolean read_only, final SafFile3 in_file) {
		if (!isUiEnabled()) return;
        mZipFileMgr.showZipFile(read_only, in_file);
        mTabLayout.setCurrentTabByName(mContext.getString(R.string.msgs_main_tab_name_zip));
	};

	private void invokeLogManagement() {
        NotifyEvent ntfy=new NotifyEvent(mContext);
        ntfy.setListener(new NotifyEvent.NotifyEventListener(){
            @Override
            public void positiveResponse(Context c, Object[] o) {
            }
            @Override
            public void negativeResponse(Context c, Object[] o) {
            }
        });
        mUtil.flushLog();
        LogManagementFragment lfm= LogManagementFragment.newInstance(mContext, false, mContext.getString(R.string.msgs_log_management_title));
        lfm.showDialog(mContext, getSupportFragmentManager(), lfm, ntfy);
	};

	public boolean isApplicationTerminating() {return mTerminateApplication;}

	private void confirmExit() {
		NotifyEvent ntfy=new NotifyEvent(mContext);
		ntfy.setListener(new NotifyEventListener(){
			@Override
			public void positiveResponse(Context c, Object[] o) {
				mTerminateApplication=true;
				finish();
			}
			@Override
			public void negativeResponse(Context c, Object[] o) {
				mGp.settingExitClean=false;
			}
		});
		if (mGp.settingConfirmAppExit) mCommonDlg.showCommonDialog(true, "W", mContext.getString(R.string.msgs_main_exit_confirm_msg), "", ntfy);
		else ntfy.notifyToListener(true, null);
	};

	private void confirmKill() {
		NotifyEvent ntfy=new NotifyEvent(mContext);
		ntfy.setListener(new NotifyEventListener(){
			@Override
			public void positiveResponse(Context c, Object[] o) {
				android.os.Process.killProcess(android.os.Process.myPid());
			}
			@Override
			public void negativeResponse(Context c, Object[] o) {
				mGp.settingExitClean=false;
			}
		});
		mCommonDlg.showCommonDialog(true, "W", mContext.getString(R.string.msgs_main_kill_confirm_msg), "", ntfy);
	};

    public String getAppVersionName() {
        try {
            String packegeName = getPackageName();
            PackageInfo packageInfo = getPackageManager().getPackageInfo(packegeName, PackageManager.GET_META_DATA);
            return packageInfo.versionName;
        } catch (NameNotFoundException e) {
            return "";
        }
    };

    private void aboutApplicaion() {
        final Dialog dialog = new Dialog(mActivity, mGp.applicationTheme);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.about_dialog);

        final LinearLayout title_view = (LinearLayout) dialog.findViewById(R.id.about_dialog_title_view);
        final TextView title = (TextView) dialog.findViewById(R.id.about_dialog_title);
        title_view.setBackgroundColor(mGp.themeColorList.title_background_color);
        title.setTextColor(mGp.themeColorList.title_text_color);
        title.setText(getString(R.string.msgs_dlg_title_about)+"(Ver "+getAppVersionName()+")");

        CustomTabLayout tab_layout=(CustomTabLayout)dialog.findViewById(R.id.about_tab_view);
        tab_layout.addTab(mContext.getString(R.string.msgs_about_dlg_func_btn));
        tab_layout.addTab(mContext.getString(R.string.msgs_about_dlg_privacy_btn));
        tab_layout.addTab(mContext.getString(R.string.msgs_about_dlg_change_btn));
        tab_layout.adjustTabWidth();

        LayoutInflater vi = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        LinearLayout ll_func=(LinearLayout)vi.inflate(R.layout.about_dialog_func,null);
        LinearLayout ll_change=(LinearLayout)vi.inflate(R.layout.about_dialog_change,null);
        LinearLayout ll_privacy=(LinearLayout)vi.inflate(R.layout.about_dialog_privacy,null);

        final WebView func_view=(WebView)ll_func.findViewById(R.id.about_dialog_function);
        func_view.loadUrl("File:///android_asset/"+getString(R.string.msgs_dlg_title_about_func_desc));
        func_view.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
//	    func_view.getSettings().setBuiltInZoomControls(true);
//        func_view.getSettings().setDisplayZoomControls(true);

        final WebView change_view=(WebView)ll_change.findViewById(R.id.about_dialog_change_history);
        change_view.loadUrl("File:///android_asset/"+getString(R.string.msgs_dlg_title_about_change_desc));
        change_view.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
//		change_view.getSettings().setBuiltInZoomControls(true);
//        change_view.getSettings().setDisplayZoomControls(true);

        final WebView privacy_view=(WebView)ll_privacy.findViewById(R.id.about_dialog_privacy);
        privacy_view.loadUrl("File:///android_asset/"+getString(R.string.msgs_dlg_title_about_privacy_desc));
        privacy_view.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
//        privacy_view.getSettings().setBuiltInZoomControls(true);
//        privacy_view.getSettings().setDisplayZoomControls(true);

        final CustomViewPagerAdapter adapter=new CustomViewPagerAdapter(mActivity, new WebView[]{func_view, privacy_view, change_view});
        final CustomViewPager mAboutViewPager=(CustomViewPager)dialog.findViewById(R.id.about_view_pager);
//	    mMainViewPager.setBackgroundColor(mThemeColorList.window_color_background);

        mAboutViewPager.setAdapter(adapter);
        mAboutViewPager.setOffscreenPageLimit(3);//adapter.getCount());
        mAboutViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener(){
            @Override
            public void onPageSelected(int position) {
		    	mUtil.addDebugMsg(2,"I","onPageSelected entered, pos="+position);
                tab_layout.setCurrentTabByPosition(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
		    	mUtil.addDebugMsg(2,"I","onPageScrollStateChanged entered, state="+state);
            }

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
//		    	util.addDebugMsg(2,"I","onPageScrolled entered, pos="+position);
            }
        });

        tab_layout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener(){
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                mUtil.addDebugMsg(2,"I","onTabSelected entered, state="+tab);
                mAboutViewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                mUtil.addDebugMsg(2,"I","onTabUnselected entered, state="+tab);
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                mUtil.addDebugMsg(2,"I","onTabReselected entered, state="+tab);
            }

        });

        final Button btnOk = (Button) dialog.findViewById(R.id.about_dialog_btn_ok);

//        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        CommonDialog.setDlgBoxSizeLimit(dialog,true);

        // OKボタンの指定
        btnOk.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        // Cancelリスナーの指定
        dialog.setOnCancelListener(new Dialog.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface arg0) {
                btnOk.performClick();
            }
        });

        dialog.show();
    };

    public String getApplVersionName() {
		try {
		    String packegeName = getPackageName();
		    PackageInfo packageInfo = getPackageManager().getPackageInfo(packegeName, PackageManager.GET_META_DATA);
		    return packageInfo.versionName;
		} catch (NameNotFoundException e) {
			return "";
		}
	}

//    private boolean isPrimaryStorageAccessGranted() {
//        if (mGp.safMgr.isUuidRegistered(SAF_FILE_PRIMARY_UUID)) return true;
//        return false;
//    }

    private boolean isLegacyStorageAccessGranted() {
        if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) return false;
        return true;
    }

    private boolean checkLegacyStoragePermissions(final NotifyEvent p_ntfy) {
        log.debug("Prermission WriteExternalStorage="+checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE));
        if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            NotifyEvent ntfy=new NotifyEvent(mContext);
            ntfy.setListener(new NotifyEventListener(){
                @Override
                public void positiveResponse(Context c, Object[] o) {
                    ActivityResultLauncher<String> request_permission = mActivity.registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                        if (isGranted) {
                            p_ntfy.notifyToListener(true, null);
                        } else {
                            NotifyEvent ntfy_term = new NotifyEvent(mContext);
                            ntfy_term.setListener(new NotifyEvent.NotifyEventListener() {
                                @Override
                                public void positiveResponse(Context c, Object[] o) {
                                    finish();
                                }

                                @Override
                                public void negativeResponse(Context c, Object[] o) {}
                            });
                            mCommonDlg.showCommonDialog(false, "W",
                                    mContext.getString(R.string.msgs_main_permission_primary_storage_title),
                                    mContext.getString(R.string.msgs_main_permission_primary_storage_denied_msg), ntfy_term);
                        }

                    });
                    request_permission.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE);
                }
                @Override
                public void negativeResponse(Context c, Object[] o) {
                    NotifyEvent ntfy_term=new NotifyEvent(mContext);
                    ntfy_term.setListener(new NotifyEventListener(){
                        @Override
                        public void positiveResponse(Context c, Object[] o) {
                            finish();
                        }
                        @Override
                        public void negativeResponse(Context c, Object[] o) {}
                    });
                    mCommonDlg.showCommonDialog(false, "W",
                            mContext.getString(R.string.msgs_main_permission_primary_storage_title),
                            mContext.getString(R.string.msgs_main_permission_primary_storage_denied_msg), ntfy_term);
                }
            });
            mCommonDlg.showCommonDialog(false, "W",
                    mContext.getString(R.string.msgs_main_permission_primary_storage_title),
                    mContext.getString(R.string.msgs_main_permission_primary_storage_request_msg),
                    ntfy);
            return false;
        } else {
            return true;
        }
    };

//    private void checkInternalStoragePermission(final NotifyEvent p_ntfy) {
//        ArrayList<SafStorage3>ssl=mGp.safMgr.getSafStorageList();
//        boolean internal_permitted=isPrimaryStorageAccessGranted();
//        if (!internal_permitted) {
//            NotifyEvent ntfy_request=new NotifyEvent(mContext);
//            mStoragePermissionPrimaryListener = ntfy_request;
//            ntfy_request.setListener(new NotifyEvent.NotifyEventListener() {
//                @Override
//                public void positiveResponse(Context context, Object[] objects) {
//                    final NotifyEvent ntfy_response=new NotifyEvent(mContext);
//                    mStoragePermissionPrimaryListener = ntfy_response;
//                    ntfy_response.setListener(new NotifyEvent.NotifyEventListener() {
//                        @Override
//                        public void positiveResponse(Context context, Object[] objects) {
//                            int requestCode=(Integer)objects[0];
//                            int resultCode=(Integer)objects[1];
//                            Intent data=(Intent)objects[2];
//
//                            if (resultCode == Activity.RESULT_OK) {
//                                if (data==null || data.getDataString()==null) {
//                                    mCommonDlg.showCommonDialog(false, "W", "Storage access grant request was failed because null intent data was returned.", "", null);
//                                    mUtil.addLogMsg("E", "Storage Grant write permission failed because null intent data was returned.", "");
//                                    return;
//                                }
//                                mUtil.addDebugMsg(1, "I", "Intent=" + data.getData().toString());
//                                if (!mGp.safMgr.isRootTreeUri(data.getData())) {
//                                    mUtil.addDebugMsg(1, "I", "Selected UUID="+ SafManager3.getUuidFromUri(data.getData().toString()));
//
//                                    NotifyEvent ntfy_retry = new NotifyEvent(mContext);
//                                    ntfy_retry.setListener(new NotifyEvent.NotifyEventListener() {
//                                        @Override
//                                        public void positiveResponse(Context c, Object[] o) {
//                                            mStoragePermissionPrimaryListener = ntfy_response;
//                                            requestStoragePermissionsByUuid(SAF_FILE_PRIMARY_UUID, mStoragePermissionPrimaryRequestCode);
//                                        }
//                                        @Override
//                                        public void negativeResponse(Context c, Object[] o) {
//                                            NotifyEvent ntfy_term = new NotifyEvent(mContext);
//                                            ntfy_term.setListener(new NotifyEvent.NotifyEventListener() {
//                                                @Override
//                                                public void positiveResponse(Context c, Object[] o) {
//                                                    finish();
//                                                }
//                                                @Override
//                                                public void negativeResponse(Context c, Object[] o) {}
//                                            });
//                                            mCommonDlg.showCommonDialog(false, "W",
//                                                    mContext.getString(R.string.msgs_main_permission_primary_storage_title),
//                                                    mContext.getString(R.string.msgs_main_permission_primary_storage_denied_msg), ntfy_term);
//                                        }
//                                    });
//                                    mCommonDlg.showCommonDialog(true, "W", mContext.getString(R.string.msgs_main_permission_primary_storage_request_msg),
//                                            data.getData().getPath(), ntfy_retry);
//                                } else {
//                                    mUtil.addDebugMsg(1, "I", "Selected UUID="+SafManager3.getUuidFromUri(data.getData().toString()));
//                                    boolean rc=mGp.safMgr.addUuid(data.getData());
//                                    if (!rc) {
//                                        String saf_msg=mGp.safMgr.getLastErrorMessage();
//                                        mCommonDlg.showCommonDialog(false, "W", "Primary UUID registration failed.", saf_msg, null);
//                                        mUtil.addLogMsg("E", "Primary UUID registration failed.\n", saf_msg);
//                                    }
//                                    p_ntfy.notifyToListener(true, null);
//                                }
//                            } else {
//                                NotifyEvent ntfy_term = new NotifyEvent(mContext);
//                                ntfy_term.setListener(new NotifyEvent.NotifyEventListener() {
//                                    @Override
//                                    public void positiveResponse(Context c, Object[] o) {
//                                        finish();
//                                    }
//                                    @Override
//                                    public void negativeResponse(Context c, Object[] o) {}
//                                });
//                                mCommonDlg.showCommonDialog(false, "W",
//                                        mContext.getString(R.string.msgs_main_permission_primary_storage_title),
//                                        mContext.getString(R.string.msgs_main_permission_primary_storage_denied_msg), ntfy_term);
//                            }
//                        }
//                        @Override
//                        public void negativeResponse(Context context, Object[] objects) {}
//                    });
//                    mStoragePermissionPrimaryListener = ntfy_response;
//                    requestStoragePermissionsByUuid(SAF_FILE_PRIMARY_UUID, mStoragePermissionPrimaryRequestCode);
//                }
//
//                @Override
//                public void negativeResponse(Context context, Object[] objects) {
//                    NotifyEvent ntfy_term = new NotifyEvent(mContext);
//                    ntfy_term.setListener(new NotifyEvent.NotifyEventListener() {
//                        @Override
//                        public void positiveResponse(Context c, Object[] o) {
//                            finish();
//                        }
//
//                        @Override
//                        public void negativeResponse(Context c, Object[] o) {}
//                    });
//                    mCommonDlg.showCommonDialog(false, "W",
//                            mContext.getString(R.string.msgs_main_permission_primary_storage_title),
//                            mContext.getString(R.string.msgs_main_permission_primary_storage_denied_msg), ntfy_term);
//                }
//            });
//            mCommonDlg.showCommonDialog(true, "W",
//                    mContext.getString(R.string.msgs_main_permission_primary_storage_title),
//                    mContext.getString(R.string.msgs_main_permission_primary_storage_request_msg),
//                    ntfy_request);
//        } else {
//            p_ntfy.notifyToListener(true, null);
//        }
//    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //Do not add code
    };

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        //Do not add code
    }

    public void requestStoragePermissionsByUuid(String uuid, NotifyEvent ntfy) {
        Intent intent = null;
        StorageManager sm = (StorageManager) mContext.getSystemService(Context.STORAGE_SERVICE);
        ArrayList<SafManager3.StorageVolumeInfo>vol_list=SafManager3.getStorageVolumeInfo(mContext);
        for(SafManager3.StorageVolumeInfo svi:vol_list) {
            if (svi.uuid.equals(uuid)) {
                if (Build.VERSION.SDK_INT>=29) intent=svi.volume.createOpenDocumentTreeIntent();
                else intent=svi.volume.createAccessIntent(null);
                try {
                    ActivityResultLauncher<Intent> laucher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
                        @Override
                        public void onActivityResult(ActivityResult result) {
                            ntfy.notifyToListener(true, new Object[]{result.getResultCode(), result.getData(), uuid});
                        }
                    });
                    laucher.launch(intent);
                } catch(Exception e) {
                    String st= MiscUtil.getStackTraceString(e);
                    mCommonDlg.showCommonDialog(false, "E", "SAF Request permission error", e.getMessage()+"\n"+st, null);
                }
                break;
            }
        }
    }

    private void requestLocalStoragePermission(final NotifyEvent p_ntfy) {
        NotifyEvent ntfy=new NotifyEvent(mContext);
        ntfy.setListener(new NotifyEvent.NotifyEventListener() {
            @Override
            public void positiveResponse(Context context, Object[] objects) {
                ArrayList<String>uuid_list=(ArrayList<String>)objects[0];
                final NotifyEvent ntfy_response=new NotifyEvent(mContext);
                ntfy_response.setListener(new NotifyEvent.NotifyEventListener() {
                    @Override
                    public void positiveResponse(Context context, Object[] objects) {
                        final int resultCode=(Integer)objects[0];
                        final Intent data=(Intent)objects[1];
                        final String uuid=(String)objects[2];

                        if (resultCode == Activity.RESULT_OK) {
                            if (data==null || data.getDataString()==null) {
                                mCommonDlg.showCommonDialog(false, "W", "Storage Grant write permission failed because null intent data was returned.", "", null);
                                mUtil.addLogMsg("E", "Storage Grant write permission failed because null intent data was returned.", "");
                                return;
                            }
                            mUtil.addDebugMsg(1, "I", "Intent=" + data.getData().toString());
                            if (!mGp.safMgr.isRootTreeUri(data.getData())) {
                                mUtil.addDebugMsg(1, "I", "Selected UUID="+ SafManager3.getUuidFromUri(data.getData().toString()));
                                String em=mGp.safMgr.getLastErrorMessage();
                                if (em.length()>0) mUtil.addDebugMsg(1, "I", "SafMessage="+em);

                                NotifyEvent ntfy_retry = new NotifyEvent(mContext);
                                ntfy_retry.setListener(new NotifyEvent.NotifyEventListener() {
                                    @Override
                                    public void positiveResponse(Context c, Object[] o) {
                                        requestStoragePermissionsByUuid(uuid, ntfy_response);
                                    }

                                    @Override
                                    public void negativeResponse(Context c, Object[] o) {}
                                });
                                mCommonDlg.showCommonDialog(true, "W", mContext.getString(R.string.msgs_main_external_storage_select_retry_select_msg),
                                        data.getData().getPath(), ntfy_retry);
                            } else {
                                mUtil.addDebugMsg(1, "I", "Selected UUID="+SafManager3.getUuidFromUri(data.getData().toString()));
                                String em=mGp.safMgr.getLastErrorMessage();
                                if (em.length()>0) mUtil.addDebugMsg(1, "I", "SafMessage="+em);
                                boolean rc=mGp.safMgr.addUuid(data.getData());
                                if (!rc) {
                                    String saf_msg=mGp.safMgr.getLastErrorMessage();
                                    mCommonDlg.showCommonDialog(false, "W", "Primary UUID registration failed.", saf_msg, null);
                                    mUtil.addLogMsg("E", "Primary UUID registration failed.\n", saf_msg);
                                } else {
//                                    mGp.safMgr.refreshSafList();
                                    mLocalFileMgr.refreshLocalStorageSelector();
                                    if (p_ntfy!=null) p_ntfy.notifyToListener(true, null);
                                }
                            }
                        } else {
                            mCommonDlg.showCommonDialog(false, "W",
                                    mContext.getString(R.string.msgs_main_external_storage_select_required_title),
                                    mContext.getString(R.string.msgs_main_external_storage_select_deny_msg), null);

                        }
                    }

                    @Override
                    public void negativeResponse(Context context, Object[] objects) {

                    }
                });
                for(String uuid:uuid_list) {
                    requestStoragePermissionsByUuid(uuid, ntfy_response);
                }
            }

            @Override
            public void negativeResponse(Context context, Object[] objects) {}
        });
        StoragePermission sp=new StoragePermission(mActivity, mCommonDlg, ntfy);
        sp.showDialog();

    }

    private void invokeSettingsActivity() {
        mUtil.addDebugMsg(1,"I","Invoke Settings.");

        ActivityResultLauncher<Intent> laucher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                mUtil.addDebugMsg(1,"I","Return from Setting activity.");
                applySettingParms(result.getData());
            }
        });
        Intent intent=new Intent(mContext, ActivitySettings.class);
        laucher.launch(intent);
    };

    private void applySettingParms(Intent in) {
		int prev_theme=mGp.applicationTheme;
		String prev_language=in.getExtras().getString(ActivitySettings.LANGUAGE_KEY, "");
        String prev_font_scale=in.getExtras().getString(ActivitySettings.FONT_SCALE_KEY, "");
		mGp.loadSettingsParms(mContext);
		mGp.refreshMediaDir(mContext);
		
        if (mGp.settingFixDeviceOrientationToPortrait) setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        else setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);

        String new_font_scale=mGp.getFontScaleFactor(mContext);
        if (prev_theme!=mGp.applicationTheme || (!prev_language.equals("") && !prev_language.equals(mGp.settingLanguageValue)) ||
            !prev_font_scale.equals(new_font_scale)) {
            NotifyEvent ntfy=new NotifyEvent(mContext);
            ntfy.setListener(new NotifyEventListener() {
                @Override
                public void positiveResponse(Context context, Object[] objects) {
                    finish();
                    mGp.settingExitClean=true;
                    Intent in=new Intent(mContext, ActivityMain.class);
                    startActivity(in);

                }

                @Override
                public void negativeResponse(Context context, Object[] objects) {

                }
            });
        	mCommonDlg.showCommonDialog(true, "W", mContext.getString(R.string.msgs_main_theme_changed_msg), "", ntfy);
//        	mGp.settingExitClean=true;
        }

    };

    private boolean enableMainUi=true;

	public void setUiEnabled() {
		enableMainUi=true;
		mTabLayout.setEnabled(enableMainUi);
		mMainViewPager.setSwipeEnabled(enableMainUi);
		refreshOptionMenu();
		
		try {
		    if (mSvcClient!=null)
			    mSvcClient.aidlUpdateNotificationMessage(mContext.getString(R.string.msgs_main_notification_end_message));
		} catch (RemoteException e) {
			e.printStackTrace();
		}

	};
	
	public void putNotificationMsg(String msg) {
		if (mSvcClient!=null) {
			try {
				mSvcClient.aidlUpdateNotificationMessage(msg);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
	};
	
	public void setUiDisabled() {
		enableMainUi=false;
        mTabLayout.setEnabled(enableMainUi);
		mMainViewPager.setSwipeEnabled(enableMainUi);
		refreshOptionMenu();
	};
	
	public boolean isUiEnabled() {
		return enableMainUi;
	};

    public void clearCopyCutItem() {
        if (mLocalFileMgr!=null) mLocalFileMgr.setContextButtonPasteEnabled(false);
        if (mZipFileMgr!=null) mZipFileMgr.setContextButtonPasteEnabled(false);
        mGp.copyCutList.clear();
        mGp.localCopyCutView.setVisibility(LinearLayout.GONE);
        mGp.zipCopyCutView.setVisibility(LinearLayout.GONE);

        CommonDialog.showToastShort(mActivity, mContext.getString(R.string.msgs_zip_local_file_clear_copy_cut_list_cleared));
    }

    public void setCopyCutItemView() {
        String c_list = "", sep = "";
        for (TreeFilelistItem tfl : mGp.copyCutList) {
            if (tfl.isZipFileItem()) {
                c_list += sep + "/"+tfl.getPath()+"/"+tfl.getName();
                sep = ", ";
            } else {
                c_list += sep + tfl.getPath()+"/"+tfl.getName();
                sep = ", ";
            }
        }
        String from=mGp.copyCutFrom.equals(COPY_CUT_FROM_LOCAL)? mContext.getString(R.string.msgs_zip_local_file_clear_copy_cut_from_local) : mContext.getString(R.string.msgs_zip_local_file_clear_copy_cut_from_zip);
        String mode=mGp.copyCutModeIsCut?mContext.getString(R.string.msgs_zip_cont_header_cut):mContext.getString(R.string.msgs_zip_cont_header_copy);

        mGp.localCopyCutView.setVisibility(LinearLayout.VISIBLE);
        mGp.localCopyCutItemMode.setText(mode);
        mGp.localCopyCutItemFrom.setText(from);
        mGp.localCopyCutItemInfo.setText(c_list);
        Handler hndl=new Handler();
        hndl.post(new Runnable() {
            @Override
            public void run() {
                mGp.localCopyCutItemInfo.requestLayout();
            }
        });


        mGp.zipCopyCutView.setVisibility(LinearLayout.VISIBLE);
        mGp.zipCopyCutItemMode.setText(mode);
        mGp.zipCopyCutItemFrom.setText(from);
        mGp.zipCopyCutItemInfo.setText(c_list);
        mGp.zipCopyCutItemInfo.requestLayout();

        if (mLocalFileMgr!=null) {
            mLocalFileMgr.setContextButtonPasteEnabled(false);
        }
        if (mZipFileMgr!=null) {
            mZipFileMgr.setContextButtonPasteEnabled(false);
        }

    }

    private LinearLayout mLocalView;
	private LinearLayout mZipView;

	private CustomViewPager mMainViewPager;

	private CustomTabLayout mTabLayout=null;

    private void createTabView() {
        mTabLayout=(CustomTabLayout)findViewById(R.id.main_screen_tab);
        mTabLayout.addTab(mContext.getString(R.string.msgs_main_tab_name_local));
        mTabLayout.addTab(mContext.getString(R.string.msgs_main_tab_name_zip));

        LinearLayout ll_main=(LinearLayout)findViewById(R.id.main_screen_view);
//		ll_main.setBackgroundColor(mGp.themeColorList.window_background_color_content);

        LayoutInflater vi = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mLocalView=(LinearLayout)vi.inflate(R.layout.main_local_file,null);
//		if (mGp.themeIsLight) mLocalView.setBackgroundColor(0xffc0c0c0);
//		else mLocalView.setBackgroundColor(0xff303030);

        LinearLayout dv=(LinearLayout)mLocalView.findViewById(R.id.main_dialog_view);
        dv.setVisibility(LinearLayout.GONE);

        LinearLayout lv=(LinearLayout)mLocalView.findViewById(R.id.local_file_view);
        lv.setVisibility(LinearLayout.GONE);

//		mLocalView.setBackgroundColor(mGp.themeColorList.window_background_color_content);
        mZipView=(LinearLayout)vi.inflate(R.layout.main_zip_file,null);
//		if (mGp.themeIsLight) mZipView.setBackgroundColor(0xffc0c0c0);
//		else mZipView.setBackgroundColor(0xff303030);
//		mZipView.setBackgroundColor(mGp.themeColorList.window_background_color_content);

        mMainViewPager=(CustomViewPager)findViewById(R.id.main_screen_pager);
        CustomViewPagerAdapter adapter=new CustomViewPagerAdapter(mActivity,
                new View[]{mLocalView, mZipView});

//	    mMainViewPager.setBackgroundColor(mGp.themeColorList.window_background_color_content);

        mMainViewPager.setAdapter(adapter);
//        mMainViewPager.setOffscreenPageLimit(mMainViewPagerAdapter.getCount());
        mMainViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener(){
            @Override
            public void onPageSelected(int position) {
                mUtil.addDebugMsg(2,"I","onPageSelected entered, pos="+position);
                mTabLayout.setCurrentTabByPosition(position);
                if (mTabLayout.getSelectedTabName()!=null) {
                    if (mTabLayout.getSelectedTabName().equals(mContext.getString(R.string.msgs_main_tab_name_local))) {
                        if (mLocalFileMgr!=null) {
                            mUiHandler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    mLocalFileMgr.refreshFileList();
                                }
                            },100);
                        }
                    } else {
                        if (mZipFileMgr!=null) mZipFileMgr.refreshFileList();
                    }
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                mUtil.addDebugMsg(2,"I","onPageScrollStateChanged entered, state="+state);
            }

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
//		    	util.addDebugMsg(2,"I","onPageScrolled entered, pos="+position);
            }
        });

        mTabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener(){
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                mUtil.addDebugMsg(2,"I","onTabSelected entered, state="+tab);
                mMainViewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                mUtil.addDebugMsg(2,"I","onTabUnselected entered, state="+tab);
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                mUtil.addDebugMsg(2,"I","onTabReselected entered, state="+tab);
            }

        });

    };

	private ISvcClient mSvcClient=null;
	private ServiceConnection mSvcConnection=null;

	private void openService(final NotifyEvent p_ntfy) {
 		mUtil.addDebugMsg(1,"I",CommonUtilities.getExecutedMethodName()+" entered");
 		if (mSvcConnection==null) {
            mSvcConnection = new ServiceConnection(){
                public void onServiceConnected(ComponentName arg0, IBinder service) {
                    mUtil.addDebugMsg(1,"I",CommonUtilities.getExecutedMethodName()+" entered");
                    mSvcClient=ISvcClient.Stub.asInterface(service);
                    setCallbackListener();
                    p_ntfy.notifyToListener(true, null);
                }
                public void onServiceDisconnected(ComponentName name) {
                    mSvcConnection = null;
                    mUtil.addDebugMsg(1,"I",CommonUtilities.getExecutedMethodName()+" entered");
                }
            };

            Intent intmsg = new Intent(mContext, ZipService.class);
            intmsg.setAction("Bind");
            bindService(intmsg, mSvcConnection, BIND_AUTO_CREATE);
        }
	};

	private void closeService() {
    	
		mUtil.addDebugMsg(1,"I",CommonUtilities.getExecutedMethodName()+" entered");

        unsetCallbackListener();
    	if (mSvcConnection!=null) {
    		mSvcClient=null;
    		unbindService(mSvcConnection);
	    	mSvcConnection=null;
    	}
	};
	
	final private void setCallbackListener() {
		mUtil.addDebugMsg(1, "I", CommonUtilities.getExecutedMethodName()+" entered");
		try{
			mSvcClient.setCallBack(mSvcCallbackStub);
		} catch (RemoteException e){
			e.printStackTrace();
			mUtil.addDebugMsg(1,"E", "setCallbackListener error :"+e.toString());
		}
	};

	final private void unsetCallbackListener() {
		mUtil.addDebugMsg(1, "I", CommonUtilities.getExecutedMethodName()+" entered");
		if (mSvcClient!=null) {
			try{
				mSvcClient.removeCallBack(mSvcCallbackStub);
			} catch (RemoteException e){
				e.printStackTrace();
				mUtil.addDebugMsg(1,"E", "unsetCallbackListener error :"+e.toString());
			}
		}
	};

	private ISvcCallback mSvcCallbackStub=new ISvcCallback.Stub() {
		@Override
		public void cbNotifyMediaStatus(String action) throws RemoteException {
            mUtil.addDebugMsg(1,"I", "cbNotifyMediaStatus entered, Action="+action);
            if (mLocalFileMgr!=null) {
                boolean sc=mLocalFileMgr.refreshLocalStorageSelector();
                if (sc) mCommonDlg.showCommonDialog(false, "W", mContext.getString(R.string.msgs_main_local_mount_point_unmounted), "", null);
            }
		}
    };

}
