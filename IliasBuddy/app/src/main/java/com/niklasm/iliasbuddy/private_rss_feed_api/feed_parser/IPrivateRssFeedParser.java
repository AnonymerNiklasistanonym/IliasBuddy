package com.niklasm.iliasbuddy.private_rss_feed_api.feed_parser;

import android.support.annotation.NonNull;

import com.niklasm.iliasbuddy.private_rss_feed_api.feed_entry.IliasRssEntry;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;

public interface IPrivateRssFeedParser {
    /**
     * Parse input stream that contains the RSS XML data string to private Ilias RSS entries
     *
     * @param inputStream RSS XML data
     * @return Array that contains all private Ilias RSS entries
     * @throws XmlPullParserException XML parse exception
     * @throws IOException            IO exception
     * @throws ParseException         Date parse exception
     */
    @NonNull
    IliasRssEntry[] parseFeed(@NonNull final InputStream inputStream) throws XmlPullParserException, IOException, ParseException;
}
