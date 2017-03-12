package com.lightmsg.activity.mall;

import java.util.List;

import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;

public class MallViewPagerAdaper extends PagerAdapter {

    private List<View> mList;

    public MallViewPagerAdaper(List<View> list) {
        mList = list;
    }

    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return mList.size();
    }

    @Override
    public boolean isViewFromObject(View arg0, Object arg1) {
        return arg0 == arg1;
    }

    @Override
    public void destroyItem(View container, int position, Object object) {
        // TODO Auto-generated method stub
        ((ViewPager) container).removeView(mList.get(position));
    }

    @Override
    public Object instantiateItem(View container, int position) {
        ViewGroup group = (ViewGroup) mList.get(position).getParent();
        if (group != null) {
            group.removeView(mList.get(position));
        }
        ((ViewPager) container).addView(mList.get(position));
        return mList.get(position);
    }

}
