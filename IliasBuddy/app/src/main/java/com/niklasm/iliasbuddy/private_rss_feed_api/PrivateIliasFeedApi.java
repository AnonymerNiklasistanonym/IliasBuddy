package com.niklasm.iliasbuddy.private_rss_feed_api;

import android.content.Context;
import android.support.annotation.NonNull;

import com.niklasm.iliasbuddy.private_rss_feed_api.feed_requester.IPrivateRssFeedRequester;
import com.niklasm.iliasbuddy.private_rss_feed_api.feed_requester.PrivateRssFeedRequester;
import com.niklasm.iliasbuddy.private_rss_feed_api.feed_requester_api_client.PrivateRssFeedRequesterApiIClient;

public class PrivateIliasFeedApi implements IPrivateIliasFeedApi {

    final private IPrivateRssFeedRequester requester;

    public <T extends Context & IPrivateIliasFeedApiClient> PrivateIliasFeedApi(@NonNull final T clientClass) {
        requester = new PrivateRssFeedRequester(new PrivateRssFeedRequesterApiIClient(clientClass), clientClass);
    }

    @Override
    public void getCurrentPrivateIliasFeed() {
        requester.requestPrivateRssFeed();
    }
}
