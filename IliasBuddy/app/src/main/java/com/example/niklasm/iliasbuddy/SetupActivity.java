package com.example.niklasm.iliasbuddy;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Patterns;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Objects;

/**
 * Check for a better way to save the password: https://stackoverflow.com/questions/9233035/best-option-to-store-username-and-password-in-android-app
 */
public class SetupActivity extends AppCompatActivity {

    final static public String ILIAS_PRIVATE_RSS_FEED_URL = "ilias_url";
    final static public String ILIAS_PRIVATE_RSS_FEED_USER = "ilias_user_name";
    final static public String ILIAS_PRIVATE_RSS_FEED_PASSWORD = "ilias_password";
    public static final String ILIAS_PRIVATE_RSS_FEED_CREDENTIALS = "myPrefs";

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final ActionBar actionBar = getSupportActionBar();
        Objects.requireNonNull(actionBar).setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);

        final Context CONTEXT = this;
        final AutoCompleteTextView rssUrl = findViewById(R.id.url);
        final EditText rssUserName = findViewById(R.id.userName);
        final EditText rssPassword = findViewById(R.id.password);
        final View fabButton = findViewById(R.id.fabSetup);

        final SharedPreferences PREFERENCES =
                getSharedPreferences(SetupActivity.ILIAS_PRIVATE_RSS_FEED_CREDENTIALS,
                        Context.MODE_PRIVATE);
        rssUrl.setText(
                PREFERENCES.getString(SetupActivity.ILIAS_PRIVATE_RSS_FEED_URL, ""));
        rssUserName.setText(
                PREFERENCES.getString(SetupActivity.ILIAS_PRIVATE_RSS_FEED_USER, ""));
        rssPassword.setText(
                PREFERENCES.getString(SetupActivity.ILIAS_PRIVATE_RSS_FEED_PASSWORD, ""));

        rssUrl.setValidator(new AutoCompleteTextView.Validator() {
            @Override
            public boolean isValid(final CharSequence charSequence) {
                // check if input is a web url
                return Patterns.WEB_URL.matcher(charSequence.toString()).matches();
            }

            @Override
            public CharSequence fixText(final CharSequence invalidText) {
                // show toast id url is not valid
                Toast.makeText(CONTEXT,
                        R.string.setup_activity_input_ilias_private_rss_feed_url_is_invalid_message,
                        Toast.LENGTH_LONG).show();
                return invalidText;
            }
        });
        rssUrl.setOnFocusChangeListener((v, focused) -> {
            // if view is not focused perform validation
            if (!focused) {
                ((AutoCompleteTextView) v).performValidation();
            }
        });

        // set fab on click listener
        fabButton.setOnClickListener(view -> {
            if (rssUrl.getValidator().isValid(rssUrl.getText().toString())) {
                PREFERENCES.edit()
                        .putString(SetupActivity.ILIAS_PRIVATE_RSS_FEED_URL,
                                rssUrl.getText().toString())
                        .putString(SetupActivity.ILIAS_PRIVATE_RSS_FEED_USER,
                                rssUserName.getText().toString())
                        .putString(SetupActivity.ILIAS_PRIVATE_RSS_FEED_PASSWORD,
                                rssPassword.getText().toString())
                        .apply();
                startActivity(new Intent(SetupActivity.this, MainActivity.class));
            } else {
                Snackbar.make(fabButton,
                        R.string.setup_activity_input_ilias_private_rss_feed_url_is_invalid_message,
                        Snackbar.LENGTH_INDEFINITE).show();
            }
        });

        final Intent intent = getIntent();
        if (intent != null) {
            final String errorTitle = intent.getStringExtra(MainActivity.ERROR_MESSAGE_WEB_TITLE);
            final String errorMsg = intent.getStringExtra(MainActivity.ERROR_MESSAGE_WEB_MESSAGE);
            if (errorTitle != null && errorMsg != null) {
                Snackbar.make(fabButton, errorTitle, Snackbar.LENGTH_LONG)
                        .setActionTextColor(Color.RED) //to change the color of action text
                        .setAction(R.string.dialog_more, view ->
                                new AlertDialog.Builder(this)
                                        .setTitle(errorTitle)
                                        .setMessage(errorMsg)
                                        .setNeutralButton(R.string.dialog_ok,
                                                (dialog, which) -> dialog.dismiss())
                                        .create()
                                        .show())
                        .show();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_setup, menu);
        return true;
    }

    public void openHelp(final MenuItem menu) {
        final AlertDialog alertDialog = new AlertDialog.Builder(SetupActivity.this)
                .setTitle(R.string.setup_activity_toolbar_action_help_instructions)
                .setMessage(Html.fromHtml(getString(R.string.setup_activity_toolbar_action_help_instructions_popup_html_content)))
                .setNeutralButton(R.string.dialog_ok, (dialog, which) -> dialog.dismiss())
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
