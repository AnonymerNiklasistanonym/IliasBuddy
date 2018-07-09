package com.example.niklasm.iliasbuddy.ilias_rss_handler;

import com.android.volley.AuthFailureError;
import com.android.volley.VolleyError;

public interface IliasRssXmlWebRequesterInterface {

    /**
     * Gets called when the ILIAS RSS feed was fetched
     *
     * @param FEED_XML_DATA (String) - The fetched ILIAS RSS XML feed
     */
    void processIliasXml(final String FEED_XML_DATA);

    /**
     * Gets called when there was an authentication error to the ILIAS RSS XML feed
     *
     * @param authenticationError (AuthFailureError) - Error message
     */
    void webAuthenticationError(AuthFailureError authenticationError);

    /**
     * Gets called when there was a response error of some kind
     *
     * @param responseError (VolleyError) - Error message
     */
    void webResponseError(VolleyError responseError);
}
