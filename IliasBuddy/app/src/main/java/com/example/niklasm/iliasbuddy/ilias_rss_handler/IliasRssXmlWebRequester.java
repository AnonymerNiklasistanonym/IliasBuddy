package com.example.niklasm.iliasbuddy.ilias_rss_handler;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.niklasm.iliasbuddy.SetupActivity;

import java.util.HashMap;
import java.util.Map;

public class IliasRssXmlWebRequester {

    final private IliasRssXmlWebRequesterInterface classThatImplementsInterface;
    final private Context CONTEXT;

    public IliasRssXmlWebRequester(
            final IliasRssXmlWebRequesterInterface classThatImplementsInterface) {
        this.classThatImplementsInterface = classThatImplementsInterface;
        CONTEXT = (Context) classThatImplementsInterface;
    }

    private IliasRssCredentials getCredentialsAndUrl() {
        final SharedPreferences myPrefs =
                CONTEXT.getSharedPreferences(SetupActivity.ILIAS_PRIVATE_RSS_FEED_CREDENTIALS,
                        Context.MODE_PRIVATE);
        final String iliasRssUrl =
                myPrefs.getString(SetupActivity.ILIAS_PRIVATE_RSS_FEED_URL, null);
        final String iliasRssUserName =
                myPrefs.getString(SetupActivity.ILIAS_PRIVATE_RSS_FEED_USER, null);
        final String iliasRssPassword =
                myPrefs.getString(SetupActivity.ILIAS_PRIVATE_RSS_FEED_PASSWORD, null);
        return new IliasRssCredentials(iliasRssUrl, iliasRssUserName, iliasRssPassword);
    }

    public void getWebContent() {

        final IliasRssCredentials CREDENTIALS = getCredentialsAndUrl();

        final RequestQueue queue = Volley.newRequestQueue(CONTEXT);

        // Request a string response from the provided URL.
        final StringRequest stringRequest = new StringRequest(Request.Method.GET, CREDENTIALS.url,
                classThatImplementsInterface::processIliasXml, error -> {
            if (error instanceof AuthFailureError) {
                classThatImplementsInterface.webAuthenticationError((AuthFailureError) error);
                return;
            }
            classThatImplementsInterface.webResponseError(error);
        }) {
            @Override
            public Map<String, String> getHeaders() {
                final Map<String, String> headers = new HashMap<>();
                final String credentials = CREDENTIALS.getUserName() + ":" +
                        CREDENTIALS.getPassword();
                final String auth = "Basic " + Base64.encodeToString(credentials.getBytes(),
                        Base64.NO_WRAP);
                headers.put("Content-Type", "application/json; charset=UTF-8");
                headers.put("Authorization", auth);
                return headers;
            }
        };
        // Add the request to the RequestQueue.
        queue.add(stringRequest);
    }

    final private class IliasRssCredentials {
        final String url;
        final String userName;
        final String password;

        IliasRssCredentials(final String url, final String userName, final String password) {
            this.url = url;
            this.userName = userName;
            this.password = password;
        }

        public String getUrl() {
            return url;
        }

        public String getUserName() {
            return userName;
        }

        public String getPassword() {
            return password;
        }
    }

}
