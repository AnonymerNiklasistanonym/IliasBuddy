package com.example.niklasm.iliasbuddy;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.ContextCompat;
import android.util.Base64;
import android.util.Log;

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
        Log.i("BackgroundIntentService", "onStartCommand");
        // make visible that service started
        // Toast.makeText(getApplicationContext(), "BackgroundIntentService onStartCommand", Toast.LENGTH_SHORT).show();

        // get the important data for a web request
        final SharedPreferences myPrefs = getSharedPreferences("myPrefs", MODE_PRIVATE);
        iliasRssUrl = myPrefs.getString(getString(R.string.ilias_url), "nothing_found");
        iliasRssUserName = myPrefs.getString(getString(R.string.ilias_user_name), "nothing_found");
        iliasRssPassword = myPrefs.getString(getString(R.string.ilias_password), "nothing_found");

        // make a web request with the important data
        getWebContent();

        // return this so that the service can be restarted
        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    public void createNotification(String previewString, String bigString) {

        Notification notification2 = new Notification();
        notification2.defaults |= Notification.DEFAULT_SOUND;
        notification2.defaults |= Notification.DEFAULT_VIBRATE;

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "CHANNEL")
                .setDefaults(notification2.defaults)
                .setSmallIcon(R.drawable.ic_ilias_logo_notification)
                .setColor(ContextCompat.getColor(BackgroundIntentService.this, R.color.colorPrimary))
                .setContentTitle("Ilias Buddy Notification!")
                .setLights(getResources().getColor(R.color.colorPrimary), 3000, 3000)
                .setContentText(previewString);

        NotificationCompat.BigTextStyle bigText = new NotificationCompat.BigTextStyle();
        bigText.bigText(bigString);
        builder.setStyle(bigText);

        Intent resultIntent = new Intent(this, MainActivity.class);
        resultIntent.putExtra(getString(R.string.render_new_elements), true);
        resultIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(pendingIntent);

        Notification notification = builder.build();
        notification.flags = Notification.FLAG_AUTO_CANCEL;

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(0, notification);
    }

    public void getWebContent() {
        Log.i("BackgroundIntentService", "getWebContent");
        final RequestQueue queue = Volley.newRequestQueue(this);
        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, this.iliasRssUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.i("BackgroundIntentService", "Got RSS data response");
                try {
                    parseXml(response);
                } catch (IOException | XmlPullParserException err) {
                    Log.e("IliasRssHandler Error", err.toString());
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("BackgroundIntentService", "Error RSS data response" + error.toString());
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
        Log.i("BackgroundIntentService", "parseXml");
        final XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
        factory.setNamespaceAware(true);
        final XmlPullParser xpp = factory.newPullParser();
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
                                final SimpleDateFormat sf1 = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss ZZZZZ");
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

        // convert ArrayList to Array
        final IliasRssItem[] myDataSet = entries.toArray(new IliasRssItem[0]);

        // get latest item string form shared preferences
        final SharedPreferences myPrefs = getSharedPreferences("myPrefs", MODE_PRIVATE);
        final String latestItem = myPrefs.getString(getString(R.string.latestItem),"nothing_found");

        int latestEntry = -1;
        boolean searchingForNewElements = true;
        for (int i = 0; i < myDataSet.length; i++) {
            if (searchingForNewElements && myDataSet[i].toString().equals(latestItem)) {
                    latestEntry = i;
                    searchingForNewElements = false;
                }
            }
        if (latestEntry == -1) {
            Log.i("BackgroundIntentService", "All entries are new");
            final String previewString = myDataSet.length + " new entries (all are new)";
            final StringBuilder bigString = new StringBuilder(previewString + "\n");
            final SimpleDateFormat viewDateFormat = new SimpleDateFormat("dd.MM HH:mm", getResources().getConfiguration().locale);
            for (IliasRssItem entry : myDataSet) {
                bigString.append("- ")
                        .append(entry.getCourse())
                        .append(" >> ")
                        .append(entry.getTitle())
                        .append(" (")
                        .append(viewDateFormat.format(entry.getDate()))
                        .append(")\n");
            }
            createNotification(previewString, bigString.toString());
        } else if (latestEntry == 0) {
            Log.i("BackgroundIntentService", "No new entry found");
        } else {
            Log.i("BackgroundIntentService", "New entries found");
            final String previewString = latestEntry == 1 ? "one new entry found" : latestEntry + " new entries found";
            final StringBuilder bigString = new StringBuilder(previewString);
            final SimpleDateFormat viewDateFormat = new SimpleDateFormat("dd.MM HH:mm", getResources().getConfiguration().locale);
            for (int i = 0; i < latestEntry; i++) {
                bigString.append(myDataSet[i].getCourse())
                        .append(myDataSet[i].getTitle())
                        .append(" (")
                        .append(viewDateFormat.format(myDataSet[i].getDate()))
                        .append(")\n");
            }
            createNotification(previewString, bigString.toString());
        }
    }
}