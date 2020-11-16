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
package com.sentaroh.android.ZipUtility3;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.sentaroh.android.Utilities3.Widget.CustomSpinnerAdapter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ZipFileSelectorAdapter extends CustomSpinnerAdapter {
    final private static Logger log= LoggerFactory.getLogger(ZipFileSelectorAdapter.class);
    private Context mContext=null;

    public ZipFileSelectorAdapter(Context c, int textViewResourceId) {
        super(c, textViewResourceId);
        mContext=c;
    }

    @Override
    public void add(String item) {
        super.add(item);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        TextView view;
        if (convertView == null) {
            view=(TextView)super.getView(position,convertView,parent);
        } else {
            view = (TextView)convertView;
        }
        String fp=getItem(position);
        String fn="";
        if (fp.lastIndexOf("/")>=0) fn=fp.substring(fp.lastIndexOf("/")+1);
        else fn=fp;
        view.setText(fn);
        view.setCompoundDrawablePadding(10);
        view.setCompoundDrawablesWithIntrinsicBounds(
                mContext.getResources().getDrawable(android.R.drawable.arrow_down_float),
                null, null, null);
        setSelectedPosition(position);
        return view;
    }



}
