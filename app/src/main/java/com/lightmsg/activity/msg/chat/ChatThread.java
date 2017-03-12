package com.lightmsg.activity.msg.chat;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.os.Bundle;
import android.text.Editable;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
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
import com.lightmsg.service.CoreService;
import com.lightmsg.util.EmojiUtil;
import com.lightmsg.util.MediaFile;

import java.io.File;
import java.util.List;

public class ChatThread extends Activity {
    private static final String TAG = ChatThread.class.getSimpleName();
    private LightMsg app;
    private CoreService xs;
    
    private ListView lvChatList;
    private ChatListAdapter mChatListAdapter;

    private String mGroup = "";
    private String mUser = "";
    private boolean isGroup;
    private String mName = "";
    
    private ImageButton ibEmoji;
    private ImageButton ibMore;
    private Button btnSend;
    private EditText etSend;
    
    private EmojiRelativeLayout cbrl;

    private ImageView hint;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        app = (LightMsg)getApplication();
        xs = app.xs;
        
        /*new Thread(new Runnable() {
            @Override
            public void run() {
                EmojiUtil.getInstance().getFileText(getApplication());
            }
        }).start();*/
        
        Intent intent = getIntent();
        mGroup = intent.getStringExtra("group");
        if (mGroup != null && !mGroup.isEmpty()) {
            isGroup = true;
            if (mGroup == null) {
                ChatThread.this.finish();
            }
            mName = intent.getStringExtra("group_name");
        } else {
            mUser = xs.guaranteeUid(intent.getStringExtra("user"));
            isGroup = false;
            if (mUser == null) {
                ChatThread.this.finish();
            }
            mName = intent.getStringExtra("name");
        }

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        //getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.chat_layout);

        cbrl = (EmojiRelativeLayout) findViewById(R.id.chat_btm_rl);
        cbrl.setChatThread(this);
        measureKeyboardLayout(cbrl);
        
