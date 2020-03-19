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
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.widget.Button;

import com.sentaroh.android.Utilities3.SafManager3;
import com.sentaroh.android.Utilities3.ThemeColorList;
import com.sentaroh.android.Utilities3.ThemeColorList;
import com.sentaroh.android.ZipUtility3.Log.LogUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.LoggerWriter;

import java.io.File;
import java.util.ArrayList;

import static com.sentaroh.android.ZipUtility3.Constants.APPLICATION_TAG;
import static com.sentaroh.android.ZipUtility3.Constants.LOG_FILE_NAME;

public class GlobalParameters {
    private static Logger slf4jLog = LoggerFactory.getLogger(GlobalParameters.class);

	public boolean activityIsDestroyed=false;
	public boolean activityIsBackground=false;
	public int applicationTheme=-1;
	public ThemeColorList themeColorList=null;
	public boolean themeIsLight=false;

	public ISvcCallback callbackStub=null;

	public Context appContext=null;
	
	public boolean debuggable=false;

	public SafManager3 safMgr=null;

	public ArrayList<TreeFilelistItem> copyCutList=new ArrayList<TreeFilelistItem>();
	public String copyCutFilePath="";
	public String copyCutCurrentDirectory="";
	public String copyCutEncoding="";
	public String copyCutType=COPY_CUT_FROM_LOCAL;
	public final static String COPY_CUT_FROM_LOCAL="L";
	public final static String COPY_CUT_FROM_ZIP="Z";
	public boolean copyCutModeIsCut=false;
	public Button copyCutItemClear=null;
	public Button copyCutItemInfo=null;
	
//	Settings parameter	    	
	public boolean settingExitClean=true;
	public boolean settingUseLightTheme=false;

	public boolean settingFixDeviceOrientationToPortrait=false;

    public boolean settingConfirmAppExit=true;
	
	public String settingZipDefaultEncoding="UTF-8";
	public String settingNoCompressFileType=DEFAULT_NOCOMPRESS_FILE_TYPE;
	static final public String DEFAULT_NOCOMPRESS_FILE_TYPE=
			"aac;avi;gif;ico;gz;jpe;jpeg;jpg;m3u;m4a;m4u;mov;movie;mp2;mp3;mpe;mpeg;mpg;mpga;ogg;png;qt;ra;ram;svg;tgz;wmv;zip;";

    public String settingOpenAsTextFileType=DEFAULT_OPEN_AS_TEXT_FILE_TYPE;
    static final public String DEFAULT_OPEN_AS_TEXT_FILE_TYPE=
            "log;";

