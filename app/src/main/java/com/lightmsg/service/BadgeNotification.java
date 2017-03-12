/*
 * Copyright (C) 2007 The Android Open Source Project
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

import static android.util.Log.e;
import static android.util.Log.w;

import java.io.ByteArrayOutputStream;

import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Debug;
import android.os.RemoteException;
import android.provider.BaseColumns;
import android.util.Log;

/**
 * The Badge provider contains information about placed and received calls.
 */
public class BadgeNotification {
    private static final String TAG = "BadgeNotification";
    private static final boolean DEBUGGABLE = true;
    public static final String AUTHORITY = "com.sec.badge";

    private static final String CONTENT_AUTHORITY_SLASH = "content://" + AUTHORITY + "/";
    
    public static class Apps implements BaseColumns {
        /**
         * The content:// style URL for this table
         */
        public static final Uri CONTENT_URI =
                Uri.parse(CONTENT_AUTHORITY_SLASH + "apps");
        
        /**
         * The package name of the application
         * <P>Type: TEXT</P>
         */
        public static final String PACKAGENAME = "package";
        
        /**
         * The class name of the application
         * <P>Type: TEXT</P>
         */
        public static final String CLASSNAME = "class";
        
        /**
         * The badge count of the application
         * <P>Type: INTEGER</P>
         */
        public static final String BADGECOUNT = "badgecount";
        
        /**
         * The icon of the application
         * <P>Type: BLOB</P>
         */
        public static final String ICON = "icon";

        /**
         * The extra data for individual badge count of same component name in the application
         * <P>Type: TEXT</P>
         */
        public static final String EXTRADATA = "extraData";

        public static final String INTENT_EXTRA_BADGE = "have_badge";

        public static final String INTENT_DEFAULT_EXTRA_DATA = "base_extra_badge";

        public static final Uri getContentUri(long id) {
            return Uri.parse(CONTENT_AUTHORITY_SLASH + "apps/" + id);
        }
        
        public static final long getId(ContentResolver cr, String packageName, String className) {
            return getId(cr, packageName, className, Apps.INTENT_DEFAULT_EXTRA_DATA);
        }
        public static final long getId(ContentResolver cr, String packageName, String className, String extraData) {
            if (DEBUGGABLE) Log.d(TAG, "getId : className = " + className);
            Cursor cursor = null;
            long id = -1;
                    
            ContentProviderClient client = null;
            try {
                client = cr.acquireContentProviderClient(Apps.CONTENT_URI);
                cursor = client.query(Apps.CONTENT_URI,
                        new String[] {Apps._ID},
                        Apps.PACKAGENAME + "='" + packageName + "' AND " + Apps.CLASSNAME + "='" + className + "' AND "
                        +Apps.EXTRADATA + "='" + extraData + "'",
                        null, null);
            } catch (NullPointerException e) {
                Log.e(TAG, "getId() " + e); 
            } catch (SQLiteException e) {
                Log.e(TAG, "getId() " + e);
            } catch (IllegalStateException e) {
                Log.e(TAG, "getId() " + e);
            } catch (RemoteException e) {
                Log.e(TAG, "getId() " + e);
            } finally {
                if (cursor != null) {
                    if (cursor.getCount() > 0) {
                        cursor.moveToFirst();
                        id = cursor.getInt(0);
                    }
                    cursor.close();
                }
                if (client != null) {
                    if (DEBUGGABLE) Log.d(TAG, "ContentProviderClient is released");
                    client.release();
                }
            }
            return id;
        }
        
        public static final void setBadgeCount(ContentResolver cr, String packageName, String className, int badgeCount) {
            setBadgeCount(cr, packageName, className, INTENT_DEFAULT_EXTRA_DATA, badgeCount);
        }

        public static final void setBadgeCount(ContentResolver cr, String packageName, String className, String extraData, int badgeCount) {
            if (DEBUGGABLE) Log.d(TAG,"setBadgeCount : badgeCount = " + badgeCount);
            long id = getId(cr, packageName, className, extraData);
            ContentValues values = new ContentValues();
            values.put(Apps.BADGECOUNT, badgeCount);
            if (id == -1) {
                values.put(Apps.PACKAGENAME, packageName);
                values.put(Apps.CLASSNAME, className);
                values.put(Apps.EXTRADATA, extraData);
                cr.insert(Apps.CONTENT_URI, values);
            } else {
                cr.update(getContentUri(id), values, null, null);
            }
        }
        public static final void setIcon(ContentResolver cr, String packageName, String className, String extraData, byte [] data) {
            w(TAG, "No supported API");
            long id = getId(cr, packageName, className, extraData);
            ContentValues values = new ContentValues();
            values.put(Apps.ICON, data);
            if (id == -1) {
                values.put(Apps.PACKAGENAME, packageName);
                values.put(Apps.CLASSNAME, className);
                values.put(Apps.EXTRADATA, extraData);
                cr.insert(Apps.CONTENT_URI, values);
            } else {
                cr.update(getContentUri(id), values, null, null);
            }
        }
        public static final void setIcon(ContentResolver cr, String packageName, String className, String extraData, Bitmap bmp) {
            w(TAG, "No supported API");
            long id = getId(cr, packageName, className, extraData);
            ContentValues values = new ContentValues();
            values.put(Apps.ICON, getStreamFromBitmap(bmp));
            if (id == -1) {
                values.put(Apps.PACKAGENAME, packageName);
                values.put(Apps.CLASSNAME, className);
                values.put(Apps.EXTRADATA, extraData);
                cr.insert(Apps.CONTENT_URI, values);
            } else {
                cr.update(getContentUri(id), values, null, null);
            }
        }
        public static final void setIcon(ContentResolver cr, String packageName, String className, String extraData, Drawable drawable) {
            w(TAG, "No supported API");
            long id = getId(cr, packageName, className, extraData);
            ContentValues values = new ContentValues();
            values.put(Apps.ICON, getStreamFromDrawable(drawable));
            if (id == -1) {
                values.put(Apps.PACKAGENAME, packageName);
                values.put(Apps.CLASSNAME, className);
                values.put(Apps.EXTRADATA, extraData);
                cr.insert(Apps.CONTENT_URI, values);
            } else {
                cr.update(getContentUri(id), values, null, null);
            }
        }
        private static final byte [] getStreamFromBitmap(Bitmap bmp) {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bmp.compress(Bitmap.CompressFormat.JPEG, 75, stream);
            return stream.toByteArray();
        }
        private static final byte [] getStreamFromDrawable(Drawable drawable) {
            Bitmap bmp = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bmp);
            drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
            drawable.draw(canvas);
            byte [] data = getStreamFromBitmap(bmp);
            bmp.recycle();
            return data;
        }       
    }
}
