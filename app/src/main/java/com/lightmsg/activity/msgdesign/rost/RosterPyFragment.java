package com.lightmsg.activity.msgdesign.rost;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;

import com.lightmsg.R;
import com.lightmsg.LightMsg;
import com.lightmsg.activity.PyParser;
import com.lightmsg.activity.RostEntry;
import com.lightmsg.activity.msgdesign.LightMsgActivity;
import com.lightmsg.activity.msgdesign.chat.ChatThread;
import com.lightmsg.activity.msgdesign.etc.AddFriend;
import com.lightmsg.service.CoreService;
import com.lightmsg.util.LightMsgAsyncTask;

public class RosterPyFragment extends Fragment implements OnItemClickListener, OnItemLongClickListener, OnGestureListener {

    private static final String TAG = RosterPyFragment.class.getSimpleName();
    private LightMsg app = null;
    private CoreService xs = null;
    private LightMsgActivity activity = null;

    //private PinnedHeaderListView lvRoster = null;
    private ArrayList<RostEntry> mRostList = null;
    private RostListPyAdapter mAdapter;

    private ProgressDialog progressDialog = null;

    private GestureDetector gd = null;

    private final LightMsgAsyncTask.Tracker mTaskTracker = new LightMsgAsyncTask.Tracker();

    @SuppressWarnings("deprecation")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.v(TAG, "onCreate()...");
        super.onCreate(savedInstanceState);
        activity = (LightMsgActivity)getActivity();
        app = (LightMsg)activity.getApplication();
        xs = app.xs;

        //requestWindowFeature(Window.FEATURE_NO_TITLE);
        //getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        //setContentView(R.layout.roster);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.v(TAG, "onCreateView()...");
        View view = inflater.inflate(R.layout.roster_design_py, container, false);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        Log.v(TAG, "onActivityCreated()...");
        super.onActivityCreated(savedInstanceState);

        /*lvRoster = (PinnedHeaderListView) activity.findViewById(R.id.ph_list);//getListView();
        lvRoster.setOnItemClickListener(this);
        lvRoster.setOnItemLongClickListener(this);*/

        //progressDialog = ProgressDialog.show(activity, getString(R.string.waiting), getString(R.string.fetching_roster), true);

        final RosterPyFragment me = this;
        new LightMsgAsyncTask<Void, Void, Long>(mTaskTracker) {
            @Override
            protected Long doInBackground(Void... params) {
                Log.v(TAG, "LightMsgAsyncTask.doInBackground()...");
                mRostList = me.xs.getRosterArrayList(me.activity);
                Log.v(TAG, "LightMsgAsyncTask.doInBackground(), mRostList.size()="+mRostList.size());

                setData();
                //setView();
                return Long.valueOf(-1);//not used now.
            }

            @Override
            protected void onSuccess(Long accountId) {
                Log.v(TAG, "LightMsgAsyncTask.onSuccess()...");
                //lvRoster.setAdapter(mAdapter);
                setView();
                //progressDialog.dismiss();
            }
        }.executeSerial();

