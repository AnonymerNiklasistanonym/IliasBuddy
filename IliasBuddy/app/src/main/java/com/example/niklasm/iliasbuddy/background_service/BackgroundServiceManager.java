package com.example.niklasm.iliasbuddy.background_service;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.SystemClock;
import android.util.Log;

import com.example.niklasm.iliasbuddy.notification_handler.IliasBuddyNotificationHandler;

public class BackgroundServiceManager {

    private static AlarmManager am;
    private static PendingIntent pendingIntent;

    public static void startBackgroundService(final Context CONTEXT) {
        Log.d("BackgroundServiceMan...", "startBackgroundService()");

        // Create a new Intent that calls the BackgroundIntentService class
        final Intent intent = new Intent(CONTEXT, BackgroundIntentService.class);
        // and add it to a pending Intent
        BackgroundServiceManager.pendingIntent = PendingIntent.getService(CONTEXT,
                12345, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        // and add this Intent to the alarm manager
        BackgroundServiceManager.am =
                (AlarmManager) CONTEXT.getSystemService(Activity.ALARM_SERVICE);


        final SharedPreferences prefs =
                android.preference.PreferenceManager.getDefaultSharedPreferences(CONTEXT);
        final String RINGTONE = prefs.getString("notifications_new_message_ringtone", null);
        final boolean VIBRATE = prefs.getBoolean("notifications_new_message_vibrate", true);
        final String FREQUENCY = prefs.getString("sync_frequency", null);

        Log.i("BackgroundServiceMan...",
                "RINGTONE = " + (RINGTONE != null ? RINGTONE : "null => None"));
        Log.i("BackgroundServiceMan...",
                "VIBRATE = " + String.valueOf(VIBRATE));
        Log.i("BackgroundServiceMan...", "FREQUENCY = "
                + (FREQUENCY != null ? Integer.valueOf(FREQUENCY) : "null => 5"));

        // call the pending intent every ... minutes with the default value 5
        final int MINUTES = (FREQUENCY != null ? Integer.valueOf(FREQUENCY) : 5);

        BackgroundServiceManager.am.setRepeating(AlarmManager.ELAPSED_REALTIME,
                SystemClock.elapsedRealtime(), 1000 * 60 * MINUTES,
                BackgroundServiceManager.pendingIntent);

        // also make a sticky notification so that the user knows the background service is running
        IliasBuddyNotificationHandler.showStickyNotification(CONTEXT);
    }

    public static boolean isAlarmManagerCurrentlyActivated() {
        return BackgroundServiceManager.am != null;
    }

    public static void stopBackgroundService(final Context CONTEXT) {
        Log.d("BackgroundServiceMan...", "stopBackgroundService()");

        // cancel the alarm if there is one
        if (BackgroundServiceManager.am != null) {
            BackgroundServiceManager.am.cancel(BackgroundServiceManager.pendingIntent);
            BackgroundServiceManager.am = null;
        }

        // also remove the sticky notification so that the user knows the bg service is not running
        IliasBuddyNotificationHandler.hideStickyNotification(CONTEXT);
    }
}
