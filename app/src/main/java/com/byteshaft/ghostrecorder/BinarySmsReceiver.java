package com.byteshaft.ghostrecorder;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.BatteryManager;
import android.util.Log;

public class BinarySmsReceiver extends BroadcastReceiver {

    private String LOG_TAG = AppGlobals.getLogTag(getClass());
    private String mAction;
    private boolean mAutoResponse;
    private String batteryThresholdValue;
    private SharedPreferences mPreferences;
    private int batteryValueCheck;
    private int mDurationRecord;
    private int mDelay;
    private int mTotalScheduledRecordingDuration;
    private boolean mInvalidCommandResponse;
    RecorderHelpers mRecordHelpers;

    @Override
    public void onReceive(Context context, Intent intent) {
        mRecordHelpers = new RecorderHelpers(context);
        AppGlobals.logInformation(LOG_TAG, "Message Received");
        Intent batteryIntent = context.registerReceiver(
                null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        int currentBatteryLevel = batteryIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);

        mPreferences = Helpers.getPreferenceManager(context);
        /* Check if the Recorder Service was enabled by the user. Only then
        proceed any further.
         */
        boolean isServiceEnabled = mPreferences.getBoolean("service_state", false);
        mInvalidCommandResponse = mPreferences.getBoolean("invalid_command_response", false);
        batteryThresholdValue = mPreferences.getString("battery_level", "5");
        if (!isServiceEnabled) {
            AppGlobals.logError(LOG_TAG, "The Recorder Service is disabled. Ignoring SMS command.");
            return;
        }

        /* Read the incoming binary SMS and make it parsable, by splitting with
        an underscore, which is our designed separator for differentiating sub
        commands.
         */

        String incomingSmsText = Helpers.decodeIncomingSmsText(intent);
        String[] smsCommand = incomingSmsText.split("_");

        /* Check, if the incoming binary SMS contains at least 2 commands, separated
        by an underscore. If the command length is shorter than that, just return
        and don't do anything.
         */

        if (!isSmsCommandOfValidLength(smsCommand)) {
            if (mInvalidCommandResponse) {
                // FIXME: Send SMS Response
            }
            AppGlobals.logError(LOG_TAG, "Invalid Command.");
            return;
        }

        /* Check if the password is valid in the incoming binary SMS,
        before doing anything further.
         */
        String mPassword = smsCommand[0];
        if (!isPasswordValid(mPassword)) {
            Log.e(LOG_TAG, "Invalid Password.");
            if (mInvalidCommandResponse) {
                // FIXME: Send SMS Response
            }
            AppGlobals.logError(LOG_TAG, "Invalid Password.");
            return;
        }

        Intent smsServiceIntent = new Intent(
                context.getApplicationContext(), AudioRecorderService.class);

        String actionRaw;
        /* If the SMS command contains two sub commands example: password_action */
        if (smsCommand.length == 2) {
            actionRaw = smsCommand[1];

            /* Check if the requested action in the SMS command is of valid format. */
            if (!isActionValid(actionRaw)) {
                AppGlobals.logError(LOG_TAG, "Invalid action command.");
                if (mInvalidCommandResponse) {
                    // FIXME: Send Response SMS
                }
            } else if (mAction.equals("start") && batteryValueCheck > currentBatteryLevel) {
                AppGlobals.logError(
                        LOG_TAG, "Current battery level is below specified value, recording " +
                                "request ignored.");
                if (mAutoResponse) {
                    // FIXME: Implement sending a response SMS.
                }
            } else if (mAction.equals("start")) {
                if (!CustomMediaRecorder.isRecording()) {
                    smsServiceIntent.putExtra("ACTION", mAction);
                    smsServiceIntent.putExtra("RECORD_TIME", 1000 * 60 * 3600);
                    if (mAutoResponse) {
                        Log.i(LOG_TAG, "Starting recording, response generated");
                        // FIXME: Implement sending a response SMS.
                    }
                    context.startService(smsServiceIntent);
                } else {
                    AppGlobals.logInformation(
                            LOG_TAG, "Recording already in progress, ignoring request");
                    if (mAutoResponse) {
                        // FIXME: Implement sending a response SMS.
                    }
                }
            } else if (mAction.equals("stop")) {
                if (CustomMediaRecorder.isRecording()) {
                    mRecordHelpers.stopRecording();
                    Log.i(LOG_TAG, "Stopping recording, response generated");
                    if (mAutoResponse) {
                        // FIXME: Implement sending a response SMS.
                    }
                } else {
                    AppGlobals.logInformation(
                            LOG_TAG, "Nothing to stop, no recording in progress.");
                    if (mAutoResponse) {
                        // FIXME: Implement sending a response SMS.
                    }
                }
//            } else if (mAction.equals("reset")) {
//                smsServiceIntent.putExtra("RESET", mAction);
//                AppGlobals.logInformation(
//                        LOG_TAG, "Reset Command Received, resetting schedules");
//                if (mAutoResponse) {
//                    // FIXME: Implement sending a response SMS.
//                }
            }
        /* If the SMS command contains three sub commands example: password_action_schedule */
        } else if (smsCommand.length == 3) {
            actionRaw = smsCommand[1];
            String time = smsCommand[2];
            if (!isActionValid(actionRaw)) {
                AppGlobals.logError(LOG_TAG, "Invalid action command.");
                if (mInvalidCommandResponse) {
                    // FIXME: Send SMS Response.
                }
            } else if (mAction.equals("start") && batteryValueCheck > currentBatteryLevel) {
                AppGlobals.logError(
                        LOG_TAG, "Current battery level is below specified value, recording " +
                                "request ignored.");
                if (mAutoResponse) {
                    // FIXME: Implement sending a response SMS.
                }
            } else if (!isTimeValid(time)) {
                AppGlobals.logError(LOG_TAG, "Invalid time command.");
                if (mInvalidCommandResponse) {
                    // FIXME: Implement sending a response SMS.
                }
            } else {
                if (mAction.equals("start")) {
                    if (!CustomMediaRecorder.isRecording()) {
                        smsServiceIntent.putExtra("ACTION", mAction);
                        smsServiceIntent.putExtra("RECORD_TIME", mDurationRecord * 1000 * 60);
                        smsServiceIntent.putExtra("DELAY", mDelay);
                        smsServiceIntent.putExtra("TOTAL_RECORDING_DURATION", mTotalScheduledRecordingDuration);
                        if (mAutoResponse) {
                            Log.i(LOG_TAG, "Starting recording, response generated");
                            // FIXME: Implement sending a response SMS.
                        }
                        context.startService(smsServiceIntent);
                    } else {
                        AppGlobals.logError(LOG_TAG, "Invalid Action Command.");
                        if (mInvalidCommandResponse) {
                            // FIXME: Implement sending a response SMS.
                        }
                    }
                } else if (mAction.equals("stop")) {
                    if (mInvalidCommandResponse) {
                        Log.i(LOG_TAG, "Invalid Command");
                        mRecordHelpers.stopRecording();
                        // FIXME: Implement sending a response SMS.
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
//            } else if (actionArray[0].equalsIgnoreCase("reset")) {
//                mAction = "reset";
//                return true;
            } else {
                return false;
            }
        } else if (actionArray.length == 2) {
            mAutoResponse = actionArray[1].equalsIgnoreCase("y") || actionArray[1].equalsIgnoreCase("yes");
            if (actionArray[0].equalsIgnoreCase("start")) {
                mAction = "start";
                return true;
            } else if (actionArray[0].equalsIgnoreCase("stop")) {
                mAction = "stop";
                return true;
//            } else if (actionArray[0].equalsIgnoreCase("reset")) {
//                mAction = "reset";
//                return true;
            } else {
                return false;
            }
        } else if (actionArray.length == 3) {
            mAutoResponse = actionArray[1].equalsIgnoreCase("y") || actionArray[1].equalsIgnoreCase("yes");
            if (actionArray[0].equalsIgnoreCase("start")) {
                mAction = "start";
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
        Log.e(LOG_TAG, time);
        String[] timeArray = time.split(":");
        try {
            mDurationRecord = Integer.valueOf(timeArray[0]);
        } catch (NumberFormatException e) {
            return false;
        }
        if (timeArray.length == 1) {
            return mDurationRecord > 0 && mDurationRecord <= 3600;
        } else if (timeArray.length == 2) {
            return false;
        } else if (timeArray.length == 3){
            if (mDurationRecord > 3600 || mDurationRecord < 0) {
                return false;
            }
            String delay = timeArray[1];

            try {
                mDelay = Integer.parseInt(delay);
            } catch (NumberFormatException e) {
                return false;
            } if (mDelay < 0 || mDelay > 3600) {
                return false;
            }
            String totalDuration = timeArray[2];
            try {
                mTotalScheduledRecordingDuration = Integer.parseInt(totalDuration);
            } catch (NumberFormatException e) {
                return false;
            }
            if (mTotalScheduledRecordingDuration < 0 || mTotalScheduledRecordingDuration > 3600) {
                return false;
            }
        } return true;
    }
}