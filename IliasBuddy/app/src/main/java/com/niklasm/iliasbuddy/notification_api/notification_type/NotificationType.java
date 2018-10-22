package com.niklasm.iliasbuddy.notification_api.notification_type;

import android.app.Notification;
import android.content.Context;
import android.support.annotation.NonNull;

public class NotificationType implements INotificationType {

    final int NOTIFICATION_ID;
    final String NOTIFICATION_DESCRIPTION;
    final String NOTIFICATION_TITLE;
    final NotificationOptions NOTIFICATION_OPTIONS;

    public NotificationType(final int id, @NonNull final String title,
                            @NonNull final String description,
                            @NonNull final NotificationOptions notificationOptions) {
        NOTIFICATION_ID = id;
        NOTIFICATION_DESCRIPTION = description;
        NOTIFICATION_TITLE = title;
        NOTIFICATION_OPTIONS = notificationOptions;
    }

    @NonNull
    @Override
    public Notification getNotification(@NonNull final Context context) {
        return new Notification();
    }

    @Override
    public int getNotificationId() {
        return NOTIFICATION_ID;
    }
}
