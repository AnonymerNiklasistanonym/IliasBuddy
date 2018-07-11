package com.example.niklasm.iliasbuddy.background_service;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.text.Html;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.VolleyError;
import com.example.niklasm.iliasbuddy.MainActivity;
import com.example.niklasm.iliasbuddy.R;
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
import java.util.ArrayList;

public class BackgroundIntentService extends Service implements IliasRssXmlWebRequesterInterface {

    public final static String NOTIFICATION_INTENT_EXTRA_PREVIEW_STRING = "previewString";
    public final static String NOTIFICATION_INTENT_EXTRA_BIG_STRING = "bigString";
    public static final String NOTIFICATION_INTENT_MESSAGE_COUNT = "only_one";
    private final static String LATEST_ITEM_NOT_FOUND = "nothing_found";

    @Override
    public int onStartCommand(final Intent intent, final int flags, final int startId) {
        Log.i("BackgroundIntentService", "onStartCommand");

        // make a web request with the important data
        final IliasRssXmlWebRequester webRequester = new IliasRssXmlWebRequester(this);
        webRequester.getWebContent();

        // return this so that the service can be restarted
        return Service.START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(final Intent arg0) {
        return null;
    }

    public void createNotification(final String titleString, final String previewString, final String bigString, final int MESSAGE_COUNT, final String[] INBOX_MESSAGES) {

        final SharedPreferences myPrefs = getSharedPreferences("myPrefs", BackgroundIntentService.MODE_PRIVATE);
        final String latestItem = myPrefs.getString(getString(R.string.lastNotification), BackgroundIntentService.LATEST_ITEM_NOT_FOUND);

        if (!latestItem.equals(BackgroundIntentService.LATEST_ITEM_NOT_FOUND) && latestItem.equals(bigString)) {
            Log.i("BackgroundIntentService", "Do not make a new notification, the text is the same");
            Log.i("BackgroundIntentService", "latestItem: " + latestItem);
            Log.i("BackgroundIntentService", "bigString: " + bigString);
            return;
        } else {
            Log.i("BackgroundIntentService", "Make a new notification, the text is NOT the same");
        }

        // if new notification save the old String
        final SharedPreferences.Editor e = myPrefs.edit();
        e.putString(getString(R.string.lastNotification), bigString);
        e.apply();

        final Intent ON_CLICK = new Intent(this, MainActivity.class)
                .putExtra(MainActivity.NEW_ENTRY_FOUND, true)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        BackgroundServiceNewEntriesNotification.show(this, titleString, previewString, bigString, INBOX_MESSAGES, ON_CLICK, MESSAGE_COUNT);

        final Intent callMainActivity = new Intent(MainActivity.RECEIVE_JSON)
                .putExtra(BackgroundIntentService.NOTIFICATION_INTENT_EXTRA_PREVIEW_STRING, previewString)
                .putExtra(BackgroundIntentService.NOTIFICATION_INTENT_MESSAGE_COUNT, MESSAGE_COUNT)
                .putExtra(BackgroundIntentService.NOTIFICATION_INTENT_EXTRA_BIG_STRING, bigString);
        LocalBroadcastManager.getInstance(this).sendBroadcast(callMainActivity);
    }

    private IliasRssItem[] getNewElements(final IliasRssItem[] NEW_DATA_SET, final String CURRENT_LATEST_ENTRY) {

        // if new data is null return null
        if (NEW_DATA_SET == null || NEW_DATA_SET.length == 0) {
            return null;
        }

        // if current latest entry is null return the whole data set
        if (CURRENT_LATEST_ENTRY == null) {
            return NEW_DATA_SET;
        }

        final ArrayList<IliasRssItem> newIliasRssItems = new ArrayList<>();

        // else check which elements of the new data set are new
        for (final IliasRssItem NEW_ENTRY : NEW_DATA_SET) {
            if (NEW_ENTRY.toString().equals(CURRENT_LATEST_ENTRY)) {
                // if newest entry was found return all found entries
                return newIliasRssItems.toArray(new IliasRssItem[0]);
            } else {
                newIliasRssItems.add(NEW_ENTRY);
            }
        }

        // if all entries where new return the complete new data set
        return NEW_DATA_SET;
    }

    @Override
    public void processIliasXml(final String FEED_XML_DATA) {
        Log.i("BackgroundIntentService", "parseXml");
        final InputStream stream = new ByteArrayInputStream(FEED_XML_DATA.replace("<rss version=\"2.0\">", "").replace("</rss>", "").getBytes(StandardCharsets.UTF_8));
        final IliasRssItem[] myDataSet;
        try {
            myDataSet = IliasRssXmlParser.parse(stream);
        } catch (ParseException | XmlPullParserException | IOException e) {
            e.printStackTrace();
            return;
        }

        // get latest item string form shared preferences
        final SharedPreferences myPrefs = getSharedPreferences("myPrefs", BackgroundIntentService.MODE_PRIVATE);
        final String latestItem = myPrefs.getString(getString(R.string.latestItem), null);

        final IliasRssItem[] NEW_ENTRIES = getNewElements(myDataSet, latestItem);

        final SimpleDateFormat VIEW_DATE_FORMAT = new SimpleDateFormat("dd.MM HH:mm", getResources().getConfiguration().locale);

        if (NEW_ENTRIES == null || NEW_ENTRIES.length == 0) {
            Log.i("BackgroundIntentService", "No new entries where found");
        } else if (NEW_ENTRIES.length == 1) {
            Log.i("BackgroundIntentService", "One new entry was found");

            final String previewString = myDataSet[0].getCourse() + (myDataSet[0].getExtra() != null ? " > " + myDataSet[0].getExtra() : "");
            final String bigString = NEW_ENTRIES[0].toStringNotificationPreview2(VIEW_DATE_FORMAT)
                    + "\n\n" + Html.fromHtml(myDataSet[0].getDescription());

            createNotification("One new Ilias entry!", previewString, bigString, 1, new String[]{bigString});
        } else {
            Log.i("BackgroundIntentService", "More than one new entry was found");

            final String previewString = "(" + myDataSet[0].getCourse() + ", " + myDataSet[1].getCourse() + ",... )";
            final StringBuilder bigString = new StringBuilder("");
            final String[] inboxArray = new String[NEW_ENTRIES.length];
            for (int i = 0; i < NEW_ENTRIES.length; i++) {
                final IliasRssItem entry = NEW_ENTRIES[i];
                inboxArray[i] = entry.toStringNotificationPreview(VIEW_DATE_FORMAT);
                bigString.append("- ").append(inboxArray[i]).append((i != NEW_ENTRIES.length - 1 ? "\n" : ""));
            }

            createNotification(NEW_ENTRIES.length + " new Ilias entries!", previewString, bigString.toString(), NEW_ENTRIES.length, inboxArray);
        }
    }

    @Override
    public void webAuthenticationError(final AuthFailureError error) {
        Log.e("BackgroundInt - AuthErr", error.toString());
    }

    @Override
    public void webResponseError(final VolleyError error) {
        Log.e("BackgroundInt - RespErr", error.toString());
    }
}
