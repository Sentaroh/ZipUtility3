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
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import android.webkit.MimeTypeMap;

import com.sentaroh.android.Utilities3.MiscUtil;
import com.sentaroh.android.Utilities3.StringUtil;
import com.sentaroh.android.Utilities3.Zip.ZipFileListItem;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.io.StreamCorruptedException;


public class TreeFilelistItem 
		implements Cloneable, Serializable, Comparable<TreeFilelistItem> {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private String fileName;
	private String mimeType="";
	private String fileExt="";
    private String fileSize="0", fileLastModDate="", fileLastModTime="";
    private boolean zipFileItem=false;
	private boolean isDirectory=false;
	private boolean isEncrypted=false;
	private long fileLength;
	private long lastModdate;
	private boolean isChecked=false;
	private boolean canRead=false;
	private boolean isHidden=false;
	private boolean canWrite=false;
	private String filePath;
	private boolean childListExpanded=false;
	private int listLevel=0;
	private boolean hideListItem=false;
	private boolean subDirLoaded=false;
	private int subDirItemCount=0;
	private boolean triState=false;
	private boolean enableItem=true;

	private String zipPassword="";
	private String zipFileName="";
	private long zipFileCompressedSize=0;
	public static final int ZIP_COMPRESSION_METHOD_STORE=ZipFileListItem.COMPRESSION_METHOD_STORE;
    public static final int ZIP_COMPRESSION_METHOD_DEFLATE= ZipFileListItem.COMPRESSION_METHOD_DEFLATE;
    private int zipFileCompressedMethod=-1;

    public static final int ENCRYPTION_METHOD_NONE= ZipFileListItem.ENCRPTION_METHOD_NONE;
    public static final int ENCRYPTION_METHOD_AES= ZipFileListItem.ENCRPTION_METHOD_AES;
    public static final int ENCRYPTION_METHOD_ZIP= ZipFileListItem.ENCRPTION_METHOD_ZIP;
    private int zipFileEncryptionMethod=0;

	private String sortKeyName="";
	private String sortKeySize="";
	private String sortKeyTime="";

	public void dump(String id) {
		String did=(id+"            ").substring(0,12);
		Log.v("TreeFileListItem",did+"FileName="+fileName+", filePath="+filePath);
		Log.v("TreeFileListItem",did+"isDir="+isDirectory+", Length="+fileLength+
				", lastModdate="+fileLastModDate+" "+fileLastModTime+", isChecked="+isChecked+
				", canRead="+canRead+",canWrite="+canWrite+", isHidden="+isHidden);
		Log.v("TreeFileListItem",did+"childListExpanded="+childListExpanded+
				", listLevel=="+listLevel+", hideListItem="+hideListItem+
				", subDirLoaded="+subDirLoaded+", subDirItemCount="+subDirItemCount+
				", triState="+triState+", enableItem="+enableItem);
	};
	
	@Override
	final public TreeFilelistItem clone() {
		return deSerialize(serialize());
    };
	
	final static private TreeFilelistItem deSerialize(byte[] buf) {
		TreeFilelistItem o=null;
		try {
			ByteArrayInputStream bis = new ByteArrayInputStream(buf);
			ObjectInput in = new ObjectInputStream(bis);
			o=(TreeFilelistItem) in.readObject(); 
		    in.close(); 
		} catch (StreamCorruptedException e) {
			Log.v("TreeFilelistItem", "deSerialize error", e);
		} catch (IOException e) {
			Log.v("TreeFilelistItem", "deSerialize error", e);
		} catch (ClassNotFoundException e) {
			Log.v("TreeFilelistItem", "deSerialize error", e);
		}
		return o;
	};
	
	final private byte[] serialize() {
		ByteArrayOutputStream bos = new ByteArrayOutputStream(10000);
		byte[] buf=null; 
	    try { 
	    	ObjectOutput out = new ObjectOutputStream(bos);
		    out.writeObject(this);
		    out.flush(); 
		    buf= bos.toByteArray(); 
	    } catch(IOException e) {
	    	Log.v("TreeFilelistItem", "deSerialize error", e);
		}
		return buf;
	};
	
	public TreeFilelistItem(String fn){
		fileName = fn;
	};

	@SuppressLint("DefaultLocale")
	public TreeFilelistItem(String fn,
                            boolean d, long fl, long lm, boolean ic,
                            boolean cr, boolean cw, boolean hd, String fp, int lvl)
	{
		fileName = fn;
		fileLength = fl;
		isDirectory=d;
		lastModdate=lm;
		isChecked =ic;
		canRead=cr;
		canWrite=cw;
		isHidden=hd;
		filePath=fp;
		listLevel=lvl;
        fileSize= MiscUtil.convertFileSize(fileLength);
        String[] dt= StringUtil.convDateTimeTo_YearMonthDayHourMinSec(lastModdate).split(" ");
        fileLastModDate=dt[0];
        fileLastModTime=dt[1];
        if (isDirectory) {
			createSortKey("D");
		} else {
			fileExt=fileName.lastIndexOf(".")>0?fileName.substring(fileName.lastIndexOf(".")+1).toLowerCase():"";
			if (!fileExt.equals("") || fileExt.length()<=5) {
				String mt= MimeTypeMap.getSingleton().getMimeTypeFromExtension(fileExt);
				mimeType=(mt==null)?"":mt;
			}
			createSortKey("F");
		}
	};

	public void setLength(long fl) {
	    fileLength=fl;
        fileSize= MiscUtil.convertFileSize(fileLength);
        if (isDirectory) {
            createSortKey("D");
        } else {
            createSortKey("F");
        }
    }

	private void createSortKey(String type) {
		StringBuilder sb=new StringBuilder(256);
		sortKeyName=sb.append(type).append(fileName).toString();
		sb.setLength(0);
		String tfl=sb.append("000000000").append(String.valueOf(fileLength)).toString();
		sb.setLength(0);
		sortKeySize=sb.append(tfl.substring(tfl.length()-10)).append(type).append(fileName).toString();
		sb.setLength(0);
		tfl=sb.append("0000000000000000000").append(String.valueOf(lastModdate)).toString();
		sb.setLength(0);
		sortKeyTime=sb.append(tfl.substring(tfl.length()-20)).append(type).append(fileName).toString();
	};
	
	public String getName(){return fileName;}
	public long getLength(){return fileLength;}

    public String getFileSize() {return fileSize;}
    public String getFileLastModDate() {return fileLastModDate;}
    public String getFileLastModTime() {return fileLastModTime;}

    public boolean isZipFileItem(){return zipFileItem;}
    public void setZipFileItem(boolean zip_file_item){zipFileItem=zip_file_item;}

    public boolean isDirectory(){return isDirectory;}
	public long getLastModified(){return lastModdate;}
//	public void setLastModified(long p){lastModdate=p;}
	public boolean isChecked(){return isChecked;}
	public void setChecked(boolean p){
	    if (isSelectableItem(this)) {
            isChecked=p;
            if (p) triState=false;
        }
	};

	static final String[] UNSELECTABLE_DIRECTORY=new String[]{"/Android"};
    static public boolean isSelectableItem(TreeFilelistItem tfli) {
        if (Build.VERSION.SDK_INT>=30 && !tfli.isZipFileItem()) {
            if (tfli.canRead) {
                String fp=tfli.getPath()+"/"+tfli.getName();
                String abs_dir="";
                if (fp.startsWith("/storage/emulated/0")) {
                    abs_dir=fp.replace("/storage/emulated/0", "");
                } else {
                    String[] dir_parts=fp.split("/");
                    if (dir_parts.length>=4) {
                        String new_fp="/"+dir_parts[1]+"/"+dir_parts[2];
                        abs_dir=fp.replace(new_fp, "");
                    }
                }
                for(String item:UNSELECTABLE_DIRECTORY) {
                    if (abs_dir.equalsIgnoreCase(item)) return false;
                }
            } else {
                return false;
            }
        }
        return true;
    }

    public boolean canRead(){return canRead;}
	public boolean canWrite(){return canWrite;}
	public boolean isHidden(){return isHidden;}
	public String getPath(){return filePath;}
	public void setChildListExpanded(boolean p){childListExpanded=p;}
	public boolean isChildListExpanded(){return childListExpanded;}
	public void setListLevel(int p){listLevel=p;}
	public int getListLevel(){return listLevel;}
	public boolean isHideListItem(){return hideListItem;}
	public void setHideListItem(boolean p){hideListItem=p;}
	public void setSubDirItemCount(int p){subDirItemCount=p;}
	public int getSubDirItemCount(){return subDirItemCount;}
	public boolean isSubDirLoaded() {return subDirLoaded;}
	public void setSubDirLoaded(boolean p) {subDirLoaded=p;}
	public void setTriState(boolean p) {triState=p;}
	public boolean isTriState() {return triState;}
	public void setEnableItem(boolean p) {enableItem=p;}
	public boolean isEnableItem() {return enableItem;}
	
	public boolean isZipEncrypted() {return isEncrypted;}
	public void setZipEncrypted(boolean p) {isEncrypted=p;}

	public String getZipPassword() {return zipPassword;}
	public void setZipPassword(String p) {zipPassword=p;}

	public String getZipFileName() {return zipFileName;}
	public void setZipFileName(String p) {zipFileName=p;}

	public long getZipFileCompressedSize() {return zipFileCompressedSize;}
	public void setZipFileCompressedSize(long p) {zipFileCompressedSize=p;}
	
	public int getZipFileCompressionMethod() {return zipFileCompressedMethod;}
	public void setZipFileCompressionMethod(int p) {zipFileCompressedMethod=p;}

    public int getZipFileEncryptionMethod() {return zipFileEncryptionMethod;}
    public void setZipFileEncryptionMethod(int p) {zipFileEncryptionMethod=p;}

    public String getMimeType() {return mimeType;}
//	public void setMimeType(String p) {mimeType=p;}

	public String getFileExtention() {return fileExt;}
//	public void setFileExtention(String p) {fileExt=p;}

	public String getSortKeyName() {return sortKeyName;}
//	public void setSortKeyName(String p) {sortKeyName=p;}

	public String getSortKeySize() {return sortKeySize;}
//	public void setSortKeySize(String p) {sortKeySize=p;}

	public String getSortKeyTime() {return sortKeyTime;}
//	public void setSortKeyTime(String p) {sortKeyTime=p;}

	@Override
	public int compareTo(TreeFilelistItem o) {
		if(this.fileName != null) {
			return sortKeyName.compareToIgnoreCase(o.getSortKeyName());
		} else 
			throw new IllegalArgumentException();
	}
}
