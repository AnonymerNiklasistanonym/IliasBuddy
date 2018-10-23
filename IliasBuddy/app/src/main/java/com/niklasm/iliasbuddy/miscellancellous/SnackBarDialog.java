package com.niklasm.iliasbuddy.miscellancellous;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.view.View;

import com.example.niklasm.iliasbuddy.R;

public class SnackBarDialog {

    /**
     * Display a snack bar with an error message
     *
     * @param CONTEXT Current activity
     * @param PARENT  Parent to which respect the snack bar should be shown (FAB for example)
     * @param TITLE   String id of title of error snack bar
     * @param MESSAGE Message of snack bar
     */
    public static Snackbar displayErrorSnackBar(@NonNull final Context CONTEXT,
                                                @NonNull final View PARENT,
                                                @NonNull final String TITLE,
                                                @NonNull final String MESSAGE) {
        final Snackbar snackbar = Snackbar.make(PARENT, TITLE, Snackbar.LENGTH_INDEFINITE)
                .setActionTextColor(Color.RED)
                .setAction(R.string.dialog_more,
                        view -> PopupDialog.showOkDialog(CONTEXT, TITLE, MESSAGE));
        snackbar.show();
        return snackbar;
    }

    public static Snackbar displayNormalSnackBar(@NonNull final View PARENT,
                                                 @NonNull final String TITLE,
                                                 final boolean displayLongOrShort) {
        final Snackbar snackbar = Snackbar.make(PARENT, TITLE,
                displayLongOrShort ? Snackbar.LENGTH_INDEFINITE : Snackbar.LENGTH_SHORT);
        snackbar.show();
        return snackbar;
    }

    public static Snackbar displayActionSnackBar(@NonNull final View PARENT,
                                                 @NonNull final String TITLE,
                                                 @NonNull final String ACTION_TITLE,
                                                 @NonNull final View.OnClickListener onClickListener) {
        final Snackbar snackbar = Snackbar.make(PARENT, TITLE, Snackbar.LENGTH_INDEFINITE)
                .setAction(ACTION_TITLE, onClickListener);
        snackbar.show();
        return snackbar;
    }
}
