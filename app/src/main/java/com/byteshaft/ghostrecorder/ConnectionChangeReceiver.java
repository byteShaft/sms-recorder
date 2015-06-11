package com.byteshaft.ghostrecorder;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

public class ConnectionChangeReceiver extends BroadcastReceiver {

    @Override
    public void onReceive( Context context, Intent intent )
    {
        ConnectivityManager connectivityManager = (ConnectivityManager)
                context.getSystemService( Context.CONNECTIVITY_SERVICE );
        NetworkInfo activeNetInfo = connectivityManager.getActiveNetworkInfo();
        NetworkInfo mobNetInfo = connectivityManager.getNetworkInfo (
                ConnectivityManager.TYPE_MOBILE );
        if ( activeNetInfo != null ){
            Log.i("Network", "Active network type " + activeNetInfo);
        }
        if( mobNetInfo != null ){
            Log.i("Network", "Mobile network type"+ mobNetInfo);
        }
    }
}
