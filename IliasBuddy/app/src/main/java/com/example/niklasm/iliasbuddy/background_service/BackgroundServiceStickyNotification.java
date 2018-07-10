package com.example.niklasm.iliasbuddy.background_service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.example.niklasm.iliasbuddy.MainActivity;
import com.example.niklasm.iliasbuddy.R;

import java.util.Objects;

public class BackgroundServiceStickyNotification {

    final private static int NOTIFICATION_ID = 192168;
    final private static String CHANNEL_ID = "BackgroundServiceStickyNotification";
    final private static CharSequence CHANNEL_NAME = "Sticky background service is active";
    final private static String CHANNEL_Description = "Shows sticky notification when background service is activated";

    public static void show(final Context CONTEXT) {
        Log.d("BackgroundServiceSti...", "show()");

        // create PendingIntent for opening the app on click
        final PendingIntent openAppPendingIntent =
                PendingIntent.getActivity(CONTEXT, 0,
                        new Intent(CONTEXT, MainActivity.class),
                        PendingIntent.FLAG_UPDATE_CURRENT);
        // create PendingIntent for stopping the background service
        final PendingIntent stopServicePendingIntent =
                PendingIntent.getActivity(CONTEXT, 0,
                        new Intent(CONTEXT, MainActivity.class)
                                .putExtra(MainActivity.STOP_BACKGROUND_SERVICE, true),
                        PendingIntent.FLAG_CANCEL_CURRENT);


        final NotificationManager notificationManager =
                (NotificationManager) CONTEXT.getSystemService(Context.NOTIFICATION_SERVICE);

        // setup oreo notification channel - https://stackoverflow.com/a/47974065
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            final NotificationChannel mChannel =
                    new NotificationChannel(BackgroundServiceStickyNotification.CHANNEL_ID,
                            BackgroundServiceStickyNotification.CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH);
            mChannel.setDescription(BackgroundServiceStickyNotification.CHANNEL_Description);
            mChannel.enableLights(false);
            mChannel.enableVibration(false);
            mChannel.setShowBadge(false);
            mChannel.setLockscreenVisibility(Notification.VISIBILITY_SECRET);
            Objects.requireNonNull(notificationManager).createNotificationChannel(mChannel);
        }

        // build sticky notification
        final Notification stickyNotification = new NotificationCompat.Builder(CONTEXT, BackgroundServiceStickyNotification.CHANNEL_ID)
                .setContentTitle("IliasBuddy - Running in the background")
                .setContentText("Click to open the app or expand to stop the background service")
                .setContentIntent(openAppPendingIntent)
                .addAction(new NotificationCompat.Action.Builder(R.drawable.ic_close, "Stop background service", stopServicePendingIntent).build())
                .setSmallIcon(R.drawable.ic_ilias_logo_notification)
                .setPriority(Notification.PRIORITY_MIN)
                .setColor(ContextCompat.getColor(CONTEXT, R.color.colorPrimary))
                .setAutoCancel(false) // on click the notification does not disappear
                .setOngoing(true) // make it not clear-able
                .build();

        // show the notification
        NotificationManagerCompat.from(CONTEXT).notify(BackgroundServiceStickyNotification.NOTIFICATION_ID, stickyNotification);
    }

    public static void hide(final Context CONTEXT) {
        Log.d("BackgroundServiceSti...", "hide()");
        NotificationManagerCompat.from(CONTEXT).cancel(BackgroundServiceStickyNotification.NOTIFICATION_ID);
    }
}
