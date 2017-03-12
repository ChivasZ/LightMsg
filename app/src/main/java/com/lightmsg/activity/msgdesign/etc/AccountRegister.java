package com.lightmsg.activity.msgdesign.etc;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.lightmsg.R;
import com.lightmsg.LightMsg;
import com.lightmsg.service.CoreService;
import com.lightmsg.util.CharUtils;

public class AccountRegister extends AppCompatActivity {

    private static final String TAG = AccountRegister.class.getName();

    private LightMsg app = null;
    private CoreService xs = null;
    private IntentFilter registerTaskFilter = null;
    private RegisterTaskReceiver registerTaskReceiver = null;

    private Button btnRegister;
    private Button btnCancel;

    private EditText etAccountUsername;
    private EditText etAccountName;
    private EditText etPwd;
    private String account;
    private String accountName;
    private String password;
    private CheckBox cbShowPwd;
    private TextView tvShowPwd;
    
    private LayoutInflater mLayoutflater;
    private LinearLayout mTopLayout = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.v(TAG, "onCreate()... ");
        super.onCreate(savedInstanceState);
        app = (LightMsg)getApplication();
        xs = app.xs;

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE |
                WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        
        //requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.account_register_design_layout);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //for Title display.
        /*mLayoutflater = getLayoutInflater();
        mTopLayout = (LinearLayout)findViewById(R.id.account_register_top);
        mTopLayout.addView(mLayoutflater.inflate(R.layout.account_register_top, null));
        TextView tvTopCenter = (TextView)mTopLayout.findViewById(R.id.top_center);
        tvTopCenter.setText(R.string.register);*/

        etAccountUsername = (EditText)findViewById(R.id.account_new_username);
        etAccountName = (EditText)findViewById(R.id.account_new_name);
        etPwd = (EditText)findViewById(R.id.account_password);
        btnRegister = (Button)findViewById(R.id.btnRegister);
        btnRegister.setOnClickListener(btnRegisterListener);
        btnCancel = (Button)findViewById(R.id.btnCancel);
        btnCancel.setOnClickListener(btnCancelListener);

