package com.example.niklasm.iliasbuddy.preferences_handler;

import android.support.annotation.NonNull;

public class IliasBuddyCredentials {

    @NonNull
    private final String USER_NAME;
    @NonNull
    private final String USER_PASSWORD;
    @NonNull
    private final String USER_URL;

    /**
     * @param USER_NAME     User name of Ilias RSS private feed
     * @param USER_PASSWORD User password of Ilias RSS private feed
     * @param USER_URL      User RSS URL of Ilias RSS private feed
     */
    IliasBuddyCredentials(@NonNull final String USER_NAME, @NonNull final String USER_PASSWORD,
                          @NonNull final String USER_URL) {
        this.USER_NAME = USER_NAME;
        this.USER_PASSWORD = USER_PASSWORD;
        this.USER_URL = USER_URL;
    }

    @NonNull
    public String getUserName() {
        return USER_NAME;
    }

    @NonNull
    public String getUserPassword() {
        return USER_PASSWORD;
    }

    @NonNull
    public String getUserUrl() {
        return USER_URL;
    }
}
