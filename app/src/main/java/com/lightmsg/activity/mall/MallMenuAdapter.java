package com.lightmsg.activity.mall;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.lightmsg.R;

public class MallMenuAdapter extends BaseAdapter {

    private Context mContext;

    /**
     * 侧滑菜单的ListView的数据来源mDataList
     */
    private List<HashMap<String, Object>> mDataList;

    public MallMenuAdapter(Context context) {
        mContext = context;
        initDataList();
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
            view = LayoutInflater.from(mContext).inflate(R.layout.mall_menu_frag_item, null);
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

    /**
     * 初始化侧滑菜单的ListView的数据
     */
    private void initDataList() {
        mDataList = new ArrayList<HashMap<String, Object>>();
        int[] imageId = { R.drawable.ic_fish_menu_1, R.drawable.ic_fish_menu_2,
                R.drawable.ic_fish_menu_3, R.drawable.ic_fish_menu_4, R.drawable.ic_fish_menu_5,
                R.drawable.ic_fish_menu_6, R.drawable.ic_fish_menu_6};
        String[] text = { "     首页", "     商店", "     餐饮", "     时鲜", "     渔乐",
                "     健康", "     易物"};
        HashMap<String, Object> map;
        for (int i = 0; i < imageId.length; i++) {
            map = new HashMap<String, Object>();
            map.put("name", text[i]);
            map.put("icon", imageId[i]);
            mDataList.add(map);
        }
    }

}
