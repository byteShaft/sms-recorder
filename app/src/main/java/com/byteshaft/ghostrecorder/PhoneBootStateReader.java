package com.byteshaft.ghostrecorder;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.widget.Toast;

public class PhoneBootStateReader extends BroadcastReceiver {

    ComponentName mComponentName;
    PackageManager mPackageManager;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
            System.out.println("schedule task resumed...");
        }

        mComponentName = new ComponentName(context, PhoneBootStateReader.class);
        mPackageManager = context.getPackageManager();

        mPackageManager.setComponentEnabledSetting(mComponentName, PackageManager
                .COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
    }
}
