package com.lightmsg.activity.mall;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.lightmsg.activity.ViewAdapter;
import com.lightmsg.R;

public class MallListViewAdaper extends ViewAdapter {

    public MallListViewAdaper(Context context) {
        super(context);
    }

    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return 15;
    }

    @Override
    public Object getItem(int position) {
        // TODO Auto-generated method stub
        return position;
    }

    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // TODO Auto-generated method stub
        View view;
        ViewHolder viewHolder;
        if (convertView == null) {
            view = LayoutInflater.from(mContext).inflate(R.layout.mall_fishery_listview_item_text,
                    null);
            viewHolder = new ViewHolder();
            view.setTag(viewHolder);
        } else {
            view = convertView;
            viewHolder = (ViewHolder) view.getTag();
        }
        return view;

    }

    class ViewHolder {

        TextView textView;

    }

    @Override
    protected void initDataList(boolean online) {
        // TODO Auto-generated method stub
        
    }

}
