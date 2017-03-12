package com.lightmsg.activity.msgdesign.chat;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.AnimationDrawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;


public class ShyView extends View {
    private final static String TAG = ShyView.class.getSimpleName();
    
    public ShyView(Context context) {
        super(context);
    }

    public ShyView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ShyView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
    

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        Log.v(TAG, "onMeasure(), "+widthMeasureSpec+", "+heightMeasureSpec);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        Log.v(TAG, "onLayout(), "+changed+", "+left+", "+top+", "+right+", "+bottom);
        super.onLayout(changed, left, top, right, bottom);
    }
    
    protected void onDraw(Canvas canvas) {
        Log.v(TAG, "onDraw(), " + canvas);
        super.onDraw(canvas);
        
        ((AnimationDrawable)getBackground()).start();
    }

    /**
     * Called when the visibility of the view or an ancestor of the view has
     * changed.
     *
     * @param changedView The view whose visibility changed. May be
     *                    {@code this} or an ancestor view.
     * @param visibility The new visibility, one of {@link #VISIBLE},
     *                   {@link #INVISIBLE} or {@link #GONE}.
     */
    protected void onVisibilityChanged(View changedView, int visibility) {
        Log.v(TAG, "onVisibilityChanged(), "+changedView+", "+visibility);
        super.onVisibilityChanged(changedView, visibility);
    }

    /**
     * Called when the window containing has change its visibility
     * (between {@link #GONE}, {@link #INVISIBLE}, and {@link #VISIBLE}).  Note
     * that this tells you whether or not your window is being made visible
     * to the window manager; this does <em>not</em> tell you whether or not
     * your window is obscured by other windows on the screen, even if it
     * is itself visible.
     *
     * @param visibility The new visibility of the window.
     */
    protected void onWindowVisibilityChanged(int visibility) {
        Log.v(TAG, "onWindowVisibilityChanged(), "+visibility);
        super.onWindowVisibilityChanged(visibility);
    }
}

