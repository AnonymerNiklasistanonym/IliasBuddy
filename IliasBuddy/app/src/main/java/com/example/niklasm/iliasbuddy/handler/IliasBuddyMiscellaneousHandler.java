package com.example.niklasm.iliasbuddy.handler;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.app.ShareCompat;

public class IliasBuddyMiscellaneousHandler {

    /**
     * Open a website in the device browser
     *
     * @param CONTEXT Current activity
     * @param URL     Website link/address that should be opened externally
     */
    public static void openUrl(@NonNull final Context CONTEXT, @NonNull final String URL) {
        CONTEXT.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(URL)));
    }

    /**
     * Share a link via Android intent to all apss that support plain text
     *
     * @param ACTIVITY          Current activity
     * @param TEXT              Text that should be shared
     * @param TEXT_LINK         Link to text that should be shared
     * @param DESCRIPTION_TITLE Description of share (shown on select of the app to share)
     */
    public static void shareLink(@NonNull final Activity ACTIVITY, @NonNull final String TEXT,
                                 @NonNull final String TEXT_LINK,
                                 @NonNull final String DESCRIPTION_TITLE) {
        ShareCompat.IntentBuilder.from(ACTIVITY)
                .setType("text/plain")
                .setSubject(TEXT)
                .setChooserTitle(DESCRIPTION_TITLE)
                .setText(TEXT_LINK)
                .startChooser();
    }

}
