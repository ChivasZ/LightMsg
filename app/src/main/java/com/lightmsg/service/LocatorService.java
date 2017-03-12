package com.lightmsg.service;

import com.lightmsg.activity.LocationByManual;
import com.lightmsg.util.CountDownTimer;
import com.lightmsg.util.Locator;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

public class LocatorService extends Service {
    private static final String TAG = LocatorService.class.getSimpleName();
    
    private Locator locator;
    private String current;
    private CountDownTimer cdtLocation;

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate()...");
        super.onCreate();
        
        try {
            locator = Locator.getInstance(this);
            locator.initLocation();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        cdtLocation = new CountDownTimer(10000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                Log.v(TAG, "cdtLocation.onTick(), millisUntilFinished="+millisUntilFinished);
                current = locator.getCurrentLocation();
                if (current != null && !current.isEmpty()) {
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
        }.start();
    }
    
    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy()...");
        super.onDestroy();
    }

    public class LocatorServiceBinder extends Binder {
        public LocatorService getService() {
            return LocatorService.this;
        }
    };
    
    @Override
    public IBinder onBind(Intent intent) {
        // TODO Auto-generated method stub
        Log.d(TAG, "onBind(), intent="+intent);
        return new LocatorServiceBinder();
    }


    //public static final int REQUEST_CODE_MANUAL_LOCATION =	1000;
    private void startManualLocation() {
        Log.v(TAG, "startManualLocation()..");
        Intent intent = new Intent();
        intent.setClass(this, LocationByManual.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        
        intent.putExtra("fromLocatorService", true);

        this.startActivity(intent);
        //startActivity(intent, REQUEST_CODE_MANUAL_LOCATION); 
    }

    public void setCurrent(String loc) {
        current = loc;
        locator.setManualLocation(current);
    }
}
