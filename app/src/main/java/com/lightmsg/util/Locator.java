package com.lightmsg.util;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import com.lightmsg.LightMsg;
import com.lightmsg.R;

import android.content.Context;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.util.Log;

public class Locator {
    private static final String TAG = Locator.class.getSimpleName();

    private Context ctx;
    private LightMsg app;

    private static Locator instance;
    private static int instanceCnt = -1;
    private LocationManager lm;
    private LocationListener ll1;
    private LocationListener ll2;
    private Location lastLocation;

    private List<String> allProviders;
    private String bestProvider;

    private Geocoder gc;
    private List<Address> addresses;

    private String current;
    private String manual;

    private GpsStatus gs;

    public Locator(Context context) throws Exception {
        if (instanceCnt == 0) {
            ctx = context;
            instanceCnt++;
        } else {
            throw new Exception("Should keep only one instance by invoke getInstance()");
        }
    }

    public static Locator getInstance(Context context) throws Exception {
        if (instance == null) {
            instanceCnt++;
            instance = new Locator(context);
        }

        return instance;
    }

    public void initLocation() {
        //downloadGPSXtra();

        lm = (LocationManager) ctx.getSystemService(Context.LOCATION_SERVICE);
        initProviders();

        ll1 = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                // TODO Auto-generated method stub
                Log.d(TAG, "LocationListener.onLocationChanged(), location="+location);
                Log.d(TAG, "时间："+location.getTime());
                Log.d(TAG, "经度："+location.getLongitude());
                Log.d(TAG, "纬度："+location.getLatitude());
                Log.d(TAG, "海拔："+location.getAltitude());

                lastLocation = location;
                setSiteByLocation(lastLocation);

                //cdtLocation.cancel();
                //lm.removeUpdates(ll1);
            }

            @Override
            public void onProviderDisabled(String provider) {
                // TODO Auto-generated method stub
                Log.d(TAG, "LocationListener.onProviderDisabled(), provider="+provider);
                Log.d(TAG, "当前GPS状态：禁用\n");
            }

