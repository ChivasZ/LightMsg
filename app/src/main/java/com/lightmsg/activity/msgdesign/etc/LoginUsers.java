package com.lightmsg.activity.msgdesign.etc;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.lightmsg.LightMsg;
import com.lightmsg.R;
import com.lightmsg.activity.msgdesign.LightMsgActivity;
import com.lightmsg.service.CoreService;
import com.lightmsg.service.CoreService.Account;
import com.lightmsg.util.CharUtils;
import com.lightmsg.util.CountDownTimer;

public class LoginUsers extends AppCompatActivity implements OnGestureListener{

    private static final String TAG = LoginUsers.class.getName();

    private boolean bLoginOption;

    private LightMsg app = null;
    private CoreService xs = null;

    private LoginTaskReceiver registerTaskReceiver = null;
    private IntentFilter registerTaskFilter = null;

    private Button btnLogin;
    private Button btnRegister;
    private EditText etAccount;
    private EditText etPwd;
    private String account;
    private String password;
    private CheckBox cbRememberPwd;
    private CheckBox cbAutoLogin;
    private TextView tvRememberPwd;
    private TextView tvAutoLogin;
    private int rememberPassword = 0;
    private int autoLogin = 0;

    private ProgressDialog progressDialog;
    private CountDownTimer cdt;

    private GestureDetector gd;

    //private LayoutInflater mLayoutflater;
    //private LinearLayout mTopLayout = null;
    NestedScrollView nsv;

    @SuppressWarnings("deprecation")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.v(TAG, "onCreate()...");
        super.onCreate(savedInstanceState);
        app = (LightMsg)getApplication();
        xs = app.xs;

        //getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN |
        //		WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        //requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.login_design_layout);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        /*LinearLayout login = (LinearLayout)findViewById(R.id.login);
        login.setBackgroundResource(R.anim.anim_login);
        final AnimationDrawable animationDrawable =(AnimationDrawable)login.getBackground();
        login.post(new Runnable() {
            @Override
            public void run()  {
                Log.v(TAG, "start animation.");
                animationDrawable.start();
            }
        });*/

        //for Title display.
        /*mLayoutflater = getLayoutInflater();
        mTopLayout = (LinearLayout)findViewById(R.id.login_top);
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        mTopLayout.addView(mLayoutflater.inflate(R.layout.login_top, null), params);
        TextView tvTopCenter = (TextView)mTopLayout.findViewById(R.id.top_center);
        tvTopCenter.setText(R.string.login);*/

        /*FrameLayout common = (FrameLayout)findViewById(R.id.login_common_layout);
        common.setOnTouchListener(new OnTouchListener() {

            @Override
            public boolean onTouch(View arg0, MotionEvent event) {
                Log.v(TAG, "common.onTouch()...");
                return gd.onTouchEvent(event);
            }

        });*/
        nsv = (NestedScrollView) findViewById(R.id.nsv);

        etAccount = (EditText)findViewById(R.id.account);
        etPwd = (EditText)findViewById(R.id.account_password);
        btnLogin = (Button)findViewById(R.id.btnLogin);
        btnLogin.setOnClickListener(btnLoginListener);
        btnRegister = (Button)findViewById(R.id.btnJumpReg);
        btnRegister.setOnClickListener(btnJumpRegListener);

        etAccount.addTextChangedListener(accountTextWatcher);

