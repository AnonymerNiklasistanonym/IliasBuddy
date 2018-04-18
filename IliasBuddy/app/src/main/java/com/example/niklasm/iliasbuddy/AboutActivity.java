package com.example.niklasm.iliasbuddy;

import android.content.DialogInterface;
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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        View aboutPage = new AboutPage(this)
                .isRTL(false)
                .setDescription("Description of the app")
                //.setImage(R.drawable.ic_launcher_foreground)
                .addItem(new Element().setTitle("Version " + BuildConfig.VERSION_NAME))
                .addItem(new Element().setTitle("License").setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        AlertDialog alertDialog = new AlertDialog.Builder(AboutActivity.this)
                                .setTitle("License")
                                .setMessage("MIT License\n" +
                                        "\n" +
                                        "Copyright (c) 2018 Niklas\n" +
                                        "\n" +
                                        "Permission is hereby granted, free of charge, to any person obtaining a copy\n" +
                                        "of this software and associated documentation files (the \"Software\"), to deal\n" +
                                        "in the Software without restriction, including without limitation the rights\n" +
                                        "to use, copy, modify, merge, publish, distribute, sublicense, and/or sell\n" +
                                        "copies of the Software, and to permit persons to whom the Software is\n" +
                                        "furnished to do so, subject to the following conditions:\n" +
                                        "\n" +
                                        "The above copyright notice and this permission notice shall be included in all\n" +
                                        "copies or substantial portions of the Software.\n" +
                                        "\n" +
                                        "THE SOFTWARE IS PROVIDED \"AS IS\", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR\n" +
                                        "IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,\n" +
                                        "FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE\n" +
                                        "AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER\n" +
                                        "LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,\n" +
                                        "OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE\n" +
                                        "SOFTWARE.")
                                .setNeutralButton("BACK",
                                        new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int which) {
                                                dialog.dismiss();
                                            }
                                        })
                                .create();
                        alertDialog.show();
                    }
                }))
                .addItem(new Element().setTitle("Open source licenses").setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        OssLicensesMenuActivity.setActivityTitle(getString(R.string.custom_license_title));
                        startActivity(new Intent(AboutActivity.this, OssLicensesMenuActivity.class));
                    }
                }))
                //.addGroup("Connect with us")
                //.addEmail("elmehdi.sakout@gmail.com")
                .addWebsite("https://github.com/AnonymerNiklasistanonym/IliasBuddy", "Visit website with source code and bug tracker")
                //.addFacebook("the.medy")
                //.addTwitter("medyo80")
                //.addYoutube("UCdPQtdWIsg7_pi4mrRu46vA")
                //.addPlayStore("com.ideashower.readitlater.pro")
                .addGitHub("AnonymerNiklasistanonym", "AnonymerNiklasistanonym on GitHub")
                .create();

        setContentView(aboutPage);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowHomeEnabled(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                super.onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
