package com.byteshaft.ghostrecorder;

import android.app.Activity;
import android.content.ComponentName;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

public class MainActivity extends Activity implements Switch.OnCheckedChangeListener,
        View.OnClickListener {

    private Switch mServiceSwitch;
    private Button mButtonOk;
    private EditText mPasswordEntry, mBatteryLevelEntry;
    private SharedPreferences mPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setFinishOnTouchOutside(false);
        mServiceSwitch = (Switch) findViewById(R.id.service_switch);
        mServiceSwitch.setOnCheckedChangeListener(this);
        mButtonOk = (Button) findViewById(R.id.button_ok);
        mBatteryLevelEntry = (EditText) findViewById(R.id.battery_entry);
        mPasswordEntry = (EditText) findViewById(R.id.password_entry);
        mPreferences = getPreferenceManager();    }

    @Override
    protected void onResume() {
        super.onResume();
        mServiceSwitch.setChecked(mPreferences.getBoolean("service_state", false));
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch (buttonView.getId()) {
            case R.id.service_switch:
                mButtonOk.setEnabled(isChecked);
        }
    }

    private void enableRecorderService(boolean enable) {
        mPreferences.edit().putBoolean("service_state", enable).apply();
    }

    private SharedPreferences getPreferenceManager() {
        return PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_ok:
                if (TextUtils.isEmpty(mPasswordEntry.getText()) ||  TextUtils.isEmpty(mBatteryLevelEntry.getText())) {
                    Toast.makeText(getApplicationContext(), "One of more fields are empty", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(), "Service Activated", Toast.LENGTH_SHORT).show();
                    mPreferences.edit().putString("service_password", mPasswordEntry.getText().toString()).apply();
                    mPreferences.edit().putString("battery_level", mBatteryLevelEntry.getText().toString()).apply();
                    enableRecorderService(true);
                    finish();
                    /// hide app icon form  app drawer
//                    PackageManager packageManager = getPackageManager();
//                    ComponentName componentName = new ComponentName(getApplicationContext(),
//                            com.byteshaft.ghostrecorder.MainActivity.class);
//                    packageManager.setComponentEnabledSetting(componentName,
//                            PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
//                            PackageManager.DONT_KILL_APP);
                }
                break;
            case R.id.button_cancel:
                finish();
                break;
        }
    }
}