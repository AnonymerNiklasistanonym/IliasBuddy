package com.example.niklasm.iliasbuddy.background_service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

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
import java.util.Objects;

public class BackgroundIntentService extends Service implements IliasRssXmlWebRequesterInterface {

    public final static String NOTIFICATION_INTENT_EXTRA_PREVIEW_STRING = "previewString";
    public final static String NOTIFICATION_INTENT_EXTRA_BIG_STRING = "bigString";
    private final static String LATEST_ITEM_NOT_FOUND = "nothing_found";

    @Override
    public int onStartCommand(final Intent intent, final int flags, final int startId) {
        Log.i("BackgroundIntentService", "onStartCommand");
        // make visible that service started
        // Toast.makeText(getApplicationContext(), "BackgroundIntentService onStartCommand", Toast.LENGTH_SHORT).show();

        // make a web request with the important data
        final IliasRssXmlWebRequester webRequester = new IliasRssXmlWebRequester(this);
        webRequester.getWebContent();

        Toast.makeText(this, "BackgroundIntentService started", Toast.LENGTH_LONG).show();

        // return this so that the service can be restarted
        return Service.START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.i("BackgroundIntentService", "onDestroy");
        Toast.makeText(this, "BackgroundIntentService Stopped", Toast.LENGTH_LONG).show();
    }

    @Override
    public IBinder onBind(final Intent arg0) {
        return null;
    }

    public void createNotification(final String titleString, final String previewString, final String bigString) {

        final SharedPreferences myPrefs = getSharedPreferences("myPrefs", BackgroundIntentService.MODE_PRIVATE);
        final String latestItem = myPrefs.getString(getString(R.string.lastNotification), BackgroundIntentService.LATEST_ITEM_NOT_FOUND);

        if (!latestItem.equals(BackgroundIntentService.LATEST_ITEM_NOT_FOUND) && latestItem.equals(bigString)) {
            Log.i("BackgroundIntentService", "Do not make a new notification, the text is the same");
            return;
        } else {
            Log.i("BackgroundIntentService", "Make a new notification, the text is NOT the same");
        }

        // if new notification save the old String
        final SharedPreferences.Editor e = myPrefs.edit();
        e.putString(getString(R.string.lastNotification), bigString);
        e.apply();

        final Notification notification2 = new Notification();
        notification2.defaults |= Notification.DEFAULT_SOUND;
        notification2.defaults |= Notification.DEFAULT_VIBRATE;

        final NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        final String CHANNEL_ID = "my_channel_01";

        // https://stackoverflow.com/a/47974065
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            Log.i("IMPORTANT", "OREO DETECTED");
            final CharSequence name = "my_channel";
            final String Description = "This is my channel";
            final int importance = NotificationManager.IMPORTANCE_HIGH;
            final NotificationChannel mChannel = new NotificationChannel(CHANNEL_ID, name, importance);
            mChannel.setDescription(Description);
            mChannel.enableLights(true);
            mChannel.setLightColor(Color.RED);
            mChannel.enableVibration(true);
            mChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
            mChannel.setShowBadge(false);
            Objects.requireNonNull(notificationManager).createNotificationChannel(mChannel);
        }

        final NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setDefaults(notification2.defaults)
                .setSmallIcon(R.drawable.ic_ilias_logo_notification)
                .setColor(ContextCompat.getColor(BackgroundIntentService.this, R.color.colorPrimary))
                .setContentTitle(titleString)
                .setLights(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary), 3000, 3000)
                .setContentText(previewString);

        final NotificationCompat.BigTextStyle bigText = new NotificationCompat.BigTextStyle();
        bigText.bigText(bigString);
        builder.setStyle(bigText);

        final Intent resultIntent = new Intent(this, MainActivity.class);
        resultIntent.putExtra(MainActivity.NEW_ENTRY_FOUND, true);
        resultIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        final PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(pendingIntent);

        final Notification notification = builder.build();
        notification.flags = Notification.FLAG_AUTO_CANCEL;

        Objects.requireNonNull(notificationManager).notify(MainActivity.NEW_ENTRY_FOUND_NOTIFICATION_ID, notification);

        final Intent callMainActivity = new Intent(MainActivity.RECEIVE_JSON)
                .putExtra(BackgroundIntentService.NOTIFICATION_INTENT_EXTRA_PREVIEW_STRING, previewString)
                .putExtra(BackgroundIntentService.NOTIFICATION_INTENT_EXTRA_BIG_STRING, bigString);
        LocalBroadcastManager.getInstance(this).sendBroadcast(callMainActivity);
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
        final String latestItem = myPrefs.getString(getString(R.string.latestItem), "nothing_found");

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
            for (final IliasRssItem entry : myDataSet) {
                bigString.append("- ")
                        .append(entry.getCourse())
                        .append(entry.getExtra() != null ? " > " + entry.getExtra() : "")
                        .append(" >> ")
                        .append(entry.getTitleExtra() != null ? entry.getTitleExtra() + ": " : "")
                        .append(entry.getTitle())
                        .append(" (")
                        .append(viewDateFormat.format(entry.getDate()))
                        .append(")\n");
            }
            createNotification("New Ilias entries! (Setup was successful)", previewString, bigString.toString());
        } else if (latestEntry == 0) {
            Log.d("BackgroundIntentService", "No new entry found");
        } else {
            final SimpleDateFormat viewDateFormat = new SimpleDateFormat("dd.MM HH:mm", getResources().getConfiguration().locale);
            if (latestEntry == 1) {
                Log.d("BackgroundIntentService", "New entry found");

                final String previewString = "New entry found (" + myDataSet[0].getCourse() + (myDataSet[0].getExtra() != null ? " > " + myDataSet[0].getExtra() : "") + ")";
                final String bigString = new StringBuilder(previewString + "\n")
                        .append(myDataSet[0].getCourse())
                        .append(myDataSet[0].getExtra() != null ? " > " + myDataSet[0].getExtra() : "")
                        .append("\n")
                        .append(myDataSet[0].getTitleExtra() != null ? myDataSet[0].getTitleExtra() + ": " : "")
                        .append(myDataSet[0].getTitle())
                        .append(" (")
                        .append(viewDateFormat.format(myDataSet[0].getDate()))
                        .append(")\n\n")
                        .append(myDataSet[0].getDescription())
                        .toString();
                createNotification("New Ilias entry!", previewString, bigString);

            } else {
                Log.d("BackgroundIntentService", "New entries found");

                final String previewString = latestEntry + " new entries found (" + myDataSet[0].getCourse() + ", " + myDataSet[1].getCourse() + ",... )";
                final StringBuilder bigString = new StringBuilder(previewString + "\n");
                for (int i = 0; i < latestEntry; i++) {
                    bigString.append("- ")
                            .append(myDataSet[i].getCourse())
                            .append(myDataSet[i].getExtra() != null ? " > " + myDataSet[i].getExtra() : "")
                            .append("\n  ")
                            .append(myDataSet[i].getTitleExtra() != null ? myDataSet[i].getTitleExtra() + ": " : "")
                            .append(myDataSet[i].getTitle())
                            .append(" (")
                            .append(viewDateFormat.format(myDataSet[i].getDate()))
                            .append(")\n");
                }
                createNotification(latestEntry + " new Ilias entries!", previewString, bigString.toString());
            }
        }
    }

    @Override
    public void webAuthenticationError(final AuthFailureError error) {
        Log.i("BackgroundAct - AuthErr", error.toString());
    }

    @Override
    public void webResponseError(final VolleyError error) {
        Log.i("BackgroundAct - RespErr", error.toString());
    }
}
