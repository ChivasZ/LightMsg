/*
 * Copyright (C) 2010 Moduad Co., Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.lightmsg.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.lightmsg.R;
import com.lightmsg.activity.msgdesign.chat.ChatThread;

import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import static com.lightmsg.service.MessageReceiveService.setBadge;
import static org.jivesoftware.smack.packet.PrivacyItem.Type.group;

/**
 * Service that continues to run in background and respond to the push 
 * notification events from the server. This should be registered as service
 * in AndroidManifest.xml. 
 * 
 */
public class NotificationService extends Service {

    private static final String TAG = NotificationService.class.getSimpleName();

    public static final String SERVICE_NAME = "com.lightmsg.NotificationService";

    private TelephonyManager telephonyManager;
    
    private NotificationManager notificationManager;

    private BroadcastReceiver notificationReceiver;

    private BroadcastReceiver connectivityReceiver;

    private PhoneStateListener phoneStateListener;

    private ExecutorService executorService;

    private TaskSubmitter taskSubmitter;

    private TaskTracker taskTracker;

    private SharedPreferences sharedPrefs;

    private String deviceId;

    public static final String SHARED_PREFERENCE_NAME = "device_info";
    
    // PREFERENCE KEYS
    public static final String DEVICE_ID = "DEVICE_ID";
    public static final String EMULATOR_DEVICE_ID = "EMULATOR_DEVICE_ID";
    public static final String NOTIFICATION_ICON = "NOTIFICATION_ICON";
    public static final String SETTINGS_NOTIFICATION_ENABLED = "SETTINGS_NOTIFICATION_ENABLED";
    public static final String SETTINGS_SOUND_ENABLED = "SETTINGS_SOUND_ENABLED";
    public static final String SETTINGS_VIBRATE_ENABLED = "SETTINGS_VIBRATE_ENABLED";

    // NOTIFICATION FIELDS
    public static final int STATUSBAR_NOTIFICATION_ID = 9999;
    public static final String NOTIFICATION_ID = "NOTIFICATION_ID";
    public static final String NOTIFICATION_API_KEY = "NOTIFICATION_API_KEY";
    public static final String NOTIFICATION_TITLE = "NOTIFICATION_TITLE";
    public static final String NOTIFICATION_MESSAGE = "NOTIFICATION_MESSAGE";
    public static final String NOTIFICATION_URI = "NOTIFICATION_URI";
    // INTENT ACTIONS
    public static final String ACTION_SHOW_NOTIFICATION = "com.smartcommunit.SHOW_NOTIFICATION";
    public static final String ACTION_NOTIFICATION_CLICKED = "com.smartcommunit.NOTIFICATION_CLICKED";
    public static final String ACTION_NOTIFICATION_CLEARED = "com.smartcommunit.NOTIFICATION_CLEARED";

    public NotificationService() {
    }

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate()...");
        
        notificationReceiver = new NotificationServiceReceiver(this);
        notificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        //connectivityReceiver = new ConnectivityReceiver(this);
        //phoneStateListener = new PhoneStateChangeListener(this);
        
        telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        // wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        // connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        
        sharedPrefs = getSharedPreferences(NotificationService.SHARED_PREFERENCE_NAME,
                Context.MODE_PRIVATE);
        
        // Get deviceId
        deviceId = telephonyManager.getDeviceId();
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
        Log.d(TAG, "deviceId=" + deviceId);
        
