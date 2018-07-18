package com.example.niklasm.iliasbuddy.notification_handler;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.example.niklasm.iliasbuddy.MainActivity;
import com.example.niklasm.iliasbuddy.R;
import com.example.niklasm.iliasbuddy.background_service.BackgroundIntentService;
import com.example.niklasm.iliasbuddy.preferences_handler.IliasBuddyPreferenceHandler;

import java.util.Objects;

public class IliasBuddyNotification {

    @NonNull
    private static NotificationCompat.Builder create(@NonNull final Context CONTEXT,
                                                     @NonNull final String CHANNEL_ID,
                                                     @NonNull final CharSequence CHANNEL_NAME,
                                                     @NonNull final String CHANNEL_DESCRIPTION,
                                                     @NonNull final String CONTENT_TITLE,
                                                     @NonNull final String CONTENT_PREVIEW,
                                                     @NonNull final Intent ONCLICK_ACTIVITY_INTENT,
                                                     final boolean HIGH_PRIORITY,
                                                     final boolean STICKY) {

        /*
        create notification channel if Android version is O or bigger
        when CONTENT_BIG == null this means this is the sticky notification
         */
        IliasBuddyNotification.createNotificationChannel(CONTEXT, CHANNEL_ID, CHANNEL_NAME,
                CHANNEL_DESCRIPTION, HIGH_PRIORITY, HIGH_PRIORITY, HIGH_PRIORITY &&
                        IliasBuddyPreferenceHandler.getNotificationVibrate(CONTEXT, true),
                HIGH_PRIORITY);

        return new NotificationCompat.Builder(CONTEXT, CHANNEL_ID)
                .setContentTitle(CONTENT_TITLE)
                .setContentText(CONTENT_PREVIEW)
                .setContentIntent(PendingIntent.getActivity(CONTEXT, 0,
                        ONCLICK_ACTIVITY_INTENT, PendingIntent.FLAG_UPDATE_CURRENT))
                .setColor(ContextCompat.getColor(CONTEXT, R.color.colorPrimary))
                .setSmallIcon(R.drawable.ic_ilias_logo_white_24dp)
                .setPriority(HIGH_PRIORITY ? Notification.PRIORITY_MAX : Notification.PRIORITY_MIN)
                .setOngoing(STICKY)
                .setAutoCancel(STICKY);
    }

    @NonNull
    private static Notification createSticky(@NonNull final Context CONTEXT) {

        return IliasBuddyNotification.create(CONTEXT,
                IliasBuddyNotificationStickyInterface.CHANNEL_ID,
                CONTEXT.getString(R.string.notification_channel_sticky_name),
                CONTEXT.getString(R.string.notification_channel_sticky_description),
                CONTEXT.getString(R.string.app_name),
                CONTEXT.getString(R.string.notification_background_service_is_active),
                new Intent(CONTEXT, MainActivity.class), false, true)
                // do not display timestamp
                .setShowWhen(false)
                .build();
    }

