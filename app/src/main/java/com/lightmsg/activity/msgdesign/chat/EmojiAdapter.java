package com.lightmsg.activity.msgdesign.chat;

import java.util.List;

import com.lightmsg.util.EmojiUtil.ChatEmoji;
import com.lightmsg.R;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

public class EmojiAdapter extends BaseAdapter {
    private static final String TAG = "SmartCommunity/" + EmojiAdapter.class.getSimpleName();
    
    private List<ChatEmoji> data;

    private LayoutInflater inflater;

    private int size=0;

    public EmojiAdapter(Context context, List<ChatEmoji> list) {
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
        ChatEmoji emoji=data.get(position);
        ViewHolder viewHolder=null;
        if(convertView == null) {
            viewHolder=new ViewHolder();
            convertView=inflater.inflate(R.layout.item_emoji, null);
            viewHolder.iv_emoji=(ImageView)convertView.findViewById(R.id.item_iv_emoji);
            convertView.setTag(viewHolder);
        } else {
            viewHolder=(ViewHolder)convertView.getTag();
        }
        if(emoji.getId() == R.drawable.face_del_icon) {
            convertView.setBackgroundDrawable(null);
            viewHolder.iv_emoji.setImageResource(emoji.getId());
        } else if(TextUtils.isEmpty(emoji.getCharacter())) {
            convertView.setBackgroundDrawable(null);
            viewHolder.iv_emoji.setImageDrawable(null);
        } else {
            viewHolder.iv_emoji.setTag(emoji);
            viewHolder.iv_emoji.setImageResource(emoji.getId());
        }

        return convertView;
    }

    class ViewHolder {

        public ImageView iv_emoji;
    }
}