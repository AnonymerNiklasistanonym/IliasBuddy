package com.niklasm.iliasbuddy.handler;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ShareCompat;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.TextView;

import com.example.niklasm.iliasbuddy.R;

import java.util.Objects;

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

    /**
     * Display a snack bar with an error message
     *
     * @param CONTEXT Current activity
     * @param PARENT  Parent to which respect the snack bar should be shown (FAB for example)
     * @param TITLE   String id of title of error snack bar
     * @param MESSAGE Message of snack bar
     */
    public static void displayErrorSnackBar(@NonNull final Context CONTEXT,
                                            @NonNull final View PARENT,
                                            @NonNull final String TITLE,
                                            @NonNull final String MESSAGE) {
        Snackbar.make(PARENT, TITLE, Snackbar.LENGTH_INDEFINITE)
                .setActionTextColor(Color.RED)
                .setAction(R.string.dialog_more,
                        view -> new AlertDialog.Builder(CONTEXT)
                                .setTitle(TITLE)
                                .setMessage(MESSAGE)
                                .setNeutralButton(R.string.dialog_ok,
                                        (dialog, which) -> dialog.dismiss())
                                .create()
                                .show())
                .show();
    }

    public static void makeAlertDialogTextSelectable(final AlertDialog ALERT_DIALOG) {
        final TextView textView = Objects.requireNonNull(ALERT_DIALOG.getWindow()).getDecorView()
                .findViewById(android.R.id.message);
        textView.setTextIsSelectable(true);
    }
}
