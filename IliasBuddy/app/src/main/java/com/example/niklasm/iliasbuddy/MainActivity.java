package com.example.niklasm.iliasbuddy;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.graphics.Color;
import android.net.Uri;
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
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.VolleyError;
import com.example.niklasm.iliasbuddy.background_service.BackgroundIntentService;
import com.example.niklasm.iliasbuddy.background_service.BackgroundServiceManager;
import com.example.niklasm.iliasbuddy.background_service.BackgroundServiceNewEntriesNotification;
import com.example.niklasm.iliasbuddy.ilias_rss_handler.IliasRssCache;
import com.example.niklasm.iliasbuddy.ilias_rss_handler.IliasRssItem;
import com.example.niklasm.iliasbuddy.ilias_rss_handler.IliasRssItemListAdapter;
import com.example.niklasm.iliasbuddy.ilias_rss_handler.IliasRssItemListAdapterInterface;
import com.example.niklasm.iliasbuddy.ilias_rss_handler.IliasRssXmlParser;
import com.example.niklasm.iliasbuddy.ilias_rss_handler.IliasRssXmlWebRequester;
import com.example.niklasm.iliasbuddy.ilias_rss_handler.IliasRssXmlWebRequesterInterface;

import org.xmlpull.v1.XmlPullParserException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Objects;

