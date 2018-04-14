package com.example.niklasm.iliasbuddy;

import android.content.DialogInterface;
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
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

public class SetupActivity extends AppCompatActivity {
    EditText rssUrl;
    EditText rssUserName;
    EditText rssPassword;
    SharedPreferences myPrefs;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                backToMainActivity();
            }
        });

        // set fab on click listener
        findViewById(R.id.fabSetup).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveInput();
                backToMainActivity();
            }
        });

        myPrefs = getSharedPreferences("myPrefs", MODE_PRIVATE);

        rssUrl = findViewById(R.id.url);
        rssUserName = findViewById(R.id.userName);
        rssPassword = findViewById(R.id.password);

        final String iliasRssUrl = myPrefs.getString(getString(R.string.ilias_url), "nothing_found");
        final String iliasRssUserName = myPrefs.getString(getString(R.string.ilias_user_name), "nothing_found");
        final String iliasRssPassword = myPrefs.getString(getString(R.string.ilias_password), "nothing_found");

        if (!iliasRssUrl.equals("nothing_found")) rssUrl.setText(iliasRssUrl);
        if (!iliasRssUserName.equals("nothing_found")) rssUserName.setText(iliasRssUserName);
        if (!iliasRssPassword.equals("nothing_found")) rssPassword.setText(iliasRssPassword);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_setup, menu);
        return true;
    }

    public void openHelp(MenuItem menu) {
        AlertDialog alertDialog = new AlertDialog.Builder(SetupActivity.this)
                .setTitle(R.string.help_instructions)
                .setMessage(Html.fromHtml("<p>To use this service you need to visit Ilias and click on the orange <b>RSS</b> button to get to the <a href='https://ilias3.uni-stuttgart.de/ilias.php?view=0&col_side=right&block_type=pdnews&cmd=showFeedUrl&cmdClass=ilpdnewsblockgui&cmdNode=sh:6b:rv&baseClass=ilPersonalDesktopGUI#il_mhead_t_focus'>Ilias news feed</a>.</p><p>From there you need to copy the private feed url and replace the <b>password</b> with a password you set in the <a href='https://ilias3.uni-stuttgart.de/ilias.php?view=0&col_side=right&block_type=pdnews&cmd=editSettings&cmdClass=ilpdnewsblockgui&cmdNode=sh:6b:rv&baseClass=ilPersonalDesktopGUI#il_mhead_t_focus'>Ilias settings</a> (you get there by clicking the settings symbol at the top right of the same card you just clicked the orange RSS button.</p><p>Then insert the url in the first text field and your username (The name string in the url without any spaces) and your just set password and click the check button.</p>"))
                .setNeutralButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .create();
        alertDialog.show();
        ((TextView) alertDialog.findViewById(android.R.id.message)).setMovementMethod(LinkMovementMethod.getInstance());
    }

    public void backToMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    public void saveInput() {
        final SharedPreferences.Editor e = myPrefs.edit();
        e.putString(getString(R.string.ilias_url), rssUrl.getText().toString());
        e.putString(getString(R.string.ilias_user_name), rssUserName.getText().toString());
        e.putString(getString(R.string.ilias_password), rssPassword.getText().toString());
        e.apply();
    }
}
