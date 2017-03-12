package com.lightmsg.service;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.telephony.TelephonyManager;
import android.telephony.cdma.CdmaCellLocation;
import android.telephony.gsm.GsmCellLocation;
import android.text.TextUtils;
import android.util.Log;

import com.lightmsg.activity.ChatMsgEntry;
import com.lightmsg.activity.ConvEntry;
import com.lightmsg.activity.PyParser;
import com.lightmsg.activity.RostEntry;
import com.lightmsg.provider.LmMessageProvider;
import com.lightmsg.provider.LmProvider;
import com.lightmsg.util.EmojiUtil;
import com.lightmsg.util.Locator;
import com.lightmsg.util.MediaFile;
import com.lightmsg.util.SqliteWrapper;
import com.lightmsg.util.TimeUtils;
import com.lightmsg.xm.XmppManager;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
//import org.jivesoftware.smack.RosterEntry;

public class CoreService extends Service {
    private static final String TAG = CoreService.class.getSimpleName();

    // Binder given to clients
    private final IBinder xsBinder = new XmppServiceBinder();

    public NotificationService ns;

    private ExecutorService exeSvcXmpp;
    private ExecutorService exeSvcBg;
    //private SharedPreferences sharedPrefsReg;
    //private SharedPreferences sharedPrefsLogin;
    private Account account;
    private XmppManager xm;
    private ClientStream cs;
    private BlockingClientStream bcs;
    private String mServerHost;
    private String mServerName;

    private static String 		SC_SERVER_DOMAIN_TEST = "109.131.18.203";
    private static String 		SC_SERVER_DOMAIN_Q_REL = "app.lightmsg.com";
    private static String 		SC_SERVER_DOMAIN_Q_ROOT = "lightmsg.com";
    private static String 		SC_SERVER_DOMAIN = SC_SERVER_DOMAIN_Q_REL;
    private static int 			SC_SERVER_PORT = 9913;

    private static String 		SC_STREAM_SERVER_DOMAIN = SC_SERVER_DOMAIN;
    private static int 			SC_STREAM_SERVER_PORT = 9922;
    public static final String	SC_RESOURCE_ID = "ANDROID";

    // Actions for Sc itself.
    public static final String ACTION_CONNECT_OK		= "com.LightMsg.CoreService.action.CONNECT_OK";
    public static final String ACTION_CONNECT_FAIL		= "com.LightMsg.CoreService.action.CONNECT_FAIL";
    public static final String ACTION_REGISTER_OK 		= "com.LightMsg.CoreService.action.REGISTER_OK";
    public static final String ACTION_REGISTER_FAIL		= "com.LightMsg.CoreService.action.REGISTER_FAIL";
    public static final String ACTION_LOGIN_OK 			= "com.LightMsg.CoreService.action.LOGIN_OK";
    public static final String ACTION_LOGIN_FAIL		= "com.LightMsg.CoreService.action.LOGIN_FAIL";
    public static final String ACTION_PROFILE_OK 		= "com.LightMsg.CoreService.action.PROFILE_OK";
    public static final String ACTION_PROFILE_FAIL		= "com.LightMsg.CoreService.action.PROFILE_FAIL";
    public static final String ACTION_GET_ROSTER_OK 	= "com.LightMsg.CoreService.action.GET_ROSTER_OK";
    public static final String ACTION_GET_ROSTER_FAIL	= "com.LightMsg.CoreService.action.GET_ROSTER_FAIL";
    public static final String ACTION_RECEIVE_NEW_MSG	= "com.LightMsg.CoreService.action.RECEIVE_NEW_MSG";
    public static final String ACTION_READY_SEND_FILE	= "com.LightMsg.CoreService.action.READY_SEND_FILE";
    public static final String ACTION_MSG_STATE_CHANGE	= "com.LightMsg.CoreService.action.MESSAGE_STATE_CHANGE";

    public static final String ACTION_MSG_SEND_FILE		= "com.LightMsg.CoreService.action.MESSAGE_SND_FILE";
    public static final String ACTION_MSG_RECEIVE_FILE	= "com.LightMsg.CoreService.action.MESSAGE_RCV_FILE";

    public void updateBadge() {
        //Get unread count, then set here...
        int cnt_single = getConvTotalUnread(this);
        int cnt_group = getGroupConvTotalUnread(this);
        int cnt = cnt_single + cnt_group;

        Log.v(TAG, "updateBadge(), "+cnt_single+"+"+cnt_group);
        MessageReceiveService.setBadge(this, cnt);
    }
    
    private static final int XMPP_EVENT_RECONNECT = 1;
    private static final int XMPP_EVENT_HEARTBEAT = 2;
    private Handler mXmppHandler;
    private class XmppServiceHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case XMPP_EVENT_RECONNECT:
                    Log.d(TAG, " XMPP_EVENT_RECONNECT");
                    testAndSetReconnectionThread();
                    break;

