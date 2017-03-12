package com.lightmsg.activity.mall;

import android.content.Context;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class MallViewPager extends ViewPager {

    /**
     * 触摸屏幕时刚落下的点
     */
    private float mFirstPosX;

    public MallViewPager(Context context) {
        super(context);
    }

    public MallViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        final int action = MotionEventCompat.getActionMasked(ev);
        switch (action) {
        case MotionEvent.ACTION_DOWN:
            mFirstPosX = MotionEventCompat.getX(ev, 0);
            break;
        default:
            break;
        }
        // 如果落在ViewPager上的点横坐标大于30，则不让父组件了拦截，交给ViewPager处理。
        if (mFirstPosX > 30)
            this.getParent().requestDisallowInterceptTouchEvent(true);
        return super.dispatchTouchEvent(ev);
    }
}
