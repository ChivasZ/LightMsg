package com.lightmsg.activity.msgdesign;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.ImageView;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TabHost.TabSpec;
import android.widget.TabWidget;
import android.widget.TextView;

import com.lightmsg.BadgeView;
import com.lightmsg.LightMsg;
import com.lightmsg.R;
import com.lightmsg.activity.LocationByManual;
import com.lightmsg.activity.mall.MallActivity;
import com.lightmsg.activity.msgdesign.chat.ChatThread;
import com.lightmsg.service.CoreService;
import com.lightmsg.service.CoreService.Account;
import com.lightmsg.util.CountDownTimer;
import com.lightmsg.util.Locator;


public class LightMsgActivity extends AppCompatActivity {
    private static final String TAG = LightMsgActivity.class.getName();

    private LightMsg app;
    private CoreService xs;

    public TabHost th;
    public TabWidget tw;
    //private BadgeView bv;

    private Locator locator;
    private String current;
    private String manual;

    private CountDownTimer cdtLocation;

    //For badge icon.
    private BadgeView mbvUnreadCount;

    //Update unread messages.
    private MessageReceiver messageReceiver;
    private IntentFilter messageFilter;

    private Resources res;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.v(TAG, "onCreate()..");

        super.onCreate(savedInstanceState);
        app = (LightMsg)getApplication();
        xs = app.xs;

        //Check here...
        if (xs == null) {
            Log.e(TAG, "onCreate(), to check: xs is null!!!");
            finish();
            return;
        }

        setContentView(R.layout.lightmsg_design_layout);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //AppBarLayout appbar = (AppBarLayout) findViewById(R.id.appbar);
        //appbar.setExpanded(false, false);

        initTabHost();
        
        if (xs != null) {
            //if () //Preference
            xs.updateRoster();
            xs.setReceiveFile();
        }

