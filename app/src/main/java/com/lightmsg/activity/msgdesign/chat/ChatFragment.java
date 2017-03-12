package com.lightmsg.activity.msgdesign.chat;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.Editable;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.lightmsg.LightMsg;
import com.lightmsg.R;
import com.lightmsg.activity.ChatMsgEntry;
import com.lightmsg.activity.msgdesign.conv.ConverFragment;
import com.lightmsg.service.CoreService;
import com.lightmsg.service.CoreService.Account;
import com.lightmsg.util.EmojiUtil;
import com.lightmsg.util.Locator;

import java.util.List;

public class ChatFragment extends Fragment {
    private static final String TAG = "LightMsg/" + ChatFragment.class.getSimpleName();
    private LightMsg app = null;
    private CoreService xs = null;
    
    private ListView lvChatList = null;
    private ChatListAdapter mChatListAdapter = null;
    private String mGroup = "";
    private String mUser = "";
    private boolean isGroup = false;
    private String mName = "";
    
    private ImageButton ibEmoji;
    private Button btnSend;
    private ImageButton ibMore;
    private EditText etSend;
    
    Locator locator;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        //getActivity().getActionBar().hide();
        super.onCreate(savedInstanceState);
        
        app = (LightMsg)getActivity().getApplication();
        xs = app.xs;
        
        Bundle bundle = this.getArguments();
        if (bundle != null) {
            mGroup = bundle.getString("group");
            mName = bundle.getString("group_name");
        }
        
        if (mGroup == null || mGroup.isEmpty()) {
            try {
                locator = Locator.getInstance(xs);
                mGroup = locator.getMessageGroupId();
                mName = locator.getMessageGroupName();
                
                if (mGroup != null && !mGroup.isEmpty()) {
                    isGroup = true;
                    //if (mGroup == null) {
                    //	backToList();
                    //}
                }
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            
            if (mGroup == null || mGroup.isEmpty()) {
                mUser = getLastUser();
                isGroup = false;
            }
        } else {
            isGroup = true;
        }
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        Log.v(TAG, "onCreateView()...");
        View view = inflater.inflate(R.layout.chat_layout, container, false);
        Log.v(TAG, "onCreateView(), view=" + view);
        return view;
    }
    
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        Log.v(TAG, "onActivityCreated()...");
        super.onActivityCreated(savedInstanceState);
        
