package com.example.niklasm.iliasbuddy.background_service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class MyStartServiceReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(final Context context, final Intent intent) {
        Log.i("MyStartServiceReceiver", "onReceive - begin");
        if (intent.getAction() != null && Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            Util.scheduleJob(context);
        }
        Log.i("MyStartServiceReceiver", "onReceive - end");
    }
}