        registerReceiver();
    }

    public void initLocator() {
        //getActionBar().setTitle(getResources().getString(R.string.locating));

        try {
            locator = Locator.getInstance(this);
            locator.initLocation();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
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

    private boolean setUserInfo(String un, String oldpwd, String newpwd, String name,
            String email, String relation, String group, String portrait) {
        Log.v(TAG, "setUserInfo()..");

        if (xs != null) {
            Account account = xs.getAccount();
            if (account == null) {
                account = xs.new Account();
            }
            account.account = un;
            account.nick = name;
            account.pwd = newpwd;
            account.gender = "未知";
            
            xs.setAccount(account);
            /*Editor editor = xs.getSharedPreferencesLogin().edit();
            editor.putString("username", un);
            editor.putString("password", oldpwd);
            editor.putString("email", email);
            editor.putString("name", name);
            editor.putString("newpwd", newpwd);
            editor.putString("relation", relation);
            editor.putString("group", group);
            editor.putString("portrait", portrait);
            editor.commit();*/
        }

        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.lightmsg, menu);
        return true;
    }

    @Override
    protected void onResume() {
        Log.v(TAG, "onResume()...");
        super.onResume();
        xs.unregisterMessageReceiver();
    }

    @Override
    protected void onPause() {
        Log.v(TAG, "onPause()...");
        super.onPause();
        xs.registerMessageReceiver();
    }

    @Override
    protected void onStart() {
        Log.v(TAG, "onStart()...");
        super.onStart();

        //UpperPagerViewFragment.setUpperPagerView(this);

        xs.cancelNotificationMessage();
    }

    @Override
    protected void onStop() {
        Log.v(TAG, "onStop()...");
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver();
    }

    public static final int REQUEST_CODE_MANUAL_LOCATION =	1000;
    private void startManualLocation() {
        Log.v(TAG, "startManualLocation()..");
        Intent intent = new Intent();
        intent.setClass(this, LocationByManual.class);

        startActivityForResult(intent, REQUEST_CODE_MANUAL_LOCATION); 
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
        case REQUEST_CODE_MANUAL_LOCATION:
            if (resultCode == Activity.RESULT_OK) {
                Bundle bundle = data.getExtras();
                current = bundle.getString("province", "广东省");
            } else {
                // Try to set as last location??
                current = "山东省";
                Log.e(TAG, "Must check here, not set province by user!!!");
            }
            //getActionBar().setTitle(current);
            locator.setManualLocation(current);
            break;
        default:
            break;
        }
    }

    private void initTabHost() {
        res = this.getResources();

        th = (TabHost) findViewById(R.id.tabhost1);
        tw = (TabWidget) findViewById(android.R.id.tabs);
        th.setup();
        prepareTabs();
        th.setCurrentTab(0);
        tw.setStripEnabled(false);
        tw.setDividerDrawable(null);

        th.setOnTabChangedListener(new OnTabChangeListener() {
            @Override
            public void onTabChanged(String tabId) {
                Log.v(TAG, "onTabChanged()... tabId=\"" + tabId + "\"");

                /*if (!tabId.equals(getString(R.string.tab3))) {
                    getActionBar().show();
                } else {
                    Log.v(TAG, "message_fragment="+getFragmentManager().findFragmentByTag("message_fragment")
                            +", chat_fragment="+getFragmentManager().findFragmentByTag("chat_fragment"));
                    if (getFragmentManager().findFragmentByTag("message_fragment") == null) {
                        getActionBar().hide();
                    }
                }*/
            }
        });

        tw.getChildTabViewAt(0).setOnTouchListener(new View.OnTouchListener() {
            private boolean mDoubleTap = false;
            private boolean mFirstTouch = false;
            private long mLastTouchUpTime = 0;
            
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                final int action = event.getActionMasked();

                if (th.getCurrentTab() != 0) {
                    return false;
                }
                
                if (action == MotionEvent.ACTION_DOWN) {
                    // Detect double tap and inform the Editor.
                    if (mFirstTouch && (SystemClock.uptimeMillis() - mLastTouchUpTime) <=
                            ViewConfiguration.getDoubleTapTimeout()) {
                        mDoubleTap = true;
                        mFirstTouch = false;

                        //Intent intent = new Intent();
                        //intent.setClass(LightMsgActivity.this, MallActivity.class);
                        //startActivity(intent);
                        chatWithSelf();
                    } else {
                        mDoubleTap = false;
                        mFirstTouch = true;
                    }
                }

                if (action == MotionEvent.ACTION_UP) {
                    mLastTouchUpTime = SystemClock.uptimeMillis();
                }
                
                return false;
            }
        });

        updateMessageUnreadCount();
    }

    protected void prepareTabs() {
        Log.v(TAG, "prepareTabs()... ");

        mbvUnreadCount = new BadgeView(this.getApplicationContext());
        int size = (int)res.getDimension(R.dimen.unread_cnt_ball_size);
        mbvUnreadCount.setHeight(size);
        mbvUnreadCount.setWidth(size);
        float textSize = res.getDimension(R.dimen.unread_cnt_text_size);
        mbvUnreadCount.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
        mbvUnreadCount.setGravity(Gravity.CENTER);
        mbvUnreadCount.setMaxLines(1);

        Log.v(TAG, "prepareTabs(), size:" +res.getDimension(R.dimen.unread_cnt_ball_size)
                +", pSize:"+res.getDimensionPixelSize(R.dimen.unread_cnt_ball_size)
                +", textSize:"+res.getDimension(R.dimen.unread_cnt_text_size)
                +", pTextSize:"+res.getDimensionPixelSize(R.dimen.unread_cnt_text_size));

        setTab(getString(R.string.tab1),                // title
                R.drawable.lightmsg_bottom_1,           // icon
                -1,//R.drawable.lightmsg_bottom_bg,     // background
                mbvUnreadCount,                         // unread msg count
                R.id.tab1);                             // intent

        setTab(getString(R.string.tab2),
                R.drawable.lightmsg_bottom_2,
                -1,//R.drawable.lightmsg_bottom_bg,
                null,
                R.id.tab2);

        setTab(getString(R.string.tab3),
                R.drawable.lightmsg_bottom_3,
                -1,//R.drawable.lightmsg_bottom_bg,
                null,
                R.id.tab3);

        setTab(getString(R.string.tab4),
                R.drawable.lightmsg_bottom_4,
                -1,//R.drawable.lightmsg_bottom_bg,
                null,
                R.id.tab4);
    }

    private void setTab(String title, int icon, int bg, View bv, int id) {
        Log.v(TAG, "setTab()... TAB <"+title+">");

        //Set text view
        View tabItem = getLayoutInflater().inflate(R.layout.tab_item, null);
        TextView tv = (TextView) tabItem.findViewById(R.id.tab_item_tv);
        ImageView iv = (ImageView) tabItem.findViewById(R.id.tab_item_iv);
        tv.setPadding(0,0,0,0);
        tv.setText(title);
        //tv.setBackgroundResource(bg);
        //tv.setCompoundDrawablesWithIntrinsicBounds(0, icon, 0, 0);
        iv.setImageResource(icon);
        if (bg != -1) {
            iv.setBackgroundResource(bg);
        }

        //Configure for unread count indicator.
        if (bv != null) {
            BadgeView bv1 = (BadgeView)bv;
            bv1.setTarget(this.getApplicationContext(), iv);
            bv1.setBadgePosition(BadgeView.POSITION_TOP_RIGHT);
            //bv1.setBadgeMargin(0);
            //bv1.setBadgeTopMargin(BadgeView.dpToPx(this, 1));
            bv1.setBadgeRightMargin(res.getDimensionPixelSize(R.dimen.unread_cnt_ball_margin_right));
        }

        //Set tab spec, and add into TabHost.
        TabSpec tabSpec = th.newTabSpec(title); //Id is equal to <title>.
        tabSpec.setIndicator(tabItem); //Set TextView as indicator.
        tabSpec.setContent(id); //Set Intent for action.
        th.addTab(tabSpec);
    }

    //public RadioButton getCheckedRadioButton() {
    //return (RadioButton) findViewById(mRadioGroup.getCheckedRadioButtonId());
    //}

    private class MessageReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.v(TAG, "onReceive()... "+intent.getAction());
            if (CoreService.ACTION_RECEIVE_NEW_MSG.equals(intent.getAction())) {
                Log.v(TAG, "onReceive(), >>ACTION_RECEIVE_NEW_MSG");
                updateMessageUnreadCount();
            } else if (CoreService.ACTION_MSG_STATE_CHANGE.equals(intent.getAction())) {
                Log.v(TAG, "onReceive(), >>ACTION_MSG_STATE_CHANGE");
                updateMessageUnreadCount();
                //Fetch data when fresh...
                
            } else {

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

    private void updateMessageUnreadCount() {
        if (xs == null) return;
        
        //Get unread count, then set here...
        int cnt_single = xs.getConvTotalUnread(this);
        int cnt_group = xs.getGroupConvTotalUnread(this);
        int cnt = cnt_single + cnt_group;
        String cntText;
        Log.v(TAG, "updateMessageUnreadCount(), " + cnt_single + "+" + cnt_group);
        
        //cnt = 100;
        if (cnt > 0) {
            if (cnt > 99) {
                cntText = res.getString(R.string.unread_cnt_more_than_99);
            } else {
                cntText = String.valueOf(cnt);
            }
            mbvUnreadCount.setText(cntText);
            mbvUnreadCount.show();
        } else {
            mbvUnreadCount.setText("");
            mbvUnreadCount.hide();
        }
    }

    private void startChatWith(String user, String name) {
        Log.v(TAG, "startChatWith()... user="+user+", name="+name);

        Intent intent = new Intent();
        intent.setClass(this, ChatThread.class);
        intent.putExtra("user", user);
        intent.putExtra("name", name);

        startActivity(intent);
    }
    
    private void chatWithSelf() {
        Account account = xs.getAccount();
        if (account != null) {
            startChatWith(account.account, account.nick+" ("+getString(R.string.me)+")");
        }
    }
}
