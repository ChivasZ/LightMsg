/*
 * Copyright (C) 2010 Moduad Co., Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.lightmsg.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/** 
 * Broadcast receiver that handles push notification messages from the server.
 * This should be registered as receiver in AndroidManifest.xml. 
 * 
 */
public final class NotificationServiceReceiver extends BroadcastReceiver {

    private static final String TAG = NotificationServiceReceiver.class.getSimpleName();
    private NotificationService ns;

    public NotificationServiceReceiver(NotificationService ns) {
        this.ns = ns;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "NotificationServiceReceiver.onReceive()...");
        String action = intent.getAction();
        Log.d(TAG, "action=" + action);

        if (NotificationService.ACTION_SHOW_NOTIFICATION.equals(action)) {
            String notificationId = intent
                    .getStringExtra(NotificationService.NOTIFICATION_ID);
            String notificationApiKey = intent
                    .getStringExtra(NotificationService.NOTIFICATION_API_KEY);
            String notificationTitle = intent
                    .getStringExtra(NotificationService.NOTIFICATION_TITLE);
            String notificationMessage = intent
                    .getStringExtra(NotificationService.NOTIFICATION_MESSAGE);
            String notificationUri = intent
                    .getStringExtra(NotificationService.NOTIFICATION_URI);

            Log.d(TAG, "notificationId=" + notificationId);
            Log.d(TAG, "notificationApiKey=" + notificationApiKey);
            Log.d(TAG, "notificationTitle=" + notificationTitle);
            Log.d(TAG, "notificationMessage=" + notificationMessage);
            Log.d(TAG, "notificationUri=" + notificationUri);

            //ns.notify(notificationId, notificationApiKey,
            //		notificationTitle, notificationMessage, notificationUri);
        }
    }

}
