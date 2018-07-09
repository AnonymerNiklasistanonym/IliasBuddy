package com.example.niklasm.iliasbuddy;

import android.content.Context;
import android.content.SharedPreferences;

public class PreferenceManager {

    // Shared preferences file name
    private static final String PREF_NAME = "intro_slider-welcome";
    private static final String IS_FIRST_TIME_LAUNCH = "IsFirstTimeLaunch";
    private final SharedPreferences pref;

    PreferenceManager(final Context context) {
        final int PRIVATE_MODE = 0;
        pref = context.getSharedPreferences(PreferenceManager.PREF_NAME, PRIVATE_MODE);
    }

    public boolean isFirstTimeLaunch() {
        return pref.getBoolean(PreferenceManager.IS_FIRST_TIME_LAUNCH, true);
    }

    public void setFirstTimeLaunch(final boolean isFirstTime) {
        pref.edit().putBoolean(PreferenceManager.IS_FIRST_TIME_LAUNCH, isFirstTime).apply();
    }
}
