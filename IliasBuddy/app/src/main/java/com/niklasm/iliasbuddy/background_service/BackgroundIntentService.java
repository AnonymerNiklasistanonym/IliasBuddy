package com.niklasm.iliasbuddy.background_service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.VolleyError;
import com.example.niklasm.iliasbuddy.R;
import com.niklasm.iliasbuddy.MainActivity;
import com.niklasm.iliasbuddy.feed_parser.IliasRssXmlParser;
import com.niklasm.iliasbuddy.feed_parser.IliasRssXmlWebRequester;
import com.niklasm.iliasbuddy.feed_parser.IliasRssXmlWebRequesterInterface;
import com.niklasm.iliasbuddy.handler.IliasBuddyBroadcastHandler;
import com.niklasm.iliasbuddy.handler.IliasBuddyCacheHandler;
import com.niklasm.iliasbuddy.handler.IliasBuddyPreferenceHandler;
import com.niklasm.iliasbuddy.notification_handler.IliasBuddyNotificationHandler;
import com.niklasm.iliasbuddy.objects.IliasRssFeedItem;

import org.xmlpull.v1.XmlPullParserException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.ArrayList;

public class BackgroundIntentService extends Service implements IliasRssXmlWebRequesterInterface {

    @Override
    public int onStartCommand(final Intent INTENT, final int flags, final int startId) {
        Log.d("BackgroundIntentService", "onStartCommand");

        // check the intent to find out if the current notification was dismissed
        if (INTENT.getBooleanExtra(IliasBuddyNotificationHandler.NOTIFICATION_DISMISSED,
                false)) {
            Log.d("BackgroundIntentService", "NOTIFICATION_DISMISSED");
            // then get all new entries
            final Parcelable[] NEW_ENTRIES_EXTRA_Parcelable =
                    INTENT.getParcelableArrayExtra(IliasBuddyNotificationHandler.NEW_ENTRIES);
            // if the entries are not null add them to the cache
            if (NEW_ENTRIES_EXTRA_Parcelable != null) {
                // convert Parcelable[] to IliasRssFeedItem[]
                final IliasRssFeedItem[] NEW_ENTRIES_EXTRA =
                        IliasRssFeedItem.readParcelableArray(NEW_ENTRIES_EXTRA_Parcelable);
                try {
                    // add them to the cache
                    IliasBuddyCacheHandler.addToCache(this, NEW_ENTRIES_EXTRA);
                    // dismiss notification if everything went right
                    IliasBuddyNotificationHandler.hideNotificationNewEntries(this);
                    // and if the main activity is opened refresh content
                    Log.d("BackgroundIntentService", "Send broadcast to MainActivity");
                    IliasBuddyBroadcastHandler.sendBroadcastUpdateSilent(this);
                } catch (final IOException | ClassNotFoundException e) {
                    Log.e("BackgroundIntentService", "Parcelable items weren't be added" +
                            e.toString());
                    e.printStackTrace();
                }
            } else {
                Log.e("BackgroundIntentService", "Parcelable items could not be read");
            }
        }

        // make a web request
        new IliasRssXmlWebRequester(this).getWebContent();

        // return this so that the service can be restarted
        return Service.START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(final Intent arg0) {
        return null;
    }

    public void createNotification(@NonNull final IliasRssFeedItem[] NEW_ENTRIES) {

        /*
        Get the latest element from the last notification as String
        If the element is different to the current latest element continue
        */
        final String LAST_NOTIFICATION_LATEST_ITEM = IliasBuddyPreferenceHandler
                .getLatestItemToString(this, null);
        if (LAST_NOTIFICATION_LATEST_ITEM == null ||
                LAST_NOTIFICATION_LATEST_ITEM.equals(NEW_ENTRIES[0].toString())) {
            Log.d("BackgroundIntentService", "No notification, latest element not latest");
            return;
        }

        // Save current latest element in the preferences
        IliasBuddyPreferenceHandler.setLatestItemToString(this, NEW_ENTRIES[0].toString());

        // Determine notification title, preview and big content
        final String NOTIFICATION_TITLE;
        final String NOTIFICATION_PREVIEW_CONTENT;
        final String[] NOTIFICATION_TEXT_ARRAY = new String[NEW_ENTRIES.length];
        if (NEW_ENTRIES.length == 1) {
            // Only one new item
            NOTIFICATION_TITLE =
                    getString(R.string.notification_channel_new_entries_one_new_ilias_entry);
            NOTIFICATION_PREVIEW_CONTENT =
                    NEW_ENTRIES[0].toStringNotificationPreview(this);
            NOTIFICATION_TEXT_ARRAY[0] =
                    NEW_ENTRIES[0].toStringNotificationBigSingle(this);
        } else {
            // More than one new item
            NOTIFICATION_TITLE = NEW_ENTRIES.length + " " +
                    getString(R.string.notification_channel_new_entries_new_ilias_entries);
            NOTIFICATION_PREVIEW_CONTENT = "(" + NEW_ENTRIES[0].getCourse() + ", "
                    + NEW_ENTRIES[1].getCourse() + ",...)";
            for (int i = 0; i < NEW_ENTRIES.length; i++) {
                NOTIFICATION_TEXT_ARRAY[i] =
                        NEW_ENTRIES[i].toStringNotificationBigMultiple(this);
            }
        }

        // Create notification on click intent which gives all the information to the main activity
        final Intent ON_CLICK = new Intent(this, MainActivity.class)
                .putExtra(IliasBuddyNotificationHandler.NEW_ENTRY_FOUND, true)
                .putExtra(IliasBuddyNotificationHandler.NEW_ENTRY_DATA, NEW_ENTRIES)
                .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

        // Show notification with all the values
        IliasBuddyNotificationHandler.showNotificationNewEntries(this, NOTIFICATION_TITLE,
                NOTIFICATION_PREVIEW_CONTENT, NOTIFICATION_TEXT_ARRAY, ON_CLICK, NEW_ENTRIES,
                NEW_ENTRIES[0].getLink());

        // Additionally send a broadcast to the main activity if it currently is running
        IliasBuddyBroadcastHandler.sendBroadcastNewEntriesFound(this,
                NOTIFICATION_PREVIEW_CONTENT, NEW_ENTRIES);
    }

    private IliasRssFeedItem[] getNewElements(final IliasRssFeedItem[] NEW_DATA_SET) {

        // if new data is null return null
        if (NEW_DATA_SET == null || NEW_DATA_SET.length == 0) {
            return null;
        }

        final String CURRENT_LATEST_ENTRY =
                IliasBuddyPreferenceHandler.getLatestItemToString(this, null);

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
        Log.d("BackgroundIntentService", "processIliasXml");
        MainActivity.devOptionSetLastResponse(FEED_XML_DATA);

        // Parse the current Ilias feed to an IliasRssFeedItem[]
        final IliasRssFeedItem[] PARSED_WEB_RSS_FEED;
        try {
            Log.d("BackgroundIntentService", "processIliasXml >> Parse entries");
            PARSED_WEB_RSS_FEED = IliasRssXmlParser.parse(new ByteArrayInputStream(FEED_XML_DATA
                    .replace("<rss version=\"2.0\">", "")
                    .replace("</rss>", "").getBytes(StandardCharsets.UTF_8)));
        } catch (ParseException | XmlPullParserException | IOException e) {
            e.printStackTrace();
            return;
        }

        // Find out which entries of them are new
        final IliasRssFeedItem[] NEW_ENTRIES = getNewElements(PARSED_WEB_RSS_FEED);

        // Do nothing if there are no entries or zero new ones
        if (NEW_ENTRIES == null || NEW_ENTRIES.length == 0) {
            Log.d("BackgroundIntentService", "processIliasXml >> No new entries");
            return;
        }

        createNotification(NEW_ENTRIES);
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
