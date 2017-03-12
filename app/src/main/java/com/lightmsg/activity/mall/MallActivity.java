package com.lightmsg.activity.mall;

import com.lightmsg.R;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.jeremyfeinstein.slidingmenu.lib.app.SlidingActivity;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;


public class MallActivity extends SlidingActivity {

    private static final String TAG = MallActivity.class.getSimpleName();
    
    private SlidingMenu mSlidingMenu;

    private FrameLayout mContent;
    
    private RadioGroup mRadioGroup;

    private RadioButton rdButton1;
    
    private int state = HOME;
    private static final int HOME = 0;
    private static final int STORE = 1;
    private static final int RESTAURANT = 2;
    private static final int FRUIT = 3;
    private static final int FISHERY = 4;
    private static final int HEALTH = 5;
    
    private OnCheckedChangeListener homeOnCheckedState = new OnCheckedChangeListener() {

        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            // TODO Auto-generated method stub
            Log.d(TAG, "homeOnCheckedState.onCheckedChanged(), checkedId="+checkedId);
            FragmentManager fragManager = getFragmentManager();
            FragmentTransaction fragTransaction = fragManager.beginTransaction();
            switch (checkedId) {
            case R.id.radioButton1:
                fragTransaction.replace(R.id.content, new FisheryFragment());
                fragTransaction.commit();
                break;
            case R.id.radioButton2:
                fragTransaction.replace(R.id.content, new MapFragment());
                fragTransaction.commit();
                break;
            case R.id.radioButton3:
                fragTransaction.replace(R.id.content, new MallFragment());
                fragTransaction.commit();
                break;
            default:
                fragTransaction.replace(R.id.content, new OthersFragment());
                fragTransaction.commit();
                break;
            }
        }
        
    };
    
    private OnCheckedChangeListener storeOnCheckedState = new OnCheckedChangeListener() {

        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            // TODO Auto-generated method stub
            Log.d(TAG, "storeOnCheckedState.onCheckedChanged(), checkedId="+checkedId);
            FragmentManager fragManager = getFragmentManager();
            FragmentTransaction fragTransaction = fragManager.beginTransaction();
            switch (checkedId) {
            case R.id.radioButton1:
                fragTransaction.replace(R.id.content, new FisheryFragment());
                fragTransaction.commit();
                break;
            case R.id.radioButton2:
                fragTransaction.replace(R.id.content, new MapFragment());
                fragTransaction.commit();
                break;
            case R.id.radioButton3:
                fragTransaction.replace(R.id.content, new MallFragment());
                fragTransaction.commit();
                break;
            default:
                fragTransaction.replace(R.id.content, new OthersFragment());
                fragTransaction.commit();
                break;
            }
        }
        
    };
    
    private OnCheckedChangeListener restaurantOnCheckedState = new OnCheckedChangeListener() {

        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            // TODO Auto-generated method stub
            Log.d(TAG, "restaurantOnCheckedState.onCheckedChanged(), checkedId="+checkedId);
            FragmentManager fragManager = getFragmentManager();
            FragmentTransaction fragTransaction = fragManager.beginTransaction();
            switch (checkedId) {
            case R.id.radioButton1:
                fragTransaction.replace(R.id.content, new FisheryFragment());
                fragTransaction.commit();
                break;
            case R.id.radioButton2:
                fragTransaction.replace(R.id.content, new MapFragment());
                fragTransaction.commit();
                break;
            case R.id.radioButton3:
                fragTransaction.replace(R.id.content, new MallFragment());
                fragTransaction.commit();
                break;
            default:
                fragTransaction.replace(R.id.content, new OthersFragment());
                fragTransaction.commit();
                break;
            }
        }
        
    };
    
    private OnCheckedChangeListener fruitOnCheckedState = new OnCheckedChangeListener() {

        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            // TODO Auto-generated method stub
            Log.d(TAG, "fruitOnCheckedState.onCheckedChanged(), checkedId="+checkedId);
            FragmentManager fragManager = getFragmentManager();
            FragmentTransaction fragTransaction = fragManager.beginTransaction();
            switch (checkedId) {
            case R.id.radioButton1:
                fragTransaction.replace(R.id.content, new FisheryFragment());
                fragTransaction.commit();
                break;
            case R.id.radioButton2:
                fragTransaction.replace(R.id.content, new MapFragment());
                fragTransaction.commit();
                break;
            case R.id.radioButton3:
                fragTransaction.replace(R.id.content, new MallFragment());
                fragTransaction.commit();
                break;
            default:
                fragTransaction.replace(R.id.content, new OthersFragment());
                fragTransaction.commit();
                break;
            }
        }
        
    };
    
    private OnCheckedChangeListener fisheryOnCheckedState = new OnCheckedChangeListener() {

        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            // TODO Auto-generated method stub
            Log.d(TAG, "fisheryOnCheckedState.onCheckedChanged(), checkedId="+checkedId);
            FragmentManager fragManager = getFragmentManager();
            FragmentTransaction fragTransaction = fragManager.beginTransaction();
            switch (checkedId) {
            case R.id.radioButton1:
                fragTransaction.replace(R.id.content, new FisheryFragment());
                fragTransaction.commit();
                break;
            case R.id.radioButton2:
                fragTransaction.replace(R.id.content, new MapFragment());
                fragTransaction.commit();
                break;
            case R.id.radioButton3:
                fragTransaction.replace(R.id.content, new MallFragment());
                fragTransaction.commit();
                break;
            default:
                fragTransaction.replace(R.id.content, new OthersFragment());
                fragTransaction.commit();
                break;
            }
        }
        
    };
    
    private OnCheckedChangeListener healthOnCheckedState = new OnCheckedChangeListener() {

        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            // TODO Auto-generated method stub
            Log.d(TAG, "healthOnCheckedState.onCheckedChanged(), checkedId="+checkedId);
            FragmentManager fragManager = getFragmentManager();
            FragmentTransaction fragTransaction = fragManager.beginTransaction();
            switch (checkedId) {
            case R.id.radioButton1:
                fragTransaction.replace(R.id.content, new FisheryFragment());
                fragTransaction.commit();
                break;
            case R.id.radioButton2:
                fragTransaction.replace(R.id.content, new MapFragment());
                fragTransaction.commit();
                break;
            case R.id.radioButton3:
                fragTransaction.replace(R.id.content, new MallFragment());
                fragTransaction.commit();
                break;
            default:
                fragTransaction.replace(R.id.content, new OthersFragment());
                fragTransaction.commit();
                break;
            }
        }
        
    };
    
    private OnCheckedChangeListener exchangeOnCheckedState = new OnCheckedChangeListener() {

        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            // TODO Auto-generated method stub
            Log.d(TAG, "healthOnCheckedState.onCheckedChanged(), checkedId="+checkedId);
            FragmentManager fragManager = getFragmentManager();
            FragmentTransaction fragTransaction = fragManager.beginTransaction();
            switch (checkedId) {
            case R.id.radioButton1:
                fragTransaction.replace(R.id.content, new FisheryFragment());
                fragTransaction.commit();
                break;
            case R.id.radioButton2:
                fragTransaction.replace(R.id.content, new MapFragment());
                fragTransaction.commit();
                break;
            case R.id.radioButton3:
                fragTransaction.replace(R.id.content, new MallFragment());
                fragTransaction.commit();
                break;
            default:
                fragTransaction.replace(R.id.content, new OthersFragment());
                fragTransaction.commit();
                break;
            }
        }
        
    };
    
    private OnCheckedChangeListener[] onCheckedChangeStates = new OnCheckedChangeListener[] {
            homeOnCheckedState,
            storeOnCheckedState,
            restaurantOnCheckedState,
            fruitOnCheckedState,
            fisheryOnCheckedState,
            healthOnCheckedState,
            exchangeOnCheckedState
    };
    
    private static interface MallTypeChangeListener {
        void onTypeSeleted(int index);
    }
    
    private MallTypeChangeListener homeState = new MallTypeChangeListener() {

        @Override
        public void onTypeSeleted(int index) {
            // TODO Auto-generated method stub
            Log.d(TAG, "onTypeSeleted("+index+")");
            mRadioGroup.setVisibility(View.GONE);
            //mRadioGroup.setOnCheckedChangeListener(onCheckedChangeStates[index]);
            //rdButton1.setChecked(true);
            
            FragmentManager fragManager = getFragmentManager();
            FragmentTransaction fragTransaction = fragManager.beginTransaction();
            fragTransaction.replace(R.id.content, new MallFragment());
            fragTransaction.commit();
        }
    };
    
    private MallTypeChangeListener storeState = new MallTypeChangeListener() {

        @Override
        public void onTypeSeleted(int index) {
            // TODO Auto-generated method stub
            Log.d(TAG, "onTypeSeleted("+index+")");
            mRadioGroup.setVisibility(View.GONE);
            //mRadioGroup.setOnCheckedChangeListener(onCheckedChangeStates[index]);
            //rdButton1.setChecked(true);
            
            FragmentManager fragManager = getFragmentManager();
            FragmentTransaction fragTransaction = fragManager.beginTransaction();
            fragTransaction.replace(R.id.content, new MallFragment());
            fragTransaction.commit();
        }
    };
    
    private MallTypeChangeListener restaurantState = new MallTypeChangeListener() {

        @Override
        public void onTypeSeleted(int index) {
            // TODO Auto-generated method stub
            Log.d(TAG, "onTypeSeleted("+index+")");
            mRadioGroup.setVisibility(View.GONE);
            //mRadioGroup.setOnCheckedChangeListener(onCheckedChangeStates[index]);
            //rdButton1.setChecked(true);
            
            FragmentManager fragManager = getFragmentManager();
            FragmentTransaction fragTransaction = fragManager.beginTransaction();
            fragTransaction.replace(R.id.content, new FisheryFragment());
            fragTransaction.commit();
        }
    };
    
    private MallTypeChangeListener fruitState = new MallTypeChangeListener() {

        @Override
        public void onTypeSeleted(int index) {
            // TODO Auto-generated method stub
            Log.d(TAG, "onTypeSeleted("+index+")");
            mRadioGroup.setVisibility(View.GONE);
            //mRadioGroup.setOnCheckedChangeListener(onCheckedChangeStates[index]);
            //rdButton1.setChecked(true);
            
            FragmentManager fragManager = getFragmentManager();
            FragmentTransaction fragTransaction = fragManager.beginTransaction();
            fragTransaction.replace(R.id.content, new FisheryFragment());
            fragTransaction.commit();
        }
    };
    
    private MallTypeChangeListener fisheryState = new MallTypeChangeListener() {

        @Override
        public void onTypeSeleted(int index) {
            // TODO Auto-generated method stub
            Log.d(TAG, "onTypeSeleted("+index+")");
            mRadioGroup.setVisibility(View.VISIBLE);
            mRadioGroup.setOnCheckedChangeListener(onCheckedChangeStates[index]);
            rdButton1.setChecked(true);
        }
    };
    
    private MallTypeChangeListener healthState = new MallTypeChangeListener() {

        @Override
        public void onTypeSeleted(int index) {
            // TODO Auto-generated method stub
            Log.d(TAG, "onTypeSeleted("+index+")");
            mRadioGroup.setVisibility(View.GONE);
            //mRadioGroup.setOnCheckedChangeListener(onCheckedChangeStates[index]);
            //rdButton1.setChecked(true);
            
            FragmentManager fragManager = getFragmentManager();
            FragmentTransaction fragTransaction = fragManager.beginTransaction();
            fragTransaction.replace(R.id.content, new FisheryFragment());
            fragTransaction.commit();
        }
    };
    
    private MallTypeChangeListener exchangeState = new MallTypeChangeListener() {

        @Override
        public void onTypeSeleted(int index) {
            // TODO Auto-generated method stub
            Log.d(TAG, "onTypeSeleted("+index+")");
            mRadioGroup.setVisibility(View.GONE);
            //mRadioGroup.setOnCheckedChangeListener(onCheckedChangeStates[index]);
            //rdButton1.setChecked(true);
            
            FragmentManager fragManager = getFragmentManager();
            FragmentTransaction fragTransaction = fragManager.beginTransaction();
            fragTransaction.replace(R.id.content, new FisheryFragment());
            fragTransaction.commit();
        }
    };
    
    private MallTypeChangeListener[] states = new MallTypeChangeListener[] {
        homeState,
        storeState,
        restaurantState,
        fruitState,
        fisheryState,
        healthState,
        exchangeState
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mall_layout);
        
        mContent = (FrameLayout)findViewById(R.id.content);

        initRadioGroup();
        initSlidingMenu();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case android.R.id.home:
            toggle();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    private void initSlidingMenu() {
        mSlidingMenu = getSlidingMenu();
        mSlidingMenu.setMode(SlidingMenu.LEFT);
        mSlidingMenu.setTouchModeAbove(SlidingMenu.TOUCHMODE_MARGIN);
        mSlidingMenu.setBehindOffsetRes(R.dimen.slidingmenu_offset);
        setBehindContentView(R.layout.mall_menu);
        mSlidingMenu.setFadeDegree(0.35f);
        setSlidingActionBarEnabled(true);
        
        // 设置home的点击效果和箭头
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setDisplayHomeAsUpEnabled(false);
    }

    private void initRadioGroup() {
        mRadioGroup = (RadioGroup) findViewById(R.id.mall_radioGroup);
        rdButton1 = (RadioButton) findViewById(R.id.radioButton1);
        
        mRadioGroup.setVisibility(View.GONE);
        //mRadioGroup.setOnCheckedChangeListener(homeState);
        //rdButton1.setChecked(true);
        changeMallType(HOME);
    }
    
    public void changeMallType(int index) {
        switch(index) {
        case HOME:
            Log.d(TAG, "changeRadioGroup(), >>HOME");
            break;
        case STORE:
            Log.d(TAG, "changeRadioGroup(), >>STORE");
            break;
        case RESTAURANT:
            Log.d(TAG, "changeRadioGroup(), >>RESTAURANT");
            break;
        case FRUIT:
            Log.d(TAG, "changeRadioGroup(), >>FRUIT");
            break;
        case FISHERY:
            Log.d(TAG, "changeRadioGroup(), >>FISHERY");
            break;
        case HEALTH:
            Log.d(TAG, "changeRadioGroup(), >>HEALTH");
            break;
        default:
            Log.d(TAG, "changeRadioGroup(), >>OTHERS??!!");
            break;
        }
        
        states[index].onTypeSeleted(index);
    }
}
