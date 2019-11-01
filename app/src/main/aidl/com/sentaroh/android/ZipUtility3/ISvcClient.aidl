package com.sentaroh.android.ZipUtility3;

import com.sentaroh.android.ZipUtility3.ISvcCallback;

interface ISvcClient{
	
	void setCallBack(ISvcCallback callback);
	void removeCallBack(ISvcCallback callback);

	void aidlStopService() ;
	
	void aidlSetActivityInBackground() ;
	void aidlSetActivityInForeground() ;
	
	void aidlUpdateNotificationMessage(String msg_text) ;
}