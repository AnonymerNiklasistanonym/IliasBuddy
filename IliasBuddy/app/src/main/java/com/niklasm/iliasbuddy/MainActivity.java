package com.niklasm.iliasbuddy;

import android.app.SearchManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.android.volley.AuthFailureError;
import com.android.volley.VolleyError;
import com.example.niklasm.iliasbuddy.R;
import com.niklasm.iliasbuddy.background_service.BackgroundIntentService;
import com.niklasm.iliasbuddy.background_service.BackgroundServiceManager;
import com.niklasm.iliasbuddy.handler.IliasBuddyBroadcastHandler;
import com.niklasm.iliasbuddy.handler.IliasBuddyCacheHandler;
import com.niklasm.iliasbuddy.handler.IliasBuddyMiscellaneousHandler;
import com.niklasm.iliasbuddy.handler.IliasBuddyPreferenceHandler;
import com.niklasm.iliasbuddy.handler.IliasBuddyUpdateHandler;
import com.niklasm.iliasbuddy.miscellancellous.SnackBarDialog;
import com.niklasm.iliasbuddy.notification_handler.IliasBuddyNotificationHandler;
import com.niklasm.iliasbuddy.private_rss_feed_api.IPrivateIliasFeedApiClient;
import com.niklasm.iliasbuddy.private_rss_feed_api.PrivateIliasFeedApi;
import com.niklasm.iliasbuddy.private_rss_feed_api.feed_entry.IliasRssEntry;
import com.niklasm.iliasbuddy.recycler_view.IliasRssItemDecoration;
import com.niklasm.iliasbuddy.recycler_view.IliasRssItemListAdapter;
import com.niklasm.iliasbuddy.recycler_view.IliasRssItemListAdapterInterface;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity implements
        SwipeRefreshLayout.OnRefreshListener, NavigationView.OnNavigationItemSelectedListener,
        IPrivateIliasFeedApiClient, IliasRssItemListAdapterInterface {

    public static final String ERROR_MESSAGE_WEB_TITLE = "ERROR_MESSAGE_WEB_TITLE";
    public static final String ERROR_MESSAGE_WEB_MESSAGE = "ERROR_MESSAGE_WEB_MESSAGE";
    private static String lastResponse;
    private BroadcastReceiver broadcastReceiver;
    private LocalBroadcastManager broadcastManager;
    private RecyclerView rssEntryRecyclerView;
    private IliasRssItemListAdapter mAdapter;
    private Snackbar newEntriesMessage;
    private PrivateIliasFeedApi iliasRssXmlWebRequester;
    private long latestRssEntryTime;
    private SwipeRefreshLayout rssEntryRecyclerViewSwipeToRefreshLayout;
    private List<IliasRssEntry> items;
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

        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        final ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        final NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

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
        rssEntryRecyclerViewSwipeToRefreshLayout
                .post(() -> checkForRssUpdates(true));

        // setup floating action button action which checks for updates on click
        findViewById(R.id.fab).setOnClickListener(view -> checkForRssUpdates(true));

        // setup broadcast receiver
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(final Context CONTEXT, @NonNull final Intent INTENT) {
                Log.d("MainActivity", "BroadcastReceiver::onReceive > Intent: "
                        + INTENT.getAction());

                // check if action is not null
                if (INTENT.getAction() == null) {
                    Log.e("MainActivity",
                            "BroadcastReceiver::onReceive > Intent.getAction() == null!");
                    return;
                }


                switch (INTENT.getAction()) {
                    case IliasBuddyBroadcastHandler.NEW_ENTRIES_FOUND:
                        /*
                        Background service just found new entries and pushed a notification -
                        thus create snack bar message that refreshes feed on action click
                        */
                        newEntriesMessage = SnackBarDialog.displayActionSnackBar(
                                findViewById(R.id.fab),
                                INTENT.getStringExtra(IliasBuddyBroadcastHandler.NEW_ENTRIES_FOUND_PREVIEW),
                                getString(R.string.main_activity_floating_button_tooltip_refresh),
                                view -> checkForRssUpdates(true));
                        break;
                    case IliasBuddyBroadcastHandler.UPDATE_SILENT:
                        // notification clicked that said update feed silently
                        checkForRssUpdates(false);
                        break;
                    case IliasBuddyBroadcastHandler.ENABLE_SHORTCUT_CAMPUS:
                        menu.findItem(R.id.campus_icon).setVisible(
                                IliasBuddyPreferenceHandler.getEnableCampusShortcut(
                                        CONTEXT, false));
                        break;
                    case IliasBuddyBroadcastHandler.ENABLE_SHORTCUT_DEV:
                        menu.findItem(R.id.developer_options).setVisible(
                                IliasBuddyPreferenceHandler.getEnableDeveloperOptionsShortcut(
                                        CONTEXT, false));
                        break;
                    case IliasBuddyBroadcastHandler.ENABLE_SHORTCUT_SETUP:
                        menu.findItem(R.id.setup).setVisible(
                                IliasBuddyPreferenceHandler.getEnableAccountShortcut(
                                        CONTEXT, false));
                        break;
                    default:
                        Log.e("MainActivity", "Intent action not found!");
                }
            }
        };

        // setup broadcast manager in registering the intents he can get
        broadcastManager = LocalBroadcastManager.getInstance(this);
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(IliasBuddyBroadcastHandler.NEW_ENTRIES_FOUND);
        intentFilter.addAction(IliasBuddyBroadcastHandler.UPDATE_SILENT);
        intentFilter.addAction(IliasBuddyBroadcastHandler.ENABLE_SHORTCUT_CAMPUS);
        intentFilter.addAction(IliasBuddyBroadcastHandler.ENABLE_SHORTCUT_DEV);
        intentFilter.addAction(IliasBuddyBroadcastHandler.ENABLE_SHORTCUT_SETUP);
        broadcastManager.registerReceiver(broadcastReceiver, intentFilter);

        // setup web request manager
        iliasRssXmlWebRequester = new PrivateIliasFeedApi(this);

        /*
          Load data & Co
         */

        try {
            // If possible load and render cached entries
            renderNewList(IliasBuddyCacheHandler.getCache(this));
        } catch (final IOException | ClassNotFoundException e) {
            // If not possible load and render an empty list plus clear cache (create empty cache)
            e.printStackTrace();
            renderNewList(new IliasRssEntry[0]);
            try {
                IliasBuddyCacheHandler.clearCache(this);
            } catch (final IOException e2) {
                e2.printStackTrace();
            }
        }
        // set latest entry time to currently latest item
        updateLatestRssEntryToNewestEntry();

        // Start background service if there is not already one running and if settings say so
        if (!BackgroundServiceManager.isAlarmManagerCurrentlyActivated() &&
                IliasBuddyPreferenceHandler
                        .getBackgroundNotificationsEnabled(this, true)) {
            BackgroundServiceManager.startBackgroundService(this);
        }

        // check for a new version silently (only if update is there show notification)
        if (IliasBuddyPreferenceHandler.getEnableAutoCheckForUpdates(this, true)) {
            IliasBuddyUpdateHandler.checkForUpdate(this, true);
        }
    }

    public void menuDevOptionExampleNotification(final MenuItem menuItem) {
        IliasBuddyNotificationHandler.showNotificationNewEntries(this, "titleString",
                "titleStringBig (single demo)",
                "previewString (single demo)", new String[]{"one"},
                new Intent(this, MainActivity.class)
                        .setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), new IliasRssEntry[0],
                "https://ilias3.uni-stuttgart.de");
    }

    public void menuDevOptionExampleNotifications(final MenuItem item) {
        IliasBuddyNotificationHandler.showNotificationNewEntries(this, "titleString",
                "not important",
                "previewString (multiple demo)",
                new String[]{"one", "two", "three"},
                new Intent(this, MainActivity.class)
                        .setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), new IliasRssEntry[0],
                "https://ilias3.uni-stuttgart.de");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        broadcastManager.unregisterReceiver(broadcastReceiver);
    }

    /**
     * Update the current latest time to the currently time of the latest element
     */
    public void updateLatestRssEntryToNewestEntry() {
        latestRssEntryTime = (items != null && items.size() >= 1) && items.get(0) != null ?
                items.get(0).DATE.getTime() : 0;
        mAdapter.notifyDataSetChanged();
    }

    public void updateLatestRssEntryToHighlightNothing() {
        latestRssEntryTime = -1;
        mAdapter.notifyDataSetChanged();
    }

    public void menuOpenAboutActivity(final MenuItem menuItem) {
        startActivity(new Intent(this, AboutActivity.class));
    }

    public void openSettings(final MenuItem menuItem) {
        startActivity(new Intent(this, SettingsActivity.class));
    }

    public void noNewEntryFound() {
        SnackBarDialog.displayNormalSnackBar(findViewById(R.id.fab),
                getString(R.string.dialog_snack_bar_no_new_entry_found), false);
    }

    @Override
    public void onConfigurationChanged(final Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    /**
     * Check if there are any new RSS entries + start necessary visual animation
     *
     * @param HIGHLIGHT_NEW_ENTRIES If true new entries will be highlighted
     */
    private void checkForRssUpdates(final boolean HIGHLIGHT_NEW_ENTRIES) {
        Log.d("MainActivity", "checkForRssUpdates");
        // dismiss new entries notification
        IliasBuddyNotificationHandler.hideNotificationNewEntries(this);
        // dismiss new entries snack bar
        if (newEntriesMessage != null && newEntriesMessage.isShownOrQueued()) {
            newEntriesMessage.dismiss();
        }

        if (HIGHLIGHT_NEW_ENTRIES) {
            updateLatestRssEntryToNewestEntry();
        } else {
            updateLatestRssEntryToHighlightNothing();
        }

        // activate refresh animation of SwipeToRefreshLayout
        rssEntryRecyclerViewSwipeToRefreshLayout.setRefreshing(true);
        // check Ilias RSS feed
        iliasRssXmlWebRequester.getCurrentPrivateIliasFeed();
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
            IliasBuddyPreferenceHandler.setLastNotificationText(this, "");
            IliasBuddyPreferenceHandler.setLatestItemToString(this, "");
            // set latestRssEntryTime to null
            latestRssEntryTime = 0;
            // render empty list to override the current one
            renderNewList(new IliasRssEntry[0]);
            // clear the last server response
            MainActivity.lastResponse = null;
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Convenience method for visual debugging
     *
     * @param NEW_CACHE_DATA New cache data
     */
    private void setCache(final IliasRssEntry[] NEW_CACHE_DATA) {
        try {
            IliasBuddyCacheHandler.setCache(this, NEW_CACHE_DATA);
        } catch (final IOException e) {
            e.printStackTrace();
            SnackBarDialog.displayErrorSnackBar(this,
                    findViewById(R.id.fab), getString(R.string.dialog_error_cache), e.toString());
        }
    }

    public void renderNewList(final IliasRssEntry[] NEW_ENTRIES) {

        // save latest element of new data set in preferences
        IliasBuddyPreferenceHandler.setLatestItemToString(this,
                NEW_ENTRIES.length > 0 ? NEW_ENTRIES[0].toString() : "");

        // set new cache
        setCache(NEW_ENTRIES);

        // update adapter entries
        items.clear();
        items.addAll(Arrays.asList(NEW_ENTRIES));
        mAdapter.notifyDataSetChanged();

        // filter new rendered list to filter settings (posts/file changes)
        mAdapter.filter();
    }

    @Override
    protected void onNewIntent(final Intent intent) {
        super.onNewIntent(intent);
        Log.d("MainActivity", "onNewIntent");

        if (intent == null) {
            return;
        }

        // check if a new entry was found
        if (intent.getBooleanExtra(IliasBuddyNotificationHandler.NEW_ENTRY_FOUND, false)) {
            Log.d("MainActivity", "onNewIntent: NEW_ENTRY_FOUND");
            // update Ilias RSS feed
            checkForRssUpdates(true);
            // if there is NEW_ENTRY_DATA extra perform a virtual click on the only new element
            final Parcelable[] NEW_ENTRIES_EXTRA_Parcelable =
                    intent.getParcelableArrayExtra(IliasBuddyNotificationHandler.NEW_ENTRY_DATA);
            // if the entries are not null add them to the cache
            if (NEW_ENTRIES_EXTRA_Parcelable == null) {
                Log.e("MainActivity", "Parcelable was null");
            }
            // convert Parcelable[] to IliasRssFeedItem[]
            final IliasRssEntry[] NEW_ENTRIES_EXTRA =
                    IliasRssEntry.readParcelableArray(NEW_ENTRIES_EXTRA_Parcelable);
            // if the new entries have only a length of one show alert dialog to this element
            if (NEW_ENTRIES_EXTRA != null && NEW_ENTRIES_EXTRA.length == 1) {
                IliasRssItemListAdapter.alertDialogRssFeedEntry(NEW_ENTRIES_EXTRA[0], this);
            }
        }
    }

    public void menuDevOptionShowLastResponse(final MenuItem menuItem) {
        // show selectable popup with last response
        IliasBuddyMiscellaneousHandler.makeAlertDialogTextSelectable(
                new AlertDialog.Builder(this)
                        .setTitle(R.string.main_activity_show_last_response_title)
                        .setMessage((MainActivity.lastResponse != null) ?
                                MainActivity.lastResponse :
                                getString(R.string.main_activity_show_last_response_no_response))
                        .setCancelable(true)
                        .setNeutralButton(getString(R.string.dialog_back),
                                (dialog1, id) -> dialog1.cancel())
                        .show());
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        this.menu = menu;
        // load filter settings
        menu.findItem(R.id.filter_files).setChecked(IliasBuddyPreferenceHandler
                .getFilterFileChanges(this, true));
        menu.findItem(R.id.filter_posts).setChecked(IliasBuddyPreferenceHandler
                .getFilterPosts(this, true));
        // disable/enable shortcuts if the setting says so
        menu.findItem(R.id.campus_icon).setVisible(IliasBuddyPreferenceHandler
                .getEnableCampusShortcut(this, false));
        menu.findItem(R.id.setup).setVisible(IliasBuddyPreferenceHandler
                .getEnableAccountShortcut(this, true));
        menu.findItem(R.id.developer_options).setVisible(IliasBuddyPreferenceHandler
                .getEnableDeveloperOptionsShortcut(this, false));
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
    public long listAdapterGetLatestEntryTime() {
        return latestRssEntryTime;
    }

    @Override
    public int listAdapterGetRecyclerViewChildLayoutPosition(final View view) {
        return rssEntryRecyclerView.getChildLayoutPosition(view);
    }

    @Override
    public void onRefresh() {
        // check for feed updates
        checkForRssUpdates(true);
    }

    public void menuDevOptionSetFirstLaunch(final MenuItem item) {
        IliasBuddyPreferenceHandler.setFirstTimeLaunch(this, true);
        startActivity(new Intent(this, WelcomeActivity.class));
    }

    public void menuDevOptionCleanFirstElement(final MenuItem item) {
        try {
            // load IliasRssFeedItem[] from cache
            final IliasRssEntry[] CURRENT_ILIAS_ENTRIES =
                    IliasBuddyCacheHandler.getCache(this);
            // if it's not null and IliasRssFeedItem[] has at least one element remove the first
            if (CURRENT_ILIAS_ENTRIES.length >= 1) {
                final IliasRssEntry[] CURRENT_ILIAS_ENTRIES_2 = Arrays.copyOfRange(
                        CURRENT_ILIAS_ENTRIES, 1, CURRENT_ILIAS_ENTRIES.length);
                IliasBuddyCacheHandler.setCache(this, CURRENT_ILIAS_ENTRIES_2);
                // clean things in the future
                latestRssEntryTime = CURRENT_ILIAS_ENTRIES_2[0].DATE.getTime();
                // clear latest item and latest notification from preferences
                IliasBuddyPreferenceHandler.setLatestItemToString(this,
                        CURRENT_ILIAS_ENTRIES_2[0].toString());
                // render empty list to override the current one
                renderNewList(CURRENT_ILIAS_ENTRIES_2);
            }
        } catch (final IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void filterFileChanges(final MenuItem item) {
        mAdapter.showFileChanges(!item.isChecked());
        IliasBuddyPreferenceHandler.setFilterFileChanges(this, !item.isChecked());
        item.setChecked(!item.isChecked());

    }

    public void filterPosts(final MenuItem item) {
        mAdapter.showPosts(!item.isChecked());
        IliasBuddyPreferenceHandler.setFilterPosts(this, !item.isChecked());
        item.setChecked(!item.isChecked());
    }

    public void menuShare(final MenuItem item) {
        IliasBuddyMiscellaneousHandler.shareRepositoryReleaseUrl(this);
    }

    public void menuDevOptionForceStartBackgroundService(final MenuItem item) {
        startService(new Intent(this, BackgroundIntentService.class));
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull final MenuItem item) {
        // Handle navigation view item clicks here.
        final int id = item.getItemId();

        /*
        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }*/

        final DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onFeedResponse(@NonNull final IliasRssEntry[] iliasRssEntries) {
        // get the latest RSS entry from the main activity
        // only continue if the latest object is different
        if ((items == null || items.size() == 0) ||
                (iliasRssEntries.length == 0 || !items.get(0).toString().equals(iliasRssEntries[0].toString()))) {
            renderNewList(iliasRssEntries);
        } else {
            noNewEntryFound();
        }
        // at last stop refresh animation of swipe to refresh layout
        rssEntryRecyclerViewSwipeToRefreshLayout.setRefreshing(false);
    }

    @Override
    public void onAuthenticationError(@NonNull final AuthFailureError authenticationError) {
        Log.e("MainActivity - AuthErr", authenticationError.toString());
        rssEntryRecyclerViewSwipeToRefreshLayout.setRefreshing(false);
        openSetupActivity(R.string.dialog_error_authentication, authenticationError.toString());
    }

    @Override
    public void onResponseError(@NonNull final VolleyError responseError) {
        Log.e("MainActivity - RespErr", responseError.toString());
        rssEntryRecyclerViewSwipeToRefreshLayout.setRefreshing(false);
        openSetupActivity(R.string.dialog_error_web_response, responseError.toString());
    }

    @Override
    public void onFeedParseError(@NonNull final Exception feedParseError) {
        Log.e("MainActivity - ParseErr", feedParseError.toString());
        rssEntryRecyclerViewSwipeToRefreshLayout.setRefreshing(false);
        openSetupActivity(R.string.dialog_error_parse, feedParseError.toString());
    }
}
