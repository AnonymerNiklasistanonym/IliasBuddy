package com.example.niklasm.iliasbuddy;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.VolleyError;
import com.example.niklasm.iliasbuddy.ilias_rss_handler.IliasRssCache;
import com.example.niklasm.iliasbuddy.ilias_rss_handler.IliasRssItem;
import com.example.niklasm.iliasbuddy.ilias_rss_handler.IliasRssXmlParser;
import com.example.niklasm.iliasbuddy.ilias_rss_handler.IliasRssXmlWebRequester;
import com.example.niklasm.iliasbuddy.ilias_rss_handler.IliasRssXmlWebRequesterInterface;

import org.xmlpull.v1.XmlPullParserException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class MainActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener, IliasRssXmlWebRequesterInterface {

    // test - https://stackoverflow.com/a/12997537/7827128
    final public static String RECEIVE_JSON = "FOUND_A_NEW_ENTRY";
    final public static String NEW_ENTRY_FOUND = "NEW_ENTRY_FOUND";
    final public static int NEW_ENTRY_FOUND_NOTIFICATION_ID = 42424242;
    final private static int FIXED_NOTIFICATION_ID = 1234;
    final private static String STOP_BACKGROUND_SERVICE = "STOP_BACKGROUND_SERVICE";
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
            // test - https://stackoverflow.com/a/12997537/7827128
            if (intent.getAction() != null && intent.getAction().equals(MainActivity.RECEIVE_JSON)) {
                final String previewString = intent.getStringExtra("previewString");
                newEntriesMessage = Snackbar.make(findViewById(R.id.fab), previewString, Snackbar.LENGTH_INDEFINITE)
                        .setAction("REFRESH", view -> checkForRssUpdates());
                newEntriesMessage.show();
            }
        }
    };
    private IliasRssCache rssDataSaver;
    private AlarmManager am;
    private PendingIntent pendingIntent;

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
            mAdapter = new MyAdapter(Arrays.asList(myDataSet));
            mRecyclerView.setAdapter(mAdapter);
            dataSetLength = myDataSet.length;

        } else {
            latestRssEntry = null;
            latestRssEntry2 = null;
        }

        if (mAdapter == null) {
            mAdapter = new MyAdapter(Arrays.asList(new IliasRssItem[0]));
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
        errorSnackbar("Response Error", error.toString());
        openSetup(null);
    }

    public void openSettings(final MenuItem menuItem) {
        startActivity(new Intent(this, SettingsActivity.class));
    }

    public IliasRssItem getLatestRssEntry() {
        return latestRssEntry;
    }

    public void noNewEntryFound() {
        Log.i("MainActivity", "noNewEntryFound (Snackbar)");
        Snackbar.make(findViewById(R.id.fab), "No new entry found", Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public void onConfigurationChanged(final Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // setContentView(R.layout.myLayout);
        Log.i("MainActivity", "Configuration changed - " + newConfig.toString());
    }

    public void errorSnackbar(final String title, final String message) {
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
        if (newEntriesMessage != null && newEntriesMessage.isShownOrQueued()) {
            newEntriesMessage.dismiss();
        }
        final NotificationManager mNotificationManager;
        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (mNotificationManager != null) {
            mNotificationManager.cancel(MainActivity.NEW_ENTRY_FOUND_NOTIFICATION_ID);
        }
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
        mAdapter = new MyAdapter(Arrays.asList(newDataSet));
        mRecyclerView.setAdapter(mAdapter);

        dataSetLength = newDataSet.length;

        // this.latestRssEntry = newDataSet.length > 0 ? newDataSet[0] : null;
        latestRssEntry2 = newDataSet.length > 0 ? newDataSet[0] : null;
    }

    @Override
    protected void onNewIntent(final Intent intent) {
        super.onNewIntent(intent);
        // gets called if an intent to this Activity was executed
        Log.i("MainActivity", "onNewIntent");

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

        // Create a new Intent that calls the BackgroundIntentService class
        final Intent intent = new Intent(getApplicationContext(), BackgroundIntentService.class);
        // and add it to a pending Intent
        pendingIntent = PendingIntent.getService(getApplicationContext(), 12345, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        // and add this Intent to the alarm manager
        am = (AlarmManager) getSystemService(Activity.ALARM_SERVICE);
        // call the pending intent every ... minutes
        final int minutes = 5;
        am.setRepeating(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime(), 1000 * 60 * minutes, pendingIntent);

        // also make a sticky notification so that the user knows the background service is running
        makeStickyNotification();
    }

    private void stopBackgroundService() {
        Log.d("MainActivity", "stopBackgroundService()");

        // cancel the alarm
        Objects.requireNonNull(am).cancel(pendingIntent);

        // also remove the sticky notification so that the user knows the background service is not running
        removeStickyNotification();
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

    public void openUrl(final String url) {
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
    }

    @Override
    public void onRefresh() {
        removeColoring();
        checkForRssUpdates();
    }

    private void makeStickyNotification() {
        Log.d("MainActivity", "makeStickyNotification()");

        // create PendingIntent for opening the app on click
        final Intent openAppIntent = new Intent(this, MainActivity.class);
        final PendingIntent openAppPendingIntent = PendingIntent.getActivity(this,
                0, openAppIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        // create PendingIntent for stopping the background service
        final Intent stopServiceIntent = new Intent(this, MainActivity.class)
                .setAction(MainActivity.STOP_BACKGROUND_SERVICE);
        final PendingIntent stopServicePendingIntent = PendingIntent.getService(this, 0,
                stopServiceIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        // setup oreo notification channel
        final NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        final String CHANNEL_ID = "GG WP";
        // https://stackoverflow.com/a/47974065
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            Log.i("IMPORTANT", "OREO DETECTED");
            final CharSequence name = "main_activity_channel";
            final String Description = "Oreo notification channel";
            final int importance = NotificationManager.IMPORTANCE_HIGH;
            final NotificationChannel mChannel = new NotificationChannel(CHANNEL_ID, name, importance);
            mChannel.setDescription(Description);
            mChannel.enableLights(true);
            mChannel.setLightColor(Color.RED);
            mChannel.enableVibration(true);
            mChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
            mChannel.setShowBadge(false);
            assert notificationManager != null;
            notificationManager.createNotificationChannel(mChannel);
        }
        // build sticky notification
        final NotificationCompat.Action action = new NotificationCompat.Action.Builder(R.drawable.ic_close, "Stop background service", stopServicePendingIntent).build();

        final Notification stickyNotification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("IliasBuddy - Running in the background")
                .setContentText("Click to open the app or expand to stop the background service")
                .setContentIntent(openAppPendingIntent)
                .addAction(action)
                .setSmallIcon(R.drawable.ic_ilias_logo_notification)
                .setPriority(Notification.PRIORITY_MIN)
                .setColor(ContextCompat.getColor(this, R.color.colorPrimary))
                .setAutoCancel(false) // on click the notification does not disappear
                .setOngoing(true) // make it not clear-able
                .build();
        // show the notification
        NotificationManagerCompat.from(this).notify(MainActivity.FIXED_NOTIFICATION_ID, stickyNotification);
    }

    private void removeStickyNotification() {
        Log.d("MainActivity", "removeStickyNotification()");

        final NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.cancel(MainActivity.FIXED_NOTIFICATION_ID);
    }


    public class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {

        private final List<IliasRssItem> items;
        private final SimpleDateFormat viewDateFormat = new SimpleDateFormat("dd.MM", getResources().getConfiguration().locale);
        private final SimpleDateFormat viewTimeFormat = new SimpleDateFormat("HH:mm", getResources().getConfiguration().locale);

        // Allows to remember the last item shown on screen
        private int lastPosition = -1;

        MyAdapter(final List<IliasRssItem> dataSet) {
            items = dataSet;
        }


        @Override
        @NonNull
        public MyAdapter.ViewHolder onCreateViewHolder(@NonNull final ViewGroup parent, final int viewType) {
            // Create new views (invoked by the layout manager)
            final View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.recycler_view_new, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
            // Replace the contents of a view (invoked by the layout manager)
            // - get element from the data set at this position
            // - replace the contents of the view with that element
            final IliasRssItem entry = items.get(position);
            final String description = entry.getDescription();

            // CHECK THIS LATER
            if (latestRssEntry == null || latestRssEntry.getDate().getTime() < entry.getDate().getTime()) {
                holder.background.setBackgroundResource(R.color.colorNewEntry);
            }
            // CHECK THIS LATER

            // These views have always these values
            holder.course.setText(entry.getCourse());
            holder.date.setText(viewDateFormat.format(entry.getDate()));
            holder.time.setText(viewTimeFormat.format(entry.getDate()));
            holder.star.setVisibility(View.GONE);
            holder.title.setText(entry.getTitle());

            // if extra is null hide extra card or else set the text
            if (entry.getExtra() == null) {
                holder.extraCard.setVisibility(View.GONE);
            } else {
                holder.extra.setText(entry.getExtra());
            }

            if (entry.getTitleExtra() == null) {
                holder.titleExtraCard.setVisibility(View.GONE);
            } else {
                holder.titleExtra.setText(entry.getTitleExtra());
            }

            if (description == null || description.equals("")) {
                holder.description.setVisibility(View.GONE);
            } else {
                holder.description.setText(Html.fromHtml(description).toString().replaceAll("\\s+", " ").trim());
            }

            if ((description == null || description.equals("")) && entry.getTitleExtra() != null) {
                holder.titleExtra.setText(getResources().getString(R.string.new_file));
                holder.title.setText(entry.getTitleExtra());
                holder.titleExtraCard.setCardBackgroundColor(getResources().getColor(android.R.color.holo_red_dark));
            }

            /* if there is no description make title longer and hide it
            if (description == null || description.equals("")) {
                holder.description.setVisibility(View.GONE);
                holder.title.setLines(2);
                holder.title.setText(titleExtra);
                holder.titleExtra.setText(context.getResources().getString(R.string.new_file));
                holder.titleExtraCard.setCardBackgroundColor(ContextCompat.getColor(context, R.color.holo_red_dark));
            } else {
            }

            // if there is no title extra and description hide label
            if (titleExtra == null && !(description == null || description.equals(""))) {
                holder.titleExtraCard.setVisibility(View.GONE);
            } else {
                holder.titleExtra.setText(titleExtra);
            }*/


            /*final ImageView starView = holder.star;
            holder.star.setOnClickListener(new View.OnClickListener() {
                private boolean clicked = false;
                @Override
                public void onClick(final View view) {
                    final IliasRssItem entry = dataSet[position];
                    Log.i("MainActivity", "Star clicked " + entry.toString());
                    if (clicked) {
                        starView.setImageResource(R.drawable.ic_star);
                    } else {
                        starView.setImageResource(R.drawable.ic_star_filled);
                    }
                    clicked = !clicked;
                }
            });*/

            setAnimation(holder.itemView, position);
        }

        /**
         * Here is the key method to apply the animation
         * https://stackoverflow.com/a/26748274/7827128
         */
        private void setAnimation(final View viewToAnimate, final int position) {
            // If the bound view wasn't previously displayed on screen, it's animated
            if (position > lastPosition) {
                // R.anim.slide_up, slide_in_left
                final Animation animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fall_down);
                viewToAnimate.startAnimation(animation);
                lastPosition = position;
            }
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        /**
         * Provide a reference to the views for each data item - Complex data items may need more
         * than one view per item, and you provide access to all the views for a data item in a
         * view holder
         */
        public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            final public TextView course, title, date, time, description, extra, titleExtra;
            final public LinearLayout background;
            final public ImageView star;
            final public CardView extraCard, titleExtraCard;

            private ViewHolder(final View itemView) {
                super(itemView);
                // add on click listener to each view holder
                itemView.setOnClickListener(this);
                // set the views of the view holder
                background = itemView.findViewById(R.id.background);
                course = itemView.findViewById(R.id.course);
                date = itemView.findViewById(R.id.date);
                description = itemView.findViewById(R.id.description);
                extra = itemView.findViewById(R.id.extra);
                extraCard = itemView.findViewById(R.id.extraCard);
                star = itemView.findViewById(R.id.star);
                time = itemView.findViewById(R.id.time);
                title = itemView.findViewById(R.id.title);
                titleExtra = itemView.findViewById(R.id.titleExtra);
                titleExtraCard = itemView.findViewById(R.id.titleExtraCard);
            }

            @Override
            public void onClick(final View view) {
                final int itemPosition = mRecyclerView.getChildLayoutPosition(view);
                final IliasRssItem entry = items.get(itemPosition);

                if (entry.getDescription() == null || entry.getDescription().equals("")) {
                    // if there is no description this means it was an upload
                    // therefore instantly link to the Ilias page
                    openUrl(entry.getLink());
                } else {
                    // if not this must be a legit message for which a popup dialog will be opened
                    final String message = ">> " + entry.getTitle() + "\n\n" + Html.fromHtml(entry.getDescription());
                    final AlertDialog dialog = new AlertDialog.Builder(MainActivity.this)
                            .setTitle(entry.getCourse() + " (" + viewDateFormat.format(entry.getDate()) + ")")
                            .setMessage(message)
                            .setCancelable(true)
                            .setPositiveButton(getString(R.string.open_in_ilias), (dialog1, id) -> openUrl(entry.getLink()))
                            .setNegativeButton(getString(R.string.go_back), (dialog12, id) -> dialog12.cancel())
                            .show();
                    final TextView textView = Objects.requireNonNull(dialog.getWindow()).getDecorView().findViewById(android.R.id.message);
                    textView.setTextIsSelectable(true);
                }
            }
        }
    }

}
