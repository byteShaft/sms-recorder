package com.byteshaft.ghostrecorder;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.BatteryManager;
import android.util.Log;
import android.widget.Toast;


public class BinarySmsReceiver extends BroadcastReceiver {

    private String mPassword;
    private String mActionRaw;
    private String mAction;
    private String mTime;
    private boolean mAutoResponse;
    private String batteryThresholdValue;
    private SharedPreferences mPreferences;
    private int batteryValueCheck;
    private int mDurationRecord;
    private int hours;
    private int minutes;
    private int intervals;
    private Context mContext;

    @Override
    public void onReceive(Context context, Intent intent) {
        mContext = context;
        Log.i(AppGlobals.LOG_TAG, "Message Received");

        Intent batteryIntent = context.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        int currentBatteryLevel = batteryIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);


        mPreferences = Helpers.getPreferenceManager(context);
        /* Check if Recorder Service was enabled by the user. Only then
        proceed any further.
         */
        boolean isServiceEnabled = mPreferences.getBoolean("service_state", false);
        batteryThresholdValue = mPreferences.getString("battery_level", "5");
        if (!isServiceEnabled) {
            Toast.makeText(context, "Service is disabled", Toast.LENGTH_SHORT).show();
            return;
        }

        /* Check if the incoming binary SMS contains at least 2 commands, separated
        by an underscore. If the command is short just return and don't do anything.
         */

        String incomingSmsText = Helpers.decodeIncomingSmsText(intent);
        String[] smsCommand = incomingSmsText.split("_");

        if (!isSmsCommandOfValidLength(smsCommand)) {
            Log.e(AppGlobals.LOG_TAG, "Invalid Command.");
            return;
        }
        mPassword = smsCommand[0];
        if (!isPasswordValid(mPassword)) {
            Log.e(AppGlobals.LOG_TAG, "Invalid Password.");
            return;
        }
        Intent smsServiceIntent = new Intent(
                context.getApplicationContext(), AudioRecorderService.class);

        System.out.println(smsCommand.length);

        if (smsCommand.length == 2) {
            System.out.println("Command 2");
            mActionRaw = smsCommand[1];

            /* Checks to see if the Command is valid. If yes, progresses further.*/

            if (!isActionValid(mActionRaw)) {
                Log.e(AppGlobals.LOG_TAG, "Invalid Command.");
            } else if (mAction.equals("start") && batteryValueCheck > currentBatteryLevel) {
                Log.e(AppGlobals.LOG_TAG, "Battery level below specified value");
            } else {
                if (mAction.equals("start")) {
                    if (!CustomMediaRecorder.isRecording()) {
                        smsServiceIntent.putExtra("ACTION", mAction);
                        smsServiceIntent.putExtra("RECORD_TIME", 1000 * 60 * 3600);
                        if (mAutoResponse) {
                            Log.i(AppGlobals.LOG_TAG, "Starting recording, response generated");
                            // FIXME: Implement sending a response SMS.
                        }
                        context.startService(smsServiceIntent);
                    } else {
                        Log.i(AppGlobals.LOG_TAG, "Recording already in progress");
                    }
                } else if (mAction.equals("stop")) {
                    if (CustomMediaRecorder.isRecording()) {
                        Log.i(AppGlobals.LOG_TAG, "Stopping Recording");
                        AudioRecorderService.instance.mRecorderHelpers.stopRecording();
                    } else {
                        Log.i(AppGlobals.LOG_TAG, "No recording in progress");
                    }
                }
            }
        } else if (smsCommand.length == 3) {
            mActionRaw = smsCommand[1];
            mTime = smsCommand[2];
            if (!isActionValid(mActionRaw)) {
                Log.e(AppGlobals.LOG_TAG, "Invalid Command.");
            } else if (mAction.equals("start") && batteryValueCheck > currentBatteryLevel) {
                Log.e(AppGlobals.LOG_TAG, "Battery level below specified value");
            } else if (!isTimeValid(mTime)) {
                Log.e(AppGlobals.LOG_TAG, "Invalid Command");
            } else {
                Log.i(AppGlobals.LOG_TAG, "Hours" + hours);
                Log.i(AppGlobals.LOG_TAG, "Minutes" + minutes);
                if (mAction.equals("start")) {
                    if (!CustomMediaRecorder.isRecording()) {
                        smsServiceIntent.putExtra("ACTION", mAction);
                        smsServiceIntent.putExtra("RECORD_TIME", mDurationRecord * 1000 * 60);
                        smsServiceIntent.putExtra("SCHEDULE", minutes);
                        if (mAutoResponse) {
                            Log.i(AppGlobals.LOG_TAG, "Starting recording, response generated");
                            // FIXME: Implement sending a response SMS.
                        }
                        context.startService(smsServiceIntent);
                    } else {
                        Log.i(AppGlobals.LOG_TAG, "Recording already in progress");
                    }
                } else if (mAction.equals("stop")) {
                    if (CustomMediaRecorder.isRecording()) {
                        Log.i(AppGlobals.LOG_TAG, "Stopping Recording");
                        AudioRecorderService.instance.mRecorderHelpers.stopRecording();
                    } else {
                        Log.i(AppGlobals.LOG_TAG, "No recording in progress");
                    }
                }
            }
        }

