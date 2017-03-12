package com.lightmsg.activity.msgdesign.chat;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.lightmsg.LightMsg;
import com.lightmsg.R;
import com.lightmsg.activity.ChatMsgEntry;
import com.lightmsg.service.CoreService;
import com.lightmsg.util.EmojiUtil;
import com.lightmsg.util.MediaFile;

import java.io.File;
import java.util.List;

public class ChatThreadRecyclerView extends AppCompatActivity {
    private static final String TAG = ChatThreadRecyclerView.class.getSimpleName();
    private LightMsg app;
    private CoreService xs;

    private InputMethodManager imm;
    
    private RecyclerView mChatList;
    private ChatRecyclerAdapter mChatListAdapter;
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
        
        Intent intent = getIntent();
        mGroup = intent.getStringExtra("group");
        if (mGroup != null && !mGroup.isEmpty()) {
            isGroup = true;
            if (mGroup == null) {
                ChatThreadRecyclerView.this.finish();
            }
            mName = intent.getStringExtra("group_name");
        } else {
            mUser = xs.guaranteeUid(intent.getStringExtra("user"));
            isGroup = false;
            if (mUser == null) {
                ChatThreadRecyclerView.this.finish();
            }
            mName = intent.getStringExtra("name");
        }

        setContentView(R.layout.chat_design_layout);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(""); //Must set empty(""), not null, to hide title. See @ToolbarWidgetWrapper {mTitleSet = mTitle != null;}
        setSupportActionBar(toolbar);

        //Must set navigation click listener after setSupportActionBar(), 'cause it set its listener to override ours.See @ToolbarWidgetWrapper {mToolbar.setNavigationOnClickListener(...)}
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.v(TAG, "onClick(), " + v);
                View et = cbrl.findViewById(R.id.et_sendmessage);
                imm.hideSoftInputFromWindow(et.getWindowToken(), 0);

