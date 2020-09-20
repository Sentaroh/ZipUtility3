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
import android.app.AlarmManager;
import android.app.Notification;
import android.app.Notification.Builder;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.BitmapFactory;
import android.hardware.usb.UsbManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.RemoteException;
import android.os.SystemClock;

import com.sentaroh.android.Utilities3.SafStorage3;
import com.sentaroh.android.ZipUtility3.Log.LogUtil;

import java.util.ArrayList;

import static com.sentaroh.android.ZipUtility3.Constants.SERVICE_HEART_BEAT;

public class ZipService extends Service {
	private GlobalParameters mGp=null;
	
	private CommonUtilities mUtil=null;
	
	private Context mContext=null;
	
//	private SleepReceiver mSleepReceiver=new SleepReceiver();
	
	private WakeLock mPartialWakelock=null;
	
	@Override
	public void onCreate() {
		super.onCreate();
        mContext=getApplicationContext();
		mGp=GlobalWorkArea.getGlobalParameters(mContext);
		mUtil=new CommonUtilities(getApplicationContext(), "Service", mGp, null);
		
		mUtil.addDebugMsg(1,"I","onCreate entered");
		
//		IntentFilter int_filter = new IntentFilter();
//        int_filter.addAction(Intent.ACTION_SCREEN_OFF);
//        int_filter.addAction(Intent.ACTION_SCREEN_ON);
//        int_filter.addAction(Intent.ACTION_USER_PRESENT);
//        registerReceiver(mSleepReceiver, int_filter);

    	mPartialWakelock=((PowerManager)getSystemService(Context.POWER_SERVICE))
    			.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, "ZipUtility-Partial");
        initNotification();
	}

	private void setHeartBeat() {
//		if (Build.VERSION.SDK_INT>=21) {
////			Thread.dumpStack();
//			long time= System.currentTimeMillis()+1000*5;
////			Intent in = new Intent(mContext, SyncService.class);
//			Intent in = new Intent(mContext, ZipService.class);
//			in.setAction(SERVICE_HEART_BEAT);
//			PendingIntent pi = PendingIntent.getService(mContext, 0, in, PendingIntent.FLAG_UPDATE_CURRENT);
////			PendingIntent pi = PendingIntent.getService(mContext, 0, in, PendingIntent.FLAG_UPDATE_CURRENT);
//		    AlarmManager am = (AlarmManager)mContext.getSystemService(Context.ALARM_SERVICE);
//		    if (Build.VERSION.SDK_INT>=23) am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, time, pi);
//		    else am.set(AlarmManager.RTC_WAKEUP, time, pi);
//		}
	}
	
	private void cancelHeartBeat() {
//		Intent in = new Intent(mContext, SyncService.class);
        Intent in = new Intent(mContext, ZipService.class);
		in.setAction(SERVICE_HEART_BEAT);
		PendingIntent pi = PendingIntent.getService(mContext, 0, in, PendingIntent.FLAG_CANCEL_CURRENT);
//		PendingIntent pi = PendingIntent.getService(mContext, 0, in, PendingIntent.FLAG_CANCEL_CURRENT);
	    AlarmManager am = (AlarmManager)mContext.getSystemService(Context.ALARM_SERVICE);
	    am.cancel(pi);
	};
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		super.onStartCommand(intent, flags, startId);
		WakeLock wl=((PowerManager)getSystemService(Context.POWER_SERVICE))
    			.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK| PowerManager.ACQUIRE_CAUSES_WAKEUP, "ZipUtility-Service-1");
		wl.acquire();
		String action="";
		if (intent!=null) if (intent.getAction()!=null) action=intent.getAction();
		if (action.equals(SERVICE_HEART_BEAT)) {
            setHeartBeat();
        } else if (action.equals(Intent.ACTION_MEDIA_MOUNTED) || action.equals(Intent.ACTION_MEDIA_UNMOUNTED) ||
                action.equals(UsbManager.ACTION_USB_DEVICE_ATTACHED)|| action.equals(UsbManager.ACTION_USB_DEVICE_DETACHED)) {
            mUtil.addDebugMsg(1,"I","onStartCommand entered, action="+action);
            Handler hndl=new Handler();
            final String f_action=action;
            Thread th=new Thread() {
                @Override
                public void run() {
                    int prev_ls=mGp.safMgr.getSafStorageList().size();
                    for(int i=0;i<10;i++) {
                        mGp.refreshMediaDir(mContext);
                        ArrayList<SafStorage3> new_list=mGp.safMgr.getSafStorageList();
                        if (prev_ls!=new_list.size()) {
                            hndl.post(new Runnable() {
                                @Override
                                public void run() {
                                    if (mGp.callbackStub!=null) {
                                        try {
                                            mGp.callbackStub.cbNotifyMediaStatus(f_action);
                                        } catch (RemoteException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }
                            });
                            break;
                        } else {
                            SystemClock.sleep(300);
                        }
//                        mUtil.addDebugMsg(1,"I","i="+i+", cnt="+prev_ls+", new="+new_list.size());
                    }
                }
            };
            th.start();
		} else {
			mUtil.addDebugMsg(2,"I","onStartCommand entered, action="+action);
		}
		wl.release();
//		if (isServiceToBeStopped()) stopSelf();
		return START_STICKY;
	};

	@Override
	public IBinder onBind(Intent intent) {
		mUtil.addDebugMsg(1,"I",CommonUtilities.getExecutedMethodName()+" entered,action="+intent.getAction());
		setActivityForeground();
//		if (arg0.getAction().equals("MessageConnection")) 
			return mSvcClientStub;
//		else return svcInterface;
	};
	
	@Override
	public boolean onUnbind(Intent intent) {
		mUtil.addDebugMsg(1,"I",CommonUtilities.getExecutedMethodName()+" entered");
		return super.onUnbind(intent);
	};
	
	@Override
	public void onLowMemory() {
		super.onLowMemory();
		mUtil.addDebugMsg(1, "I", "onLowMemory entered");
        // Application process is follow
		
	};
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		mUtil.addDebugMsg(1,"I",CommonUtilities.getExecutedMethodName()+" entered");
//		unregisterReceiver(mSleepReceiver);
		cancelHeartBeat();
		stopForeground(true);
		LogUtil.closeLog(mContext);
		if (mGp.settingExitClean) {
            android.os.Process.killProcess(android.os.Process.myPid());
//			Handler hndl=new Handler();
//			hndl.postDelayed(new Runnable(){
//				@Override
//				public void run() {
//					android.os.Process.killProcess(android.os.Process.myPid());
//				}
//			}, 100);
		} else {
//			mGp=null;
			System.gc();
		}
	};

	
    final private ISvcClient.Stub mSvcClientStub = new ISvcClient.Stub() {
		@Override
		public void setCallBack(ISvcCallback callback)
				throws RemoteException {
			mUtil.addDebugMsg(1, "I", CommonUtilities.getExecutedMethodName()+" entered");
			mGp.callbackStub=callback;
		}

		@Override
		public void removeCallBack(ISvcCallback callback)
				throws RemoteException {
			mUtil.addDebugMsg(1, "I", CommonUtilities.getExecutedMethodName()+" entered");
			mGp.callbackStub=null;
		}

		@Override
		public void aidlStopService() throws RemoteException {
			stopSelf();
		}

		@Override
		public void aidlSetActivityInBackground() throws RemoteException {
			mUtil.addDebugMsg(1, "I", CommonUtilities.getExecutedMethodName()+" entered");
			setActivityBackground();
		}
		
		@Override
		public void aidlSetActivityInForeground() throws RemoteException {
			mUtil.addDebugMsg(1, "I", CommonUtilities.getExecutedMethodName()+" entered");
			setActivityForeground();
		}

		@Override
		public void aidlUpdateNotificationMessage(String msg_text) throws RemoteException {
			mUtil.addDebugMsg(1, "I", CommonUtilities.getExecutedMethodName()+" entered");
			showNotification(msg_text);
		}

    };

    private void initNotification() {
        NotificationManager nm=(NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT>=26) {
            NotificationChannel channel = new NotificationChannel(
                    "ZipUtility",
                    "ZipUtility",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            channel.enableLights(false);
            channel.setSound(null,null);
//            channel.setLightColor(Color.GREEN);
            channel.enableVibration(false);
            channel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
            nm.deleteNotificationChannel("ZipUtility");
            nm.createNotificationChannel(channel);
        }

        mNotificationBuilder=new Builder(mContext);
        mNotificationBuilder.setWhen(System.currentTimeMillis())
                .setContentTitle(mContext.getString(R.string.msgs_main_notification_title))
                .setContentText(mContext.getString(R.string.msgs_main_notification_message))
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.zip_utility))
                .setSmallIcon(R.drawable.ic_32_file_zip);
        Intent activity_intent = new Intent(mContext, ActivityMain.class);
        PendingIntent activity_pi= PendingIntent.getActivity(mContext, 0, activity_intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        mNotificationBuilder.setContentIntent(activity_pi);
        if (Build.VERSION.SDK_INT>=26) {
            mNotificationBuilder.setChannelId("ZipUtility");
        }

    }

    private void showNotification(String msg_text) {
        if (mNotificationBuilder!=null) {
            mNotificationBuilder
                    .setWhen(System.currentTimeMillis())
                    .setContentText(msg_text);
            if (mNotification!=null) {
                mNotification=mNotificationBuilder.build();
                NotificationManager nm=(NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
                nm.notify(R.string.app_name, mNotification);
            }
        }
    }

    private void setActivityForeground() {
		mGp.activityIsBackground=false;
		cancelHeartBeat();
		if (mPartialWakelock.isHeld()) mPartialWakelock.release();
		stopForeground(true);
		mNotification=null;
    };

    private Notification.Builder mNotificationBuilder=null;
    private Notification mNotification=null;
    private void setActivityBackground() {
		mGp.activityIsBackground=true;
		if (!mPartialWakelock.isHeld()) mPartialWakelock.acquire();;
		setHeartBeat();

		mNotificationBuilder.setWhen(System.currentTimeMillis())
	    	.setContentText(mContext.getString(R.string.msgs_main_notification_message));

		mNotification=mNotificationBuilder.build();
		NotificationManager nm=(NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
		nm.notify(R.string.app_name, mNotification);
		
		startForeground(R.string.app_name, mNotification);
    };

//    final private class SleepReceiver  extends BroadcastReceiver {
//		@Override
//		final public void onReceive(Context c, Intent in) {
//			String action = in.getAction();
//			if(action.equals(Intent.ACTION_SCREEN_ON)) {
//			} else if(action.equals(Intent.ACTION_SCREEN_OFF)) {
//			} else if(action.equals(Intent.ACTION_USER_PRESENT)) {
//			}
//		}
//    };

}