package com.niklasm.iliasbuddy.private_rss_feed_api.feed_requester;

/**
 * Methods PrivateRssFeedRequester should implement
 */
public interface IPrivateRssFeedRequester {

    /**
     * Make a request to download/fetch the current private Ilias RSS feed
     */
    void requestPrivateRssFeed();
}
