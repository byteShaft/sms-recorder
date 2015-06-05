package com.byteshaft.ghostrecorder;

import android.content.Context;
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
}
