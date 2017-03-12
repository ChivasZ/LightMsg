package com.lightmsg.activity.msgdesign.chat;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.SpannableString;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.ActionMode;
import android.view.DragEvent;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.lightmsg.R;
import com.lightmsg.activity.ViewPagerAdapter;
import com.lightmsg.util.EmojiUtil;
import com.lightmsg.util.EmojiUtil.ChatEmoji;


public class EmojiRelativeLayout extends RelativeLayout implements View.OnLongClickListener, View.OnDragListener, View.OnTouchListener, OnClickListener, View.OnFocusChangeListener, OnLayoutChangeNotify {
    private static final String TAG = EmojiRelativeLayout.class.getSimpleName();

    private ChatThread ct;
    private Context context;

    private OnCorpusSelectedListener mListener;

    private View imeBg;
    private InputMethodManager imm;

    private boolean keyboardCollapsed = true;
    private boolean longPressedEditText;

    // For emoji layout.
    private ViewPager vp_emoji;
    private ArrayList<View> pageViews;
    private LinearLayout layout_point;
    private ArrayList<ImageView> pointViews;
    private List<EmojiAdapter> emojiAdapters;
    private List<List<ChatEmoji>> emojis;
    private int current = 0;
    private View emojiView;

    // For more operation layout.
    private ViewPager vp_more;
    private ArrayList<View> pageViews2;
    private LinearLayout layout_point2;
    private ArrayList<ImageView> pointViews2;
    private List<MoreOpeAdapter> moreOpAdapters;
    private static List<List<ChatMoreOp>> ops = new ArrayList<List<ChatMoreOp>>();
    ;
    private int current2 = 0;
    private View moreOpView;

    private static final String ACTION_IMAGE = "image";
    private static final String ACTION_FILE = "file";
    private static final String ACTION_LOCATION = "location";
    private static final String ACTION_VOICE = "voice";

    private State state = State.none;

    private enum State {
        none,
        emoji,
        more
    }

    private EditText et_sendmessage;

    private int DOT_IMAGE_SIZE = 5;
    private float DOT_IMAGE_PX_SIZE;

    static {
        List<ChatMoreOp> op = new ArrayList<ChatMoreOp>();
        ChatMoreOp cmo1 = new ChatMoreOp();
        cmo1.setId(R.drawable.mime_detail_ic_images);
        cmo1.setAction(ACTION_IMAGE);
        op.add(cmo1);

        ChatMoreOp cmo2 = new ChatMoreOp();
        cmo2.setId(R.drawable.mime_detail_ic_memo);
        cmo2.setAction(ACTION_FILE);
        op.add(cmo2);

        ChatMoreOp cmo3 = new ChatMoreOp();
        cmo3.setId(R.drawable.attach_map);
        cmo3.setAction(ACTION_LOCATION);
        op.add(cmo3);

        ChatMoreOp cmo4 = new ChatMoreOp();
        cmo4.setId(R.drawable.email_attach_record_audio);
        cmo4.setAction(ACTION_VOICE);
        op.add(cmo4);

        ops.add(op);
    }

    public static class ChatMoreOp {
        private int mID;
        private String mAction;

        public ChatMoreOp setId(int resID) {
            mID = resID;

            return this;
        }

        public ChatMoreOp setAction(String action) {
            mAction = action;

            return this;
        }

        public int getId() {
            return mID;
        }

        public String getAction() {
            return mAction;
        }
    }

    public EmojiRelativeLayout(Context context) {
        super(context);
        this.context = context;
    }

