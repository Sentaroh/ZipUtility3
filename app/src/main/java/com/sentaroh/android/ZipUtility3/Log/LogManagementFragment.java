package com.sentaroh.android.ZipUtility3.Log;

import android.content.Context;
import android.os.Bundle;

import com.sentaroh.android.Utilities3.LogUtil.CommonLogManagementFragment;
import static com.sentaroh.android.ZipUtility3.Constants.APPLICATION_TAG;
import com.sentaroh.android.ZipUtility3.R;


public class LogManagementFragment extends CommonLogManagementFragment {
    public static LogManagementFragment newInstance(Context c, boolean retainInstance, String title) {
        LogManagementFragment frag = new LogManagementFragment();
        Bundle bundle = new Bundle();
        bundle.putBoolean("retainInstance", retainInstance);
        bundle.putString("title", title);
        bundle.putString("msgtext", c.getString(R.string.msgs_log_management_send_log_file_warning));
        bundle.putString("enableMsg", c.getString(R.string.msgs_log_management_enable_log_file_warning));
        bundle.putString("subject", APPLICATION_TAG+" log");
        bundle.putString("hint", c.getString(R.string.msgs_log_management_enable_log_file_hint));
        frag.setArguments(bundle);
        return frag;
    }

}