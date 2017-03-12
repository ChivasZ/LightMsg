package com.lightmsg.provider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;

/*import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;*/
import net.sqlcipher.database.SQLiteDatabase;
import net.sqlcipher.database.SQLiteOpenHelper;

import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

public class LmProvider extends ContentProvider {
    private static final String TAG = LmProvider.class.getSimpleName();
    
    private SQLiteOpenHelper mSqlOpenHelper = null;
    public static final String AUTHORITY = "com.lightmsg.provider.LmProvider";
    
    public static final String UNDERLINE = "_";
    
    public static final String TABLE_ACCOUNT = "account";
    
    public static final String TABLE_ROSTER = "roster";
    
    // For <thread> table.
    public static final String TABLE_THREAD = "thread";
    
    public static final String TABLE_GROUP_THREAD = "group_thread";
    
    public static final String _ID = "_id";
    public static final String ACCOUNT = "account";
    public static final String PWD = "pwd";
    public static final String REM_PWD = "rem_pwd";
    public static final String AUTO_LOG = "auto_login";
    public static final String LOGIN = "is_login";
    public static final String THREAD_ID = "thread_id";
    public static final String DATE = "date";
    public static final String UID = "uid";
    public static final String GROUP = "grp";
    public static final String JID = "jid";
    public static final String NAME = "name";
    public static final String SORT = "sort";
    public static final String NICK = "nick";
    public static final String GENDER = "gender";
    public static final String REGION = "region";
    public static final String PORTRAIT = "portrait";
    public static final String STATE = "state";
    public static final String THREAD_SNIPPET = "thread_snippet";
    public static final String UNREAD_COUNT = "unread_count";
    public static final String ERROR = "error";
    
    /*public static final int COLUMN_REC_NO = 0;
    public static final int COLUMN_ID = 1;
    public static final int COLUMN_THREAD_ID = 2;
    public static final int COLUMN_DATE = 3;
    public static final int COLUMN_JID = 4;
    public static final int COLUMN_NAME = 5;
    public static final int COLUMN_THREAD_SNIPPET = 6;
    public static final int COLUMN_UNREAD_COUNT = 7;
    public static final int COLUMN_ERROR = 8;*/
    
    // For <account> table.
    private static final int URI_ACCOUNT			= 0;
    private static final int URI_ACCOUNT_ID			= 0;
    
    // For <thread> table.
    private static final int URI_THREAD				= 1;
    private static final int URI_THREAD_UID			= 2;
    private static final int URI_THREAD_GROUP		= 3;
    private static final int URI_THREAD_GROUPID		= 4;
    
