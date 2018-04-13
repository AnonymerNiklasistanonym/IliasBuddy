package com.example.niklasm.iliasbuddy;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.StringReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private String iliasRssUrl;
    private String iliasRssPassword;
    private String iliasRssUserName;

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    private IliasRssItem latestRssEntry = null;
    private String lastResponse = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
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
        iliasRssUrl = myPrefs.getString(getString(R.string.ilias_url), "nothing_found");
        iliasRssUserName = myPrefs.getString(getString(R.string.ilias_user_name), "nothing_found");
        iliasRssPassword = myPrefs.getString(getString(R.string.ilias_password), "nothing_found");

        if (iliasRssUrl.equals("nothing_found") || (iliasRssUserName.equals("nothing_found") || iliasRssPassword.equals("nothing_found"))) {
            Intent intent = new Intent(this, SetupActivity.class);
            startActivity(intent);
        }

        findViewById(R.id.fab).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getRssFeed();
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });


        // try to load saved RSS feed
        final IliasRssItem[] myDataset = readRssFeed(this, "TestFile.test");
        if (myDataset != null && myDataset.length > 0) {
            // save latest object
            latestRssEntry = myDataset[0];
            // specify an adapter (see also next example)
            mAdapter = new MyAdapter(myDataset);
            mRecyclerView.setAdapter(mAdapter);
        }

        // load newest changes
        getRssFeed();
    }

    public void openSetup(MenuItem menuItem) {
        Intent intent = new Intent(this, SetupActivity.class);
        startActivity(intent);
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
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(browserIntent);
    }

    public void getRssFeed() {

        RequestQueue queue = Volley.newRequestQueue(this);

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, iliasRssUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                lastResponse = response;
                // Log.i("Response", response);
                try {
                    test(response);
                } catch (IOException | XmlPullParserException err) {
                    Log.e("Error", err.toString());
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this)
                        .setTitle("Response Error")
                        .setMessage(error.toString())
                        .setNeutralButton("OK",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                })
                        .create();
                alertDialog.show();
                Log.e("Response is", error.toString());
            }
        }) {
            @Override
            public Map<String, String> getHeaders() {
                final Map<String, String> headers = new HashMap<>();
                final String credentials = iliasRssUserName + ":" + iliasRssPassword;
                final String auth = "Basic " + Base64.encodeToString(credentials.getBytes(), Base64.NO_WRAP);
                headers.put("Content-Type", "application/json");
                headers.put("Authorization", auth);
                return headers;
            }
        };
        // Add the request to the RequestQueue.
        queue.add(stringRequest);
    }

    public void test(String content) throws XmlPullParserException, IOException {
        XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
        factory.setNamespaceAware(true);
        XmlPullParser xpp = factory.newPullParser();

        xpp.setInput(new StringReader(content));
        int eventType = xpp.getEventType();
        boolean inItem = false;
        String currentTag = null;
        String course = null;
        String title = null;
        String description = null;
        String link = null;
        Date date = null;
        List<IliasRssItem> entries = new ArrayList<>();

        while (eventType != XmlPullParser.END_DOCUMENT) {
            if (eventType == XmlPullParser.START_DOCUMENT) {
                // Log.i("XML-Test","Start document");
            } else if (eventType == XmlPullParser.START_TAG) {
                currentTag = xpp.getName();
                // Log.i("XML-Test","Start tag "+currentTag);
                if (currentTag.equals("item")) {
                    inItem = true;
                    course = null;
                    title = null;
                    description = null;
                    link = null;
                    date = null;
                    // Log.i("XML-Test","---------------------");
                }
            } else if (eventType == XmlPullParser.END_TAG) {
                // Log.i("XML-Test","End tag "+xpp.getName());
                if (xpp.getName().equals("item")) {
                    inItem = false;
                    entries.add(new IliasRssItem(course, title, link, description, date));
                    // Log.i("XML-Test","---------------------");
                }
                currentTag = null;
            } else if (eventType == XmlPullParser.TEXT) {
                // Log.i("XML-Test","Text "+xpp.getText());
                if (inItem && currentTag != null) {
                    final String currentText = xpp.getText();
                    switch (currentTag) {
                        case "title":
                            // convert title into course and title
                            course = currentText.substring(currentText.indexOf("[") + 1, currentText.indexOf("]")).trim();
                            title = currentText.substring(currentText.indexOf("]") + 1).trim();
                            break;
                        case "link":
                            link = currentText;
                            break;
                        case "description":
                            description = currentText;
                            break;
                        case "pubDate":
                            try {
                                SimpleDateFormat sf1 = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss ZZZZZ");
                                date = sf1.parse(currentText);
                            } catch (ParseException e) {
                                Log.e("Error Date", e.toString());
                            }
                            break;
                    }
                }
            }
            eventType = xpp.next();
        }
        // Log.i("XML-Test","End document");

        //String[] myDataset = new String[entries.size()];

        IliasRssItem[] myDataset = entries.toArray(new IliasRssItem[0]);

        // only continue if the latest object is different
        if (latestRssEntry != null && latestRssEntry.toString().equals(myDataset[0].toString())) return;

        writeRssFeed(this, "TestFile.test", myDataset);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);

        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        // specify an adapter (see also next example)
        mAdapter = new MyAdapter(myDataset);
        mRecyclerView.setAdapter(mAdapter);

        latestRssEntry = myDataset[0];
    }

    public void writeRssFeed(Context context, String filename, Object saveThisObject) {

        final File directory = new File(context.getFilesDir().getAbsolutePath()
                + File.separator + "serialisation");
        if (!directory.exists() && !directory.mkdirs()) {
                Log.e("Error", "Directory (" + directory.toString() + "could not be created!");
        }

        try {
            final ObjectOutput out = new ObjectOutputStream(new FileOutputStream(directory
                    + File.separator + filename));
            out.writeObject(saveThisObject);
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public IliasRssItem[] readRssFeed(Context context, String filename) {

        final File directory = new File(context.getFilesDir().getAbsolutePath()
                + File.separator + "serialisation");
        IliasRssItem[] ReturnClass = null;

        try {
            final ObjectInputStream input = new ObjectInputStream(new FileInputStream(directory
                    + File.separator + filename));
            ReturnClass = (IliasRssItem[]) input.readObject();
            input.close();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return ReturnClass;
    }

    public class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {

        private final IliasRssItem[] dataSet;

        private MyAdapter(IliasRssItem[] dataSet) {
            this.dataSet = dataSet;
        }

        @Override
        @NonNull
        public MyAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            // Create new views (invoked by the layout manager)
            final View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.recycler_view, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            // Replace the contents of a view (invoked by the layout manager)
            // - get element from the data set at this position
            // - replace the contents of the view with that element
            final IliasRssItem entry = dataSet[position];
            final SimpleDateFormat sdf = new SimpleDateFormat("dd.MM HH:mm", getResources().getConfiguration().locale);
            if (latestRssEntry.getDate().getTime() < entry.getDate().getTime()) {
                holder.background.setBackgroundResource(R.color.colorFabButton);
            }
            if (entry.getDescription() == null) {
                holder.description.setVisibility(View.GONE);
                holder.title.setLines(2);
                holder.course.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        openUrl(entry.getLink());
                    }
                });
            } else {
                holder.description.setText(Html.fromHtml(entry.getDescription().replaceAll("\\s+", " ")));
                holder.course.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        AlertDialog alertDialogBuilder = new AlertDialog.Builder(
                                MainActivity.this)
                                .setTitle(entry.getCourse() + " (" + sdf.format(entry.getDate()) + ")")
                                .setMessage(entry.getTitle() + "\n\n" + Html.fromHtml(entry.getDescription()))
                                .setCancelable(false)
                                .setPositiveButton("Open Ilias", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        openUrl(entry.getLink());
                                    }
                                })
                                .setNegativeButton("BACK", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.cancel();
                                    }
                                })
                                .create();
                        alertDialogBuilder.show();
                    }
                });
            }
            holder.course.setText(entry.getCourse());
            holder.title.setText(entry.getTitle());
            holder.date.setText(sdf.format(entry.getDate()));
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
        public class ViewHolder extends RecyclerView.ViewHolder {
            final public TextView course, title, date, description;
            final public LinearLayout background;

            private ViewHolder(View view) {
                super(view);
                background = view.findViewById(R.id.background);
                course = view.findViewById(R.id.course);
                date = view.findViewById(R.id.date);
                title = view.findViewById(R.id.title);
                description = view.findViewById(R.id.description);

            }
        }
    }

}
