package com.niklasm.iliasbuddy;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Patterns;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;

import com.example.niklasm.iliasbuddy.R;
import com.niklasm.iliasbuddy.handler.IliasBuddyPreferenceHandler;
import com.niklasm.iliasbuddy.miscellancellous.PopupDialog;
import com.niklasm.iliasbuddy.miscellancellous.SnackBarDialog;
import com.niklasm.iliasbuddy.objects.IliasRssFeedCredentials;

import java.util.Objects;

/**
 * Check for a better way to save the password: https://stackoverflow.com/questions/9233035/best-option-to-store-username-and-password-in-android-app
 */
public class SetupActivity extends AppCompatActivity {

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final ActionBar actionBar = getSupportActionBar();
        Objects.requireNonNull(actionBar).setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);

        final AutoCompleteTextView rssUrl = findViewById(R.id.url);
        final EditText rssUserName = findViewById(R.id.userName);
        final EditText rssPassword = findViewById(R.id.password);
        final View fabButton = findViewById(R.id.fabSetup);

        final IliasRssFeedCredentials CREDENTIALS =
                IliasBuddyPreferenceHandler.getCredentials(this);
        rssUrl.setText(CREDENTIALS.getUserUrl());
        rssUserName.setText(CREDENTIALS.getUserName());
        rssPassword.setText(CREDENTIALS.getUserPassword());

        rssUrl.setValidator(new AutoCompleteTextView.Validator() {
            @Override
            public boolean isValid(final CharSequence charSequence) {
                // check if input is a web url
                return Patterns.WEB_URL.matcher(charSequence.toString()).matches();
            }

            @Override
            public CharSequence fixText(final CharSequence invalidText) {
                // show url is not valid
                SnackBarDialog.displayNormalSnackBar(fabButton,
                        getString(R.string.setup_activity_input_ilias_private_rss_feed_url_is_invalid_message),
                        true);
                return invalidText;
            }
        });
        rssUrl.setOnFocusChangeListener((view, focused) -> {
            // if view is not focused perform validation
            if (!focused) {
                ((AutoCompleteTextView) view).performValidation();
            }
        });

        // set fab on click listener
        fabButton.setOnClickListener(view -> {
            if (rssUrl.getValidator().isValid(rssUrl.getText().toString())) {
                IliasBuddyPreferenceHandler.saveCredentials(this,
                        rssUserName.getText().toString(), rssPassword.getText().toString(),
                        rssUrl.getText().toString());
                startActivity(new Intent(SetupActivity.this, MainActivity.class));
            } else {
                SnackBarDialog.displayNormalSnackBar(fabButton,
                        getString(R.string.setup_activity_input_ilias_private_rss_feed_url_is_invalid_message),
                        true);
            }
        });

        final Intent intent = getIntent();
        if (intent != null) {
            final String errorTitle = intent.getStringExtra(MainActivity.ERROR_MESSAGE_WEB_TITLE);
            final String errorMsg = intent.getStringExtra(MainActivity.ERROR_MESSAGE_WEB_MESSAGE);
            if (errorTitle != null && errorMsg != null) {
                SnackBarDialog.displayErrorSnackBar(this, fabButton, errorTitle, errorMsg);
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
        PopupDialog.showOkDialogSelectable(this,
                getString(R.string.setup_activity_toolbar_action_help_instructions),
                Html.fromHtml(getString(R.string.setup_activity_toolbar_action_help_instructions_popup_html_content)));
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
