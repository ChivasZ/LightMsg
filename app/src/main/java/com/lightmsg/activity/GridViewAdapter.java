package com.lightmsg.activity;

import java.util.HashMap;
import java.util.List;

import com.lightmsg.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public abstract class GridViewAdapter extends ViewAdapter {

    public GridViewAdapter(Context context) {
        super(context);
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

    /*
     * Move to Sub-class
     * 
     * @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view;
        ViewHolder viewHolder;
        if (convertView == null) {
            viewHolder = new ViewHolder();
            view = LayoutInflater.from(mContext).inflate(R.layout.tenement_gridview_item, null);
            viewHolder.imageView = (ImageView) view.findViewById(R.id.tenement_imageView);
            viewHolder.textView = (TextView) view.findViewById(R.id.tenement_textView);
            view.setTag(viewHolder);
        } else {
            view = convertView;
            viewHolder = (ViewHolder) view.getTag();
        }
        viewHolder.imageView.setImageResource((Integer) mDataList.get(position).get("icon"));
        viewHolder.textView.setText((String) mDataList.get(position).get("name"));
        return view;
    }*/

    public class ViewHolder {
        public ImageView imageView;
        public TextView textView;
    }
}