        // TODO: implement code or listener for battery level change.
        // This code WORKS. just need to hook some logic into BatteryChargeChangeListener class.
//        IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
//        BroadcastReceiver batteryListener = new BatteryChargeChangeListener();
//        context.getApplicationContext().registerReceiver(batteryListener, filter);
    }

    private boolean isPasswordValid(String password) {
        String currentPassword = mPreferences.getString("service_password", null);
        return password.equals(currentPassword);
    }

    private boolean isActionValid(String action) {
        String[] actionArray = action.split(":");
        if (actionArray.length == 1) {
            if (actionArray[0].equalsIgnoreCase("start")) {
                mAction = "start";
                return true;
            } else if (actionArray[0].equalsIgnoreCase("stop")) {
                mAction = "stop";
                return true;
            } else {
                return false;
            }
        } else if (actionArray.length == 2) {
            mAutoResponse = actionArray[1].equalsIgnoreCase("y") || actionArray[1].equalsIgnoreCase("yes");
            if (!mAutoResponse) {
                return false;
            }
            if (actionArray[0].equalsIgnoreCase("start")) {
                mAction = "start";
                return true;
            } else if (actionArray[0].equalsIgnoreCase("stop")) {
                mAction = "stop";
                return true;
            } else {
                return false;
            }
        } else if (actionArray.length == 3) {
            mAutoResponse = actionArray[1].equalsIgnoreCase("y") || actionArray[1].equalsIgnoreCase("yes");
            if (!mAutoResponse) {
                return false;
            }
            if (actionArray[0].equalsIgnoreCase("start")) {
                mAction = "start";
            } else if (actionArray[0].equalsIgnoreCase("stop")) {
                mAction = "stop";
            } else {
                return false;
            }
            batteryThresholdValue = actionArray[2];
            try {
                batteryValueCheck = Integer.parseInt(batteryThresholdValue);
            } catch (NumberFormatException e) {
                return false;
            }
            if (batteryValueCheck > 0 && batteryValueCheck <= 100) {
                return true;
            }
        }
        return false;
    }

    private boolean isSmsCommandOfValidLength(String[] command) {
        return command.length > 1 && command.length <= 5;
    }

    private boolean isTimeValid(String time) {
        Log.e(AppGlobals.LOG_TAG, time);
        String[] timeArray = time.split(":");
        try {
            mDurationRecord = Integer.valueOf(timeArray[0]);
        } catch (NumberFormatException e) {
            return false;
        }
        if (timeArray.length == 1) {
            return mDurationRecord > 0 && mDurationRecord <= 3600;
        } else if (timeArray.length == 2) {            
            if (mDurationRecord > 3600 || mDurationRecord < 0) {
                return false;
            }
            String when = timeArray[1];
            System.out.print(when);
            if(when.length() != 4){
                return false;
            }
            String[] splitWhen = when.split("(?<=\\G.{2})");
            try {
                hours = Integer.parseInt(splitWhen[0]);
                minutes = Integer.parseInt(splitWhen[1]);
            } catch (NumberFormatException e) {
                return false;
            } if (hours > 23 || minutes > 59) {
                return false;
            }
        } else if (timeArray.length == 3){
            if (mDurationRecord > 3600 || mDurationRecord < 0) {
                return false;
            }
            String when = timeArray[1];
            if(when.length() != 4){
                return false;
            }
            String[] splitWhen = when.split("(?<=\\G.{2})");
            try {
                hours = Integer.parseInt(splitWhen[0]);
                minutes = Integer.parseInt(splitWhen[1]);
            } catch (NumberFormatException e) {
                return false;
            } if (hours > 23 || minutes > 59) {
                return false;
            }
            String interval = timeArray[2];
            try {
                intervals = Integer.parseInt(interval);
            } catch (NumberFormatException e) {
                return false;
            }
            if (intervals < 0 || intervals > 10) {
                return false;
            }
        } return true;
    }
}
