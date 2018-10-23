package com.niklasm.iliasbuddy.handler;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.example.niklasm.iliasbuddy.BuildConfig;
import com.example.niklasm.iliasbuddy.R;
import com.niklasm.iliasbuddy.miscellancellous.PopupDialog;

import org.json.JSONException;
import org.json.JSONObject;

public class IliasBuddyUpdateHandler {

    public static void checkForUpdate(final Context CONTEXT, final boolean ONLY_CHECK) {
        Volley.newRequestQueue(CONTEXT).add(new JsonArrayRequest(Request.Method.GET,
                IliasBuddyMiscellaneousHandler.GITHUB_REPOSITORY_RELEASES_API_URL, null,
                response -> {
                    try {
                        final JSONObject NEWEST_RELEASE = response.getJSONObject(0);
                        final String NEWEST_VERSION = NEWEST_RELEASE.getString("tag_name").substring(1);
                        final String NEWEST_VERSION_URL = NEWEST_RELEASE.getString("html_url");
                        if (NEWEST_VERSION.equals(BuildConfig.VERSION_NAME)) {
                            if (!ONLY_CHECK) {
                                PopupDialog.showOkDialog(CONTEXT,
                                        CONTEXT.getResources().getString(R.string.dialog_version_check_new_version_not_found),
                                        CONTEXT.getResources().getString(R.string.dialog_version_check_new_version_already_installed));
                            }
                        } else {
                            PopupDialog.showOkCustomDialog(CONTEXT,
                                    CONTEXT.getResources().getString(R.string.dialog_version_check_new_version_found),
                                    CONTEXT.getResources().getString(R.string.dialog_version_check_current_version) +
                                            ": " + BuildConfig.VERSION_NAME + "\n" +
                                            CONTEXT.getString(R.string.dialog_version_check_latest_version) +
                                            ": " + NEWEST_VERSION,
                                    CONTEXT.getString(R.string.dialog_version_check_open_release_page),
                                    (dialog, which) -> {
                                        IliasBuddyMiscellaneousHandler.openUrl(CONTEXT, NEWEST_VERSION_URL);
                                        dialog.dismiss();
                                    });
                        }
                    } catch (final JSONException e) {
                        e.printStackTrace();
                        PopupDialog.showBackDialog(CONTEXT,
                                CONTEXT.getResources().getString(R.string.dialog_error_json),
                                e.getMessage() + "\n\n" + response.toString());
                    }
                },
                error -> PopupDialog.showBackDialog(CONTEXT,
                        CONTEXT.getResources().getString(R.string.dialog_error_volley),
                        error + "\n\n" + new String(error.networkResponse.data))
        ));
    }
}
