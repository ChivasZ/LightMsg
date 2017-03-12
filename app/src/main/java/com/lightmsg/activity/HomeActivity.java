package com.lightmsg.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;

import com.lightmsg.R;
import com.lightmsg.activity.mall.MallActivity;


public class HomeActivity extends Activity implements OnItemClickListener {
    private static final String TAG = HomeActivity.class.getName();
    private GridView mGridView;
    private HomeGridViewAdapter mHomeGridViewAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.home_layout);

        mGridView = (GridView) findViewById(R.id.gridView1);
        mHomeGridViewAdapter = new HomeGridViewAdapter(this);
        mGridView.setAdapter(mHomeGridViewAdapter);
        mGridView.setOnItemClickListener(this);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent intent = new Intent();
        switch (position) {
        case 0:
            //intent.setClass(this, BellActivity.class);
            //startActivity(intent);
            break;
        case 1:
            intent.setClass(this, MallActivity.class);
            startActivity(intent);
            break;
        case 2:
            intent.setClass(this, MallActivity.class);
            startActivity(intent);
            break;
        case 3:
            intent.setClass(this, MallActivity.class);
            startActivity(intent);
            break;
        default:
            break;
        }
    }
    
    @Override
    protected void onStart() {
        super.onStart();
        
        UpperPagerViewFragment.setUpperPagerView(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
    }
}
