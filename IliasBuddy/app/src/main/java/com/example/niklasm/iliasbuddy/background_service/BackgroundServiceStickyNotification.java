package com.example.niklasm.iliasbuddy.background_service;

import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.niklasm.iliasbuddy.MainActivity;
import com.example.niklasm.iliasbuddy.R;
import com.example.niklasm.iliasbuddy.notifications.IliasBuddyNotificationHelper;

public class BackgroundServiceStickyNotification {

    final private static int NOTIFICATION_ID = 192168;
    final private static String CHANNEL_ID = "BackgroundServiceStickyNotification";
    final private static CharSequence CHANNEL_NAME = "Sticky background service is active";
    final private static String CHANNEL_DESCRIPTION = "Shows sticky notification when background service is activated";
    final private static String CONTENT_TITLE = "IliasBuddy - Running in the background";
    final private static String CONTENT_TEXT = "Click to open the app or expand to stop the background service";
    final private static String ACTION_TITLE = "Stop background service";

    public static void show(final Context CONTEXT) {
        Log.d("BackgroundServiceSti...", "show()");
        final Notification NOTIFICATION = IliasBuddyNotificationHelper.createStickyNotification(CONTEXT,
                BackgroundServiceStickyNotification.CHANNEL_ID, BackgroundServiceStickyNotification.CHANNEL_NAME,
                BackgroundServiceStickyNotification.CHANNEL_DESCRIPTION, BackgroundServiceStickyNotification.CONTENT_TITLE,
                BackgroundServiceStickyNotification.CONTENT_TEXT, new Intent(CONTEXT, MainActivity.class),
                BackgroundServiceStickyNotification.ACTION_TITLE, R.drawable.ic_close, new Intent(CONTEXT, BootCompletedReceiver.class)
                        .putExtra(MainActivity.STOP_BACKGROUND_SERVICE, true));
        IliasBuddyNotificationHelper.showNotification(CONTEXT, BackgroundServiceStickyNotification.NOTIFICATION_ID, NOTIFICATION);
    }

    public static void hide(final Context CONTEXT) {
        Log.d("BackgroundServiceSti...", "hide()");
        IliasBuddyNotificationHelper.cancelNotification(CONTEXT, BackgroundServiceStickyNotification.NOTIFICATION_ID);
    }
}
