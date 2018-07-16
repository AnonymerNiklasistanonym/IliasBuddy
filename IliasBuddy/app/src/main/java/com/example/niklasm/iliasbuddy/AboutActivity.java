package com.example.niklasm.iliasbuddy;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity;

import org.json.JSONException;
import org.json.JSONObject;

import mehdi.sakout.aboutpage.AboutPage;
import mehdi.sakout.aboutpage.Element;

public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final View aboutPage = new AboutPage(this)
                .isRTL(false)
                .setDescription(getString(R.string.about_app_description))
                .addItem(new Element().setTitle(getString(R.string.word_version) + " " + BuildConfig.VERSION_NAME))
                .addItem(new Element().setTitle(getString(R.string.word_check_for_new_version)).setOnClickListener(view -> {
                    final String URL = "https://api.github.com/repos/AnonymerNiklasistanonym/IliasBuddy/releases";
                    Volley.newRequestQueue(this).add(new JsonArrayRequest
                            (Request.Method.GET, URL, null,
                                    response -> {
                                        try {
                                            final JSONObject NEWEST_RELEASE = response.getJSONObject(0);
                                            final String NEWEST_VERSION = NEWEST_RELEASE.getString("tag_name").substring(1);
                                            final String NEWEST_VERSION_URL = NEWEST_RELEASE.getString("html_url");
                                            if (NEWEST_VERSION.equals(BuildConfig.VERSION_NAME)) {
                                                new AlertDialog.Builder(AboutActivity.this)
                                                        .setTitle(R.string.dialog_new_version_not_found)
                                                        .setMessage(R.string.dialog_new_version_already_installed)
                                                        .setNeutralButton(R.string.dialog_back,
                                                                (dialog, which) -> dialog.dismiss())
                                                        .create().show();
                                            } else {
                                                new AlertDialog.Builder(AboutActivity.this)
                                                        .setTitle(R.string.dialog_new_version_found)
                                                        .setMessage(getString(R.string.dialog_current_version) + ": " + BuildConfig.VERSION_NAME
                                                                + "\n" + getString(R.string.dialog_latest_version) + ": " + NEWEST_VERSION)
                                                        .setPositiveButton(R.string.dialog_new_version_open_release_page,
                                                                (dialog, which) -> {
                                                                    openUrl(NEWEST_VERSION_URL);
                                                                    dialog.dismiss();
                                                                })
                                                        .setNeutralButton(R.string.dialog_back,
                                                                (dialog, which) -> dialog.dismiss())
                                                        .create().show();
                                            }
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                            new AlertDialog.Builder(AboutActivity.this)
                                                    .setTitle(R.string.dialog_json_exception)
                                                    .setMessage(e.getMessage() + "\n\n" + response.toString())
                                                    .setNeutralButton(R.string.dialog_back,
                                                            (dialog, which) -> dialog.dismiss())
                                                    .create().show();
                                        }
                                    },
                                    error -> new AlertDialog.Builder(AboutActivity.this)
                                            .setTitle(R.string.dialog_volley_error)
                                            .setMessage("> " + URL + "\n> " + error + "\n"
                                                    + new String(error.networkResponse.data))
                                            .setNeutralButton(R.string.dialog_back,
                                                    (dialog, which) -> dialog.dismiss())
                                            .create().show()
                            ));
                }))
                .addItem(new Element().setTitle(getString(R.string.word_license)).setOnClickListener(
                        view -> new AlertDialog.Builder(AboutActivity.this)
                                .setTitle(R.string.word_license)
                                .setMessage(R.string.about_app_license)
                                .setNeutralButton(R.string.dialog_back,
                                        (dialog, which) -> dialog.dismiss())
                                .create().show()))
                .addItem(new Element().setTitle(getString(R.string.word_open_source_licenses)).setOnClickListener(
                        view -> {
                            OssLicensesMenuActivity.setActivityTitle(getString(R.string.word_open_source_licenses));
                            startActivity(new Intent(AboutActivity.this,
                                    OssLicensesMenuActivity.class));
                        }))
                .addWebsite("https://github.com/AnonymerNiklasistanonym/IliasBuddy",
                        getString(R.string.url_ilias_buddy_repository_title))
                //.addPlayStore("com.ideashower.readitlater.pro") // later
                .addGitHub("AnonymerNiklasistanonym", getString(R.string.author_github_user_name_title))
                .create();

        setContentView(aboutPage);

        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowHomeEnabled(true);
        }
    }

    /**
     * Open a website in the device browser
     *
     * @param URL (String) - Website link/address
     */
    private void openUrl(final String URL) {
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(URL)));
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                super.onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
