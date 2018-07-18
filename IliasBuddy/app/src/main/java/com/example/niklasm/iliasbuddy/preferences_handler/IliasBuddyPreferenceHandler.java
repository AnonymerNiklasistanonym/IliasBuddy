package com.example.niklasm.iliasbuddy.preferences_handler;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;

public class IliasBuddyPreferenceHandler {

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
                IliasBuddyPreferenceHandlerInterface.ILIAS_PRIV_FEED_PREFERENCE_NAME,
                Context.MODE_PRIVATE).edit()
                .putString(IliasBuddyPreferenceHandlerInterface.ILIAS_PRIV_FEED_USER_NAME,
                        USER_NAME)
                .putString(IliasBuddyPreferenceHandlerInterface.ILIAS_PRIV_FEED_USER_PASSWORD,
                        USER_PASSWORD)
                .putString(IliasBuddyPreferenceHandlerInterface.ILIAS_PRIV_FEED_URL,
                        USER_URL)
                .apply();
    }

    @NonNull
    public static IliasBuddyCredentials getCredentials(@NonNull final Context CONTEXT) {

        final SharedPreferences PREF = CONTEXT.getSharedPreferences(
                IliasBuddyPreferenceHandlerInterface.ILIAS_PRIV_FEED_PREFERENCE_NAME,
                Context.MODE_PRIVATE);

        return new IliasBuddyCredentials(
                PREF.getString(IliasBuddyPreferenceHandlerInterface.ILIAS_PRIV_FEED_USER_NAME,
                        ""),
                PREF.getString(IliasBuddyPreferenceHandlerInterface.ILIAS_PRIV_FEED_USER_PASSWORD,
                        ""),
                PREF.getString(IliasBuddyPreferenceHandlerInterface.ILIAS_PRIV_FEED_URL,
                        ""));
    }
}
