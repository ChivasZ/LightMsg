package com.lightmsg.activity.msg.chat;

import java.util.List;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.lightmsg.R;
import com.lightmsg.activity.msg.chat.EmojiRelativeLayout.ChatMoreOp;

public class MoreOpeAdapter extends BaseAdapter {
    private static final String TAG = MoreOpeAdapter.class.getSimpleName();

    private List<ChatMoreOp> data;

    private LayoutInflater inflater;

    private int size=0;

    public MoreOpeAdapter(Context context, List<ChatMoreOp> list) {
        this.inflater=LayoutInflater.from(context);
        this.data=list;
        this.size=list.size();
    }

    @Override
    public int getCount() {
        return this.size;
    }

    @Override
    public Object getItem(int position) {
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ChatMoreOp more = data.get(position);
        ViewHolder viewHolder=null;
        if(convertView == null) {
            viewHolder=new ViewHolder();
            convertView=inflater.inflate(R.layout.item_more_ope, null);
            viewHolder.iv_op=(ImageView)convertView.findViewById(R.id.item_iv_op);
            convertView.setTag(viewHolder);
        } else {
            viewHolder=(ViewHolder)convertView.getTag();
        }
        if(TextUtils.isEmpty(more.getAction())) {
            convertView.setBackgroundDrawable(null);
            viewHolder.iv_op.setImageDrawable(null);
        } else {
            viewHolder.iv_op.setTag(more);
            viewHolder.iv_op.setImageResource(more.getId());
        }

        return convertView;
    }

    class ViewHolder {

        public ImageView iv_op;
    }
}