        TextView tvName = (TextView) getView().findViewById(R.id.name);
        tvName.setText(mName);
        ImageButton btnBack = (ImageButton) getView().findViewById(R.id.back);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                backToList();
            }
        });

        ibEmoji = (ImageButton) getView().findViewById(R.id.btn_emo);
        etSend = (EditText) getView().findViewById(R.id.et_sendmessage);
        btnSend = (Button) getView().findViewById(R.id.btn_send);
        ibMore = (ImageButton) getView().findViewById(R.id.btn_more);
        etSend.addTextChangedListener(new TextWatcher() {
            
            @Override
            public void afterTextChanged(Editable s) {
                Log.v(TAG, "<Text to be sent>.afterTextChanged(), "+s);
                if (s.toString().isEmpty()) {
                    btnSend.setVisibility(View.INVISIBLE);
                    ibMore.setVisibility(View.VISIBLE);
                } else {
                    btnSend.setVisibility(View.VISIBLE);
                    ibMore.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int before, int count) {
                Log.v(TAG, "<Text to be sent>.beforeTextChanged(), "+s+", start="+start+", before="+before+", count="+count);
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Log.v(TAG, "<Text to be sent>.onTextChanged(), "+s+", start="+start+", before="+before+", count="+count);
            }
        });
        
        btnSend.setOnClickListener(new View.OnClickListener() {
            
            @Override
            public void onClick(View v) {
                String msg = etSend.getText().toString();
                etSend.setText("");

                if (isGroup) {
                    xs.chatInRoom(mGroup, msg);
                } else {
                    xs.sendMsg(mUser, msg);
                }
            }
        });
        
        initView();
        registerReceiver();
        
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN |
                WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
    }
    
    private String getLastUser() {
        String lu = "";

        if (xs != null) {
            Account account = xs.getAccount();
            if (account == null) {
                account = xs.new Account();
            }
            lu = account.account;
        }
        //lu = app.getSharedPreferences("login", Context.MODE_PRIVATE).getString("username", "");
        Log.v(TAG, "getLastUser(), \"username\"="+lu);

        return lu;
    }
    
    private void backToList() {
        Log.v(TAG, "backToList()...");
        FragmentManager fragManager = getFragmentManager();
        FragmentTransaction fragTransaction = fragManager.beginTransaction();
        Fragment frag = new ConverFragment();
        fragTransaction.replace(R.id.tab3, frag, "message_fragment");
        fragTransaction.commit();
        
        //getActivity().getActionBar().show();
    }

    private void initView() {
        lvChatList = (ListView) getView().findViewById(R.id.listview);
        lvChatList.setDividerHeight(0);
        
        List<ChatMsgEntry> chatList;
        if (isGroup) {
            chatList = xs.getChatArrayList(mGroup);
        } else {
            chatList = xs.getChatArrayList(mUser);
        }
        
        mChatListAdapter = new ChatListAdapter(getActivity(), R.layout.chat_list_item_left, R.layout.chat_list_item_right, chatList);
        lvChatList.setAdapter(mChatListAdapter);
        
        //lvChatList.smoothScrollToPosition(chatList.size()-1);
        //lvChatList.setSelection(lvChatList.getBottom());
        lvChatList.setSelection(lvChatList.getCount()-1);
    }
    
    class ChatListAdapter extends BaseAdapter {

        private LayoutInflater mInflater = null;
        private int mResFromId = 0;
        private int mResToId = 0;
        private List<ChatMsgEntry> mChatList;
        private Context mContext;  
        
        public ChatListAdapter(Context context, int resFrom, int resTo, List<ChatMsgEntry> list) {
            //super(context, resource, list);
            mContext = context;
            mResFromId = resFrom;
            mResToId = resTo;
            mChatList = list;
            mInflater = LayoutInflater.from(mContext);
            //mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }
        
        @Override
        public int getCount() {
            Log.v(TAG, "ChatListAdapter.getCount()...mChatList.size()="+mChatList.size());
            return mChatList.size();
        }

        @Override
        public ChatMsgEntry getItem(int pos) {
            Log.v(TAG, "ChatListAdapter.getItem()... pos="+pos);
            return mChatList.get(pos);
        }

        @Override
        public long getItemId(int pos) {
            Log.v(TAG, "ChatListAdapter.getItemId()... pos="+pos);
            return pos;
        }
        
        @Override
        public int getItemViewType(int pos) {
            Log.v(TAG, "####ChatListAdapter.getItemViewType()... pos="+pos);
            return 0;	
        }
        
        @Override
        public int getViewTypeCount() {
            Log.v(TAG, "####ChatListAdapter.getViewTypeCount()...");
            return 2;
        }

        @Override
        public View getView(int pos, View convertView, ViewGroup parent) {
            Log.v(TAG, "ChatListAdapter.getView()... pos="+pos);
            ChatMsgEntry chatMsg = mChatList.get(pos);
            
            LinearLayout v;
            ChatMsgItemViewHolder cmvh;
            
            boolean bFrom = chatMsg.isFrom();
            if (convertView == null) {
                if (bFrom) {
                    v = (LinearLayout)mInflater.inflate(mResFromId, parent, false);
                } else {
                    v = (LinearLayout)mInflater.inflate(mResToId, parent, false);
                }
                cmvh = new ChatMsgItemViewHolder();
                cmvh.ivPortrait = (ImageView) v.findViewById(R.id.portrait);
                cmvh.tvName = (TextView) v.findViewById(R.id.name);
                cmvh.tvDate = (TextView) v.findViewById(R.id.date);
                cmvh.tvMsg = (TextView) v.findViewById(R.id.msg);
                cmvh.bFrom = bFrom;
                v.setTag(cmvh);
            } else {
                v = (LinearLayout)convertView;
                cmvh = (ChatMsgItemViewHolder)convertView.getTag();
                
                if (cmvh.bFrom != bFrom) {
                    if (bFrom) {
                        v = (LinearLayout)mInflater.inflate(mResFromId, parent, false);
                    } else {
                        v = (LinearLayout)mInflater.inflate(mResToId, parent, false);
                    }
                    cmvh = new ChatMsgItemViewHolder();
                    cmvh.ivPortrait = (ImageView) v.findViewById(R.id.portrait);
                    cmvh.tvName = (TextView) v.findViewById(R.id.name);
                    cmvh.tvDate = (TextView) v.findViewById(R.id.date);
                    cmvh.tvMsg = (TextView) v.findViewById(R.id.msg);
                    cmvh.bFrom = bFrom;
                    v.setTag(cmvh);
                }
            }
            
            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.v(TAG, "###v..onClick()...");
                }
            });
            
            cmvh.tvName.setText(chatMsg.getName());
            if (isGroup)
                cmvh.tvName.setVisibility(View.VISIBLE); //DON'T DISPLAY NAME ANYMORE.
            cmvh.tvName.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.v(TAG, "###cmvh.tvName..onClick()...");
                }
            });
            cmvh.tvDate.setText(chatMsg.getDate());
            cmvh.tvDate.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.v(TAG, "###cmvh.tvDate..onClick()...");
                }
            });
            EmojiUtil.getInstance().setImageSizeSmall();
            SpannableString spannableString = EmojiUtil.getInstance().getExpressionString(ChatFragment.this.getActivity(), chatMsg.getMsg());
            cmvh.tvMsg.setText(spannableString);
            cmvh.tvMsg.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.v(TAG, "###cmvh.tvMsg..onClick()...");
                }
            });
            cmvh.ivPortrait.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.v(TAG, "###cmvh.ivPortrait..onClick()...");
                }
            });
            
            return v;
        }
        
        public void update() {
            if (isGroup) {
                mChatList = xs.getChatArrayList(mGroup);
            } else {
                mChatList = xs.getChatArrayList(mUser);
            }
            
            notifyDataSetChanged();
            lvChatList.setSelection(mChatList.size()-1);
        }
    }
    
    public static class ChatMsgItemViewHolder {
        public TextView tvName;
        public TextView tvDate;
        public TextView tvMsg;
        public ImageView ivPortrait;
        public boolean bFrom;
    }
    
    /*public static class ChatMsgEntry {
        private String mName;
        private String mDate;
        private String mMsg;
        private boolean mbFrom;
        
        public ChatMsgEntry(String name, String msg, String date, boolean bFrom) {
            mName = name;
            mDate = date;
            mMsg = msg;
            mbFrom = bFrom;
        }
        
        public ChatMsgEntry() {
            mName = "";
            mDate = "";
            mMsg = "";
            mbFrom = false;
        }
        
        public void setName(String name) {
            mName = name;
        }
        
        public void setDate(String date) {
            mDate = date;
        }
        
        public void setMsg(String msg) {
            mMsg = msg;
        }
        
        public void setFrom(boolean bFrom) {
            mbFrom = bFrom;
        }
        
        public String getName() {
            return mName;
        }
        
        public String getDate() {
            return mDate;
        }
        
        public String getMsg() {
            return mMsg;
        }
        
        public boolean isFrom() {
            return mbFrom;
        }
    }*/
    
    private MessageReceiver messageReceiver = null;
    private IntentFilter messageFilter = null;
    private class MessageReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.v(TAG, "onReceive()... "+intent.getAction());
            if (CoreService.ACTION_RECEIVE_NEW_MSG.equals(intent.getAction())) {
                Log.v(TAG, "onReceive(), >>ACTION_RECEIVE_NEW_MSG");
                //Fetch data when fresh...
                mChatListAdapter.update();
            } else if (CoreService.ACTION_MSG_STATE_CHANGE.equals(intent.getAction())) {
                Log.v(TAG, "onReceive(), >>ACTION_MSG_STATE_CHANGE");
                //Fetch data when fresh...
                mChatListAdapter.update();
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

    @Override
    public void onResume() {
        Log.v(TAG, "onResume()...");
        super.onResume();
        
        //getActivity().getActionBar().hide();
        
        if (isGroup) {
            xs.setGroupRead(xs, mGroup);
        } else {
            xs.setUserRead(xs, mUser);
        }
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
}
