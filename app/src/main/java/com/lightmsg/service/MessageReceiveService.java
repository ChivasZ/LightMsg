package com.lightmsg.service;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDiskIOException;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import com.lightmsg.LightMsg;
import com.lightmsg.R;
import com.lightmsg.activity.msgdesign.chat.ChatThread;

public class MessageReceiveService extends IntentService {
    private static final String TAG = "LightMsg/" + MessageReceiveService.class.getSimpleName();
    private LightMsg app = null;
    private CoreService xs = null;
    
    public MessageReceiveService(String name) {
        super(name);
    }
    
    public MessageReceiveService() {
        super("MessageReceiveService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.v(TAG, "onHandleIntent()...");
        app = (LightMsg)getApplication();
        xs = app.xs;
        
        if (CoreService.ACTION_RECEIVE_NEW_MSG == intent.getAction()) {
            /*
             * Check if app's activity is running...
             */
            applyNotification(intent);
        } else if (CoreService.ACTION_MSG_STATE_CHANGE == intent.getAction()) {
            
        } else {
            
        }
    }
    
    private void applyNotification(Intent intent) {
        int count = xs.getConvTotalUnread(this);
        String current = xs.getCurrentThreadUser();

        boolean isGroup = false;
        boolean ignore = false;
        String group = null;
        String group_name = null;
        String user = null;
        String name = null;
        Log.v(TAG, "MSG_TYPE:"+intent.getStringExtra("MSG_TYPE"));
        if ("groupchat".equals(intent.getStringExtra("MSG_TYPE"))) {
            Log.v(TAG, "MSG_GROUP:"+intent.getStringExtra("MSG_GROUP"));
            Log.v(TAG, "MSG_GROUPNAME:"+intent.getStringExtra("MSG_GROUPNAME"));
            group = intent.getStringExtra("MSG_GROUP");
            group_name = intent.getStringExtra("MSG_GROUPNAME");
            isGroup = true;
        } else {
            Log.v(TAG, "MSG_USER:"+intent.getStringExtra("MSG_USER"));
            Log.v(TAG, "MSG_NAME:"+intent.getStringExtra("MSG_NAME"));
            user = intent.getStringExtra("MSG_USER");
            name = intent.getStringExtra("MSG_NAME");
            isGroup = false;

            if (current != null && current.equals(xs.guaranteeUid(user))) {
                ignore = true;
            }
        }


        if (ignore) return;

        /*
         * Set Badge for Samusng Tw APP Icon.
         */
        setBadge(this, count);
        
        /*
         * Set Status Bar Notification.
         */
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentTitle(String.format(this.getResources().getString(R.string.noti_content_title), count))
                .setContentText(intent.getStringExtra("MSG_CONTENT"));
        // Creates an explicit intent for an Activity in your app
        Intent resultIntent = new Intent();
        resultIntent.setClass(this, ChatThread.class);
        if (isGroup) {
            resultIntent.putExtra("group", group);
            resultIntent.putExtra("group_name", group_name);
        } else {
            resultIntent.putExtra("user", user);
            resultIntent.putExtra("name", name);
        }

        
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
        
        /*Intent[] intents = new Intent[2];
        
        //intents[0] = new Intent(app, LightMsgActivity.class);
        intents[0] = Intent.makeRestartActivityTask(new ComponentName(app, LightMsgActivity.class));
        intents[1] = new Intent(app, ChatThread.class);
        intents[1].setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intents[1].setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        PendingIntent resultPendingIntent = PendingIntent.getActivities(
                app, 
                0,
                intents,
                PendingIntent.FLAG_UPDATE_CURRENT);*/
        
        mBuilder.setContentIntent(resultPendingIntent);
        mBuilder.setAutoCancel(true);
        mBuilder.setNumber(count);
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

    private static String BADGE_PACKAGENAME;// = "com.lightmsg";
    private static String BADGE_CLASSNAME;// = "com.lightmsg.activity.Welcome";
    
    public static void setBadge(Context context, int count) {
        Log.v(TAG, "setBadge()...");
        ContentResolver cr = context.getContentResolver();

        if (BADGE_PACKAGENAME == null) {
            BADGE_PACKAGENAME = context.getResources().getString(R.string.app_package_name);
        }
        if (BADGE_CLASSNAME == null) {
            BADGE_CLASSNAME = context.getResources().getString(R.string.app_badge_class);
        }
        
        try {
            BadgeNotification.Apps.setBadgeCount(cr, BADGE_PACKAGENAME, BADGE_CLASSNAME, count);
            Log.d(TAG, "setBadgeCount(), count=" + count);
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "setBadgeCount() BadgeProvider wasn't installed!!!");
        } catch (SQLiteDiskIOException e) {
            Log.e(TAG, "setBadgeCount() caught SQLiteDiskIOException while setting badge count, "+e);
        } catch (SecurityException e) {
            Log.e(TAG, "setBadgeCount() caught SecurityException while setting badge count, "+e);
        }  catch (Exception e) {
            Log.e(TAG, "setBadgeCount() caught Exception while setting badge count, "+e);
        }
    }
}
