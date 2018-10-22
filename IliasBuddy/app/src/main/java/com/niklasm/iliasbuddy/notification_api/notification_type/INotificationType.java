package com.niklasm.iliasbuddy.notification_api.notification_type;

import android.app.Notification;
import android.content.Context;
import android.support.annotation.NonNull;

public interface INotificationType {

    @NonNull
    Notification getNotification(@NonNull Context context);

    int getNotificationId();
}
