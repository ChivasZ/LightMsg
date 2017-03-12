package com.lightmsg.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.util.Log;

public class EmojiFileUtils {
    private static final String TAG = "SmartCommunity/" + EmojiFileUtils.class.getSimpleName();

    public static List<String> getEmojiFile(Context context) {
        try {
            List<String> list = new ArrayList<String>();
            InputStream in = context.getResources().getAssets().open("emoji");
            BufferedReader br = new BufferedReader(new InputStreamReader(in, "UTF-8"));
            String str = null;
            while ((str = br.readLine()) != null) {
                list.add(str);
                //Log.v(TAG, "getEmojiFile()... Add str="+str);
            }

            return list;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}