package com.byteshaft.ghostrecorder;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

public class ConnectionChangeReceiver extends BroadcastReceiver {

    private String LOG_TAG = AppGlobals.getLogTag(getClass());

    @Override
    public void onReceive(Context context, Intent intent) {
        ConnectivityManager connectivityManager = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetInfo = connectivityManager.getActiveNetworkInfo();
        NetworkInfo mobNetInfo = connectivityManager.getNetworkInfo(
                ConnectivityManager.TYPE_MOBILE);
        if (activeNetInfo != null) {
            AppGlobals.logInformation(LOG_TAG, "Active network type " + activeNetInfo);
        }

        if( mobNetInfo != null ) {
            AppGlobals.logInformation(LOG_TAG, "Mobile network type"+ mobNetInfo);
        }
    }
}
