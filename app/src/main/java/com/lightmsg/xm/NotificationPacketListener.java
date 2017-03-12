package com.lightmsg.xm;

import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.packet.Packet;

import com.lightmsg.service.NotificationService;

import android.content.Intent;
import android.util.Log;

/** 
 * This class notifies the receiver of incoming notifcation packets asynchronously.  
 *
 */
public class NotificationPacketListener implements PacketListener {

    private static final String TAG = NotificationPacketListener.class.getSimpleName();

    private final XmppManager xmppManager;

    public NotificationPacketListener(XmppManager xmppManager) {
        this.xmppManager = xmppManager;
    }

    @Override
    public void processPacket(Packet packet) {
        Log.d(TAG, "NotificationPacketListener.processPacket()...");
        Log.d(TAG, "packet.toXML()=" + packet.toXML());

        if (packet instanceof NotificationIQ) {
            NotificationIQ notification = (NotificationIQ) packet;

            if (notification.getChildElementXML().contains(
                    "jabber:iq:notification")) {
                String notificationId = notification.getId();
                String notificationApiKey = notification.getApiKey();
                String notificationTitle = notification.getTitle();
                String notificationMessage = notification.getMessage();
                //                String notificationTicker = notification.getTicker();
                String notificationUri = notification.getUri();

                Intent intent = new Intent(NotificationService.ACTION_SHOW_NOTIFICATION);
                intent.putExtra(NotificationService.NOTIFICATION_ID, notificationId);
                intent.putExtra(NotificationService.NOTIFICATION_API_KEY,
                        notificationApiKey);
                intent
                .putExtra(NotificationService.NOTIFICATION_TITLE,
                        notificationTitle);
                intent.putExtra(NotificationService.NOTIFICATION_MESSAGE,
                        notificationMessage);
                intent.putExtra(NotificationService.NOTIFICATION_URI, notificationUri);

                xmppManager.getContext().sendBroadcast(intent);
            }
        }

    }

}