public class MainActivity extends AppCompatActivity implements
        SwipeRefreshLayout.OnRefreshListener, IliasRssXmlWebRequesterInterface,
        IliasRssItemListAdapterInterface {

    // test - https://stackoverflow.com/a/12997537/7827128
    final public static String RECEIVE_JSON = "FOUND_A_NEW_ENTRY";
    final public static String NEW_ENTRY_FOUND = "NEW_ENTRY_FOUND";
    public final static String STOP_BACKGROUND_SERVICE = "STOP_BACKGROUND_SERVICE";
    private LocalBroadcastManager broadcastManager;
    private RecyclerView rssEntryRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private Snackbar newEntriesMessage;
    private IliasRssXmlWebRequester iliasRssXmlWebRequester;
    private IliasRssItem latestRssEntry;
    private IliasRssItem latestRssEntryNewIliasRssFeedEntries;
    private String lastResponse = null;
    private int currentDataSetLength = 0;
    private SwipeRefreshLayout rssEntryRecyclerViewSwipeToRefreshLayout;
    private BroadcastReceiver broadcastReceiver;
    private IliasRssCache iliasRssFeedCacheManager;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setSupportActionBar(findViewById(R.id.toolbar));

        /*
          SETUP things
         */

        // setup the recycler view
        rssEntryRecyclerView = findViewById(R.id.my_recycler_view);
        rssEntryRecyclerView.setHasFixedSize(true);
        rssEntryRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        rssEntryRecyclerView.setItemAnimator(new DefaultItemAnimator());

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
            public void onReceive(final Context context, @NonNull final Intent intent) {
                Log.i("MainActivity", "BroadcastReceiver::onReceive > Intent: "
                        + intent.getAction());

                if (intent.getAction() != null &&
                        intent.getAction().equals(MainActivity.RECEIVE_JSON)) {
                    final String PREVIEW_STRING = intent.getStringExtra(BackgroundIntentService
                            .NOTIFICATION_INTENT_EXTRA_PREVIEW_STRING);
                    final int MESSAGE_COUNT = intent.getIntExtra(BackgroundIntentService
                            .NOTIFICATION_INTENT_MESSAGE_COUNT, 0);
                    final String BIG_STRING = intent.getStringExtra(BackgroundIntentService
                            .NOTIFICATION_INTENT_EXTRA_BIG_STRING);
                    Log.d("MainActivity", "BroadcastReceiver > FOUND_A_NEW_ENTRY\n" +
                            ">> BIG_STRING: " + BIG_STRING + "\n" +
                            ">> MESSAGE_COUNT: " + MESSAGE_COUNT + "\n" +
                            ">> PREVIEW_STRING: " + PREVIEW_STRING);

                    // create snack bar message that refreshes feed on action click
                    newEntriesMessage = Snackbar.make(findViewById(R.id.fab), PREVIEW_STRING,
                            Snackbar.LENGTH_INDEFINITE)
                            .setAction(R.string.word_refresh, view -> checkForRssUpdates());
                    newEntriesMessage.show();
                }
            }
        };

        // setup broadcast manager
        broadcastManager = LocalBroadcastManager.getInstance(this);
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(MainActivity.RECEIVE_JSON);
        broadcastManager.registerReceiver(broadcastReceiver, intentFilter);

        // setup cache manager
        iliasRssFeedCacheManager = new IliasRssCache(this);

        // setup web request manager
        iliasRssXmlWebRequester = new IliasRssXmlWebRequester(this);

        /*
          Load data & Co
         */

        // try to load saved RSS feed
        IliasRssItem[] myDataSet = null;
        try {
            myDataSet = iliasRssFeedCacheManager.getCache();
        } catch (IliasRssCache.IliasRssCacheException | IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }

        if (myDataSet == null) {
            try {
                iliasRssFeedCacheManager.setCache(new IliasRssItem[0]);
            } catch (IliasRssCache.IliasRssCacheException | IOException e) {
                e.printStackTrace();
            }
            latestRssEntryNewIliasRssFeedEntries = null;
            latestRssEntry = null;
            // specify an adapter (see also next example)
            currentDataSetLength = 0;

        } else if (myDataSet.length > 0) {
            // save latest object
            latestRssEntryNewIliasRssFeedEntries = myDataSet[0];
            latestRssEntry = myDataSet[0];
            // specify an adapter (see also next example)
            mAdapter = new IliasRssItemListAdapter(Arrays.asList(myDataSet), this,
                    this);
            rssEntryRecyclerView.setAdapter(mAdapter);
            currentDataSetLength = myDataSet.length;

        } else {
            latestRssEntry = null;
            latestRssEntryNewIliasRssFeedEntries = null;
        }

        if (mAdapter == null) {
            mAdapter = new IliasRssItemListAdapter(Arrays.asList(new IliasRssItem[0]),
                    this, this);
        }


        // Start background service if there is not already one running and only if settings say so
        if (!BackgroundServiceManager.isAlarmManagerCurrentlyActivated() &&
                android.preference.PreferenceManager.getDefaultSharedPreferences(this)
                        .getBoolean("activate_background_notifications", true)) {
            BackgroundServiceManager.startBackgroundService(this);
        }

        // check on create if there was a RSS feed update
        checkForRssUpdates();
    }

    public void menuDevOptionExampleNotification(final MenuItem menuItem) {
        BackgroundServiceNewEntriesNotification.show(this, "titleString",
                "previewString", "bigString",
                new String[]{"one", "two", "three"},
                new Intent(this, MainActivity.class),
                42, "https://ilias3.uni-stuttgart.de");
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
        devOptionSetLastResponse(FEED_XML_DATA);
        final InputStream stream = new ByteArrayInputStream(FEED_XML_DATA.replace("<rss version=\"2.0\">", "").replace("</rss>", "").getBytes(StandardCharsets.UTF_8));
        final IliasRssItem[] myDataSet;
        try {
            myDataSet = IliasRssXmlParser.parse(stream);
        } catch (XmlPullParserException | IOException | ParseException e) {
            e.printStackTrace();
            return;
        }

        // get the latest RSS entry from the main activity
        final IliasRssItem latestRssEntry = getLatestRssEntry();

        // only continue if the latest object is different
        final boolean newEntryFound = latestRssEntry == null || !latestRssEntry.toString().equals(myDataSet[0].toString());

        if (newEntryFound) {
            Log.i("MainActivity", "New entry found");
            renderNewList(myDataSet);
        } else {
            Log.i("MainActivity", "NO new entry found");
            noNewEntryFound();
        }
        rssEntryRecyclerViewSwipeToRefreshLayout.setRefreshing(false);
    }

    @Override
    public void webAuthenticationError(final AuthFailureError error) {
        Log.i("MainActivity - AuthErr", error.toString());
        rssEntryRecyclerViewSwipeToRefreshLayout.setRefreshing(false);
        Toast.makeText(this, "Authentication error", Toast.LENGTH_SHORT).show();
        menuOpenSetupActivity(null);
    }

    @Override
    public void webResponseError(final VolleyError error) {
        Log.i("MainActivity - RespErr", error.toString());
        errorSnackBar("Response Error", error.toString());
        menuOpenSetupActivity(null);
    }

    public void openSettings(final MenuItem menuItem) {
        startActivity(new Intent(this, SettingsActivity.class));
    }

    public IliasRssItem getLatestRssEntry() {
        return latestRssEntry;
    }

    public void noNewEntryFound() {
        Log.i("MainActivity", "noNewEntryFound (SnackBar)");
        Snackbar.make(findViewById(R.id.fab), "No new entry found", Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public void onConfigurationChanged(final Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // setContentView(R.layout.myLayout);
        Log.i("MainActivity", "Configuration changed - " + newConfig.toString());
    }

    public void errorSnackBar(final String title, final String message) {
        final Snackbar snackbar = Snackbar.make(findViewById(R.id.fab), title, Snackbar.LENGTH_LONG);
        snackbar.setActionTextColor(Color.RED); //to change the color of action text
        snackbar.setAction("MORE", view -> {
            final AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this)
                    .setTitle(title)
                    .setMessage(message)
                    .setNeutralButton("SETTINGS",
                            (dialog, which) -> openSetupActivity())
                    .setNeutralButton("OK",
                            (dialog, which) -> dialog.dismiss())
                    .create();
            alertDialog.show();
        });
        snackbar.show();
    }

    /**
     * Check if there are any new RSS entries + start necessary visual animation
     */
    private void checkForRssUpdates() {
        // dismiss new entries notification
        BackgroundServiceNewEntriesNotification.hide(this);
        // dismiss new entries snack bar
        if (newEntriesMessage != null && newEntriesMessage.isShownOrQueued()) {
            newEntriesMessage.dismiss();
        }
        // activate refresh animation of SwipeToRefreshLayout
        rssEntryRecyclerViewSwipeToRefreshLayout.setRefreshing(true);
        // check Ilias RSS feed
        iliasRssXmlWebRequester.getWebContent();
    }

    /**
     * Save latest response of Ilias RSS feed website
     *
     * @param RESPONSE (String) - Current website data as a String
     */
    public void devOptionSetLastResponse(final String RESPONSE) {
        lastResponse = RESPONSE;
    }

    public void menuOpenSetupActivity(final MenuItem menuItem) {
        openSetupActivity();
    }

    /**
     * Open activity 'Setup' in which the user can enter the RSS feed URL and his credentials
     */
    private void openSetupActivity() {
        startActivity(new Intent(this, SetupActivity.class));
    }

    public void menuDevOptionCleanList(final MenuItem menuItem) {
        devOptionCleanRssEntryList();
    }

    /**
     * Clear current saved RSS feed
     */
    public void devOptionCleanRssEntryList() {
        // clear cache by saving empty Feed
        try {
            iliasRssFeedCacheManager.setCache(new IliasRssItem[0]);
        } catch (IliasRssCache.IliasRssCacheException | IOException e) {
            e.printStackTrace();
        }
        // clear latest item and latest notification from preferences
        PreferenceManager.getDefaultSharedPreferences(this).edit()
                .putString(getString(R.string.latestItem), "")
                .putString(getString(R.string.lastNotification), "")
                .apply();
        // set latestRssEntry to null
        latestRssEntry = null;
        // render empty list to override the current one
        renderNewList(new IliasRssItem[0]);
        currentDataSetLength = 0;
    }

    public void renderNewList(final IliasRssItem[] NEW_ILIAS_RSS_FEED_ENTRIES) {
        // save latest element of new data set in preferences
        PreferenceManager.getDefaultSharedPreferences(this).edit()
                .putString(getString(R.string.latestItem),
                        NEW_ILIAS_RSS_FEED_ENTRIES.length > 0 ?
                                NEW_ILIAS_RSS_FEED_ENTRIES[0].toString() : "").apply();
        // set new cache
        try {
            iliasRssFeedCacheManager.setCache(NEW_ILIAS_RSS_FEED_ENTRIES);
        } catch (final IOException e1) {
            e1.printStackTrace();
        }
        // specify a new adapter with the new data set
        mAdapter = new IliasRssItemListAdapter(Arrays.asList(NEW_ILIAS_RSS_FEED_ENTRIES),
                this, this);
        rssEntryRecyclerView.setAdapter(mAdapter);
        currentDataSetLength = NEW_ILIAS_RSS_FEED_ENTRIES.length;
        latestRssEntryNewIliasRssFeedEntries = currentDataSetLength > 0 ?
                NEW_ILIAS_RSS_FEED_ENTRIES[0] : null;
    }

    @Override
    protected void onNewIntent(final Intent intent) {
        super.onNewIntent(intent);
        // gets called if an intent to this Activity was executed
        Log.i("MainActivity", "why the hell does this not catch any intent...");

        // check if new elements were found
        if (intent.getBooleanExtra(MainActivity.NEW_ENTRY_FOUND, false)) {
            Log.i("MainActivity", "onNewIntent " + getString(R.string.render_new_elements));
            checkForRssUpdates();
        }

        if (intent.getBooleanExtra(MainActivity.STOP_BACKGROUND_SERVICE, true)) {
            BackgroundServiceManager.stopBackgroundService(this);
        }
    }

    public void showLastResponse(final MenuItem menuItem) {
        Log.d("MainActivity", "showLastResponse(MenuItem)");

        // show popup with last response
        final AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Last response")
                .setMessage((lastResponse != null) ? lastResponse : "NO RESPONSE UNTIL NOW")
                .setCancelable(true)
                .setNeutralButton(getString(R.string.go_back), (dialog1, id) -> dialog1.cancel())
                .show();
        final TextView textView = Objects.requireNonNull(dialog.getWindow()).getDecorView().findViewById(android.R.id.message);
        textView.setTextIsSelectable(true);
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        // disable campus icon if the setting says so
        menu.findItem(R.id.campus_icon).setVisible(PreferenceManager.getDefaultSharedPreferences(this)
                .getBoolean("enable_campus_shortcut", true));
        return true;
    }

    public void openCampus(final MenuItem item) {
        openUrl("https://campus.uni-stuttgart.de/cusonline/webnav.ini");
    }

    public void openIlias(final MenuItem item) {
        openUrl("https://ilias3.uni-stuttgart.de/login.php?client_id=Uni_Stuttgart&lang=de");
    }

    /**
     * Open a website in the device browser
     *
     * @param URL (String) - Website link/address
     */
    private void openUrl(final String URL) {
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(URL)));
    }

    @Override
    public IliasRssItem listAdapterGetLatestEntry() {
        return latestRssEntry;
    }

    @Override
    public void listAdapterOpenUrl(final String URL) {
        openUrl(URL);
    }

    @Override
    public int listAdapterGetRecyclerViewChildLayoutPosition(final View view) {
        return rssEntryRecyclerView.getChildLayoutPosition(view);
    }

    @Override
    public void onRefresh() {
        // remove all colors
        updateLatestRssEntryToNewestEntry();
        // check for feed updates
        checkForRssUpdates();
    }

    public void menuDevOptionSendIntent(final MenuItem item) {
        startActivity(new Intent(this, MainActivity.class)
                .putExtra("some_key", "String data"));
    }

    public void menuDevOptionSetFirstLaunch(final MenuItem item) {
        WelcomeActivity.setFirstTimeLaunch(this, true);
    }
}
