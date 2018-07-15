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

    public static void show(final Context CONTEXT) {
        Log.d("BackgroundServiceSti...", "show()");
        final Notification NOTIFICATION = IliasBuddyNotificationHelper.createStickyNotification(
                CONTEXT, BackgroundServiceStickyNotification.CHANNEL_ID,
                CONTEXT.getString(R.string.notification_channel_sticky_name),
                CONTEXT.getString(R.string.notification_channel_sticky_description),
                CONTEXT.getString(R.string.app_name),
                CONTEXT.getString(R.string.notification_background_service_is_active),
                new Intent(CONTEXT, MainActivity.class));
        IliasBuddyNotificationHelper.showNotification(CONTEXT,
                BackgroundServiceStickyNotification.NOTIFICATION_ID, NOTIFICATION);
    }

    public static void hide(final Context CONTEXT) {
        Log.d("BackgroundServiceSti...", "hide()");
        IliasBuddyNotificationHelper.cancelNotification(CONTEXT,
                BackgroundServiceStickyNotification.NOTIFICATION_ID);
    }
}
