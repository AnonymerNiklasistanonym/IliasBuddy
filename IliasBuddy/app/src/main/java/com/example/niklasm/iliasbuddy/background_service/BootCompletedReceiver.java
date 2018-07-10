package com.example.niklasm.iliasbuddy.background_service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class BootCompletedReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(final Context context, final Intent intent) {
        Log.i("BootCompletedReceiver", "onReceive - begin");
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            context.startActivity(
                    new Intent(context,
                            BackgroundServiceManager.class)
                            .putExtra(BackgroundServiceManager.INTENT_EXTRA_NAME,
                                    BackgroundServiceManager.START_BACKGROUND_SERVICE));
        }
        Log.i("BootCompletedReceiver", "onReceive - end");
    }
}
