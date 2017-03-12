package com.lightmsg.provider;

import android.content.Context;
import net.sqlcipher.database.SQLiteDatabase;
import net.sqlcipher.database.SQLiteOpenHelper;
import android.util.Log;

public class LMCipherDatabaseHelper extends SQLiteOpenHelper {
    private static final String TAG = LMCipherDatabaseHelper.class.getSimpleName();
    private static LMCipherDatabaseHelper sInstance = null;
    static final String DATABASE_NAME = "lm.db";
    static final int DATABASE_VERSION = 1;
    @SuppressWarnings("unused")
    private final Context mContext;

    public LMCipherDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        mContext = context;
        Log.d(TAG, "LMDatabaseHelper()...");
        SQLiteDatabase.loadLibs(mContext);
    }
    
    @Override
    public void onOpen(SQLiteDatabase db) {
        Log.d(TAG, "onOpen()...");
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d(TAG, "onCreate()...");
        db.execSQL("CREATE TABLE account (" +
                LmProvider._ID+" INTEGER PRIMARY KEY," +
                LmProvider.ACCOUNT+" TEXT," +
                LmProvider.NICK+" TEXT," +
                LmProvider.GENDER+" TEXT DEFAULT \"null\"," +
                LmProvider.PWD+" TEXT," +
                LmProvider.LOGIN+" INTEGER DEFAULT 0," +
                LmProvider.REM_PWD+" INTEGER DEFAULT 1," +
                LmProvider.AUTO_LOG+" INTEGER DEFAULT 1," +
                LmProvider.PORTRAIT+" TEXT DEFAULT \"PORTRAIT IMAGE\"" +
                ");");
        db.execSQL("CREATE TABLE roster (" +
                LmProvider._ID+" INTEGER PRIMARY KEY," +
                LmProvider.UID+" TEXT DEFAULT \"null\"," +
                LmProvider.NAME+" TEXT DEFAULT \"null\"," +
                LmProvider.NICK+" TEXT DEFAULT \"null\"," +
                LmProvider.GENDER+" TEXT DEFAULT \"null\"," +
                LmProvider.REGION+" TEXT DEFAULT \"null\"," +
                LmProvider.GROUP+" TEXT DEFAULT \"null\"," +
                LmProvider.THREAD_ID+" TEXT DEFAULT \"null\"," +
                LmProvider.PORTRAIT+" TEXT DEFAULT \"PORTRAIT IMAGE\"," +
                LmProvider.STATE+" INTEGER DEFAULT 0," +
                LmProvider.SORT+" TEXT DEFAULT \"null\"" +
                ");");
        db.execSQL("CREATE TABLE "+ LmProvider.TABLE_THREAD+" (" +
                LmProvider._ID+" INTEGER PRIMARY KEY," +
                LmProvider.THREAD_ID+" TEXT DEFAULT \"null\"," +
                LmProvider.DATE+" INGEGER DEFAULT 0," +
                LmProvider.UID+" TEXT DEFAULT \"null\"," +
                LmProvider.JID+" TEXT DEFAULT \"null\"," +
                LmProvider.NAME+" TEXT DEFAULT \"null\"," +
                LmProvider.THREAD_SNIPPET+" TEXT DEFAULT \"null\"," +
                LmProvider.UNREAD_COUNT+" INTEGER DEFAULT 0," +
                LmProvider.ERROR+" INTEGER" +
                ");");
        db.execSQL("CREATE TABLE "+ LmProvider.TABLE_GROUP_THREAD+" (" +
                LmProvider._ID+" INTEGER PRIMARY KEY," +
                LmProvider.GROUP+" TEXT DEFAULT \"null\"," +
                LmProvider.DATE+" INGEGER DEFAULT 0," +
                LmProvider.UID+" TEXT DEFAULT \"null\"," +
                LmProvider.JID+" TEXT DEFAULT \"null\"," +
                LmProvider.NAME+" TEXT DEFAULT \"null\"," +
                LmProvider.THREAD_SNIPPET+" TEXT DEFAULT \"null\"," +
                LmProvider.UNREAD_COUNT+" INTEGER DEFAULT 0," +
                LmProvider.ERROR+" INTEGER" +
                ");");
        db.execSQL("CREATE TABLE "+ LmMessageProvider.TABLE_MSG+" (" +
                LmMessageProvider._ID+" INTEGER PRIMARY KEY," +
                LmMessageProvider.UID+" TEXT DEFAULT \"null\"," +
                LmMessageProvider.DATE+" INGEGER DEFAULT 0," +
                //LmMessageProvider.DATE_SENT+" INGEGER DEFAULT 0," +
                LmMessageProvider.MO+" INGEGER DEFAULT 0," +
                LmMessageProvider.JID_TO+" TEXT DEFAULT \"null\"," +
                LmMessageProvider.JID_FROM+" TEXT DEFAULT \"null\"," +
                LmMessageProvider.MSG_TYPE+" TEXT DEFAULT \"chat\"," +
                LmMessageProvider.CONTENT+" TEXT DEFAULT \"null\"," +
                LmMessageProvider.XML+" TEXT DEFAULT \"null\"," +
                LmMessageProvider.READ+" INTEGER DEFAULT 0," +
                LmMessageProvider.SENT+" INTEGER DEFAULT 0," +
                LmMessageProvider.ERROR+" INTEGER DEFAULT 0," +
                LmMessageProvider.MSG_ID+" TEXT DEFAULT \"null\"," +
                LmMessageProvider.THREAD_ID+" TEXT DEFAULT \"null\"" +
                ");");
        db.execSQL("CREATE TABLE pending_msg (" +
                "_id INTEGER PRIMARY KEY," +
                "thread_id TEXT," +
                "date INGEGER," +
                "date_sent INGEGER," +
                "jid TEXT," +
                "content TEXT," +
                "xml TEXT," +
                "error INTEGER" +
                ");");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(TAG, "onUpgrade()...");
    }
    
    static synchronized LMCipherDatabaseHelper getInstance(Context context) {
        Log.d(TAG, "getInstance()...");
        if (sInstance == null) {
            sInstance = new LMCipherDatabaseHelper(context);
        }
        return sInstance;
    }

}
