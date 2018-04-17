package com.example.niklasm.iliasbuddy;

import android.util.Log;
import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class IliasXmlParser {

    // We don't use namespaces
    private static final String ns = null;

    /**
     *
     * @param inputStream Contains the XML data String
     * @return Array that contains all listed IliasRssItems from the IliasRssFeed
     * @throws XmlPullParserException If a XML tag is in the wrong place, XML data is incomplete
     * or anything other XML related
     * @throws IOException Input stream errors or I don't know
     */
    public static IliasRssItem[] parse(InputStream inputStream) throws XmlPullParserException, IOException {
        try {
            // create XmlPullParser parser for parsing the XML data in the shape of a InputStream
            final XmlPullParser parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(inputStream, null);
            parser.nextTag();
            return readFeed(parser);
        } finally {
            // close input stream if something bad happens
            inputStream.close();
        }
    }

    /**
     * Extract all listed IliasRssItems from the IliasRssFeed with the given XmlPullParser
     * @param parser XmlPullParser that has as content the XML data "array" with all entries
     * @return Array that contains all listed IliasRssItems from the IliasRssFeed
     * @throws XmlPullParserException If a XML tag is in the wrong place, XML data is incomplete
     * or anything other XML related
     * @throws IOException Input stream errors or I don't know
     */
    private static IliasRssItem[] readFeed(XmlPullParser parser) throws XmlPullParserException, IOException {

        // create ArrayList for all IliasRssItems in XML file
        ArrayList<IliasRssItem> entries = new ArrayList<>();
        // require that "channel" is the current Tag
        parser.require(XmlPullParser.START_TAG, ns, "channel");
        // iterate to the next element as long as the start tag that was just required is not found
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) continue;
            switch (parser.getName()) {
                case "item":
                    // if an item tag was found read this RSS entry
                    entries.add(readEntry(parser));
                    break;
                default:
                    // if another tag was found skip it
                    skip(parser);
            }
        }
        // return an array from the ArrayList
        return entries.toArray(new IliasRssItem[0]);
    }

    private static IliasRssItem readEntry(XmlPullParser parser) throws XmlPullParserException, IOException {

        parser.require(XmlPullParser.START_TAG, ns, "item");
        String[] courseExtraTitle = new String[]{null, null, null};
        String link = null;
        String description = null;
        Date date = null;

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            switch (name) {
                case "title":
                    courseExtraTitle = readCourseExtraTitle(parser);
                    break;
                case "link":
                    link = readLink(parser);
                    break;
                case "description":
                    description = readDescription(parser);
                    break;
                case "pubDate":
                    date = readDate(parser);
                    break;
                default:
                    skip(parser);
            }
        }
        return new IliasRssItem(courseExtraTitle[0], courseExtraTitle[1], courseExtraTitle[2], description, link, date);
    }

    private static Date readDate(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, ns, "pubDate");
        final String dateString = readText(parser);
        parser.require(XmlPullParser.END_TAG, ns, "pubDate");
        try {
            final SimpleDateFormat sf1 = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss ZZZZZ");
            return sf1.parse(dateString);
        } catch (ParseException e) {
            Log.e("Error Date", e.toString());
            return null;
        }
    }

    private static String readDescription(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, ns, "description");
        final String description = readText(parser);
        parser.require(XmlPullParser.END_TAG, ns, "description");
        return description;
    }

    private static String readLink(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, ns, "link");
        final String link = readText(parser);
        parser.require(XmlPullParser.END_TAG, ns, "link");
        return link;
    }

    private static String[] readCourseExtraTitle(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, ns, "title");
        final String courseExtraTitle = readText(parser);
        final String courseExtra = courseExtraTitle.substring(courseExtraTitle.indexOf("[") + 1, courseExtraTitle.indexOf("]")).trim();
        final boolean extraExists = courseExtraTitle.contains(">");
        final String course = extraExists ? courseExtra.substring(0, courseExtra.indexOf(">") - 1).trim() : courseExtra;
        final String extra = extraExists ? courseExtra.substring(courseExtra.indexOf(">") + 1).trim() : null;
        final String title = courseExtraTitle.substring(courseExtraTitle.indexOf("]") + 1).trim();
        parser.require(XmlPullParser.END_TAG, ns, "title");
        return new String[]{course, extra, title};
    }

    // For the tags title and summary, extracts their text values.
    private static String readText(XmlPullParser parser) throws IOException, XmlPullParserException {
        String result = "";
        if (parser.next() == XmlPullParser.TEXT) {
            result = parser.getText();
            parser.nextTag();
        }
        return result;
    }

    private static void skip(XmlPullParser parser) throws XmlPullParserException, IOException {
        if (parser.getEventType() != XmlPullParser.START_TAG) {
            throw new IllegalStateException();
        }
        int depth = 1;
        while (depth != 0) {
            switch (parser.next()) {
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
