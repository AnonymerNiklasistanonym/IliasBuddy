package com.niklasm.iliasbuddy.handler;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ShareCompat;
import android.support.v7.app.AlertDialog;
import android.text.Html;
import android.view.View;
import android.widget.TextView;

import com.example.niklasm.iliasbuddy.R;
import com.niklasm.iliasbuddy.objects.IliasRssFeedItem;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
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

    public static String notificationBigTitleSingle(@NonNull final IliasRssFeedItem ENTRY) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return ENTRY.getCourse() + " >> " +
                    (ENTRY.getTitleExtra() != null ? ENTRY.getTitleExtra() : "");
        } else {
            return ENTRY.getCourse();
        }

    }

    public static String notificationTitleSingle(@NonNull final IliasRssFeedItem ENTRY,
                                                 @NonNull final Context CONTEXT) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return CONTEXT.getString(R.string.notification_channel_new_entries_one_new_ilias_entry) +
                    " [" + ENTRY.getCourse() + "]";
        } else {
            return CONTEXT.getString(R.string.notification_channel_new_entries_one_new_ilias_entry);
        }
    }

    public static String notificationBigContentSingle(@NonNull final IliasRssFeedItem ENTRY,
                                                      @NonNull final Context CONTEXT) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return ENTRY.getTitle() + " (" + new SimpleDateFormat("dd.MM HH:mm",
                    CONTEXT.getResources().getConfiguration().locale).format(ENTRY.getDate()) + ")" +
                    (ENTRY.getDescription().equals("") ? "" : "\n\n" + Html.fromHtml(ENTRY.getDescription()).toString().replace("\n\n", "\n"));
        } else {
            return ">> " +
                    (ENTRY.getTitleExtra() != null ? ENTRY.getTitleExtra() + " > " : "") +
                    ENTRY.getTitle() + "\n(" + new SimpleDateFormat("dd.MM HH:mm",
                    CONTEXT.getResources().getConfiguration().locale).format(ENTRY.getDate()) + ")" +
                    (ENTRY.getDescription().equals("") ? "" : "\n\n" + Html.fromHtml(ENTRY.getDescription()).toString().replace("\n\n", "\n"));
        }
    }

    public static String notificationPreviewSingle(@NonNull final IliasRssFeedItem ENTRY,
                                                   @NonNull final Context CONTEXT) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return (ENTRY.getTitleExtra() != null ? ENTRY.getTitleExtra() + ": " : "") +
                    ENTRY.getTitle() + " (" + new SimpleDateFormat("dd.MM HH:mm",
                    CONTEXT.getResources().getConfiguration().locale).format(ENTRY.getDate()) + ")";
        } else {
            return ENTRY.getCourse();
        }
    }

    public static String notificationPreviewMultiple(@NonNull final IliasRssFeedItem[] ENTRIES) {

        final StringBuilder STRING_BUILDER = new StringBuilder("");
        final ArrayList<String> COURSE_LIST = new ArrayList<>();
        final ArrayList<Integer> COUNTER_LIST = new ArrayList<>();

        for (final IliasRssFeedItem ENTRY : ENTRIES) {
            // check if course was already added
            if (COURSE_LIST.contains(ENTRY.getCourse())) {
                // if yes increment the number of occurrences counter
                final int INDEX_OF_COURSE = COURSE_LIST.indexOf(ENTRY.getCourse());
                COUNTER_LIST.set(INDEX_OF_COURSE, COUNTER_LIST.get(INDEX_OF_COURSE) + 1);
            } else {
                // if no add the course to the list
                COURSE_LIST.add(ENTRY.getCourse());
                COUNTER_LIST.add(1);
            }
        }

        // now iterate over the COURSE_COUNTER list and add them to the string with the number
        for (int i = 0; i < COURSE_LIST.size(); i++) {
            STRING_BUILDER.append(COURSE_LIST.get(i));
            // check if the occurrences are bigger than one
            if (COUNTER_LIST.get(i) > 1) {
                STRING_BUILDER.append(" (").append(COUNTER_LIST.get(i)).append(")");
            }
            STRING_BUILDER.append(i == COURSE_LIST.size() - 1 ? "" : ", ");
        }

        return STRING_BUILDER.toString();
    }
}
