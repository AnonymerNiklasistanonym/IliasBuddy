package com.niklasm.iliasbuddy.private_rss_feed_api.feed_requester;

import android.support.annotation.NonNull;

import com.android.volley.AuthFailureError;
import com.android.volley.VolleyError;

/**
 * Callback interface for any class that wants to make a private Ilias RSS feed request
 */
public interface IPrivateRssFeedRequesterClient {

    /**
     * Gets called when the Ilias RSS feed response is ready
     *
     * @param privateRssFeedXmlData The fetched Ilias RSS XML feed
     */
    void onFeedResponse(@NonNull final String privateRssFeedXmlData);

    /**
     * Gets called when there was an authentication error to the private Ilias RSS feed
     *
     * @param authenticationError Error exception
     */
    void onAuthenticationError(@NonNull final AuthFailureError authenticationError);

    /**
     * Gets called when there was a web response error of some kind
     *
     * @param responseError Error exception
     */
    void onResponseError(@NonNull final VolleyError responseError);
}
