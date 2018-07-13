package com.example.niklasm.iliasbuddy.notifications;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.ContextCompat;

import com.example.niklasm.iliasbuddy.R;

import java.util.Objects;

public class IliasBuddyNotificationHelper {

    public static Notification createNewEntryNotification(@NonNull final Context CONTEXT,
                                                          @NonNull final String CHANNEL_ID,
                                                          @NonNull final CharSequence CHANNEL_NAME,
                                                          final String CHANNEL_DESCRIPTION,
                                                          @NonNull final String CONTENT_TITLE,
                                                          final String CONTENT_TEXT,
                                                          final String CONTENT_TEXT_BIG,
                                                          final String[] CONTENT_TEXT_ARRAY,
                                                          final Intent ONCLICK_INTENT,
                                                          final int MESSAGE_COUNT,
                                                          final String URL) {

        // get settings
        final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(CONTEXT);
        final boolean VIBRATE = sharedPreferences.getBoolean("notifications_new_message_vibrate", true);
        final String RINGTONE = sharedPreferences.getString("notifications_new_message_ringtone", "content://settings/system/notification_sound");

        // Create the TaskStackBuilder and add the intent, which inflates the back stack
        final TaskStackBuilder stackBuilder = TaskStackBuilder.create(CONTEXT);
        stackBuilder.addNextIntentWithParentStack(ONCLICK_INTENT);
        // Get the PendingIntent containing the entire back stack
        final PendingIntent openAppPendingIntent =
                stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        // setup oreo notification channel - https://stackoverflow.com/a/47974065
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            final NotificationManager notificationManager =
                    (NotificationManager) CONTEXT.getSystemService(Context.NOTIFICATION_SERVICE);

            final NotificationChannel mChannel =
                    new NotificationChannel(CHANNEL_ID,
                            CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH);
            mChannel.setDescription(CHANNEL_DESCRIPTION);
            mChannel.enableLights(true);
            mChannel.enableVibration(VIBRATE);
            mChannel.setShowBadge(true);
            Objects.requireNonNull(notificationManager).createNotificationChannel(mChannel);
        }

        final NotificationCompat.Style NOTIFICATION_STYLE;
        if (CONTENT_TEXT_ARRAY.length > 1) {
            final NotificationCompat.InboxStyle NOTIFICATION_STYLE_2 = new NotificationCompat.InboxStyle();
            //.setBigContentTitle("Big content title")
            //.setSummaryText(MESSAGE_COUNT + "entries");

            for (final String LINE : CONTENT_TEXT_ARRAY) {
                NOTIFICATION_STYLE_2.addLine(LINE);
            }
            NOTIFICATION_STYLE = NOTIFICATION_STYLE_2;
        } else {
            NOTIFICATION_STYLE = new NotificationCompat.BigTextStyle()
                    .bigText(CONTENT_TEXT_ARRAY[0])
                    .setBigContentTitle(CONTENT_TEXT);
        }

        // build new entries notification
        final NotificationCompat.Builder NOTIFICATION_BUILDER = new NotificationCompat.Builder(CONTEXT, CHANNEL_ID)
                .setContentTitle(CONTENT_TITLE)
                .setContentText(CONTENT_TEXT)
                .setContentIntent(openAppPendingIntent)
                .setSmallIcon(R.drawable.ic_ilias_logo_white_24dp)
                .setPriority(Notification.PRIORITY_MAX)
                .setColor(ContextCompat.getColor(CONTEXT, R.color.colorPrimary))
                .setLights(ContextCompat.getColor(CONTEXT, R.color.colorPrimary), 3000, 3000)
                .setVibrate((VIBRATE ? new long[]{1000, 1000, 1000, 1000, 1000} : new long[]{0})) // new long[]{0, 250, 250, 250}
                .setStyle(NOTIFICATION_STYLE)
                .setSound(Uri.parse(RINGTONE))
                .setAutoCancel(true) // on click the notification does not disappear
                .setNumber(MESSAGE_COUNT);

        if (VIBRATE) {
            NOTIFICATION_BUILDER.setDefaults(Notification.DEFAULT_VIBRATE);
        }

        // only add additional action if there is an URL
        if (URL != null) {
            NOTIFICATION_BUILDER.addAction(
                    new NotificationCompat.Action.Builder(
                            R.drawable.ic_open_in_browser_black,
                            CONTEXT.getString(R.string.open_in_ilias),
                            PendingIntent.getActivity(CONTEXT,
                                    0, new Intent(Intent.ACTION_VIEW, Uri.parse(URL)),
                                    PendingIntent.FLAG_UPDATE_CURRENT)).build());
        }

        return NOTIFICATION_BUILDER.build();
    }

    public static Notification createStickyNotification(final Context CONTEXT,
                                                        final String CHANNEL_ID,
                                                        final CharSequence CHANNEL_NAME,
                                                        final String CHANNEL_DESCRIPTION,
                                                        final String CONTENT_TITLE,
                                                        final String CONTENT_TEXT,
                                                        final Intent ONCLICK_INTENT) {

        // create PendingIntent for opening the app on click
        final PendingIntent openAppPendingIntent =
                PendingIntent.getActivity(CONTEXT, 0, ONCLICK_INTENT,
                        PendingIntent.FLAG_UPDATE_CURRENT);

        // setup oreo notification channel - https://stackoverflow.com/a/47974065
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            final NotificationManager notificationManager =
                    (NotificationManager) CONTEXT.getSystemService(Context.NOTIFICATION_SERVICE);

            final NotificationChannel mChannel =
                    new NotificationChannel(CHANNEL_ID,
                            CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH);
            mChannel.setDescription(CHANNEL_DESCRIPTION);
            mChannel.enableLights(false);
            mChannel.enableVibration(false);
            mChannel.setShowBadge(false);
            mChannel.setLockscreenVisibility(Notification.VISIBILITY_SECRET);
            Objects.requireNonNull(notificationManager).createNotificationChannel(mChannel);
        }

        // build sticky notification
        return new NotificationCompat.Builder(CONTEXT, CHANNEL_ID)
                .setContentTitle(CONTENT_TITLE)
                .setContentText(CONTENT_TEXT)
                .setContentIntent(openAppPendingIntent)
                .setSmallIcon(R.drawable.ic_ilias_logo_white_24dp)
                .setPriority(Notification.PRIORITY_MIN)
                .setColor(ContextCompat.getColor(CONTEXT, R.color.colorPrimary))
                .setAutoCancel(false) // on click the notification does not disappear
                .setOngoing(true) // make it not clear-able
                .setShowWhen(false) // do not display timestamp
                .build();
    }

    public static void showNotification(final Context CONTEXT, final int NOTIFICATION_ID, final Notification NOTIFICATION) {
        NotificationManagerCompat.from(CONTEXT).notify(NOTIFICATION_ID, NOTIFICATION);
    }

    public static void cancelNotification(final Context CONTEXT, final int NOTIFICATION_ID) {
        NotificationManagerCompat.from(CONTEXT).cancel(NOTIFICATION_ID);
    }
}