            @Override
            public void onProviderEnabled(String provider) {
                // TODO Auto-generated method stub
                Log.d(TAG, "LocationListener.onProviderEnabled(), provider="+provider);
                Log.d(TAG, "当前GPS状态：开启\n");
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
                // TODO Auto-generated method stub
                Log.d(TAG, "LocationListener.onStatusChanged(), provider="+provider+", status="+status+", extras="+extras);
                if(status==LocationProvider.AVAILABLE){
                    Log.d(TAG, "当前GPS状态：可见的\n");
                }else if(status==LocationProvider.OUT_OF_SERVICE){
                    Log.d(TAG, "当前GPS状态：服务区外\n");
                }else if(status==LocationProvider.TEMPORARILY_UNAVAILABLE){
                    Log.d(TAG, "当前GPS状态：暂停服务\n");
                }
            }

        };

        try {
            lm.addGpsStatusListener(new GpsStatus.Listener() {

                @Override
                public void onGpsStatusChanged(int status) {
                    // TODO Auto-generated method stub
                    gs = lm.getGpsStatus(gs);
                    switch (status) {
                        case GpsStatus.GPS_EVENT_STARTED:
                            Log.d(TAG, "onGpsStatusChanged, GPS_EVENT_STARTED");
                            break;
                        case GpsStatus.GPS_EVENT_STOPPED:
                            Log.d(TAG, "onGpsStatusChanged, GPS_EVENT_STOPPED");
                            break;
                        case GpsStatus.GPS_EVENT_FIRST_FIX:
                            Log.d(TAG, "onGpsStatusChanged, GPS_EVENT_FIRST_FIX");
                            Log.d(TAG, "TimeToFirstFix: " + gs.getTimeToFirstFix());
                            break;
                        case GpsStatus.GPS_EVENT_SATELLITE_STATUS:
                            Log.d(TAG, "onGpsStatusChanged, GPS_EVENT_SATELLITE_STATUS");

                            Iterable<GpsSatellite> gss = gs.getSatellites();
                            Log.d(TAG, "Satellites: " + gss.toString());
                            Iterator<GpsSatellite> it = gss.iterator();
                            while (it.hasNext()) {
                                Log.d(TAG, "Satellite: " + it.next());
                            }
                            break;
                        default:
                            break;
                    }
                }

            });
        } catch (SecurityException e) {
            e.printStackTrace();
        }

        getLocation();
        if (lastLocation == null) {
            Log.e(TAG, "Get last location failed.");
            requestLocationUpdates();
        } else {
            setSiteByLocation(lastLocation);
        }
    }

    private void downloadGPSXtra() {
        LocationManager locationmanager = (LocationManager) ctx.getSystemService(Context.LOCATION_SERVICE);
        Bundle bundle = new Bundle();
        locationmanager.sendExtraCommand(LocationManager.GPS_PROVIDER, "force_xtra_injection", bundle);
        locationmanager.sendExtraCommand(LocationManager.GPS_PROVIDER, "force_time_injection", bundle);
        //locationmanager.sendExtraCommand(LocationManager.GPS_PROVIDER, "delete_aiding_data", null);
    }

    private void initProviders() {
        Criteria criteria = new Criteria();
        //criteria.setAccuracy(Criteria.ACCURACY_FINE); //高精度
        criteria.setAccuracy(Criteria.ACCURACY_COARSE); //低精度
        criteria.setAltitudeRequired(false); //不要求海拔
        criteria.setBearingRequired(false); //不要求方位
        criteria.setCostAllowed(false); //不允许有话费
        criteria.setPowerRequirement(Criteria.POWER_LOW); //低功耗

        bestProvider = lm.getBestProvider(criteria, true);
        Log.d(TAG, "Best provider for Location: "+bestProvider);

        allProviders = lm.getAllProviders();
        Log.d(TAG, "All providers for Location: "+allProviders);
    }

    public boolean getLocation() {
        try {
            lastLocation = lm.getLastKnownLocation(bestProvider);
        } catch (SecurityException e) {
            e.printStackTrace();
        }
        Log.d(TAG, "getLocation(), lastLocation(from best provider):"+lastLocation);

        if (lastLocation == null) {
            for (String provider : allProviders) { //LocationManager.GPS_PROVIDER/LocationManager.NETWORK_PROVIDER
                try {
                    lastLocation = lm.getLastKnownLocation(provider);
                } catch (SecurityException e) {
                    e.printStackTrace();
                }
                if (lastLocation != null) {
                    Log.d(TAG, "getLocation(), lastLocation(from '"+provider+"'):"+lastLocation);
                    break;
                }
            }
        }

        Log.d(TAG, "getLocation(), lastLocation="+lastLocation);
        if (lastLocation != null) {
            return true;
        }
        return false;
    }

    public void removeLocationUpdates() {
        if (ll1 != null) {
            try {
                lm.removeUpdates(ll1);
            } catch (SecurityException e) {
                e.printStackTrace();
            }
        }
    }

    private boolean isProviderOn(String provider) {
        if (lm != null) {
            return lm.isProviderEnabled(provider);
        }
        return false;
    }

    public void requestLocationUpdates() {
        Log.v(TAG, "requestLocationUpdates()..");
        for (String provider : allProviders) { //LocationManager.GPS_PROVIDER/LocationManager.NETWORK_PROVIDER
            if (isProviderOn(provider)) {
                Log.e(TAG, "requestLocationUpdates(), request to \""+provider+"\"");
                try {
                    lm.requestSingleUpdate(provider, ll1, null);
                } catch (SecurityException e) {
                    e.printStackTrace();
                }
            } else {
                Log.e(TAG, "requestLocationUpdates(), \""+provider+"\" is not ON!");
            }
        }
    }

    private void setSiteByLocation(Location lastLocation) {

        gc = new Geocoder(ctx.getApplicationContext(), Locale.CHINA);
        try {
            addresses = gc.getFromLocation(lastLocation.getLatitude(), lastLocation.getLongitude(), 1);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        Log.d(TAG, "addresses="+addresses);
        if (addresses.size() > 0) {
            setCurrentLocation(addresses.get(0).getAdminArea());
        }
    }

    public String getCurrentLocation() {
        return current;
    }

    public void setCurrentLocation(String loc) {
        current = loc;
        setMessageGroupIdAndName(loc);
    }

    public String getManualLocation() {
        return manual;
    }

    public void setManualLocation(String loc) {
        manual = loc;
        setMessageGroupIdAndName(loc);
    }

    private String groupName;
    private String groupId;
    private String[] provinces;
    private String[] provinceIds;
    public void setMessageGroupIdAndName(String loc) {
        provinces = ctx.getResources().getStringArray(R.array.provinces);
        provinceIds = ctx.getResources().getStringArray(R.array.province_ids);

        int i = 0;
        for (i = 0; i < provinces.length; i++) {
            if (loc.equals(provinces[i]))
                break;
        }
        groupName = provinces[i];
        groupId = provinceIds[i];
    }

    public String getMessageGroupName() {
        return groupName;
    }

    public String getMessageGroupId() {
        return groupId;
    }


    private void test1111() {
        CellInfoManager cellManager = new CellInfoManager(ctx);
        WifiInfoManager wifiManager = new WifiInfoManager(ctx);
        CellLocationManager locationManager = new CellLocationManager(ctx, cellManager, wifiManager) {
            @Override
            public void onLocationChanged() {
                this.stop();
            }
        };
        locationManager.start();
    }
}
