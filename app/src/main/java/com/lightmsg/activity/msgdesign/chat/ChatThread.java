package com.lightmsg.activity.msgdesign.chat;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v4.util.ArrayMap;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Display;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.lightmsg.LightMsg;
import com.lightmsg.R;
import com.lightmsg.activity.ChatMsgEntry;
import com.lightmsg.service.CoreService;
import com.lightmsg.util.EmojiUtil;
import com.lightmsg.util.MediaFile;
import com.lightmsg.util.ThumbnailUtils;

import java.io.File;
import java.util.List;
import java.util.Map;

public class ChatThread extends AppCompatActivity {
    private static final String TAG = ChatThread.class.getSimpleName();
    private LightMsg app;
    private CoreService xs;

    private InputMethodManager imm;
    private ClipboardManager cbm;

    private ChatListView lvChatList;
    private ChatListAdapter mChatListAdapter;
    private int mLoadMsgCnt = 18;
    private int mLoadMsgDelta = 18;

    private String mGroup = "";
    private String mUser = "";
    private boolean isGroup;
    private String mName = "";
    
    private ImageButton ibEmoji;
    private ImageButton ibMore;
    private Button btnSend;
    private EditText etSend;
    private ProgressBar loadingbar;
    
    private EmojiRelativeLayout cbrl;

    private ImageView hint;

    private PopupMenu mPopupMenu;
    
