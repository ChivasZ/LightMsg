package com.lightmsg.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class MainReceiver extends BroadcastReceiver {
    private static final String TAG = MainReceiver.class.getSimpleName();

    public MainReceiver() {
        Log.v(TAG, "MainReceiver created.");
    }
    
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.v(TAG, "onReceive(), action=" + intent.getAction());

        /* System State Change: Auto-start after boot completed */
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            Intent intentSvc = new Intent(context, CoreService.class);
            context.startService(intentSvc);
        }
    }
}
