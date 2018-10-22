package com.niklasm.iliasbuddy.notification_api;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationManagerCompat;

import com.niklasm.iliasbuddy.notification_api.notification_type.INotificationType;

public class NotificationApi implements INotificationApi {

    private final Context context;

    public NotificationApi(@NonNull final Context context) {
        this.context = context;
    }

    @Override
    public void showNotification(@NonNull final INotificationType notificationType) {
        NotificationManagerCompat.from(context).notify(notificationType.getNotificationId(),
                notificationType.getNotification(context));
    }

    @Override
    public void clearNotification(final int notificationId) {
        NotificationManagerCompat.from(context).cancel(notificationId);
    }

    @Override
    public void clearNotifications() {
        NotificationManagerCompat.from(context).cancelAll();
    }
}
