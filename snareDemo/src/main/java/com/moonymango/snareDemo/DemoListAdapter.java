package com.moonymango.snareDemo;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class DemoListAdapter extends BaseAdapter {

    private final Context mCtx;
    
    public DemoListAdapter(Context ctx) {
        mCtx = ctx;
    }
    
    @Override
    public int getCount() {
        return Demo.getCount();
    }

    @Override
    public Object getItem(int arg0) {
        return null;
    }

    @Override
    public long getItemId(int arg0) {
        return arg0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        TextView textView;
        if (convertView != null && convertView instanceof TextView) {
            textView = (TextView) convertView;
        } else {
            View v = LayoutInflater.from(mCtx).inflate(android.R.layout.simple_list_item_1, null);
            textView = (TextView) v.findViewById(android.R.id.text1);
        }
        
        textView.setText(Demo.get(position).getName());
        return textView;
    }

}
