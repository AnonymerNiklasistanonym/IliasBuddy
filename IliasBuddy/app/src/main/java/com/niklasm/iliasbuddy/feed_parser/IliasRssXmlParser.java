package com.niklasm.iliasbuddy.feed_parser;

import android.support.annotation.NonNull;
import android.util.Xml;

import com.niklasm.iliasbuddy.objects.IliasRssFeedItem;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

/**
 * Class that parses IliasRssFeedItem's from an IliasRssFeed InputStream
 * (mostly inspired by this tutorial: https://developer.android.com/training/basics/network-ops/xml.html)
 */
public class IliasRssXmlParser {

    final private static String DATE_TAG = "pubDate";
    final private static String DESCRIPTION_TAG = "description";
    final private static String LINK_TAG = "link";
    final private static String TITLE_TAG = "title";
    final private static String ENTRY_TAG = "item";
    final private static String FEED_START_TAG = "channel";
    final private static String TIME_PARSE_FORMAT = "EEE, dd MMM yyyy HH:mm:ss ZZZZZ";

    /**
     * @param inputStream Contains the XML data String
     * @return Array that contains all listed IliasRssItems from the IliasRssFeed
     * @throws XmlPullParserException If a XML tag is in the wrong place, XML data is incomplete
     *                                or anything other XML related
     * @throws IOException            Input stream errors or I don't know
     */
    @NonNull
    public static IliasRssFeedItem[] parse(@NonNull final InputStream inputStream)
            throws XmlPullParserException, IOException, ParseException {
        try {
            // create XmlPullParser parser for parsing the XML data in the shape of a InputStream
            final XmlPullParser parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(inputStream, null);
            parser.nextTag();
            return IliasRssXmlParser.readFeed(parser);
        } finally {
            // close input stream if something bad happens
            inputStream.close();
        }
    }

    /**
     * Extract all listed IliasRssItems from the IliasRssFeed with the given XmlPullParser
     *
     * @param PARSER XmlPullParser that has as content the XML data "array" with all entries
     * @return Array that contains all listed IliasRssItems from the IliasRssFeed
     * @throws XmlPullParserException If a XML tag is in the wrong place, XML data is incomplete
     *                                or anything other XML related
     * @throws IOException            Input stream errors or I don't know
     */
    @NonNull
    private static IliasRssFeedItem[] readFeed(@NonNull final XmlPullParser PARSER)
            throws XmlPullParserException, IOException, ParseException {

        // create ArrayList for all IliasRssItems in XML file
        final ArrayList<IliasRssFeedItem> ENTRIES = new ArrayList<>();
        // require that "channel" is the current Tag
        PARSER.require(XmlPullParser.START_TAG, null, IliasRssXmlParser.FEED_START_TAG);
        // iterate to the next element as long as the start tag that was just required is not found
        while (PARSER.next() != XmlPullParser.END_TAG) {
            if (PARSER.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            switch (PARSER.getName()) {
                case IliasRssXmlParser.ENTRY_TAG:
                    // if an item tag was found read this RSS entry
                    ENTRIES.add(IliasRssXmlParser.readEntry(PARSER));
                    break;
                default:
                    // if another tag was found skip it
                    IliasRssXmlParser.skip(PARSER);
            }
        }
        // return an array from the ArrayList
        return ENTRIES.toArray(new IliasRssFeedItem[0]);
    }

    @NonNull
    private static IliasRssFeedItem readEntry(@NonNull final XmlPullParser PARSER)
            throws XmlPullParserException, IOException, ParseException {

        PARSER.require(XmlPullParser.START_TAG, null, IliasRssXmlParser.ENTRY_TAG);
        String[] courseExtraTitleExtraTitle = new String[]{null, null, null, null};
        String link = null;
        String description = null;
        Date date = null;

        while (PARSER.next() != XmlPullParser.END_TAG) {
            if (PARSER.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            final String name = PARSER.getName();
            switch (name) {
                case IliasRssXmlParser.TITLE_TAG:
                    courseExtraTitleExtraTitle =
                            IliasRssXmlParser.readCourseExtraTitleTitleExtra(PARSER);
                    break;
                case IliasRssXmlParser.LINK_TAG:
                    link = IliasRssXmlParser.readLink(PARSER);
                    break;
                case IliasRssXmlParser.DESCRIPTION_TAG:
                    description = IliasRssXmlParser.readDescription(PARSER);
                    break;
                case IliasRssXmlParser.DATE_TAG:
                    date = IliasRssXmlParser.readDate(PARSER);
                    break;
                default:
                    IliasRssXmlParser.skip(PARSER);
            }
        }

        // Quickfix: TODO make later better file recognition
        // if description is empty we have a file
        if (description == null || description.equals("")) {
            final String temp = courseExtraTitleExtraTitle[1];
            courseExtraTitleExtraTitle[1] = courseExtraTitleExtraTitle[3];
            courseExtraTitleExtraTitle[3] = temp;
        }

        return new IliasRssFeedItem(courseExtraTitleExtraTitle[0], courseExtraTitleExtraTitle[1],
                courseExtraTitleExtraTitle[2], Objects.requireNonNull(description),
                Objects.requireNonNull(link), Objects.requireNonNull(date),
                courseExtraTitleExtraTitle[3]);
    }

    @NonNull
    private static Date readDate(@NonNull final XmlPullParser parser)
            throws IOException, XmlPullParserException, ParseException {
        parser.require(XmlPullParser.START_TAG, null, IliasRssXmlParser.DATE_TAG);
        final String DATE_STRING = IliasRssXmlParser.readText(parser);
        parser.require(XmlPullParser.END_TAG, null, IliasRssXmlParser.DATE_TAG);
        return new SimpleDateFormat(IliasRssXmlParser.TIME_PARSE_FORMAT, Locale.US)
                .parse(DATE_STRING);
    }

    @NonNull
    private static String readDescription(@NonNull final XmlPullParser parser)
            throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, null, IliasRssXmlParser.DESCRIPTION_TAG);
        final String DESCRIPTION = IliasRssXmlParser.readText(parser);
        parser.require(XmlPullParser.END_TAG, null, IliasRssXmlParser.DESCRIPTION_TAG);
        return DESCRIPTION;
    }