                case XMPP_EVENT_HEARTBEAT:
                    Log.d(TAG, " XMPP_EVENT_HEARTBEAT");
                    xm.sendHeartBeat(hb.genCellLocChildXml());
                    break;
            }
        }
    };

    private String mCurrentThreadUser;
    public void setCurrentThreadUser(String user) {
        mCurrentThreadUser = user;
    }

    public String getCurrentThreadUser() {
        return mCurrentThreadUser;
    }

    private MessageReceiver messageReceiver;
    public static final int NOTIFICATION_MESSAGE_ID = 0x990;
    public void registerMessageReceiver() {
        Log.v(TAG, "registerMessageReceiver()...");
        IntentFilter messageFilter = new IntentFilter();
        messageFilter.addAction(CoreService.ACTION_RECEIVE_NEW_MSG);
        messageFilter.addAction(CoreService.ACTION_MSG_STATE_CHANGE);

        if (messageReceiver == null) {
            messageReceiver = new MessageReceiver();
        }
        registerReceiver(messageReceiver, messageFilter);
    }

    public void unregisterMessageReceiver() {
        Log.v(TAG, "unregisterMessageReceiver()...");
        if (messageReceiver != null) {
            unregisterReceiver(messageReceiver);
            messageReceiver = null;
        }
    }
    
    public void cancelNotificationMessage() {
        NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        int id = CoreService.NOTIFICATION_MESSAGE_ID;
        nm.cancel(id);
    }

    /* Xmpp Operation for Client -START- */
    private String regUser;
    private String regName;
    private String regPwd;
    public String getRegUser() {
        return regUser;
    }
    public String getRegName() {
        return regName;
    }
    public String getRegPwd() {
        return regPwd;
    }
    public void register(Bundle bundle) {
        Log.v(TAG, "registerUser()...");
        regUser = bundle.getString("user");
        regName = bundle.getString("name");
        regPwd = bundle.getString("pwd");
        
        xm.registerUser();
    }

    public void connect() {
        Log.v(TAG, "connect()...");
        xm.connect();
    }

    public void login() {
        Log.e(TAG, "login()...");
        xm.login();
    }

    public void logout() {
        Log.e(TAG, "logout()");
        xm.logout();

        Account account = getAccount();
        if (account != null) {
            account.is_login = 0;
            account.pwd = "";
            setAccount(account);
        }
    }

    public boolean isOnceLogined() {
        // Only for UI DBG
        //if (true) {
        //    return true;
        //}
        
        //return xm.isOnceLogined();
        Account account = getAccount();

        boolean login = account != null && account.is_login == 1;

        Log.e(TAG, "isOnceLogined(), "+login);
        return login;
    }

    public boolean isNetworkActive() {
        NetworkInfo info = cm.getActiveNetworkInfo();

        boolean active = (info != null && info.isConnected());

        Log.e(TAG, "isNetworkActive(), "+active+", "+info);
        return active;
    }

    public void testIfReconnct() {
        testIfReconnct(false);
    }

    public void testIfReconnct(boolean delay) {
        if (!isOnceLogined() || !isNetworkActive()) {
            return;
        }

        long delayMillis = 0;
        if (delay) {
            delayMillis = 1000;
        }
        Message msg = mXmppHandler.obtainMessage(XMPP_EVENT_RECONNECT);
        mXmppHandler.sendMessageDelayed(msg, delayMillis);
    }

    public void testAndSetReconnectionThread() {
        if (isNetworkActive() && isOnceLogined()) {
            // Directly reconnect if network available.
            Log.v(TAG, "xm.startReconnectionThread()...");
            xm.startReconnectionThread();
        } else {
            // Waiting for network to be available.
            Log.v(TAG, "xm.stopReconnectionThread(), Waiting for network to be available...");
            xm.stopReconnectionThread();
        }
    }
    
    public void addFriend(String uid, String name) {
        xm.addFriendInRoster(uid, name);
    }

    private String changeUser;
    private String changeName;
    private String changeCurrentPwd;
    private String changeNewPwd;
    public String getChangeUser() {
        return changeUser;
    }
    public String getChangeName() {
        return changeName;
    }
    public String getChangeCurrentPwd() {
        return changeCurrentPwd;
    }
    public String getChangeNewPwd() {
        return changeNewPwd;
    }
    public void setProfiles(Bundle bundle) {
        Log.v(TAG, "setProfiles()...");
        changeUser = bundle.getString("user");
        changeName = bundle.getString("name");
        changeCurrentPwd = bundle.getString("curpwd");
        changeNewPwd = bundle.getString("newpwd");
        
        xm.setProfiles();
    }

    public void updateRoster() {
        Log.v(TAG, "getRoster()...");
        xm.updateRoster();
    }

    public class Account {
        public String nick;
        public String account;
        public String gender;
        public String pwd;
        public int is_login;
        public int rem_pwd;
        public int auto_log;
        public String strPortrait;
        public Bitmap portrait;
    }

    public Account getAccount() {
        Account account = null;

        ContentResolver resolver = this.getContentResolver();
        Uri contentUri = Uri.parse("content://"+ LmProvider.AUTHORITY+"/"+ LmProvider.TABLE_ACCOUNT);
        String[] projection = {
                LmProvider._ID,
                LmProvider.ACCOUNT,
                LmProvider.NICK,
                LmProvider.GENDER,
                LmProvider.PWD,
                LmProvider.LOGIN,
                LmProvider.REM_PWD,
                LmProvider.AUTO_LOG,
                LmProvider.PORTRAIT,
        };

        Cursor c = SqliteWrapper.query(this, resolver, contentUri, projection, null, null, null);

        try {
            if (c != null && c.getCount() > 0) {
                account = new Account();
                
                if (c.moveToFirst()) {
                    String acc = c.getString(c.getColumnIndexOrThrow(LmProvider.ACCOUNT));
                    String nick = c.getString(c.getColumnIndexOrThrow(LmProvider.NICK));
                    String gender = c.getString(c.getColumnIndexOrThrow(LmProvider.GENDER));
                    String pwd = c.getString(c.getColumnIndexOrThrow(LmProvider.PWD));
                    int login = c.getInt(c.getColumnIndexOrThrow(LmProvider.LOGIN));
                    int rem = c.getInt(c.getColumnIndexOrThrow(LmProvider.REM_PWD));
                    int auto = c.getInt(c.getColumnIndexOrThrow(LmProvider.AUTO_LOG));
                    String portrait = c.getString(c.getColumnIndexOrThrow(LmProvider.PORTRAIT));

                    account.account = acc;
                    account.nick = nick;
                    account.gender = gender;
                    account.pwd = pwd;
                    account.is_login = login;
                    account.rem_pwd = rem;
                    account.auto_log = auto;
                    account.strPortrait = portrait;
                    Log.v(TAG, " getAccount(), Successfully!");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally{
            if (c != null) {
                c.close();
            }
        }

        return account;
    }
    
    public void setAccount(String acc, String nick, String gen, 
                String pwd, int login, int rem, int auto, String portrait) {
        Account account = new Account();
        
        account.account = acc;
        account.nick = nick;
        account.gender = gen;
        account.pwd = pwd;
        account.is_login = login;
        account.rem_pwd = rem;
        account.auto_log = auto;
        account.strPortrait = portrait;
        
        setAccount(account);
    }

    public void setAccount(Account account) {
        ContentResolver resolver = this.getContentResolver();
        Uri contentUri = Uri.parse("content://"+ LmProvider.AUTHORITY+"/"+ LmProvider.TABLE_ACCOUNT);

        ContentValues values = new ContentValues();
        values.put(LmProvider.ACCOUNT, account.account);
        values.put(LmProvider.NICK, account.nick);
        values.put(LmProvider.GENDER, account.gender);
        values.put(LmProvider.PWD, account.pwd);
        values.put(LmProvider.LOGIN, account.is_login);
        values.put(LmProvider.REM_PWD, account.rem_pwd);
        values.put(LmProvider.AUTO_LOG, account.auto_log);
        values.put(LmProvider.PORTRAIT, account.strPortrait);

        try {
            SqliteWrapper.insert(this, resolver, contentUri, values);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*public Collection<RosterEntry> getServerRosterEntries() {
        Log.v(TAG, "getRosterEntries()...");
        return xm.getRosterEntries();
    }*/

    ArrayList<RostEntry> rostList = new ArrayList<RostEntry>();
    public ArrayList<RostEntry> getRosterArrayList(Context context) {
        Log.v(TAG, "getRosterArrayList()...");

        ContentResolver resolver = context.getContentResolver();
        Uri contentUri = Uri.parse("content://" + LmProvider.AUTHORITY + "/" + LmProvider.TABLE_ROSTER);
        String[] projection = {
                LmProvider._ID,
                LmProvider.UID,
                LmProvider.NAME,
                LmProvider.NICK,
                LmProvider.GENDER,
                LmProvider.REGION,
                LmProvider.GROUP,
                LmProvider.THREAD_ID,
                LmProvider.PORTRAIT,
                LmProvider.STATE,
                LmProvider.SORT //Checking Pinyin for Chinese-Character
        };

        /*
         * Order By Options.
         */
        String ASC = "ASC";
        String DES = "DESC";
        String sortBy = LmProvider.SORT+" "+ASC; //Checking Pinyin for Chinese-Character

        Cursor c = SqliteWrapper.query(context, resolver, contentUri, projection, null, null, sortBy);

        rostList.clear(); // Clear older roster list firstly
        try {
            if (c != null && c.getCount() > 0) {
                if (c.moveToFirst()) {
                    do {
                        boolean bFound = false;
                        RostEntry entry = new RostEntry();

                        String uid = c.getString(c.getColumnIndexOrThrow(LmProvider.UID));
                        String name = c.getString(c.getColumnIndexOrThrow(LmProvider.NAME));
                        String nick = c.getString(c.getColumnIndexOrThrow(LmProvider.NICK));
                        String gender = c.getString(c.getColumnIndexOrThrow(LmProvider.GENDER));
                        String region = c.getString(c.getColumnIndexOrThrow(LmProvider.REGION));
                        String group = c.getString(c.getColumnIndexOrThrow(LmProvider.GROUP));
                        String threadId = c.getString(c.getColumnIndexOrThrow(LmProvider.THREAD_ID));
                        String portrait = c.getString(c.getColumnIndexOrThrow(LmProvider.PORTRAIT));
                        int state = c.getInt(c.getColumnIndexOrThrow(LmProvider.STATE));
                        String sortName = c.getString(c.getColumnIndexOrThrow(LmProvider.SORT)); //Checking Pinyin for Chinese-Character.

                        entry.setUser(uid);
                        entry.setName(name);
                        entry.setName(name);
                        entry.setNick(nick);
                        entry.setGender(gender);
                        entry.setRegion(region);
                        entry.setGroup(group);
                        entry.setThreadId(threadId);
                        entry.setPortrait(portrait);
                        entry.setState(state);
                        entry.setSortName(sortName); //Checking Pinyin for Chinese-Character.

                        Log.v(TAG, "getRosterArrayList(), rostList.add("+entry+")");
                        rostList.add(entry);
                    } while(c.moveToNext());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally{
            if (c != null) {
                c.close();
            }
        }

        return rostList;
    }

    public String[] getRosterEntriesString(Context context) {
        Log.v(TAG, "getRosterEntriesString()...");
        int i = 0;
        ArrayList<RostEntry> rostList = getRosterArrayList(context);
        String[] entryStrings = new String[rostList.size()];
        for (RostEntry entry : rostList) {
            Log.v(TAG, "getRoster(), entry:"+entry);
            entryStrings[i++] = entry.toString();
        }
        return entryStrings;
    }

    public void insertOrUpdateRoster(String uid, String name, String nick, 
            String gender, String region, String group, 
            String threadId, String portrait, int state) {
        Log.v(TAG, "insertOrUpdateRoster()...");
        Log.v(TAG, "insertOrUpdateRoster(), uid=" + uid + ", " + name + ", " + nick + ", " + gender + ", " + region + ", " + group + ", " + threadId + ", " + portrait + ", " + state);
        uid = getUidFromJid(uid);
        ContentResolver resolver = getContentResolver();
        Uri contentUri = Uri.parse("content://"+ LmProvider.AUTHORITY+"/"+ LmProvider.TABLE_ROSTER+ LmProvider.UNDERLINE+ LmProvider.UID);

        contentUri = Uri.withAppendedPath(contentUri, uid);
        ContentValues values = new ContentValues();

        //Get original data firstly...
        int cnt = 0;
        cnt = getUserUnread(this, uid);
        Log.v(TAG, "updateThread(), Unread Count original: " + cnt);

        values.put(LmProvider.UID, uid);
        values.put(LmProvider.NAME, name);
        values.put(LmProvider.NICK, nick);
        values.put(LmProvider.GENDER, gender);
        values.put(LmProvider.REGION, region);
        values.put(LmProvider.GROUP, group);
        values.put(LmProvider.THREAD_ID, threadId);
        values.put(LmProvider.PORTRAIT, portrait);
        values.put(LmProvider.STATE, state);

        //Checking Pinyin for Chinese-Character. -S-
        String sortName;
        String FORMAT = "^[a-z,A-Z].*$";
        if (name.matches(FORMAT)) {
            sortName = name;
        } else {
            sortName = PyParser.getInstance().getSpelling(name);

            if (!sortName.matches(FORMAT)) {
                sortName = "#";
            }
        }
        values.put(LmProvider.SORT, sortName.toUpperCase());
        //Checking Pinyin for Chinese-Character. -E-

        try {
            SqliteWrapper.insert(this, resolver, contentUri, values);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private ArrayList<ConvEntry> convList;
    public void setGroupConvArrayList(ArrayList<ConvEntry> list) {
        Log.v(TAG, "setGroupConvArrayList()...");
        convList = list;
    }

    public ArrayList<ConvEntry> getGroupConvArrayList(Context context) {
        Log.v(TAG, "getGroupConvArrayList()...");
        //ArrayList<ConvEntry> convList1 = new ArrayList<ConvEntry>();

        ContentResolver resolver = context.getContentResolver();
        Uri contentUri = Uri.parse("content://"+ LmProvider.AUTHORITY+"/"+ LmProvider.TABLE_GROUP_THREAD);
        String[] projection = {
                LmProvider._ID,
                LmProvider.GROUP,
                LmProvider.DATE,
                LmProvider.UID,
                LmProvider.JID,
                LmProvider.NAME,
                LmProvider.THREAD_SNIPPET,
                LmProvider.UNREAD_COUNT,
                LmProvider.ERROR
        };

        /*
         * Order By Options.
         */
        String ASC = "ASC";
        String DES = "DESC";
        String sortBy = LmProvider.DATE+" "+ASC;

        Cursor c = SqliteWrapper.query(context, resolver, contentUri, projection, null, null, sortBy);
        try {
            if (c != null && c.getCount() > 0) {
                if (c.moveToFirst()) {
                    do {
                        boolean bFound = false;
                        ConvEntry entry = null;

                        // Search the entity by group id.
                        String group = c.getString(c.getColumnIndexOrThrow(LmProvider.GROUP));
                        Iterator<ConvEntry> iter = convList.iterator();
                        while (iter.hasNext()) {
                            entry = iter.next();
                            if (group.equals(entry.getUser())) { //Not add current province.
                                bFound = true;
                                break;
                            }
                        }
                        // Create one if not exist.
                        if (!bFound) {
                            entry = new ConvEntry();
                            convList.add(0, entry);
                        } else {
                            convList.remove(entry);
                            convList.add(0, entry);
                        }

                        entry.setThreadId(group);
                        entry.setUser(group);

                        // Not set name anymore.
                        //String name = c.getString(c.getColumnIndexOrThrow(LmProvider.NAME));
                        //entry.setName(name);

                        long date = c.getLong(c.getColumnIndexOrThrow(LmProvider.DATE));
                        entry.setDate(TimeUtils.getCurString(date));
                        TimeUtils.getCurString();
                        //Log.v(TAG, "getConvArrayList()...date="+TimeUtils.getCurString(date));

                        // uid is set to group id
                        String uid = c.getString(c.getColumnIndexOrThrow(LmProvider.UID));
                        //entry.setUser(uid);

                        String snippet = c.getString(c.getColumnIndexOrThrow(LmProvider.THREAD_SNIPPET));
                        entry.setSnippet(uid + ": " + snippet);

                        int count = c.getInt(c.getColumnIndexOrThrow(LmProvider.UNREAD_COUNT));
                        entry.setUnreadCount(count);
                        Log.v(TAG, "getConvArrayList()...unread_count="+count);

                        int e = c.getInt(c.getColumnIndexOrThrow(LmProvider.ERROR));
                        if (e == 0) {
                            entry.setError(false);
                        } else {
                            entry.setError(true);
                        }

                        //convList1.add(entry);
                    } while(c.moveToNext());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally{
            if (c != null) {
                c.close();
            }
        }

        /*Object[] clArray = convList1.toArray();
        for (i = 0; i < clArray.length; i++) {
            convList.add(i, (ConvEntry)clArray[i]);
        }*/

        return convList;
    }

    public ArrayList<ConvEntry> getConvArrayList(Context context) {
        Log.v(TAG, "getConvArrayList()...");
        ArrayList<ConvEntry> convList = new ArrayList<ConvEntry>();

        /*
        //TEST...
        ConvEntry entry1 = new ConvEntry("重阳", "hiphop99", "今天晚上要去跳舞，一会儿见！", "2013/11/26");
        convList.add(entry1);
        ConvEntry entry2 = new ConvEntry("罗伊", "roy", "See you 8 o'clock.", "2013/11/26");
        convList.add(entry2);
        ConvEntry entry3 = new ConvEntry("凯文", "kevin", "When will you come? Tell me if you finish it.", "2013/11/24");
        convList.add(entry3);
        ConvEntry entry4 = new ConvEntry("小明", "ming", "帮我拿一下快递，我下午去上班。", "2013/11/20");
        convList.add(entry4);
        ConvEntry entry0 = new ConvEntry("GD", "广东省", "guangdong", "讨论组", "2015/07");
        convList.add(entry0);*/

        ContentResolver resolver = context.getContentResolver();
        Uri contentUri = Uri.parse("content://"+ LmProvider.AUTHORITY+"/"+ LmProvider.TABLE_THREAD);
        String[] projection = {
                LmProvider._ID,
                LmProvider.THREAD_ID,
                LmProvider.DATE,
                LmProvider.UID,
                LmProvider.JID,
                LmProvider.NAME,
                LmProvider.THREAD_SNIPPET,
                LmProvider.UNREAD_COUNT,
                LmProvider.ERROR
        };

        /*
         * Order By Options.
         */
        String ASC = "ASC";
        String DES = "DESC";
        String sortBy = LmProvider.DATE+" "+DES;

        Cursor c = SqliteWrapper.query(context, resolver, contentUri, projection, null, null, sortBy);
        try {
            if (c != null && c.getCount() > 0) {
                if (c.moveToFirst()) {
                    do {
                        ConvEntry entry = new ConvEntry();
                        String threadId = c.getString(c.getColumnIndexOrThrow(LmProvider.THREAD_ID));
                        entry.setThreadId(threadId);
                        String name = c.getString(c.getColumnIndexOrThrow(LmProvider.NAME));
                        entry.setName(name);
                        //Log.v(TAG, "getConvArrayList()...name="+name);
                        long date = c.getLong(c.getColumnIndexOrThrow(LmProvider.DATE));
                        entry.setDate(TimeUtils.getCurString(date));
                        TimeUtils.getCurString();
                        //Log.v(TAG, "getConvArrayList()...date="+TimeUtils.getCurString(date));
                        String snippet = c.getString(c.getColumnIndexOrThrow(LmProvider.THREAD_SNIPPET));
                        entry.setSnippet(snippet);
                        //Log.v(TAG, "getConvArrayList()...snippet="+snippet);
                        String uid = c.getString(c.getColumnIndexOrThrow(LmProvider.UID));
                        entry.setUser(uid);
                        //Log.v(TAG, "getConvArrayList()...uid="+uid);
                        int count = c.getInt(c.getColumnIndexOrThrow(LmProvider.UNREAD_COUNT));
                        entry.setUnreadCount(count);
                        Log.v(TAG, "getConvArrayList()...unread_count="+count);
                        int e = c.getInt(c.getColumnIndexOrThrow(LmProvider.ERROR));
                        if (e == 0) {
                            entry.setError(false);
                        } else {
                            entry.setError(true);
                        }

                        convList.add(entry);
                    } while(c.moveToNext());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally{
            if (c != null) {
                c.close();
            }
        }

        return convList;
    }

    /*public void updateConvArrayList(Context context, ArrayList<ConvEntry> list) {
        Log.v(TAG, "updateConvArrayList()...");

        ContentResolver resolver = context.getContentResolver();
        Uri contentUri = Uri.parse("content://"+LmProvider.AUTHORITY+"/"+LmProvider.TABLE_THREAD);
        String[] projection = {
                LmProvider._ID,
                LmProvider.THREAD_ID,
                LmProvider.DATE,
                LmProvider.UID,
                LmProvider.JID,
                LmProvider.NAME,
                LmProvider.THREAD_SNIPPET,
                LmProvider.UNREAD_COUNT,
                LmProvider.ERROR
            };

        Cursor c = SqliteWrapper.query(context, resolver, contentUri, projection, null, null, null);
        try {
            int index = 0;
            if (c != null && c.getCount() > 0) {
                if (c.moveToFirst()) {
                    do {
                        ConvEntry entry = list.get(index);
                        String threadId = c.getString(c.getColumnIndexOrThrow(LmProvider.THREAD_ID));
                        entry.setThreadId(threadId);
                        String name = c.getString(c.getColumnIndexOrThrow(LmProvider.NAME));
                        entry.setName(name);
                        //Log.v(TAG, "updateConvArrayList()...name="+name);
                        String date = c.getLong(c.getColumnIndexOrThrow(LmProvider.DATE));
                        entry.setDate(date);
                        //Log.v(TAG, "updateConvArrayList()...date="+date);
                        String snippet = c.getString(c.getColumnIndexOrThrow(LmProvider.THREAD_SNIPPET));
                        entry.setSnippet(snippet);
                        //Log.v(TAG, "updateConvArrayList()...snippet="+snippet);
                        String uid = c.getString(c.getColumnIndexOrThrow(LmProvider.UID));
                        entry.setUser(uid);
                        //Log.v(TAG, "updateConvArrayList()...uid="+uid);
                        int count = c.getInt(c.getColumnIndexOrThrow(LmProvider.UNREAD_COUNT));
                        entry.setUnreadCount(count);
                        Log.v(TAG, "updateConvArrayList()...unread_count="+count);
                        int e = c.getInt(c.getColumnIndexOrThrow(LmProvider.ERROR));
                        if (e == 0) {
                            entry.setError(false);
                        } else {
                            entry.setError(true);
                        }

                        list.set(index, entry);
                        index++;
                    } while(c.moveToNext());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally{
            if (c != null) {
                c.close();
            }
        }
    }*/

    public int getGroupConvTotalUnread(Context context) {
        Log.v(TAG, "getGroupConvTotalUnread()...");
        int convTotalUnread = 0;

        ContentResolver resolver = context.getContentResolver();
        Uri contentUri = Uri.parse("content://"+ LmProvider.AUTHORITY+"/"+ LmProvider.TABLE_GROUP_THREAD);
        String[] projection = {
                LmProvider.UNREAD_COUNT
        };

        Cursor c = SqliteWrapper.query(context, resolver, contentUri, projection, null, null, null);
        try {
            if (c != null && c.getCount() > 0) {
                if (c.moveToFirst()) {
                    do {
                        convTotalUnread += c.getInt(c.getColumnIndexOrThrow(LmProvider.UNREAD_COUNT));
                    } while(c.moveToNext());
                }
            }
        }finally{
            if (c != null) {
                c.close();
            }
        }

        return convTotalUnread;
    }

    public int getConvTotalUnread(Context context) {
        Log.v(TAG, "getConvTotalUnread()...");
        int convTotalUnread = 0;

        ContentResolver resolver = context.getContentResolver();
        Uri contentUri = Uri.parse("content://"+ LmProvider.AUTHORITY+"/"+ LmProvider.TABLE_THREAD);
        String[] projection = {
                LmProvider.UNREAD_COUNT
        };

        Cursor c = SqliteWrapper.query(context, resolver, contentUri, projection, null, null, null);
        try {
            if (c != null && c.getCount() > 0) {
                if (c.moveToFirst()) {
                    do {
                        convTotalUnread += c.getInt(c.getColumnIndexOrThrow(LmProvider.UNREAD_COUNT));
                    } while(c.moveToNext());
                }
            }
        }finally{
            if (c != null) {
                c.close();
            }
        }

        return convTotalUnread;
    }

    public int getGroupUnread(Context context, String group) {
        Log.v(TAG, "getUserUnread()...");
        int groupUnread = 0;

        ContentResolver resolver = context.getContentResolver();
        Uri contentUri = Uri.parse("content://" + LmProvider.AUTHORITY + "/" + LmProvider.TABLE_GROUP_THREAD);
        String[] projection = {
                LmProvider.UNREAD_COUNT
        };
        String selection = LmProvider.GROUP+"=\""+group+"\"";

        Cursor c = SqliteWrapper.query(context, resolver, contentUri, projection, selection, null, null);
        try {
            if (c != null && c.getCount() > 0) {
                if (c.moveToFirst()) {
                    do {
                        groupUnread += c.getInt(c.getColumnIndexOrThrow(LmProvider.UNREAD_COUNT));
                    } while(c.moveToNext());
                }
            }
        }finally{
            if (c != null) {
                c.close();
            }
        }

        return groupUnread;
    }

    public int getUserUnread(Context context, String uid) {
        Log.v(TAG, "getUserUnread()...");
        int userUnread = 0;

        ContentResolver resolver = context.getContentResolver();
        Uri contentUri = Uri.parse("content://"+ LmProvider.AUTHORITY+"/"+ LmProvider.TABLE_THREAD);
        String[] projection = {
                LmProvider.UNREAD_COUNT
        };
        String selection = LmProvider.UID+"=\""+uid+"\"";

        Cursor c = SqliteWrapper.query(context, resolver, contentUri, projection, selection, null, null);
        try {
            if (c != null && c.getCount() > 0) {
                if (c.moveToFirst()) {
                    do {
                        userUnread += c.getInt(c.getColumnIndexOrThrow(LmProvider.UNREAD_COUNT));
                    } while(c.moveToNext());
                }
            }
        }finally{
            if (c != null) {
                c.close();
            }
        }

        return userUnread;
    }

    public int setGroupRead(Context context, String group) {
        Log.v(TAG, "setUserRead()...");

        ContentResolver resolver = context.getContentResolver();
        Uri uri = Uri.parse("content://" + LmProvider.AUTHORITY + "/" + LmProvider.TABLE_GROUP_THREAD + LmProvider.UNDERLINE + LmProvider.GROUP + "/" + group);
        //String selection = LmProvider.UID+"=\""+uid+"\"";

        ContentValues values = new ContentValues();
        values.put(LmProvider.UNREAD_COUNT, 0);
        int ret = SqliteWrapper.update(context, resolver, uri, values, null, null);

        Intent intent = new Intent();
        intent.setAction(CoreService.ACTION_MSG_STATE_CHANGE);
        sendBroadcast(intent);

        updateBadge();
        return ret;
    }

    public int setUserRead(Context context, String uid) {
        Log.v(TAG, "setUserRead()...");

        if (getUserUnread(context, uid) <= 0) {
            return -1;
        }

        ContentResolver resolver = context.getContentResolver();
        Uri uri = Uri.parse("content://" + LmProvider.AUTHORITY + "/" + LmProvider.TABLE_THREAD + LmProvider.UNDERLINE + LmProvider.UID + "/" + uid);
        //String selection = LmProvider.UID+"=\""+uid+"\"";

        ContentValues values = new ContentValues();
        values.put(LmProvider.UNREAD_COUNT, 0);
        int ret = SqliteWrapper.update(context, resolver, uri, values, null, null);

        Intent intent = new Intent();
        intent.setAction(CoreService.ACTION_MSG_STATE_CHANGE);
        sendBroadcast(intent);

        updateBadge();
        return ret;
    }

    public ChatMsgEntry getLastestChatMsg(String uid) {
        Log.v(TAG, "getLastestChatMsg(), uid=" + uid);
        
        ContentResolver resolver = getContentResolver();
        Uri contentUri = Uri.parse("content://" + LmMessageProvider.AUTHORITY + "/" + LmMessageProvider.UID + "/" + uid);
        
        /*
         * Order By Options.
         */
        String ASC = "ASC";
        String DES = "DESC";
        String sortBy = LmProvider.DATE+" "+DES;

        Cursor c = SqliteWrapper.query(this, resolver, contentUri, null, null, null, sortBy, 0, 1);
        ChatMsgEntry entry = null;
        try {
            if (c != null && c.getCount() == 1) {
                if (c.moveToFirst()) {
                    entry = new ChatMsgEntry();
                    long date;

                    if (isMsgMo(c)) {
                        entry.setFrom(false);
                    } else {
                        entry.setFrom(true);
                    }

                    if (!isFileMsg(c)) {
                        entry.setFileType(false);
                        entry.setMsg(c.getString(c.getColumnIndexOrThrow(LmMessageProvider.CONTENT)));
                        entry.setFile("");
                    } else {
                        entry.setFileType(true);
                        entry.setMsg("");
                        entry.setFile(c.getString(c.getColumnIndexOrThrow(LmMessageProvider.CONTENT)));
                    }

                    date = c.getLong(c.getColumnIndexOrThrow(LmMessageProvider.DATE));
                    entry.setDate(TimeUtils.getCurString(date));
                    //TimeUtils.getCurString();
                    entry.setDateLong(date);
                    //Not need name info.
                    String name = guaranteeUid(c.getString(c.getColumnIndexOrThrow(LmMessageProvider.JID_FROM)));
                    //Log.v(TAG, "getChatArrayList(), name="+name);
                    entry.setName(name);

                    entry.setMsgId(c.getString(c.getColumnIndexOrThrow(LmMessageProvider.MSG_ID)));
                }
            }
        }finally{
            if (c != null) {
                c.close();
            }
        }

        return entry;
    }

    public List<ChatMsgEntry> getChatArrayList(String uid) {
        return getChatArrayList(uid, 0);
    }

    public List<ChatMsgEntry> getChatArrayList(String uid, int cnt) {
        List<ChatMsgEntry> chatList = new ArrayList<>();
        getChatArrayList(chatList, uid, 0, cnt);
        
        return chatList;
    }

    public int getChatArrayList(List<ChatMsgEntry> chatList, String uid, int offset, int cnt) {
        Log.v(TAG, "getChatArrayList(), uid=" + uid+", offset="+offset+", cnt="+cnt+", chatList="+chatList);
        int realCnt = 0;
        
        if (chatList == null) {
            chatList = new ArrayList<>();
        }

        //TEST...
        /*ChatMsgEntry entry1 = new ChatMsgEntry("重阳", "今天晚上要去跳舞，一会儿见！", "2013/11/26", false);
        chatList.add(entry1);
        ChatMsgEntry entry2 = new ChatMsgEntry("罗伊", "See you 8 o'clock.", "2013/11/26", true);
        chatList.add(entry2);
        ChatMsgEntry entry3 = new ChatMsgEntry("凯文", "When will you come? Tell me if you finish it.", "2013/11/24", false);
        chatList.add(entry3);
        ChatMsgEntry entry4 = new ChatMsgEntry("小明", "帮我拿一下快递，我下午去上班。", "2013/11/20", true);
        chatList.add(entry4);
        ChatMsgEntry entry5 = new ChatMsgEntry("小红", "如果有你，云会很淡，水会很清，空气也很稀薄。如果没有你，小鸟不会歌唱，花儿不再开放，世界再也没有阳光。", "2013/11/20", true);
        chatList.add(entry5);
        ChatMsgEntry entry6 = new ChatMsgEntry("小明", "哦。", "2013/11/20", false);
        chatList.add(entry6);*/


        //String uid = getUidFromJid(jid);
        ContentResolver resolver = getContentResolver();
        Uri contentUri = Uri.parse("content://" + LmMessageProvider.AUTHORITY + "/" + LmMessageProvider.UID + "/" + uid);
        //Uri contentUri = Uri.parse("content://"+LmProvider.AUTHORITY+"/"+LmMessageProvider.UID_FROM+"/"+uid);
        //contentUri = ContentUris.withAppendedId(contentUri, 0);

        /*
         * Order By Options.
         */
        String ASC = "ASC";
        String DES = "DESC";
        String sortBy = LmProvider.DATE+" "+DES;
        
        Cursor c = SqliteWrapper.query(this, resolver, contentUri, null, null, null, sortBy, offset, cnt);
        try {
            if (c != null && c.getCount() > 0) {
                realCnt = c.getCount();
                if (c.moveToFirst()) {
                    do {
                        ChatMsgEntry entry = new ChatMsgEntry();
                        long date;

                        if (isMsgMo(c)) {
                            entry.setFrom(false);
                        } else {
                            entry.setFrom(true);
                        }

                        if (!isFileMsg(c)) {
                            entry.setFileType(false);
                            entry.setMsg(c.getString(c.getColumnIndexOrThrow(LmMessageProvider.CONTENT)));
                            entry.setFile("");
                        } else {
                            entry.setFileType(true);
                            entry.setMsg("");
                            entry.setFile(c.getString(c.getColumnIndexOrThrow(LmMessageProvider.CONTENT)));
                        }

                        date = c.getLong(c.getColumnIndexOrThrow(LmMessageProvider.DATE));
                        entry.setDate(TimeUtils.getCurString(date));
                        //TimeUtils.getCurString();
                        entry.setDateLong(date);
                        //Not need name info.
                        String name = guaranteeUid(c.getString(c.getColumnIndexOrThrow(LmMessageProvider.JID_FROM)));
                        //Log.v(TAG, "getChatArrayList(), name="+name);
                        entry.setName(name);

                        entry.setMsgId(c.getString(c.getColumnIndexOrThrow(LmMessageProvider.MSG_ID)));

                        chatList.add(entry);
                    } while(c.moveToNext());
                }
            } else {
                realCnt = 0;
            }
        }finally{
            if (c != null) {
                c.close();
            }
        }

        Log.v(TAG, "getChatArrayList(), realCnt=" + realCnt);
        return realCnt;
    }
    
    public void deleteMsg(String uid, String msgId, long date) {
        ContentResolver resolver = getContentResolver();
        Uri contentUri = Uri.parse("content://" + LmMessageProvider.AUTHORITY + "/" + LmMessageProvider.UID + "/" + uid);
        

        int ret = SqliteWrapper.delete(this, resolver, contentUri,
                LmMessageProvider.UID + "=? AND "
                        + LmMessageProvider.MSG_ID + "=? AND "
                        + LmMessageProvider.DATE + "=?", new String[]{uid, msgId, String.valueOf(date)});
        Log.v(TAG, "deleteMsg(), ret=" + ret);

        ChatMsgEntry entry = getLastestChatMsg(uid);
        updateThread(makeJid(uid), null, null, entry.getMsg(), entry.getDateLong(), !entry.isFrom());
        
        Intent intent = new Intent();
        intent.setAction(CoreService.ACTION_MSG_STATE_CHANGE);
        sendBroadcast(intent);
    }

    public void chatWith(String jid, String msg) {
        Log.v(TAG, "chatWith()...");
        xm.chatWith(jid, null, null, msg, null);
    }

    public void chatInRoom(final String room, final String msg) {
        Log.v(TAG, "chatInRoom()...room=" + room + ", msg=" + msg);
        new Thread(new Runnable() {
            @Override
            public	void run() {
                xm.chatInRoom(room, msg);
            }
        }, "chatInRoom").start();
    }

    public void sendMsg(String uid, String msg) {
        Log.v(TAG, "sendMsg()...uid=" + uid);
        xm.chatWith(makeJid(uid), null, null, msg, null);
    }

    // Check MessageProvider's query result.
    public boolean isMsgMo(Cursor c) {
        /*String jidFrom = c.getString(c.getColumnIndexOrThrow(LmMessageProvider.JID_FROM));
        String jidTo = c.getString(c.getColumnIndexOrThrow(LmMessageProvider.JID_TO));
        String uid = c.getString(c.getColumnIndexOrThrow(LmMessageProvider.UID));
        if (uid.equals(this.getUidFromJid(jidFrom))) {
            Log.e(TAG, "isMo()...false, uid="+uid+", jidFrom="+jidFrom+", jidTo="+jidTo);
            return false;
        } else if (uid.equals(this.getUidFromJid(jidTo))) {
            Log.e(TAG, "isMo()...true, uid="+uid+", jidFrom="+jidFrom+", jidTo="+jidTo);
            return true;
        } else {
            Log.e(TAG, "isMo()... ERROR!!!!! uid="+uid+", jidFrom="+jidFrom+", jidTo="+jidTo);
        }*/

        int mo = c.getInt(c.getColumnIndexOrThrow(LmMessageProvider.MO));
        if (mo == 1) {
            return true;
        }
        return false;
    }

    public boolean isFileMsg(Cursor c) {
        String fm = c.getString(c.getColumnIndexOrThrow(LmMessageProvider.MSG_TYPE));
        if ("file".equals(fm)) {
            return true;
        }
        return false;
    }

    public String getServerHost() {
        String host;
        if (xm.isConnected()) {
            host = mServerHost;//"109.131.13.132";
        } else {
            Log.e(TAG, "Get server host when not connected");
            //mServerHost = "";
            host = "";
        }
        Log.v(TAG, "getServerHost()...host="+host);
        return host;
    }

    public void setServerHost(String host) {
        mServerHost = host;
    }

    public String getServerName() {
        String name;
        if (xm.isConnected()/* && xm.isAuthenticated()*/) {
            name = mServerName;//"hwqm913d200058v";
        } else {
            Log.e(TAG, "Get server name when not authoried.");
            //mServerName = "";
            name = "";
        }
        Log.v(TAG, "getServerName()...name="+name);
        return name;
    }

    public void setServerName(String name) {
        mServerName = name;
    }

    public String getJidSuffix() {
        String jidSuf;
        if (xm.isConnected() && xm.isAuthenticated()) {
            String jid = xm.getJidAfterLogin();//"hwqm913d200058v";
            jidSuf = getServerFromJid(jid);
        } else {
            Log.e(TAG, "Get server name when not authoried.");
            //mServerName = "";
            jidSuf = "";
        }
        Log.v(TAG, "getJidSuffix()...jidSuf="+jidSuf);
        return jidSuf;
    }

    public String makeJid(String uid) {
        return (uid+"@"+getJidSuffix());
    }

    public void updateThread(String jid,
                             String threadId, String name, String content,
                             long date,
                             boolean isMo) {
        Log.v(TAG, "updateThread()...");
        Log.v(TAG, "updateThread(), jid=" + jid + ", " + threadId + ", " + name + ", " + content);
        String uid = getUidFromJid(jid);
        ContentResolver resolver = getContentResolver();
        Uri contentUri = Uri.parse("content://"+ LmProvider.AUTHORITY+"/"+ LmProvider.TABLE_THREAD+ LmProvider.UNDERLINE+ LmProvider.UID);

        contentUri = Uri.withAppendedPath(contentUri, uid);
        ContentValues values = new ContentValues();

        //Get original data firstly...
        int cnt = 0;
        cnt = getUserUnread(this, uid);
        Log.v(TAG, "updateThread(), Unread Count original: " + cnt);

        if (!TextUtils.isEmpty(name))
            values.put(LmProvider.NAME, name);
        if (!TextUtils.isEmpty(threadId))
            values.put(LmProvider.THREAD_ID, threadId);
        if (!TextUtils.isEmpty(uid))
            values.put(LmProvider.UID, uid);
        if (!TextUtils.isEmpty(jid))
            values.put(LmProvider.JID, jid);
        if (date == -1)
            values.put(LmProvider.DATE, TimeUtils.getCurMillis());
        else
            values.put(LmProvider.DATE, date);
        if (!TextUtils.isEmpty(content))
            values.put(LmProvider.THREAD_SNIPPET, content);
        if (!isMo)
            values.put(LmProvider.UNREAD_COUNT, ++cnt);

        try {
            SqliteWrapper.insert(this, resolver, contentUri, values);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /* Normal message incoming.
    <message 
        id="zdmyq-7" 
        to="test1@hwqm913d200058v" 
        from="test2@hwqm913d200058v/SC" 
        type="chat">
        <body>[笑脸]</body>
        <thread>V3YB33</thread>
    </message>
     */
    public void storeRawMessage(String msgId, String jidTo, String jidFrom, 
            String type, String content, String threadId, String xml, boolean isMo) {
        Log.v(TAG, "storeMessage()...");
        ContentResolver resolver = getContentResolver();

        Uri contentUri = Uri.parse("content://"+ LmMessageProvider.AUTHORITY+"/"+ LmMessageProvider.TABLE_MSG);
        //contentUri = Uri.withAppendedPath(contentUri, uid);
        ContentValues values = new ContentValues();
        values.put(LmMessageProvider.MSG_ID, msgId);
        if (isMo) {
            String uid = getUidFromJid(jidTo);
            values.put(LmMessageProvider.UID, uid);
            values.put(LmMessageProvider.MO, 1);
        } else {
            String uid = getUidFromJid(jidFrom);
            values.put(LmMessageProvider.UID, uid);
            values.put(LmMessageProvider.MO, 0);
        }

        values.put(LmMessageProvider.DATE, TimeUtils.getCurMillis());
        values.put(LmMessageProvider.JID_TO, jidTo);
        values.put(LmMessageProvider.JID_FROM, jidFrom);

        values.put(LmMessageProvider.MSG_TYPE, type);
        values.put(LmMessageProvider.CONTENT, content);
        values.put(LmMessageProvider.XML, xml);
        values.put(LmMessageProvider.READ, "0");
        values.put(LmMessageProvider.SENT, "0");
        values.put(LmMessageProvider.ERROR, "0");

        try {
            SqliteWrapper.insert(this, resolver, contentUri, values);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void updateFileMessage(String tid, String file, boolean isMo) {
        Log.v(TAG, "updateFileMessage(), tid=" + tid + ", file=" + file + ", Mo=" + isMo);
        ContentResolver resolver = getContentResolver();

        Uri contentUri = Uri.parse("content://"+ LmMessageProvider.AUTHORITY+"/"+ LmMessageProvider.TABLE_MSG);
        String[] projection = {
                LmMessageProvider.MO,
                LmMessageProvider.MSG_ID
        };
        String selection = LmMessageProvider.MO+"="+(isMo?1:0)
                    +" AND "
                    + LmMessageProvider.MSG_ID+"=\""+tid+"\"";

        Log.v(TAG, "updateFileMessage(), selection=" + selection);
        Cursor c = SqliteWrapper.query(this, resolver, contentUri, projection, selection, null, null);
        try {
            if (c != null && c.getCount() > 0) {
                Log.v(TAG, "updateFileMessage(), matched " + c.getCount());
                if (c.moveToFirst()) {
                }
            }
        }finally{
            if (c != null) {
                c.close();
            }
        }

        ContentValues values = new ContentValues();
        values.put(LmMessageProvider.CONTENT, file);

        try {
            SqliteWrapper.update(this, resolver, contentUri, values, selection, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void storeGroupThread(String jid, 
            String threadId, String name, String content, boolean isMo) {
        Log.v(TAG, "storeGroupThread(), jid=" + jid + ", " + threadId + ", " + name + ", " + content);
        String uid = getUidFromJid(jid);
        ContentResolver resolver = getContentResolver();
        Uri contentUri = Uri.parse("content://"+ LmProvider.AUTHORITY+"/"+ LmProvider.TABLE_GROUP_THREAD+ LmProvider.UNDERLINE+ LmProvider.GROUP);

        contentUri = Uri.withAppendedPath(contentUri, threadId);
        ContentValues values = new ContentValues();

        //Get original data firstly...
        int cnt = 0;
        cnt = getGroupUnread(this, threadId);
        Log.v(TAG, "updateThread(), Unread Count original: " + cnt);

        values.put(LmProvider.NAME, name);
        values.put(LmProvider.GROUP, threadId);
        values.put(LmProvider.UID, uid);
        values.put(LmProvider.JID, jid);
        values.put(LmProvider.DATE, TimeUtils.getCurMillis());
        values.put(LmProvider.THREAD_SNIPPET, content);
        if (!isMo)
            values.put(LmProvider.UNREAD_COUNT, ++cnt);

        try {
            SqliteWrapper.insert(this, resolver, contentUri, values);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /* Normal message incoming.
    <message 
        id="zdmyq-7" 
        to="test1@hwqm913d200058v" 
        from="test2@hwqm913d200058v/SC" 
        type="chat">
        <body>[笑脸]</body>
        <thread>V3YB33</thread>
    </message>
     */
    public void storeGroupRawMessage(String msgId, String jidTo, String jidFrom, 
            String type, String content, String threadId, String xml, boolean isMo) {
        Log.v(TAG, "storeMessage()...");
        ContentResolver resolver = getContentResolver();

        Uri contentUri = Uri.parse("content://"+ LmMessageProvider.AUTHORITY+"/"+ LmMessageProvider.TABLE_MSG);
        //contentUri = Uri.withAppendedPath(contentUri, uid);
        ContentValues values = new ContentValues();
        values.put(LmMessageProvider.MSG_ID, msgId);


        String uid = getUidFromJid(jidTo);
        values.put(LmMessageProvider.UID, uid);
        values.put(LmMessageProvider.DATE, TimeUtils.getCurMillis());

        if (isMo) {
            values.put(LmMessageProvider.MO, 1);
        } else {
            values.put(LmMessageProvider.MO, 0);
        }

        values.put(LmMessageProvider.JID_TO, jidTo);
        values.put(LmMessageProvider.JID_FROM, jidFrom);

        values.put(LmMessageProvider.MSG_TYPE, type);
        values.put(LmMessageProvider.CONTENT, content);
        values.put(LmMessageProvider.XML, xml);
        values.put(LmMessageProvider.READ, "0");
        values.put(LmMessageProvider.SENT, "0");
        values.put(LmMessageProvider.ERROR, "0");

        try {
            SqliteWrapper.insert(this, resolver, contentUri, values);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getUidFromJid(String jid) {
        //Log.v(TAG, "getUidFromJid()...");
        String[] uids = TextUtils.split(jid, "@");

        return uids[0];
    }

    // FORMAT: {username@server/resource}
    public String getServerFromJid(String jid) {
        Log.v(TAG, "getServerFromJid()...");
        int start = jid.indexOf('@');
        start++;
        int end = jid.indexOf('/', jid.indexOf('@'));
        if (end == -1) {
            end = jid.length()-1;
        }
        //end--;
        String server = jid.substring(start, end);

        Log.v(TAG, "getServerFromJid()...server=" + server);
        return server;
    }

    public String getClientFromJid(String jid) {
        Log.v(TAG, "getClientFromJid()...");
        int start = jid.indexOf('/');
        start++;
        String clientType = jid.substring(start);

        Log.v(TAG, "getClientFromJid()...clientType=" + clientType);
        return clientType;
    }

    public boolean isScClient(String jid) {
        Log.v(TAG, "getClientFromJid()...");
        return (SC_RESOURCE_ID.equals(getClientFromJid(jid)));
    }

    public String guaranteeJidNoResource(String jid) {
        //Log.v(TAG, "GuaranteeJidNoResource()...jid="+jid);
        if (jid == null) return "";

        int c = jid.indexOf('/');
        if (c == -1) {
            c = jid.length();
        }
        String newJid = jid.substring(0, c);

        //Log.v(TAG, "GuaranteeJidNoResource()...newJid="+newJid);
        return newJid;
    }

    public String guaranteeUid(String jid) {
        //Log.v(TAG, "GuaranteeUid()...jid="+jid);
        if (jid == null) {
            return null;
        }

        int c = jid.indexOf('@');
        if (c == -1) {
            c = jid.length();
        }
        String uid = jid.substring(0, c);

        //Log.v(TAG, "GuaranteeUid()...uid="+uid);
        return uid;
    }

    public static String getServer() {
        return SC_SERVER_DOMAIN;
    }

    public static int getPort() {
        return SC_SERVER_PORT;
    }

    public static String getStreamServer() {
        return SC_STREAM_SERVER_DOMAIN;
    }

    public static int getStreamPort() {
        return SC_STREAM_SERVER_PORT;
    }

    /* Xmpp Operation for Client -END- */

    public CoreService() {
        Log.v(TAG, "CoreService()...");
        exeSvcBg = Executors.newSingleThreadExecutor();
        exeSvcXmpp = Executors.newSingleThreadExecutor();
        xm = new XmppManager(CoreService.this);
        //cs = new ClientStream();

        mServerHost = "";
        mServerName = "";
    }

    public Future<?> submmitTaskInSingle(Runnable task) {
        Log.v(TAG, "submmitTaskInSingle(exeSvcBg), task=" + task.getClass().getSimpleName());

        Future<?> result = null;
        if (!exeSvcBg.isTerminated()
                && !exeSvcBg.isShutdown()
                && task != null) {
            result = exeSvcBg.submit(task);
        }
        return result;
    }

    /**
     * Submit all task in a single work thread
     * @param task
     * @return
     */
    public Future<Integer> submmitTaskInSingle(Callable task) {
        Log.v(TAG, "submmitTaskInSingle(exeSvcXmpp), task=" + task.getClass().getSimpleName());

        Future<Integer> result = null;
        if (!exeSvcXmpp.isTerminated()
                && !exeSvcXmpp.isShutdown()
                && task != null) {
            result = exeSvcXmpp.submit(task);
        }
        return result;
    }

    //public SharedPreferences getSharedPreferencesReg() {
    //	return sharedPrefsReg;
    //}

    //public SharedPreferences getSharedPreferencesLogin() {
    //	return sharedPrefsLogin;
    //}

    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    public class XmppServiceBinder extends Binder {
        public CoreService getService() {
            // Return this instance of CoreService,
            // so clients can call public methods directly.
            return CoreService.this;
        }
    }

    @Override
    public IBinder onBind(Intent arg0) {
        // TODO Auto-generated method stub
        Log.d(TAG, "onBind...");
        return xsBinder;
    }

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate()...");

        mXmppHandler = new XmppServiceHandler();

        //Initialize emoji resources.
        new Thread(new Runnable() {
            @Override
            public void run() {
                //EmojiUtil.getInstance().getFileText(getApplication());
                EmojiUtil.getInstance().getResText(getApplication());
            }
        }).start();

        try {
            initTelephonyStateManagers();
        } catch (SecurityException e) {
            e.printStackTrace();
        }

        //startNotificationService();

        XmppServiceReceiver.registerConnectionStateChange(this, this);

        if (isOnceLogined() && isNetworkActive()) {
            login();
        }

        startHeartBeats();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand()...");

        // If we get killed, after returning from here, restart
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.e(TAG, "onDestroy()...");

        //stopNotificationService();
        exeSvcBg.shutdown();
        exeSvcXmpp.shutdown();
        timer.cancel();
    }

    public void startNotificationService() {
        Thread serviceThread = new Thread(new Runnable() {
            @Override
            public void run() {
                Intent intent = NotificationService.getIntent();
                CoreService.this.startService(intent);
            }
        });
        serviceThread.start();
    }

    public void stopNotificationService() {
        Intent intent = NotificationService.getIntent();
        CoreService.this.stopService(intent);
    }

    private TelephonyManager 	tm;
    private WifiManager			wm;
    private ConnectivityManager	cm;
    private String 				deviceId;
    private int					phoneType = TelephonyManager.PHONE_TYPE_NONE;
    private int					networkType = TelephonyManager.NETWORK_TYPE_UNKNOWN;

    private SharedPreferences sharedPrefs;
    public static final String SHARED_PREFERENCE_NAME = "device_info";
    public void initTelephonyStateManagers() throws SecurityException {
        Log.v(TAG, "initTelephonyStateManagers()...");

        tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        wm = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        sharedPrefs = getSharedPreferences(SHARED_PREFERENCE_NAME,	Context.MODE_PRIVATE);

        //String androidId = System.getString(this.getContentResolver(), Settings.Secure.ANDROID_ID);

        if (false) {

        }
        // Get deviceId
        deviceId = tm.getDeviceId();
        Editor editor = sharedPrefs.edit();
        editor.putString(NotificationService.DEVICE_ID, deviceId);
        editor.commit();

        // If running on an emulator
        if (deviceId == null || deviceId.trim().length() == 0
                || deviceId.matches("0+")) {
            if (sharedPrefs.contains("EMULATOR_DEVICE_ID")) {
                deviceId = sharedPrefs.getString(NotificationService.EMULATOR_DEVICE_ID,
                        "");
            } else {
                deviceId = (new StringBuilder("EMU")).append(
                        (new Random(System.currentTimeMillis())).nextLong())
                        .toString();
                editor.putString(NotificationService.EMULATOR_DEVICE_ID, deviceId);
                editor.commit();
            }
        }
        Log.v(TAG, "deviceId=" + deviceId);

        // Get Phone Type
        phoneType = tm.getPhoneType();

        // Get Network Type
        networkType = tm.getNetworkType();
    }

    public String getDeviceId() {
        return deviceId;
    }

    public String getMessageGroupName() {
        try {
            return Locator.getInstance(this).getMessageGroupName();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return "TRASH";
    }

    public String getMessageGroupId() {
        try {
            return Locator.getInstance(this).getMessageGroupId();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return "TRASH";
    }

    public void sendFile(String uid, String msg, final File file) {
        PendingIntent pi = PendingIntent.getBroadcast(this, 0, 
                new Intent(ACTION_READY_SEND_FILE), PendingIntent.FLAG_CANCEL_CURRENT);
        final String tid = xm.chatWith(makeJid(uid), null, file, msg, pi);

        final BlockingClientStreamCallback cb = new  BlockingClientStreamCallback() {

            @Override
            public void start() {
                Intent intent = new Intent();
                intent.setAction(CoreService.ACTION_MSG_SEND_FILE);
                intent.putExtra("start", true);
                CoreService.this.sendBroadcast(intent);
            }

            @Override
            public void update(long percent) {
                Intent intent = new Intent();
                intent.setAction(CoreService.ACTION_MSG_SEND_FILE);
                intent.putExtra("percent", percent);
                CoreService.this.sendBroadcast(intent);
            }

            @Override
            public void finish(int ret, String tid, File nf) {
                if (ret == 0)
                    CoreService.this.updateFileMessage(tid, nf.getAbsolutePath(), true);

                Intent intent = new Intent();
                intent.setAction(CoreService.ACTION_MSG_SEND_FILE);
                intent.putExtra("finish", true);
                if (ret == 0) {
                    intent.putExtra("error", false);
                } else {
                    intent.putExtra("error", true);
                }
                CoreService.this.sendBroadcast(intent);
            }
        };
        registerReceiver(new BroadcastReceiver() {

            @Override
            public void onReceive(Context paramContext, Intent paramIntent) {
                Log.i(TAG, "onReceive(), intent=" + paramIntent);
                if (ACTION_READY_SEND_FILE.equals(paramIntent.getAction())) {
                    Log.v(TAG, "onReceive(), >>ACTION_READY_SEND_FILE");
                    //cs.send(tid, file);
                    BlockingClientStream.getInstance().send(tid, file, cb);
                }
            }
        }, new IntentFilter(ACTION_READY_SEND_FILE));

    }

    public void receiveFile(String tid, String filename, String size, String type, String hash) {
        BlockingClientStreamCallback cb = new  BlockingClientStreamCallback() {

            @Override
            public void start() {
                Intent intent = new Intent();
                intent.setAction(CoreService.ACTION_MSG_RECEIVE_FILE);
                intent.putExtra("start", true);
                CoreService.this.sendBroadcast(intent);
            }

            @Override
            public void update(long percent) {
                Intent intent = new Intent();
                intent.setAction(CoreService.ACTION_MSG_RECEIVE_FILE);
                intent.putExtra("percent", percent);
                CoreService.this.sendBroadcast(intent);
            }

            @Override
            public void finish(int ret, String tid, File nf) {
                if (ret == 0)
                    CoreService.this.updateFileMessage(tid, nf.getAbsolutePath(), false);

                Intent intent = new Intent();
                intent.setAction(CoreService.ACTION_MSG_RECEIVE_FILE);
                intent.putExtra("finish", true);
                if (ret == 0) {
                    intent.putExtra("error", false);
                } else {
                    intent.putExtra("error", true);
                }
                CoreService.this.sendBroadcast(intent);
            }
        };

        String nf = new Date().getTime()+"_"+hash+filename.substring(filename.lastIndexOf('.')-1);
        BlockingClientStream.getInstance().recv(tid, Integer.parseInt(size),
                new File(MediaFile.getCurrentFilePath(this)
                        +"//"+nf),
                cb);
    }

    public void setReceiveFile() {
        xm.setReceiveFile();
    }

    public void loadRoster() {
        xm.loadRoster();
    }

    public void setLoadRosterAtLogin(boolean load) {
        xm.setLoadRosterAtLogin(load);
    }

    Timer timer = new Timer("HeartBeat");
    HeartBeat hb = new HeartBeat();
    private void startHeartBeats() {
        timer.schedule(hb, 
                1000*60, 
                1000*60*5);
        //Delay 60s to execute, then every 5min successive execute.
    }

    private class HeartBeat extends TimerTask {

        @Override
        public void run() {
            // TODO Auto-generated method stub
            if (xm.isLogined()) {
                Log.v(TAG, "HeartBeat.run()...");
                Message message = new Message();
                message.what = XMPP_EVENT_HEARTBEAT;
                mXmppHandler.sendMessage(message);
            }
        }
        
        @Override
        public boolean cancel() {
            Log.v(TAG, "HeartBeat.cancel()...");
            return super.cancel();
        }
        
        @Override
        public long scheduledExecutionTime() {
            long time = super.scheduledExecutionTime();
            Log.v(TAG, "HeartBeat.scheduledExecutionTime(), " + time);
            return time;
        }

        // Running in MAIN thread.
        /*private final Handler mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                // TODO Auto-generated method stub
                super.handleMessage(msg);
                Log.v(TAG, " mHandler Thread:"+Thread.currentThread().getName());
                if(msg.what==1) {
                    Log.v(TAG, " sendHeartBeat()...");

                    xm.sendHeartBeat(genCellLocChildXml());
                }
            }

        };*/

        public String genCellLocChildXml() {
            StringBuilder buf = new StringBuilder();
            buf.append("<location>");
            //if (phoneType == TelephonyManager.PHONE_TYPE_CDMA) {
            if (tm.getPhoneType() == TelephonyManager.PHONE_TYPE_CDMA) {
                CdmaCellLocation ccl = null;
                try {
                    ccl = (CdmaCellLocation) tm.getCellLocation();
                } catch (SecurityException se) {
                    se.printStackTrace();
                }

                buf.append("<cdma>");
                if (ccl != null) {
                    buf.append("<sid>");
                    buf.append(ccl.getSystemId());
                    buf.append("</sid>");
                    buf.append("<nid>");
                    buf.append(ccl.getNetworkId());
                    buf.append("</nid>");
                } else {
                    buf.append("location_not_available");
                }
                buf.append("</cdma>");
                //} else if (phoneType == TelephonyManager.PHONE_TYPE_GSM) {
            } else if (tm.getPhoneType() == TelephonyManager.PHONE_TYPE_GSM) {
                GsmCellLocation gcl = null;
                try {
                    gcl = (GsmCellLocation) tm.getCellLocation();
                } catch (SecurityException se) {
                    se.printStackTrace();
                }

                buf.append("<gsm>");
                if (gcl != null) {
                    buf.append("<lac>");
                    buf.append(gcl.getLac());
                    buf.append("</lac>");
                    buf.append("<cid>");
                    buf.append(gcl.getCid());
                    buf.append("</cid>");
                } else {
                    buf.append("location_not_available");
                }
                buf.append("</gsm>");
            } else {
                buf.append("unknown_phone_type");
            }
            buf.append("</location>");
            return buf.toString();
        }
    }


}

//<iq type="error" id="lqms2-1" to="catination.com/b373e0a"><query xmlns="jabber:iq:auth"><username>test1</username><digest>1c8ef05ede7bbda143286b16fb73f556b741db57</digest><resource>shanghai</resource></query><error code="500" type="wait"><internal-server-error xmlns="urn:ietf:params:xml:ns:xmpp-stanzas"/></error></iq>