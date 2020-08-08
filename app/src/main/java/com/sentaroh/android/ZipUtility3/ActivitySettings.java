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
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;

import java.util.List;
import java.util.Locale;

import static com.sentaroh.android.ZipUtility3.GlobalParameters.DEFAULT_OPEN_AS_TEXT_FILE_TYPE;

public class ActivitySettings extends PreferenceActivity {
	private static Context mContext=null;
	private static PreferenceFragment mPrefFrag=null;

	private static ActivitySettings mPrefActivity=null;

	private static GlobalParameters mGp=null;

	private CommonUtilities mUtil=null;

    private static String mCurrentThemeLangaue= GlobalParameters.LANGUAGE_USE_SYSTEM_SETTING;
//	private GlobalParameters mGp=null;

	@Override
	protected boolean isValidFragment(String fragmentName) {
		return true;
	}

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(new GlobalParameters().setNewLocale(base, false));
    }

    static final public String LANGUAGE_KEY="language";

    @Override
	public void onCreate(Bundle savedInstanceState) {
		mContext=ActivitySettings.this;
        SharedPreferences shared_pref = PreferenceManager.getDefaultSharedPreferences(mContext);
		mGp=GlobalWorkArea.getGlobalParameters(mContext);
		setTheme(mGp.applicationTheme);
		super.onCreate(savedInstanceState);
		mPrefActivity=ActivitySettings.this;
        mCurrentThemeLangaue=shared_pref.getString(getString(R.string.settings_language), GlobalParameters.LANGUAGE_USE_SYSTEM_SETTING);
		if (mUtil==null) mUtil=new CommonUtilities(mContext, "SettingsActivity", mGp, null);
		mUtil.addDebugMsg(1, "I", CommonUtilities.getExecutedMethodName()+" entered");
		if (mGp.settingFixDeviceOrientationToPortrait) setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		else setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);

		Intent in=new Intent();
		in.putExtra(LANGUAGE_KEY, mCurrentThemeLangaue);
		setResult(Activity.RESULT_OK, in);
	}

	@Override
	public void onStart(){
		super.onStart();
		mUtil.addDebugMsg(1, "I", CommonUtilities.getExecutedMethodName()+" entered");
	};

	@Override
	public void onResume(){
		super.onResume();
		mUtil.addDebugMsg(1, "I", CommonUtilities.getExecutedMethodName()+" entered");
//		setTitle(R.string.settings_main_title);
	};

	@Override
	public void onBuildHeaders(List<Header> target) {
		mUtil.addDebugMsg(1, "I", CommonUtilities.getExecutedMethodName()+" entered");
		loadHeadersFromResource(R.xml.settings_frag, target);
	};

	@Override
	public boolean onIsMultiPane () {
		mContext=ActivitySettings.this.getApplicationContext();
		mGp=GlobalWorkArea.getGlobalParameters(mContext);
//    	mPrefActivity=this;
		mUtil=new CommonUtilities(mContext, "SettingsActivity", mGp, null);
		mUtil.addDebugMsg(1, "I", CommonUtilities.getExecutedMethodName()+" entered");
		return isTablet(mContext, mUtil);
	};

    public static boolean isTablet(Context context, CommonUtilities cu) {
        int multiPaneDP=540;
        String lang_code=Locale.getDefault().getLanguage();
        if (lang_code.equals("en")) multiPaneDP=500;
        else if (lang_code.equals("fr")) multiPaneDP=540;
        else if (lang_code.equals("ja")) multiPaneDP=500;
        else if (lang_code.equals("ru")) multiPaneDP=1000;
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        final float x_px = (float) Math.min(metrics.heightPixels, metrics.widthPixels);
        final float y_px = (float) Math.max(metrics.heightPixels, metrics.widthPixels);
        boolean portrait_mp = (x_px/metrics.density) >= multiPaneDP;
        boolean land_mp = (y_px/metrics.density) >= multiPaneDP;

        int orientation = context.getResources().getConfiguration().orientation;
        boolean sc_land_mp = land_mp && orientation == Configuration.ORIENTATION_LANDSCAPE; //screen is in landscape orientation and width size >= multiPaneDP
//        cu.addDebugMsg(1, "I", "orientation="+orientation+", density="+metrics.density+", x_dpi="+metrics.xdpi+", y_dpi="+metrics.ydpi+
//                ", densityDpi="+metrics.densityDpi+", heightPixels="+metrics.heightPixels+", widthPixels="+metrics.widthPixels+", sz_mp="+sz_mp+", sc_or="+sc_or);
        return portrait_mp||sc_land_mp; //use MultiPane display in portrait if width >= multiPaneDP or in landscape if largest screen side >= multiPaneDP
    }

	@Override
	protected void onPause() {
		super.onPause();
		mUtil.addDebugMsg(1, "I", CommonUtilities.getExecutedMethodName()+" entered");
	};

	@Override
	final public void onStop() {
		super.onStop();
		mUtil.addDebugMsg(1, "I", CommonUtilities.getExecutedMethodName()+" entered");
	};

	@Override
	final public void onDestroy() {
		super.onDestroy();
		mUtil.addDebugMsg(1, "I", CommonUtilities.getExecutedMethodName()+" entered");
	};

	public static class SettingsMisc extends PreferenceFragment {
        private SharedPreferences.OnSharedPreferenceChangeListener listenerAfterHc = null;
        private PreferenceFragment mPrefFrag=null;
		private CommonUtilities mUtil=null;
		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
            mPrefFrag=this;
            listenerAfterHc = new SharedPreferences.OnSharedPreferenceChangeListener() {
                public void onSharedPreferenceChanged(SharedPreferences shared_pref, String key_string) {
                    checkSettingValue(getContext(), mUtil, shared_pref, key_string);
                }
            };
			mUtil=new CommonUtilities(getContext(), "SettingsMisc", mGp, null);
			mUtil.addDebugMsg(1, "I", CommonUtilities.getExecutedMethodName()+" entered");

			addPreferencesFromResource(R.xml.settings_frag_misc);

			SharedPreferences shared_pref = PreferenceManager.getDefaultSharedPreferences(getContext());

			shared_pref.edit().putBoolean(getString(R.string.settings_exit_clean),true).commit();
			findPreference(getString(R.string.settings_exit_clean).toString()).setEnabled(false);
			checkSettingValue(getContext(), mUtil, shared_pref,getString(R.string.settings_exit_clean));
		};

        private boolean checkSettingValue(Context c, CommonUtilities ut, SharedPreferences shared_pref, String key_string) {
            boolean isChecked = false;
            Preference pref_key=mPrefFrag.findPreference(key_string);

            if (key_string.equals(c.getString(R.string.settings_exit_clean))) {
                isChecked=true;
            }

            return isChecked;
        };

        @Override
		public void onStart() {
			super.onStart();
			mUtil.addDebugMsg(1, "I", CommonUtilities.getExecutedMethodName()+" entered");
			getPreferenceScreen().getSharedPreferences()
					.registerOnSharedPreferenceChangeListener(listenerAfterHc);
			getActivity().setTitle(R.string.settings_misc_title);
		};
		@Override
		public void onStop() {
			super.onStop();
			mUtil.addDebugMsg(1, "I", CommonUtilities.getExecutedMethodName()+" entered");
			getPreferenceScreen().getSharedPreferences()
					.unregisterOnSharedPreferenceChangeListener(listenerAfterHc);
		};
	};

	public static class SettingsCompress extends PreferenceFragment {
        private SharedPreferences.OnSharedPreferenceChangeListener listenerAfterHc = null;
        private PreferenceFragment mPrefFrag=null;
        private CommonUtilities mUtil=null;
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            mPrefFrag=this;
            listenerAfterHc = new SharedPreferences.OnSharedPreferenceChangeListener() {
                public void onSharedPreferenceChanged(SharedPreferences shared_pref, String key_string) {
                    checkSettingValue(getContext(), mUtil, shared_pref, key_string);
                }
            };
            mUtil=new CommonUtilities(getContext(), "SettingsCompress", mGp, null);
            mUtil.addDebugMsg(1, "I", CommonUtilities.getExecutedMethodName()+" entered");

            addPreferencesFromResource(R.xml.settings_frag_compress);

            SharedPreferences shared_pref = PreferenceManager.getDefaultSharedPreferences(getContext());

            checkSettingValue(getContext(), mUtil, shared_pref,getString(R.string.settings_no_compress_file_type));
			checkSettingValue(getContext(), mUtil, shared_pref,getString(R.string.settings_zip_default_encoding));

		};

        private boolean checkSettingValue(Context c, CommonUtilities ut, SharedPreferences shared_pref, String key_string) {
            boolean isChecked = false;
            Preference pref_key=mPrefFrag.findPreference(key_string);

            if (key_string.equals(c.getString(R.string.settings_no_compress_file_type))) {
                isChecked=true;
                pref_key.setSummary(shared_pref.getString(key_string, ""));
            } else if (key_string.equals(c.getString(R.string.settings_zip_default_encoding))) {
                isChecked=true;
                pref_key.setSummary(shared_pref.getString(key_string, ""));
            }

            return isChecked;
        };

		@Override
		public void onStart() {
			super.onStart();
			mUtil.addDebugMsg(1, "I", CommonUtilities.getExecutedMethodName()+" entered");
			getPreferenceScreen().getSharedPreferences()
					.registerOnSharedPreferenceChangeListener(listenerAfterHc);
			getActivity().setTitle(R.string.settings_ui_title);
		};
		@Override
		public void onStop() {
			super.onStop();
			mUtil.addDebugMsg(1, "I", CommonUtilities.getExecutedMethodName()+" entered");
			getPreferenceScreen().getSharedPreferences()
					.unregisterOnSharedPreferenceChangeListener(listenerAfterHc);
		};
	};

	public static class SettingsUi extends PreferenceFragment {
		private SharedPreferences.OnSharedPreferenceChangeListener listenerAfterHc = null;
		private PreferenceFragment mPrefFrag=null;
		private CommonUtilities mUtil=null;
		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			mPrefFrag=this;
            listenerAfterHc = new SharedPreferences.OnSharedPreferenceChangeListener() {
                public void onSharedPreferenceChanged(SharedPreferences shared_pref, String key_string) {
                    checkSettingValue(getContext(), mUtil, shared_pref, key_string);
                }
            };
			mUtil=new CommonUtilities(getContext(), "SettingsUi", mGp, null);
			mUtil.addDebugMsg(1, "I", CommonUtilities.getExecutedMethodName()+" entered");

			addPreferencesFromResource(R.xml.settings_frag_ui);

			SharedPreferences shared_pref = PreferenceManager.getDefaultSharedPreferences(mContext);

//            mCurrentThemeLangaue=shared_pref.getString(getString(R.string.settings_language), GlobalParameters.LANGUAGE_USE_SYSTEM_SETTING);

			checkSettingValue(getContext(), mUtil, shared_pref,getString(R.string.settings_use_light_theme));
            checkSettingValue(getContext(), mUtil, shared_pref,getString(R.string.settings_language));
			checkSettingValue(getContext(), mUtil, shared_pref,getString(R.string.settings_device_orientation_portrait));
            checkSettingValue(getContext(), mUtil, shared_pref,getString(R.string.settings_open_as_text_file_type));
            checkSettingValue(getContext(), mUtil, shared_pref,getString(R.string.settings_confirm_exit));

		};

        private boolean checkSettingValue(Context c, CommonUtilities ut, SharedPreferences shared_pref, String key_string) {
            boolean isChecked = false;
            Preference pref_key=mPrefFrag.findPreference(key_string);
            if (key_string.equals(c.getString(R.string.settings_use_light_theme))) {
                isChecked=true;
            } else if (key_string.equals(c.getString(R.string.settings_language))) {
                isChecked=true;
                String lang_value=shared_pref.getString(key_string, GlobalParameters.LANGUAGE_USE_SYSTEM_SETTING);
                String[] lang_msgs = c.getResources().getStringArray(R.array.settings_language_list_entries);
                String sum_msg = lang_msgs[Integer.parseInt(lang_value)];
                pref_key.setSummary(sum_msg);
                if (!lang_value.equals(mCurrentThemeLangaue)) {
                    getActivity().finish();//relaunch current preferences activity. Will trigger to prompt for restart app when back to main activity
                    mGp.setNewLocale(getActivity(), true);
                    Intent intent = new Intent(getActivity(), ActivitySettings.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    getActivity().startActivity(intent);
                }

            } else if (key_string.equals(c.getString(R.string.settings_device_orientation_portrait))) {
                isChecked=true;
            } else if (key_string.equals(c.getString(R.string.settings_confirm_exit))) {
                isChecked=true;
            } else if (key_string.equals(c.getString(R.string.settings_open_as_text_file_type))) {
                isChecked=true;
                pref_key.setSummary(shared_pref.getString(key_string, DEFAULT_OPEN_AS_TEXT_FILE_TYPE));
            }

            return isChecked;
        };

		@Override
		public void onStart() {
			super.onStart();
			mUtil.addDebugMsg(1, "I", CommonUtilities.getExecutedMethodName()+" entered");
			getPreferenceScreen().getSharedPreferences()
					.registerOnSharedPreferenceChangeListener(listenerAfterHc);
			getActivity().setTitle(R.string.settings_ui_title);
		};
		@Override
		public void onStop() {
			super.onStop();
			mUtil.addDebugMsg(1, "I", CommonUtilities.getExecutedMethodName()+" entered");
			getPreferenceScreen().getSharedPreferences()
					.unregisterOnSharedPreferenceChangeListener(listenerAfterHc);
		};
	};
}