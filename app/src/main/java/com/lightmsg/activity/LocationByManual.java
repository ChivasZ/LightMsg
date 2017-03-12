package com.lightmsg.activity;

import com.lightmsg.service.LocatorService;
import com.lightmsg.service.LocatorService.LocatorServiceBinder;
import com.lightmsg.R;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class LocationByManual extends Activity {
    private static final String TAG = "SmartCommunity/" + LocationByManual.class.getSimpleName();

    private ListView lv;
    String[] provinces;
    int selected;
    
    private boolean fromLocatorService;
    private LocatorServiceBinder binder;
    private ServiceConnection sc;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        //getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.manual_location);
        
        fromLocatorService = getIntent().getBooleanExtra("fromLocatorService", false);

        sc = new ServiceConnection(){
            @Override
            public void onServiceConnected(ComponentName className,
                    IBinder service) {
                Log.v(TAG, "onServiceConnected()...");
                // We've bound to LocalService, cast the IBinder and get LocalService instance
                binder = (LocatorServiceBinder) service;
            }

            @Override
            public void onServiceDisconnected(ComponentName arg0) {
                Log.v(TAG, "onServiceDisconnected()...");
            }
        };
        if (fromLocatorService) {
            Intent intent = new Intent(this, LocatorService.class);
            bindService(intent, 
                sc,
                Context.BIND_AUTO_CREATE);
        }
        
        initView();
    }
    
    @Override
    public void onDestroy() {
        unbindService(sc);
        super.onDestroy();
    }

    private void initView() {
        lv = (ListView) findViewById(R.id.lv_province);
        provinces = getResources().getStringArray(R.array.provinces);

        lv.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View v, int pos, long id) {
                Log.v(TAG, "onItemClick(), av="+parent+", v="+v+", pos="+pos+", id="+id);
                selected = pos;
                if (!fromLocatorService) {
                    Intent intent = new Intent();
                    intent.putExtra("province", provinces[pos]);
                    setResult(Activity.RESULT_OK, intent);
                    LocationByManual.this.finish();
                } else {
                    if (binder != null) {
                        ((LocatorService)binder.getService()).setCurrent(provinces[selected]);
                        LocationByManual.this.finish();
                    } else {
                        Log.e(TAG, "binder is null, not bind yet!!!");
                    }
                }
            }

        });
        lv.setAdapter(new BaseAdapter() {

            @Override
            public int getCount() {
                // TODO Auto-generated method stub
                return provinces.length;
            }

            @Override
            public View getView(int pos, View view, ViewGroup arg2) {
                // TODO Auto-generated method stub
                if (view != null) {
                    ((TextView) view).setText(provinces[pos]);
                } else {
                    view = new TextView(LocationByManual.this);
                    ((TextView) view).setText(provinces[pos]);
                    ((TextView) view).setTextSize(20);
                }
                return view;
            }

            @Override
            public Object getItem(int arg0) {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public long getItemId(int arg0) {
                // TODO Auto-generated method stub
                return 0;
            }

        });
    }
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            /*AlertDialog.Builder dialog = new AlertDialog.Builder(this);
            dialog.setIcon(android.R.drawable.ic_dialog_info);
            dialog.setTitle("警告");
            dialog.setMessage("你确定要退出当前程序？");
            dialog.setPositiveButton("确定",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    });
            dialog.setNegativeButton("取消",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    });
            dialog.show();*/
            
            Toast.makeText(this, R.string.quit_manual_locate, Toast.LENGTH_LONG).show();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}