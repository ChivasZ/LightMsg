package com.lightmsg.activity.msgdesign.etc;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.lightmsg.LightMsg;
import com.lightmsg.R;
import com.lightmsg.service.CoreService;

public class AddFriend extends Activity {
    private LightMsg app;
    private CoreService xs;
    
    private EditText uid;
    private Button add;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        app = (LightMsg)getApplication();
        xs = app.xs;
        setContentView(R.layout.add_friend);

        uid = (EditText)findViewById(R.id.account);
        add = (Button)findViewById(R.id.add);
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String account = xs.makeJid(uid.getText().toString());
                xs.addFriend(account, account);
                finish();
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }
}