        TextView tvName = (TextView) findViewById(R.id.name);
        tvName.setText(mName);
        ImageButton btnBack = (ImageButton) findViewById(R.id.back);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ChatThread.this.finish();
            }
        });
        hint = (ImageView) findViewById(R.id.hint);

        ibEmoji = (ImageButton) findViewById(R.id.btn_emo);
        ibMore = (ImageButton) findViewById(R.id.btn_more);
        etSend = (EditText) findViewById(R.id.et_sendmessage);
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
        btnSend = (Button) findViewById(R.id.btn_send);
        btnSend.setOnClickListener(new View.OnClickListener() {
            
            @Override
            public void onClick(View v) {
                String msg = etSend.getText().toString();
                etSend.setText("");
                //xs.sendMsg("qinghua@hwqm913d200058v", msg);
                if (isGroup) {
                    xs.chatInRoom(mGroup, msg);
                } else {
                    xs.sendMsg(mUser, msg);
                }
            }
        });
        
        initView();
        registerReceiver();

        /*Log.v(TAG, "PATH APK:" + MediaFile.getCurrentApkPath(app));
        Log.v(TAG, "PATH Data:"+MediaFile.getCurrentDataPath(app));
        Log.v(TAG, "PATH DB:"+MediaFile.getCurrentDBPath(app, "sc.db"));
        Log.v(TAG, "PATH SDCard/Cache:"+MediaFile.getCurrentSDCardCachePath(app));*/

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN |
                WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
    }

    private View findContentView(final View v) {
        View root = v.getRootView();
        return root.findViewById(Window.ID_ANDROID_CONTENT);
    }

    private void measureKeyboardLayout(final View v) {
        final View content = findContentView(v);
        final OnLayoutChangeNotify olcn = (OnLayoutChangeNotify)v;
        content.getViewTreeObserver().addOnGlobalLayoutListener( new ViewTreeObserver.OnGlobalLayoutListener() {
            private boolean bCollapse;
            @Override
            public void onGlobalLayout() {
                Rect rect = new Rect();
                content.getWindowVisibleDisplayFrame(rect);
                int height = content.getRootView().getHeight() - rect.bottom;
                if (height == 0) { //Collapse
                    if (!bCollapse) {
                        Log.d(TAG, "onGlobalLayout(), keyboard collapsed");
                        olcn.collapse();
                        bCollapse = true;
                    }
                } else { //Expand
                    if (height != keyboardHeight) {
                        Log.d(TAG, "onGlobalLayout(), keyboard height changed:" + height);
                        keyboardHeight = height;

                        SharedPreferences sp = getSharedPreferences("keyboard_height", Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sp.edit();
                        editor.putInt("keyboardHeight", height);
                        editor.commit();
                    } else {
                        Log.d(TAG, "onGlobalLayout(), keyboard height not change:"+height);
                    }

                    if (bCollapse) {
                        Log.d(TAG, "onGlobalLayout(), keyboard expanded");
                        olcn.expand(height);
                        bCollapse = false;
                    }
                }
            }
        });
    }

    private int keyboardHeight;
    public int getBottomHeight() {
        Log.d(TAG, "getBottomHeight(), keyboardHeight="+keyboardHeight);
        return keyboardHeight;
    }
    
    private void initView() {
        lvChatList = (ListView)findViewById(R.id.listview);
        lvChatList.setDividerHeight(0);
        
        List<ChatMsgEntry> chatList;
        if (isGroup) {
            chatList = xs.getChatArrayList(mGroup);
        } else {
            chatList = xs.getChatArrayList(mUser);
        }
        mChatListAdapter = new ChatListAdapter(this, R.layout.chat_list_item_left, R.layout.chat_list_item_right, chatList);
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
                cmvh.ivMsg = (ImageView) v.findViewById(R.id.msg_img);
                cmvh.bFrom = bFrom;
                v.setTag(cmvh);
            } else {
                v = (LinearLayout)convertView;
                cmvh = (ChatMsgItemViewHolder)convertView.getTag();
                
                /* Reset by Mo or Mt. 2013/12/06
                 * Problem: ListView displays the same in & out arrange.
                 */
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
                    cmvh.ivMsg = (ImageView) v.findViewById(R.id.msg_img);
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
            cmvh.tvName.setVisibility(View.INVISIBLE); //DON'T DISPLAY NAME ANYMORE.
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
            SpannableString spannableString = EmojiUtil.getInstance().getExpressionString(ChatThread.this, chatMsg.getMsg());
            if (!chatMsg.isFileType()) {
                cmvh.tvMsg.setText(spannableString);
                cmvh.tvMsg.setVisibility(View.VISIBLE);
                cmvh.ivMsg.setVisibility(View.GONE);
            } else {
                Log.v(TAG, "ChatListAdapter.getView(), file=" + chatMsg.getFile());
                BitmapFactory.Options op = new BitmapFactory.Options();
                op.inJustDecodeBounds = true;
                Bitmap b = BitmapFactory.decodeFile(chatMsg.getFile(), op);

                if (b != null) {
                    Log.d(TAG, "outWidth="+op.outWidth+", outHeight="+op.outHeight);
                    if (op.outWidth*op.outHeight > 480*640) {
                        op.inSampleSize = 2;
                    }
                    op.inJustDecodeBounds = false;

                    b = BitmapFactory.decodeFile(chatMsg.getFile(), op);
                    Log.d(TAG, "b="+b);
                    cmvh.ivMsg.setImageBitmap(b);
                    cmvh.ivMsg.setVisibility(View.VISIBLE);
                    cmvh.tvMsg.setVisibility(View.GONE);
                }
            }
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
        public ImageView ivMsg;
        public ImageView ivPortrait;
        public boolean bFrom;
    }
    
    private SelfMessageReceiver messageReceiver = null;
    private IntentFilter messageFilter = null;
    private class SelfMessageReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.v(TAG, "onReceive()... "+intent.getAction());
            if (CoreService.ACTION_RECEIVE_NEW_MSG.equals(intent.getAction())) {
                Log.v(TAG, "onReceive(), >>ACTION_RECEIVE_NEW_MSG");
                //Fetch data when fresh...
                mChatListAdapter.update();

                if (!isGroup) {
                    if ("chat".equals(intent.getStringExtra("MSG_TYPE"))) {
                        String user = xs.guaranteeUid(intent.getStringExtra("MSG_USER"));
                        if(!mUser.equals(user)) {
                            //Show hint view.
                            hint.setVisibility(View.VISIBLE);
                        } else {
                            xs.setUserRead(ChatThread.this, mUser);
                        }
                    } else {
                        //Show hint view.
                        hint.setVisibility(View.VISIBLE);
                    }
                } else {
                    if ("groupchat".equals(intent.getStringExtra("MSG_TYPE"))) {
                        String group = xs.guaranteeUid(intent.getStringExtra("MSG_GROUP"));
                        if(!mGroup.equals(group)) {
                            //Show hint view.
                            hint.setVisibility(View.VISIBLE);
                        } else {
                            xs.setUserRead(ChatThread.this, mGroup);
                        }
                    } else {
                        //Show hint view.
                        hint.setVisibility(View.VISIBLE);
                    }
                }
            } else if (CoreService.ACTION_MSG_STATE_CHANGE.equals(intent.getAction())) {
                Log.v(TAG, "onReceive(), >>ACTION_MSG_STATE_CHANGE");
                //Fetch data when fresh...
                mChatListAdapter.update();
            } else if (CoreService.ACTION_MSG_SEND_FILE.equals(intent.getAction())) {
                Log.v(TAG, "onReceive(), >>ACTION_MSG_SEND_FILE");
                mChatListAdapter.update();
            } else if (CoreService.ACTION_MSG_RECEIVE_FILE.equals(intent.getAction())) {
                Log.v(TAG, "onReceive(), >>ACTION_MSG_RECEIVE_FILE");
                mChatListAdapter.update();
            }
        }
    }

    private void registerReceiver() {
        Log.v(TAG, "registerReceiver()...");
        messageFilter = new IntentFilter();
        messageFilter.addAction(CoreService.ACTION_RECEIVE_NEW_MSG);
        messageFilter.addAction(CoreService.ACTION_MSG_STATE_CHANGE);
        messageReceiver = new SelfMessageReceiver();
        registerReceiver(messageReceiver, messageFilter);
    }

    private void unregisterReceiver() {
        Log.v(TAG, "unregisterReceiver()...");
        if (messageReceiver != null) {
            unregisterReceiver(messageReceiver);
            messageReceiver = null;
        }
    }

    @Override
    protected void onResume() {
        Log.v(TAG, "onResume()...");
        super.onResume();
        if (isGroup) {
            xs.setGroupRead(ChatThread.this, mGroup);
        } else {
            xs.setUserRead(ChatThread.this, mUser);
        }

        if (!isGroup) {
            xs.setCurrentThreadUser(mUser);
        } else {

        }
        //xs.unregisterMessageReceiver();
    }

    @Override
    public void onPause() {
        Log.v(TAG, "onPause()... ");
        super.onPause();

        if (!isGroup) {
            xs.setCurrentThreadUser(null);
        } else {

        }
        //xs.registerMessageReceiver();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.SmartCommunity, menu);
        return true;
    }

    @Override
    protected void onStart() {
        Log.v(TAG, "onStart()...");
        super.onStart();

        xs.cancelNotificationMessage();
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
    
    public static final int ACTION_IMAGE = 10;
    public static final int ACTION_FILE = 11;
    public static final int ACTION_LOCATION = 12;
    public static final int ACTION_VOICE = 13;
    public void fetchImage() {
        //Intent i = new Intent(Intent.ACTION_GET_CONTENT);
        Intent i = new Intent(Intent.ACTION_PICK);
        i.setType("image/*");

        i.putExtra("return-data", false);
        //i.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        //i.putExtra("return-data", true);
        //i.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
        //i.putExtra("noFaceDetection", true);

        startActivityForResult(i, ACTION_IMAGE);
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult(), "+requestCode+", "+resultCode+", "+data);
        if (resultCode != RESULT_OK) { // RESULT_CANCELED / ...
            return;
        }

        switch (requestCode) {
        case ACTION_IMAGE:
            //Bitmap bitmap = data.getParcelableExtra("data");
            //BitmapDrawable bd = new BitmapDrawable(bitmap);
            //this.findViewById(R.id.self).setBackgroundDrawable(bd);
            String file = MediaFile.uriToPath(ChatThread.this, data.getData());
            xs.sendFile(mUser, "IMAGE",new File(file));
            break;

        case ACTION_FILE:
            break;

        case ACTION_LOCATION:
            break;
        }
    }


}
