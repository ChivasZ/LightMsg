package com.lightmsg.xm;

import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.provider.IQProvider;
import org.xmlpull.v1.XmlPullParser;

import android.util.Log;

public class XmppManagerIQProvider {}/*implements IQProvider {

    private static final String TAG = 
            "SmartCommunity/" + XmppManagerIQProvider.class.getSimpleName();
    
    // Every implementation of this interface must have
    // a public, no-argument constructor. 
    public XmppManagerIQProvider() {
    }
    
    @Override
    public IQ parseIQ(XmlPullParser parser) throws Exception {
        // TODO Auto-generated method stub
        Log.i(TAG, "parseIQ()...");
        XmppManagerIQ xmiq = new XmppManagerIQ();
        
        for (;;) {
            int eventType = parser.next();
            if (eventType == org.xmlpull.v1.XmlPullParser.START_TAG) {
                /*if ("id".equals(parser.getName())) {
                    xmiq.setId(parser.nextText());
                }
                if ("apiKey".equals(parser.getName())) {
                    xmiq.setApiKey(parser.nextText());
                }
                if ("title".equals(parser.getName())) {
                    xmiq.setTitle(parser.nextText());
                }
                if ("message".equals(parser.getName())) {
                    xmiq.setMessage(parser.nextText());
                }
                if ("uri".equals(parser.getName())) {
                    xmiq.setUri(parser.nextText());
                }*/
            /*} else if (eventType == org.xmlpull.v1.XmlPullParser.END_TAG 
                    && "xmiq".equals(parser.getName())) {
                break;
            }
        }
        
        return xmiq;
    }

}*/
