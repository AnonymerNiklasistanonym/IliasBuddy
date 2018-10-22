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
import com.niklasm.iliasbuddy.handler.IliasBuddyBroadcastHandler;
import com.niklasm.iliasbuddy.handler.IliasBuddyCacheHandler;
import com.niklasm.iliasbuddy.handler.IliasBuddyMiscellaneousHandler;
import com.niklasm.iliasbuddy.handler.IliasBuddyPreferenceHandler;
import com.niklasm.iliasbuddy.notification_handler.IliasBuddyNotificationHandler;
import com.niklasm.iliasbuddy.private_rss_feed_api.IPrivateIliasFeedApiClient;
import com.niklasm.iliasbuddy.private_rss_feed_api.PrivateIliasFeedApi;
import com.niklasm.iliasbuddy.private_rss_feed_api.feed_entry.IliasRssEntry;

import java.io.IOException;
import java.util.ArrayList;

public class BackgroundIntentService extends Service implements IPrivateIliasFeedApiClient {

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
                final IliasRssEntry[] NEW_ENTRIES_EXTRA =
                        IliasRssEntry.readParcelableArray(NEW_ENTRIES_EXTRA_Parcelable);
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
        new PrivateIliasFeedApi(this).getCurrentPrivateIliasFeed();

        // return this so that the service can be restarted
        return Service.START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(final Intent arg0) {
        return null;
    }

    public void createNotification(@NonNull final IliasRssEntry[] NEW_ENTRIES) {

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
        final String NOTIFICATION_TITLE_BIG;
        if (NEW_ENTRIES.length == 1) {
            // Only one new item
            NOTIFICATION_TITLE = IliasBuddyMiscellaneousHandler
                    .notificationTitleSingle(NEW_ENTRIES[0], this);
            NOTIFICATION_PREVIEW_CONTENT = IliasBuddyMiscellaneousHandler
                    .notificationPreviewSingle(NEW_ENTRIES[0], this);
            NOTIFICATION_TEXT_ARRAY[0] = IliasBuddyMiscellaneousHandler
                    .notificationBigContentSingle(NEW_ENTRIES[0], this);
            NOTIFICATION_TITLE_BIG = IliasBuddyMiscellaneousHandler
                    .notificationBigTitleSingle(NEW_ENTRIES[0]);
        } else {
            // More than one new item
            NOTIFICATION_TITLE = NEW_ENTRIES.length + " " +
                    getString(R.string.notification_title_new_ilias_entries);
            NOTIFICATION_TITLE_BIG = "";
            NOTIFICATION_PREVIEW_CONTENT =
                    IliasBuddyMiscellaneousHandler.notificationPreviewMultiple(NEW_ENTRIES);
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
                NOTIFICATION_TITLE_BIG, NOTIFICATION_PREVIEW_CONTENT, NOTIFICATION_TEXT_ARRAY,
                ON_CLICK, NEW_ENTRIES, NEW_ENTRIES[0].LINK);

        // Additionally send a broadcast to the main activity if it currently is running
        IliasBuddyBroadcastHandler.sendBroadcastNewEntriesFound(this,
                NOTIFICATION_PREVIEW_CONTENT, NEW_ENTRIES);
    }

    private IliasRssEntry[] getNewElements(final IliasRssEntry[] NEW_DATA_SET) {

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

        final ArrayList<IliasRssEntry> newIliasRssItems = new ArrayList<>();

        // else check which elements of the new data set are new
        for (final IliasRssEntry NEW_ENTRY : NEW_DATA_SET) {
            if (NEW_ENTRY.toString().equals(CURRENT_LATEST_ENTRY)) {
                // if newest entry was found return all found entries
                return newIliasRssItems.toArray(new IliasRssEntry[0]);
            } else {
                newIliasRssItems.add(NEW_ENTRY);
            }
        }

        // if all entries where new return the complete new data set
        return NEW_DATA_SET;
    }

    @Override
    public void onFeedResponse(@NonNull final IliasRssEntry[] iliasRssEntries) {
        // Find out which entries of them are new
        final IliasRssEntry[] NEW_ENTRIES = getNewElements(iliasRssEntries);

        // Do nothing if there are no entries or zero new ones
        if (NEW_ENTRIES == null || NEW_ENTRIES.length == 0) {
            Log.d("BackgroundIntentService", "onFeedResponse >> No new entries");
            return;
        }

        createNotification(NEW_ENTRIES);
    }

    @Override
    public void onAuthenticationError(@NonNull final AuthFailureError authenticationError) {
        Log.e("BackgroundInt - AuthErr", authenticationError.toString());
    }

    @Override
    public void onResponseError(@NonNull final VolleyError responseError) {
        Log.e("BackgroundInt - RespErr", responseError.toString());
    }

    @Override
    public void onFeedParseError(@NonNull final Exception feedParseError) {
        Log.e("BackgroundInt - FeedErr", feedParseError.toString());
    }
}