    protected Handler mHandler;
    
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
        imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        cbm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);

        //Setup UI content
        setContentView(R.layout.chat_design_layout);
        
        initView();

        /*Log.v(TAG, "PATH APK:" + MediaFile.getCurrentApkPath(app));
        Log.v(TAG, "PATH Data:"+MediaFile.getCurrentDataPath(app));
        Log.v(TAG, "PATH DB:"+MediaFile.getCurrentDBPath(app, "sc.db"));
        Log.v(TAG, "PATH SDCard/Cache:"+MediaFile.getCurrentSDCardCachePath(app));*/

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN
                /*| WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN*/);

        mHandler = new Handler();
        etSend.requestFocus();
    }
    
    private void initView() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(""); //Must set empty(""), not null, to hide title. See @ToolbarWidgetWrapper {mTitleSet = mTitle != null;}
        setSupportActionBar(toolbar);

        //Must set navigation click listener after setSupportActionBar(), 'cause it set its listener to override ours.See @ToolbarWidgetWrapper {mToolbar.setNavigationOnClickListener(...)}
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.v(TAG, "onClick(), " + v);
                imm.hideSoftInputFromWindow(etSend.getWindowToken(), 0);

                finish();
            }
        });
        
        cbrl = (EmojiRelativeLayout) findViewById(R.id.chat_btm_rl);
        cbrl.setChatThread(this);
        measureKeyboardLayout(cbrl);
        
        hint = (ImageView) findViewById(R.id.hint);
        TextView tvName = (TextView) findViewById(R.id.name);
        tvName.setText(mName);

        ibEmoji = (ImageButton) findViewById(R.id.btn_emo);
        ibMore = (ImageButton) findViewById(R.id.btn_more);
        etSend = (EditText) findViewById(R.id.et_sendmessage);
        etSend.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                Log.v(TAG, "<Text to be sent>.afterTextChanged(), " + s);
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
                Log.v(TAG, "<Text to be sent>.beforeTextChanged(), " + s + ", start=" + start + ", before=" + before + ", count=" + count);
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Log.v(TAG, "<Text to be sent>.onTextChanged(), " + s + ", start=" + start + ", before=" + before + ", count=" + count);
            }
        });
        etSend.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                Log.v(TAG, v + ".onEditorAction(), " + actionId + ", " + event);
                switch (actionId) {
                    case EditorInfo.IME_ACTION_NONE:
                        return false;
                    case EditorInfo.IME_ACTION_SEND:
                        return false;
                    case EditorInfo.IME_ACTION_DONE:

                        /*
                        * Not show ime BG if landscape, so needn't hide it anymore
                        *
                        int orient = app.getResources().getConfiguration().orientation;
                        Log.v(TAG, ".onEditorAction(), >>IME_ACTION_DONE, " + orient);
                        if (orient == Configuration.ORIENTATION_LANDSCAPE) { //Only if horizontal
                            cbrl.collapse();
                        }*/

                        if (imm.isActive() && imm.isFullscreenMode()) {
                            Log.v(TAG, ".onEditorAction(), imm is FULL SCREEN, not show ime BG!");
                            cbrl.collapse();
                        }
                        imm.hideSoftInputFromWindow(etSend.getWindowToken(), 0);
                        return true;
                }
                return false;
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

        // Initialize the ChatList and Adapter.
        lvChatList = (ChatListView)findViewById(R.id.listview);
        lvChatList.setDividerHeight(0);
        List<ChatMsgEntry> chatList;
        if (isGroup) {
            chatList = xs.getChatArrayList(mGroup, mLoadMsgCnt);
        } else {
            chatList = xs.getChatArrayList(mUser, mLoadMsgCnt);
        }
        mChatListAdapter = new ChatListAdapter(this, R.layout.chat_list_item_left, R.layout.chat_list_item_right, chatList);
        lvChatList.setAdapter(mChatListAdapter);
        lvChatList.setSelection(lvChatList.getCount() - 1);
        lvChatList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                Log.v(TAG, "onItemLongClick(), " + parent + ", " + view + ", " + position + ", " + id);
                return false;
            }
        });
        lvChatList.setOnPreTouchListener(new ChatListView.OnPreTouchListener() {
            @Override
            public void onPreTouch(MotionEvent event) {
                Log.v(TAG, "onPreTouch(), " + event);
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                    case MotionEvent.ACTION_CANCEL:
                        mChatListAdapter.bListItemLongPressed = false;
                        break;
                    case MotionEvent.ACTION_MOVE:
                        break;
                    case MotionEvent.ACTION_UP:
                        if (mChatListAdapter.bListItemLongPressed) {
                            mChatListAdapter.bListItemLongPressed = false;
                        } else {
                            //mHandler.postDelayed(new Runnable() {
                            //    @Override
                            //    public void run() {
                            hideFooterViews();
                            //    }
                            //}, 100);
                        }
                        break;

                    default:
                        break;
                }

            }
        });
        loadingbar = (ProgressBar) findViewById(R.id.loadingbar);
        loadingbar.setVisibility(View.GONE);

        SharedPreferences sp = getSharedPreferences("keyboard_height", Context.MODE_PRIVATE);
        keyboardHeight = sp.getInt("keyboardHeight", 900);

        //mPopupMenu = new PopupMenu(this, lvChatList, Gravity.CENTER);
    }
    
    private void hideFooterViews() {
        imm.hideSoftInputFromWindow(etSend.getWindowToken(), 0);
        findViewById(R.id.ll_emojichoose).setVisibility(View.GONE);
        findViewById(R.id.ll_more_operations).setVisibility(View.GONE);
    }

    class ChatListAdapter extends BaseAdapter {
        private long SHOW_TIME_GAP_THRESHOLD = 1000*20;
        
        private LayoutInflater mInflater = null;
        private int mResFromId = 0;
        private int mResToId = 0;
        private List<ChatMsgEntry> mChatList;
        private Map<String, Bitmap> mImgMap;
        private Context mContext;
        
        private boolean bListItemLongPressed;
        
        private boolean bNoMoreLoad;
        //private ProgressDialog bProgressDialog;

        public ChatListAdapter(Context context, int resFrom, int resTo, List<ChatMsgEntry> list) {
            //super(context, resource, list);
            mContext = context;
            mResFromId = resFrom;
            mResToId = resTo;
            mChatList = list;
            mInflater = LayoutInflater.from(mContext);
            //mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            mImgMap = new ArrayMap<String, Bitmap>();
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
            Log.v(TAG, "ChatListAdapter.ChatListAdapter.getItemViewType()... pos="+pos);
            return 0;
        }

        @Override
        public int getViewTypeCount() {
            Log.v(TAG, "ChatListAdapter.ChatListAdapter.getViewTypeCount()...");
            return 2;
        }

        @Override
        public View getView(int pos, View convertView, ViewGroup parent) {
            Log.v(TAG, "ChatListAdapter.getView(), pos="+pos+", convertView="+convertView+", "+parent);
            if (pos == 0 && !bNoMoreLoad) {
                int realCnt;
                if (isGroup) {
                    realCnt = xs.getChatArrayList(mChatList, mGroup, mLoadMsgCnt, mLoadMsgDelta);
                } else {
                    realCnt = xs.getChatArrayList(mChatList, mUser, mLoadMsgCnt, mLoadMsgDelta);
                }
                Log.v(TAG, "ChatListAdapter.getView(), realCnt="+realCnt);
                if (realCnt > 0) {
                    loadingbar.setVisibility(View.VISIBLE);
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            loadingbar.setVisibility(View.GONE);
                        }
                    }, 800);
                    
                    mLoadMsgCnt += realCnt;
                    notifyDataSetChanged();
                    //lvChatList.setSelection(realCnt - 1);
                    lvChatList.setSelection(realCnt + 1);
                    
                    if (realCnt < mLoadMsgDelta) {
                        Log.v(TAG, "ChatListAdapter.getView(), realCnt("+realCnt+") < mLoadMsgDelta("+mLoadMsgDelta+")");
                        bNoMoreLoad = true;
                    }
                }
            }
            
            int cnt = getCount();
            int reversePos = cnt - pos - 1;
            ChatMsgEntry chatMsg = mChatList.get(reversePos);
            ChatMsgEntry previousChatMsg;
            if (reversePos >= cnt-1) {
                previousChatMsg = null;
            } else {
                previousChatMsg = mChatList.get(reversePos+1);
            }

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
            
            v.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    Log.v(TAG, "ChatListAdapter.[item].LinearLayout..onTouch(), bListItemLongPressed=" + bListItemLongPressed);
                    return false;
                }
            });

            /**
             * Set to not display ripple when click list layout in above v21.
             */
            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.v(TAG, "ChatListAdapter.[item].LinearLayout..onClick()...");
                }
            });

            cmvh.tvName.setText(chatMsg.getName());
            cmvh.tvName.setVisibility(View.INVISIBLE); //DON'T DISPLAY NAME ANYMORE.
            cmvh.tvName.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.v(TAG, "ChatListAdapter.[item].cmvh.tvName..onClick()...");
                }
            });
            
            if (previousChatMsg != null &&
                    chatMsg.getDateLong() - previousChatMsg.getDateLong() < SHOW_TIME_GAP_THRESHOLD) {
                long gap = chatMsg.getDateLong() - previousChatMsg.getDateLong();
                Log.v(TAG, "ChatListAdapter.[item].cmvh, time gap="+gap);
                cmvh.tvDate.setVisibility(View.GONE);
            } else {
                cmvh.tvDate.setVisibility(View.VISIBLE);
                cmvh.tvDate.setText(chatMsg.getDate());
                cmvh.tvDate.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.v(TAG, "ChatListAdapter.[item].cmvh.tvDate..onClick()...");
                    }
                });
            }
            
            EmojiUtil.getInstance().setImageSizeSmall();
            SpannableString spannableString = EmojiUtil.getInstance().getExpressionString(ChatThread.this, chatMsg.getMsg());
            if (!chatMsg.isFileType() || TextUtils.isEmpty(chatMsg.getFile())) {
                cmvh.tvMsg.setText(spannableString);
                cmvh.tvMsg.setVisibility(View.VISIBLE);
                cmvh.ivMsg.setVisibility(View.GONE);
            } else {
                Log.v(TAG, "ChatListAdapter.[item].getView(), file=" + chatMsg.getFile());
                Bitmap b = null;

                if (mImgMap.containsKey(chatMsg.getFile())) {
                    b = mImgMap.get(chatMsg.getFile());
                    Log.v(TAG, "[mImgMap] Already decoded before, just use, " + b);
                } else {
                    /**
                     // Decode test
                     final int QVGA = 320 * 240; //Quarter VGA
                     final int HVGA = 320 * 480; //Half-size VGA
                     final int VGA = 640 * 480; //Video Graphics Array 307200
                     final int WVGA = 800 * 480; //Wide Video Graphics Array
                     final int XGA = 1024 * 768; //Extended Graphics Array
                     final int SXGA = 1280 * 1024; //Super eXtended Graphics Array
                     BitmapFactory.Options op = new BitmapFactory.Options();
                     op.inJustDecodeBounds = true;
                     b = BitmapFactory.decodeFile(chatMsg.getFile(), op);
                     Log.d(TAG, "outWidth=" + op.outWidth + ", outHeight=" + op.outHeight + ", b=" + b);

                     // Real decode
                     double radio = 1.0;
                     int pixels = op.outWidth*op.outHeight;
                     if (pixels < QVGA) {
                     op.inSampleSize = (int)radio;
                     } else if (pixels < HVGA) {
                     op.inSampleSize = (int)radio*2;
                     } else if (pixels < VGA) {
                     op.inSampleSize = (int)radio*4;
                     } else if (pixels < WVGA) {
                     op.inSampleSize = (int)radio*5;
                     } else if (pixels < XGA) {
                     op.inSampleSize = (int)radio*10;
                     } else if (pixels < SXGA) {
                     op.inSampleSize = (int)radio*17;
                     } else {
                     op.inSampleSize = (int)radio*20;
                     }
                     op.inJustDecodeBounds = false;
                     Log.d(TAG, "Original pixels=" + pixels + ", adjusted pixels="+pixels+"/"+op.inSampleSize);
                     b = BitmapFactory.decodeFile(chatMsg.getFile(), op);*/
                    b = ThumbnailUtils.createImageThumbnail(chatMsg.getFile(), MediaStore.Images.Thumbnails.MINI_KIND);
                    mImgMap.put(chatMsg.getFile(), b);
                    Log.v(TAG, "[mImgMap] Not decoded before, decoded, " + b);
                }

                Log.v(TAG, "b=" + b);
                if (b != null) {
                    Log.v(TAG, "b=" + b+", ["+b.getWidth()+", "+b.getHeight()+"]");
                    cmvh.ivMsg.setImageBitmap(b);
                    cmvh.ivMsg.setVisibility(View.VISIBLE);
                    cmvh.tvMsg.setVisibility(View.GONE);
                }
            }
            cmvh.tvMsg.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.v(TAG, "ChatListAdapter.[item].cmvh.tvMsg..onClick()...");
                }
            });
            cmvh.tvMsg.setOnLongClickListener(new OnMsgLongClick(reversePos));
            cmvh.ivPortrait.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.v(TAG, "ChatListAdapter.[item].cmvh.ivPortrait..onClick()...");
                }
            });

            return v;
        }
        
        private class OnMsgLongClick implements View.OnLongClickListener {
            private int mListPos;
            public OnMsgLongClick(int pos) {
                mListPos = pos;
            }
            
            @Override
            public boolean onLongClick(View v) {
                Log.v(TAG, "ChatListAdapter.[item].cmvh.tvMsg..onLongClick(), "+v);
                bListItemLongPressed = true;
                final CharSequence msg = ((TextView)v).getText();
                
                AlertDialog ops = new AlertDialog.Builder(mContext)//, R.style.AppTheme_OptionDialog)
                        .setItems(R.array.chat_msg_options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Log.v(TAG, "AlertDialog..onClick(), " + which);
                                switch (which) {
                                    case 0: //Copy
                                        cbm.setPrimaryClip(ClipData.newPlainText(null, msg));
                                        break;
                                    case 1: //Forward
                                        break;
                                    case 2: //Revoke
                                        break;
                                    case 3: //Delete
                                        ChatMsgEntry chatMsg = mChatList.get(mListPos);
                                        xs.deleteMsg(mUser, chatMsg.getMsgId(), chatMsg.getDateLong());
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

        public void update() {
            Log.v(TAG, "ChatListAdapter.update()..");

            if (isGroup) {
                mChatList = xs.getChatArrayList(mGroup, mLoadMsgCnt);
            } else {
                mChatList = xs.getChatArrayList(mUser, mLoadMsgCnt);
            }
            notifyDataSetChanged();
            lvChatList.setSelection(mChatList.size() - 1);
            bNoMoreLoad = false;
        }

        public class ChatMsgItemViewHolder {
            public TextView tvName;
            public TextView tvDate;
            public TextView tvMsg;
            public ImageView ivMsg;
            public ImageView ivPortrait;
            public boolean bFrom;
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
            //private boolean bInit = true;
            private boolean bCollapse = true;
            @Override
            public void onGlobalLayout() {
                //if (bInit) {
                //    Log.v(TAG, "onGlobalLayout(), ignore layout init.");
                //    bInit = false;
                //    return;
                //}
                
                Rect rect = new Rect();
                content.getWindowVisibleDisplayFrame(rect);
                Log.w(TAG, "onGlobalLayout(), content.getRootView().getHeight()="+content.getRootView().getHeight()+", rect.bottom="+rect.bottom);
                int height = content.getRootView().getHeight() - rect.bottom - getNavigationBarHeight(ChatThread.this);

                Log.w(TAG, "onGlobalLayout(), height="+height+", bCollapse="+bCollapse);
                if (height == 0) { // To collapse
                    if (!bCollapse) {
                        Log.w(TAG, "onGlobalLayout(), keyboard collapsed");
                        olcn.collapse();
                        bCollapse = true;

                        scrollToBottom();
                    } else {
                        Log.w(TAG, "onGlobalLayout(), already collapsed, ignore");
                        //bCollapse = false;
                    }
                    //scrollToBottom();
                } else { // To expand
                    int orient = app.getResources().getConfiguration().orientation;
                    if (orient == Configuration.ORIENTATION_LANDSCAPE) {
                        Log.w(TAG, "onGlobalLayout(), ORIENTATION_LANDSCAPE, ignore, " + orient);
                        return;
                    }
                    
                    if (height != keyboardHeight) {
                        Log.w(TAG, "onGlobalLayout(), keyboard height changed:" + height);
                        keyboardHeight = height;

                        SharedPreferences sp = getSharedPreferences("keyboard_height", Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sp.edit();
                        editor.putInt("keyboardHeight", height);
                        editor.commit();

                        Log.w(TAG, "onGlobalLayout(), keyboard expanded");
                        olcn.expand(height, true);
                        bCollapse = false;

                        scrollToBottom();
                    } else {
                        Log.w(TAG, "onGlobalLayout(), keyboard height not change:" + height);

                        if (bCollapse) {
                            Log.w(TAG, "onGlobalLayout(), keyboard expanded");
                            olcn.expand(height, false);
                            bCollapse = false;

                            scrollToBottom();
                        }
                    }
                }
            }
        });
    }
    
    //Fix NavigationBar Start
    public boolean isNavigationBarShow(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            Display display = getWindowManager().getDefaultDisplay();
            Point size = new Point();
            Point realSize = new Point();
            display.getSize(size);
            display.getRealSize(realSize);
            return realSize.y!=size.y;
        }else {
            boolean menu = ViewConfiguration.get(this).hasPermanentMenuKey();
            boolean back = KeyCharacterMap.deviceHasKey(KeyEvent.KEYCODE_BACK);
            if(menu || back) {
                return false;
            }else {
                return true;
            }
        }
    }

    public int getNavigationBarHeight(Activity activity) {
        if (!isNavigationBarShow()){
            return 0;
        }
        Resources resources = activity.getResources();
        int resourceId = resources.getIdentifier("navigation_bar_height",
                "dimen", "android");
        //获取NavigationBar的高度
        int height = resources.getDimensionPixelSize(resourceId);
        return height;
    }

    public int getSceenHeight(Activity activity) {
        return activity.getWindowManager().getDefaultDisplay().getHeight()+getNavigationBarHeight(activity);
    }
    //Fix NavigationBar End

    private int keyboardHeight;
    public int getBottomHeight() {
        Log.v(TAG, "getBottomHeight(), keyboardHeight=" + keyboardHeight);
        return keyboardHeight;
    }

    public void scrollToBottom() {
        //Scroll to the bottom
        Log.v(TAG, "scrollToBottom()..");
        lvChatList.setSelection(lvChatList.getCount() - 1);
    }

    public void postScrollToBottom() {
        Log.v(TAG, "postScrollToBottom()..");
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                scrollToBottom();
            }
        }, 50);
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
                            xs.setGroupRead(ChatThread.this, mGroup);
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

        if (!isGroup) {
            xs.setCurrentThreadUser(mUser);
            if (xs.getUserUnread(this, mUser) > 0) {
                mChatListAdapter.update();
            }
        } else {
            //Todo: deal with group here
        }

        if (isGroup) {
            xs.setGroupRead(ChatThread.this, mGroup);
        } else {
            xs.setUserRead(ChatThread.this, mUser);
        }
        
        registerReceiver();
        //xs.unregisterMessageReceiver();
    }

    @Override
    public void onPause() {
        Log.v(TAG, "onPause()... ");
        super.onPause();

        //Collapse the IMM
        imm.hideSoftInputFromWindow(etSend.getWindowToken(), 0);
        

        if (!isGroup) {
            xs.setCurrentThreadUser(null);
        } else {

        }
        unregisterReceiver();
        //xs.registerMessageReceiver();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.chatthread, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.add_friend:
                Intent i = new Intent();
                i.setClass(this, com.lightmsg.activity.msgdesign.etc.AddFriend.class);
                startActivity(i);
                return true;
            case R.id.search:
                return true;
            case R.id.contact_us:
                return true;

            case R.id.home:
                return true;
        }
        return super.onOptionsItemSelected(item);
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
            String file = MediaFile.uriToPath(ChatThread.this, data.getData());
            xs.sendFile(mUser, "IMAGE",new File(file));
            break;

        case ACTION_FILE:
            break;

        case ACTION_LOCATION:
            break;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //Log.v(TAG, "[TOUCH_INVEST]onTouchEvent(), event=" + event);
        //printStack();
        return super.onTouchEvent(event);
    }

    private void printStack() {
        try {
            throw new Exception();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
