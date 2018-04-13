package com.example.niklasm.iliasbuddy;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;
import android.widget.Toast;

public class BackgroundIntentService extends Service {
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("Ilias Buddy", "Do something in the background");
        Toast.makeText(getApplicationContext(), "hi there", Toast.LENGTH_LONG).show();
        createNotification();
        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    public void createNotification() {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "CHANNEL")
                .setSmallIcon(android.R.drawable.ic_dialog_alert)
                .setContentTitle("Ilias Buddy Notification!")
                .setContentText("This is my first notification!");

        NotificationCompat.BigTextStyle bigText = new NotificationCompat.BigTextStyle();
        bigText.bigText("Lorem ipsum dolor sit amet, consectetur adipiscing elit. " +
                "Etiam hendrerit risus ut congue feugiat. Donec rutrum tristique purus, at tempus ipsum pharetra eget. " +
                "Ut tristique aliquet elementum. Sed hendrerit quis sapien a mattis. " +
                "Pellentesque interdum neque a felis mattis finibus. ");
        builder.setStyle(bigText);

        Intent resultIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(pendingIntent);

        Notification notification = builder.build();
        notification.flags = Notification.FLAG_AUTO_CANCEL;

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(0, notification);
    }
}