    public EmojiRelativeLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
    }

    public EmojiRelativeLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.context = context;
    }

    public void setOnCorpusSelectedListener(OnCorpusSelectedListener listener) {
        mListener = listener;
    }

    public interface OnCorpusSelectedListener {
        void onCorpusSelected(ChatEmoji emoji);

        void onCorpusDeleted();
    }

    @Override
    protected void onFinishInflate() {
        Log.v(TAG, "onCreate()...");
        super.onFinishInflate();
        emojis = EmojiUtil.getInstance().emojiLists;
        onCreate();
    }

    private void onCreate() {
        Log.v(TAG, "onCreate()...");
        init();
    }

    private void init() {
        state = State.none;

        initView();
        initViewPager();
        initPoint();
        initData();

        initViewPager2();
        initPoint2();
        initData2();
    }

    private int getAdjustHeight() {
        int adjH = 0;
        if (ct != null) {
            adjH = ct.getBottomHeight();
            //adjH = pxToDp(ct, ct.getBottomHeight());
        }
        Log.v(TAG, "getAdjustHeight(), " + adjH);
        return adjH;
    }

    @Override
    public void expand(int h, boolean changed) {
        Log.v(TAG, "expand(), height=" + h +", changed="+changed+ ", keyboardCollapsed=" + keyboardCollapsed);
        
        
        if (changed && imeBg.getVisibility() == View.VISIBLE) {
            Log.v(TAG, "expand(1), " + h);
            if (h != 0) {
                ViewGroup.LayoutParams rl = imeBg.getLayoutParams();
                rl.height = h;
                imeBg.setLayoutParams(rl);
            }
        }/* else if (emojiView.getVisibility() == View.VISIBLE) {
            Log.v(TAG, "expand(2), " + h);
            if (h != 0) {
                ViewGroup.LayoutParams rl = emojiView.getLayoutParams();
                rl.height = h;
                emojiView.setLayoutParams(rl);
            }
        } else if (moreOpView.getVisibility() == View.VISIBLE) {
            Log.v(TAG, "expand(3), " + h);
            if (h != 0) {
                ViewGroup.LayoutParams rl = moreOpView.getLayoutParams();
                rl.height = h;
                moreOpView.setLayoutParams(rl);
            }
        } else { // To show IME BG
            Log.v(TAG, "expand(4), " + h);
            if (h != 0) {
                ViewGroup.LayoutParams rl = imeBg.getLayoutParams();
                rl.height = h;
                imeBg.setLayoutParams(rl);
            }
            imeBg.setVisibility(View.VISIBLE);
        }*/

        keyboardCollapsed = false;
    }

    @Override
    public void collapse() {
        Log.v(TAG, "collapse(), " + keyboardCollapsed);
        if (imeBg.getVisibility() == View.VISIBLE) {
            //imm.hideSoftInputFromWindow(et_sendmessage.getWindowToken(), 0);
            imeBg.setVisibility(View.GONE);
        } else {
            Log.v(TAG, "collapse(), Nothing to do?");
        }

        keyboardCollapsed = true;
    }
    
    public boolean isImmShowing() {
        Log.v(TAG, "isImmShowing(), " + (ct.getWindow().getAttributes().softInputMode & WindowManager.LayoutParams.SOFT_INPUT_MASK_STATE));
        return ((ct.getWindow().getAttributes().softInputMode
                    & WindowManager.LayoutParams.SOFT_INPUT_MASK_STATE)
                != WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        Log.v(TAG, "onFocusChange(), "+v+", "+hasFocus);
        if (true) return;
        if (v.getId() == R.id.et_sendmessage) {
            if (hasFocus) {
                if (emojiView.getVisibility() == View.VISIBLE) {
                    emojiView.setVisibility(View.GONE);
                }
                if (moreOpView.getVisibility() == View.VISIBLE) {
                    moreOpView.setVisibility(View.GONE);
                }
                
                if (imeBg.getVisibility() != View.VISIBLE) {
                    Log.v(TAG, "onFocusChange(), et_sendmessage");

                    /**
                     * @deprecated Should not check orientation, but if IMM full screen.
                     */
                    int orient = ct.getResources().getConfiguration().orientation;
                    if (orient == Configuration.ORIENTATION_LANDSCAPE) {
                        Log.v(TAG, "onFocusChange(), ORIENTATION_LANDSCAPE, not show ime BG!");
                        return;
                    }
                    /*if (imm.isActive() && imm.isFullscreenMode()) {
                        Log.v(TAG, "onClick(), imm is FULL SCREEN, not show ime BG!");
                        return;
                    }*/
                    
                    int h = getAdjustHeight();
                    if (h != 0) {
                        ViewGroup.LayoutParams rl = imeBg.getLayoutParams();
                        rl.height = h;
                        imeBg.setLayoutParams(rl);
                    }
                    imeBg.setVisibility(View.VISIBLE);
                }
            }
        }
    }
    
    @Override
    public void onClick(View v) {
        Log.v(TAG, "onClick()...v=" + v);
        switch (v.getId()) {
            case R.id.btn_emo:
                state = State.emoji;
                if (imeBg.getVisibility() == View.VISIBLE) {
                    imeBg.setVisibility(View.GONE);
                    imm.hideSoftInputFromWindow(et_sendmessage.getWindowToken(), 0);
                }
                if (moreOpView.getVisibility() == View.VISIBLE) {
                    moreOpView.setVisibility(View.GONE);
                }
                if (emojiView.getVisibility() == View.VISIBLE) {
                    emojiView.setVisibility(View.GONE);
                } else {
                    //emojiView.setLayoutParams(new RelativeLayout.LayoutParams(emojiView.getWidth(), getAdjustHeight()));
                    int h = getAdjustHeight();
                    if (h != 0) {
                        ViewGroup.LayoutParams rl = emojiView.getLayoutParams();
                        rl.height = h;
                        emojiView.setLayoutParams(rl);
                    }

                    emojiView.setVisibility(View.VISIBLE);
                    ct.postScrollToBottom();
                }
                break;
            case R.id.btn_more:
                state = State.more;
                if (imeBg.getVisibility() == View.VISIBLE) {
                    imeBg.setVisibility(View.GONE);
                    imm.hideSoftInputFromWindow(et_sendmessage.getWindowToken(), 0);
                }
                if (emojiView.getVisibility() == View.VISIBLE) {
                    emojiView.setVisibility(View.GONE);
                }
                if (moreOpView.getVisibility() == View.VISIBLE) {
                    moreOpView.setVisibility(View.GONE);
                } else {
                    int h = getAdjustHeight();
                    if (h != 0) {
                        ViewGroup.LayoutParams rl = moreOpView.getLayoutParams();
                        //rl.width = moreOpView.getWidth();
                        rl.height = h;
                        moreOpView.setLayoutParams(rl);
                    }

                    moreOpView.setVisibility(View.VISIBLE);
                    ct.postScrollToBottom();
                }
                break;
            default:
                state = State.none;
                break;
        }
    }
    
    /**
     * Called when a drag event is dispatched to a view. This allows listeners
     * to get a chance to override base View behavior.
     *
     * @param v The View that received the drag event.
     * @param event The {@link android.view.DragEvent} object for the drag event.
     * @return {@code true} if the drag event was handled successfully, or {@code false}
     * if the drag event was not handled. Note that {@code false} will trigger the View
     * to call its {@link #onDragEvent(DragEvent) onDragEvent()} handler.
     */
    @Override
    public boolean onDrag(View v, DragEvent event) {
        Log.v(TAG, "onDrag()...v=" + v + ", ev=" + event);
        return false;
    }

    private class ActionModeCB implements ActionMode.Callback {
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            Log.v(TAG, ".onCreateActionMode(), " + mode + ", " + menu);
            return false;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            Log.v(TAG, ".onPrepareActionMode(), " + mode + ", " + menu);
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            Log.v(TAG, ".onActionItemClicked(), " + mode + ", " + item);
            return false;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            Log.v(TAG, ".onDestroyActionMode(), " + mode);
        }
    }

    /**
     * Called when a touch event is dispatched to a view. This allows listeners to
     * get a chance to respond before the target view.
     *
     * @param v The view the touch event has been dispatched to.
     * @param event The MotionEvent object containing full information about
     *        the event.
     * @return True if the listener has consumed the event, false otherwise.
     */
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        Log.v(TAG, "onTouch()...v=" + v + ", ev=" + event);
        if (event.getAction() == MotionEvent.ACTION_UP) {
            switch (v.getId()) {
                case R.id.et_sendmessage:
                    state = State.none;
                    if (emojiView.getVisibility() == View.VISIBLE) {
                        emojiView.setVisibility(View.GONE);
                    }
                    if (moreOpView.getVisibility() == View.VISIBLE) {
                        moreOpView.setVisibility(View.GONE);
                    }
                    if (imeBg.getVisibility() != View.VISIBLE) {
                        Log.v(TAG, "onTouch(), click et_sendmessage");

                        /**
                         * @deprecated Should not check orientation, but if IMM full screen.
                         */
                        int orient = ct.getResources().getConfiguration().orientation;
                        if (orient == Configuration.ORIENTATION_LANDSCAPE) {
                            Log.v(TAG, "onTouch(), ORIENTATION_LANDSCAPE, not show ime BG!");
                            break;
                        }
                    /*if (imm.isActive() && imm.isFullscreenMode()) {
                        Log.v(TAG, "onClick(), imm is FULL SCREEN, not show ime BG!");
                        break;
                    }*/

                        if (!et_sendmessage.isFocused()) {
                            Log.v(TAG, "onTouch(), et_sendmessage is not focused!!!");
                            return false;
                        }
                        
                        if (longPressedEditText) {
                            Log.v(TAG, "onTouch(), long pressed, not show ime BG!");
                            longPressedEditText = false;
                            
                            //return false; // Instead return true @ onLongClick to ignore up.
                            return true; //Ignore up event, not to show keyboard
                        }

                        //Always show imm if reach here.
                        imm.viewClicked(et_sendmessage);
                        imm.showSoftInput(et_sendmessage, 0);

                        int h = getAdjustHeight();
                        if (h != 0) {
                            ViewGroup.LayoutParams rl = imeBg.getLayoutParams();
                            rl.height = h;
                            imeBg.setLayoutParams(rl);
                        }
                        imeBg.setVisibility(View.VISIBLE);

                        ct.mHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                if (keyboardCollapsed) {
                                    Log.e(TAG, "Hide imebg, due to keyboard not shown in 500ms");
                                    imeBg.setVisibility(View.GONE);
                                }
                            }
                        }, 1000);
                    } else {
                        if (longPressedEditText) {
                            Log.v(TAG, "onTouch(), long pressed, but ime BG is showing!");
                            longPressedEditText = false;
                        }
                    }
                    break;
                default:
                    break;
            }
            return false;
        }
        return false;
    }

    @Override
    public boolean onLongClick(View v) {
        Log.v(TAG, "onLongClick()...v=" + v);
        if (v.getId() == R.id.et_sendmessage) {
            longPressedEditText = true;
            //return true; // To ignore following up event
        }
        
        return false;
    }

    private void initView() {
        Log.v(TAG, "Init_View()...");
        et_sendmessage = (EditText) findViewById(R.id.et_sendmessage);
        //et_sendmessage.setOnClickListener(this);
        //et_sendmessage.setOnFocusChangeListener(this);
        //et_sendmessage.setOnDragListener(this);
        //et_sendmessage.setCustomSelectionActionModeCallback(new ActionModeCB());
        et_sendmessage.setOnTouchListener(this);
        et_sendmessage.setOnLongClickListener(this);

        vp_emoji = (ViewPager) findViewById(R.id.vp_contains);
        layout_point = (LinearLayout) findViewById(R.id.iv_image);
        findViewById(R.id.btn_emo).setOnClickListener(this);
        emojiView = findViewById(R.id.ll_emojichoose);

        vp_more = (ViewPager) findViewById(R.id.vp_contains2);
        layout_point2 = (LinearLayout) findViewById(R.id.iv_image2);
        findViewById(R.id.btn_more).setOnClickListener(this);
        moreOpView = findViewById(R.id.ll_more_operations);

        imeBg = findViewById(R.id.ime_bg);

        //RelativeLayout emoji_rl = (RelativeLayout) findViewById(R.id.emoji_rl);
        //controlKeyboardLayout(this.getRootView(), emoji_rl);
        imm = (InputMethodManager)context.getSystemService(Context.INPUT_METHOD_SERVICE);

        //Set bottom height with previous keyboard height
        SharedPreferences sp = context.getSharedPreferences("keyboard_height", Context.MODE_PRIVATE);
        int h = sp.getInt("keyboardHeight", 0);
        if (h != 0) {
            ViewGroup.LayoutParams rl1 = imeBg.getLayoutParams();
            rl1.height = h;
            imeBg.setLayoutParams(rl1);

            ViewGroup.LayoutParams rl2 = emojiView.getLayoutParams();
            rl2.height = h;
            emojiView.setLayoutParams(rl2);

            ViewGroup.LayoutParams rl3 = moreOpView.getLayoutParams();
            rl3.height = h;
            moreOpView.setLayoutParams(rl3);
        }
    }

    private void initViewPager() {
        Log.v(TAG, "initViewPager()...");
        pageViews = new ArrayList<View>();

        View nullView1 = new View(context);

        nullView1.setBackgroundColor(Color.TRANSPARENT);
        pageViews.add(nullView1);

        emojiAdapters = new ArrayList<EmojiAdapter>();
        for (int i = 0; i < emojis.size(); i++) {
            Log.v(TAG, "initViewPager()...i=" + i);
            GridView view = new GridView(context);
            EmojiAdapter adapter = new EmojiAdapter(context, emojis.get(i));
            view.setAdapter(adapter);
            emojiAdapters.add(adapter);
            view.setOnItemClickListener(oicl);
            view.setNumColumns(7);
            view.setBackgroundColor(Color.TRANSPARENT);
            view.setHorizontalSpacing(1);
            view.setVerticalSpacing(dpToPx(context, 16));
            view.setStretchMode(GridView.STRETCH_COLUMN_WIDTH);
            view.setCacheColorHint(0);
            view.setPadding(5, 5, 5, 5);
            view.setSelector(new ColorDrawable(Color.TRANSPARENT));
            view.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
                    LayoutParams.WRAP_CONTENT));
            view.setGravity(Gravity.CENTER);
            pageViews.add(view);
        }

        View nullView2 = new View(context);
        nullView2.setBackgroundColor(Color.TRANSPARENT);
        pageViews.add(nullView2);
    }

    private void initPoint() {
        Log.v(TAG, "initPoint()...");
        pointViews = new ArrayList<ImageView>();
        ImageView imageView;
        for (int i = 0; i < pageViews.size(); i++) {
            imageView = new ImageView(context);
            imageView.setBackgroundResource(R.drawable.d1);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                    new ViewGroup.LayoutParams(LayoutParams.WRAP_CONTENT,
                            LayoutParams.WRAP_CONTENT));
            layoutParams.leftMargin = 10;
            layoutParams.rightMargin = 10;
            DOT_IMAGE_PX_SIZE = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, DOT_IMAGE_SIZE,
                    context.getResources().getDisplayMetrics());
            layoutParams.width = (int) DOT_IMAGE_PX_SIZE;
            layoutParams.height = (int) DOT_IMAGE_PX_SIZE;
            layout_point.addView(imageView, layoutParams);
            if (i == 0 || i == pageViews.size() - 1) {
                imageView.setVisibility(View.GONE);
            }
            if (i == 1) {
                imageView.setBackgroundResource(R.drawable.d2);
            }
            pointViews.add(imageView);

        }
    }

    private void initData() {
        Log.v(TAG, "initData()...");
        vp_emoji.setAdapter(new ViewPagerAdapter(pageViews));

        vp_emoji.setCurrentItem(1);
        current = 0;
        vp_emoji.setOnPageChangeListener(new OnPageChangeListener() {
            @Override
            public void onPageSelected(int pageNum) {
                Log.v(TAG, "onPageSelected()...");
                current = pageNum - 1;
                drawPoint(pageNum);
                if (pageNum == pointViews.size() - 1 || pageNum == 0) {
                    if (pageNum == 0) {
                        vp_emoji.setCurrentItem(pageNum + 1);
                        pointViews.get(1).setBackgroundResource(R.drawable.d2);
                    } else {
                        vp_emoji.setCurrentItem(pageNum - 1);
                        pointViews.get(pageNum - 1).setBackgroundResource(
                                R.drawable.d2);
                    }
                }
            }

            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2) {
                Log.v(TAG, "onPageScrolled()...");
            }

            @Override
            public void onPageScrollStateChanged(int arg0) {
                Log.v(TAG, "onPageScrollStateChanged()...");
            }
        });

    }

    public void drawPoint(int index) {
        Log.v(TAG, "drawPoint()...");
        for (int i = 1; i < pointViews.size(); i++) {
            if (index == i) {
                pointViews.get(i).setBackgroundResource(R.drawable.d2);
            } else {
                pointViews.get(i).setBackgroundResource(R.drawable.d1);
            }
        }
    }

    private OnItemClickListener oicl = new OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> parent,
                                View view,
                                int position,
                                long id) {
            // TODO Auto-generated method stub
            Log.v(TAG, "onItemClick()...");
            ChatEmoji emoji = (ChatEmoji) emojiAdapters.get(current).getItem(position);
            if (emoji.getId() == R.drawable.face_del_icon) {
                int selection = et_sendmessage.getSelectionStart();
                String text = et_sendmessage.getText().toString();
                if (selection > 0) {
                    String text2 = text.substring(selection - 1);
                    if ("]".equals(text2)) {
                        int start = text.lastIndexOf("[");
                        int end = selection;
                        et_sendmessage.getText().delete(start, end);
                        return;
                    }
                    et_sendmessage.getText().delete(selection - 1, selection);
                }
            }
            if (!TextUtils.isEmpty(emoji.getCharacter())) {
                if (mListener != null)
                    mListener.onCorpusSelected(emoji);
                SpannableString spannableString = EmojiUtil.getInstance()
                        .addEmoji(getContext(), emoji.getId(), emoji.getCharacter());

                int start = et_sendmessage.getSelectionStart();
                int end = et_sendmessage.getSelectionEnd();
                int length = et_sendmessage.length();
                if (start == end) {
                    if (length > end) {
                        CharSequence msg1 = et_sendmessage.getText().subSequence(0, end);
                        CharSequence msg2 = et_sendmessage.getText().subSequence(end, length);
                        et_sendmessage.setText(msg1);
                        et_sendmessage.append(spannableString);
                        int len = et_sendmessage.length();
                        et_sendmessage.append(msg2);
                        et_sendmessage.setSelection(len);
                    } else {
                        et_sendmessage.append(spannableString);
                    }
                } else if (end > start) {
                    CharSequence msg1 = et_sendmessage.getText().subSequence(0, start);
                    CharSequence msg2 = et_sendmessage.getText().subSequence(end, length);
                    et_sendmessage.setText(msg1);
                    et_sendmessage.append(spannableString);
                    int len = et_sendmessage.length();
                    et_sendmessage.append(msg2);
                    et_sendmessage.setSelection(len);
                }
                
                //et_sendmessage.setTextSize(TypedValue.COMPLEX_UNIT_PX, ct.getResources().getDimensionPixelSize(R.dimen.chat_btm_send_edittext_textsize));
                //Log.v(TAG, "chat_btm_send_edittext_textsize="+ct.getResources().getDimensionPixelSize(R.dimen.chat_btm_send_edittext_textsize));
            }
        }

    };

    private void initViewPager2() {
        Log.v(TAG, "initViewPager2()...");
        pageViews2 = new ArrayList<View>();

        View nullView1 = new View(context);

        nullView1.setBackgroundColor(Color.TRANSPARENT);
        pageViews2.add(nullView1);

        moreOpAdapters = new ArrayList<MoreOpeAdapter>();
        for (int i = 0; i < ops.size(); i++) {
            Log.v(TAG, "initViewPager()...i=" + i);
            GridView view = new GridView(context);
            MoreOpeAdapter adapter = new MoreOpeAdapter(context, ops.get(i));
            view.setAdapter(adapter);
            moreOpAdapters.add(adapter);
            view.setOnItemClickListener(oicl2);
            view.setNumColumns(4);
            view.setBackgroundColor(Color.TRANSPARENT);
            view.setHorizontalSpacing(1);
            view.setVerticalSpacing(1);
            view.setStretchMode(GridView.STRETCH_COLUMN_WIDTH);
            view.setCacheColorHint(0);
            view.setPadding(5, 5, 5, 5);
            view.setSelector(new ColorDrawable(Color.TRANSPARENT));
            view.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
                    LayoutParams.WRAP_CONTENT));
            view.setGravity(Gravity.CENTER);
            pageViews2.add(view);
        }

        View nullView2 = new View(context);
        nullView2.setBackgroundColor(Color.TRANSPARENT);
        pageViews2.add(nullView2);
    }

    private void initPoint2() {
        Log.v(TAG, "initPoint2()...");
        pointViews2 = new ArrayList<ImageView>();
        ImageView imageView;
        for (int i = 0; i < pageViews2.size(); i++) {
            imageView = new ImageView(context);
            imageView.setBackgroundResource(R.drawable.d1);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                    new ViewGroup.LayoutParams(LayoutParams.WRAP_CONTENT,
                            LayoutParams.WRAP_CONTENT));
            layoutParams.leftMargin = 10;
            layoutParams.rightMargin = 10;
            DOT_IMAGE_PX_SIZE = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, DOT_IMAGE_SIZE,
                    context.getResources().getDisplayMetrics());
            layoutParams.width = (int) DOT_IMAGE_PX_SIZE;
            layoutParams.height = (int) DOT_IMAGE_PX_SIZE;
            layout_point2.addView(imageView, layoutParams);
            if (i == 0 || i == pageViews2.size() - 1) {
                imageView.setVisibility(View.GONE);
            }
            if (i == 1) {
                imageView.setBackgroundResource(R.drawable.d2);
            }
            pointViews2.add(imageView);
        }
    }

    private void initData2() {
        Log.v(TAG, "initData2()...");
        vp_more.setAdapter(new ViewPagerAdapter(pageViews2));

        vp_more.setCurrentItem(1);
        current2 = 0;
        vp_more.setOnPageChangeListener(new OnPageChangeListener() {
            @Override
            public void onPageSelected(int pageNum) {
                Log.v(TAG, "onPageSelected2()...");
                current2 = pageNum - 1;
                drawPoint(pageNum);
                if (pageNum == pointViews2.size() - 1 || pageNum == 0) {
                    if (pageNum == 0) {
                        vp_more.setCurrentItem(pageNum + 1);
                        pointViews2.get(1).setBackgroundResource(R.drawable.d2);
                    } else {
                        vp_more.setCurrentItem(pageNum - 1);
                        pointViews2.get(pageNum - 1).setBackgroundResource(
                                R.drawable.d2);
                    }
                }
            }

            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2) {
                Log.v(TAG, "onPageScrolled2()...");
            }

            @Override
            public void onPageScrollStateChanged(int arg0) {
                Log.v(TAG, "onPageScrollStateChanged2()...");
            }
        });

    }

    public void drawPoint2(int index) {
        Log.v(TAG, "drawPoint()...");
        for (int i = 1; i < pointViews2.size(); i++) {
            if (index == i) {
                pointViews2.get(i).setBackgroundResource(R.drawable.d2);
            } else {
                pointViews2.get(i).setBackgroundResource(R.drawable.d1);
            }
        }
    }

    private OnItemClickListener oicl2 = new OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> parent,
                                View view,
                                int position,
                                long id) {
            // TODO Auto-generated method stub
            Log.v(TAG, "onItemClick()...");
            ChatMoreOp op = (ChatMoreOp) moreOpAdapters.get(current).getItem(position);
            //if (op.getId() == R.drawable.face_del_icon) {
            //}
            if (!TextUtils.isEmpty(op.getAction())) {
                if (ACTION_IMAGE.equals(op.getAction())) {
                    ct.fetchImage();
                } else if (ACTION_FILE.equals(op.getAction())) {

                } else if (ACTION_LOCATION.equals(op.getAction())) {

                } else if (ACTION_VOICE.equals(op.getAction())) {

                } else {

                }
            }
        }

    };

    public void setChatThread(ChatThread ct) {
        this.ct = ct;
    }

    private static int dpToPx(Context context, float dp) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }

    private static int pxToDp(Context context, float px) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (px / scale + 0.5f);
    }

    public boolean hideEmojiView() {
        Log.v(TAG, "hideEmojiView()...");
        state = State.none;
        if (emojiView.getVisibility() == View.VISIBLE) {
            emojiView.setVisibility(View.GONE);
            return true;
        }
        return false;
    }

    public boolean hideMoreOpView() {
        Log.v(TAG, "hideEmojiView()...");
        state = State.none;
        if (moreOpView.getVisibility() == View.VISIBLE) {
            moreOpView.setVisibility(View.GONE);
            return true;
        }
        return false;
    }
}

