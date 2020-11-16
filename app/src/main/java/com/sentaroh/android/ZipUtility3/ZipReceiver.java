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
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbManager;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;

import com.sentaroh.android.ZipUtility3.Log.LogUtil;

public class ZipReceiver extends BroadcastReceiver {

    private static Context mContext = null;

//    private static GlobalParameters mGp = null;

    private static LogUtil mLog = null;

    @Override
    final public void onReceive(Context c, Intent received_intent) {
        WakeLock wl =
                ((PowerManager) c.getSystemService(Context.POWER_SERVICE))
                        .newWakeLock(PowerManager.PARTIAL_WAKE_LOCK
                                | PowerManager.ON_AFTER_RELEASE, "Receiver");
        wl.acquire(1000);
        mContext = c;
//        if (mGp == null) {
//            mGp = new GlobalParameters();
//            mGp.appContext = c;
//        }
//        mGp.loadSettingsParms(c);

        if (mLog == null) mLog = new LogUtil(c, "Receiver");

        String action = received_intent.getAction();
        if (action != null) {
            if (action.equals(Intent.ACTION_MEDIA_MOUNTED) ||
                    action.equals(Intent.ACTION_MEDIA_EJECT) ||
                    action.equals(Intent.ACTION_MEDIA_REMOVED) ||
                    action.equals(Intent.ACTION_MEDIA_UNMOUNTED) ||
                    action.equals(UsbManager.ACTION_USB_DEVICE_ATTACHED) ||
                    action.equals(UsbManager.ACTION_USB_DEVICE_DETACHED)
            ) {
                mLog.addDebugMsg(1, "I", "Receiver action=" + action);
                Intent in = new Intent(mContext, ZipService.class);
                in.setAction(action);
                in.setData(received_intent.getData());
                if (received_intent.getExtras() != null) in.putExtras(received_intent.getExtras());
                try {
                    mContext.startService(in);
                } catch(Exception e) {
                    e.printStackTrace();
                }
            } else {
                mLog.addDebugMsg(1, "I", "Receiver action=" + action);
            }
        }
    }

}