    @NonNull
    private static Notification createNewEntries(@NonNull final Context CONTEXT,
                                                 @NonNull final String CHANNEL_ID,
                                                 @NonNull final CharSequence CHANNEL_NAME,
                                                 @NonNull final String CHANNEL_DESCRIPTION,
                                                 @NonNull final String CONTENT_TITLE,
                                                 @NonNull final String CONTENT_PREVIEW,
                                                 @NonNull final String[] CONTENT_BIG,
                                                 @NonNull final Intent ONCLICK_ACTIVITY_INTENT,
                                                 final String URL) {

        // determine single/multiple new entries notification style
        final NotificationCompat.Style NOTIFICATION_STYLE;
        if (CONTENT_BIG.length == 1) {
            NOTIFICATION_STYLE = new NotificationCompat.BigTextStyle()
                    .bigText(CONTENT_BIG[0])
                    .setBigContentTitle(CONTENT_PREVIEW);
        } else {
            final NotificationCompat.InboxStyle NOTIFICATION_MULTIPLE_STYLE =
                    new NotificationCompat.InboxStyle();
            for (final String LINE : CONTENT_BIG) {
                NOTIFICATION_MULTIPLE_STYLE.addLine(LINE);
            }
            NOTIFICATION_STYLE = NOTIFICATION_MULTIPLE_STYLE;
        }

        final NotificationCompat.Builder NOTIFICATION_BUILDER = IliasBuddyNotification.create(
                CONTEXT, CHANNEL_ID, CHANNEL_NAME, CHANNEL_DESCRIPTION, CONTENT_TITLE,
                CONTENT_PREVIEW, ONCLICK_ACTIVITY_INTENT, true, false)
                // add notification big content
                .setStyle(NOTIFICATION_STYLE)
                // set color for notification icon bg
                .setColor(ContextCompat.getColor(CONTEXT, R.color.colorPrimary))
                // show lights
                .setLights(ContextCompat.getColor(CONTEXT, R.color.colorPrimary),
                        3000, 3000)
                // set ringtone from preferences
                .setSound(Uri.parse(IliasBuddyPreferenceHandler.getNotificationRingtone(CONTEXT,
                        "content://settings/system/notification_sound")))
                // number in badge/notification
                .setNumber(CONTENT_BIG.length)
                // dismiss action button
                .addAction(IliasBuddyNotification.actionDismiss(CONTEXT));

        // Set vibration if preferences say so
        if (IliasBuddyPreferenceHandler.getNotificationVibrate(CONTEXT, true)) {
            NOTIFICATION_BUILDER.setDefaults(Notification.DEFAULT_VIBRATE);
        }

        // Add URL action if URL is not null
        if (URL != null) {
            NOTIFICATION_BUILDER.addAction(IliasBuddyNotification.actionOpenUrl(CONTEXT, URL));
        }

        return NOTIFICATION_BUILDER.build();
    }

