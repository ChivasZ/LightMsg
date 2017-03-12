package com.lightmsg.xm;

import org.jivesoftware.smack.packet.IQ;

import android.util.Log;

public class XmppManagerIQ extends IQ {

    private static final String TAG = 
            "SmartCommunity/" + XmppManagerIQ.class.getSimpleName();
    
    @Override
    public String getChildElementXML() {
        // TODO Auto-generated method stub
        Log.i(TAG, "getChildElementXML()...");
        
        return null;
    }

}
