package com.niklasm.iliasbuddy.notification_api.notification_channel;

import android.content.Context;
import android.support.annotation.NonNull;

public interface INotificationChannel {

    void createNotificationChannel(@NonNull final Context context);
}
