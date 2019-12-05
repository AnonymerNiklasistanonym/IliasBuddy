package com.niklasm.iliasbuddy.handler;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import androidx.annotation.NonNull;

import com.niklasm.iliasbuddy.objects.IliasRssFeedCredentials;

public class IliasBuddyPreferenceHandler {

    private static final String PRIVATE_FEED_URL = "ilias_url";
    private static final String PRIVATE_FEED_USER_NAME = "ilias_user_name";
    private static final String PRIVATE_FEED_USER_PASSWORD = "ilias_password";
    private static final String PREFERENCE_NAME_PRIVATE_FEED = "myPrefs";
    private static final String PREFERENCE_NOTIFICATION_VIBRATE = "notifications_new_message_vibrate";
    private static final String PREFERENCE_NOTIFICATION_RINGTONE = "notifications_new_message_ringtone";
    private static final String PREFERENCE_SYNC_FREQUENCY = "sync_frequency";
    private static final String FEED_LAST_NOTIFICATION_TEXT = "FEED_LAST_NOTIFICATION_TEXT";
    private static final String FEED_LATEST_ELEMENT_STRING = "FEED_LATEST_ELEMENT_STRING";
    private static final String FIRST_TIME_LAUNCH = "FIRST_TIME_LAUNCH";
    private static final String SHORTCUT_CAMPUS_ENABLED = "enable_campus_shortcut";
    private static final String SHORTCUT_ACCOUNT_ENABLED = "enable_setup_shortcut";
    private static final String SHORTCUT_DEVELOPER_OPTIONS_ENABLED = "enable_dev_shortcut";
    private static final String PREFERENCE_BACKGROUND_NOTIFICATIONS_ENABLED = "activate_background_notifications";
    private static final String AUTO_UPDATE_NOTIFICATION_ENABLED = "enable_auto_update_check";
    private static final String START_BACKGROUND_SERVICE_ON_BOOT = "start_background_notifications_on_boot";
    private static final String FILTER_SHOW_FILE_CHANGES = "FILTER_SHOW_FILE_CHANGES";
    private static final String FILTER_SHOW_POSTS = "FILTER_SHOW_POSTS";

    private static boolean getPreferenceBoolean(@NonNull final Context CONTEXT,
                                                @NonNull final String PREFERENCE,
                                                final boolean DEFAULT_VALUE) {
        return PreferenceManager.getDefaultSharedPreferences(CONTEXT)
                .getBoolean(PREFERENCE, DEFAULT_VALUE);
    }

    private static String getPreferenceString(@NonNull final Context CONTEXT,
                                              @NonNull final String PREFERENCE,
                                              final String DEFAULT_VALUE) {
        return PreferenceManager.getDefaultSharedPreferences(CONTEXT)
                .getString(PREFERENCE, DEFAULT_VALUE);
    }

    private static void setPreferenceString(@NonNull final Context CONTEXT,
                                            @NonNull final String PREFERENCE,
                                            final String VALUE) {
        PreferenceManager.getDefaultSharedPreferences(CONTEXT)
                .edit().putString(PREFERENCE, VALUE).apply();
    }

    private static void setPreferenceBoolean(@NonNull final Context CONTEXT,
                                             @NonNull final String PREFERENCE,
                                             final boolean VALUE) {
        PreferenceManager.getDefaultSharedPreferences(CONTEXT)
                .edit().putBoolean(PREFERENCE, VALUE).apply();
    }

    public static void saveCredentials(@NonNull final Context CONTEXT,
                                       @NonNull final String USER_NAME,
                                       @NonNull final String USER_PASSWORD,
                                       @NonNull final String USER_URL) {
        CONTEXT.getSharedPreferences(
                IliasBuddyPreferenceHandler.PREFERENCE_NAME_PRIVATE_FEED,
                Context.MODE_PRIVATE).edit()
                .putString(IliasBuddyPreferenceHandler.PRIVATE_FEED_USER_NAME,
                        USER_NAME)
                .putString(IliasBuddyPreferenceHandler.PRIVATE_FEED_USER_PASSWORD,
                        USER_PASSWORD)
                .putString(IliasBuddyPreferenceHandler.PRIVATE_FEED_URL,
                        USER_URL)
                .apply();
    }

    @NonNull
    public static IliasRssFeedCredentials getCredentials(@NonNull final Context CONTEXT) {

        final SharedPreferences PREF = CONTEXT.getSharedPreferences(
                IliasBuddyPreferenceHandler.PREFERENCE_NAME_PRIVATE_FEED,
                Context.MODE_PRIVATE);

        return new IliasRssFeedCredentials(
                PREF.getString(IliasBuddyPreferenceHandler.PRIVATE_FEED_USER_NAME, ""),
                PREF.getString(IliasBuddyPreferenceHandler.PRIVATE_FEED_USER_PASSWORD, ""),
                PREF.getString(IliasBuddyPreferenceHandler.PRIVATE_FEED_URL, ""));
    }

    public static boolean getNotificationVibrate(@NonNull final Context CONTEXT,
                                                 final boolean DEFAULT) {
        return IliasBuddyPreferenceHandler.getPreferenceBoolean(CONTEXT,
                IliasBuddyPreferenceHandler.PREFERENCE_NOTIFICATION_VIBRATE, DEFAULT);
    }

    @NonNull
    public static String getNotificationRingtone(@NonNull final Context CONTEXT,
                                                 final String DEFAULT) {
        return IliasBuddyPreferenceHandler.getPreferenceString(CONTEXT,
                IliasBuddyPreferenceHandler.PREFERENCE_NOTIFICATION_RINGTONE, DEFAULT);
    }

