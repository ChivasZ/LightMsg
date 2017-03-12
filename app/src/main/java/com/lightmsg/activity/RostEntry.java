package com.lightmsg.activity;


public class RostEntry {
    private String mUser;
    private String mSortName;
    private String mName;
    private String mNick;
    private String mGender;
    private String mRegion;
    private String mGroup;
    private String mThreadId;
    private String mPortrait;
    private int mState;

    public RostEntry(String user, String sort, String name, String nick,
                     String gender, String region, String group,
                     String threadId, String portrait, int state) {
        mUser = user;
        mSortName = sort;
        mName = name;
        mNick = nick;
        mGender = gender;
        mRegion = region;
        mGroup = group;
        mThreadId = threadId;
        mPortrait = portrait;
        mState = state;
    }

    public RostEntry() {
        mUser = "";
        mSortName = "";
        mName = "";
        mNick = "";
        mGender = "";
        mRegion = "";
        mGroup = "";
        mThreadId = "";
        mPortrait = "";
        mState = 0;
    }

    public String getUser() {
        return mUser;
    }
    public String getSortName() {
        return mSortName;
    }
    public String getName() {
        return mName;
    }
    public String getNick() {
        return mNick;
    }
    public String getGender() {
        return mGender;
    }
    public String getRegion() {
        return mRegion;
    }
    public String getGroup() {
        return mGroup;
    }
    public String getThreadId() {
        return mThreadId;
    }
    public String getPortrait() {
        return mPortrait;
    }
    public int getState() {
        return mState;
    }


    public void setUser(String user) {
        mUser = user;
    }
    public void setSortName(String name) {
        mSortName = name;
    }
    public void setName(String name) {
        mName = name;
    }
    public void setNick(String nick) {
        mNick = nick;
    }
    public void setGender(String gender) {
        mGender = gender;
    }
    public void setRegion(String region) {
        mRegion = region;
    }
    public void setGroup(String group) {
        mGroup = group;
    }
    public void setThreadId(String threadId) {
        mThreadId = threadId;
    }
    public void setPortrait(String portrait) {
        mPortrait = portrait;
    }
    public void setState(int state) {
        mState = state;
    }
}