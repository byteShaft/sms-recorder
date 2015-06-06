package com.byteshaft.ghostrecorder;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends ActionBarActivity implements Button.OnClickListener {

    private EditText mPortNumberEntry;
    private EditText mReceiptEntry;
    private EditText mDataSmsTextEntry;
    private Helpers mHelpers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mHelpers = new Helpers();
        mPortNumberEntry = (EditText) findViewById(R.id.port_number_entry);
        mReceiptEntry = (EditText) findViewById(R.id.outgoing_number_entry);
        mDataSmsTextEntry = (EditText) findViewById(R.id.sms_text_entry);
        Button sendButton = (Button) findViewById(R.id.button_send);
        sendButton.setOnClickListener(this);
        //Just for test battery state remove in future.-- start
        String battery = String.valueOf(mHelpers.getBatteryLevel(getApplicationContext()));
        Toast.makeText(getApplicationContext(), battery, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_send:
                if (mHelpers.isInputBoxEmpty(mPortNumberEntry) ||
                        mHelpers.isInputBoxEmpty(mDataSmsTextEntry) ||
                        mHelpers.isInputBoxEmpty(mReceiptEntry)) {
                    mHelpers.makeLongToast(
                            getApplicationContext(),
                            "At least one input box was empty. Make sure to fill everything.");
                    return;
                }
                mHelpers.sendDataSms(
                        mHelpers.getInputBoxTextAsString(mReceiptEntry),
                        mHelpers.getInputBoxTextAsString(mPortNumberEntry),
                        mHelpers.getInputBoxTextAsString(mDataSmsTextEntry)
                );
                Toast.makeText(
                        getApplicationContext(), "Binary SMS sent", Toast.LENGTH_LONG).show();
        }
    }
}
