package com.niklasm.iliasbuddy.private_rss_feed_api;

import android.support.annotation.NonNull;

import com.android.volley.AuthFailureError;
import com.android.volley.VolleyError;
import com.niklasm.iliasbuddy.private_rss_feed_api.feed_entry.IliasRssEntry;

/**
 * API client
 */
public interface IPrivateIliasFeedApiClient {
    /**
     * Gets called when the Ilias RSS feed response is ready
     *
     * @param iliasRssEntries The fetched private Ilias RSS feed entries
     */
    void onFeedResponse(@NonNull final IliasRssEntry[] iliasRssEntries);

    /**
     * Gets called when there was an authentication error to the private Ilias RSS feed
     *
     * @param authenticationError Error exception
     */
    void onAuthenticationError(@NonNull final AuthFailureError authenticationError);

    /**
     * Gets called when there was a web response error
     *
     * @param responseError Error exception
     */
    void onResponseError(@NonNull final VolleyError responseError);

    /**
     * Gets called when there was an feed parse error
     *
     * @param feedParseError Error exception
     */
    void onFeedParseError(@NonNull final Exception feedParseError);
}
