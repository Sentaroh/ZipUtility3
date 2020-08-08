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

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.Handler;
import android.os.LocaleList;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.widget.Button;

import androidx.annotation.RequiresApi;

import com.sentaroh.android.Utilities3.SafManager3;
import com.sentaroh.android.Utilities3.ThemeColorList;
import com.sentaroh.android.ZipUtility3.Log.LogUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.LoggerWriter;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;

public class GlobalParameters {
    private static Logger log = LoggerFactory.getLogger(GlobalParameters.class);

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

    public static final String LANGUAGE_USE_SYSTEM_SETTING = "0";
    public static final String LANGUAGE_INIT = "-99";//ensure onStartSettingScreenThemeLanguageValue is assigned language value only at first app start and when language change by user
    public static String settingLanguage = LANGUAGE_USE_SYSTEM_SETTING;//holds language code (fr, en... or "0" for system default)
    public static String settingLanguageValue = LANGUAGE_USE_SYSTEM_SETTING;//language value (array index) in listPreferences: "0" for system default, "1" for english...
//    public static String onStartSettingLanguageValue = LANGUAGE_INIT;//on first App start, it will be assigned the active language value

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
        log.setWriter(slf4j_lw);


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

        if (!prefs.contains(c.getString(R.string.settings_language)))
            prefs.edit().putString(c.getString(R.string.settings_language), LANGUAGE_USE_SYSTEM_SETTING).commit();

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

        loadLanguagePreference(c);

    };

    //+ To use createConfigurationContext() non deprecated method:
    //  - set LANGUAGE_LOCALE_USE_NEW_API to true
    //  - use as wrapper in attachBaseContext() for all activities and App or extend all App/Activities from a base Activity Class with attachBaseContext()
    //  - for App: implement also in onConfigurationChanged(), not needed in AppCompatActivity class
    //  - in ActivityMain, when wrapping language in attachBaseContext(), we must first load language value by calling loadLanguagePreference(context)
    //    because preferences manager is init later in onCreate()
    //+ To use updateConfiguration() deprecated method on new API:
    //  - set LANGUAGE_LOCALE_USE_NEW_API to false
    //  - no need to use wrapper in attachBaseContext()
    //  - call "mGp.setNewLocale(this, false)" in onCreate() and onConfigurationChanged() of all activities and App, not needed in Activity fragments
    //  - SyncTaskEditor.java onCreate() and onConfigurationChanged() must be also edited
    //  - all activities and App in AndroidManifest must have: android:configChanges="locale|orientation|screenSize|keyboardHidden|layoutDirection"
    public Context setNewLocale(Context c, boolean init) {
        if (init) loadLanguagePreference(c);
        return updateLanguageResources(c, settingLanguage);
    }

    // wrap language layout in the base context for all activities
    private Context updateLanguageResources(Context context, String language) {
        //if language is set to system default (defined as "0"), do not apply non existing language code "0" and return current context without wrapped language
        if (!language.equals(LANGUAGE_USE_SYSTEM_SETTING)) {
            Locale locale = new Locale(language);
            Locale.setDefault(locale);

            Resources res = context.getResources();
            Configuration config = new Configuration(res.getConfiguration());
            if (Build.VERSION.SDK_INT >= 24) {//we can ignore and use "config.setLocale(locale)" like Build.VERSION.SDK_INT >= 21 if we target above API 24 (API 24 only bug)
                setLocaleForApi24(config, locale);
                context = context.createConfigurationContext(config);
            } else if (Build.VERSION.SDK_INT >= 21) {
                config.setLocale(locale);
                context = context.createConfigurationContext(config);
            } else {
                config.locale = locale;
                res.updateConfiguration(config, res.getDisplayMetrics());
            }
        }
        return context;
    }

    //workaround a bug issue in Android N, but not needed after N
    @RequiresApi(api = 24)
    private void setLocaleForApi24(Configuration config, Locale target) {
        Set<Locale> set = new LinkedHashSet<>();
        // bring the target locale to the front of the list
        set.add(target);

        LocaleList all = LocaleList.getDefault();
        for (int i = 0; i < all.size(); i++) {
            // append other locales supported by the user
            set.add(all.get(i));
        }

        Locale[] locales = set.toArray(new Locale[0]);
        config.setLocales(new LocaleList(locales));
    }

    //load language list value from listPreferences. New languages can be added with Excel template without any change in code.
    //Language list entries must contain "(language_code)" string, exp "English (en)"
    //"English" will be the preferences menu description and "en" the language code
    //settingScreenThemeLanguageValue and settingScreenThemeLanguage are updated only here by loadLanguagePreference()
    //loadLanguagePreference() is called on mainActivity start, any mainActivity result, SyncReceiver, and any init to GlobalParameters (onCreate of most activities)
    //onStartSettingScreenThemeLanguageValue: is the currently selected language by user that will be applied on next app launch
    //it is assigned the value of current active language (settingScreenThemeLanguage) at first app start
    //if user changes language or when import config file with new language, settingScreenThemeLanguage is immeadiately assigned the new language
    //when back to MainActivity, if settingScreenThemeLanguage != onStartSettingScreenThemeLanguageValue, prompted to restart
    //onStartSettingScreenThemeLanguageValue is assigned the new selected language even if user doesn't restart app
    //it reflects current user selected language that will be effective on next app start
    public void loadLanguagePreference(Context c) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(c);
        settingLanguageValue = prefs.getString(c.getString(R.string.settings_language), LANGUAGE_USE_SYSTEM_SETTING);
        String[] lang_entries = c.getResources().getStringArray(R.array.settings_language_list_entries);
        if (settingLanguageValue.equals(LANGUAGE_USE_SYSTEM_SETTING)) {
            settingLanguage = LANGUAGE_USE_SYSTEM_SETTING;
        } else {
            settingLanguage = lang_entries[Integer.parseInt(settingLanguageValue)].split("[\\(\\)]")[1]; // language entries are in the format "description (languageCode)"
        }
//        if (onStartSettingLanguageValue.equals(LANGUAGE_INIT))
//            onStartSettingLanguageValue = settingLanguageValue;
    }

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