    public boolean settingSupressAddExternalStorageNotification =false;
    public boolean isSupressAddExternalStorageNotification() {
        return settingSupressAddExternalStorageNotification;
    }
    public void setSupressAddExternalStorageNotification(Context c, boolean suppress) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(c);
        prefs.edit().putBoolean(c.getString(R.string.settings_suppress_add_external_storage_notification), suppress).commit();
        settingSupressAddExternalStorageNotification=suppress;
    }

    public Handler uiHandler=null;


	public GlobalParameters() {
//		Log.v("","constructed");
	};
	
    public void initGlobalParameter(Context c) {
//		Log.v("","onCreate dir="+getFilesDir().toString());
        appContext=c;
        uiHandler=new Handler();
        debuggable=isDebuggable();

        final LogUtil slf4j_lu = new LogUtil(appContext, "SLF4J");
        Slf4jLogWriter slf4j_lw=new Slf4jLogWriter(slf4j_lu);
        slf4jLog.setWriter(slf4j_lw);


        initSettingsParms(c);
        loadSettingsParms(c);

        refreshMediaDir(c);

    };
    class Slf4jLogWriter extends LoggerWriter {
        private LogUtil mLu =null;
        public Slf4jLogWriter(LogUtil lu) {
            mLu =lu;
        }
        @Override
        public void write(String msg) {
            mLu.addDebugMsg(1,"I", msg);
        }
    }

    public void clearParms() {
	};
	
	public void refreshMediaDir(Context c) {
        if (safMgr==null) {
            safMgr=new SafManager3(c);
        } else {
            safMgr.refreshSafList();
        }
	};
	
	public void initSettingsParms(Context c) {
		
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(c);
		
		if (!prefs.contains(c.getString(R.string.settings_zip_default_encoding))) {

            prefs.edit().putString(c.getString(R.string.settings_no_compress_file_type),
                    "aac;ai;avi;gif;gz;jpe;jpeg;jpg;m3u;m4a;m4u;mov;movie;mp3;mp4;mpe;mpeg;mpg;mpga;pdf;png;psd;qt;ra;ram;svg;tgz;wmv;").commit();

            prefs.edit().putBoolean(c.getString(R.string.settings_exit_clean), true).commit();

            String enc="UTF-8";
			prefs.edit().putString(c.getString(R.string.settings_zip_default_encoding), enc).commit();
            prefs.edit().putString(c.getString(R.string.settings_open_as_text_file_type), DEFAULT_OPEN_AS_TEXT_FILE_TYPE).commit();
        }

        if (!prefs.contains(c.getString(R.string.settings_confirm_exit))) {
            prefs.edit().putBoolean(c.getString(R.string.settings_confirm_exit), true).commit();
        }
	};

	public void loadSettingsParms(Context c) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(c);

		settingNoCompressFileType=prefs.getString(c.getString(R.string.settings_no_compress_file_type), DEFAULT_NOCOMPRESS_FILE_TYPE);

        settingOpenAsTextFileType=prefs.getString(c.getString(R.string.settings_open_as_text_file_type), DEFAULT_OPEN_AS_TEXT_FILE_TYPE);

		themeIsLight=settingUseLightTheme=prefs.getBoolean(c.getString(R.string.settings_use_light_theme), false);
		if (settingUseLightTheme) {
			applicationTheme=R.style.MainLight;
//			dialogViewBackGroundColor=Color.argb(255, 50, 50, 50);//.BLACK;
		} else {
			applicationTheme=R.style.Main;
//			dialogViewBackGroundColor=Color.argb(255, 50, 50, 50);//.BLACK;
		}
//		if (Build.VERSION.SDK_INT>=21) dialogViewBackGroundColor=0xff333333;
		settingFixDeviceOrientationToPortrait=prefs.getBoolean(c.getString(R.string.settings_device_orientation_portrait),false);
		
		settingZipDefaultEncoding=prefs.getString(c.getString(R.string.settings_zip_default_encoding), "UTF-8");

        settingConfirmAppExit=prefs.getBoolean(c.getString(R.string.settings_confirm_exit),true);

        settingSupressAddExternalStorageNotification=
                prefs.getBoolean(c.getString(R.string.settings_suppress_add_external_storage_notification), false);

    };
	
	private boolean isDebuggable() {
		boolean result=false;
        PackageManager manager = appContext.getPackageManager();
        ApplicationInfo appInfo = null;
        try {
            appInfo = manager.getApplicationInfo(appContext.getPackageName(), 0);
        } catch (NameNotFoundException e) {
        	result=false;
        }
        if ((appInfo.flags & ApplicationInfo.FLAG_DEBUGGABLE) == ApplicationInfo.FLAG_DEBUGGABLE)
        	result=true;
//        Log.v("","debuggable="+result);
        return result;
    };
    
	static private boolean isScreenOn(Context context, CommonUtilities util) {
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        if (Build.VERSION.SDK_INT>=23) {
        	util.addDebugMsg(1, "I", "isDeviceIdleMode()="+pm.isDeviceIdleMode()+
            		", isPowerSaveMode()="+pm.isPowerSaveMode()+", isInteractive()="+pm.isInteractive());
        } else {
        	util.addDebugMsg(1, "I", "isPowerSaveMode()="+pm.isPowerSaveMode()+", isInteractive()="+pm.isInteractive());
        }
        return pm.isInteractive();
    };
	
}

