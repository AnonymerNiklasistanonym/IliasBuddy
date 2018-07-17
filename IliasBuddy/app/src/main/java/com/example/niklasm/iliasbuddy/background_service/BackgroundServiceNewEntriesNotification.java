package com.example.niklasm.iliasbuddy.background_service;

import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.niklasm.iliasbuddy.R;
import com.example.niklasm.iliasbuddy.notifications.IliasBuddyNotificationHelper;

public class BackgroundServiceNewEntriesNotification {

    final private static int NOTIFICATION_ID = 324234234;
    final private static String CHANNEL_ID = "BackgroundServiceNewEntriesNotification";

    public static void show(final Context CONTEXT, final String CONTENT_TITLE,
                            final String CONTENT_TEXT, final String[] CONTENT_TEXT_ARRAY,
                            final Intent ON_CLICK, final int MESSAGE_COUNT, final String URL,
                            final Intent ON_DISMISS_CLICK) {
        Log.d("BackgroundServiceNew...", "show()");
        final Notification NOTIFICATION = IliasBuddyNotificationHelper.createNewEntryNotification(
                CONTEXT, BackgroundServiceNewEntriesNotification.CHANNEL_ID,
                CONTEXT.getString(R.string.notification_channel_new_entries_name),
                CONTEXT.getString(R.string.notification_channel_new_entries_description),
                CONTENT_TITLE, CONTENT_TEXT, CONTENT_TEXT_ARRAY, ON_CLICK, MESSAGE_COUNT, URL,
                ON_DISMISS_CLICK);
        IliasBuddyNotificationHelper.showNotification(CONTEXT,
                BackgroundServiceNewEntriesNotification.NOTIFICATION_ID, NOTIFICATION);
    }

    public static void hide(final Context CONTEXT) {
        Log.d("BackgroundServiceNew...", "hide()");
        IliasBuddyNotificationHelper.cancelNotification(CONTEXT,
                BackgroundServiceNewEntriesNotification.NOTIFICATION_ID);
    }
}
