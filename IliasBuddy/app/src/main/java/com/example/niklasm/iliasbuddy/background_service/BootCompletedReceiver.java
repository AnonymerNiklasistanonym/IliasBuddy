package com.example.niklasm.iliasbuddy.background_service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class BootCompletedReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(final Context context, final Intent intent) {
        Log.i("BootCompletedReceiver", "onReceive - begin");
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            BackgroundServiceManager.startBackgroundService(context);
        }
        Log.i("BootCompletedReceiver", "onReceive - end");
    }
}
