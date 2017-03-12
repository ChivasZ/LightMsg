package com.lightmsg.activity;

import org.json.JSONException;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.GridView;
import android.widget.ListAdapter;

public class GridViewCust extends GridView {
    private String url;
    public GridViewCust(Context context) {
        super(context);
        // TODO Auto-generated constructor stub
    }
    
    public GridViewCust(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    
    public GridViewCust(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }
    
    public void setAdapter(ListAdapter la) {
        try {
            new GridViewAdapterBuilder().create((ViewAdapter)la, null);
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        super.setAdapter(la);
    }

}
