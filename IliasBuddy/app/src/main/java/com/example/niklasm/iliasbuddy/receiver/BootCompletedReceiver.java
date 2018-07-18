package com.example.niklasm.iliasbuddy.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.example.niklasm.iliasbuddy.background_service.BackgroundServiceManager;

public class BootCompletedReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(final Context CONTEXT, final Intent INTENT) {
        Log.i("BootCompletedReceiver", "onReceive - begin");

        // check if intent is the system ACTION_BOOT_COMPLETED intent
        if (Intent.ACTION_BOOT_COMPLETED.equals(INTENT.getAction())) {
            // check if settings has background service activated/and on boot start
            final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(CONTEXT);
            final boolean BACKGROUND_SERVICE_ENABLED = sharedPreferences.getBoolean("activate_background_notifications", true);
            final boolean BACKGROUND_SERVICE_OB_BOOT_ENABLED = sharedPreferences.getBoolean("start_background_notifications_on_boot", true);
            if (BACKGROUND_SERVICE_ENABLED && BACKGROUND_SERVICE_OB_BOOT_ENABLED) {
                // only then start background service on boot
                BackgroundServiceManager.startBackgroundService(CONTEXT);
            }
        }
        Log.i("BootCompletedReceiver", "onReceive - end");
    }
}
