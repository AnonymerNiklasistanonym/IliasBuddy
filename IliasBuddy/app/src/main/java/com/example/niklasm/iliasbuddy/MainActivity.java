package com.example.niklasm.iliasbuddy;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
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
import android.widget.TextView;

import com.android.volley.AuthFailureError;
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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;
import java.io.StringReader;
import java.text.DateFormat;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mRecyclerView = findViewById(R.id.my_recycler_view);

        SharedPreferences myPrefs = getSharedPreferences("myPrefs", MODE_PRIVATE);
        iliasRssUrl = myPrefs.getString(getString(R.string.ilias_url), "nothing_found");
        iliasRssUserName = myPrefs.getString(getString(R.string.ilias_user_name), "nothing_found");
        iliasRssPassword = myPrefs.getString(getString(R.string.ilias_password), "nothing_found");

        if (iliasRssUrl.equals("nothing_found") || (iliasRssUserName.equals("nothing_found") || iliasRssPassword.equals("nothing_found"))) {
            Intent intent = new Intent(this, SetupActivity.class);
            startActivity(intent);
        }

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getRssFeed();
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        //getRssFeed();

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);

        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        final IliasRssItem[] myDataset = readRssFeed(this, "TestFile.test");

        if (myDataset != null && myDataset.length > 0) {
            // specify an adapter (see also next example)
            mAdapter = new MyAdapter(myDataset);
            mRecyclerView.setAdapter(mAdapter);
        }
    }

    public void openSetup(MenuItem menuItem) {
        Intent intent = new Intent(this, SetupActivity.class);
        startActivity(intent);
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
                AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this)
                        .setTitle("Response")
                        .setIcon(R.drawable.ic_ilias_logo)
                        .setMessage(response)
                        .create();
                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                alertDialog.show();
                Log.i("Response", response);
                try {
                    test(response);
                } catch (IOException err) {
                    Log.e("Error", err.toString());
                } catch (XmlPullParserException err) {
                    Log.e("Error", err.toString());
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
                alertDialog.setTitle("Response Error");
                alertDialog.setMessage(error.toString());
                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                alertDialog.show();
                Log.e("Response is", error.toString());
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                String credentials = iliasRssUserName + ":" + iliasRssPassword;
                String auth = "Basic "
                        + Base64.encodeToString(credentials.getBytes(), Base64.NO_WRAP);
                headers.put("Content-Type", "application/json");
                headers.put("Authorization", auth);
                return headers;
            }
        };
        // Add the request to the RequestQueue.
        queue.add(stringRequest);
        // Add the request to the RequestQueue.
        Log.i("Test", "test");

    }

    public void test(String content) throws XmlPullParserException, IOException
    {
        XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
        factory.setNamespaceAware(true);
        XmlPullParser xpp = factory.newPullParser();

        xpp.setInput( new StringReader( content ) );
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
            if(eventType == XmlPullParser.START_DOCUMENT) {
                // Log.i("XML-Test","Start document");
            } else if(eventType == XmlPullParser.START_TAG) {
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
            } else if(eventType == XmlPullParser.END_TAG) {
                // Log.i("XML-Test","End tag "+xpp.getName());
                if (xpp.getName().equals("item")) {
                    inItem = false;
                    entries.add(new IliasRssItem(course, title, link, description, date));
                    // Log.i("XML-Test","---------------------");
                }
                currentTag = null;
            } else if(eventType == XmlPullParser.TEXT) {
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

        writeRssFeed(this, "TestFile.test", myDataset);

        for (int i = 0; i < entries.size(); i++) {
            IliasRssItem cE = entries.get(i);
            Log.i("Entrylist", "Course: " + cE.getCourse() + ", Title: " + cE.getTitle() + ", Link: " + cE.getLink() + ", Description: " + cE.getDescription() + ", Date: " + DateFormat.getDateInstance(DateFormat.LONG).format(cE.getDate()));
        }

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);

        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        // specify an adapter (see also next example)
        mAdapter = new MyAdapter(myDataset);
        mRecyclerView.setAdapter(mAdapter);

    }

    public class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {

        private final IliasRssItem[] mDataset;




        // Provide a reference to the views for each data item
        // Complex data items may need more than one view per item, and
        // you provide access to all the views for a data item in a view holder
        public class ViewHolder extends RecyclerView.ViewHolder  {
            // each data item is just a string in this case
            public TextView course, title, date, description;
            public ViewHolder(View view) {
                super(view);
                course = view.findViewById(R.id.course);
                date =view.findViewById(R.id.date);
                title = view.findViewById(R.id.title);
                description = view.findViewById(R.id.description);

            }
        }

        // Provide a suitable constructor (depends on the kind of dataset)
        public MyAdapter(IliasRssItem[] myDataset) {
            mDataset = myDataset;
        }


        // Create new views (invoked by the layout manager)
        @Override
        public MyAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                       int viewType) {
            // create a new view
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.recycler_view, parent, false);
            return new ViewHolder(v);
        }

        // Replace the contents of a view (invoked by the layout manager)
        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            // - get element from your dataset at this position
            // - replace the contents of the view with that element
            final IliasRssItem entry = mDataset[position];
            final SimpleDateFormat sdf = new SimpleDateFormat("dd.MM HH:mm");
            if (entry.getDescription() == null) {
                holder.course.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        openUrl(entry.getLink());
                    }
                });
            } else {
                holder.course.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        AlertDialog alertDialogBuilder = new AlertDialog.Builder(
                                MainActivity.this)
                                .setTitle(entry.getCourse() + " (" + sdf.format(entry.getDate()) + ")")
                                .setMessage(entry.getTitle() + "\n\n" + Html.fromHtml(entry.getDescription()))
                                .setCancelable(false)
                                .setPositiveButton("Open Ilias",new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,int id) {
                                        openUrl(entry.getLink());
                                    }
                                })
                                .setNegativeButton("BACK",new DialogInterface.OnClickListener() {
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
            if (entry.getDescription() != null) {
                holder.description.setText(Html.fromHtml(entry.getDescription().replaceAll("\\s+", " ")));
            }
            holder.date.setText(sdf.format(entry.getDate()));
        }

        // Return the size of your dataset (invoked by the layout manager)
        @Override
        public int getItemCount() {
            return mDataset.length;
        }
    }

    public void writeRssFeed(Context context, String filename, Object saveThisObject) {

        File directory = new File(context.getFilesDir().getAbsolutePath()
                + File.separator + "serialisation");
        if (!directory.exists()) {
            directory.mkdirs();
        }

        ObjectOutput out;

        try {
            out = new ObjectOutputStream(new FileOutputStream(directory
                    + File.separator + filename));
            out.writeObject(saveThisObject);
            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public IliasRssItem[] readRssFeed(Context context, String filename) {

        File directory = new File(context.getFilesDir().getAbsolutePath()
                + File.separator + "serialisation");

        ObjectInputStream input;
        IliasRssItem[] ReturnClass = null;

        try {

            input = new ObjectInputStream(new FileInputStream(directory
                    + File.separator + filename));
            ReturnClass = (IliasRssItem[]) input.readObject();
            input.close();

        } catch (StreamCorruptedException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return ReturnClass;
    }

}
