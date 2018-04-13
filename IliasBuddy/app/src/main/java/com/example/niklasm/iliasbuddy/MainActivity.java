package com.example.niklasm.iliasbuddy;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.NonNull;
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
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;

public class MainActivity extends AppCompatActivity {

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    private IliasRssItem latestRssEntry = null;
    private String lastResponse = null;

    private IliasRssHandler rssHandler;
    private IliasRssDataSaver rssDataSaver;

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
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
            }
        });


        // try to load saved RSS feed
        final IliasRssItem[] myDataset = rssDataSaver.readRssFeed();
        if (myDataset != null && myDataset.length > 0) {
            // save latest object
            latestRssEntry = myDataset[0];
            // specify an adapter (see also next example)
            mAdapter = new MyAdapter(myDataset);
            mRecyclerView.setAdapter(mAdapter);
        }

        // load newest changes
        checkForRssUpdates();
    }

    public void checkForRssUpdates() {
        rssHandler.getWebContent();
    }

    public void openSetup(MenuItem menuItem) {
        startActivity(new Intent(this, SetupActivity.class));
    }

    public void renderNewList(IliasRssItem[] newDataSet) {

        this.rssDataSaver.writeRssFeed(newDataSet);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);

        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        // specify an adapter (see also next example)
        mAdapter = new MyAdapter(newDataSet);
        mRecyclerView.setAdapter(mAdapter);
    }

    public void startService(MenuItem menuItem) {
        //Create a new PendingIntent and add it to the AlarmManager
        Intent intent = new Intent(getApplicationContext(), BackgroundIntentService.class);
        PendingIntent pendingIntent = PendingIntent.getService(getApplicationContext(), 12345, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager am = (AlarmManager) getSystemService(Activity.ALARM_SERVICE);
        if (am != null) {
            am.setRepeating(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime(), 60000, pendingIntent);
        }
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
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
        private final SimpleDateFormat viewDateFormat = new SimpleDateFormat("dd.MM HH:mm", getResources().getConfiguration().locale);


        private MyAdapter(IliasRssItem[] dataSet) {
            this.dataSet = dataSet;
        }

        @Override
        @NonNull
        public MyAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            // Create new views (invoked by the layout manager)
            final View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.recycler_view, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            // Replace the contents of a view (invoked by the layout manager)
            // - get element from the data set at this position
            // - replace the contents of the view with that element
            final IliasRssItem entry = dataSet[position];
            if (latestRssEntry.getDate().getTime() < entry.getDate().getTime()) {
                Log.i("COLOR BACKGROUND", "IT HAPPENED????" + latestRssEntry.getDate().getTime() + ", " + entry.getDate().getTime());
                holder.background.setBackgroundResource(R.color.colorFabButton);
                Toast.makeText(MainActivity.this, "WOW - there is a new post: " + entry.toString(), Toast.LENGTH_LONG).show();
            }
            if (entry.getDescription() == null) {
                holder.description.setVisibility(View.GONE);
                holder.title.setLines(2);
            } else {
                holder.description.setText(Html.fromHtml(entry.getDescription().replaceAll("\\s+", " ")));
            }
            holder.course.setText(entry.getCourse());
            holder.title.setText(entry.getTitle());
            holder.date.setText(viewDateFormat.format(entry.getDate()));
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
            final public TextView course, title, date, description;
            final public LinearLayout background;

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