        gd = new GestureDetector(this);
    }

    private static final String FORMAT = "^[a-z,A-Z].*$";
    private PinnedHeaderListView mListView;
    private SlipBladeView mSlipBlade;
    private final List<String> mSections 			= new ArrayList<String>();
    private final Map<String, List<String>> mMap 	= new HashMap<String, List<String>>();
    private final List<Integer> mPositions 			= new ArrayList<Integer>();
    private final Map<String, Integer> mIndexer 	= new HashMap<String, Integer>();;
    private final PyParser pp = PyParser.getInstance();

    private void setData() {
        Log.v(TAG, "setData()..."+mIndexer);

        /*mSections = new ArrayList<String>();
        mMap = new HashMap<String, List<String>>();
        mPositions = new ArrayList<Integer>();
        mIndexer = new HashMap<String, Integer>();*/

        for (int i = 0; i < mRostList.size(); i++) {
            String name = mRostList.get(i).getSortName();
            Log.v(TAG, "setData(), name="+name);

            String firstName = name.substring(0, 1).toUpperCase();
            if (firstName.matches(FORMAT)) {
                if (mSections.contains(firstName)) {
                    mMap.get(firstName).add(mRostList.get(i).getName());
                } else {
                    mSections.add(firstName);
                    List<String> list = new ArrayList<String>();
                    list.add(mRostList.get(i).getName());
                    mMap.put(firstName, list);
                }
            } else {
                if (mSections.contains("#")) {
                    mMap.get("#").add(mRostList.get(i).getName());
                } else {
                    mSections.add("#");
                    List<String> list = new ArrayList<String>();
                    list.add(mRostList.get(i).getName());
                    mMap.put("#", list);
                }
            }
        }

        Collections.sort(mSections);
        int position = 0;
        for (int i = 0; i < mSections.size(); i++) {
            mIndexer.put(mSections.get(i), position);
            mPositions.add(position);
            position += mMap.get(mSections.get(i)).size();
        }

        Log.v(TAG, "setData()，mSections="+mSections);
        Log.v(TAG, "setData()，mMap="+mMap);
        Log.v(TAG, "setData()，mIndexer="+mIndexer);
    }

    private void setView() {
        Log.v(TAG, "setView()..."+mIndexer);

        //View v = lvRoster;
        mListView = (PinnedHeaderListView) activity.findViewById(R.id.ph_list);
        mSlipBlade = (SlipBladeView) activity.findViewById(R.id.sbv);
        mSlipBlade.setOnItemClickListener(new SlipBladeView.OnItemClickListener() {
            @Override
            public void onItemClick(String s) {
                Log.v(TAG, "SlipBladeView..onItemClick()..."+s+", "+mIndexer+", "+mIndexer.get(s));
                if (mIndexer.get(s) != null) {
                    mListView.setSelection(mIndexer.get(s));
                }
            }
        });
        mAdapter = new RostListPyAdapter(activity, R.layout.roster_list_item_py, mRostList, mSections, mPositions);

        mListView.setAdapter(mAdapter);
        mListView.setOnScrollListener(mAdapter);
        mListView.setPinnedHeaderView(LayoutInflater.from(activity).inflate(
                R.layout.roster_list_pinnedheader, mListView, false));

        mListView.setOnItemClickListener(this);
        mListView.setOnItemLongClickListener(this);
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
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
        case R.id.menu_add_friends:
            Intent i = new Intent();
            i.setClass(activity, AddFriend.class);
            startActivity(i);
            break;
        default:
            break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onStart() {
        Log.v(TAG, "onStart()...");
        super.onStart();
    }

    @Override
    public void onStop() {
        Log.v(TAG, "onStop()...");
        super.onStop();
    }

    @Override
    public void onDestroy() {
        Log.v(TAG, "onDestroy()...");
        super.onStop();
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View v, int pos,
            long id) {
        Log.v(TAG, "onItemLongClick()...");
        return false;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View v, int pos, long id) {
        Log.v(TAG, "onItemClick()...");
        String jid = mRostList.get(pos).getUser();
        String name = mRostList.get(pos).getName();
        startChatWith(jid, name);
    }

    private void startChatWith(String user, String name) {
        Log.v(TAG, "startChatWith()... user="+user);

        Intent intent = new Intent();
        intent.setClass(activity, ChatThread.class);
        intent.putExtra("user", user);
        intent.putExtra("name", name);

        startActivity(intent);
    }

    /*@Override
    public boolean onTouchEvent(MotionEvent event) {
        Log.v(TAG, "onTouchEvent()...");
        return this.gd.onTouchEvent(event);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        Log.v(TAG, "dispatchTouchEvent...");
        boolean ret = gd.onTouchEvent(ev);
        Log.v(TAG, "dispatchTouchEvent, ret="+ret);
        return (ret ? ret:super.dispatchTouchEvent(ev));
    }*/

    //Add for Gesture function --START--
    @Override
    public boolean onDown(MotionEvent arg0) {
        Log.v(TAG, "GestureDetectorListener.onDown()...");
        return false;
    }

    @Override
    public boolean onFling(MotionEvent before, MotionEvent current, float velocityX,
            float velocityY) {
        Log.v(TAG, "GestureDetectorListener.onFling()...");
        if (before.getX() - current.getX() > 120) {//Left fling.
            Log.v(TAG, "GestureDetectorListener.onFling(), LEFT FLING<<<");
            return true;
        } else if (before.getX() - current.getX() < -120){//Right fling.
            Log.v(TAG, "GestureDetectorListener.onFling(), RIGHT FLING>>>");
            return true;
        }
        return false;
    }

    @Override
    public void onLongPress(MotionEvent arg0) {
        Log.v(TAG, "GestureDetectorListener.onLongPress()...");
    }

    @Override
    public boolean onScroll(MotionEvent arg0, MotionEvent arg1, float arg2,
            float arg3) {
        Log.v(TAG, "GestureDetectorListener.onScroll()...");
        return false;
    }

    @Override
    public void onShowPress(MotionEvent arg0) {
        Log.v(TAG, "GestureDetectorListener.onShowPress()...");
    }

    @Override
    public boolean onSingleTapUp(MotionEvent arg0) {
        Log.v(TAG, "GestureDetectorListener.onSingleTapUp()...");
        return false;
    }
    //Add for Gesture function --END--
}
