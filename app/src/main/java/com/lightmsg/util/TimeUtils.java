package com.lightmsg.util;

import android.annotation.SuppressLint;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;

@SuppressLint("SimpleDateFormat")
public class TimeUtils {
    static private long ONE_DAY = 1000*60*60*24;
    static private long ONE_MONTH = ONE_DAY*30;
    static private long ONE_YEAR = ONE_MONTH*12;

    static public long getCurMillis() {
        long time = System.currentTimeMillis();
        Log.v("TimeUtils", "current="+time);
        return time;
    }
    
    static public String getCurString() {
        SimpleDateFormat formatter = new SimpleDateFormat(getFormat());     
        Date curDate = new Date(System.currentTimeMillis());   
        String str = formatter.format(curDate); 
        //Log.v("TimeUtils", str);
        return str;
    }
    
    static public String getCurString(long millis) {
        //Log.v("TimeUtils", "millis="+millis);
        SimpleDateFormat formatter = new SimpleDateFormat(getFormat(millis));     
        Date date = new Date(millis);  
        String str = formatter.format(date);     
        return str;
    }
    
    static private String getFormat() {
        return "yyyy年MM月dd日 HH:mm:ss";
    }

    static private String getFormat(long millis) {
        if (System.currentTimeMillis() - millis < ONE_DAY) {
            return "HH:mm:ss";
        } else if (System.currentTimeMillis() - millis < ONE_MONTH) {
            return "MM月dd日 HH:mm:ss";
        } else if (System.currentTimeMillis() - millis < ONE_YEAR) {
            return "MM月dd日";
        } else {
            return "yyyy年MM月dd日";
        }
        //return "yyyy年MM月dd日 HH:mm:ss";
    }
}
