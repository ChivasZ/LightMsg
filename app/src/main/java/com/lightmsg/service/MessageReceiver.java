package com.lightmsg.service;

import com.lightmsg.LightMsg;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class MessageReceiver extends BroadcastReceiver {
    private static final String TAG = "LightMsg/" + MessageReceiver.class.getSimpleName();
    private LightMsg app = null;
    private CoreService xs = null;
    
    public MessageReceiver() {
        Log.v(TAG, "MessageReceiver created.");
    }
    
    @Override
    public void onReceive(Context context, Intent intent) {

        String action = intent.getAction();
        int flags = intent.getFlags();
        Log.v(TAG, "Received, action=" + action + ", flags=" + flags);
        
        intent.setClass(context, MessageReceiveService.class);
        context.startService(intent);
    }
}
