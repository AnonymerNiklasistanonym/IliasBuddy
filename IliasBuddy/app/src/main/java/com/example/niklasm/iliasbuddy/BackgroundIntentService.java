package com.example.niklasm.iliasbuddy;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.AlertDialog;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.StringReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BackgroundIntentService extends Service {
    String iliasRssUrl;
    String iliasRssUserName;
    String iliasRssPassword;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("Ilias Buddy", "Do something in the background");
        Toast.makeText(getApplicationContext(), "hi there", Toast.LENGTH_LONG).show();

        SharedPreferences myPrefs = getSharedPreferences("myPrefs", MODE_PRIVATE);
        iliasRssUrl = myPrefs.getString(getString(R.string.ilias_url), "nothing_found");
        iliasRssUserName = myPrefs.getString(getString(R.string.ilias_user_name), "nothing_found");
        iliasRssPassword = myPrefs.getString(getString(R.string.ilias_password), "nothing_found");

        getWebContent();

        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    public void createNotification(String previewString, String bigString) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "CHANNEL")
                .setSmallIcon(R.drawable.ic_fiber_new)
                .setContentTitle("Ilias Buddy Notification!")
                .setContentText(previewString);

        NotificationCompat.BigTextStyle bigText = new NotificationCompat.BigTextStyle();
        bigText.bigText(bigString);
        builder.setStyle(bigText);

        Intent resultIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(pendingIntent);

        Notification notification = builder.build();
        notification.flags = Notification.FLAG_AUTO_CANCEL;

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(0, notification);
    }

    public void getWebContent() {

        RequestQueue queue = Volley.newRequestQueue(this);

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, this.iliasRssUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.i("BackgroundIntentService", response);
                try {
                    parseXml(response);
                } catch (IOException | XmlPullParserException err) {
                    Log.e("IliasRssHandler Error", err.toString());
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                AlertDialog alertDialog = new AlertDialog.Builder(BackgroundIntentService.this)
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
                Log.e("IliasRssHandler - Error", error.toString());
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

    public void parseXml(String content) throws XmlPullParserException, IOException {
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
        SharedPreferences myPrefs = getSharedPreferences("myPrefs", MODE_PRIVATE);
        final String latestItem = myPrefs.getString(getString(R.string.latestItem),"nothing_found");
        ArrayList<IliasRssItem> stringArrayList = new ArrayList<>();
        boolean searchingForNewElements = true;
        for (int i = 0; i < myDataset.length; i++) {
            if (searchingForNewElements) {
                if (myDataset[i].toString().equals(latestItem)) {
                    searchingForNewElements = false;
                } else {
                    stringArrayList.add(myDataset[i]);
                }
            }
        }
        if (stringArrayList.size() > 0) {
            Log.i("IliasRssHandler", "New entry found");
            final String previewString = stringArrayList.size() + " new entries";
            String bigString = previewString;
            for (int i = 0; i < stringArrayList.size(); i++) {
                bigString += stringArrayList.get(i).toString() + "\n";
            }
            createNotification(previewString, bigString);
        } else {
            Log.i("IliasRssHandler", "No new entry found");
        }

        // only continue if the latest object is different
        // final boolean newEntryFound = latestRssEntry != null && latestRssEntry.toString().equals(myDataset[0].toString());

        /*if (newEntryFound) {
            latestRssEntry = myDataset[0];
            Log.i("IliasRssHandler", "New entry found");
            mainActivity.renderNewList(myDataset);
        } else {
            Log.i("IliasRssHandler", "No new entry found");
        }*/
    }
}