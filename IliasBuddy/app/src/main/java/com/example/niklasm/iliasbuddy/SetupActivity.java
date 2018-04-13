package com.example.niklasm.iliasbuddy;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;

public class SetupActivity extends AppCompatActivity {
    EditText rssUrl;
    EditText rssUserName;
    EditText rssPassword;
    SharedPreferences myPrefs;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

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
