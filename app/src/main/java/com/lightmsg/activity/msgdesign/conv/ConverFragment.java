package com.lightmsg.activity.msgdesign.conv;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ListFragment;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.text.SpannableString;
import android.util.Log;
import android.util.TypedValue;
import android.view.GestureDetector.OnGestureListener;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.lightmsg.BadgeView;
import com.lightmsg.LightMsg;
import com.lightmsg.R;
import com.lightmsg.activity.ChatMsgEntry;
import com.lightmsg.activity.ConvEntry;
import com.lightmsg.activity.msgdesign.LightMsgActivity;
import com.lightmsg.activity.msgdesign.chat.ChatFragment;
import com.lightmsg.activity.msgdesign.chat.ChatThread;
import com.lightmsg.service.CoreService;
import com.lightmsg.util.EmojiUtil;
import com.lightmsg.util.Locator;

import java.util.ArrayList;

public class ConverFragment extends ListFragment implements OnItemClickListener, OnItemLongClickListener, OnGestureListener {
    private static final String TAG = "LightMsg/" + ConverFragment.class.getSimpleName();
    private LightMsg app = null;
    protected CoreService xs = null;
    private LightMsgActivity activity = null;

    private ListView lvConversation = null;
    private ArrayList<ConvEntry> mConvList = null;
    private ConvListAdapter adapter = null;
    //private List<BadgeEntry> bvList = null;

    private MessageReceiver messageReceiver = null;
    private IntentFilter messageFilter = null;

    private TextView empty;

    protected boolean isGroup = false;
    protected String mCurrentGroup = "";
    protected String mCurrentName = "";
    protected Locator locator;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.v(TAG, "onCreate()...");
        super.onCreate(savedInstanceState);
        activity = (LightMsgActivity) getActivity();
        app = (LightMsg) activity.getApplication();
        xs = app.xs;

        try {
            locator = Locator.getInstance(xs);
            mCurrentGroup = locator.getMessageGroupId();
            mCurrentName = locator.getMessageGroupName();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        Log.v(TAG, "onCreateView()...");
        View view = inflater.inflate(R.layout.conversation, container, false);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        Log.v(TAG, "onActivityCreated()...");
        super.onActivityCreated(savedInstanceState);

        lvConversation = getListView();
        lvConversation.setOnItemClickListener(this);
        lvConversation.setOnItemLongClickListener(this);

        if (!isGroup) {
            Log.v(TAG, "onActivityCreated(), xs=" + xs);
            mConvList = xs.getConvArrayList(this.getActivity().getApplicationContext());
        } else {
            mConvList = xs.getGroupConvArrayList(this.getActivity().getApplicationContext());
        }
        adapter = new ConvListAdapter(this.getActivity(), R.layout.conversation_list_item, mConvList);

        lvConversation.setAdapter(adapter);
        registerReceiver();

        empty = (TextView) getView().findViewById(R.id.empty);
        if (mConvList.size() == 0) {
            empty.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onResume() {
        Log.v(TAG, "onResume()...");
        super.onResume();
    }

    @Override
    public void onPause() {
        Log.v(TAG, "onPause()... ");
        super.onPause();
    }

    @Override
    public void onStart() {
        Log.v(TAG, "onStart()...");
        super.onStart();

    }

    @Override
    public void onStop() {
        Log.v(TAG, "onStop()...");
        super.onStop();
    }

    @Override
    public void onDestroy() {
        Log.v(TAG, "onDestroy()...");
        super.onDestroy();
        unregisterReceiver();
    }

    private class MessageReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.v(TAG, "onReceive()... " + intent.getAction());
            if (CoreService.ACTION_RECEIVE_NEW_MSG.equals(intent.getAction())) {
                Log.v(TAG, "onReceive(), >>ACTION_RECEIVE_NEW_MSG");
                //Fetch data when fresh...
                if (!isGroup) {
                    mConvList = xs.getConvArrayList(ConverFragment.this.getActivity().getApplicationContext());
                } else {
                    mConvList = xs.getGroupConvArrayList(ConverFragment.this.getActivity().getApplicationContext());
                }
                adapter.notifyDataSetChanged();
            } else if (CoreService.ACTION_MSG_STATE_CHANGE.equals(intent.getAction())) {
                Log.v(TAG, "onReceive(), >>ACTION_MSG_STATE_CHANGE");
                //Fetch data when fresh...
                if (!isGroup) {
                    mConvList = xs.getConvArrayList(ConverFragment.this.getActivity().getApplicationContext());
                } else {
                    mConvList = xs.getGroupConvArrayList(ConverFragment.this.getActivity().getApplicationContext());
                }
                adapter.notifyDataSetChanged();
            }

            if (mConvList.size() == 0) {
                empty.setVisibility(View.VISIBLE);
            } else {
                empty.setVisibility(View.GONE);
            }
        }
    }

