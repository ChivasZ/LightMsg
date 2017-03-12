package com.lightmsg.activity.msgdesign.chat;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ListView;


public class ChatListView extends ListView {
    private static final String TAG = ChatListView.class.getSimpleName();

    public ChatListView(Context context) {
        this(context, null);
    }

    public ChatListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ChatListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    /**
     * 返回false，则事件会向子控件传递；返回true，则调用onTouchEvent方法
         1.  down事件首先会传递到onInterceptTouchEvent()方法 
         2.  如果该ViewGroup的onInterceptTouchEvent()在接收到down事件处理完成之后return false，那么后续的move, up等事件将继续会先传递给该ViewGroup，之后才和down事件一样传递给最终的目标view的onTouchEvent()处理。 
         3.  如果该ViewGroup的onInterceptTouchEvent()在接收到down事件处理完成之后return true，那么后续的move, up等事件将不再传递给onInterceptTouchEvent()，而是和down事件一样传递给该ViewGroup的onTouchEvent()处理，注意，目标view将接收不到任何事件。 
         4.  如果最终需要处理事件的view的onTouchEvent()返回了false，那么该事件将被传递至其上一层次的view的onTouchEvent()处理。 
         5.  如果最终需要处理事件的view 的onTouchEvent()返回了true，那么后续事件将可以继续传递给该view的onTouchEvent()处理。 
     
     * If return false, dispatch to his children;
     * If return true, invoke his onTouchEvent();
     * 
     */
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        Log.v(TAG, "onInterceptTouchEvent(), ev="+ev);
        //if (mOnPreTouchListener != null) {
        //    mOnPreTouchListener.onPreTouch(ev);
        //}
        boolean ret = super.onInterceptTouchEvent(ev);
        Log.v(TAG, "onInterceptTouchEvent(), ret="+ret);
        return ret;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        Log.v(TAG, "onTouchEvent(), ev="+ev);
        //if (mOnPreTouchListener != null) {
        //    mOnPreTouchListener.onPreTouch(ev);
        //}
        boolean ret = super.onTouchEvent(ev);
        Log.v(TAG, "onTouchEvent(), ret="+ret);
        return ret;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        Log.v(TAG, "dispatchTouchEvent(), ev="+ev);
        if (mOnPreTouchListener != null) {
            mOnPreTouchListener.onPreTouch(ev);
        }
        boolean ret = super.dispatchTouchEvent(ev);
        Log.v(TAG, "dispatchTouchEvent(), ret="+ret);
        return ret;
    }

    protected void setOnPreTouchListener(OnPreTouchListener li) {
        mOnPreTouchListener = li;
    }

    OnPreTouchListener mOnPreTouchListener;
    protected interface OnPreTouchListener {
        void onPreTouch(MotionEvent ev);
    }

    /**
     * Return true if the pressed state should be delayed for children or descendants of this
     * ViewGroup. Generally, this should be done for containers that can scroll, such as a List.
     * This prevents the pressed state from appearing when the user is actually trying to scroll
     * the content.
     *
     * The default implementation returns true for compatibility reasons. Subclasses that do
     * not scroll should generally override this method and return false.
     */
    /*@Override
    public boolean shouldDelayChildPressedState() {
        super.shouldDelayChildPressedState();
        printStack();
        return false;
    }

    private void printStack() {
        try {
            throw new Exception();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }*/
}
