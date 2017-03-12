package com.lightmsg.activity;

public class ConvEntry {
    private String mThreadId;
    private String mName;
    private String mUser;
    private String mSnippet;
    private String mDate;
    private int mUnreadCount;
    private boolean mError;
    //private ListItemViewHolder mLivh = null;

    public ConvEntry(String threadId, String name, String user, String snippet, String date, int unreadCount, boolean error) {
        mThreadId = threadId;
        mName = name;
        mUser = user;
        mSnippet = snippet;
        mDate = date;
        mUnreadCount = unreadCount;
        mError = error;

        //mLivh = null;
    }

    public ConvEntry() {
        mThreadId = "";
        mName = "";
        mUser = "";
        mSnippet = "";
        mDate = "";
        mUnreadCount = 0;
        mError = false;

        //mLivh = null;
    }

    public String getThreadId() {
        return mThreadId;
    }

    public String getName() {
        return mName;
    }

    public String getUser() {
        return mUser;
    }

    public String getSnippet() {
        return mSnippet;
    }

    public String getDate() {
        return mDate;
    }

    public int getUnreadCount() {
        return mUnreadCount;
    }

    public boolean getError() {
        return mError;
    }

    //public ListItemViewHolder getBv() {
    //	mLivh;
    //}

    public void setThreadId(String threadId) {
        mThreadId = threadId;
    }

    public void setName(String name) {
        mName = name;
    }

    public void setUser(String user) {
        mUser = user;
    }

    public void setSnippet(String snippet) {
        mSnippet = snippet;
    }

    public void setDate(String date) {
        mDate = date;
    }

    public void setUnreadCount(int unreadCount) {
        mUnreadCount = unreadCount;
    }

    public void setError(boolean error) {
        mError = error;
    }

    //public void setBv(ListItemViewHolder livh) {
    //	mLivh = livh;
    //}
}