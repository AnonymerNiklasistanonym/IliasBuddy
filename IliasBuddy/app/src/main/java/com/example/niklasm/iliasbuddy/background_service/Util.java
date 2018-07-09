package com.example.niklasm.iliasbuddy.background_service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.example.niklasm.iliasbuddy.MainActivity;
import com.example.niklasm.iliasbuddy.R;

public class Util {

    final private static int FIXED_NOTIFICATION_ID = 1234;
    final private static String STOP_BACKGROUND_SERVICE = "STOP_BACKGROUND_SERVICE";

    public static void scheduleJob(final Context context) {
        Log.i("Util", "scheduleJob - begin");
        final ComponentName serviceComponent = new ComponentName(context, TestJobService.class);
        final JobInfo.Builder builder = new JobInfo.Builder(0, serviceComponent);
        builder.setPeriodic(5 * 60 * 1000); // wait 5min
        final JobScheduler jobScheduler = context.getSystemService(JobScheduler.class);
        assert jobScheduler != null;
        jobScheduler.schedule(builder.build());
        Log.i("Util", "scheduleJob - end");

        Util.makeStickyNotification(context);
    }

    private static void makeStickyNotification(final Context CONTEXT) {
        Log.d("Util", "makeStickyNotification()");

        // create PendingIntent for opening the app on click
        final Intent openAppIntent = new Intent(CONTEXT, MainActivity.class);
        final PendingIntent openAppPendingIntent = PendingIntent.getActivity(CONTEXT,
                0, openAppIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        // create PendingIntent for stopping the background service
        final Intent stopServiceIntent = new Intent(CONTEXT, Util.class)
                .setAction(Util.STOP_BACKGROUND_SERVICE);
        final PendingIntent stopServicePendingIntent = PendingIntent.getService(CONTEXT, 0,
                stopServiceIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        // setup oreo notification channel
        final NotificationManager notificationManager = (NotificationManager) CONTEXT.getSystemService(Context.NOTIFICATION_SERVICE);
        final String CHANNEL_ID = "GG WP";
        // https://stackoverflow.com/a/47974065
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            Log.i("Util", "OREO DETECTED");
            final CharSequence name = "main_activity_channel";
            final String Description = "Oreo notification channel";
            final int importance = NotificationManager.IMPORTANCE_HIGH;
            final NotificationChannel mChannel = new NotificationChannel(CHANNEL_ID, name, importance);
            mChannel.setDescription(Description);
            mChannel.enableLights(true);
            mChannel.setLightColor(Color.RED);
            mChannel.enableVibration(true);
            mChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
            mChannel.setShowBadge(false);
            assert notificationManager != null;
            notificationManager.createNotificationChannel(mChannel);
        }
        // build sticky notification
        final NotificationCompat.Action action = new NotificationCompat.Action.Builder(R.drawable.ic_close, "Stop background service", stopServicePendingIntent).build();

        final Notification stickyNotification = new NotificationCompat.Builder(CONTEXT, CHANNEL_ID)
                .setContentTitle("IliasBuddy - Running in the background")
                .setContentText("Click to open the app or expand to stop the background service")
                //.setContentIntent(openAppPendingIntent)
                //.addAction(action)
                .setSmallIcon(R.drawable.ic_ilias_logo_notification)
                .setPriority(Notification.PRIORITY_MIN)
                .setColor(ContextCompat.getColor(CONTEXT, R.color.colorPrimary))
                .setAutoCancel(false) // on click the notification does not disappear
                .setOngoing(true) // make it not clear-able
                .build();
        // show the notification
        NotificationManagerCompat.from(CONTEXT).notify(Util.FIXED_NOTIFICATION_ID, stickyNotification);
    }


}
