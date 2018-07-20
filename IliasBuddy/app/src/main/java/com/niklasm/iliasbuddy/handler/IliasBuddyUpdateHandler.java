package com.niklasm.iliasbuddy.handler;

import android.content.Context;
import android.support.v7.app.AlertDialog;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.example.niklasm.iliasbuddy.BuildConfig;
import com.example.niklasm.iliasbuddy.R;

import org.json.JSONException;
import org.json.JSONObject;

public class IliasBuddyUpdateHandler {

    public static void checkForUpdate(final Context CONTEXT, final boolean ONLY_CHECK) {
        final String URL = "https://api.github.com/repos/AnonymerNiklasistanonym/IliasBuddy/releases";
        Volley.newRequestQueue(CONTEXT).add(new JsonArrayRequest
                (Request.Method.GET, URL, null,
                        response -> {
                            try {
                                final JSONObject NEWEST_RELEASE = response.getJSONObject(0);
                                final String NEWEST_VERSION = NEWEST_RELEASE.getString("tag_name").substring(1);
                                final String NEWEST_VERSION_URL = NEWEST_RELEASE.getString("html_url");
                                if (NEWEST_VERSION.equals(BuildConfig.VERSION_NAME)) {
                                    if (!ONLY_CHECK) {
                                        new AlertDialog.Builder(CONTEXT)
                                                .setTitle(R.string.dialog_new_version_not_found)
                                                .setMessage(R.string.dialog_new_version_already_installed)
                                                .setNeutralButton(R.string.dialog_ok,
                                                        (dialog, which) -> dialog.dismiss())
                                                .create().show();
                                    }
                                } else {
                                    new AlertDialog.Builder(CONTEXT)
                                            .setTitle(R.string.dialog_new_version_found)
                                            .setMessage(CONTEXT.getString(R.string.dialog_current_version) + ": " + BuildConfig.VERSION_NAME
                                                    + "\n" + CONTEXT.getString(R.string.dialog_latest_version) + ": " + NEWEST_VERSION)
                                            .setPositiveButton(R.string.dialog_new_version_open_release_page,
                                                    (dialog, which) -> {
                                                        IliasBuddyMiscellaneousHandler.openUrl(CONTEXT, NEWEST_VERSION_URL);
                                                        dialog.dismiss();
                                                    })
                                            .setNeutralButton(R.string.dialog_ok,
                                                    (dialog, which) -> dialog.dismiss())
                                            .create().show();
                                }
                            } catch (final JSONException e) {
                                e.printStackTrace();
                                new AlertDialog.Builder(CONTEXT)
                                        .setTitle(R.string.dialog_json_exception)
                                        .setMessage(e.getMessage() + "\n\n" + response.toString())
                                        .setNeutralButton(R.string.dialog_back,
                                                (dialog, which) -> dialog.dismiss())
                                        .create().show();
                            }
                        },
                        error -> new AlertDialog.Builder(CONTEXT)
                                .setTitle(R.string.dialog_volley_error)
                                .setMessage("> " + URL + "\n> " + error + "\n"
                                        + new String(error.networkResponse.data))
                                .setNeutralButton(R.string.dialog_back,
                                        (dialog, which) -> dialog.dismiss())
                                .create().show()
                ));
    }
}
