package com.niklasm.iliasbuddy.private_rss_feed_api.feed_requester_api_client;

import android.support.annotation.NonNull;

import com.android.volley.AuthFailureError;
import com.android.volley.VolleyError;
import com.niklasm.iliasbuddy.private_rss_feed_api.IPrivateIliasFeedApiClient;
import com.niklasm.iliasbuddy.private_rss_feed_api.feed_entry.IliasRssEntry;
import com.niklasm.iliasbuddy.private_rss_feed_api.feed_parser.IPrivateRssFeedParser;
import com.niklasm.iliasbuddy.private_rss_feed_api.feed_parser.PrivateRssFeedParser;

import org.xmlpull.v1.XmlPullParserException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;

public class PrivateRssFeedRequesterApiIClient implements IPrivateRssFeedRequesterApiIClient {

    final private IPrivateIliasFeedApiClient client;
    final private IPrivateRssFeedParser parser;

    public PrivateRssFeedRequesterApiIClient(@NonNull final IPrivateIliasFeedApiClient clientClass) {
        client = clientClass;
        parser = new PrivateRssFeedParser();
    }

    @Override
    public void onFeedResponse(@NonNull final String privateRssFeedXmlData) {
        try {
            final InputStream feedInputStream = new ByteArrayInputStream(privateRssFeedXmlData
                    .replace("<rss version=\"2.0\">", "")
                    .replace("</rss>", "")
                    .getBytes(StandardCharsets.UTF_8));
            final IliasRssEntry[] parsedEntries = parser.parseFeed(feedInputStream);
            client.onFeedResponse(parsedEntries);
        } catch (final ParseException | XmlPullParserException | IOException e) {
            client.onFeedParseError(e);
        }
    }

    @Override
    public void onAuthenticationError(@NonNull final AuthFailureError authenticationError) {
        client.onAuthenticationError(authenticationError);
    }

    @Override
    public void onResponseError(@NonNull final VolleyError responseError) {
        client.onResponseError(responseError);
    }
}
