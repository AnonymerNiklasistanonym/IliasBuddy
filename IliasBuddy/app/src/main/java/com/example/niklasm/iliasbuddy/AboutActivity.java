package com.example.niklasm.iliasbuddy;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;

import com.google.android.gms.oss.licenses.OssLicensesMenuActivity;

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
                .addItem(new Element().setTitle(getString(R.string.word_license)).setOnClickListener(
                        view -> new AlertDialog.Builder(AboutActivity.this)
                                .setTitle(getString(R.string.word_license))
                                .setMessage(getString(R.string.about_app_license))
                                .setNeutralButton(getString(R.string.dialog_back),
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
