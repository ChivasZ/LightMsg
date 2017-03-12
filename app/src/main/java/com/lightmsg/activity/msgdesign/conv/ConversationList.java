package com.lightmsg.activity.msgdesign.conv;

import java.util.ArrayList;

import android.app.ListActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.SpannableString;
import android.util.Log;
import android.view.GestureDetector.OnGestureListener;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.lightmsg.BadgeView;
import com.lightmsg.R;
import com.lightmsg.LightMsg;
import com.lightmsg.activity.msgdesign.chat.ChatThread;
import com.lightmsg.activity.ConvEntry;
import com.lightmsg.service.CoreService;
import com.lightmsg.util.EmojiUtil;

public class ConversationList extends ListActivity implements OnItemClickListener, OnItemLongClickListener, OnGestureListener  {
    private static final String TAG = ConversationList.class.getSimpleName();
    private LightMsg app = null;
    private CoreService xs = null;

    private ListView lvConversation = null;
    private ArrayList<ConvEntry> mConvList = null;
    private ConvListAdapter adapter = null;
    //private List<BadgeEntry> bvList = null;

    private MessageReceiver messageReceiver = null;
    private IntentFilter messageFilter = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.v(TAG, "onCreate()...");
        super.onCreate(savedInstanceState);
        app = (LightMsg)getApplication();
        xs = app.xs;

        //requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.conversation);
        lvConversation = getListView();
        lvConversation.setOnItemClickListener(this);
        lvConversation.setOnItemLongClickListener(this);

        mConvList = xs.getConvArrayList(this);
        adapter = new ConvListAdapter(this, R.layout.conversation_list_item, mConvList);

