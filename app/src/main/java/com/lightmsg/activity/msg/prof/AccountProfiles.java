package com.lightmsg.activity.msg.prof;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.lightmsg.R;
import com.lightmsg.LightMsg;
import com.lightmsg.service.CoreService;
import com.lightmsg.service.CoreService.Account;

public class AccountProfiles extends Activity {

	private static final String TAG = "LightMsg/" + AccountProfiles.class.getSimpleName();

	private LightMsg app = null;
	private CoreService xs = null;
	private IntentFilter profilesTaskFilter = null;
	private ProfilesTaskReceiver profilesTaskReceiver = null;

	private Button btnProfiles;
	private Button btnCancel;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.v(TAG, "onCreate()... ");
		super.onCreate(savedInstanceState);
		app = (LightMsg)getApplication();
		xs = app.xs;

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.account_profiles);

		btnProfiles = (Button)findViewById(R.id.btnProfiles);
		btnProfiles.setOnClickListener(btnProfilesListener);
		btnCancel = (Button)findViewById(R.id.btnCancel);
		btnCancel.setOnClickListener(btnCancelListener);

		registerReceiver();
	}

	private class ProfilesTaskReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			Log.v(TAG, "onReceive()... ");
			if (CoreService.ACTION_PROFILE_OK.equals(intent.getAction())) {
				Log.v(TAG, "onReceive(), >>ACTION_REGISTER_OK");
				new AlertDialog.Builder(context)
				.setTitle(R.string.app_name)
				//.setIcon(R.drawable.img1)
				.setMessage("Set profiles OK")
				.setPositiveButton(R.string.login, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						//
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

			} else if (CoreService.ACTION_PROFILE_FAIL.equals(intent.getAction())) {
				Log.v(TAG, "onReceive(), >>ACTION_REGISTER_FAIL");
			} else if (CoreService.ACTION_CONNECT_FAIL.equals(intent.getAction())) {
				Log.v(TAG, "onReceive(), >>ACTION_CONNECT_STATUS_CHANGE");
				Toast.makeText(AccountProfiles.this, getString(R.string.connect_failed)+
						"\r\n"+intent.getStringExtra("connect_error"), Toast.LENGTH_LONG).show();
			} else {

			}
		}
	}

	private View.OnClickListener btnProfilesListener = new View.OnClickListener(){

		public void onClick(View v) {
			Log.v(TAG, "btnProfilesListener.onClick()... "+v);

			EditText etoldpwd = (EditText)findViewById(R.id.account_old_password);
			EditText etnewpwd1 = (EditText)findViewById(R.id.account_new_password);
			EditText etnewpwd2 = (EditText)findViewById(R.id.account_new_password2);
			EditText etnewname = (EditText)findViewById(R.id.account_new_name);
			EditText etnewemail = (EditText)findViewById(R.id.account_new_email);
			EditText etnewportrait = (EditText)findViewById(R.id.account_new_portrait_path);

			String oldpwd = etoldpwd.getText().toString();
			if (oldpwd == null || oldpwd.isEmpty()) {
				Toast.makeText(AccountProfiles.this, "Should not be null", Toast.LENGTH_LONG).show();
				return;
			}
			
			String newpwd1 = etnewpwd1.getText().toString();
			String newpwd2 = etnewpwd2.getText().toString();
			if (newpwd1 == null || newpwd2 == null 
					|| newpwd2.isEmpty() || newpwd2.isEmpty()
					|| !newpwd1.equals(newpwd2)) {
				Toast.makeText(AccountProfiles.this, "Set the correct new password, pls", Toast.LENGTH_LONG).show();
				return;
			}

			//setUserInfo(etoldpwd.getText().toString(), etnewpwd1.getText().toString(),
			//		etnewname.getText().toString(), etnewemail.getText().toString(),
			//		etnewportrait.getText().toString());
			if (xs != null) {
				Bundle bundle = new Bundle();
				bundle.putString("user", xs.getAccount().account);
				bundle.putString("name", etnewname.getText().toString());
				bundle.putString("curpwd", oldpwd);
				bundle.putString("newpwd", newpwd1);
				
				xs.setProfiles(bundle);
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
		profilesTaskFilter = new IntentFilter();
		profilesTaskFilter.addAction(CoreService.ACTION_CONNECT_OK);
		profilesTaskFilter.addAction(CoreService.ACTION_CONNECT_FAIL);
		profilesTaskFilter.addAction(CoreService.ACTION_REGISTER_OK);
		profilesTaskFilter.addAction(CoreService.ACTION_REGISTER_FAIL);
		profilesTaskReceiver = new ProfilesTaskReceiver();
		registerReceiver(profilesTaskReceiver, profilesTaskFilter);
	}

	private void unregisterReceiver() {
		if (profilesTaskReceiver != null) {
			unregisterReceiver(profilesTaskReceiver);
			profilesTaskReceiver = null;
		}
	}

	private boolean setUserInfo(String un, String oldpwd, String newpwd, String name,
			String email, String relation, String group, String portrait) {
		Log.v(TAG, "setUserInfo()..");

		if (xs != null) {
			Account account = xs.getAccount();
			if (account == null) {
				account = xs.new Account();
			}
			account.account = un;
			account.nick = name;
			account.pwd = newpwd;
			account.gender = "未知";
			
			xs.setAccount(account);
			/*Editor editor = xs.getSharedPreferencesLogin().edit();
			editor.putString("username", un);
			editor.putString("password", oldpwd);
			editor.putString("email", email);
			editor.putString("name", name);
			editor.putString("newpwd", newpwd);
			editor.putString("relation", relation);
			editor.putString("group", group);
			editor.putString("portrait", portrait);
			editor.commit();*/
		}

		return true;
	}

	private boolean setUserInfo(String oldpwd, String newpwd, String name,
			String email, String portrait) {
		Log.v(TAG, "setUserInfo()..");

		if (xs != null) {

			Account account = xs.getAccount();
			if (account == null) {
				account = xs.new Account();
			}
			account.nick = name;
			account.pwd = newpwd;
			account.gender = "未知";

			xs.setAccount(account);

			/*Editor editor = xs.getSharedPreferencesLogin().edit();
			editor.putString("password", oldpwd);
			editor.putString("email", email);
			editor.putString("name", name);
			editor.putString("newpwd", newpwd);
			editor.putString("portrait", portrait);
			editor.commit();*/
		}

		return true;
	}
}

