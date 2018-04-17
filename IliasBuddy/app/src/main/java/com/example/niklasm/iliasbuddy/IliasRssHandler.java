package com.example.niklasm.iliasbuddy;

import android.content.Context;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

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

import java.io.IOException;
import java.io.StringReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IliasRssHandler {

    private String lastResponse;
    final private Context context;
    final private String iliasUrl;
    final private String iliasRssUserName;
    final private String iliasRssPassword;
    private IliasRssItem latestRssEntry;
    private MainActivity mainActivity;

    public IliasRssHandler(final MainActivity context, final String iliasUrl, final String iliasRssUserName, final String iliasRssPassword) {
        this.context = context;
        this.iliasUrl = iliasUrl;
        this.iliasRssUserName = iliasRssUserName;
        this.iliasRssPassword = iliasRssPassword;
        this.mainActivity = context;
    }

    public void getWebContent() {

        mainActivity.refreshIcon(true);

        RequestQueue queue = Volley.newRequestQueue(this.context);

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, this.iliasUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                lastResponse = response;
                mainActivity.setLastResponse(response);
                // Log.i("IliasRssHandler - Response", response);
                try {
                    parseXml(response);
                } catch (IOException | XmlPullParserException err) {
                    Log.e("IliasRssHandler Error", err.toString());
                    mainActivity.refreshIcon(false);
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                mainActivity.refreshIcon(false);
                if (error instanceof AuthFailureError) {
                    Toast.makeText(context, "Authentication error", Toast.LENGTH_SHORT).show();
                    mainActivity.openSetup(null);
                    return;
                }
                mainActivity.errorSnackbar("Response Error", error.toString());
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

        //String[] myDataset = new String[entries.size()];

        IliasRssItem[] myDataset = entries.toArray(new IliasRssItem[0]);

        // get the latest RSS entry from the main activity
        latestRssEntry = mainActivity.getLatestRssEntry();

        // only continue if the latest object is different
        final boolean newEntryFound = latestRssEntry == null || !latestRssEntry.toString().equals(myDataset[0].toString());

        if (newEntryFound) {
            Log.i("IliasRssHandler", "New entry found");
            mainActivity.renderNewList(myDataset);
        } else {
            mainActivity.noNewEntryFound();
            Log.i("IliasRssHandler", "No new entry found");
        }
        mainActivity.refreshIcon(false);
    }

    public void reset() {
        latestRssEntry = null;
    }
}
