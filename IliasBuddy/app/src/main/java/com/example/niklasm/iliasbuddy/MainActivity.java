package com.example.niklasm.iliasbuddy;

import android.app.SearchManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.VolleyError;
import com.example.niklasm.iliasbuddy.background_service.BackgroundIntentService;
import com.example.niklasm.iliasbuddy.background_service.BackgroundServiceManager;
import com.example.niklasm.iliasbuddy.handler.IliasBuddyCacheHandler;
import com.example.niklasm.iliasbuddy.handler.IliasBuddyMiscellaneousHandler;
import com.example.niklasm.iliasbuddy.handler.IliasBuddyUpdateHandler;
import com.example.niklasm.iliasbuddy.notification_handler.IliasBuddyNotificationHandler;
import com.example.niklasm.iliasbuddy.objects.IliasRssFeedItem;
import com.example.niklasm.iliasbuddy.rss_handler.IliasRssItemDecoration;
import com.example.niklasm.iliasbuddy.rss_handler.IliasRssItemListAdapter;
import com.example.niklasm.iliasbuddy.rss_handler.IliasRssItemListAdapterInterface;
import com.example.niklasm.iliasbuddy.rss_handler.IliasRssXmlParser;
import com.example.niklasm.iliasbuddy.rss_handler.IliasRssXmlWebRequester;
import com.example.niklasm.iliasbuddy.rss_handler.IliasRssXmlWebRequesterInterface;

