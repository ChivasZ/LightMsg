package com.lightmsg.activity.mall;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;

import com.lightmsg.activity.HomeActivity;
import com.lightmsg.R;

public class MallMenuFragment extends Fragment implements OnItemClickListener {

    private static final String TAG = MallMenuFragment.class.getSimpleName();
    private MallActivity activity;

    private ListView mListView;

    private MallMenuAdapter mAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = (MallActivity)getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.mall_menu_fragment, container, false);
        mListView = (ListView) view.findViewById(R.id.mall_menu_frag);
        mAdapter = new MallMenuAdapter(getActivity());

        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(this);

        //Add header view
        //mListView.addHeaderView(getMenuHeaderView());

        //Add footer view
        //mListView.addFooterView(getMenuFooterView());

        return view;
    }
    
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        
        View menu = activity.getSlidingMenu().getMenu();
        ImageView ivButton = (ImageView)menu.findViewById(R.id.mall_menu_header);
        ivButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                Log.d(TAG, "onClick(), v="+v);
            }
        });
        Button button = (Button)menu.findViewById(R.id.mall_menu_footer);
        button.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                Log.d(TAG, "onClick(), v="+v);
            }
        });
    }

    private View getMenuHeaderView() {
        View v = LayoutInflater.from(getActivity()).inflate(R.layout.mall_menu_headerview, null, false);
        return v;
    }

    private View getMenuFooterView() {
        View v = LayoutInflater.from(getActivity()).inflate(R.layout.mall_menu_footer, null, false);
        Button button = (Button)v.findViewById(R.id.mall_menu_footer);
        button.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                Log.d(TAG, "getMenuFooterView(), v="+v);
            }
        });
        return v;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        activity.changeMallType(position);
        activity.showContent();
        
        /*switch (position) {
        case 1: //HomePage
            setContentView(R.layout.mall_layout);
            break;
        case 2: //Store
            setContentView(R.layout.mall_store_layout);
            break;
        case 3: //Restaurant
            setContentView(R.layout.mall_restaurant_layout);
            break;
        case 4: //Fruit
            setContentView(R.layout.mall_fruit_layout);
            break;
        case 5: //Fishery
            setContentView(R.layout.mall_fishery_layout);
        case 6: //Health
            setContentView(R.layout.mall_health_layout);
            break;
        default:
            return;
        }*/
    }
}
