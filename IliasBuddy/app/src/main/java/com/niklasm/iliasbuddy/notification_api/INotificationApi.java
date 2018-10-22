package com.niklasm.iliasbuddy.notification_api;

import android.support.annotation.NonNull;

import com.niklasm.iliasbuddy.notification_api.notification_type.INotificationType;

public interface INotificationApi {
    void showNotification(@NonNull final INotificationType notificationType);

    void clearNotification(final int notificationId);

    void clearNotifications();
}