        lvConversation.setAdapter(adapter);
        registerReceiver();
    }

    @Override
    protected void onResume() {
        Log.v(TAG, "onResume()...");
        super.onResume();
    }

    @Override
    public void onPause() {
        Log.v(TAG, "onPause()... ");
        super.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.aspark, menu);
        //return true;
        return false;
    }

    @Override
    protected void onStart() {
        Log.v(TAG, "onStart()...");
        super.onStart();

    }

    @Override
    protected void onStop() {
        Log.v(TAG, "onStop()...");
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        Log.v(TAG, "onDestroy()...");
        super.onDestroy();
        unregisterReceiver();
    }

    private class MessageReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.v(TAG, "onReceive()... "+intent.getAction());
            if (CoreService.ACTION_RECEIVE_NEW_MSG.equals(intent.getAction())) {
                Log.v(TAG, "onReceive(), >>ACTION_RECEIVE_NEW_MSG");
                //Fetch data when fresh...
                mConvList = xs.getConvArrayList(ConversationList.this);
                adapter.notifyDataSetChanged();
            } else if (CoreService.ACTION_MSG_STATE_CHANGE.equals(intent.getAction())) {
                Log.v(TAG, "onReceive(), >>ACTION_MSG_STATE_CHANGE");
                //Fetch data when fresh...
                mConvList = xs.getConvArrayList(ConversationList.this);
                adapter.notifyDataSetChanged();
            }
        }
    }

    private void registerReceiver() {
        Log.v(TAG, "registerReceiver()...");
        messageFilter = new IntentFilter();
        messageFilter.addAction(CoreService.ACTION_RECEIVE_NEW_MSG);
        messageFilter.addAction(CoreService.ACTION_MSG_STATE_CHANGE);
        messageReceiver = new MessageReceiver();
        registerReceiver(messageReceiver, messageFilter);
    }

    private void unregisterReceiver() {
        Log.v(TAG, "unregisterReceiver()...");
        if (messageReceiver != null) {
            unregisterReceiver(messageReceiver);
            messageReceiver = null;
        }
    }

    class ConvListAdapter extends ArrayAdapter<ConvEntry> {

        private LayoutInflater mInflater = null;
        private int mResId = 0;
        public ConvListAdapter(Context context, int resource, ArrayList<ConvEntry> list) {
            super(context, resource, list);
            mResId = resource;
            mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }
        
        @Override
        public int getCount() {
            Log.v(TAG, "ConvListAdapter.getCount()...mConvList.size()="+mConvList.size());
            return mConvList.size();
        }
 
        @Override
        public ConvEntry getItem(int pos) {
            Log.v(TAG, "ConvListAdapter.getItem()... pos="+pos);
            return mConvList.get(pos);
        }
 
        @Override
        public long getItemId(int pos) {
            Log.v(TAG, "ConvListAdapter.getItemId()... pos="+pos);
            return pos;
        }
        
        @Override
        public int getItemViewType(int pos) {
            Log.v(TAG, "####ConvListAdapter.getItemViewType()... pos="+pos);
            return 0;	
        }
        
        @Override
        public int getViewTypeCount() {
            Log.v(TAG, "####ConvListAdapter.getViewTypeCount()...");
            return 1;
        }

        @Override
        public View getView(int pos, View convertView, ViewGroup parent) {
            Log.v(TAG, "ConvListAdapter.getView()... pos="+pos);
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

            ImageView portrait = (ImageView)v.findViewById(R.id.conv_list_portrait);
            portrait.setImageResource(R.drawable.portrait_thumbnail_img_03);
            livh.ivPortrait = portrait;
            
            //Set unread count on each conversation item.
            int unreadCount = mConvList.get(pos).getUnreadCount();
            BadgeView bv;
            if (livh.bv == null) {
                Context context = ConversationList.this.getApplicationContext();
                bv = new BadgeView(context, (View)portrait);
                bv.setBadgeMargin(0);
                
                livh.bv = bv;
            } else {
                livh = (ListItemViewHolder)v.getTag();
                bv = livh.bv;
            }
            if (bv != null) {
                if (unreadCount > 0) {
                    Log.v(TAG, "ConvListAdapter.getView()... unreadCount="+unreadCount+", SHOWN!");
                    String cntText;
                    if (unreadCount > 99) {
                        cntText = "99+";
                    } else {
                        cntText = String.valueOf(unreadCount);
                    }
                    bv.setText(cntText);
                    bv.show();
                } else {
                    Log.v(TAG, "ConvListAdapter.getView()... unreadCount="+unreadCount+", HIDDEN!");
                    bv.setText("");
                    bv.hide();
                }
            } else {
                Log.e(TAG, "ConvListAdapter.getView()... NO BADGEVIEW OBJECT!!!");
            }

            String name = mConvList.get(pos).getName();
            TextView tvName = (TextView)v.findViewById(R.id.conv_list_name);
            if (name != null && !name.isEmpty())
                tvName.setText(name);
            else
                tvName.setText(R.string.nul);
            Log.v(TAG, "ConvListAdapter.getView(), name="+name);
            livh.tvName = tvName;

            String snippet = mConvList.get(pos).getSnippet();
            SpannableString spannableString = EmojiUtil.getInstance().getExpressionString(ConversationList.this, snippet);
            TextView tvSnippet = (TextView)v.findViewById(R.id.conv_list_snippet);
            if (snippet != null && !snippet.isEmpty())
                tvSnippet.setText(spannableString);
            else
                tvSnippet.setText(R.string.nul);
            Log.v(TAG, "ConvListAdapter.getView(), snippet="+snippet);
            livh.tvSnippet = tvSnippet;

            String date = mConvList.get(pos).getDate();
            TextView tvDate = (TextView)v.findViewById(R.id.conv_list_date);
            if (date != null && !date.isEmpty())
                tvDate.setText(date);
            else
                tvDate.setText(R.string.nul);
            Log.v(TAG, "ConvListAdapter.getView(), date="+date);
            livh.tvDate = tvDate;

            //Check messages state in the thread.
            Boolean error = mConvList.get(pos).getError();
            ImageView icon = (ImageView)v.findViewById(R.id.conv_list_icon);
            if (error) {
                icon.setImageResource(R.drawable.msg_state_failed);
            } else {
                icon.setVisibility(View.GONE);
            }
            livh.ivIcon = icon;
            
            livh.user = mConvList.get(pos).getUser();
            livh.name = mConvList.get(pos).getName();
            
            return v;
        }
    }
    
    private static class ListItemViewHolder {
        public TextView tvName;
        public TextView tvSnippet;
        public TextView tvDate;
        public ImageView ivPortrait;
        public ImageView ivIcon;
        public BadgeView bv;
        public String user;
        public String name;
        
        public ListItemViewHolder() {
            tvName = null;
            tvSnippet = null;
            tvDate = null;
            ivPortrait = null;
            ivIcon = null;
            bv = null;
            user = "";
        }
    }

    @Override
    public boolean onDown(MotionEvent arg0) {
        Log.v(TAG, "onDown()...ME="+arg0);
        return false;
    }

    @Override
    public boolean onFling(MotionEvent arg0, MotionEvent arg1, float arg2,
            float arg3) {
        Log.v(TAG, "onFling()...ME="+arg0);
        return false;
    }

    @Override
    public void onLongPress(MotionEvent arg0) {
        Log.v(TAG, "onLongPress()...ME="+arg0);
    }

    @Override
    public boolean onScroll(MotionEvent arg0, MotionEvent arg1, float arg2,
            float arg3) {
        Log.v(TAG, "onScroll()...ME1="+arg0+", ME2="+arg1);
        return false;
    }

    @Override
    public void onShowPress(MotionEvent arg0) {
        Log.v(TAG, "onShowPress()...ME="+arg0);

    }

    @Override
    public boolean onSingleTapUp(MotionEvent arg0) {
        Log.v(TAG, "onSingleTapUp()...ME="+arg0);
        return false;
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int arg2,
            long arg3) {
        Log.v(TAG, "onItemLongClick()...AV="+arg0+", v="+arg1+", "+arg2+", "+arg3);
        return false;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View v, int pos, long id) {
        Log.v(TAG, "onItemClick()...AV="+parent+", v="+v+", "+pos+", "+id);
        ListItemViewHolder livh;
        livh = (ListItemViewHolder)v.getTag();
        startChatWith(livh.user, livh.name);
    }
    
    private void startChatWith(String user, String name) {
        Log.v(TAG, "startChatWith()... user="+user);
        
        Intent intent = new Intent();
        intent.setClass(ConversationList.this, ChatThread.class);
        intent.putExtra("user", user);
        intent.putExtra("name", name);
        
        startActivity(intent);
    }

    /*public static class BadgeEntry {
        private int mPos;
        private BadgeView mBv;

        public int getPos() {
            return mPos;
        }
        
        public void setPos(int pos) {
            mPos = pos;
        }
        
        public BadgeView getBv() {
            return mBv;
        }
        
        public void setBv(BadgeView bv) {
            mBv = bv;
        }
    }*/
}
