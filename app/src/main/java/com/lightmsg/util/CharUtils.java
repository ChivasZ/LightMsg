package com.lightmsg.util;

/**
 * Created by ZhangQh on 2016/3/24.
 */
public class CharUtils {
    public static boolean isValidUsername(CharSequence str) {
        final int len = str.length();
        for (int i=0; i<len; i++) {
            if (!Character.isLetterOrDigit(str.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    public static boolean isValidPassword(CharSequence str) {
        final int len = str.length();
        for (int i=0; i<len; i++) {
            if (!Character.isLetterOrDigit(str.charAt(i))) {
                return false;
            }
        }
        return true;
    }
}