    private void registerReceiver() {
        Log.v(TAG, "registerReceiver()...");
        messageFilter = new IntentFilter();
        messageFilter.addAction(CoreService.ACTION_RECEIVE_NEW_MSG);
        messageFilter.addAction(CoreService.ACTION_MSG_STATE_CHANGE);
        messageReceiver = new MessageReceiver();
        getActivity().registerReceiver(messageReceiver, messageFilter);
    }

    private void unregisterReceiver() {
        Log.v(TAG, "unregisterReceiver()...");
        if (messageReceiver != null) {
            getActivity().unregisterReceiver(messageReceiver);
            messageReceiver = null;
        }
    }

    class ConvListAdapter extends ArrayAdapter<ConvEntry> {
        private Context mContext;
        private LayoutInflater mInflater = null;
        private int mResId = 0;
        private Resources mRes;
        private int mBadgeSize;
        private float mBadgeTextSize;

        public ConvListAdapter(Context context, int resource, ArrayList<ConvEntry> list) {
            super(context, resource, list);
            mContext = context;
            mResId = resource;
            mRes = context.getResources();
            mBadgeSize = mRes.getDimensionPixelSize(R.dimen.unread_cnt_ball_size);
            mBadgeTextSize = mRes.getDimension(R.dimen.unread_cnt_text_size);
            mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getCount() {
            //Log.v(TAG, "ConvListAdapter.getCount()...mConvList.size()="+mConvList.size());
            return mConvList.size();
        }

        @Override
        public ConvEntry getItem(int pos) {
            //Log.v(TAG, "ConvListAdapter.getItem()... pos="+pos);
            return mConvList.get(pos);
        }

        @Override
        public long getItemId(int pos) {
            //Log.v(TAG, "ConvListAdapter.getItemId()... pos="+pos);
            return pos;
        }

        @Override
        public int getItemViewType(int pos) {
            //Log.v(TAG, "####ConvListAdapter.getItemViewType()... pos="+pos);
            return 0;
        }

        @Override
        public int getViewTypeCount() {
            //Log.v(TAG, "####ConvListAdapter.getViewTypeCount()...");
            return 1;
        }

        @Override
        public View getView(int pos, View convertView, ViewGroup parent) {
            //Log.v(TAG, "ConvListAdapter.getView()... pos="+pos);
            RelativeLayout v;

            ListItemViewHolder livh;
            if (convertView == null) {
                v = (RelativeLayout) mInflater.inflate(mResId, parent, false);
                livh = new ListItemViewHolder();
                v.setTag(livh);
            } else {
                v = (RelativeLayout) convertView;
                livh = (ListItemViewHolder) v.getTag();
            }

            ImageView portrait = (ImageView) v.findViewById(R.id.conv_list_portrait);
            portrait.setImageResource(R.drawable.portrait_thumbnail_img_03);
            livh.ivPortrait = portrait;

            //Set unread count on each conversation item.
            int unreadCount = mConvList.get(pos).getUnreadCount();
            BadgeView bv;
            if (livh.bv == null) {
                Context context = ConverFragment.this.getActivity().getApplicationContext();
                bv = new BadgeView(context, (View) portrait);
                bv.setBadgeMargin(0);
                bv.setHeight(mBadgeSize);
                bv.setWidth(mBadgeSize);
                bv.setTextSize(TypedValue.COMPLEX_UNIT_PX, mBadgeTextSize);
                bv.setGravity(Gravity.CENTER);
                bv.setMaxLines(1);

                livh.bv = bv;
            } else {
                livh = (ListItemViewHolder) v.getTag();
                bv = livh.bv;
            }
            if (bv != null) {
                //unreadCount = 100;
                if (unreadCount > 0) {
                    //Log.v(TAG, "ConvListAdapter.getView()... unreadCount="+unreadCount+", SHOWN!");
                    String cntText;
                    if (unreadCount > 99) {
                        cntText = mRes.getString(R.string.unread_cnt_more_than_99);
                    } else {
                        cntText = String.valueOf(unreadCount);
                    }
                    bv.setText(cntText);
                    bv.show();
                } else {
                    //Log.v(TAG, "ConvListAdapter.getView()... unreadCount="+unreadCount+", HIDDEN!");
                    bv.setText("");
                    bv.hide();
                }
            } else {
                Log.e(TAG, "ConvListAdapter.getView()... NO BADGEVIEW OBJECT!!!");
            }

            String name = mConvList.get(pos).getName();
            TextView tvName = (TextView) v.findViewById(R.id.conv_list_name);
            if (name != null && !name.isEmpty())
                tvName.setText(name);
            else
                tvName.setText(R.string.nul);
            //Log.v(TAG, "ConvListAdapter.getView(), name="+name);
            livh.tvName = tvName;

            String snippet = mConvList.get(pos).getSnippet();
            EmojiUtil.getInstance().setImageSizeSmall();
            SpannableString spannableString = EmojiUtil.getInstance().getExpressionString(ConverFragment.this.getActivity().getApplicationContext(), snippet);
            TextView tvSnippet = (TextView) v.findViewById(R.id.conv_list_snippet);
            if (snippet != null && !snippet.isEmpty())
                tvSnippet.setText(spannableString);
            else
                tvSnippet.setText(R.string.nul);
            //Log.v(TAG, "ConvListAdapter.getView(), snippet="+snippet);
            livh.tvSnippet = tvSnippet;

            String date = mConvList.get(pos).getDate();
            TextView tvDate = (TextView) v.findViewById(R.id.conv_list_date);
            if (date != null && !date.isEmpty())
                tvDate.setText(date);
            else
                tvDate.setText(R.string.nul);
            //Log.v(TAG, "ConvListAdapter.getView(), date="+date);
            livh.tvDate = tvDate;

            //Check messages state in the thread.
            Boolean error = mConvList.get(pos).getError();
            ImageView icon = (ImageView) v.findViewById(R.id.conv_list_icon);
            if (error) {
                icon.setImageResource(R.drawable.msg_state_failed);
            } else {
                icon.setVisibility(View.GONE);
            }
            livh.ivIcon = icon;

            livh.user = mConvList.get(pos).getUser();
            livh.name = mConvList.get(pos).getName();

            //v.setOnLongClickListener(new OnConvLongClick(pos));
            return v;
        }

        private class OnConvLongClick implements View.OnLongClickListener {
            private int mListPos;
            public OnConvLongClick(int pos) {
                mListPos = pos;
            }

            @Override
            public boolean onLongClick(View v) {
                Log.v(TAG, "ConvListAdapter.[item].cmvh.tvMsg..onLongClick(), "+v);
//                bListItemLongPressed = true;
//                final CharSequence msg = ((TextView)v).getText();

                AlertDialog ops = new AlertDialog.Builder(mContext)//, R.style.AppTheme_OptionDialog)
                        .setItems(R.array.conv_msg_options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Log.v(TAG, "AlertDialog..onClick(), " + which);
                                switch (which) {
                                    case 0: //Copy
                                        break;
                                    case 1: //Delete
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
        Log.v(TAG, "onDown()...ME=" + arg0);
        return false;
    }

    @Override
    public boolean onFling(MotionEvent arg0, MotionEvent arg1, float arg2,
                           float arg3) {
        Log.v(TAG, "onFling()...ME=" + arg0);
        return false;
    }

    @Override
    public void onLongPress(MotionEvent arg0) {
        Log.v(TAG, "onLongPress()...ME=" + arg0);
    }

    @Override
    public boolean onScroll(MotionEvent arg0, MotionEvent arg1, float arg2,
                            float arg3) {
        Log.v(TAG, "onScroll()...ME1=" + arg0 + ", ME2=" + arg1);
        return false;
    }

    @Override
    public void onShowPress(MotionEvent arg0) {
        Log.v(TAG, "onShowPress()...ME=" + arg0);

    }

    @Override
    public boolean onSingleTapUp(MotionEvent arg0) {
        Log.v(TAG, "onSingleTapUp()...ME=" + arg0);
        return false;
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int arg2,
                                   long arg3) {
        Log.v(TAG, "onItemLongClick()...AV=" + arg0 + ", v=" + arg1 + ", " + arg2 + ", " + arg3);
        return false;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View v, int pos, long id) {
        Log.v(TAG, "onItemClick()...AV=" + parent + ", v=" + v + ", " + pos + ", " + id);
        ListItemViewHolder livh;
        livh = (ListItemViewHolder) v.getTag();

        if (isGroup) {
            //startChatInRoom(livh.user, livh.name);
            startChatInRoomByFragment(livh.user, livh.name);
        } else {
            startChatWith(livh.user, livh.name);
        }
    }

    protected void startChatInRoom(String room, String name) {
        Log.v(TAG, "startChatInRoom()... room=" + room);

        Intent intent = new Intent();
        intent.setClass(ConverFragment.this.getActivity(), ChatThread.class);
        intent.putExtra("group", room);
        intent.putExtra("group_name", name);

        startActivity(intent);
    }

    protected void startChatInRoomByFragment(String room, String name) {
        Log.v(TAG, "startChatInRoomInFragment()... room=" + room);

        FragmentManager fragManager = getFragmentManager();
        FragmentTransaction fragTransaction = fragManager.beginTransaction();

        Bundle bundle = new Bundle();
        bundle.putString("group", room);
        bundle.putString("group_name", name);
        Fragment frag = new ChatFragment();
        frag.setArguments(bundle);

        fragTransaction.replace(R.id.tab3, frag, "chat_fragment");
        fragTransaction.commit();
    }

    protected void startChatWith(String user, String name) {
        Log.v(TAG, "startChatWith()... user=" + user);

        Intent intent = new Intent();
        intent.setClass(ConverFragment.this.getActivity(), ChatThread.class);
        intent.putExtra("user", user);
        intent.putExtra("name", name);

        startActivity(intent);
        
        /*FragmentManager fragManager = getFragmentManager();
        FragmentTransaction fragTransaction = fragManager.beginTransaction();
        fragTransaction.replace(R.id.tab3, new ChatFragment());
        fragTransaction.commit();*/
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
