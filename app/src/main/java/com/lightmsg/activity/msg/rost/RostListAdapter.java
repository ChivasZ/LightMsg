package com.lightmsg.activity.msg.rost;

import java.util.ArrayList;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.lightmsg.R;
import com.lightmsg.activity.RostEntry;


class RostListAdapter extends ArrayAdapter<RostEntry> {
    private static final String TAG = RostListAdapter.class.getSimpleName();

    private ArrayList<RostEntry> mRostList = null;
    
    private LayoutInflater mInflater = null;
    private int mResId = 0;
    public RostListAdapter(Context context, int resource,
            ArrayList<RostEntry> list) {
        super(context, resource, list);
        mResId = resource;
        mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mRostList = list;
    }

    @Override
    public View getView(int pos, View convertView, ViewGroup parent) {
        Log.v(TAG, "RostListAdapter.getView(), pos="+pos+", convertView="+convertView);
        Log.v(TAG, "RostListAdapter.getView(), mRostList.size()="+mRostList.size());
        RelativeLayout v;

        ListItemViewHolder livh;
        if (convertView == null) {
            v = (RelativeLayout)mInflater.inflate(mResId, parent, false);
            livh = new ListItemViewHolder();
            v.setTag(livh);
        } else {
            v = (RelativeLayout)convertView;
            livh = (ListItemViewHolder)v.getTag();
        }

        ImageView portrait = (ImageView)v.findViewById(R.id.roster_portrait);
        portrait.setImageResource(R.drawable.portrait_thumbnail_img_03);
        livh.ivPortrait = portrait;

        String name = mRostList.get(pos).getName();
        TextView tvName = (TextView)v.findViewById(R.id.roster_name);
        if (name != null && !name.isEmpty())
            tvName.setText(name);
        else
            tvName.setText(R.string.nul);
        Log.v(TAG, "RostListAdapter.getView(), name="+name);
        livh.tvName = tvName;

        /*String info = mRosterList.get(pos).getUser();
        TextView tvInfo = (TextView)v.findViewById(R.id.roster_info);
        //tvInfo.setTextColor(Color.rgb(50, 50, 255));//(R.color.beige);
        if (info != null && !info.isEmpty())
            tvInfo.setText(info);
        else
            tvInfo.setText(R.string.nul);
        Log.v(TAG, "RosterListAdapter.getView(), info="+info);*/


        return v;
    }

    private static class ListItemViewHolder {
        public TextView tvName;
        public ImageView ivPortrait;
        
        public ListItemViewHolder() {
            tvName = null;
            ivPortrait = null;
        }
    }
}