        registerNotificationServiceReceiver();
    }

    @Override
    public void onStart(Intent intent, int startId) {
        Log.d(TAG, "onStart()...");
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy()...");
        unregisterNotificationServiceReceiver();
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind()...");
        return null;
    }

    @Override
    public void onRebind(Intent intent) {
        Log.d(TAG, "onRebind()...");
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d(TAG, "onUnbind()...");
        return true;
    }
    
    private void registerNotificationServiceReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_SHOW_NOTIFICATION);
        filter.addAction(ACTION_NOTIFICATION_CLICKED);
        filter.addAction(ACTION_NOTIFICATION_CLEARED);
        registerReceiver(notificationReceiver, filter);
    }

    private void unregisterNotificationServiceReceiver() {
        unregisterReceiver(notificationReceiver);
    }

    public static Intent getIntent() {
        return new Intent(SERVICE_NAME);
    }
    

    public ExecutorService getExecutorService() {
        return executorService;
    }

    public TaskSubmitter getTaskSubmitter() {
        return taskSubmitter;
    }

    public TaskTracker getTaskTracker() {
        return taskTracker;
    }

    public SharedPreferences getSharedPreferences() {
        return sharedPrefs;
    }

    public String getDeviceId() {
        return deviceId;
    }

    private void registerConnectivityReceiver() {
        Log.d(TAG, "registerConnectivityReceiver()...");
        telephonyManager.listen(phoneStateListener,
                PhoneStateListener.LISTEN_DATA_CONNECTION_STATE);
        IntentFilter filter = new IntentFilter();
        // filter.addAction(android.net.wifi.WifiManager.NETWORK_STATE_CHANGED_ACTION);
        filter.addAction(android.net.ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(connectivityReceiver, filter);
    }

    private void unregisterConnectivityReceiver() {
        Log.d(TAG, "unregisterConnectivityReceiver()...");
        telephonyManager.listen(phoneStateListener,
                PhoneStateListener.LISTEN_NONE);
        unregisterReceiver(connectivityReceiver);
    }

    /**
     * Class for summiting a new runnable task.
     */
    public class TaskSubmitter {

        final NotificationService notificationService;

        public TaskSubmitter(NotificationService notificationService) {
            this.notificationService = notificationService;
        }

        @SuppressWarnings("unchecked")
        public Future submit(Runnable task) {
            Future result = null;
            if (!notificationService.getExecutorService().isTerminated()
                    && !notificationService.getExecutorService().isShutdown()
                    && task != null) {
                result = notificationService.getExecutorService().submit(task);
            }
            return result;
        }

    }

    /**
     * Class for monitoring the running task count.
     */
    public class TaskTracker {

        final NotificationService notificationService;

        public int count;

        public TaskTracker(NotificationService notificationService) {
            this.notificationService = notificationService;
            this.count = 0;
        }

        public void increase() {
            synchronized (notificationService.getTaskTracker()) {
                notificationService.getTaskTracker().count++;
                Log.d(TAG, "Incremented task count to " + count);
            }
        }

        public void decrease() {
            synchronized (notificationService.getTaskTracker()) {
                notificationService.getTaskTracker().count--;
                Log.d(TAG, "Decremented task count to " + count);
            }
        }

    }
    
    public void setNotificationIcon(int iconId) {
        Editor editor = sharedPrefs.edit();
        editor.putInt(NotificationService.NOTIFICATION_ICON, iconId);
        editor.commit();
    }

    private int getNotificationIcon() {
        int icon = sharedPrefs.getInt(NotificationService.NOTIFICATION_ICON, 0);
        if (icon == 0) {
            icon = R.drawable.ic_launcher;
        }
        
        return icon;
    }

    private boolean isNotificationEnabled() {
        return sharedPrefs.getBoolean(NotificationService.SETTINGS_NOTIFICATION_ENABLED,
                true);
    }

    private boolean isNotificationSoundEnabled() {
        return sharedPrefs.getBoolean(NotificationService.SETTINGS_SOUND_ENABLED, true);
    }

    private boolean isNotificationVibrateEnabled() {
        return sharedPrefs.getBoolean(NotificationService.SETTINGS_VIBRATE_ENABLED, true);
    }


    public void applyNotification(String title, String content) {
        /*
         * Set Status Bar Notification.
         */
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setContentTitle(title)
                        .setContentText(content);
        // Creates an explicit intent for an Activity in your app
        Intent resultIntent = new Intent();
        resultIntent.setClass(this, ChatThread.class);

        // The stack builder object will contain an artificial back stack for the
        // started Activity.
        // This ensures that navigating backward from the Activity leads out of
        // your application to the Home screen.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        // Adds the back stack for the Intent (but not the Intent itself)
        stackBuilder.addParentStack(ChatThread.class); //Parent is LightMsgActivity.class set@Manifest.xml
        // Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );

        mBuilder.setContentIntent(resultPendingIntent);
        mBuilder.setAutoCancel(true);
//        mBuilder.setNumber(count);
        int defaults = 0;
        defaults |= Notification.DEFAULT_SOUND;
        defaults |= Notification.DEFAULT_VIBRATE;
        //defaults |= Notification.DEFAULT_LIGHTS;
        mBuilder.setDefaults(defaults);
        mBuilder.setLights(getResources().getColor(R.color.deepskyblue),
                1500,
                1500);

        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        // mId allows you to update the notification later on.
        int mId = CoreService.NOTIFICATION_MESSAGE_ID;
        mNotificationManager.notify(mId, mBuilder.build());
    }
}
