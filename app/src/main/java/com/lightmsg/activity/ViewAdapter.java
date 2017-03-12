package com.lightmsg.activity;

import java.util.HashMap;
import java.util.List;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;


public abstract class ViewAdapter extends BaseAdapter {

    protected Context mContext;

    protected List<HashMap<String, Object>> mDataList;
    protected abstract void initDataList(boolean online);

    public ViewAdapter(Context context) {
        mContext = context;
    }
    
    public void setDataList(List<HashMap<String, Object>> dataList) {
        mDataList = dataList;
    }

    @Override
    public int getCount() {
        return mDataList.size();
    }

    @Override
    public Object getItem(int position) {
        return mDataList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return null;
    }
}
