package com.lightmsg.activity.mall;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.lightmsg.R;
import com.lightmsg.activity.UpperPagerViewFragment;

public class FisheryFragment extends Fragment {

    private Activity activity;
    private ListView mListView;

    private MallListViewAdaper mMallListViewAdaper;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.mall_fishery_fragment, container, false);
        mListView = (ListView) view.findViewById(R.id.listView1);
        mMallListViewAdaper = new MallListViewAdaper(getActivity());
        mListView.setAdapter(mMallListViewAdaper);
        mMallListViewAdaper.notifyDataSetChanged();
        return view;
    }
    
    @Override
    public void onStart() {
        super.onStart();
        
        UpperPagerViewFragment.setUpperPagerView(this.getActivity());
    }

    @Override
    public void onStop() {
        super.onStop();
    }

}
