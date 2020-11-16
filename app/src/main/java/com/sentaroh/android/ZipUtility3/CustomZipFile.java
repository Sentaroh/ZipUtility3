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

import android.content.Context;

import com.sentaroh.android.Utilities3.SafFile3;
import com.sentaroh.android.Utilities3.Zip.SeekableInputStream;
import com.sentaroh.android.Utilities3.Zip.ZipUtil;

import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.io.inputstream.ZipInputStream;
import net.lingala.zip4j.model.FileHeader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;

public class CustomZipFile {
    private static Logger log= LoggerFactory.getLogger(CustomZipFile.class);
    private Context mContext=null;
    private SafFile3 mSafFile=null;
    private String mEncoding="UTF-8";
    public CustomZipFile(Context c, SafFile3 sf, String encoding) {
        mContext=c;
        mSafFile=sf;
        mEncoding=encoding;
    }

    public SafFile3 getSafFile() {
        return mSafFile;
    }

    public String getEncoding() {
        return mEncoding;
    }

    public ArrayList<FileHeader> getFileHeaders() throws IOException {
        if (mSafFile.isSafFile()) return ZipUtil.getFileHeaders(mContext, mSafFile.getUri(), mEncoding);
        else return ZipUtil.getFileHeaders(mContext, mSafFile.getFile(), mEncoding);
    }

    private ArrayList<FileHeader> mFileHeaderList=null;
    public FileHeader getFileHeader(String fh_name) throws IOException {
        if (mFileHeaderList==null) {
            if (mSafFile.isSafFile()) mFileHeaderList=ZipUtil.getFileHeaders(mContext, mSafFile.getUri(), mEncoding);
            else mFileHeaderList=ZipUtil.getFileHeaders(mContext, mSafFile.getFile(), mEncoding);
        }
        try {
            return ZipUtil.getFileHeader(mFileHeaderList, fh_name);
        } catch (ZipException e) {
            log.error("getFileHeader error",e);
            e.printStackTrace();
            return null;
        }
    }

//    public FileHeader getFileHeader(String fh_name) {
//        ArrayList<FileHeader> fh_list=null;
//        if (mSafFile.isSafFile()) fh_list=ZipUtil.getFileHeaders(mContext, mSafFile.getUri(), mEncoding);
//        else fh_list=ZipUtil.getFileHeaders(mContext, mSafFile.getFile(), mEncoding);
//        try {
//            return ZipUtil.getFileHeader(fh_list, fh_name);
//        } catch (ZipException e) {
//            log.error("getFileHeader error",e);
//            e.printStackTrace();
//            return null;
//        }
//    }

    private String mPassword=null;
    public void setPassword(String pswd) {
        mPassword=pswd;
    }
    public String getPassword() {
        return mPassword;
    }


    public ZipInputStream getInputStream(FileHeader fh) throws Exception {
//        SplitInputStream splitInputStream = null;
        long b_time=System.currentTimeMillis();
        try {
            InputStream is=null;
            if (mSafFile.isSafFile()) is=mSafFile.getInputStream();
            else is=new FileInputStream(mSafFile.getFile());

            is.skip(fh.getOffsetLocalHeader());
            ZipInputStream zipInputStream = null;
            if (getPassword()==null) zipInputStream =new ZipInputStream(is, Charset.forName(mEncoding));
            else zipInputStream =new ZipInputStream(is, mPassword.toCharArray(), Charset.forName(mEncoding));
            zipInputStream.getNextEntry();

            return zipInputStream;
        } catch (IOException e) {
            throw e;
        }

    }
}
