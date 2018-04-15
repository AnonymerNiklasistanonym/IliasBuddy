package com.example.niklasm.iliasbuddy;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
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

import com.google.android.gms.oss.licenses.OssLicensesMenuActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    private IliasRssItem latestRssEntry = null;
    private String lastResponse = null;

    private IliasRssHandler rssHandler;
    private IliasRssDataSaver rssDataSaver;

    private boolean deleteAgain = false;

    private AlarmManager am;
    private PendingIntent pendingIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Make sure this is before calling super.onCreate
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mRecyclerView = findViewById(R.id.my_recycler_view);
        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        SharedPreferences myPrefs = getSharedPreferences("myPrefs", MODE_PRIVATE);
        String iliasRssUrl = myPrefs.getString(getString(R.string.ilias_url), "nothing_found");
        String iliasRssUserName = myPrefs.getString(getString(R.string.ilias_user_name), "nothing_found");
        String iliasRssPassword = myPrefs.getString(getString(R.string.ilias_password), "nothing_found");

        if (iliasRssUrl.equals("nothing_found") || (iliasRssUserName.equals("nothing_found") || iliasRssPassword.equals("nothing_found"))) {
            Intent intent = new Intent(this, SetupActivity.class);
            startActivity(intent);
        }

        rssHandler = new IliasRssHandler(this, iliasRssUrl, iliasRssUserName, iliasRssPassword);
        rssDataSaver = new IliasRssDataSaver(this, "TestFile.test");

        findViewById(R.id.fab).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkForRssUpdates();
            }
        });


        // try to load saved RSS feed
        final IliasRssItem[] myDataset = rssDataSaver.readRssFeed();
        if (myDataset != null && myDataset.length > 0) {
            // save latest object
            latestRssEntry = myDataset[0];
            // specify an adapter (see also next example)
            mAdapter = new MyAdapter(myDataset, this);
            mRecyclerView.setAdapter(mAdapter);
        } else {
            latestRssEntry = null;
        }

        // load newest changes
        checkForRssUpdates();
        startService();
    }

    public void openSettings(MenuItem menuItem) {
        startActivity(new Intent(this, SettingsActivity.class));

    }

    public IliasRssItem getLatestRssEntry() {
        return this.latestRssEntry;
    }

    public void noNewEntryFound() {
        Log.i("MainActivity", "noNewEntryFound (Snackbar)");
        Snackbar.make(findViewById(R.id.fab), "No new entry found", Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // setContentView(R.layout.myLayout);
        Log.i("MainActivity", "Configuration changed - " + newConfig.toString());
    }

    public void displayLicenses(MenuItem menuItem) {
        OssLicensesMenuActivity.setActivityTitle(getString(R.string.custom_license_title));
        startActivity(new Intent(this, OssLicensesMenuActivity.class));
    }

    public void errorSnackbar(final String title, final String message) {
        Snackbar snackbar = Snackbar.make(findViewById(R.id.fab), title, Snackbar.LENGTH_LONG);
        snackbar.setActionTextColor(Color.RED); //to change the color of action text
        snackbar.setAction("MORE", new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this)
                        .setTitle(title)
                        .setMessage(message)
                        .setNeutralButton("OK",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                })
                        .create();
                alertDialog.show();
            }
        });
        snackbar.show();


    }

    public void checkForRssUpdates() {
        rssHandler.getWebContent();
    }

    public void setLastResponse(final String response) {
        this.lastResponse = response;
    }

    public void openSetup(MenuItem menuItem) {
        startActivity(new Intent(this, SetupActivity.class));
    }

    public void cleanList(MenuItem menuItem) {
        this.rssDataSaver.writeRssFeed(new IliasRssItem[0]);
        this.rssHandler.reset();
        SharedPreferences myPrefs = getSharedPreferences("myPrefs", MODE_PRIVATE);
        final SharedPreferences.Editor e = myPrefs.edit();
        e.putString(getString(R.string.latestItem), "");
        e.apply();
        this.latestRssEntry = null;
        renderNewList(new IliasRssItem[0]);
    }

    public void renderNewList(IliasRssItem[] newDataSet) {

        SharedPreferences myPrefs = getSharedPreferences("myPrefs", MODE_PRIVATE);
        final SharedPreferences.Editor e = myPrefs.edit();
        e.putString(getString(R.string.latestItem), newDataSet.length > 0 ? newDataSet[0].toString() : "");
        e.apply();

        this.rssDataSaver.writeRssFeed(newDataSet);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);

        // specify an adapter (see also next example)
        mAdapter = new MyAdapter(newDataSet, this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mAdapter);

        this.latestRssEntry = newDataSet.length > 0 ? newDataSet[0] : null;
    }

    public void startService() {
        Log.i("MainActivity", "startService");
        //Create a new PendingIntent and add it to the AlarmManager
        Intent intent = new Intent(getApplicationContext(), BackgroundIntentService.class);
        pendingIntent = PendingIntent.getService(getApplicationContext(), 12345, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        am = (AlarmManager) getSystemService(Activity.ALARM_SERVICE);
        if (am != null) { // check every 5 minutes
            am.setRepeating(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime(), 1000 * 60 * 5, pendingIntent);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.i("MainActivity", "onNewIntent");
        if (intent.getStringExtra(getString(R.string.render_new_elements)).equals(true)) {
            Log.i("MainActivity", "onNewIntent " + getString(R.string.render_new_elements));
            checkForRssUpdates();
        }
    }

    public void restartService(MenuItem menuItem) {
        startService();
    }

    public void stopService(MenuItem menuItem) {
        Log.i("MainActivity", "stopService");
        am.cancel(pendingIntent);
    }

    public void showLastResponse(MenuItem menuItem) {
        AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this)
                .setTitle("Last response")
                .setMessage((lastResponse != null) ? lastResponse : "NO RESPONSE UNTIL NOW")
                .setNeutralButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .create();
        alertDialog.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    public void openCampus(MenuItem item) {
        openUrl("https://campus.uni-stuttgart.de/cusonline/webnav.ini");
    }

    public void openIlias(MenuItem item) {
        openUrl("https://ilias3.uni-stuttgart.de/login.php?client_id=Uni_Stuttgart&lang=de");
    }

    public void openUrl(final String url) {
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
    }

    public class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {

        private final IliasRssItem[] dataSet;
        private final SimpleDateFormat viewDateFormat = new SimpleDateFormat("dd.MM", getResources().getConfiguration().locale);
        private final SimpleDateFormat viewTimeFormat = new SimpleDateFormat("HH:mm", getResources().getConfiguration().locale);

        private Context context;

        // The items to display in your RecyclerView
        private ArrayList<String> items;
        // Allows to remember the last item shown on screen
        private int lastPosition = -1;

        private MyAdapter(IliasRssItem[] dataSet, Context context) {
            this.dataSet = dataSet;
            this.context = context;
        }

        @Override
        @NonNull
        public MyAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            // Create new views (invoked by the layout manager)
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.recycler_view_new, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
            // Replace the contents of a view (invoked by the layout manager)
            // - get element from the data set at this position
            // - replace the contents of the view with that element
            final IliasRssItem entry = dataSet[position];
            Log.i("MainActivity", "latestRssEntry: " + latestRssEntry);
            if (latestRssEntry == null || latestRssEntry.getDate().getTime() < entry.getDate().getTime()) {
                Log.i("COLOR BACKGROUND", "IT HAPPENED????" + latestRssEntry.getDate().getTime() + ", " + entry.getDate().getTime());
                holder.background.setBackgroundResource(R.color.colorNewEntry);
                Toast.makeText(MainActivity.this, "WOW - there is a new post: " + entry.toString(), Toast.LENGTH_LONG).show();
            }
            if (entry.getDescription() == null) {
                holder.description.setVisibility(View.GONE);
                holder.title.setLines(2);
            } else {
                holder.description.setText(Html.fromHtml(entry.getDescription()).toString().replaceAll("\\s+", " "));
                }
            holder.course.setText(entry.getCourse());
            holder.title.setText(entry.getTitle());
            holder.date.setText(viewDateFormat.format(entry.getDate()));
            holder.time.setText(viewTimeFormat.format(entry.getDate()));

            final ImageView starView = holder.star;

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
            });

            // setAnimation(holder.itemView, position);
        }

        /**
         * Here is the key method to apply the animation
         * https://stackoverflow.com/a/26748274/7827128
         */
        private void setAnimation(View viewToAnimate, int position)
        {
            // If the bound view wasn't previously displayed on screen, it's animated
            if (position > lastPosition)
            {
                // R.anim.slide_up
                Animation animation = AnimationUtils.loadAnimation(context, android.R.anim.slide_in_left);
                viewToAnimate.startAnimation(animation);
                lastPosition = position;
            }
        }

        @Override
        public int getItemCount() {
            // Return the size of the data set (invoked by the layout manager)
            return dataSet.length;
        }

        /**
         * Provide a reference to the views for each data item - Complex data items may need more
         * than one view per item, and you provide access to all the views for a data item in a
         * view holder
         */
        public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            final public TextView course, title, date, time, description;
            final public LinearLayout background;
            final public ImageView star;

            private ViewHolder(View view) {
                super(view);
                // add on click listener to each view holder
                view.setOnClickListener(this);
                // set the views of the view holder
                background = view.findViewById(R.id.background);
                course = view.findViewById(R.id.course);
                date = view.findViewById(R.id.date);
                title = view.findViewById(R.id.title);
                description = view.findViewById(R.id.description);
                time = view.findViewById(R.id.time);
                star = view.findViewById(R.id.star);
            }

            @Override
            public void onClick(final View view) {
                final int itemPosition = mRecyclerView.getChildLayoutPosition(view);
                final IliasRssItem entry = dataSet[itemPosition];

                if (entry.getDescription() == null) {
                    // if there is no description this means it was an upload
                    // therefore instantly link to the Ilias page
                    openUrl(entry.getLink());
                } else {
                    // if not this must be a legit message for which a popup dialog will be opened
                    final AlertDialog alertDialogBuilder = new AlertDialog.Builder(
                            MainActivity.this)
                            .setTitle(entry.getCourse() + " (" + viewDateFormat.format(entry.getDate()) + ")")
                            .setMessage(">> " + entry.getTitle() + "\n\n" + Html.fromHtml(entry.getDescription()))
                            .setCancelable(true)
                            .setPositiveButton(getString(R.string.open_in_ilias), new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    openUrl(entry.getLink());
                                }
                            })
                            .setNegativeButton(getString(R.string.go_back), new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                }
                            })
                            .create();
                    alertDialogBuilder.show();
                }
            }
        }
    }

}
