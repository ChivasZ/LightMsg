package com.lightmsg.activity.msg;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
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
import com.lightmsg.service.CoreService;
import com.lightmsg.service.CoreService.Account;
import com.lightmsg.util.CountDownTimer;
import com.lightmsg.util.Locator;


public class LightMsgActivity extends Activity {
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
        super.onCreate(savedInstanceState);
        app = (LightMsg)getApplication();
        xs = app.xs;

        setContentView(R.layout.lightmsg_layout);
        //Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        //setSupportActionBar(toolbar);

        initTabHost();
        //initLocator();
        
        if (xs != null) {
            //if () //Preference
            xs.updateRoster();
            xs.setReceiveFile();
        }

        //getActionBar().setDisplayHomeAsUpEnabled(true);
        //getActionBar().setDisplayHomeAsUpEnabled(false);
        //getActionBar().setDisplayOptions(0, ActionBar.DISPLAY_SHOW_HOME|ActionBar.DISPLAY_USE_LOGO);

        /*cdtLocation = new CountDownTimer(10000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                Log.v(TAG, "cdtLocation.onTick(), millisUntilFinished="+millisUntilFinished);
                String locating = LightMsgActivity.this.getResources().getString(R.string.locating);
                if ((millisUntilFinished/1000)%4 == 3) {
                    getActionBar().setTitle(locating+"");
                } else if ((millisUntilFinished/1000)%4 == 2) {
                    getActionBar().setTitle(locating+".");
                } else if ((millisUntilFinished/1000)%4 == 1) {
                    getActionBar().setTitle(locating+"..");
                } else if ((millisUntilFinished/1000)%4 == 0) {
                    getActionBar().setTitle(locating+"...");
                }

                current = locator.getCurrentLocation();
                manual = locator.getManualLocation();
                if (current != null && !current.isEmpty()
                        || manual != null && !manual.isEmpty()) {
                    if (current != null && !current.isEmpty())
                        getActionBar().setTitle(current);
                    else if (manual != null && !manual.isEmpty())
                        getActionBar().setTitle(manual);

                    Log.d(TAG, "current="+current+", manual="+manual);
                    cdtLocation.cancel();
                    locator.removeLocationUpdates();
                } else {
                    Log.d(TAG, "Still not get location!!");
                }
            }
            @Override
            public void onFinish() {
                Log.v(TAG, "cdtLocation.onFinish()...");
                locator.removeLocationUpdates();
                startManualLocation();
            }
        }.start();*/

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
        case R.id.home:
            return true;
        //case R.id.contact_us:
            //return true;
            //xs.chatWith("test1", "nihao~");
            /*new AsyncTask<Object, Object, Object>() {
                @Override
                protected Object doInBackground(Object... paramVarArgs) {
                    // TODO Auto-generated method stub
                    Log.d(TAG, "JSON: "+HttpLoader.doGet("http://10.0.2.2:8080/json/justResp.jsp"));
                    return null;
                }

            }.execute(null, null);*/

            /*Log.d(TAG, "Upload >>>PRE");
            new AsyncTask<Object, Object, Object>() {
                @Override
                protected Object doInBackground(Object... paramVarArgs) {
                    // TODO Auto-generated method stub
                    Log.d(TAG, "Upload >>>");
                    HttpRequester.uploadFile(new File("/sdcard/Download/ADVERTISEMENT_ORIG_PIC_2.jpg"));

                    return null;
                }

            }.execute(null, null);
            return true;*/
        //case R.id.weather:
            //xs.chatInRoom("Shandong", "nimenhao.");
            //setUserInfo("test1", "newpwddd", "123456", "test_123", "q.h.zhang@samsung.com", 
            //		"test2:test3", "test", "/sdcard/test.png");
            //xs.setProfiles();

            //Intent intent = new Intent(this, AccountProfiles.class);
            //startActivity(intent);

            //File file = new File("/sdcard/Download/ADVERTISEMENT_ORIG_PIC_2.jpg");
            //xs.sendFile("test2", "TestIt", file);
            /*new Thread() {
                public void run() {
                    Log.i(TAG, "sendFile(), Start...");
                    try {
                        File file = new File("/sdcard/Download/ADVERTISEMENT_ORIG_PIC_2.jpg");
                        //File file = new File("/data/com.lightmsg/1.gif");
                        xs.sendFile("test2", "TestIt", file);
                    } catch (Exception e) {
                        // Ignore
                    }
                }
            }.start();*/
            //return true;
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
                Log.v(TAG, "onTabChanged()... tabId=\""+tabId+"\"");

                /*if (!tabId.equals(getString(R.string.tab3))) {
                    getActionBar().show();
                } else {
                    Log.d(TAG, "message_fragment="+getFragmentManager().findFragmentByTag("message_fragment")
                            +", chat_fragment="+getFragmentManager().findFragmentByTag("chat_fragment"));
                    if (getFragmentManager().findFragmentByTag("message_fragment") == null) {
                        getActionBar().hide();
                    }
                }*/
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
                R.drawable.lightmsg_bottom_1,            // icon
                R.drawable.lightmsg_bottom_bg,            // background
                mbvUnreadCount,                            // unread msg count
                R.id.tab1);								// intent

        setTab(getString(R.string.tab2),
                R.drawable.lightmsg_bottom_2,
                R.drawable.lightmsg_bottom_bg,
                null,
                R.id.tab2);

        setTab(getString(R.string.tab3),
                R.drawable.lightmsg_bottom_3,
                R.drawable.lightmsg_bottom_bg,
                null,
                R.id.tab3);

        setTab(getString(R.string.tab4),
                R.drawable.lightmsg_bottom_4,
                R.drawable.lightmsg_bottom_bg,
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
        Log.v(TAG, "updateMessageUnreadCount(), "+cnt_single+"+"+cnt_group);
        
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

        /*MessageReceiveService.setBadge(this, cnt);*/
    }
}
