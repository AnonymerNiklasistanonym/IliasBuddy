package com.niklasm.iliasbuddy.notification_api.notification_channel;

import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;

import java.util.Objects;

public class NotificationChannel implements INotificationChannel {

    private final String CHANNEL_ID;
    private final String CHANNEL_NAME;
    private final String CHANNEL_DESCRIPTION;
    private final ChannelOptions CHANNEL_OPTIONS;

    NotificationChannel(@NonNull final String CHANNEL_ID, @NonNull final String CHANNEL_NAME,
                        @NonNull final String CHANNEL_DESCRIPTION,
                        @NonNull final ChannelOptions CHANNEL_OPTIONS) {
        this.CHANNEL_ID = CHANNEL_ID;
        this.CHANNEL_NAME = CHANNEL_NAME;
        this.CHANNEL_DESCRIPTION = CHANNEL_DESCRIPTION;
        this.CHANNEL_OPTIONS = CHANNEL_OPTIONS;
    }

    @Override
    public void createNotificationChannel(@NonNull final Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            final android.app.NotificationChannel notificationChannel =
                    new android.app.NotificationChannel(CHANNEL_ID, CHANNEL_NAME,
                            CHANNEL_OPTIONS.highImportance ? NotificationManager.IMPORTANCE_HIGH :
                                    NotificationManager.IMPORTANCE_MIN);
            notificationChannel.setDescription(CHANNEL_DESCRIPTION);
            notificationChannel.enableLights(CHANNEL_OPTIONS.lightsOn);
            notificationChannel.enableVibration(CHANNEL_OPTIONS.vibrationOn);
            notificationChannel.setShowBadge(CHANNEL_OPTIONS.badgeOn);
            
            Objects.requireNonNull((NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE))
                    .createNotificationChannel(notificationChannel);
        }
    }
}
