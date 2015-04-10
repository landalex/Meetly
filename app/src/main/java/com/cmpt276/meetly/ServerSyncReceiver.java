package com.cmpt276.meetly;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Receives pings at a user-defined interval to sync events with the server
 */
public class ServerSyncReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i("ServerSyncReceiver", "onReceive called");
    }
}
