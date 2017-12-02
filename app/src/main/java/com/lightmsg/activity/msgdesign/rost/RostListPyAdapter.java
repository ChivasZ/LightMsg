package com.lightmsg.activity.msgdesign.rost;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SectionIndexer;
import android.widget.TextView;

import com.lightmsg.R;
import com.lightmsg.activity.RostEntry;
import com.lightmsg.activity.msgdesign.rost.PinnedHeaderListView.PinnedHeaderAdapter;

public class RostListPyAdapter extends BaseAdapter implements SectionIndexer,
PinnedHeaderAdapter, OnScrollListener {
    private static final String TAG = RostListPyAdapter.class.getSimpleName();

    private Context mContext;
    private ArrayList<RostEntry> mRostList = null;
    private LayoutInflater mInflater = null;
    private int mResId = 0;

    private int mLocationPosition = -1;
    // 首字母集
    private List<String> mHeaders;
    private List<Integer> mHeaderPositions;
    
    private static class ListItemViewHolder {
        public TextView tvName;
        public ImageView ivPortrait;
        
        public ListItemViewHolder() {
            tvName = null;
            ivPortrait = null;
        }
    }

    public RostListPyAdapter(Context context, int resource,
            ArrayList<RostEntry> list, List<String> headers,
            List<Integer> headerPostions) {
        // TODO Auto-generated constructor stub
        mContext = context;
        mHeaders = headers;
        mHeaderPositions = headerPostions;
        mResId = resource;
        mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mRostList = list;
    }

    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return mRostList.size();
    }

    @Override
    public Object getItem(int position) {
        // TODO Auto-generated method stub
        return mRostList.get(position);
    }

    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return position;
    }
    
    @Override
    public View getView(int pos, View convertView, ViewGroup parent) {
        // TODO Auto-generated method stub
        int section = getSectionForPosition(pos);
        if (convertView == null) {
            convertView = mInflater.inflate(mResId, parent, false);
        }
        
        LinearLayout mHeaderParent = (LinearLayout) convertView.findViewById(R.id.item_header_parent);
        TextView mHeaderText = (TextView) convertView.findViewById(R.id.item_header_text);
        LinearLayout mItemParent = (LinearLayout) convertView.findViewById(R.id.item);
        if (getPositionForSection(section) == pos) {
            mHeaderParent.setVisibility(View.VISIBLE);
            mHeaderText.setText(mHeaders.get(section).toUpperCase());
            mItemParent.setVisibility(View.VISIBLE);
        } else {
            mHeaderParent.setVisibility(View.GONE);
            mItemParent.setVisibility(View.VISIBLE);
        }
        
        ImageView portrait = (ImageView)convertView.findViewById(R.id.roster_portrait);
        portrait.setImageResource(R.drawable.portrait_thumbnail_img_03);

        String name = mRostList.get(pos).getName();
        TextView tvName = (TextView)convertView.findViewById(R.id.roster_name);
        if (name != null && !name.isEmpty())
            tvName.setText(name);
        else
            tvName.setText(R.string.nul);

        //convertView.setOnLongClickListener(new OnRostLongClick(pos));
        Log.v(TAG, "RostListPyAdapter.getView(), name="+name);
        return convertView;
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem,
            int visibleItemCount, int totalItemCount) {
        // TODO Auto-generated method stub
        if (view instanceof PinnedHeaderListView) {
            ((PinnedHeaderListView) view).configureHeaderView(firstVisibleItem);
        }
    }

    @Override
    public int getPinnedHeaderState(int position) {
        int realPosition = position;
        if (realPosition < 0
                || (mLocationPosition != -1 && mLocationPosition == realPosition)) {
            return PINNED_HEADER_GONE;
        }
        mLocationPosition = -1;
        int section = getSectionForPosition(realPosition);
        int nextSectionPosition = getPositionForSection(section + 1);
        if (nextSectionPosition != -1
                && realPosition == nextSectionPosition - 1) {
            return PINNED_HEADER_PUSHED_UP;
        }
        return PINNED_HEADER_VISIBLE;
    }

    @Override
    public void configurePinnedHeader(View header, int position, int alpha) {
        // TODO Auto-generated method stub
        int realPosition = position;
        int section = getSectionForPosition(realPosition);
        if (section < 0) {
            return;
        }
        
        String title = (String) getSections()[section];
        title = title.toUpperCase();
        ((TextView) header.findViewById(R.id.list_header_text)).setText(title);
    }

    @Override
    public Object[] getSections() {
        // TODO Auto-generated method stub
        return mHeaders.toArray();
    }

    @Override
    public int getPositionForSection(int section) {
        if (section < 0 || section >= mHeaders.size()) {
            return -1;
        }
        return mHeaderPositions.get(section);
    }

    @Override
    public int getSectionForPosition(int position) {
        // TODO Auto-generated method stub
        if (position < 0 || position >= getCount()) {
            return -1;
        }
        int index = Arrays.binarySearch(mHeaderPositions.toArray(), position);
        return index >= 0 ? index : -index - 2;
    }


    private class OnRostLongClick implements View.OnLongClickListener {
        private int mListPos;
        public OnRostLongClick(int pos) {
            mListPos = pos;
        }

        @Override
        public boolean onLongClick(View v) {
            Log.v(TAG, "OnRostLongClick.[item].cmvh.tvMsg..onLongClick(), "+v);
//                bListItemLongPressed = true;
//                final CharSequence msg = ((TextView)v).getText();

            AlertDialog ops = new AlertDialog.Builder(mContext)//, R.style.AppTheme_OptionDialog)
                    .setItems(R.array.rost_msg_options, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Log.v(TAG, "AlertDialog..onClick(), " + which);
                            switch (which) {
                                case 0: //Delete
                                    break;
                            }
                        }
                    }).show();
            ops.getListView().setDivider(mContext.getResources().getDrawable(R.drawable.line));
            //ops.getListView().setDividerHeight(2); //Set in drawable/line.xml
            ops.getListView().setPadding(0, 0, 0, 0);
            //View parent = (View)ops.getListView().getParent().getParent().getParent();
            //ViewGroup.LayoutParams params = parent.getLayoutParams();
            //parent.setPadding(20, 0, 50, 0);

            return true;
        }
    }
}
