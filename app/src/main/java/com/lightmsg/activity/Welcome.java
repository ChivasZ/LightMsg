package com.lightmsg.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.Window;

import com.lightmsg.LightMsg;
import com.lightmsg.R;
import com.lightmsg.service.CoreService.Account;
import com.lightmsg.util.CountDownTimer;
import com.lightmsg.util.Locator;

public class Welcome extends Activity {

    private static final String TAG = Welcome.class.getSimpleName();
    private LightMsg app = null;
    
    //public XxxxService x = null;
    //public boolean bBind = false;
    private CountDownTimer cdt = null;
    
    private Locator locator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.v(TAG, "onCreate()...");
        super.onCreate(savedInstanceState);
        app = (LightMsg)getApplication();

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        //getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.welcome);
        
        // Bind to XxxxService
        //app.bindXxxxService();
        
        //ImageView ivCover = (ImageView)findViewById(R.id.cover);
        //ivCover.setImageBitmap(BitmapFactory.decodeFile(pathName));

        /*
        Intent intent = new Intent(this, LocatorService.class);
        startService(intent);
        
        try {
            locator = Locator.getInstance(this);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        // Count down, wait for Service binding.
        cdt = new CountDownTimer(1000, 100) {
            @Override
            public void onTick(long millisUntilFinished) {
                Log.v(TAG, "onTick()...");
            }
            @Override
            public void onFinish() {
                Log.v(TAG, "onFinish()...");
                if (app != null && app.bBind && locator.getMessageGroupId() != null) {
                    startLoginUsers();
                } else {
                    cdt.start();
                }
            }
        }.start();
        */
        
        // Count down, wait for Service binding.
        cdt = new CountDownTimer(3000, 100) {
            @Override
            public void onTick(long millisUntilFinished) {
                Log.v(TAG, "onTick()...");
            }
            @Override
            public void onFinish() {
                Log.v(TAG, "onFinish()...");
                if (app != null && app.bBind) {
                    if (app.xs.isOnceLogined()) {
                        startLightMsgActivity();
                    } else {
                        startLoginUsers();
                    }
                } else {
                    cdt.start();
                }
            }
        }.start();
        
        
        if (app != null 
                && app.bBind) {
            cdt.cancel();
            
            if (app.xs.isOnceLogined()) {
                startLightMsgActivity();
            } else {
                startLoginUsers();
            }
        }

        /*Intent intent = new Intent();
        intent.setClass(Welcome.this, ChatThread.class);
        intent.putExtra("user", "test");
        intent.putExtra("name", "TEST");
        startActivity(intent);
        finish();*/
        /*Intent intent = new Intent();
        intent.setClass(Welcome.this, FirstEntry.class);
        startActivity(intent);
        finish();*/
    }
    
    private String getLastUser() {
        String lu = "";

        if (app.xs != null) {
            Account account = app.xs.getAccount();
            if (account == null) {
                account = app.xs.new Account();
            }
            lu = account.account;
        }
        Log.v(TAG, "getLastUser(), \"username\"="+lu);

        return lu;
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        Log.v(TAG, "onTouchEvent()...");
        return false;
    }

    @Override
    protected void onResume() {
        Log.v(TAG, "onResume()...");
        super.onResume();
    }
    
    @Override
    protected void onPause() {
        Log.v(TAG, "onResume()...");
        super.onPause();
    }

    @Override
    protected void onStart() {
        Log.v(TAG, "onStart()...");
        super.onStart();
    }

    @Override
    protected void onStop() {
        Log.v(TAG, "onStop()...");
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        Log.v(TAG, "onDestroy()...");
        super.onDestroy();
    }

    /** Defines callbacks for service binding, passed to bindService() */
    /*private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                IBinder service) {
            Log.v(TAG, "onServiceConnected()...");
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            XmppServiceBinder binder = (XmppServiceBinder) service;
            xs = binder.getService();
            bBind = true;
            
            boolean bLogined = false;
            if (xs != null)
                bLogined = xs.getSharedPreferencesLogin().getBoolean("remember_password", false);
            if (bLogined) {
                cdt.cancel();
                startLoginUsers();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            Log.v(TAG, "onServiceDisconnected()...");
            bBind = false;
        }
    };*/

    public void startLightMsgActivity() {
        Log.v(TAG, "startLoginUsers()... app="+app);
        if (app != null && app.bBind ) {
            Intent intent = new Intent();
            intent.setClass(Welcome.this, com.lightmsg.activity.msgdesign.LightMsgActivity.class);
            //intent.setClass(Welcome.this, com.lightmsg.activity.msg.LightMsgActivity.class);
            startActivity(intent);
            finish();
        } else {
            Log.e(TAG, "Encount fatal error, must quit!!!!!");
            Log.e(TAG, "app="+app);
            if (app != null)
                Log.e(TAG, "app.bBind="+app.bBind);
        }
    }
    
    public void startLoginUsers() {
        Log.v(TAG, "startLoginUsers()... app="+app);
        if (app != null && app.bBind ) {
            Intent intent = new Intent();
            intent.setClass(Welcome.this, com.lightmsg.activity.msgdesign.etc.LoginUsers.class);
            startActivity(intent);
            finish();
        } else {
            Log.e(TAG, "Encount fatal error, must quit!!!!!");
            Log.e(TAG, "app="+app);
            if (app != null)
                Log.e(TAG, "app.bBind="+app.bBind);
            finish();
        }
    }
}

