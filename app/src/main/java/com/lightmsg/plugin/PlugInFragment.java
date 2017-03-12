package com.lightmsg.plugin;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

import com.lightmsg.LightMsg;
import com.lightmsg.R;
import com.lightmsg.activity.UpperPagerViewFragment;
import com.lightmsg.activity.mall.MallActivity;
import com.lightmsg.activity.msgdesign.LightMsgActivity;
import com.lightmsg.plugin.airquality.AirQuality;
import com.lightmsg.plugin.weather.WeatherActivity;
import com.lightmsg.service.CoreService;


public class PlugInFragment extends Fragment implements AdapterView.OnItemClickListener {
    private static final String TAG = PlugInFragment.class.getSimpleName();
    private LightMsg app = null;
    protected CoreService xs = null;
    private LightMsgActivity activity = null;

    private GridView mGridView;
    private PlugInViewAdapter mPlugInViewAdapter;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.v(TAG, "onCreate()...");
        super.onCreate(savedInstanceState);
        activity = (LightMsgActivity)getActivity();
        app = (LightMsg)activity.getApplication();
        xs = app.xs;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        Log.v(TAG, "onCreateView()...");
        return inflater.inflate(R.layout.plugin_layout, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        Log.v(TAG, "onActivityCreated()...");
        super.onActivityCreated(savedInstanceState);

        mGridView = (GridView) activity.findViewById(R.id.gridView1);
        mPlugInViewAdapter = new PlugInViewAdapter(activity);
        mGridView.setAdapter(mPlugInViewAdapter);
        mGridView.setOnItemClickListener(this);
    }

    @Override
    public void onResume() {
        Log.v(TAG, "onResume()...");
        super.onResume();
    }

    @Override
    public void onPause() {
        Log.v(TAG, "onPause()... ");
        super.onPause();
    }

    @Override
    public void onStart() {
        Log.v(TAG, "onStart()...");
        super.onStart();
        UpperPagerViewFragment.setUpperPagerView(activity);
    }

    @Override
    public void onStop() {
        Log.v(TAG, "onStop()...");
        super.onStop();
    }

    @Override
    public void onDestroy() {
        Log.v(TAG, "onDestroy()...");
        super.onDestroy();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent intent = new Intent();
        switch (position) {
            case 0:
                intent.setClass(activity, WeatherActivity.class);
                startActivity(intent);
                break;
            case 1:
                intent.setClass(activity, AirQuality.class);
                startActivity(intent);
                break;
            case 2:
                intent.setClass(activity, MallActivity.class);
                startActivity(intent);
                break;
            case 3:
                break;
            default:
                break;
        }
    }
}
