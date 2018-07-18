package com.example.niklasm.iliasbuddy.rss_handler;

import android.content.Context;
import android.util.Base64;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.niklasm.iliasbuddy.handler.IliasBuddyPreferenceHandler;
import com.example.niklasm.iliasbuddy.objects.IliasRssFeedCredentials;

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

    public void getWebContent() {

        final IliasRssFeedCredentials CREDENTIALS =
                IliasBuddyPreferenceHandler.getCredentials(CONTEXT);

        final RequestQueue queue = Volley.newRequestQueue(CONTEXT);

        // Request a string response from the provided URL.
        final StringRequest stringRequest = new StringRequest(Request.Method.GET,
                CREDENTIALS.getUserUrl(), classThatImplementsInterface::processIliasXml, error -> {
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
                        CREDENTIALS.getUserPassword();
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

}
