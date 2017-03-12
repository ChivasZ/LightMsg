package com.lightmsg.androidTools;


import android.R.integer;
import android.content.Context;
import android.net.Uri;

public class MyTools {
    public static String intToHanzi(int i){
        switch (i) {
        case 1:
            return "一";
        case 2:
            return "二";
        case 3:
            return "三";
        case 4:
            return "四";
        case 5:
            return "五";
        case 6:
            return "六";
        case 7:
            return "七";
        default:
            return "";
        }
    }
    public static String getServer(Context context){
        return context.getContentResolver().getType(Uri.parse("content://"+"com.dhec.ServerUrlProvider"));

    }
}

