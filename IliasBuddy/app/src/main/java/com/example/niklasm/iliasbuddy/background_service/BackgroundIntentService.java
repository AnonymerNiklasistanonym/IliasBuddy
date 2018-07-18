package com.example.niklasm.iliasbuddy.background_service;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.text.Html;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.VolleyError;
import com.example.niklasm.iliasbuddy.MainActivity;
import com.example.niklasm.iliasbuddy.R;
import com.example.niklasm.iliasbuddy.notification_handler.IliasBuddyNotification;
import com.example.niklasm.iliasbuddy.notification_handler.IliasBuddyNotificationInterface;
import com.example.niklasm.iliasbuddy.objects.IliasRssFeedItem;
import com.example.niklasm.iliasbuddy.rss_handler.IliasRssCache;
import com.example.niklasm.iliasbuddy.rss_handler.IliasRssXmlParser;
import com.example.niklasm.iliasbuddy.rss_handler.IliasRssXmlWebRequester;
import com.example.niklasm.iliasbuddy.rss_handler.IliasRssXmlWebRequesterInterface;

import org.xmlpull.v1.XmlPullParserException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class BackgroundIntentService extends Service implements IliasRssXmlWebRequesterInterface {

    public final static String NOTIFICATION_INTENT_EXTRA_PREVIEW_STRING = "previewString";
    public final static String NOTIFICATION_INTENT_EXTRA_BIG_STRING = "bigString";
    public static final String NOTIFICATION_INTENT_MESSAGE_COUNT = "only_one";
    final public static String LAST_NOTIFICATION_TEXT = "LAST_NOTIFICATION_TEXT";
    final public static String LATEST_ELEMENT = "LATEST_ELEMENT";
    private final static String LATEST_ITEM_NOT_FOUND = "nothing_found";
    private static IliasRssFeedItem[] current_items;

    @Override
    public int onStartCommand(final Intent intent, final int flags, final int startId) {
        Log.i("BackgroundIntentService", "onStartCommand");

        Log.i("BackgroundIntentService", "startId: " + startId);

        // check the intent if the current notification was dismissed but read
        if (BackgroundIntentService.current_items != null &&
                intent.getBooleanExtra(IliasBuddyNotificationInterface.NOTIFICATION_DISMISSED,
                        false)) {
            try {
                IliasRssCache.setCache(this, BackgroundIntentService.current_items);
                Log.i("BackgroundIntentService", "updated items on dismiss");
                // dismiss notification if everything went right
                IliasBuddyNotification.hideNotificationNewEntries(this);
                // and if the main activity is opened refresh content
                Log.i("BackgroundIntentService", "sendBroadcast");
                LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(IliasBuddyNotificationInterface.UPDATE_SILENT));
            } catch (final IOException e) {
                Log.e("BackgroundIntentService", "updated items NOT on dismiss");
                e.printStackTrace();
            }
        }

        // make a web request with the important data
        final IliasRssXmlWebRequester webRequester =
                new IliasRssXmlWebRequester(this);
        webRequester.getWebContent();

        // return this so that the service can be restarted
        return Service.START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(final Intent arg0) {
        return null;
    }

    public void createNotification(@NonNull final String NOTIFICATION_TITLE,
                                   @NonNull final String NOTIFICATION_PREVIEW_CONTENT,
                                   final String bigString,
                                   final String[] INBOX_MESSAGES, final String URL,
                                   final IliasRssFeedItem TEST_ENTRY, final IliasRssFeedItem[] ALL_NEW_ENTRIES) {

        final SharedPreferences myPrefs =
                android.preference.PreferenceManager.getDefaultSharedPreferences(this);
        final String latestItem =
                myPrefs.getString(BackgroundIntentService.LAST_NOTIFICATION_TEXT,
                        BackgroundIntentService.LATEST_ITEM_NOT_FOUND);

        if (!latestItem.equals(BackgroundIntentService.LATEST_ITEM_NOT_FOUND)
                && latestItem.equals(bigString)) {
            Log.i("BackgroundIntentService",
                    "Do not make a new notification, the text is the same");
            Log.i("BackgroundIntentService", "latestItem: " + latestItem);
            Log.i("BackgroundIntentService", "bigString: " + bigString);
            return;
        } else {
            Log.i("BackgroundIntentService",
                    "Make a new notification, the text is NOT the same");
        }

        // if new notification save the old String
        myPrefs.edit()
                .putString(BackgroundIntentService.LAST_NOTIFICATION_TEXT, bigString).apply();

        final Intent ON_CLICK = new Intent(this, MainActivity.class)
                .putExtra(IliasBuddyNotificationInterface.NEW_ENTRY_FOUND, true)
                .putExtra(IliasBuddyNotificationInterface.NEW_ENTRY_DATA, (Parcelable) TEST_ENTRY)
                .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

        IliasBuddyNotification.showNotificationNewEntries(this, NOTIFICATION_TITLE,
                NOTIFICATION_PREVIEW_CONTENT, INBOX_MESSAGES, ON_CLICK, URL);

        final Intent callMainActivity = new Intent(IliasBuddyNotificationInterface.FOUND_A_NEW_ENTRY)
                .putExtra(BackgroundIntentService.NOTIFICATION_INTENT_EXTRA_PREVIEW_STRING,
                        NOTIFICATION_PREVIEW_CONTENT)
                .putExtra(BackgroundIntentService.NOTIFICATION_INTENT_MESSAGE_COUNT, INBOX_MESSAGES.length)
                .putExtra(BackgroundIntentService.NOTIFICATION_INTENT_EXTRA_BIG_STRING, bigString);
        LocalBroadcastManager.getInstance(this).sendBroadcast(callMainActivity);
    }

    private IliasRssFeedItem[] getNewElements(final IliasRssFeedItem[] NEW_DATA_SET,
                                              final String CURRENT_LATEST_ENTRY) {

        // if new data is null return null
        if (NEW_DATA_SET == null || NEW_DATA_SET.length == 0) {
            return null;
        }

        // if current latest entry is null return the whole data set
        if (CURRENT_LATEST_ENTRY == null) {
            return NEW_DATA_SET;
        }

        final ArrayList<IliasRssFeedItem> newIliasRssItems = new ArrayList<>();

        // else check which elements of the new data set are new
        for (final IliasRssFeedItem NEW_ENTRY : NEW_DATA_SET) {
            if (NEW_ENTRY.toString().equals(CURRENT_LATEST_ENTRY)) {
                // if newest entry was found return all found entries
                return newIliasRssItems.toArray(new IliasRssFeedItem[0]);
            } else {
                newIliasRssItems.add(NEW_ENTRY);
            }
        }

        // if all entries where new return the complete new data set
        return NEW_DATA_SET;
    }

    @Override
    public void processIliasXml(final String FEED_XML_DATA) {
        Log.i("BackgroundIntentService", "parseXml");
        MainActivity.devOptionSetLastResponse(FEED_XML_DATA);
        final InputStream stream = new ByteArrayInputStream(FEED_XML_DATA
                .replace("<rss version=\"2.0\">", "")
                .replace("</rss>", "").getBytes(StandardCharsets.UTF_8));
        final IliasRssFeedItem[] myDataSet;
        try {
            myDataSet = IliasRssXmlParser.parse(stream);
        } catch (ParseException | XmlPullParserException | IOException e) {
            e.printStackTrace();
            return;
        }

        // get latest item string form shared preferences

        final SharedPreferences myPrefs =
                android.preference.PreferenceManager.getDefaultSharedPreferences(this);
        final String latestItem =
                myPrefs.getString(BackgroundIntentService.LATEST_ELEMENT, null);

        final IliasRssFeedItem[] NEW_ENTRIES = getNewElements(myDataSet, latestItem);

        final SimpleDateFormat VIEW_DATE_FORMAT = new SimpleDateFormat(
                "dd.MM HH:mm", getResources().getConfiguration().locale);

        if (NEW_ENTRIES != null) {
            BackgroundIntentService.current_items = NEW_ENTRIES;
        }

        if (NEW_ENTRIES == null || NEW_ENTRIES.length == 0) {
            Log.i("BackgroundIntentService", "No new entries where found");
        } else if (NEW_ENTRIES.length == 1) {
            Log.i("BackgroundIntentService", "One new entry was found");

            final String previewString = myDataSet[0].getCourse() +
                    (myDataSet[0].getExtra() != null ? " > " + myDataSet[0].getExtra() : "");
            final String bigString = NEW_ENTRIES[0].toStringNotificationSingle(VIEW_DATE_FORMAT)
                    + (myDataSet[0].getDescription().equals("") ? "\n\n"
                    + Html.fromHtml(myDataSet[0].getDescription()) : "---");

            createNotification(
                    getString(R.string.notification_channel_new_entries_one_new_ilias_entry),
                    previewString, bigString.trim(), new String[]{bigString},
                    myDataSet[0].getLink(), myDataSet[0], NEW_ENTRIES);
        } else {
            Log.i("BackgroundIntentService", "More than one new entry was found");

            final String previewString = "(" + myDataSet[0].getCourse() + ", "
                    + myDataSet[1].getCourse() + ",... )";
            final StringBuilder bigString = new StringBuilder("");
            final String[] inboxArray = new String[NEW_ENTRIES.length];
            for (int i = 0; i < NEW_ENTRIES.length; i++) {
                final IliasRssFeedItem entry = NEW_ENTRIES[i];
                inboxArray[i] = entry.toStringNotificationMultiple(VIEW_DATE_FORMAT);
                bigString.append("- ").append(inboxArray[i]).append(
                        (i != NEW_ENTRIES.length - 1 ? "\n" : ""));
            }

            createNotification(NEW_ENTRIES.length + " " +
                            getString(R.string.notification_channel_new_entries_new_ilias_entries),
                    previewString, bigString.toString(), inboxArray,
                    null, null, NEW_ENTRIES);
        }
    }

    @Override
    public void webAuthenticationError(final AuthFailureError error) {
        Log.e("BackgroundInt - AuthErr", error.toString());
    }

    @Override
    public void webResponseError(final VolleyError error) {
        Log.e("BackgroundInt - RespErr", error.toString());
    }
}
