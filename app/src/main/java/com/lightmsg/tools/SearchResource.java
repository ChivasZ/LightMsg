package com.lightmsg.androidTools;


import android.content.Context;
import android.content.res.Resources.NotFoundException;



public class SearchResource {
    /**
     * 获取图片id
     * @param cnt
     * @param name
     * @return
     * @throws NotFoundException
     */
    public static int getDrawableResId(Context cnt, String name) throws NotFoundException {
        int resid = 0;
        resid = cnt.getResources().getIdentifier(name, "drawable", cnt.getPackageName());
        if (resid == 0)
            resid = cnt.getResources().getIdentifier( "transparent_background", "drawable", cnt.getPackageName());
        return resid;
    }
    
    public static int getStringResId(Context cnt, String name) throws NotFoundException {
        int resid = 0;
        resid = cnt.getResources().getIdentifier(name, "string", cnt.getPackageName());
        if (resid == 0)
            resid = cnt.getResources().getIdentifier("empty_string", "string", cnt.getPackageName());
        return resid;
    }
    
    public static int getLayoutResId(Context cnt, String name) throws NotFoundException {
        int resid = 0;
        resid = cnt.getResources().getIdentifier(name, "layout", cnt.getPackageName());
        if (resid == 0)
            resid = cnt.getResources().getIdentifier("empty_layout","layout", cnt.getPackageName());
        return resid;
    }
    
    public static int getItemResId(Context cnt, String name) throws NotFoundException {
        int resid = cnt.getResources().getIdentifier(name, "id", cnt.getPackageName());
        return resid;
    }
    
    public static int getAnimResId(Context cnt, String name) throws NotFoundException {
        int resid = cnt.getResources().getIdentifier(name, "anim", cnt.getPackageName());
        return resid;
    }
    
    public static int getAttrResId(Context cnt, String attrName) throws NotFoundException {
        int resid = cnt.getResources().getIdentifier(attrName, "attr", cnt.getPackageName());
        return resid;
    }
    
    public static int getStyleResId(Context cnt, String attrName) throws NotFoundException {
        int resid = cnt.getResources().getIdentifier(attrName, "style", cnt.getPackageName());
        return resid;
    }
    
    public static int getMenuResId(Context cnt, String name) throws NotFoundException {
        int resid = cnt.getResources().getIdentifier(name, "menu", cnt.getPackageName());
        return resid;
    }
    
    public static int[] getStyleableArray(Context cnt, String name) {
        return null;
    }
}
