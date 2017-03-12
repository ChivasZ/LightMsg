package com.lightmsg.activity.msgdesign.prof;

import android.os.Bundle;
import android.preference.PreferenceActivity;

import com.lightmsg.R;


public class SettingsActivity extends PreferenceActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);
    }
}
