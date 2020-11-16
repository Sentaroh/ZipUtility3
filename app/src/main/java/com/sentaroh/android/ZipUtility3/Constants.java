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

@SuppressWarnings("unused")
public class Constants {

	public static final String APPLICATION_TAG="ZipUtility3";
	public static final String PACKAGE_NAME="com.sentaroh.android."+APPLICATION_TAG;
	public static final String APP_SPECIFIC_DIRECTORY="Android/data/com.sentaroh.android."+APPLICATION_TAG;
//	public static long SERIALIZABLE_NUMBER=1L;
	public static final String LOG_FILE_NAME=APPLICATION_TAG+"_log";

	public static final String DEFAULT_PREFS_FILENAME="default_preferences";
	
	public static final String WORK_DIRECTORY="Work";

	public static final String MIME_TYPE_ZIP="application/zip";
	public static final String MIME_TYPE_TEXT="text/plain";
	
	final static public String ENCODING_NAME_UTF8="UTF-8";
	
	final static public int IO_AREA_SIZE=1024*1024;

	final static public String[] ENCODING_NAME_LIST=new String[] {
		"EUC-JP","EUC-KR",
		"EUC-CN","EUC-TW",
		"GB18030",
		"ISO-2022-CN", "ISO-2022-JP","ISO-2022-KR","ISO-8859-5","ISO-8859-7","ISO-8859-8",
		"SHIFT_JIS","US-ASCII",
		"UTF-8","UTF-16BE","UTF-16LE","UTF-32BE","UTF-32LE",
		"WINDOWS-1251","WINDOWS-1252","WINDOWS-1253","WINDOWS-1255"};
	
	public static final int ACTIVITY_REQUEST_CODE_SDCARD_STORAGE_ACCESS=40;
    public static final int ACTIVITY_REQUEST_CODE_USB_STORAGE_ACCESS=50;

	public final static String SERVICE_HEART_BEAT="com.sentaroh.android."+APPLICATION_TAG+".ACTION_SERVICE_HEART_BEAT";

}
