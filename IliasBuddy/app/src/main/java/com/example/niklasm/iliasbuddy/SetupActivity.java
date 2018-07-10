package com.example.niklasm.iliasbuddy;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;

import java.util.Objects;

/**
 * Check for a better way to save the password: https://stackoverflow.com/questions/9233035/best-option-to-store-username-and-password-in-android-app
 */
public class SetupActivity extends AppCompatActivity {
    EditText rssUrl;
    EditText rssUserName;
    EditText rssPassword;
    SharedPreferences myPrefs;


    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final ActionBar actionBar = getSupportActionBar();
        Objects.requireNonNull(actionBar).setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);

        // set fab on click listener
        findViewById(R.id.fabSetup).setOnClickListener(view -> {
            myPrefs.edit()
                    .putString(getString(R.string.ilias_url), rssUrl.getText().toString())
                    .putString(getString(R.string.ilias_user_name), rssUserName.getText().toString())
                    .putString(getString(R.string.ilias_password), rssPassword.getText().toString())
                    .apply();
            startActivity(new Intent(SetupActivity.this, MainActivity.class));
        });

        rssUrl = findViewById(R.id.url);
        rssUserName = findViewById(R.id.userName);
        rssPassword = findViewById(R.id.password);

        myPrefs = getSharedPreferences("myPrefs", Context.MODE_PRIVATE);

        rssUrl.setText(myPrefs.getString(getString(R.string.ilias_url), ""));
        rssUserName.setText(myPrefs.getString(getString(R.string.ilias_user_name), ""));
        rssPassword.setText(myPrefs.getString(getString(R.string.ilias_password), ""));
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_setup, menu);
        return true;
    }

    public void openHelp(final MenuItem menu) {
        final AlertDialog alertDialog = new AlertDialog.Builder(SetupActivity.this)
                .setTitle(R.string.help_instructions)
                .setMessage(Html.fromHtml(getString(R.string.popup_help_html_content)))
                .setNeutralButton(R.string.word_ok, (dialog, which) -> dialog.dismiss())
                .show();
        ((TextView) Objects.requireNonNull(alertDialog
                .findViewById(android.R.id.message)))
                .setMovementMethod(LinkMovementMethod.getInstance());
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