    // For <roster> table.
    private static final int URI_ROSTER				= 5;
    private static final int URI_ROSTER_UID			= 6;
    private static final int URI_THREAD_ID			= 7;
    private static final int URI_CONVERSATIONS		= 8;
    private static final int URI_MESSAGE			= 9;
    private static final int URI_MESSAGES			= 10;
    private static final UriMatcher URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);
    
    
    static {
        // For <account> table.
        URI_MATCHER.addURI(AUTHORITY, TABLE_ACCOUNT, URI_ACCOUNT);
        URI_MATCHER.addURI(AUTHORITY, TABLE_ACCOUNT+"/*", URI_ACCOUNT_ID);
        
        // For <thread> table.
        URI_MATCHER.addURI(AUTHORITY, TABLE_THREAD, URI_THREAD);
        URI_MATCHER.addURI(AUTHORITY, TABLE_THREAD+UNDERLINE+UID+"/*", URI_THREAD_UID);
        URI_MATCHER.addURI(AUTHORITY, TABLE_GROUP_THREAD+UNDERLINE+GROUP+"/*", URI_THREAD_GROUPID);
        URI_MATCHER.addURI(AUTHORITY, TABLE_GROUP_THREAD, URI_THREAD_GROUP);
        
        // For <roster> table.
        URI_MATCHER.addURI(AUTHORITY, TABLE_ROSTER, URI_ROSTER);
        URI_MATCHER.addURI(AUTHORITY, TABLE_ROSTER+UNDERLINE+UID+"/*", URI_ROSTER_UID);
    }
    
    private static final String NO_DELETES_INSERTS_OR_UPDATES = 
            "LmProvider does not support DELETE, INSERT, or UPDATE for this URI: ";
    
    
    public static final String ACTION_MESSAGE_STATE_CHANGE = "com.smartcommunity.LmProvider.action.MESSAGE_STATE_CHANGE";
    
    @Override
    public boolean onCreate() {
        Log.d(TAG, "onCreate()...");
        mSqlOpenHelper = LMCipherDatabaseHelper.getInstance(getContext());
        return false;
    }
    
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        Log.d(TAG, "delete()..."+uri);
        return 0;
    }

    @Override
    public String getType(Uri uri) {
        Log.d(TAG, "getType()..."+uri);
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        Log.d(TAG, "insert()...uri="+uri+" match: "+URI_MATCHER.match(uri));
        if (URI_MATCHER.match(uri) == URI_ROSTER) {
            SQLiteDatabase db = mSqlOpenHelper.getWritableDatabase(getPassword());
            long rowId = db.insert("roster", null, values);
            return Uri.parse(uri+"/"+rowId);
        } else if (URI_MATCHER.match(uri) == URI_THREAD) {
            SQLiteDatabase db = mSqlOpenHelper.getWritableDatabase(getPassword());
            long rowId = db.insert(TABLE_THREAD, null, values);
            return Uri.parse(uri+"/"+rowId);
        } else if (URI_MATCHER.match(uri) == URI_ACCOUNT) {
            SQLiteDatabase db = mSqlOpenHelper.getWritableDatabase(getPassword());
            
            //db.execSQL("truncate table "+TABLE_ACCOUNT);
            //Check if exists firstly.
            Cursor c = null;
            long rowId;
            try {
                c = query(uri, null, null, null, null);
                if (c != null && c.getCount() == 1) {
                    Log.d(TAG, "insert(URI_ACCOUNT)...count=1");
                    if (c.moveToFirst()) {
                        //String uid = c.getString(c.getColumnIndexOrThrow(LmProvider.ACCOUNT));
                        //uri = Uri.withAppendedPath(uri, uid);
                        rowId = update(uri, values, null, null);
                        return Uri.parse(uri+"/"+rowId);
                    }
                } if (c != null && c.getCount() == 0) {
                    Log.d(TAG, "insert(URI_ACCOUNT)...count=0");
                    rowId = db.insert(TABLE_ACCOUNT, null, values);
                    return Uri.parse(uri+"/"+rowId);
                } else {
                    Log.e(TAG, "Wrong DB state!!! COUNT="+c.getCount()+"when query URI_ACCOUNT, maybe multiple entry for same user.");
                }
            } catch(Exception e) {
                e.printStackTrace();
            }finally{
                if (c != null) {
                    c.close();
                }
            }
        } else if (URI_MATCHER.match(uri) == URI_ROSTER_UID) {
            SQLiteDatabase db = mSqlOpenHelper.getWritableDatabase(getPassword());
            
            //Check if exists firstly.
            Cursor c = null;
            long rowId;
            try {
                c = query(uri, null, null, null, null);
                if (c != null && c.getCount() == 1) {
                    Log.d(TAG, "insert(URI_ROSTER_UID)...count=1");
                    if (c.moveToFirst()) {
                        rowId = update(uri, values, null, null);
                        return Uri.parse(uri+"/"+rowId);
                    }
                } if (c != null && c.getCount() == 0) {
                    Log.d(TAG, "insert(URI_ROSTER_UID)...count=0");
                    rowId = db.insert(TABLE_ROSTER, null, values);
                    return Uri.parse(uri+"/"+rowId);
                } else {
                    Log.e(TAG, "Wrong DB state!!! COUNT="+c.getCount()+"when query URI_THREAD_UID, maybe multiple entry for same user.");
                }
            } catch(Exception e) {
                e.printStackTrace();
            }finally{
                if (c != null) {
                    c.close();
                }
            }
        } else if (URI_MATCHER.match(uri) == URI_THREAD_UID) {
            SQLiteDatabase db = mSqlOpenHelper.getWritableDatabase(getPassword());
            
            //Check if exists firstly.
            Cursor c = null;
            long rowId;
            try {
                c = query(uri, null, null, null, null);
                if (c != null && c.getCount() == 1) {
                    if (c.moveToFirst()) {
                        rowId = update(uri, values, null, null);
                        return Uri.parse(uri+"/"+rowId);
                    }
                } if (c != null && c.getCount() == 0) {
                    rowId = db.insert(TABLE_THREAD, null, values);
                    return Uri.parse(uri+"/"+rowId);
                } else {
                    Log.e(TAG, "Wrong DB state!!! COUNT="+c.getCount()+"when query URI_THREAD_UID, maybe multiple entry for same user.");
                }
            } catch(Exception e) {
                e.printStackTrace();
            }finally{
                if (c != null) {
                    c.close();
                }
            }
        } else if (URI_MATCHER.match(uri) == URI_THREAD_GROUPID) {
            SQLiteDatabase db = mSqlOpenHelper.getWritableDatabase(getPassword());
            
            //Check if exists firstly.
            Cursor c = null;
            long rowId;
            try {
                c = query(uri, null, null, null, null);
                if (c != null && c.getCount() == 1) {
                    if (c.moveToFirst()) {
                        rowId = update(uri, values, null, null);
                        return Uri.parse(uri+"/"+rowId);
                    }
                } if (c != null && c.getCount() == 0) {
                    rowId = db.insert(TABLE_GROUP_THREAD, null, values);
                    return Uri.parse(uri+"/"+rowId);
                } else {
                    Log.e(TAG, "Wrong DB state!!! COUNT="+c.getCount()+"when query URI_THREAD_GROUP, maybe multiple entry for same user.");
                }
            } catch(Exception e) {
                e.printStackTrace();
            }finally{
                if (c != null) {
                    c.close();
                }
            }
        }
        throw new UnsupportedOperationException(NO_DELETES_INSERTS_OR_UPDATES + uri);
    }


    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
            String sortOrder) {
        Log.d(TAG, "query()..."+uri+" match: "+URI_MATCHER.match(uri));
        String extraSelection = null;
        String finalSelection = null;
        if (URI_MATCHER.match(uri) == URI_ROSTER) {
            SQLiteDatabase db = mSqlOpenHelper.getWritableDatabase(getPassword());
            Cursor ret = db.query(TABLE_ROSTER, projection, selection, selectionArgs, null, null, sortOrder);
            return ret;
        } else	if (URI_MATCHER.match(uri) == URI_THREAD) {
            SQLiteDatabase db = mSqlOpenHelper.getWritableDatabase(getPassword());
            Cursor ret = db.query(TABLE_THREAD, projection, selection, selectionArgs, null, null, sortOrder);
            return ret;
        } else	if (URI_MATCHER.match(uri) == URI_ACCOUNT) {
            SQLiteDatabase db = mSqlOpenHelper.getWritableDatabase(getPassword());
            Cursor ret = db.query(TABLE_ACCOUNT, projection, selection, selectionArgs, null, null, sortOrder);
            return ret;
        } else if (URI_MATCHER.match(uri) == URI_ROSTER_UID) {
            
            SQLiteDatabase db = mSqlOpenHelper.getWritableDatabase(getPassword());
            String uid = uri.getLastPathSegment();
            extraSelection =  UID + "=\"" + uid+"\"";
            if (selection != null) {
                finalSelection = selection + extraSelection;
            } else {
                finalSelection = extraSelection;
            }
            Cursor ret = db.query(TABLE_ROSTER, projection, finalSelection, selectionArgs, null, null, sortOrder);
            return ret;
        } else if (URI_MATCHER.match(uri) == URI_THREAD_UID) {
            
            SQLiteDatabase db = mSqlOpenHelper.getWritableDatabase(getPassword());
            String uid = uri.getLastPathSegment();
            extraSelection =  UID + "=\"" + uid+"\"";
            if (selection != null) {
                finalSelection = selection + extraSelection;
            } else {
                finalSelection = extraSelection;
            }
            Cursor ret = db.query(TABLE_THREAD, projection, finalSelection, selectionArgs, null, null, sortOrder);
            return ret;
        } else if (URI_MATCHER.match(uri) == URI_THREAD_GROUP) {
            
            SQLiteDatabase db = mSqlOpenHelper.getWritableDatabase(getPassword());
            //String group = uri.getLastPathSegment();
            //extraSelection =  GROUP + "=\"" + group+"\"";
            //if (selection != null) {
                finalSelection = selection; //+ extraSelection;
            //} else {
                //finalSelection = extraSelection;
            //}
            Cursor ret = db.query(TABLE_GROUP_THREAD, projection, finalSelection, selectionArgs, null, null, sortOrder);
            return ret;
        } else if (URI_MATCHER.match(uri) == URI_THREAD_GROUPID) {
            
            SQLiteDatabase db = mSqlOpenHelper.getWritableDatabase(getPassword());
            String group = uri.getLastPathSegment();
            extraSelection =  GROUP + "=\"" + group+"\"";
            if (selection != null) {
                finalSelection = selection + extraSelection;
            } else {
                finalSelection = extraSelection;
            }
            Cursor ret = db.query(TABLE_GROUP_THREAD, projection, finalSelection, selectionArgs, null, null, sortOrder);
            return ret;
        }
        throw new UnsupportedOperationException(NO_DELETES_INSERTS_OR_UPDATES + uri);
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        Log.d(TAG, "update()...uri="+uri);
        String extraSelection = null;
        String finalSelection = null;
        if (URI_MATCHER.match(uri) == URI_ROSTER_UID) {
            SQLiteDatabase db = mSqlOpenHelper.getWritableDatabase(getPassword());
            String uid = uri.getLastPathSegment();
            extraSelection =  UID + "=\"" + uid+"\"";
            if (selection != null) {
                finalSelection = selection + extraSelection;
            } else {
                finalSelection = extraSelection;
            }
            //Log.d(TAG, "update(URI_ROSTER_UID), values="+values+", finalSelection="+finalSelection);
            int ret = db.update(TABLE_ROSTER, values, finalSelection, selectionArgs);
            return ret;
        } else if (URI_MATCHER.match(uri) == URI_THREAD_UID) {
            SQLiteDatabase db = mSqlOpenHelper.getWritableDatabase(getPassword());
            String uid = uri.getLastPathSegment();
            extraSelection =  UID + "=\"" + uid+"\"";
            if (selection != null) {
                finalSelection = selection + extraSelection;
            } else {
                finalSelection = extraSelection;
            }
            int ret = db.update(TABLE_THREAD, values, finalSelection, selectionArgs);
            return ret;
        } else if (URI_MATCHER.match(uri) == URI_THREAD_GROUPID) {
            SQLiteDatabase db = mSqlOpenHelper.getWritableDatabase(getPassword());
            String group = uri.getLastPathSegment();
            extraSelection =  GROUP + "=\"" + group+"\"";
            if (selection != null) {
                finalSelection = selection + extraSelection;
            } else {
                finalSelection = extraSelection;
            }
            int ret = db.update(TABLE_GROUP_THREAD, values, finalSelection, selectionArgs);
            return ret;
        } else if (URI_MATCHER.match(uri) == URI_ACCOUNT) {
            SQLiteDatabase db = mSqlOpenHelper.getWritableDatabase(getPassword());
            String account = uri.getLastPathSegment();
            extraSelection =  _ID + "=1";
            if (selection != null) {
                finalSelection = selection + extraSelection;
            } else {
                finalSelection = extraSelection;
            }
            int ret = db.update(TABLE_ACCOUNT, values, finalSelection, selectionArgs);
            return ret;
        } else if (URI_MATCHER.match(uri) == URI_ACCOUNT_ID) {
            SQLiteDatabase db = mSqlOpenHelper.getWritableDatabase(getPassword());
            String uid = uri.getLastPathSegment();
            extraSelection =  ACCOUNT + "=\"" + uid +"\"";
            if (selection != null) {
                finalSelection = selection + extraSelection;
            } else {
                finalSelection = extraSelection;
            }
            int ret = db.update(TABLE_ACCOUNT, values, finalSelection, selectionArgs);
            return ret;
        }
        throw new UnsupportedOperationException(NO_DELETES_INSERTS_OR_UPDATES + uri);
    }

    @SuppressWarnings("unused")
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
