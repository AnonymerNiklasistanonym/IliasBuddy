package com.niklasm.iliasbuddy;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.view.MenuItem;

import com.example.niklasm.iliasbuddy.BuildConfig;
import com.example.niklasm.iliasbuddy.R;
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity;
import com.niklasm.iliasbuddy.handler.IliasBuddyMiscellaneousHandler;
import com.niklasm.iliasbuddy.handler.IliasBuddyUpdateHandler;

import mehdi.sakout.aboutpage.AboutPage;
import mehdi.sakout.aboutpage.Element;

public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // set content of activity to about page
        setContentView(new AboutPage(this)
                .setDescription(getString(R.string.about_activity_description))
                .addItem(new Element().setTitle(getString(R.string.about_activity_version) + " " + BuildConfig.VERSION_NAME))
                .addItem(new Element().setTitle(getString(R.string.about_activity_check_for_new_version)).setOnClickListener(
                        view -> IliasBuddyUpdateHandler.checkForUpdate(this, false)))
                .addItem(new Element().setTitle(getString(R.string.about_activity_license)).setOnClickListener(
                        view -> new AlertDialog.Builder(AboutActivity.this)
                                .setTitle(R.string.about_activity_license)
                                .setMessage(R.string.about_activity_license_text)
                                .setNeutralButton(R.string.dialog_back,
                                        (dialog, which) -> dialog.dismiss())
                                .create().show()))
                .addItem(new Element().setTitle(getString(R.string.about_activity_open_source_licenses)).setOnClickListener(
                        view -> {
                            OssLicensesMenuActivity.setActivityTitle(getString(R.string.about_activity_open_source_licenses));
                            startActivity(new Intent(AboutActivity.this,
                                    OssLicensesMenuActivity.class));
                        }))
                .addWebsite(IliasBuddyMiscellaneousHandler.GITHUB_REPOSITORY_URL,
                        getString(R.string.about_activity_ilias_buddy_repository_website))
                //.addPlayStore("com.ideashower.readitlater.pro") // later
                .addGitHub(IliasBuddyMiscellaneousHandler.GITHUB_USERNAME_AUTHOR,
                        IliasBuddyMiscellaneousHandler.GITHUB_USERNAME_AUTHOR + " " +
                                getString(R.string.about_activity_author_on_github))
                .create());

        // enable back button in action bar
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
