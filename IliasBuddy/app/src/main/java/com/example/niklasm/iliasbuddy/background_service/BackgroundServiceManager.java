package com.example.niklasm.iliasbuddy.background_service;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.util.Log;

import java.util.Objects;

public class BackgroundServiceManager {

    public final static String STOP_BACKGROUND_SERVICE = "stopBackgroundService";
    public final static String START_BACKGROUND_SERVICE = "startBackgroundService";
    public static final String INTENT_EXTRA_NAME = "action";
    private final Context CONTEXT;
    private AlarmManager am;
    private PendingIntent pendingIntent;
    private BackgroundServiceStickyNotification backgroundServiceStickyNotification;

    public BackgroundServiceManager(final Context CONTEXT) {
        this.CONTEXT = CONTEXT;
    }

/*    @Override
    protected void onNewIntent(final Intent intent) {
        Log.d("BackgroundServiceMan...", "onNewIntent()");
        super.onNewIntent(intent);
        Log.d("BackgroundServiceMan...", "intent extra = \"" + intent.getStringExtra(BackgroundServiceManager.INTENT_EXTRA_NAME) + "\"");
        if (intent.getStringExtra(BackgroundServiceManager.INTENT_EXTRA_NAME)
                .equals(BackgroundServiceManager.START_BACKGROUND_SERVICE)) {
            startBackgroundService();
        } else if (intent.getStringExtra(BackgroundServiceManager.INTENT_EXTRA_NAME)
                .equals(BackgroundServiceManager.STOP_BACKGROUND_SERVICE)) {
            stopBackgroundService();
        }
    }*/


    public void startBackgroundService() {
        Log.d("BackgroundServiceMan...", "startBackgroundService()");

        // Create a new Intent that calls the BackgroundIntentService class
        final Intent intent = new Intent(CONTEXT, BackgroundIntentService.class);
        // and add it to a pending Intent
        pendingIntent = PendingIntent.getService(CONTEXT, 12345, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        // and add this Intent to the alarm manager
        am = (AlarmManager) CONTEXT.getSystemService(Activity.ALARM_SERVICE);
        // call the pending intent every ... minutes
        final int minutes = 5;
        am.setRepeating(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime(), 1000 * 60 * minutes, pendingIntent);

        // also make a sticky notification so that the user knows the background service is running
        backgroundServiceStickyNotification = new BackgroundServiceStickyNotification(CONTEXT);
        backgroundServiceStickyNotification.show();
    }

    public void stopBackgroundService() {
        Log.d("BackgroundServiceMan...", "stopBackgroundService()");

        // cancel the alarm
        Objects.requireNonNull(am).cancel(pendingIntent);

        // also remove the sticky notification so that the user knows the background service is not running
        if (backgroundServiceStickyNotification != null) {
            backgroundServiceStickyNotification.hide();
            backgroundServiceStickyNotification = null;
        }
    }
}
