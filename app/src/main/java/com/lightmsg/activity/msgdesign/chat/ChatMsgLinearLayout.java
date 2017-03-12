package com.lightmsg.activity.msgdesign.chat;


import android.content.Context;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.StateListDrawable;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.lightmsg.R;

import java.util.Arrays;


public class ChatMsgLinearLayout extends LinearLayout {
    private static final String TAG = ChatMsgLinearLayout.class.getSimpleName();

    private boolean bCanHitOnlyOnBubble = true;
    private TextView msg;
    private ImageView msgImg;
    private ImageView portrait;

    public ChatMsgLinearLayout(Context context) {
        this(context, null);
    }

    public ChatMsgLinearLayout(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ChatMsgLinearLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    /*public ChatMsgLinearLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }*/

    @Override
    protected void onFinishInflate() {
        Log.v(TAG, "onFinishInflate()...");
        super.onFinishInflate();
        msg = (TextView) findViewById(R.id.msg);
        msgImg = (ImageView) findViewById(R.id.msg_img);
        portrait = (ImageView) findViewById(R.id.portrait);
    }

    /**
     * Hit rectangle in parent's coordinates
     *
     * @param outRect The hit rectangle of the view.
     */
    @Override
    public void getHitRect(Rect outRect) {
        if (bCanHitOnlyOnBubble) {
            if (msg.getVisibility() == View.VISIBLE) {
                outRect.set(msg.getLeft(), msg.getTop(), msg.getRight(), msg.getBottom());
                Log.v(TAG, "getHitRect(), hit on msg bubble");
                return;
            } else if (msgImg.getVisibility() == View.VISIBLE) {
                outRect.set(msgImg.getLeft(), msgImg.getTop(), msgImg.getRight(), msgImg.getBottom());
                Log.v(TAG, "getHitRect(), hit on msgImg bubble");
                return;
            } else if (portrait.getVisibility() == View.VISIBLE) {
                outRect.set(portrait.getLeft(), portrait.getTop(), portrait.getRight(), portrait.getBottom());
                Log.v(TAG, "getHitRect(), hit on msgImg bubble");
                return;
            }
        }
        super.getHitRect(outRect);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        Log.v(TAG, "dispatchTouchEvent(), ev="+ev);
        float x = ev.getX();
        float y = ev.getY();
        
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                //if (pointInView(msg, x, y, 0)) {
                    //Log.v(TAG, "dispatchTouchEvent(), touch in msg");
                    //Log.v(TAG, "" + msg.getBackground() + ", " + msg.getBackground().isStateful() + ", " + Arrays.toString(msg.getBackground().getState()));
                    //msg.setPressed(true);
                //}
                break;
            case MotionEvent.ACTION_MOVE:
                //if (pointInView(msg, x, y, 0)) {
                    //Log.v(TAG, "dispatchTouchEvent(), touch in msg");
                    //msg.setPressed(true);
                //}
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
            default:
                //msg.setPressed(false);
                break;
        }
        
        return super.dispatchTouchEvent(ev);
    }

    /**
     * Utility method to determine whether the given point, in local coordinates,
     * is inside the view, where the area of the view is expanded by the slop factor.
     * This method is called while processing touch-move events to determine if the event
     * is still within the view.
     *
     * @hide
     */
    public boolean pointInView(View v, float localX, float localY, float slop) {
        return localX >= -slop && localY >= -slop && localX < ((v.getRight() - v.getLeft()) + slop) &&
                localY < ((v.getBottom() - v.getTop()) + slop);
    }
}
