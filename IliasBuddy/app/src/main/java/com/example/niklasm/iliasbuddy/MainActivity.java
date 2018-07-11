package com.example.niklasm.iliasbuddy;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
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

public class MainActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener, IliasRssXmlWebRequesterInterface, IliasRssItemListAdapterInterface {

    // test - https://stackoverflow.com/a/12997537/7827128
    final public static String RECEIVE_JSON = "FOUND_A_NEW_ENTRY";
    final public static String NEW_ENTRY_FOUND = "NEW_ENTRY_FOUND";
    final public static int NEW_ENTRY_FOUND_NOTIFICATION_ID = 42424242;
    public final static String STOP_BACKGROUND_SERVICE = "STOP_BACKGROUND_SERVICE";
    private LocalBroadcastManager bManager;
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private Snackbar newEntriesMessage;
    private IliasRssXmlWebRequester webRequester;
    private IliasRssItem latestRssEntry;
    private IliasRssItem latestRssEntry2;
    private String lastResponse = null;
    private int dataSetLength = 0;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private final BroadcastReceiver bReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, @NonNull final Intent intent) {
            Log.i("MainActivity", "Broadcasts intents get in here - " + intent.getAction());

            // test - https://stackoverflow.com/a/12997537/7827128
            if (intent.getAction() != null && intent.getAction().equals(MainActivity.RECEIVE_JSON)) {
                final String PREVIEW_STRING = intent.getStringExtra(BackgroundIntentService.NOTIFICATION_INTENT_EXTRA_PREVIEW_STRING);
                final int MESSAGE_COUNT = intent.getIntExtra(BackgroundIntentService.NOTIFICATION_INTENT_MESSAGE_COUNT, 0);
                final String BIG_STRING = intent.getStringExtra(BackgroundIntentService.NOTIFICATION_INTENT_EXTRA_BIG_STRING);
                Log.d("MainActivity", "BroadcastReceiver > FOUND_A_NEW_ENTRY >> BIG_STRING: " + BIG_STRING);
                Log.d("MainActivity", "BroadcastReceiver > FOUND_A_NEW_ENTRY >> MESSAGE_COUNT: " + MESSAGE_COUNT);
                Log.d("MainActivity", "BroadcastReceiver > FOUND_A_NEW_ENTRY >> PREVIEW_STRING: " + PREVIEW_STRING);
                newEntriesMessage = Snackbar.make(findViewById(R.id.fab), PREVIEW_STRING, Snackbar.LENGTH_INDEFINITE)
                        .setAction(R.string.word_refresh, view -> {
                            // pull newest changes
                            checkForRssUpdates();
                        });
                newEntriesMessage.show();
            }
        }
    };
    private IliasRssCache rssDataSaver;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        // Make sure this is before calling super.onCreate
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setSupportActionBar(findViewById(R.id.toolbar));

        mRecyclerView = findViewById(R.id.my_recycler_view);
        mRecyclerView.setHasFixedSize(false);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());

        mSwipeRefreshLayout = findViewById(R.id.swipe_container);
        mSwipeRefreshLayout.setOnRefreshListener(this);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary,
                android.R.color.holo_orange_light,
                android.R.color.holo_green_light,
                android.R.color.holo_blue_light,
                android.R.color.holo_red_light);
        /*
          Showing Swipe Refresh animation on activity create
          As animation won't start on onCreate, post runnable is used
         */
        mSwipeRefreshLayout.post(this::checkForRssUpdates);

        rssDataSaver = new IliasRssCache(this, "TestFile.test");

        findViewById(R.id.fab).setOnClickListener(view -> {
            removeColoring();
            checkForRssUpdates();
        });

        // try to load saved RSS feed
        IliasRssItem[] myDataSet;
        try {
            myDataSet = rssDataSaver.getCache();
        } catch (IliasRssCache.IliasRssCacheException | IOException | ClassNotFoundException e) {
            e.printStackTrace();
            myDataSet = null;
        }
        if (myDataSet == null) {
            try {
                rssDataSaver.setCache(new IliasRssItem[0]);
            } catch (IliasRssCache.IliasRssCacheException | IOException e) {
                e.printStackTrace();
            }
            latestRssEntry2 = null;
            latestRssEntry = null;
            // specify an adapter (see also next example)
            dataSetLength = 0;

        } else if (myDataSet.length > 0) {
            // save latest object
            latestRssEntry2 = myDataSet[0];
            latestRssEntry = myDataSet[0];
            // specify an adapter (see also next example)
            mAdapter = new IliasRssItemListAdapter(Arrays.asList(myDataSet), this, this);
            mRecyclerView.setAdapter(mAdapter);
            dataSetLength = myDataSet.length;

        } else {
            latestRssEntry = null;
            latestRssEntry2 = null;
        }

        if (mAdapter == null) {
            mAdapter = new IliasRssItemListAdapter(Arrays.asList(new IliasRssItem[0]), this, this);
        }

        webRequester = new IliasRssXmlWebRequester(this);
        checkForRssUpdates();

        // test - https://stackoverflow.com/a/12997537/7827128
        bManager = LocalBroadcastManager.getInstance(this);
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(MainActivity.RECEIVE_JSON);
        bManager.registerReceiver(bReceiver, intentFilter);
        // test - https://stackoverflow.com/a/12997537/7827128

        startBackgroundService();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        bManager.unregisterReceiver(bReceiver);
    }

    public void removeColoring() {
        latestRssEntry = latestRssEntry2;
        if (mAdapter != null) {
            mAdapter.notifyItemRangeChanged(0, dataSetLength);
        }
    }

    public void openAbout(final MenuItem menuItem) {
        startActivity(new Intent(this, AboutActivity.class));
    }

    @Override
    public void processIliasXml(final String FEED_XML_DATA) {
        setLastResponse(FEED_XML_DATA);
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
        refreshIcon(false);
    }

    @Override
    public void webAuthenticationError(final AuthFailureError error) {
        Log.i("MainActivity - AuthErr", error.toString());
        refreshIcon(false);
        Toast.makeText(this, "Authentication error", Toast.LENGTH_SHORT).show();
        openSetup(null);
    }

    @Override
    public void webResponseError(final VolleyError error) {
        Log.i("MainActivity - RespErr", error.toString());
        errorSnackBar("Response Error", error.toString());
        openSetup(null);
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
                            (dialog, which) -> openSetup())
                    .setNeutralButton("OK",
                            (dialog, which) -> dialog.dismiss())
                    .create();
            alertDialog.show();
        });
        snackbar.show();
    }

    public void checkForRssUpdates() {
        // dismiss notification
        BackgroundServiceNewEntriesNotification.hide(this);
        // dismiss snackbar
        if (newEntriesMessage != null && newEntriesMessage.isShownOrQueued()) {
            newEntriesMessage.dismiss();
        }
        // refresh list
        mSwipeRefreshLayout.setRefreshing(true);
        webRequester.getWebContent();
    }

    public void refreshIcon(final boolean state) {
        mSwipeRefreshLayout.setRefreshing(state);
    }

    public void setLastResponse(final String response) {
        lastResponse = response;
    }

    public void openSetup(final MenuItem menuItem) {
        openSetup();
    }

    public void openSetup() {
        startActivity(new Intent(this, SetupActivity.class));
    }

    public void cleanList(final MenuItem menuItem) {
        try {
            rssDataSaver.setCache(new IliasRssItem[0]);
        } catch (IliasRssCache.IliasRssCacheException | IOException e) {
            e.printStackTrace();
        }
        final SharedPreferences myPrefs = getSharedPreferences("myPrefs", Context.MODE_PRIVATE);
        final SharedPreferences.Editor e = myPrefs.edit();
        e.putString(getString(R.string.latestItem), "");
        e.putString(getString(R.string.lastNotification), "");
        e.apply();
        latestRssEntry = null;
        renderNewList(new IliasRssItem[0]);
        // mAdapter.notifyAll();
        // mAdapter.notifyItemRangeRemoved(0, mAdapter.getItemCount());
        dataSetLength = 0;
    }

    public void renderNewList(final IliasRssItem[] newDataSet) {

        final SharedPreferences myPrefs = getSharedPreferences("myPrefs", Context.MODE_PRIVATE);
        final SharedPreferences.Editor e = myPrefs.edit();
        e.putString(getString(R.string.latestItem), newDataSet.length > 0 ? newDataSet[0].toString() : "");
        e.apply();

        try {
            rssDataSaver.setCache(newDataSet);
        } catch (final IOException e1) {
            e1.printStackTrace();
        }

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);

        // specify an adapter (see also next example)

        mAdapter.notifyDataSetChanged();
        mAdapter = new IliasRssItemListAdapter(Arrays.asList(newDataSet), this, this);
        mRecyclerView.setAdapter(mAdapter);

        dataSetLength = newDataSet.length;

        // this.latestRssEntry = newDataSet.length > 0 ? newDataSet[0] : null;
        latestRssEntry2 = newDataSet.length > 0 ? newDataSet[0] : null;
    }

    @Override
    protected void onNewIntent(final Intent intent) {
        super.onNewIntent(intent);
        // gets called if an intent to this Activity was executed
        Log.i("MainActivity", "onNewIntent WOWOWOWOWOWOWOWOWOWOWOWOWOWOWOWOWOWOWOWOW");

        // xd never gets called ever ... I am so bad

        // check if new elements were found
        if (intent.getBooleanExtra(MainActivity.NEW_ENTRY_FOUND, false)) {
            Log.i("MainActivity", "onNewIntent " + getString(R.string.render_new_elements));
            checkForRssUpdates();
        }

        if (intent.getBooleanExtra(MainActivity.STOP_BACKGROUND_SERVICE, true)) {
            stopBackgroundService();
        }
    }

    public void restartService(final MenuItem menuItem) {
        stopBackgroundService();
        startBackgroundService();
    }

    private void startBackgroundService() {
        Log.d("MainActivity", "startBackgroundService()");
        BackgroundServiceManager.startBackgroundService(getApplicationContext());
    }

    private void stopBackgroundService() {
        Log.d("MainActivity", "stopBackgroundService()");
        BackgroundServiceManager.stopBackgroundService(getApplicationContext());
    }

    public void stopService(final MenuItem menuItem) {
        Log.d("MainActivity", "stopService(MenuItem)");

        // stop the background service
        stopBackgroundService();
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
        return true;
    }

    public void openCampus(final MenuItem item) {
        openUrl("https://campus.uni-stuttgart.de/cusonline/webnav.ini");
    }

    public void openIlias(final MenuItem item) {
        openUrl("https://ilias3.uni-stuttgart.de/login.php?client_id=Uni_Stuttgart&lang=de");
    }


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
        return mRecyclerView.getChildLayoutPosition(view);
    }

    @Override
    public void onRefresh() {
        removeColoring();
        checkForRssUpdates();
    }

    public void sendIntent(final MenuItem item) {
        final Intent myIntent = new Intent(MainActivity.this, MainActivity.class);
        myIntent.putExtra("some_key", "String data");
        startActivity(myIntent);
    }
}