import org.xmlpull.v1.XmlPullParserException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class MainActivity extends AppCompatActivity implements
        SwipeRefreshLayout.OnRefreshListener, IliasRssXmlWebRequesterInterface,
        IliasRssItemListAdapterInterface {

    public static final String ERROR_MESSAGE_WEB_TITLE = "ERROR_MESSAGE_WEB_TITLE";
    public static final String ERROR_MESSAGE_WEB_MESSAGE = "ERROR_MESSAGE_WEB_MESSAGE";
    private static String lastResponse;
    private BroadcastReceiver broadcastReceiver;
    private LocalBroadcastManager broadcastManager;
    private RecyclerView rssEntryRecyclerView;
    private IliasRssItemListAdapter mAdapter;
    private Snackbar newEntriesMessage;
    private IliasRssXmlWebRequester iliasRssXmlWebRequester;
    private IliasRssFeedItem latestRssEntry;
    private IliasRssFeedItem latestRssEntryNewIliasRssFeedEntries;
    private int currentDataSetLength;
    private SwipeRefreshLayout rssEntryRecyclerViewSwipeToRefreshLayout;
    private List<IliasRssFeedItem> items;
    private Menu menu;

    /**
     * Save latest response of Ilias RSS feed website
     *
     * @param RESPONSE (String) - Current website data as a String
     */
    public static void devOptionSetLastResponse(final String RESPONSE) {
        MainActivity.lastResponse = RESPONSE;
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setSupportActionBar(findViewById(R.id.toolbar));

        /*
          SETUP things
         */

        // setup adapter
        items = new ArrayList<>();
        mAdapter = new IliasRssItemListAdapter(items, this, this);

        // setup the recycler view
        rssEntryRecyclerView = findViewById(R.id.my_recycler_view);
        rssEntryRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        rssEntryRecyclerView.setItemAnimator(new DefaultItemAnimator());
        rssEntryRecyclerView.addItemDecoration(new IliasRssItemDecoration(this));
        rssEntryRecyclerView.setAdapter(mAdapter);

        // setup the swipe to refresh layout
        rssEntryRecyclerViewSwipeToRefreshLayout = findViewById(R.id.swipe_container);
        rssEntryRecyclerViewSwipeToRefreshLayout.setOnRefreshListener(this);
        rssEntryRecyclerViewSwipeToRefreshLayout.setColorSchemeResources(R.color.colorPrimary,
                android.R.color.holo_orange_light, android.R.color.holo_green_light,
                android.R.color.holo_blue_light, android.R.color.holo_red_light);
        // refresh on onCreate does not work (Animation, etc.) thus we use the post runnable
        rssEntryRecyclerViewSwipeToRefreshLayout.post(this::checkForRssUpdates);

        // setup floating action button action
        findViewById(R.id.fab).setOnClickListener(view -> {
            updateLatestRssEntryToNewestEntry();
            checkForRssUpdates();
        });

        // setup broadcast receiver
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(final Context CONTEXT, @NonNull final Intent INTENT) {
                Log.i("MainActivity", "BroadcastReceiver::onReceive > Intent: "
                        + INTENT.getAction());

                if (INTENT.getAction() != null) {
                    if (INTENT.getAction().equals(IliasBuddyNotificationHandler.FOUND_A_NEW_ENTRY)) {
                        final String PREVIEW_STRING = INTENT.getStringExtra(BackgroundIntentService
                                .NOTIFICATION_INTENT_EXTRA_PREVIEW_STRING);

                        // create snack bar message that refreshes feed on action click
                        Snackbar.make(findViewById(R.id.fab), PREVIEW_STRING, Snackbar.LENGTH_INDEFINITE)
                                .setAction(R.string.main_activity_floating_button_tooltip_refresh,
                                        view -> checkForRssUpdates())
                                .show();
                    } else if (INTENT.getAction().equals(IliasBuddyNotificationHandler.UPDATE_SILENT)) {
                        // notification clicked that said update silently
                        try {
                            renderNewList(IliasBuddyCacheHandler.getCache(CONTEXT));
                        } catch (IOException | ClassNotFoundException e) {
                            e.printStackTrace();
                        }

                    }
                }
            }
        };

        // setup broadcast manager
        broadcastManager = LocalBroadcastManager.getInstance(this);
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(IliasBuddyNotificationHandler.FOUND_A_NEW_ENTRY);
        broadcastManager.registerReceiver(broadcastReceiver, intentFilter);

        // setup web request manager
        iliasRssXmlWebRequester = new IliasRssXmlWebRequester(this);

        /*
          Load data & Co
         */

        // try to load saved RSS feed
        IliasRssFeedItem[] myDataSet = null;
        try {
            myDataSet = IliasBuddyCacheHandler.getCache(this);
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        if (myDataSet == null) {
            // if data set is still null create clean list
            try {
                IliasBuddyCacheHandler.clearCache(this);
                currentDataSetLength = 0;
                // clean latest RSS entries
                latestRssEntryNewIliasRssFeedEntries = null;
                latestRssEntry = null;
            } catch (final IOException e) {
                e.printStackTrace();
            }
        } else if (myDataSet.length > 0) {
            // if there is data save latest object
            latestRssEntryNewIliasRssFeedEntries = myDataSet[0];
            latestRssEntry = myDataSet[0];
            // specify an adapter (see also next example)
            items.clear();
            items.addAll(Arrays.asList(myDataSet));
            mAdapter.notifyDataSetChanged();

            rssEntryRecyclerView.setAdapter(mAdapter);
            currentDataSetLength = myDataSet.length;
        } else {
            latestRssEntry = null;
            latestRssEntryNewIliasRssFeedEntries = null;
            currentDataSetLength = 0;
        }
        if (mAdapter == null) {
            items.clear();
            mAdapter.notifyDataSetChanged();
        }

        // Start background service if there is not already one running and only if settings say so
        if (!BackgroundServiceManager.isAlarmManagerCurrentlyActivated() &&
                android.preference.PreferenceManager.getDefaultSharedPreferences(this)
                        .getBoolean("activate_background_notifications", true)) {
            BackgroundServiceManager.startBackgroundService(this);
        }

        // check on create if there was a RSS feed update
        checkForRssUpdates();

        // check for a new version silently
        IliasBuddyUpdateHandler.checkForUpdate(this, true);
    }

    public void menuDevOptionExampleNotification(final MenuItem menuItem) {
        IliasBuddyNotificationHandler.showNotificationNewEntries(this, "titleString",
                "previewString (single demo)", new String[]{"one"},
                new Intent(this, MainActivity.class)
                        .setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
                "https://ilias3.uni-stuttgart.de");
    }

    public void menuDevOptionExampleNotifications(final MenuItem item) {
        IliasBuddyNotificationHandler.showNotificationNewEntries(this, "titleString",
                "previewString (multiple demo)",
                new String[]{"one", "two", "three"},
                new Intent(this, MainActivity.class)
                        .setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
                "https://ilias3.uni-stuttgart.de");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        broadcastManager.unregisterReceiver(broadcastReceiver);
    }

    public void updateLatestRssEntryToNewestEntry() {
        latestRssEntry = latestRssEntryNewIliasRssFeedEntries;
        if (mAdapter != null) {
            mAdapter.notifyItemRangeChanged(0, currentDataSetLength);
        }
    }

    public void menuOpenAboutActivity(final MenuItem menuItem) {
        startActivity(new Intent(this, AboutActivity.class));
    }

    @Override
    public void processIliasXml(final String FEED_XML_DATA) {
        // save latest response in variable
        MainActivity.devOptionSetLastResponse(FEED_XML_DATA);
        // parse String Ilias RSS feed to IliasRssFeedItem[] array
        final IliasRssFeedItem[] myDataSet;
        try {
            myDataSet = IliasRssXmlParser.parse(new ByteArrayInputStream(FEED_XML_DATA
                    .replace("<rss version=\"2.0\">", "")
                    .replace("</rss>", "")
                    .getBytes(StandardCharsets.UTF_8)));
        } catch (XmlPullParserException | IOException | ParseException e) {
            SetupActivity.errorSnackBar(this, findViewById(R.id.fab),
                    getString(R.string.dialog_parse_error), e.toString());
            e.printStackTrace();
            // at last stop refresh animation of swipe to refresh layout
            rssEntryRecyclerViewSwipeToRefreshLayout.setRefreshing(false);
            return;
        }
        // get the latest RSS entry from the main activity
        final IliasRssFeedItem latestRssEntry = getLatestRssEntry();
        // only continue if the latest object is different
        if (latestRssEntry == null || !latestRssEntry.toString().equals(myDataSet[0].toString())) {
            renderNewList(myDataSet);
        } else {
            noNewEntryFound();
        }
        // at last stop refresh animation of swipe to refresh layout
        rssEntryRecyclerViewSwipeToRefreshLayout.setRefreshing(false);
    }

    @Override
    public void webAuthenticationError(final AuthFailureError error) {
        Log.e("MainActivity - AuthErr", error.toString());
        rssEntryRecyclerViewSwipeToRefreshLayout.setRefreshing(false);
        openSetupActivity(R.string.dialog_authentication_error, error.toString());
    }

    @Override
    public void webResponseError(final VolleyError error) {
        Log.e("MainActivity - RespErr", error.toString());
        openSetupActivity(R.string.dialog_response_error, error.toString());
    }

    public void openSettings(final MenuItem menuItem) {
        startActivity(new Intent(this, SettingsActivity.class));
    }

    public IliasRssFeedItem getLatestRssEntry() {
        return latestRssEntry;
    }

    public void noNewEntryFound() {
        Snackbar.make(findViewById(R.id.fab), R.string.dialog_no_new_entry_found,
                Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public void onConfigurationChanged(final Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    /**
     * Check if there are any new RSS entries + start necessary visual animation
     */
    private void checkForRssUpdates() {
        // dismiss new entries notification
        IliasBuddyNotificationHandler.hideNotificationNewEntries(this);
        // dismiss new entries snack bar
        if (newEntriesMessage != null && newEntriesMessage.isShownOrQueued()) {
            newEntriesMessage.dismiss();
        }
        // activate refresh animation of SwipeToRefreshLayout
        rssEntryRecyclerViewSwipeToRefreshLayout.setRefreshing(true);
        // check Ilias RSS feed
        iliasRssXmlWebRequester.getWebContent();
    }

    public void menuOpenSetupActivity(final MenuItem menuItem) {
        openSetupActivity();
    }

    /**
     * Open activity 'Setup' in which the user can enter the RSS feed URL and his credentials
     */
    private void openSetupActivity() {
        openSetupActivity(-1, null);
    }

    /**
     * Open activity 'Setup' in which the user can enter the RSS feed URL and his credentials
     */
    private void openSetupActivity(final int errorTitle, final String errorMessage) {
        final Intent intent = new Intent(this, SetupActivity.class);
        if (errorTitle != -1 && errorMessage != null) {
            intent.putExtra(MainActivity.ERROR_MESSAGE_WEB_TITLE, getString(errorTitle))
                    .putExtra(MainActivity.ERROR_MESSAGE_WEB_MESSAGE, errorMessage);
        }
        startActivity(intent);
    }

    public void menuDevOptionCleanList(final MenuItem menuItem) {
        devOptionCleanRssEntryList();
    }

    /**
     * Clear current saved RSS feed
     */
    public void devOptionCleanRssEntryList() {
        try {
            // clear cache by saving empty Feed
            IliasBuddyCacheHandler.clearCache(this);
            // clear latest item and latest notification from preferences
            PreferenceManager.getDefaultSharedPreferences(this).edit()
                    .putString(BackgroundIntentService.LATEST_ELEMENT, "")
                    .putString(BackgroundIntentService.LAST_NOTIFICATION_TEXT, "____").apply();
            // set latestRssEntry to null
            latestRssEntry = null;
            // render empty list to override the current one
            renderNewList(new IliasRssFeedItem[0]);
            currentDataSetLength = 0;
            // clear the last server response
            MainActivity.lastResponse = null;
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    public void renderNewList(final IliasRssFeedItem[] NEW_ILIAS_RSS_FEED_ENTRIES) {
        // save latest element of new data set in preferences
        PreferenceManager.getDefaultSharedPreferences(this).edit()
                .putString(BackgroundIntentService.LATEST_ELEMENT,
                        NEW_ILIAS_RSS_FEED_ENTRIES.length > 0 ?
                                NEW_ILIAS_RSS_FEED_ENTRIES[0].toString() : "").apply();
        // set new cache
        try {
            IliasBuddyCacheHandler.setCache(this, NEW_ILIAS_RSS_FEED_ENTRIES);
        } catch (final IOException e1) {
            e1.printStackTrace();
        }
        // specify a new adapter with the new data set
        items.clear();
        items.addAll(Arrays.asList(NEW_ILIAS_RSS_FEED_ENTRIES));
        mAdapter.notifyDataSetChanged();

        currentDataSetLength = NEW_ILIAS_RSS_FEED_ENTRIES.length;
        latestRssEntryNewIliasRssFeedEntries = currentDataSetLength > 0 ?
                NEW_ILIAS_RSS_FEED_ENTRIES[0] : null;
    }

    @Override
    protected void onNewIntent(final Intent intent) {
        super.onNewIntent(intent);
        // check if new elements were found
        if (intent.getBooleanExtra(
                IliasBuddyNotificationHandler.NEW_ENTRY_FOUND, false)) {
            Log.i("MainActivity", "onNewIntent: NEW_ENTRY_FOUND");
            // update Ilias RSS feed
            checkForRssUpdates();
            // if there is NEW_ENTRY_DATA extra perform a virtual click on the only new element
            if (intent.getParcelableExtra(
                    IliasBuddyNotificationHandler.NEW_ENTRY_DATA) != null) {
                Log.i("MainActivity", "onNewIntent: NEW_ENTRY_DATA");
                IliasRssItemListAdapter.alertDialogRssFeedEntry(
                        intent.getParcelableExtra(IliasBuddyNotificationHandler.NEW_ENTRY_DATA),
                        this);
            }
        }
    }

    public void menuDevOptionShowLastResponse(final MenuItem menuItem) {
        // show popup with last response
        final AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(R.string.main_activity_show_last_response_title)
                .setMessage((MainActivity.lastResponse != null) ?
                        MainActivity.lastResponse : getString(R.string.main_activity_show_last_response_no_response))
                .setCancelable(true)
                .setNeutralButton(getString(R.string.dialog_back), (dialog1, id) -> dialog1.cancel())
                .show();
        // make alert dialog text (response) selectable
        final TextView textView = Objects.requireNonNull(dialog.getWindow()).getDecorView()
                .findViewById(android.R.id.message);
        textView.setTextIsSelectable(true);
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        this.menu = menu;
        // TODO change
        mAdapter.quickFixFilter(menu.findItem(R.id.filter_files).isChecked(), menu.findItem(R.id.filter_posts).isChecked());
        filterChangeCallback(true, true);
        // disable campus icon if the setting says so
        menu.findItem(R.id.campus_icon).setVisible(PreferenceManager
                .getDefaultSharedPreferences(this)
                .getBoolean("enable_campus_shortcut", false));
        // enable Search view
        final SearchManager searchManager =
                (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        final SearchView searchView = (SearchView) menu.findItem(R.id.search)
                .getActionView();
        if (searchManager != null) {
            searchView.setSearchableInfo(searchManager
                    .getSearchableInfo(getComponentName()));
            searchView.setMaxWidth(Integer.MAX_VALUE);

            // listening to search query text change
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(final String query) {
                    // filterChangeCallback recycler view when query submitted
                    mAdapter.getFilter().filter(query);
                    return false;
                }

                @Override
                public boolean onQueryTextChange(final String query) {
                    // filterChangeCallback recycler view when text is changed
                    mAdapter.getFilter().filter(query);
                    return false;
                }
            });
        }
        return true;
    }


    public void openCampus(final MenuItem item) {
        IliasBuddyMiscellaneousHandler.openUrl(this,
                "https://campus.uni-stuttgart.de/cusonline/webnav.ini");
    }

    public void openIlias(final MenuItem item) {
        IliasBuddyMiscellaneousHandler.openUrl(this,
                "https://ilias3.uni-stuttgart.de/login.php?client_id=Uni_Stuttgart&lang=de");
    }

    @Override
    public IliasRssFeedItem listAdapterGetLatestEntry() {
        return latestRssEntry;
    }

    @Override
    public int listAdapterGetRecyclerViewChildLayoutPosition(final View view) {
        return rssEntryRecyclerView.getChildLayoutPosition(view);
    }

    @Override
    public void filterChangeCallback(final boolean filterPosts, final boolean filterFiles) {
        menu.findItem(R.id.filter_files).setChecked(filterFiles);
        menu.findItem(R.id.filter_posts).setChecked(filterPosts);
    }

    @Override
    public void onRefresh() {
        // remove all colors
        updateLatestRssEntryToNewestEntry();
        // check for feed updates
        checkForRssUpdates();
    }

    public void menuDevOptionSetFirstLaunch(final MenuItem item) {
        WelcomeActivity.setFirstTimeLaunch(this, true);
        startActivity(new Intent(this, WelcomeActivity.class));
    }

    public void menuDevOptionCleanFirstElement(final MenuItem item) {
        try {
            // load IliasRssFeedItem[] from cache
            final IliasRssFeedItem[] CURRENT_ILIAS_ENTRIES = IliasBuddyCacheHandler.getCache(this);
            // if it's not null and IliasRssFeedItem[] has at least one element remove the first
            if (CURRENT_ILIAS_ENTRIES.length >= 1) {
                final IliasRssFeedItem[] CURRENT_ILIAS_ENTRIES_2 = Arrays.copyOfRange(
                        CURRENT_ILIAS_ENTRIES, 1, CURRENT_ILIAS_ENTRIES.length);
                IliasBuddyCacheHandler.setCache(this, CURRENT_ILIAS_ENTRIES_2);
                // clean things in the future
                currentDataSetLength--;
                latestRssEntry = CURRENT_ILIAS_ENTRIES_2[0];
                // clear latest item and latest notification from preferences
                PreferenceManager.getDefaultSharedPreferences(this).edit()
                        .putString(BackgroundIntentService.LATEST_ELEMENT,
                                CURRENT_ILIAS_ENTRIES_2[0].toString())
                        .putString(BackgroundIntentService.LAST_NOTIFICATION_TEXT, "____").apply();

                // render empty list to override the current one
                renderNewList(CURRENT_ILIAS_ENTRIES_2);
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void filterFileChanges(final MenuItem item) {
        mAdapter.filterFiles(!item.isChecked());
    }

    public void filterPosts(final MenuItem item) {
        mAdapter.filterPosts(!item.isChecked());
    }

    public void menuShare(final MenuItem item) {
        IliasBuddyMiscellaneousHandler.shareLink(this,
                getString(R.string.app_name),
                "IliasBuddy\nhttps://github.com/AnonymerNiklasistanonym/IliasBuddy/releases",
                getString(R.string.main_activity_toolbar_options_action_share));
    }
}
