package com.lightmsg.provider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;

/*import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;*/
import net.sqlcipher.database.SQLiteDatabase;
import net.sqlcipher.database.SQLiteException;
import net.sqlcipher.database.SQLiteOpenHelper;

import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import com.lightmsg.util.SqliteWrapper;

public class LmMessageProvider extends ContentProvider {
    private static final String TAG = LmMessageProvider.class.getSimpleName();
    
    public static final String UNDERLINE = "_";
    
    private SQLiteOpenHelper mSqlOpenHelper = null;

    public static final String TABLE_MSG = "msg";
    public static final String AUTHORITY = "com.lightmsg.provider.LmMessageProvider";
    public static final String _ID = "_id";
    public static final String MSG_ID = "msg_id";
    public static final String THREAD_ID = "thread_id";
    public static final String DATE = "date";
    //public static final String DATE_SENT = "date_sent";
    public static final String MO = "mo";
    public static final String JID_TO = "jid_to";
    public static final String JID_FROM = "jid_from";
    public static final String UID = "uid";
    public static final String MSG_TYPE = "msg_type";
    public static final String CONTENT = "content";
    public static final String XML = "xml";
    public static final String READ = "read";
    public static final String SENT = "sent";
    public static final String ERROR = "error";
    
    private static final int URI_MSG				= 0;
    private static final int URI_MSG_ID				= 1;
    private static final int URI_THREAD_ID			= 2;
    private static final int URI_UID			= 3;
    private static final int URI_JID_FROM			= 4;
    private static final int URI_JID_TO				= 5;

    private static final UriMatcher URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);
    static {
        URI_MATCHER.addURI(AUTHORITY, TABLE_MSG, URI_MSG);
        URI_MATCHER.addURI(AUTHORITY, UID+"/*", URI_UID);
        URI_MATCHER.addURI(AUTHORITY, JID_FROM+"/*", URI_JID_FROM);
        URI_MATCHER.addURI(AUTHORITY, JID_TO+"/*", URI_JID_TO);
    }
    
    private static final String NO_DELETES_INSERTS_OR_UPDATES = 
            "LmMessageProvider does not support DELETE, INSERT, or UPDATE for this URI: ";
    
    public String[] mProjAll =
        {
            _ID,
            MSG_ID,
            THREAD_ID,
            DATE,
            //DATE_SENT,
            MO,
            JID_TO,
            JID_FROM,
            UID,
            MSG_TYPE,
            CONTENT,
            XML,
            READ,
            SENT,
            ERROR
        };
    public String mSelectionJid = UID+" = ?";
    public String[] mSelectionJidArgs = {""};

    
    public static final String ACTION_MESSAGE_STATE_CHANGE = "com.smartcommunity.LmProvider.action.MESSAGE_STATE_CHANGE";
    
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        Log.d(TAG, "delete()...");
        SQLiteDatabase db = mSqlOpenHelper.getWritableDatabase(getPassword());
        return db.delete(TABLE_MSG, selection, selectionArgs);
    }

    @Override
    public String getType(Uri arg0) {
        Log.d(TAG, "getType()...");
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        Log.d(TAG, "insert()...");
        if (URI_MATCHER.match(uri) == URI_MSG) {
            SQLiteDatabase db = mSqlOpenHelper.getWritableDatabase(getPassword());
            long rowId = db.insert(TABLE_MSG, null, values);
            return Uri.parse(uri+"/"+rowId);
        }
        throw new UnsupportedOperationException(NO_DELETES_INSERTS_OR_UPDATES + uri);
    }

    @Override
    public boolean onCreate() {
        Log.d(TAG, "onCreate()...");
        //mSqlOpenHelper = LMDatabaseHelper.getInstance(getContext());
        mSqlOpenHelper = LMCipherDatabaseHelper.getInstance(getContext());
        return false;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
            String sortOrder) {
        String extraSelection = null;
        String finalSelection = null;
        String limit = SqliteWrapper.getLimit();
        Log.d(TAG, "query(), uri="+uri+", limit="+limit);
        
        if (URI_MATCHER.match(uri) == URI_MSG) {
            SQLiteDatabase db = mSqlOpenHelper.getWritableDatabase(getPassword());
            Cursor ret = db.query(false, TABLE_MSG, projection, selection, selectionArgs, null, null, sortOrder, limit);
            return ret;
        } else if (URI_MATCHER.match(uri) == URI_UID) {
            String uid = uri.getLastPathSegment();
            extraSelection = UID + "=\"" + uid + "\"" ;
            finalSelection = concatSelections(selection, extraSelection);
            SQLiteDatabase db = mSqlOpenHelper.getWritableDatabase(getPassword());
            Cursor ret = null;
            try {
                ret = db.query(false, TABLE_MSG, projection, finalSelection, selectionArgs, null, null, sortOrder, limit);
            } catch (SQLiteException e) {
                e.printStackTrace();
                ret = null;
            } finally {
                
            }
            return ret;
        } else if (URI_MATCHER.match(uri) == URI_JID_FROM) {
            String jid = uri.getLastPathSegment();
            extraSelection = JID_FROM + "=" + jid;
            finalSelection = concatSelections(selection, extraSelection);
            SQLiteDatabase db = mSqlOpenHelper.getWritableDatabase(getPassword());
            Cursor ret = db.query(false, TABLE_MSG, projection, finalSelection, selectionArgs, null, null, sortOrder, limit);
            return ret;
        }
        throw new UnsupportedOperationException(NO_DELETES_INSERTS_OR_UPDATES + uri);
    }

    @Override
    public int update(Uri uri, ContentValues value, String selection, String[] selectionArgs) {
        Log.d(TAG, "update()...");

        if (URI_MATCHER.match(uri) == URI_MSG) {
            SQLiteDatabase db = mSqlOpenHelper.getWritableDatabase(getPassword());

            int ret = db.update(TABLE_MSG, value, selection, selectionArgs);
            return ret;
        }
        throw new UnsupportedOperationException(NO_DELETES_INSERTS_OR_UPDATES + uri);
    }
    
    private static String concatSelections(String selection1, String selection2) {
        if (TextUtils.isEmpty(selection1)) {
            return selection2;
        } else if (TextUtils.isEmpty(selection2)) {
            return selection1;
        } else {
            return selection1 + " AND " + selection2;
        }
    }

    private String getPassword() {
        return "123456";
    }
}
