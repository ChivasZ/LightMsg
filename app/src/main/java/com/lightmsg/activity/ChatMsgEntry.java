package com.lightmsg.activity;

/**
 * Created by ZhangQh on 2016/3/14.
 */
public class ChatMsgEntry {
    private String mMsgId;
    private String mName;
    private String mDate;
    private String mMsg;
    private long mDateLong;
    private boolean mbFrom;

    private boolean mbFile;
    private String mFile;

    

    public ChatMsgEntry(String msgId, String name, String msg, String date, boolean bFrom, boolean bFile, String file) {
        mMsgId = msgId;
        mName = name;
        mDate = date;
        mMsg = msg;
        mbFile = bFile;
        mbFrom = bFrom;
        mFile = file;
    }

    public ChatMsgEntry() {
        mMsgId = "";
        mName = "";
        mDate = "";
        mMsg = "";
        mbFrom = false;

        mbFile = false;
        mFile = "";
    }

    public void setMsgId(String msgId) {
        mMsgId = msgId;
    }
    
    public void setName(String name) {
        mName = name;
    }

    public void setDate(String date) {
        mDate = date;
    }

    public void setDateLong(long date) {
        mDateLong = date;
    }

    public void setMsg(String msg) {
        mMsg = msg;
    }

    public void setFileType(boolean bFile) {
        mbFile = bFile;
    }

    public void setFrom(boolean bFrom) {
        mbFrom = bFrom;
    }

    public void setFile(String file) { mFile = file;}

    public String getMsgId() {
        return mMsgId;
    }
    
    public String getName() {
        return mName;
    }

    public String getDate() {
        return mDate;
    }
    
    public long getDateLong() {
        return mDateLong;
    }
    
    public String getMsg() {
        return mMsg;
    }

    public String getFile() {
        return mFile;
    }

    public boolean isFileType() {
        return mbFile;
    }

    public boolean isFrom() {
        return mbFrom;
    }
}
