package com.example.niklasm.iliasbuddy;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;

public class SetupActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        FloatingActionButton fab = findViewById(R.id.fabSetup);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveInput();
                backToMainActivity();
            }
        });
    }

    public void backToMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    public void saveInput() {
        SharedPreferences myPrefs = getSharedPreferences("myPrefs", MODE_PRIVATE);
        SharedPreferences.Editor e = myPrefs.edit();

        EditText rssUrl = findViewById(R.id.url);
        EditText rssUserName = findViewById(R.id.userName);
        EditText rssPassword = findViewById(R.id.password);

        e.putString(getString(R.string.ilias_url), rssUrl.getText().toString());
        e.putString(getString(R.string.ilias_user_name), rssUserName.getText().toString());
        e.putString(getString(R.string.ilias_password), rssPassword.getText().toString());
        e.apply();
    }
}
