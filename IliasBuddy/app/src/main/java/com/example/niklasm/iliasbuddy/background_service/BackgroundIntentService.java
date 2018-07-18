package com.example.niklasm.iliasbuddy.background_service;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.VolleyError;
import com.example.niklasm.iliasbuddy.MainActivity;
import com.example.niklasm.iliasbuddy.R;
import com.example.niklasm.iliasbuddy.feed_parser.IliasRssXmlParser;
import com.example.niklasm.iliasbuddy.feed_parser.IliasRssXmlWebRequester;
import com.example.niklasm.iliasbuddy.feed_parser.IliasRssXmlWebRequesterInterface;
import com.example.niklasm.iliasbuddy.handler.IliasBuddyCacheHandler;
import com.example.niklasm.iliasbuddy.handler.IliasBuddyPreferenceHandler;
import com.example.niklasm.iliasbuddy.notification_handler.IliasBuddyNotificationHandler;
import com.example.niklasm.iliasbuddy.objects.IliasRssFeedItem;

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
    public static final String NOTIFICATION_INTENT_MESSAGE_COUNT = "only_one";
    final public static String LAST_NOTIFICATION_TEXT = "FEED_LAST_NOTIFICATION_TEXT";
    final public static String LATEST_ELEMENT = "LATEST_ELEMENT";
    private static IliasRssFeedItem[] current_items;

    @Override
    public int onStartCommand(final Intent intent, final int flags, final int startId) {
        Log.i("BackgroundIntentService", "onStartCommand");
        
        // check the intent if the current notification was dismissed but read
        if (BackgroundIntentService.current_items != null &&
                intent.getBooleanExtra(IliasBuddyNotificationHandler.NOTIFICATION_DISMISSED,
                        false)) {
            try {
                IliasBuddyCacheHandler.setCache(this, BackgroundIntentService.current_items);
                Log.i("BackgroundIntentService", "updated items on dismiss");
                // dismiss notification if everything went right
                IliasBuddyNotificationHandler.hideNotificationNewEntries(this);
                // and if the main activity is opened refresh content
                Log.i("BackgroundIntentService", "sendBroadcast");
                LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(IliasBuddyNotificationHandler.UPDATE_SILENT));
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
                                   @NonNull final IliasRssFeedItem[] NEW_ENTRIES) {

        final String LAST_NOTIFICATION_TEXT = IliasBuddyPreferenceHandler
                .getLastNotificationText(this, null);

        // check if the last notification is not null and not the current text for no double ones
        if (LAST_NOTIFICATION_TEXT != null &&
                LAST_NOTIFICATION_TEXT.equals(NEW_ENTRIES[0].toString())) {
            Log.i("BackgroundIntentService", "No new notification, text is the same");
            return;
        }

        // if new notification save the first entry
        IliasBuddyPreferenceHandler
                .setLastNotificationText(this, NEW_ENTRIES[0].toString());

        final SimpleDateFormat VIEW_DATE_FORMAT = new SimpleDateFormat(
                "dd.MM HH:mm", getResources().getConfiguration().locale);

        final String[] NOTIFICATION_TEXT_ARRAY = new String[NEW_ENTRIES.length];
        if (NEW_ENTRIES.length > 1) {
            for (int i = 0; i < NEW_ENTRIES.length; i++) {
                NOTIFICATION_TEXT_ARRAY[i] =
                        NEW_ENTRIES[i].toStringNotificationBigMultiple(this);
            }
        } else {
            NOTIFICATION_TEXT_ARRAY[0] =
                    NEW_ENTRIES[0].toStringNotificationBigSingle(this);
        }

        final Intent ON_CLICK = new Intent(this, MainActivity.class)
                .putExtra(IliasBuddyNotificationHandler.NEW_ENTRY_FOUND, true)
                .putExtra(IliasBuddyNotificationHandler.NEW_ENTRY_DATA, (Parcelable) NEW_ENTRIES[0])
                .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

        IliasBuddyNotificationHandler.showNotificationNewEntries(this, NOTIFICATION_TITLE,
                NOTIFICATION_PREVIEW_CONTENT, NOTIFICATION_TEXT_ARRAY, ON_CLICK,
                NEW_ENTRIES[0].getLink());

        final Intent callMainActivity = new Intent(IliasBuddyNotificationHandler.FOUND_A_NEW_ENTRY)
                .putExtra(BackgroundIntentService.NOTIFICATION_INTENT_EXTRA_PREVIEW_STRING,
                        NOTIFICATION_PREVIEW_CONTENT)
                .putExtra(BackgroundIntentService.NOTIFICATION_INTENT_MESSAGE_COUNT, NEW_ENTRIES.length);
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

        if (NEW_ENTRIES == null || NEW_ENTRIES.length < 1) {
            return;
        }

        // TODO why does thi exist
        BackgroundIntentService.current_items = NEW_ENTRIES;

        if (NEW_ENTRIES.length == 1) {
            createNotification(
                    getString(R.string.notification_channel_new_entries_one_new_ilias_entry),
                    myDataSet[0].toStringNotificationPreview(this),
                    new IliasRssFeedItem[]{myDataSet[0]});
        } else {
            createNotification(NEW_ENTRIES.length + " " +
                            getString(R.string.notification_channel_new_entries_new_ilias_entries),
                    "(" + myDataSet[0].getCourse() + ", "
                            + myDataSet[1].getCourse() + ",...)", NEW_ENTRIES);
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
