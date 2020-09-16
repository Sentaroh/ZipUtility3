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
//        log.info("enabled="+isEnabled(position)+", click="+view.isClickable()+", ve="+view.isEnabled());
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
