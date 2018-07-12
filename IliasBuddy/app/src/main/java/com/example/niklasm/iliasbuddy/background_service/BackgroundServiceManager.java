package com.example.niklasm.iliasbuddy.background_service;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.util.Log;

public class BackgroundServiceManager {

    private static AlarmManager am;
    private static PendingIntent pendingIntent;

    public static void startBackgroundService(final Context CONTEXT) {
        Log.d("BackgroundServiceMan...", "startBackgroundService()");

        // Create a new Intent that calls the BackgroundIntentService class
        final Intent intent = new Intent(CONTEXT, BackgroundIntentService.class);
        // and add it to a pending Intent
        BackgroundServiceManager.pendingIntent = PendingIntent.getService(CONTEXT, 12345, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        // and add this Intent to the alarm manager
        BackgroundServiceManager.am = (AlarmManager) CONTEXT.getSystemService(Activity.ALARM_SERVICE);
        // call the pending intent every ... minutes
        final int minutes = 1;
        BackgroundServiceManager.am.setRepeating(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime(), 1000 * 60 * minutes, BackgroundServiceManager.pendingIntent);

        // also make a sticky notification so that the user knows the background service is running
        BackgroundServiceStickyNotification.show(CONTEXT);
    }

    public static void stopBackgroundService(final Context CONTEXT) {
        Log.d("BackgroundServiceMan...", "stopBackgroundService()");

        // cancel the alarm if there is one
        if (BackgroundServiceManager.am != null) {
            BackgroundServiceManager.am.cancel(BackgroundServiceManager.pendingIntent);
        }

        // also remove the sticky notification so that the user knows the background service is not running
        BackgroundServiceStickyNotification.hide(CONTEXT);
    }
}
