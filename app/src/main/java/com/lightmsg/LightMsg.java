package com.lightmsg;

import android.app.ActivityManager;
import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

import com.lightmsg.service.CoreService;
import com.lightmsg.service.CoreService.XmppServiceBinder;
import com.lightmsg.util.Locator;

public class LightMsg extends Application {
    private static final String TAG = LightMsg.class.getSimpleName();

    private static LightMsg lm;

    // To bind CoreService when start.
    public CoreService xs = null;
    public boolean bBind = false;

    public LightMsg() {
        lm = this;
    }

    public static Context getLightMsgContext() {
        return lm.getApplicationContext();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.v(TAG, "onCreate()...");
        
        if (!isXmppServiceRunning(this)) {
            Intent intent = new Intent(this, CoreService.class);
            startService(intent);
        }

        bindXmppService();
        
        try {
            Locator.getInstance(this);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        //MessageReceiverService.setBadge(getApplicationContext(), 2);
    }
    
    public void bindXmppService() {
        Log.v(TAG, "bindXmService()...bBind="+bBind);
        if (!bBind) {
            // Bind to CoreService
            Intent intent = new Intent(this, CoreService.class);
            bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        }
    }

    // Could NEVER be invoked in User Mode.
    @Override
    public void onTerminate () {
        super.onTerminate();
        Log.v(TAG, "onTerminate()...");

        // Unbind from the service
        if (bBind) {
            //unbindService(mConnection);
            bBind = false;
        }
    }

    /** Defines callbacks for service binding, passed to bindService() */
    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className,
                IBinder service) {
            Log.v(TAG, "onServiceConnected()...");
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            XmppServiceBinder binder = (XmppServiceBinder) service;
            xs = binder.getService();
            bBind = true;

            //xs.login();
            xs.registerMessageReceiver();
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            Log.v(TAG, "onServiceDisconnected()...");
            bBind = false;
        }
    };

    public boolean isXmppServiceRunning(Context context){
        Log.v(TAG, "isXmppServiceRunning()...");
        /*ActivityManager am = (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
        List<RunningServiceInfo> list = am.getRunningServices(100);
        for(RunningServiceInfo info : list){
            Log.v(TAG, "isXmppServiceRunning(), "+info.service.getClassName()+"=?="+CoreService.class.getName());
            if(info.service.getClassName().equals(CoreService.class.getName())){
                return true;
            }
        }*/
        return false;
    }
    
    public ComponentName getCurrentActivity() {
        ActivityManager am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        ComponentName cn = am.getRunningTasks(2).get(0).topActivity;
        return cn;
    }
}
