package com.niklasm.iliasbuddy.miscellancellous;

import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.text.Spanned;
import android.widget.TextView;

import com.example.niklasm.iliasbuddy.R;

import java.util.Objects;

public class PopupDialog {

    public static void showOkDialog(@NonNull final Context context, @NonNull final String title,
                                    @NonNull final String message) {
        new AlertDialog.Builder(context)
                .setTitle(title)
                .setMessage(message)
                .setNeutralButton(R.string.dialog_ok,
                        (dialog, which) -> dialog.dismiss())
                .create().show();
    }

    public static void showOkCustomDialog(@NonNull final Context context, @NonNull final String title,
                                          @NonNull final String message,
                                          @NonNull final String positiveButton,
                                          @NonNull final DialogInterface.OnClickListener listener) {
        new AlertDialog.Builder(context)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(positiveButton, listener)
                .setNeutralButton(R.string.dialog_ok,
                        (dialog, which) -> dialog.dismiss())
                .create().show();
    }

    public static void showBackDialog(@NonNull final Context context, @NonNull final String title,
                                      @NonNull final String message) {
        new AlertDialog.Builder(context)
                .setTitle(title)
                .setMessage(message)
                .setNeutralButton(R.string.dialog_back,
                        (dialog, which) -> dialog.dismiss())
                .create().show();
    }

    public static void showOkDialogSelectable(@NonNull final Context context, @NonNull final String title,
                                              @NonNull final Spanned message) {
        PopupDialog.showDialog(context, title, message, false, true);
    }

    public static void showBackDialogSelectable(@NonNull final Context context, @NonNull final String title,
                                                @NonNull final Spanned message) {
        PopupDialog.showDialog(context, title, message, true, true);
    }

    private static void showDialog(@NonNull final Context context, @NonNull final String title,
                                   @NonNull final Spanned message, final boolean backOrOk,
                                   final boolean contentSelectable) {
        final AlertDialog alertDialog = new AlertDialog.Builder(context)
                .setTitle(title)
                .setMessage(message)
                .setNeutralButton(backOrOk ? R.string.dialog_back : R.string.dialog_ok, (dialog, which) -> dialog.dismiss())
                .show();

        if (contentSelectable) {
            final TextView textView = Objects.requireNonNull(alertDialog.getWindow()).getDecorView()
                    .findViewById(android.R.id.message);
            textView.setTextIsSelectable(true);
        }
    }
}
