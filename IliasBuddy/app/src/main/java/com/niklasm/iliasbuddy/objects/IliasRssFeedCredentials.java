package com.niklasm.iliasbuddy.objects;

import androidx.annotation.NonNull;

public class IliasRssFeedCredentials {

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
    public IliasRssFeedCredentials(@NonNull final String USER_NAME,
                                   @NonNull final String USER_PASSWORD,
                                   @NonNull final String USER_URL) {
        this.USER_NAME = USER_NAME;
        this.USER_PASSWORD = USER_PASSWORD;
        this.USER_URL = USER_URL;
    }

    /**
     * @return User name of private RSS Ilias feed
     */
    @NonNull
    public String getUserName() {
        return USER_NAME;
    }

    /**
     * @return User password of private RSS Ilias feed
     */
    @NonNull
    public String getUserPassword() {
        return USER_PASSWORD;
    }

    /**
     * @return User URL of private RSS Ilias feed
     */
    @NonNull
    public String getUserUrl() {
        return USER_URL;
    }
}
