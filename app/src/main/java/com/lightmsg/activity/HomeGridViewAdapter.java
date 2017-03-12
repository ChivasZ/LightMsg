package com.lightmsg.activity;

import java.util.ArrayList;
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

public class HomeGridViewAdapter extends ViewAdapter {

    public HomeGridViewAdapter(Context context) {
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

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view;
        ViewHolder viewHolder;
        if (convertView == null) {
            viewHolder = new ViewHolder();
            view = LayoutInflater.from(mContext).inflate(R.layout.home_gridview_item, null);
            viewHolder.imageView = (ImageView) view.findViewById(R.id.imageView1);
            viewHolder.textView = (TextView) view.findViewById(R.id.textView1);
            view.setTag(viewHolder);
        } else {
            view = convertView;
            viewHolder = (ViewHolder) view.getTag();
        }
        viewHolder.imageView.setImageResource((Integer) mDataList.get(position).get("icon"));
        viewHolder.textView.setText((String) mDataList.get(position).get("name"));
        return view;
    }

    class ViewHolder {
        ImageView imageView;
        TextView textView;
    }

    @Override
    protected void initDataList(boolean online) {
        // TODO Auto-generated method stub
        if (!online) {
            mDataList = new ArrayList<HashMap<String, Object>>();
            int[] imageId = { R.drawable.ic_fish_menu_2, R.drawable.ic_fish_menu_3, R.drawable.ic_fish_menu_6,
                    R.drawable.ic_fish_menu_5 };
            String[] text = { "楼宇对讲", "物业服务", "社区商城", "交流娱乐" };
            HashMap<String, Object> map;
            for (int i = 0; i < imageId.length; i++) {
                map = new HashMap<String, Object>();
                map.put("name", text[i]);
                map.put("icon", imageId[i]);
                mDataList.add(map);
            }
        }
    }
}