    /**
     * Create notification channel if Android version is O or bigger
     *
     * @param CONTEXT             Needed to access the notification bar
     * @param CHANNEL_ID          ID of channel (for the system)
     * @param CHANNEL_NAME        Name of channel (displayed)
     * @param CHANNEL_DESCRIPTION Description of channel (should be displayed)
     * @param ENABLE_LIGHTS       Enable lights in notification channel
     * @param ENABLE_VIBRATION    Enable vibration in notification channel
     * @param ENABLE_BADGE        Enable badge in notification channel
     */
    private static void createNotificationChannel(@NonNull final Context CONTEXT,
                                                  @NonNull final String CHANNEL_ID,
                                                  @NonNull final CharSequence CHANNEL_NAME,
                                                  @NonNull final String CHANNEL_DESCRIPTION,
                                                  final boolean CHANNEL_IMPORTANCE_HIGH,
                                                  final boolean ENABLE_LIGHTS,
                                                  final boolean ENABLE_VIBRATION,
                                                  final boolean ENABLE_BADGE) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            final NotificationChannel notificationChannel =
                    new NotificationChannel(CHANNEL_ID, CHANNEL_NAME,
                            CHANNEL_IMPORTANCE_HIGH ? NotificationManager.IMPORTANCE_HIGH :
                                    NotificationManager.IMPORTANCE_MIN);
            notificationChannel.setDescription(CHANNEL_DESCRIPTION);
            notificationChannel.enableLights(ENABLE_LIGHTS);
            notificationChannel.enableVibration(ENABLE_VIBRATION);
            notificationChannel.setShowBadge(ENABLE_BADGE);
            Objects.requireNonNull(
                    (NotificationManager) CONTEXT.getSystemService(Context.NOTIFICATION_SERVICE))
                    .createNotificationChannel(notificationChannel);
        }
    }

    @NonNull
    private static NotificationCompat.Action actionOpenUrl(@NonNull final Context CONTEXT,
                                                           @NonNull final String URL) {
        return new NotificationCompat.Action.Builder(R.drawable.ic_open_in_browser_black,
                CONTEXT.getString(R.string.open_in_ilias),
                PendingIntent.getActivity(CONTEXT, 0,
                        new Intent(Intent.ACTION_VIEW, Uri.parse(URL)),
                        PendingIntent.FLAG_UPDATE_CURRENT)).build();
    }

    @NonNull
    private static NotificationCompat.Action actionDismiss(@NonNull final Context CONTEXT) {
        return new NotificationCompat.Action.Builder(R.drawable.ic_delete_sweep_black,
                CONTEXT.getString(R.string.notification_dismiss_notification),
                PendingIntent.getService(CONTEXT, 0,
                        new Intent(CONTEXT, BackgroundIntentService.class)
                                .putExtra(IliasBuddyNotificationInterface.NOTIFICATION_DISMISSED,
                                        true),
                        PendingIntent.FLAG_UPDATE_CURRENT)).build();
    }

    /**
     * Show new entries notification
     *
     * @param CONTEXT                  Needed to access the notification bar
     * @param CONTENT_TITLE            Title of the notification
     * @param CONTENT_PREVIEW          Content preview of notification
     * @param CONTENT_BIG              Array of content
     * @param ON_CLICK_ACTIVITY_INTENT Intent on click of notification
     * @param URL                      URL to Ilias entry
     */
    public static void showNotificationNewEntries(@NonNull final Context CONTEXT,
                                                  @NonNull final String CONTENT_TITLE,
                                                  @NonNull final String CONTENT_PREVIEW,
                                                  @NonNull final String[] CONTENT_BIG,
                                                  @NonNull final Intent ON_CLICK_ACTIVITY_INTENT,
                                                  final String URL) {

        Log.d("IliasBuddyNotification", "showNotificationNewEntries()");

        IliasBuddyNotification.showNotification(CONTEXT,
                IliasBuddyNotification.createNewEntries(CONTEXT,
                        IliasBuddyNotificationNewEntriesInterface.CHANNEL_ID,
                        CONTEXT.getString(R.string.notification_channel_new_entries_name),
                        CONTEXT.getString(R.string.notification_channel_new_entries_description),
                        CONTENT_TITLE, CONTENT_PREVIEW, CONTENT_BIG, ON_CLICK_ACTIVITY_INTENT,
                        URL),
                IliasBuddyNotificationNewEntriesInterface.NOTIFICATION_ID);
    }

    public static void showStickyNotification(@NonNull final Context CONTEXT) {

        Log.d("IliasBuddyNotification", "showStickyNotification()");

        IliasBuddyNotification.showNotification(CONTEXT,
                IliasBuddyNotification.createSticky(CONTEXT),
                IliasBuddyNotificationStickyInterface.NOTIFICATION_ID);
    }

    /**
     * Hide new entries notification
     *
     * @param CONTEXT Needed to access the notification bar
     */
    public static void hideNotificationNewEntries(@NonNull final Context CONTEXT) {
        IliasBuddyNotification.cancelNotification(CONTEXT,
                IliasBuddyNotificationNewEntriesInterface.NOTIFICATION_ID);
    }

    /**
     * Hide sticky notification
     *
     * @param CONTEXT Needed to access the notification bar
     */
    public static void hideStickyNotification(@NonNull final Context CONTEXT) {
        IliasBuddyNotification.cancelNotification(CONTEXT,
                IliasBuddyNotificationStickyInterface.NOTIFICATION_ID);
    }

    /**
     * Create and show a notification
     *
     * @param CONTEXT         Needed to access the notification bar
     * @param NOTIFICATION    The notification to display
     * @param NOTIFICATION_ID The ID of the notification to display
     */
    private static void showNotification(@NonNull final Context CONTEXT,
                                         @NonNull final Notification NOTIFICATION,
                                         final int NOTIFICATION_ID) {
        NotificationManagerCompat.from(CONTEXT).notify(NOTIFICATION_ID, NOTIFICATION);
    }

    /**
     * Remove/Dismiss a currently shown notification
     *
     * @param CONTEXT         Needed to access the notification bar
     * @param NOTIFICATION_ID The ID of the notification that is currently displayed
     */
    private static void cancelNotification(@NonNull final Context CONTEXT,
                                           final int NOTIFICATION_ID) {
        NotificationManagerCompat.from(CONTEXT).cancel(NOTIFICATION_ID);
    }
}