                finish();
            }
        });

        imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        cbrl = (EmojiRelativeLayout) findViewById(R.id.chat_btm_rl);
        //cbrl.setChatThread(this);
        measureKeyboardLayout(cbrl);
        
        /*
        ImageButton btnBack = (ImageButton) findViewById(R.id.back);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ChatThread.this.finish();
            }
        });
        */
        
        initView();
        registerReceiver();

        /*Log.v(TAG, "PATH APK:" + MediaFile.getCurrentApkPath(app));
        Log.v(TAG, "PATH Data:"+MediaFile.getCurrentDataPath(app));
        Log.v(TAG, "PATH DB:"+MediaFile.getCurrentDBPath(app, "sc.db"));
        Log.v(TAG, "PATH SDCard/Cache:"+MediaFile.getCurrentSDCardCachePath(app));*/

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN |
                WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
    }
    
    private void initView() {
        hint = (ImageView) findViewById(R.id.hint);
        TextView tvName = (TextView) findViewById(R.id.name);
        tvName.setText(mName);

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

        mChatList = (RecyclerView)findViewById(R.id.recyclerview);
        mChatList.setHasFixedSize(true);
        mChatList.setNestedScrollingEnabled(true);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        mChatList.setLayoutManager(layoutManager);

        List<ChatMsgEntry> chatList;
        if (isGroup) {
            chatList = xs.getChatArrayList(mGroup);
        } else {
            chatList = xs.getChatArrayList(mUser);
        }
        mChatListAdapter = new ChatRecyclerAdapter(this, R.layout.chat_list_item_left, R.layout.chat_list_item_right, chatList);
        mChatList.setAdapter(mChatListAdapter);
        mChatList.scrollToPosition(chatList.size() - 1);
        mChatList.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Log.v(TAG, "onTouch(), v="+v+", "+event);
                //switch (event.getAction()) {
                //    case MotionEvent.ACTION_DOWN:
                        View et = cbrl.findViewById(R.id.et_sendmessage);
                        imm.hideSoftInputFromWindow(et.getWindowToken(), 0);
                //    break;
                //}
                return false;
            }
        });
    }

    class ChatRecyclerAdapter extends RecyclerView.Adapter {

        private static final int TYPE_FROM = 0;
        private static final int TYPE_TO = 1;

        private LayoutInflater mInflater = null;
        private int mResFromId = 0;
        private int mResToId = 0;
        private List<ChatMsgEntry> mChatMsgList;
        private Context mContext;

        public ChatRecyclerAdapter(Context context, int resFrom, int resTo, List<ChatMsgEntry> list) {
            Log.v(TAG, "ChatRecyclerAdapter.ChatRecyclerAdapter()...");

            mContext = context;
            mResFromId = resFrom;
            mResToId = resTo;
            mChatMsgList = list;
            mInflater = LayoutInflater.from(mContext);
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            Log.v(TAG, "ChatRecyclerAdapter.onCreateViewHolder()...");

            LinearLayout l = null;
            boolean from;
            if (viewType == TYPE_FROM) {
                l = (LinearLayout)mInflater.inflate(mResFromId, parent, false);
                from = false;
            } else {
                l = (LinearLayout)mInflater.inflate(mResToId, parent, false);
                from = true;
            }

            //LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

            return new ChatViewHolder(l, from);
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int pos) {
            Log.v(TAG, "ChatRecyclerAdapter.onBindViewHolder()... pos="+pos);
            ChatMsgEntry chatMsg = mChatMsgList.get(pos);

            ChatViewHolder cmvh = (ChatViewHolder) holder;

            cmvh.tvName.setText(chatMsg.getName());
            cmvh.tvName.setVisibility(View.INVISIBLE); //DON'T DISPLAY NAME ANYMORE.
            /*cmvh.tvName.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.v(TAG, "ChatRecyclerAdapter.holder.tvName..onClick()...");
                }
            });*/
            cmvh.tvDate.setText(chatMsg.getDate());
            /*cmvh.tvDate.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.v(TAG, "ChatRecyclerAdapter.holder.tvDate..onClick()...");
                }
            });*/
            EmojiUtil.getInstance().setImageSizeSmall();
            SpannableString spannableString = EmojiUtil.getInstance().getExpressionString(ChatThreadRecyclerView.this, chatMsg.getMsg());
            if (!chatMsg.isFileType()) {
                cmvh.tvMsg.setText(spannableString);
                cmvh.tvMsg.setVisibility(View.VISIBLE);
                cmvh.ivMsg.setVisibility(View.GONE);
            } else {
                Log.v(TAG, "ChatRecyclerAdapter.onBindViewHolder(), file=" + chatMsg.getFile());
                BitmapFactory.Options op = new BitmapFactory.Options();
                op.inJustDecodeBounds = true;
                Bitmap b = BitmapFactory.decodeFile(chatMsg.getFile(), op);

                if (b != null) {
                    Log.v(TAG, "outWidth=" + op.outWidth + ", outHeight=" + op.outHeight);
                    if (op.outWidth*op.outHeight > 480*640) {
                        op.inSampleSize = 2;
                    }
                    op.inJustDecodeBounds = false;

                    b = BitmapFactory.decodeFile(chatMsg.getFile(), op);
                    Log.v(TAG, "b=" + b);
                    cmvh.ivMsg.setImageBitmap(b);
                    cmvh.ivMsg.setVisibility(View.VISIBLE);
                    cmvh.tvMsg.setVisibility(View.GONE);
                }
            }
            cmvh.tvMsg.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.v(TAG, "ChatRecyclerAdapter.holder.tvMsg..onClick()...");
                }
            });
            cmvh.ivPortrait.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.v(TAG, "ChatRecyclerAdapter.holder.ivPortrait..onClick()...");
                }
            });
        }

        @Override
        public int getItemViewType(int pos) {
            boolean from = mChatMsgList.get(pos).isFrom();
            int type = from ? TYPE_FROM : TYPE_TO;

            Log.v(TAG, "ChatRecyclerAdapter.getItemViewType()... pos="+pos+", from="+from);
            return type;
        }

        @Override
        public int getItemCount() {
            return mChatMsgList.size();
        }

        public class ChatViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener
        {

            public TextView tvName;
            public TextView tvDate;
            public TextView tvMsg;
            public ImageView ivMsg;
            public ImageView ivPortrait;
            public boolean bFrom;

            public ChatViewHolder(View v, boolean from) {
                super(v);

                Log.v(TAG, "ChatViewHolder.ChatViewHolder()...");

                ivPortrait = (ImageView) v.findViewById(R.id.portrait);
                tvName = (TextView) v.findViewById(R.id.name);
                tvDate = (TextView) v.findViewById(R.id.date);
                tvMsg = (TextView) v.findViewById(R.id.msg);
                ivMsg = (ImageView) v.findViewById(R.id.msg_img);

                bFrom = from;
            }

            @Override
            public void onClick(View v) {
                Log.v(TAG, "ChatViewHolder.onClick()...");
            }

            @Override
            public boolean onLongClick(View v) {
                Log.v(TAG, "ChatViewHolder.onLongClick()...");
                return false;
            }
        }

        public void update() {
            if (isGroup) {
                mChatMsgList = xs.getChatArrayList(mGroup);
            } else {
                mChatMsgList = xs.getChatArrayList(mUser);
            }
            notifyDataSetChanged();
            mChatList.scrollToPosition(mChatMsgList.size()-1);
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        Log.v(TAG, "onConfigurationChanged(), "+newConfig);
        super.onConfigurationChanged(newConfig);
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
                        Log.v(TAG, "onGlobalLayout(), keyboard collapsed");
                        olcn.collapse();
                        bCollapse = true;
                    }
                } else { //Expand
                    if (height != keyboardHeight) {
                        Log.v(TAG, "onGlobalLayout(), keyboard height changed:" + height);
                        keyboardHeight = height;

                        SharedPreferences sp = getSharedPreferences("keyboard_height", Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sp.edit();
                        editor.putInt("keyboardHeight", height);
                        editor.commit();

                        Log.v(TAG, "onGlobalLayout(), keyboard expanded");
                        olcn.expand(height, true);
                        bCollapse = false;
                    } else {
                        Log.v(TAG, "onGlobalLayout(), keyboard height not change:" + height);

                        if (bCollapse) {
                            Log.v(TAG, "onGlobalLayout(), keyboard expanded");
                            olcn.expand(height, false);
                            bCollapse = false;

                            scrollToBottom();
                        }
                    }
                }
            }
        });
    }

    private int keyboardHeight;
    public int getBottomHeight() {
        Log.v(TAG, "getBottomHeight(), keyboardHeight=" + keyboardHeight);
        return keyboardHeight;
    }

    public void scrollToBottom() {
        //Scroll to the bottom
        mChatList.scrollToPosition(mChatListAdapter.getItemCount()-1);
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
                            xs.setUserRead(ChatThreadRecyclerView.this, mUser);
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
                            xs.setGroupRead(ChatThreadRecyclerView.this, mGroup);
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
            xs.setGroupRead(ChatThreadRecyclerView.this, mGroup);
        } else {
            xs.setUserRead(ChatThreadRecyclerView.this, mUser);
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
        Log.v(TAG, "onActivityResult(), " + requestCode + ", " + resultCode + ", " + data);
        if (resultCode != RESULT_OK) { // RESULT_CANCELED / ...
            return;
        }

        switch (requestCode) {
        case ACTION_IMAGE:
            //Bitmap bitmap = data.getParcelableExtra("data");
            //BitmapDrawable bd = new BitmapDrawable(bitmap);
            //this.findViewById(R.id.self).setBackgroundDrawable(bd);
            String file = MediaFile.uriToPath(ChatThreadRecyclerView.this, data.getData());
            xs.sendFile(mUser, "IMAGE",new File(file));
            break;

        case ACTION_FILE:
            break;

        case ACTION_LOCATION:
            break;
        }
    }


}