        cbShowPwd = (CheckBox)findViewById(R.id.show_password);
        if (cbShowPwd.isChecked()) {
            etPwd.setInputType(InputType.TYPE_CLASS_TEXT
                    | EditorInfo.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
        } else {
            etPwd.setInputType(InputType.TYPE_CLASS_TEXT
                    | EditorInfo.TYPE_TEXT_VARIATION_PASSWORD);
        }
        cbShowPwd.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                //int inputtype = etPwd.getInputType();
                //if ((inputtype & InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD) != InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD)

                if (isChecked) {
                    etPwd.setInputType(InputType.TYPE_CLASS_TEXT
                            | EditorInfo.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                } else {
                    etPwd.setInputType(InputType.TYPE_CLASS_TEXT
                            | EditorInfo.TYPE_TEXT_VARIATION_PASSWORD);
                }
                
                //Move cursor to the end position of password.
                String password = etPwd.getText().toString();
                etPwd.setSelection(password.length());
                etPwd.invalidate();
            }
        });
        tvShowPwd = (TextView)findViewById(R.id.show_password_hint);
        tvShowPwd.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.v(TAG, "tvShowPwd.onClick()... " + v);
                if (cbShowPwd.isChecked()) {
                    cbShowPwd.setChecked(false);
                } else {
                    cbShowPwd.setChecked(true);
                }
            }
        });

        registerReceiver();
    }

    private class RegisterTaskReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.v(TAG, "onReceive()... ");
            if (CoreService.ACTION_REGISTER_OK.equals(intent.getAction())) {
                Log.v(TAG, "onReceive(), >>ACTION_REGISTER_OK");
                new AlertDialog.Builder(context)
                .setTitle(R.string.app_name)
                //.setIcon(R.drawable.img1)
                .setMessage(R.string.registerOK)
                .setPositiveButton(R.string.login, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(AccountRegister.this, LoginUsers.class);
                        intent.putExtra("username", account);
                        intent.putExtra("password", password);
                        startActivity(intent);
                    }
                })
                /*.setNeutralButton("��ͣ", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })*/
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        
                    }
                }).show();

            } else if (CoreService.ACTION_REGISTER_FAIL.equals(intent.getAction())) {
                Log.v(TAG, "onReceive(), >>ACTION_REGISTER_FAIL");
                //if (CoreService.RegisterStatus.FLAG_OFF == intent.getFlags()) {
                    if ("conflict".equals(intent.getStringExtra("register_error"))) {
                        Toast.makeText(AccountRegister.this, getString(R.string.register_failed)+
                                "\r\n"+getString(R.string.register_failed_already_exist), Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(AccountRegister.this, getString(R.string.register_failed)+
                                "\r\n"+intent.getStringExtra("register_error"), Toast.LENGTH_LONG).show();
                    }
                //}
            } else if (CoreService.ACTION_CONNECT_FAIL.equals(intent.getAction())) {
                Log.v(TAG, "onReceive(), >>ACTION_CONNECT_STATUS_CHANGE");
                //Toast.makeText(AccountRegister.this, getString(R.string.connect_failed)+
                //		"\r\n"+intent.getStringExtra("connect_error"), Toast.LENGTH_LONG).show();
                Toast.makeText(AccountRegister.this, getString(R.string.connect_failed),
                        Toast.LENGTH_LONG).show();
            } else {
                
            }
        }
    }

    private View.OnClickListener btnRegisterListener = new View.OnClickListener(){

        public void onClick(View v) {
            Log.v(TAG, "btnRegisterListener.onClick()... "+v);

            account = etAccountUsername.getText().toString();
            accountName = etAccountName.getText().toString();
            password = etPwd.getText().toString();
            
            if (TextUtils.isEmpty(account) || TextUtils.isEmpty(accountName) || TextUtils.isEmpty(password)) {
                Toast.makeText(AccountRegister.this, R.string.account_register_should_not_empty, Toast.LENGTH_LONG).show();
                return;
            }

            if (!CharUtils.isValidUsername(account)) {
                Toast.makeText(AccountRegister.this, R.string.account_register_username_format, Toast.LENGTH_LONG).show();
                return;
            }
            if (!CharUtils.isValidPassword(password)) {
                Toast.makeText(AccountRegister.this, R.string.account_register_password_format, Toast.LENGTH_LONG).show();
                return;
            }

            /*Editor editor = xs.getSharedPreferencesReg().edit();
            editor.putString("username", account);
            editor.putString("name", accountName);
            editor.putString("password", password);
            editor.commit();*/

            if (xs != null) {
                Bundle bundle = new Bundle();
                bundle.putString("user", account);
                bundle.putString("name", accountName);
                bundle.putString("pwd", password);
                
                xs.register(bundle);
            }
        }
    };

    private View.OnClickListener btnCancelListener = new View.OnClickListener(){

        public void onClick(View v) {
            Log.v(TAG, "btnCancelListener.onClick()... "+v);
            // TODO Auto-generated method stub
            finish();
        }
    };

    @Override
    public void onPause() {
        Log.v(TAG, "onPause()... ");
        super.onPause();
    }

    @Override
    protected void onStart() {
        Log.v(TAG, "onStart()... ");
        super.onStart();
    }

    @Override
    protected void onStop() {
        Log.v(TAG, "onStop()... ");
        super.onStop();
    }
    
    @Override
    protected void onDestroy() {
        Log.v(TAG, "onDestroy()... ");
        super.onDestroy();
        unregisterReceiver();
    }
    
    private void registerReceiver() {
        registerTaskFilter = new IntentFilter();
        registerTaskFilter.addAction(CoreService.ACTION_CONNECT_OK);
        registerTaskFilter.addAction(CoreService.ACTION_CONNECT_FAIL);
        registerTaskFilter.addAction(CoreService.ACTION_REGISTER_OK);
        registerTaskFilter.addAction(CoreService.ACTION_REGISTER_FAIL);
        registerTaskReceiver = new RegisterTaskReceiver();
        registerReceiver(registerTaskReceiver, registerTaskFilter);
    }
    
    private void unregisterReceiver() {
        if (registerTaskReceiver != null) {
            unregisterReceiver(registerTaskReceiver);
            registerTaskReceiver = null;
        }
    }
}

