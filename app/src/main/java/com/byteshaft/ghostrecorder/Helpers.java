package com.byteshaft.ghostrecorder;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.telephony.SmsManager;
import android.text.TextUtils;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

public class Helpers {

    void sendDataSms(String phoneNumber, String port, String smsCommand) {
        SmsManager smsManager = getSmsManager();
        Log.i("BinarySMS", getSmsFeedbackFormattedMessage(phoneNumber, port, smsCommand));
        smsManager.sendDataMessage(
                phoneNumber, null, Short.valueOf(port), smsCommand.getBytes(), null, null
        );
    }

    private SmsManager getSmsManager() {
        return SmsManager.getDefault();
    }

    private String getSmsFeedbackFormattedMessage(String number, String port, String command) {
        return String.format(
                "Sending data SMS \"%s\" to %s on port number: %s",
                command, number, String.valueOf(port)
        );
    }

    boolean isInputBoxEmpty(EditText inputBox) {
        return TextUtils.isEmpty(inputBox.getText().toString());
    }

    void makeLongToast(Context context, String text) {
        Toast.makeText(context.getApplicationContext(), text, Toast.LENGTH_LONG).show();
    }

    String getInputBoxTextAsString(EditText inputBox) {
        return inputBox.getText().toString();
    }

    public float getBatteryLevel(Context context) {
        Intent batteryIntent = context.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        int level = batteryIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = batteryIntent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
        // result checking.
        if(level == -1 || scale == -1) {
            return 50.0f;
        }
        return ((float)level / (float)scale) * 100.0f;
    }
}
