package com.example.niklasm.iliasbuddy.IliasRssClasses;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.niklasm.iliasbuddy.R;

import java.util.HashMap;
import java.util.Map;

public class IliasRssXmlWebRequester {

    final private IliasRssXmlWebRequesterInterface classThatImplementsInterface;
    final private Context context;

    public IliasRssXmlWebRequester(IliasRssXmlWebRequesterInterface classThatImplementsInterface) {
        this.classThatImplementsInterface = classThatImplementsInterface;
        this.context = (Context) classThatImplementsInterface;
    }

    private String getSharedPreferenceStringOrNull(final SharedPreferences myPrefs, final int stringId) {
        return myPrefs.getString(context.getResources().getString(stringId), null);
    }

    private IliasRssCredentials getCredentialsAndUrl() {
        final SharedPreferences myPrefs = context.getSharedPreferences("myPrefs", Context.MODE_PRIVATE);
        final String iliasRssUrl = getSharedPreferenceStringOrNull(myPrefs, R.string.ilias_url);
        final String iliasRssUserName = getSharedPreferenceStringOrNull(myPrefs, R.string.ilias_user_name);
        final String iliasRssPassword = getSharedPreferenceStringOrNull(myPrefs, R.string.ilias_password);
        return new IliasRssCredentials(iliasRssUrl, iliasRssUserName, iliasRssPassword);
    }

    public void getWebContent() {

        final IliasRssCredentials CREDENTIALS = getCredentialsAndUrl();

        RequestQueue queue = Volley.newRequestQueue(this.context);

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, CREDENTIALS.url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                classThatImplementsInterface.processIliasXml(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error instanceof AuthFailureError) {
                    classThatImplementsInterface.webAuthenticationError((AuthFailureError) error);
                    return;
                }
                classThatImplementsInterface.webResponseError(error);
            }
        }) {
            @Override
            public Map<String, String> getHeaders() {
                final Map<String, String> headers = new HashMap<>();
                final String credentials = CREDENTIALS.getUserName() + ":" + CREDENTIALS.getPassword();
                final String auth = "Basic " + Base64.encodeToString(credentials.getBytes(), Base64.NO_WRAP);
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
