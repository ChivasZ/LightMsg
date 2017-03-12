package com.lightmsg.activity;


import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.TextView;

import com.lightmsg.R;
import com.lightmsg.http.HttpLoader;
import com.lightmsg.service.CoreService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


public class UpperPagerViewFragment extends Fragment {

    private static final String TAG = UpperPagerViewFragment.class.getSimpleName();
    private static final boolean DEBUG = true;
    private static final String SERVER_DOMAIN = CoreService.getServer();
    private static final String SERVER_PORT = "8080";
    
    private static final String SERVER_REL_HOST = SERVER_DOMAIN+":"+SERVER_PORT;
    private static final String SERVER_EMU_LOCAL_HOST = "10.0.2.2"+":"+SERVER_PORT;
    private static final String SERVER_HOST = SERVER_REL_HOST;//SERVER_EMU_LOCAL_HOST;
    
    private static final String SERVER_HOST_FOLDER = "advertise";

    private Activity activity;

    private static final HttpLoader http = new HttpLoader();
    private String url;

    private View baseView;
    private UpperViewPager viewPager;
    private List<ImageView> imageViews;
    UpperPagerAdapter upperAdapter;

    private String[] imageTitles;
    private String[] imageResUrl;
    private String[] imageOrigTitles;
    private String[] imageOrigResUrl;
    private Bitmap[] imageBitmap;
    private int[] imageResIds;
    private AsyncTask<Object, Integer, Long> loader;

    private List<View> dots;
    private TextView tv_title;

    private int currentItem = 0;

    private GestureDetector gd;

    // An ExecutorService that can schedule commands to run after a given delay,
    // or to execute periodically.
    private ScheduledExecutorService scheduledExecutorService;