    public static void setLastNotificationText(@NonNull final Context CONTEXT,
                                               final String VALUE) {
        IliasBuddyPreferenceHandler.setPreferenceString(CONTEXT,
                IliasBuddyPreferenceHandler.FEED_LAST_NOTIFICATION_TEXT, VALUE);
    }

    public static String getLatestItemToString(@NonNull final Context CONTEXT,
                                               final String DEFAULT) {
        return IliasBuddyPreferenceHandler.getPreferenceString(CONTEXT,
                IliasBuddyPreferenceHandler.FEED_LATEST_ELEMENT_STRING, DEFAULT);
    }

    public static boolean getFirstTimeLaunch(@NonNull final Context CONTEXT,
                                             final boolean DEFAULT) {
        return IliasBuddyPreferenceHandler.getPreferenceBoolean(CONTEXT,
                IliasBuddyPreferenceHandler.FIRST_TIME_LAUNCH, DEFAULT);
    }

    public static void setFirstTimeLaunch(@NonNull final Context CONTEXT,
                                          final boolean VALUE) {
        IliasBuddyPreferenceHandler.setPreferenceBoolean(CONTEXT,
                IliasBuddyPreferenceHandler.FIRST_TIME_LAUNCH, VALUE);
    }

    public static void setLatestItemToString(@NonNull final Context CONTEXT,
                                             @NonNull final String VALUE) {
        IliasBuddyPreferenceHandler.setPreferenceString(CONTEXT,
                IliasBuddyPreferenceHandler.FEED_LATEST_ELEMENT_STRING, VALUE);
    }

    public static String getNotificationFrequency(@NonNull final Context CONTEXT,
                                                  final String VALUE) {
        return IliasBuddyPreferenceHandler.getPreferenceString(CONTEXT,
                IliasBuddyPreferenceHandler.PREFERENCE_SYNC_FREQUENCY, VALUE);
    }

    public static boolean getEnableCampusShortcut(@NonNull final Context CONTEXT,
                                                  final boolean DEFAULT) {
        return IliasBuddyPreferenceHandler.getPreferenceBoolean(CONTEXT,
                IliasBuddyPreferenceHandler.SHORTCUT_CAMPUS_ENABLED, DEFAULT);
    }

    public static boolean getEnableAccountShortcut(@NonNull final Context CONTEXT,
                                                   final boolean DEFAULT) {
        return IliasBuddyPreferenceHandler.getPreferenceBoolean(CONTEXT,
                IliasBuddyPreferenceHandler.SHORTCUT_ACCOUNT_ENABLED, DEFAULT);
    }

    public static boolean getEnableDeveloperOptionsShortcut(@NonNull final Context CONTEXT,
                                                            final boolean DEFAULT) {
        return IliasBuddyPreferenceHandler.getPreferenceBoolean(CONTEXT,
                IliasBuddyPreferenceHandler.SHORTCUT_DEVELOPER_OPTIONS_ENABLED, DEFAULT);
    }

    public static boolean getBackgroundNotificationsEnabled(@NonNull final Context CONTEXT,
                                                            final boolean DEFAULT) {
        return IliasBuddyPreferenceHandler.getPreferenceBoolean(CONTEXT,
                IliasBuddyPreferenceHandler.PREFERENCE_BACKGROUND_NOTIFICATIONS_ENABLED, DEFAULT);
    }

    public static boolean getEnableAutoCheckForUpdates(@NonNull final Context CONTEXT,
                                                       final boolean DEFAULT) {
        return IliasBuddyPreferenceHandler.getPreferenceBoolean(CONTEXT,
                IliasBuddyPreferenceHandler.AUTO_UPDATE_NOTIFICATION_ENABLED, DEFAULT);
    }

    public static boolean getBackgroundServiceStartOnBoot(@NonNull final Context CONTEXT,
                                                          final boolean DEFAULT) {
        return IliasBuddyPreferenceHandler.getPreferenceBoolean(CONTEXT,
                IliasBuddyPreferenceHandler.START_BACKGROUND_SERVICE_ON_BOOT, DEFAULT);
    }

    public static boolean getFilterFileChanges(@NonNull final Context CONTEXT,
                                               final boolean DEFAULT) {
        return IliasBuddyPreferenceHandler.getPreferenceBoolean(CONTEXT,
                IliasBuddyPreferenceHandler.FILTER_SHOW_FILE_CHANGES, DEFAULT);
    }

    public static void setFilterFileChanges(@NonNull final Context CONTEXT,
                                            final boolean VALUE) {
        IliasBuddyPreferenceHandler.setPreferenceBoolean(CONTEXT,
                IliasBuddyPreferenceHandler.FILTER_SHOW_FILE_CHANGES, VALUE);
    }

    public static boolean getFilterPosts(@NonNull final Context CONTEXT,
                                         final boolean DEFAULT) {
        return IliasBuddyPreferenceHandler.getPreferenceBoolean(CONTEXT,
                IliasBuddyPreferenceHandler.FILTER_SHOW_POSTS, DEFAULT);
    }

    public static void setFilterPosts(@NonNull final Context CONTEXT,
                                      final boolean VALUE) {
        IliasBuddyPreferenceHandler.setPreferenceBoolean(CONTEXT,
                IliasBuddyPreferenceHandler.FILTER_SHOW_POSTS, VALUE);
    }
}
