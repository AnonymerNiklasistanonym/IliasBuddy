package com.niklasm.iliasbuddy.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.niklasm.iliasbuddy.background_service.BackgroundServiceManager;
import com.niklasm.iliasbuddy.handler.IliasBuddyPreferenceHandler;

public class BootCompletedReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(final Context CONTEXT, final Intent INTENT) {
        Log.d("BootCompletedReceiver", "onReceive");

        // check if intent is the system ACTION_BOOT_COMPLETED intent
        if (Intent.ACTION_BOOT_COMPLETED.equals(INTENT.getAction())) {
            // check if settings has background service activated/and on boot start
            if (IliasBuddyPreferenceHandler
                    .getBackgroundNotificationsEnabled(CONTEXT, true) &&
                    IliasBuddyPreferenceHandler
                            .getBackgroundServiceStartOnBoot(CONTEXT, true)) {
                BackgroundServiceManager.startBackgroundService(CONTEXT);
            }
        }
    }
}