    private Handler handler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            viewPager.setCurrentItem(currentItem);
        };
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        Log.v(TAG, "onCreateView()...");
        baseView = inflater.inflate(R.layout.upper_pager_view, container, false);
        return baseView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        Log.v(TAG, "onActivityCreated()...");
        super.onActivityCreated(savedInstanceState);
        activity = getActivity();

        dots = new ArrayList<View>();
        dots.add(baseView.findViewById(R.id.v_dot0));
        dots.add(baseView.findViewById(R.id.v_dot1));
        dots.add(baseView.findViewById(R.id.v_dot2));
        dots.add(baseView.findViewById(R.id.v_dot3));
        dots.add(baseView.findViewById(R.id.v_dot4));

        tv_title = (TextView) baseView.findViewById(R.id.tv_title);

        try {
            load();
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
        }
        
        viewPager = (UpperViewPager) baseView.findViewById(R.id.vpr);
        upperAdapter = new UpperPagerAdapter();
        viewPager.setAdapter(upperAdapter);
        viewPager.setOnPageChangeListener(new UpperOnPageChangeListener());

        gd = new GestureDetector(activity, new UpperGestureListener());
        viewPager.setOnTouchListener(new OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // TODO Auto-generated method stub
                gd.onTouchEvent(event);
                return true;
            }

        });
    }

    ArrayList<HashMap<String, Object>> list;
    public void load() {
        Log.v(TAG, "load()...");
        
        // Image init.
        imageResIds = //new int[] { R.drawable.a, R.drawable.b, R.drawable.c, R.drawable.d, R.drawable.e };
                new int[] { R.drawable.bg, R.drawable.bg, R.drawable.bg, R.drawable.bg, R.drawable.bg };
        imageResUrl = new String[imageResIds.length];
        imageOrigResUrl = new String[imageResIds.length];
        imageBitmap = new Bitmap[imageResIds.length];
        imageViews = new ArrayList<ImageView>();
        for (int i = 0; i < imageResIds.length; i++) {
            ImageView imageView = new ImageView(activity);
            imageView.setImageResource(imageResIds[i]);
            imageView.setScaleType(ScaleType.CENTER_CROP);
            imageViews.add(imageView);
        }

        //Title init.
        imageTitles = new String[imageResIds.length];
        imageTitles[0] = "A股现史上最大停牌潮 两市逾1700股跌停";
        imageTitles[1] = "全国首家萤火虫主题公园今日落户**";
        imageTitles[2] = "广东惠州惊现20米高火龙卷";
        imageTitles[3] = "图说“七·七事变”";
        imageTitles[4] = "三情况或引爆中日战争 钓岛危机升级最可能";
        tv_title.setText(imageTitles[0]);
        
        //Reinitialize them from server.
        /*String json = http.doGet(url);
        try {
            list = parse(json);
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        Iterator iterator = list.iterator();
        HashMap<String, Object> map;
        int num = 0;
        while (iterator.hasNext()) {
            map = (HashMap<String, Object>)iterator.next();
            imageResUrl[num] = (String)map.get("icon");
            imageTitles[num] = (String)map.get("description");
            num++;
        }*/
        
        //Test
        imageOrigResUrl[0] = "http://"+SERVER_HOST+"/"+SERVER_HOST_FOLDER+"/ADVERTISEMENT_ORIG_PIC_1.jpg";
        imageOrigResUrl[1] = "http://"+SERVER_HOST+"/"+SERVER_HOST_FOLDER+"/ADVERTISEMENT_ORIG_PIC_2.jpg";
        imageOrigResUrl[2] = "http://"+SERVER_HOST+"/"+SERVER_HOST_FOLDER+"/ADVERTISEMENT_ORIG_PIC_3.jpg";
        imageOrigResUrl[3] = "http://"+SERVER_HOST+"/"+SERVER_HOST_FOLDER+"/ADVERTISEMENT_ORIG_PIC_4.jpg";
        imageOrigResUrl[4] = "http://"+SERVER_HOST+"/"+SERVER_HOST_FOLDER+"/ADVERTISEMENT_ORIG_PIC_5.jpg";
        
        imageResUrl[0] = "http://"+SERVER_HOST+"/"+SERVER_HOST_FOLDER+"/ADVERTISEMENT_UPLOAD_PIC_1.jpg";
        imageResUrl[1] = "http://"+SERVER_HOST+"/"+SERVER_HOST_FOLDER+"/ADVERTISEMENT_UPLOAD_PIC_2.jpg";
        imageResUrl[2] = "http://"+SERVER_HOST+"/"+SERVER_HOST_FOLDER+"/ADVERTISEMENT_UPLOAD_PIC_3.jpg";
        imageResUrl[3] = "http://"+SERVER_HOST+"/"+SERVER_HOST_FOLDER+"/ADVERTISEMENT_UPLOAD_PIC_4.jpg";
        imageResUrl[4] = "http://"+SERVER_HOST+"/"+SERVER_HOST_FOLDER+"/ADVERTISEMENT_UPLOAD_PIC_5.jpg";

        loader = new AsyncTask<Object, Integer, Long>() {

            @Override
            protected Long doInBackground(Object... arg0) {
                // TODO Auto-generated method stub
                if (DEBUG)
                    Log.d(TAG, "doInBackground()");
                for (int i = 0; i < imageResIds.length; i++) {
                    Bitmap bitmap = getBitmap(imageResUrl[i]);
                    if (bitmap == null) {
                        bitmap = getBitmap(imageOrigResUrl[i]);
                    }

                    if (bitmap != null) {
                        imageBitmap[i] = bitmap;
                    }
                }
                return 0L;
            }
            
            protected void onProgressUpdate(Integer... progress) {
            }

            protected void onPostExecute(Long result) {
                if (DEBUG)
                    Log.d(TAG, "onPostExecute()");
                for (int i = 0; i < imageResIds.length; i++) {
                    ImageView imageView = new ImageView(activity);
                    if (imageBitmap[i] != null) {
                        imageView.setImageBitmap(imageBitmap[i]);
                        imageView.setScaleType(ScaleType.FIT_XY);
                        imageViews.remove(i);
                        imageViews.add(i, imageView);
                    }
                }
                //viewPager.setAdapter(upperAdapter);
                upperAdapter.notifyDataSetChanged();
            }
        }.execute(null, null, null);
    }

    public Bitmap getBitmap(String url) {
        URL image = null;
        Bitmap bitmap = null;
        
        if (DEBUG)
            Log.d(TAG, "url="+url);
        try {
            image = new URL(url);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        try {
            HttpURLConnection conn = (HttpURLConnection) image.openConnection();
            conn.setDoInput(true);
            conn.connect();
            InputStream is = conn.getInputStream();
            bitmap = BitmapFactory.decodeStream(is);
            is.close();
        } catch (FileNotFoundException e) {
            Log.d(TAG, "Not upload, or original image not setup! url="+url);
        } catch (OutOfMemoryError e) {
            Log.d(TAG, "Need check the image source size, OutOfMemoryError for URL:"+url);
            //e.printStackTrace();
        } catch (Exception e) {
            Log.d(TAG, "Load failed, "+e+", url="+url);
            e.printStackTrace();
        }
        
        if (DEBUG)
            Log.d(TAG, "getBitmap(), bitmap="+bitmap);
        return bitmap;
    }


    /**
     * Parse JSON String.
     * 
     * @throws JSONException
     */
    protected ArrayList<HashMap<String, Object>> parse(String jsonStr)
            throws JSONException {
        JSONArray jsonArray = null;
        ArrayList<HashMap<String, Object>> list = new ArrayList<HashMap<String, Object>>();
        jsonArray = new JSONArray(jsonStr);
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            HashMap<String, Object> map = new HashMap<String, Object>();
            map.put("id", jsonObject.getString("id"));
            map.put("name", jsonObject.getString("name"));
            map.put("icon", jsonObject.getString("icon"));
            map.put("description", jsonObject.getString("description"));
            map.put("action", jsonObject.getString("action"));
            list.add(map);
        }
        return list;
    }

    @Override
    public void onStart() {
        scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        scheduledExecutorService.scheduleAtFixedRate(new ScrollTask(), 1, 3, TimeUnit.SECONDS);
        super.onStart();
    }

    @Override
    public void onStop() {
        scheduledExecutorService.shutdown();
        super.onStop();
    }

    public static class UpperViewPager extends ViewPager {

        //public UpperViewPager() {
        //	// TODO Auto-generated constructor stub
        //}

        public UpperViewPager(Context context) {
            super(context);
            // TODO Auto-generated constructor stub
        }

        public UpperViewPager(Context context, AttributeSet attrs) {
            super(context, attrs);
            // TODO Auto-generated constructor stub
        }

        @Override
        public boolean onInterceptTouchEvent(MotionEvent me) {
            if (DEBUG)
                Log.d(TAG, "onInterceptTouchEvent(), "+me);
            return super.onInterceptTouchEvent(me);
        }

        @Override
        public boolean onTouchEvent(MotionEvent me) {
            if (DEBUG)
                Log.d(TAG, "onTouchEvent(), "+me);
            return super.onTouchEvent(me);
        }
    }

    private class UpperGestureListener extends SimpleOnGestureListener {
        @Override
        public boolean onDown(MotionEvent arg0) {
            if (DEBUG)
                Log.v(TAG, "GestureDetectorListener.onDown()...");
            return false;
        }

        @Override
        public boolean onFling(MotionEvent before, MotionEvent current, float velocityX,
                float velocityY) {
            if (DEBUG)
                Log.v(TAG, "GestureDetectorListener.onFling()...");
            if (before.getX() - current.getX() > /*120*/60) {//Left fling.
                if (DEBUG)
                    Log.v(TAG, "GestureDetectorListener.onFling(), LEFT FLING<<<");
                currentItem = (currentItem + 1) % imageViews.size();
                viewPager.setCurrentItem(currentItem);
                scheduledExecutorService.shutdown();
                return true;
            } else if (before.getX() - current.getX() < /*-120*/-60){//Right fling.
                if (DEBUG)
                    Log.v(TAG, "GestureDetectorListener.onFling(), RIGHT FLING>>>");
                currentItem = (imageViews.size() + (currentItem - 1)) % imageViews.size();
                viewPager.setCurrentItem(currentItem);
                scheduledExecutorService.shutdown();
                return true;
            }
            return false;
        }

        @Override
        public void onLongPress(MotionEvent arg0) {
            if (DEBUG)
                Log.v(TAG, "GestureDetectorListener.onLongPress()...");
        }

        @Override
        public boolean onScroll(MotionEvent arg0, MotionEvent arg1, float arg2,
                float arg3) {
            if (DEBUG)
                Log.v(TAG, "GestureDetectorListener.onScroll()...");
            return false;
        }

        @Override
        public void onShowPress(MotionEvent arg0) {
            if (DEBUG)
                Log.v(TAG, "GestureDetectorListener.onShowPress()...");
        }

        @Override
        public boolean onSingleTapUp(MotionEvent arg0) {
            if (DEBUG)
                Log.v(TAG, "GestureDetectorListener.onSingleTapUp()...");
            return false;
        }
    }

    private class ScrollTask implements Runnable {

        public void run() {
            synchronized (viewPager) {
                //Log.d(TAG, "currentItem: " + currentItem);
                currentItem = (currentItem + 1) % imageViews.size();
                handler.obtainMessage().sendToTarget(); // 通过Handler切换图片
            }
        }

    }

    private class UpperOnPageChangeListener implements OnPageChangeListener {
        private int oldPosition = 0;

        /**
         * This method will be invoked when a new page becomes selected.
         * position: Position index of the new selected page.
         */
        public void onPageSelected(int position) {
            //Log.d(TAG, "onPageSelected(), "+position);
            currentItem = position;
            tv_title.setText(imageTitles[position]);
            dots.get(oldPosition).setBackgroundResource(R.drawable.dot_normal);
            dots.get(position).setBackgroundResource(R.drawable.dot_focused);
            oldPosition = position;
        }

        public void onPageScrollStateChanged(int arg0) {
            //Log.d(TAG, "onPageScrollStateChanged(), "+arg0);
        }

        public void onPageScrolled(int arg0, float arg1, int arg2) {
            //Log.d(TAG, "onPageScrolled(), "+arg0+", "+arg1+", "+arg2);
        }
    }

    private class UpperPagerAdapter extends PagerAdapter {

        @Override
        public int getCount() {
            return imageResIds.length;
        }

        @Override
        public Object instantiateItem(ViewGroup v, int pos) {
            if (DEBUG)
                Log.d(TAG, "instantiateItem(), v="+v+", pos="+pos);
            ((ViewPager) v).addView(imageViews.get(pos));
            return imageViews.get(pos);
        }

        @Override
        public void destroyItem(ViewGroup v, int pos, Object arg2) {
            if (DEBUG)
                Log.d(TAG, "destroyItem(), v="+v+", pos="+pos);
            ((ViewPager) v).removeView((View) arg2);
        }

        @Override
        public boolean isViewFromObject(View arg0, Object arg1) {
            return arg0 == arg1;
        }

        @Override
        public void restoreState(Parcelable arg0, ClassLoader arg1) {

        }

        @Override
        public Parcelable saveState() {
            return null;
        }

        @Override
        public void startUpdate(View arg0) {

        }

        @Override
        public void finishUpdate(View arg0) {

        }
    }

    public static void setUpperPagerView(Context context) {
        FragmentManager fragManager = ((Activity) context).getFragmentManager();
        FragmentTransaction fragTransaction = fragManager.beginTransaction();
        fragTransaction.add(R.id.upper_pager, new UpperPagerViewFragment());
        fragTransaction.commit();
    }
}