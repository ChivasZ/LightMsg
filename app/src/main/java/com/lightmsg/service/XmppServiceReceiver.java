package com.lightmsg.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

public class XmppServiceReceiver extends BroadcastReceiver {
    private static final String TAG = XmppServiceReceiver.class.getSimpleName();

    private static CoreService xs;

    /**
     * To monitor the data connection state @TelephonyIntents
     */
    public static final String ACTION_ANY_DATA_CONNECTION_STATE_CHANGED = "android.intent.action.ANY_DATA_STATE";
    public static final String STATE_KEY = "state";
    public static final String NETWORK_UNAVAILABLE_KEY = "networkUnvailable";
    public static final String DATA_STATE_CONNECTED = "CONNECTED";
    public static final String DATA_STATE_CONNECTING = "CONNECTING";
    public static final String DATA_STATE_DISCONNECTED = "DISCONNECTED";

    /**
     * To monitor the phone radio state @TelephonyIntents
     */
    public static final String ACTION_SERVICE_STATE_CHANGED = "android.intent.action.SERVICE_STATE";
    public static final String ACTION_RADIO_TECHNOLOGY_CHANGED = "android.intent.action.RADIO_TECHNOLOGY";
    public static final String ACTION_SIGNAL_STRENGTH_CHANGED = "android.intent.action.SIG_STR";

    /**
     * To monitor the connection state @ConnectivityManager
     */
    public static final String ACTION_CONNECTIVITY_CHANGE = "android.net.conn.CONNECTIVITY_CHANGE";
    public static final String ACTION_DATA_ACTIVITY_CHANGE = "android.net.conn.DATA_ACTIVITY_CHANGE";
    public static final String ACTION_INET_CONDITION = "android.net.conn.INET_CONDITION_ACTION";
    public static final String ACTION_TETHER_STATE_CHANGED = "android.net.conn.TETHER_STATE_CHANGED";

    private static XmppServiceReceiver xsr;
    public static XmppServiceReceiver getInstance() {
        if (xsr == null) {
            xsr = new XmppServiceReceiver();
        }
        return xsr;
    }

    private XmppServiceReceiver() {
        Log.v(TAG, "XmppServiceReceiver created.");
        //xsr = XmppServiceReceiver.this;
    }
    
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.v(TAG, "onReceive(), action=" + intent.getAction());

        /* Phone State Change */
        if (ACTION_ANY_DATA_CONNECTION_STATE_CHANGED.equals(intent.getAction())) {
            String state = intent.getStringExtra(STATE_KEY);
            //boolean unAvailable = intent.getBooleanExtra(NETWORK_UNAVAILABLE_KEY, true);
            Log.v(TAG, "onReceive(), state=" + state);
            if (DATA_STATE_CONNECTED.equals(state)) {
                // To connect
                //xs.testAndSetReconnectionThread();
            } else if (DATA_STATE_CONNECTING.equals(state)) {
                // Waiting
            } else if (DATA_STATE_DISCONNECTED.equals(state)) {
                // Finish all the connections
            }
        } else if (ACTION_SERVICE_STATE_CHANGED.equals(intent.getAction())) {
            //ServiceState state = ServiceState.newFromBundle(intent.getExtras());
            Log.v(TAG, "onReceive(), state=" + intent.getExtras());
        } else if (ACTION_RADIO_TECHNOLOGY_CHANGED.equals(intent.getAction())) {
            Log.v(TAG, "onReceive(), state=" + intent.getExtras());
        } else if (ACTION_SIGNAL_STRENGTH_CHANGED.equals(intent.getAction())) {
            Log.v(TAG, "onReceive(), state=" + intent.getExtras());
        }
        /* ConnectivityManager State Change */
        else if (ACTION_CONNECTIVITY_CHANGE.equals(intent.getAction())) {
            Log.v(TAG, "onReceive(), state=" + intent.getExtras());
            NetworkInfo info = //(NetworkInfo) intent.getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO);
                ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
            Log.v(TAG, "onReceive(), info=" + info);
            if (info == null) {
                Log.v(TAG, "onReceive(), CONNECTION NOT AVAILABLE!!!");
            } else if (info.isConnected() && info.isAvailable()) {
                Log.v(TAG, "onReceive(), CONNECTION AVAILABLE!!!");
            }
            xs.testIfReconnct(true);
        } else if (ACTION_INET_CONDITION.equals(intent.getAction())) {
            Log.v(TAG, "onReceive(), state=" + intent.getExtras());
            Log.v(TAG, "onReceive(), inetCondition=" + intent.getStringExtra("inetCondition"));
        } else if (ACTION_TETHER_STATE_CHANGED.equals(intent.getAction())) {
            Log.v(TAG, "onReceive(), state=" + intent.getExtras());
            Log.v(TAG, "onReceive(), availableArray={" + intent.getStringArrayExtra("availableArray")+"}");
            Log.v(TAG, "onReceive(), activeArray={" + intent.getStringArrayExtra("activeArray")+"}");
            Log.v(TAG, "onReceive(), erroredArray={" + intent.getStringArrayExtra("erroredArray")+"}");
        } else if (ACTION_DATA_ACTIVITY_CHANGE.equals(intent.getAction())) {
            Log.v(TAG, "onReceive(), state=" + intent.getExtras());
            Log.v(TAG, "onReceive(), deviceType=" + intent.getIntExtra("deviceType", 0));
            Log.v(TAG, "onReceive(), isActive=" + intent.getBooleanExtra("isActive", false));
            Log.v(TAG, "onReceive(), tsNanos=" + intent.getLongExtra("tsNanos", 0));
        } else {
            Log.v(TAG, "onReceive(), else, state=" + intent.getExtras());
        }
    }

    public static void registerConnectionStateChange(Context context, CoreService xs) {
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_ANY_DATA_CONNECTION_STATE_CHANGED);
        //filter.addAction(ACTION_SERVICE_STATE_CHANGED);
        //filter.addAction(ACTION_RADIO_TECHNOLOGY_CHANGED);
        //filter.addAction(ACTION_SIGNAL_STRENGTH_CHANGED);

        filter.addAction(ACTION_CONNECTIVITY_CHANGE);
        //filter.addAction(ACTION_DATA_ACTIVITY_CHANGE);
        //filter.addAction(ACTION_INET_CONDITION);
        //filter.addAction(ACTION_TETHER_STATE_CHANGED);

        context.registerReceiver(XmppServiceReceiver.getInstance(), filter);
        XmppServiceReceiver.xs = xs;
    }

}