    @NonNull
    private static String readLink(@NonNull final XmlPullParser parser)
            throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, null, IliasRssXmlParser.LINK_TAG);
        final String LINK = IliasRssXmlParser.readText(parser);
        parser.require(XmlPullParser.END_TAG, null, IliasRssXmlParser.LINK_TAG);
        return LINK;
    }

    @NonNull
    private static String[] readCourseExtraTitleTitleExtra(@NonNull final XmlPullParser parser)
            throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, null, IliasRssXmlParser.TITLE_TAG);
        final String courseExtraTitle = IliasRssXmlParser.readText(parser);
        final String courseExtra = courseExtraTitle.substring(courseExtraTitle.indexOf("[") + 1,
                courseExtraTitle.indexOf("]")).trim();
        final boolean extraExists = courseExtraTitle.contains(">");
        final String course = extraExists ? courseExtra.substring(0,
                courseExtra.indexOf(">")).trim() : courseExtra;
        final String extra =
                extraExists ? courseExtra.substring(courseExtra.indexOf(">") + 1).trim() : null;
        final String titleTitleExtra =
                courseExtraTitle.substring(courseExtraTitle.indexOf("]") + 1).trim();
        final boolean titleExtraExists = titleTitleExtra.contains(":");
        final String title = titleExtraExists ? titleTitleExtra
                .substring(titleTitleExtra.indexOf(":") + 1).trim() : titleTitleExtra;
        final String titleExtra = titleExtraExists ? titleTitleExtra.substring(0,
                titleTitleExtra.indexOf(":")).trim() : null;
        parser.require(XmlPullParser.END_TAG, null, IliasRssXmlParser.TITLE_TAG);

        return new String[]{course, titleExtra, title, extra};

    }

    /**
     * For the tags title and summary, extracts their text values.
     */
    @NonNull
    private static String readText(@NonNull final XmlPullParser parser)
            throws IOException, XmlPullParserException {
        String result = "";
        if (parser.next() == XmlPullParser.TEXT) {
            result = parser.getText();
            parser.nextTag();
        }
        return result;
    }

    private static void skip(@NonNull final XmlPullParser PARSER)
            throws XmlPullParserException, IOException {
        // If initial state is not a defined START_TAG throw exception
        if (PARSER.getEventType() != XmlPullParser.START_TAG) {
            throw new IllegalStateException();
        }
        // Because we already read the initial tag we set the depth to 1
        int depth = 1;
        // Now we iterate over all tags as long as we fo not find the final END_TAG (= depth is 0)
        while (depth != 0) {
            switch (PARSER.next()) {
                case XmlPullParser.END_TAG:
                    depth--;
                    break;
                case XmlPullParser.START_TAG:
                    depth++;
                    break;
            }
        }
    }
}