        cbRememberPwd = (CheckBox)findViewById(R.id.remember_password);
        //bRememberPassword = cbRememberPwd.isChecked();
        cbRememberPwd.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Log.v(TAG, "cbRememberPwd.onCheckedChanged()... "+buttonView);
                if (isChecked) {
                    rememberPassword = 1;
                } else {
                    rememberPassword = 0;
                    //AutoLogin ON only if RememberPassword is ON.
                    cbAutoLogin.setChecked(false);
                }
            }
        });
        //Combine hint with CheckBox.
        tvRememberPwd = (TextView)findViewById(R.id.remember_password_hint);
        tvRememberPwd.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.v(TAG, "tvRememberPwd.onClick()... "+v);
                if (rememberPassword == 1) {
                    cbRememberPwd.setChecked(false);
                } else {
                    cbRememberPwd.setChecked(true);
                }
            }
        });

        cbAutoLogin = (CheckBox)findViewById(R.id.auto_login);
        cbAutoLogin.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Log.v(TAG, "cbAutoLogin.onCheckedChanged()... "+buttonView);
                if (isChecked) {
                    autoLogin = 1;
                    //If AutoLogin ON, RememberPassword must be ON.
                    cbRememberPwd.setChecked(true);
                } else {
                    autoLogin = 0;
                }
            }
        });
        //Combine hint with CheckBox.
        tvAutoLogin = (TextView)findViewById(R.id.auto_login_hint);
        tvAutoLogin.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.v(TAG, "tvAutoLogin.onClick()... "+v);
                if (autoLogin == 1) {
                    cbAutoLogin.setChecked(false);
                } else {
                    cbAutoLogin.setChecked(true);
                }
            }
        });

        if (isLastLogined()) {
            //Firstly checking AutoLogin, then RememberPassword.
            autoLogin = isLastAutoLogin()?1:0;
            cbAutoLogin.setChecked(autoLogin == 1);
            rememberPassword = isLastRememberPwd()?1:0;
            cbRememberPwd.setChecked(rememberPassword == 1);
        } else {
            //Default options.
            cbAutoLogin.setChecked(true);
            cbRememberPwd.setChecked(true);
        }

        // Auto login after register or other Intents.
        String user = getIntent().getStringExtra("username");
        String pwd = getIntent().getStringExtra("password");
        if (user != null && !user.isEmpty()
                && pwd != null && !pwd.isEmpty()) {
            Log.v(TAG, "onCreate(), Use the username and pwd from Intent");
            account = user;
            etAccount.setText(account);
            etAccount.setSelection(account.length());

            password = pwd;
            etPwd.setText(password);
            etPwd.setSelection(password.length());

            startLogin();
        } else
            // Check if "remember password"&"auto login" was checked, then preset conditionally.
            Log.v(TAG, "onCreate(), checking \"remember_password\" option...");
        if (isLastRememberPwd()) {
            Log.v(TAG, "onCreate(), \"remember_password\" option ON");
            account = getLastUser();
            etAccount.setText(account);
            etAccount.setSelection(account.length());

            password = getLastUsersPwd();
            etPwd.setText(password);
            etPwd.setSelection(password.length());
        } else {
            Log.v(TAG, "onCreate(), \"remember_password\" option OFF");
            account = getLastUser();
            if (!account.isEmpty()) {
                etAccount.setText(account);
                etPwd.requestFocus();
            }
        }

        if (!bLoginOption) {
            View lo = findViewById(R.id.login_options);
            lo.setVisibility(View.GONE);
        }

        gd = new GestureDetector(this);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE |
                WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
    }

    private boolean setLastUserInfo(String lu, String lp, int rp, int al) {
        Log.v(TAG, "setLastUser(), lu="+lu+", lp="+lp+", rp="+rp+", al="+al);

        if (xs != null) {
            Account account = xs.getAccount();
            if (account == null) {
                account = xs.new Account();
            }
            account.account = lu;
            account.nick = "昵称";
            account.pwd = lp;
            account.gender = "未知";
            account.rem_pwd = rp;
            account.auto_log = al;
            
            xs.setAccount(account);
            /*Editor editor = xs.getSharedPreferencesLogin().edit();
            editor.putString("username", lu);
            editor.putString("password", lp);
            editor.putBoolean("remember_password", rp);
            editor.putBoolean("auto_login", al);
            editor.commit();*/
        }

        return true;
    }

    private boolean isLastLogined() {
        return hasLastUser();
    }

    private boolean hasLastUser() {
        if (xs != null) {

            Account account = xs.getAccount();
            if (account != null && account.account != null && !account.account.isEmpty()) {
                Log.v(TAG, "hasLastUser(), has \"username\"");
                return true;
            }
            /*if (xs.getSharedPreferencesLogin().getString("username", "").isEmpty()) {
                Log.v(TAG, "hasLastUser(), NOT has \"username\"");
                return false;
            } else {
                Log.v(TAG, "hasLastUser(), HAS \"username\"");
                return true;
            }*/
        }
        Log.e(TAG, "hasLastUser(), xs == null");
        return false;
    }


    private String getLastUser() {
        String lu = "";
        if (xs != null) {
            Account account = xs.getAccount();
            if (account != null && account.account != null && !account.account.isEmpty()) {
                Log.v(TAG, "getLastUser(), has \"username\"");
                lu = account.account;
            }
            //lu = xs.getSharedPreferencesLogin().getString("username", "");
            Log.v(TAG, "getLastUser(), \"username\"="+lu);
        } else {
            Log.e(TAG, "getLastUser(), xs == null");
        }
        return lu;
    }

    private boolean hasLastUsersPwd() {
        if (xs != null) {
            Account account = xs.getAccount();
            if (account != null && account.pwd != null && !account.pwd.isEmpty()) {
                Log.v(TAG, "hasLastUsersPwd(), has \"password\"");
                return true;
            }
            /*if (xs.getSharedPreferencesLogin().getString("password", "").isEmpty()) {
                Log.v(TAG, "hasLastUsersPwd(), NOT has \"password\"");
                return false;
            } else {
                Log.v(TAG, "hasLastUsersPwd(), HAS \"password\"");
                return true;
            }*/
        }

        Log.e(TAG, "hasLastUsersPwd(), xs == null");
        return false;

    }

    private String getLastUsersPwd() {
        String lp = "";
        if (xs != null) {
            Account account = xs.getAccount();
            if (account != null && account.pwd != null && !account.pwd.isEmpty()) {
                Log.v(TAG, "getLastUsersPwd(), has \"password\"");
                lp = account.pwd;
            }
            //lp = xs.getSharedPreferencesLogin().getString("password", "");
            Log.v(TAG, "getLastUsersPwd(), \"password\"="+lp);
        } else {
            Log.e(TAG, "getLastUsersPwd(), xs == null");
        }
        return lp;
    }

    private boolean isLastRememberPwd() {
        if (xs != null) {
            Account account = xs.getAccount();
            if (account != null && account.rem_pwd == 1) {
                Log.v(TAG, "isLastRememberPwd(), \"rem_pwd\" true");
                return true;
            }
            /*if (xs.getSharedPreferencesLogin().getBoolean("remember_password", false)) {
                Log.v(TAG, "isLastRememberPwd(), \"remember_password\" is ON...");
                return (hasLastUser()&&hasLastUsersPwd());
            } else {
                Log.v(TAG, "isLastRememberPwd(), \"remember_password\" is OFF");
                return false;
            }*/
        }
        Log.e(TAG, "isLastRememberPwd(), xs == null");
        return false;

    }

    private boolean isLastAutoLogin() {
        if (xs != null) {
            Account account = xs.getAccount();
            if (account != null && account.auto_log == 1) {
                Log.v(TAG, "isLastAutoLogin(), \"auto_log\" true");
                return true;
            }

            /*if (xs.getSharedPreferencesLogin().getBoolean("auto_login", false)) {
                Log.v(TAG, "isLastAutoLogin(), \"auto_login\" is ON...");
                return (hasLastUser()&&hasLastUsersPwd()&&isLastRememberPwd());
            } else {
                Log.v(TAG, "isLastAutoLogin(), \"auto_login\" is OFF");
                return false;
            }*/
        }
        Log.e(TAG, "isLastAutoLogin(), xs == null");
        return false;

    }

    private TextWatcher accountTextWatcher = new TextWatcher() {

        @Override
        public void afterTextChanged(Editable arg0) {
            Log.v(TAG, "accountTextWatcher.afterTextChanged()...");
        }

        @Override
        public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
                int arg3) {
            Log.v(TAG, "accountTextWatcher.beforeTextChanged()...");			
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            Log.v(TAG, "accountTextWatcher.onTextChanged()...");
            etPwd.setText("");
            etPwd.setSelection(0);
        }

    };

    @Override
    protected void onResume() {
        Log.v(TAG, "onResume()...");
        super.onResume();
    }

    @Override
    public void onPause() {
        Log.v(TAG, "onPause()... ");
        super.onPause();
        unregisterReceiver();
    }

    @Override
    protected void onStart() {
        Log.v(TAG, "onStart()...");
        super.onStart();

        registerReceiver();

        //Check if auto login.
        Log.v(TAG, "onStart(), checking \"auto login\" option, if auto login...");
        if (isLastAutoLogin()) {
            Log.v(TAG, "onStart(), \"auto_login\" option ON");
            startLogin();
        } else {
            Log.v(TAG, "onStart(), \"auto_login\" option OFF");
        }
    }

    @Override
    protected void onStop() {
        Log.v(TAG, "onStop()...");
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        Log.v(TAG, "onDestroy()...");
        super.onDestroy();
    }

    private class LoginTaskReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.v(TAG, "onReceive()... "+intent.getAction());
            if (CoreService.ACTION_LOGIN_OK.equals(intent.getAction())) {
                Log.v(TAG, "onReceive(), >>ACTION_LOGIN_OK");
                startRosterListActivity(context);
                xs.setAccount(account, "昵称", "未知", password, 1, rememberPassword, autoLogin, "portrait");
                endLogin();
                finish();
            } else if (CoreService.ACTION_LOGIN_FAIL.equals(intent.getAction())) {
                Log.v(TAG, "onReceive(), >>ACTION_LOGIN_FAIL");
                endLogin();
                //Toast.makeText(LoginUsers.this, getString(R.string.login_failed)+
                //		": "+intent.getStringExtra("login_error"), Toast.LENGTH_LONG).show();
                Snackbar.make(nsv, getString(R.string.login_failed)+
                        ": "+intent.getStringExtra("login_error"), Snackbar.LENGTH_SHORT).show();
            } else if (CoreService.ACTION_CONNECT_FAIL.equals(intent.getAction())) {
                Log.v(TAG, "onReceive(), >>ACTION_CONNECT_STATUS_CHANGE");
                endLogin();
                //Toast.makeText(LoginUsers.this, getString(R.string.login_failed_network)+
                //		"\r\n"+intent.getStringExtra("login_error"), Toast.LENGTH_LONG).show();
                Snackbar.make(nsv, getString(R.string.login_failed_network)+
                                "\r\n"+intent.getStringExtra("login_error"), Snackbar.LENGTH_SHORT).show();
                //Toast.makeText(LoginUsers.this, getString(R.string.connect_failed)+
                //		"\r\n"+intent.getStringExtra("connect_error"), Toast.LENGTH_LONG).show();
            } else {

            }
        }
    }

    private void registerReceiver() {
        Log.v(TAG, "registerReceiver()...");
        registerTaskFilter = new IntentFilter();
        registerTaskFilter.addAction(CoreService.ACTION_CONNECT_OK);
        registerTaskFilter.addAction(CoreService.ACTION_CONNECT_FAIL);
        //registerTaskFilter.addAction(CoreService.ACTION_REGISTER_OK);
        //registerTaskFilter.addAction(CoreService.ACTION_REGISTER_FAIL);
        registerTaskFilter.addAction(CoreService.ACTION_LOGIN_OK);
        registerTaskFilter.addAction(CoreService.ACTION_LOGIN_FAIL);
        registerTaskReceiver = new LoginTaskReceiver();
        registerReceiver(registerTaskReceiver, registerTaskFilter);
    }

    private void unregisterReceiver() {
        Log.v(TAG, "unregisterReceiver()...");
        if (registerTaskReceiver != null) {
            unregisterReceiver(registerTaskReceiver);
            registerTaskReceiver = null;
        }
    }

    private View.OnClickListener btnLoginListener = new View.OnClickListener(){
        public void onClick(View v) {
            Log.v(TAG, "btnLoginListener.onClick()... "+v);

            startLogin();
        }
    };

    private View.OnClickListener btnJumpRegListener = new View.OnClickListener(){

        public void onClick(View v) {
            Log.v(TAG, "btnCancelListener.onClick()... "+v);
            startAccountRegisterActivity(LoginUsers.this);
        }
    };

    private void startAccountRegisterActivity(Context context) {
        Log.v(TAG, "startAccountRegisterActivity()... ");
        Intent intent = new Intent(context, AccountRegister.class);
        startActivity(intent);
        overridePendingTransition(R.anim.dync_in_from_left, R.anim.dync_out_to_right);
        //finish();
    }

    private void startRosterListActivity(Context context) {
        Log.v(TAG, "startRosterListActivity()... ");
        Intent intent = new Intent(context, LightMsgActivity.class);
        //Intent intent = new Intent(context, FishActivity2.class);
        startActivity(intent);
        overridePendingTransition(R.anim.dync_in_from_right, R.anim.dync_out_to_left);
        finish();
    }

    private void startLogin() {
        Log.v(TAG, ">>>>>>startLogin()... ");

        //Save login information.
        account = etAccount.getText().toString();
        password = etPwd.getText().toString();
        if (TextUtils.isEmpty(account) || TextUtils.isEmpty(password)) {
            endLogin();
            Toast.makeText(LoginUsers.this, R.string.login_failed_null, Toast.LENGTH_LONG).show();
            return;
        }

        if (!CharUtils.isValidUsername(account)) {
            endLogin();
            Toast.makeText(LoginUsers.this, R.string.login_failed_username_format, Toast.LENGTH_LONG).show();
            return;
        }
        if (!CharUtils.isValidPassword(password)) {
            endLogin();
            Toast.makeText(LoginUsers.this, R.string.login_failed_password_format, Toast.LENGTH_LONG).show();
            return;
        }

        if (bLoginOption) {
            rememberPassword = cbRememberPwd.isChecked() ? 1 : 0;
            autoLogin = cbAutoLogin.isChecked() ? 1 : 0;
        } else {
            rememberPassword = 1;
            autoLogin = 1;
        }

        setLastUserInfo(account, password, rememberPassword, autoLogin);

        //Show progress dialog.
        progressDialog = ProgressDialog.show(this, getString(R.string.waiting), getString(R.string.logging_in), true);

        // Count down, wait for CoreService binding.
        cdt = new CountDownTimer(20000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                Log.v(TAG, "CountDownTimer.onTick()...");
            }
            @Override
            public void onFinish() {
                Log.v(TAG, "CountDownTimer.onFinish()...");
                endLogin();
                //Toast.makeText(LoginUsers.this, R.string.login_failed_timeout, Toast.LENGTH_LONG).show();
                Snackbar.make(nsv, R.string.login_failed_timeout, Snackbar.LENGTH_SHORT).show();
            }
        }.start();

        //Login now.
        if (xs != null) {
            //xs.setLoadRosterAtLogin(true);
            xs.login();
        }
    }

    private void endLogin() {
        Log.v(TAG, "<<<<<<endLogin()... ");
        if (cdt != null) {
            cdt.cancel();
        }
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        Log.v(TAG, "onTouchEvent()...");

        return false;
        //return this.gd.onTouchEvent(event);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        Log.v(TAG, "dispatchTouchEvent...");
        boolean ret = false;
        //ret = gd.onTouchEvent(ev);
        Log.v(TAG, "dispatchTouchEvent, ret="+ret);
        return (ret ? ret:super.dispatchTouchEvent(ev));
    } 


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
            startLogin();
            return true;
        } else if (before.getX() - current.getX() < -120){//Right fling.
            Log.v(TAG, "GestureDetectorListener.onFling(), RIGHT FLING>>>");
            startAccountRegisterActivity(LoginUsers.this);
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
