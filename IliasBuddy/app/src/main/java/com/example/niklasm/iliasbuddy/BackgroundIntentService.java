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
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.VolleyError;

import org.xmlpull.v1.XmlPullParserException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;

public class BackgroundIntentService extends Service implements IliasXmlWebRequesterInterface {

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("BackgroundIntentService", "onStartCommand");
        // make visible that service started
        // Toast.makeText(getApplicationContext(), "BackgroundIntentService onStartCommand", Toast.LENGTH_SHORT).show();

        // make a web request with the important data
        IliasXmlWebRequester webRequester = new IliasXmlWebRequester(this);
        webRequester.getWebContent();

        // return this so that the service can be restarted
        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    public void createNotification(String previewString, String bigString) {

        final SharedPreferences myPrefs = getSharedPreferences("myPrefs", MODE_PRIVATE);
        final String latestItem = myPrefs.getString(getString(R.string.lastNotification), "nothing_found");

        if (!latestItem.equals("nothing_found") && latestItem.equals(bigString)) {
            Log.i("BackgroundIntentService", "Do not make a new notification, the text is the same");
            return;
        } else {
            Log.i("BackgroundIntentService", "Make a new notification, the text is NOT the same");
        }

        // if new notification save the old String
        final SharedPreferences.Editor e = myPrefs.edit();
        e.putString(getString(R.string.lastNotification), bigString);
        e.apply();

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

        Intent callMainActivity = new Intent(MainActivity.RECEIVE_JSON)
                .putExtra("previewString", previewString)
                .putExtra("bigString", bigString);
        LocalBroadcastManager.getInstance(this).sendBroadcast(callMainActivity);
    }

    public void processIliasXml(final String xmlData) {
        Log.i("BackgroundIntentService", "parseXml");
        final InputStream stream = new ByteArrayInputStream(xmlData.replace("<rss version=\"2.0\">", "").replace("</rss>", "").getBytes(StandardCharsets.UTF_8));
        final IliasRssItem[] myDataSet;
        try {
            myDataSet = IliasXmlParser.parse(stream);
        } catch (ParseException | XmlPullParserException | IOException e) {
            e.printStackTrace();
            return;
        }

        // get latest item string form shared preferences
        final SharedPreferences myPrefs = getSharedPreferences("myPrefs", MODE_PRIVATE);
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
            for (IliasRssItem entry : myDataSet) {
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
            createNotification(previewString, bigString.toString());
        } else if (latestEntry == 0) {
            Log.i("BackgroundIntentService", "No new entry found");
        } else {
            Log.i("BackgroundIntentService", "New entries found");
            final String previewString = latestEntry == 1 ? "one new entry found" : latestEntry + " new entries found";
            final StringBuilder bigString = new StringBuilder(previewString + "\n");
            final SimpleDateFormat viewDateFormat = new SimpleDateFormat("dd.MM HH:mm", getResources().getConfiguration().locale);
            for (int i = 0; i < latestEntry; i++) {
                bigString.append("- ")
                        .append(myDataSet[i].getCourse())
                        .append(myDataSet[i].getExtra() != null ? " > " + myDataSet[i].getExtra() : "")
                        .append(" >> ")
                        .append(myDataSet[i].getTitle())
                        .append(" (")
                        .append(viewDateFormat.format(myDataSet[i].getDate()))
                        .append(")\n");
            }
            createNotification(previewString, bigString.toString());
        }
    }

    public void webAuthenticationError(AuthFailureError error) {
        Log.i("BackgroundAct - AuthErr", error.toString());
    }

    public void webResponseError(VolleyError error) {
        Log.i("BackgroundAct - RespErr", error.toString());
    }
}