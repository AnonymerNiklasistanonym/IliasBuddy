package com.example.niklasm.iliasbuddy.preferences_handler;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;

import com.example.niklasm.iliasbuddy.objects.IliasRssFeedCredentials;

public class IliasBuddyPreferenceHandler {

    private final static String PRIVATE_FEED_URL = "ilias_url";
    private final static String PRIVATE_FEED_USER_NAME = "ilias_user_name";
    private final static String PRIVATE_FEED_USER_PASSWORD = "ilias_password";
    private final static String PREFERENCE_NAME_PRIVATE_FEED = "myPrefs";
    private final static String PREFERENCE_NOTIFICATION_VIBRATE = "notifications_new_message_vibrate";
    private final static String PREFERENCE_NOTIFICATION_RINGTONE = "notifications_new_message_ringtone";

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

    public static boolean getNotificationVibrate(final Context CONTEXT, final boolean DEFAULT) {
        return IliasBuddyPreferenceHandler.getPreferenceBoolean(CONTEXT,
                IliasBuddyPreferenceHandler.PREFERENCE_NOTIFICATION_VIBRATE, DEFAULT);
    }

    @NonNull
    public static String getNotificationRingtone(final Context CONTEXT, final String DEFAULT) {
        return IliasBuddyPreferenceHandler.getPreferenceString(CONTEXT,
                IliasBuddyPreferenceHandler.PREFERENCE_NOTIFICATION_RINGTONE, DEFAULT);
    }
}
