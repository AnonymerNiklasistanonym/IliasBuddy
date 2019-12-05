package com.niklasm.iliasbuddy.feed_parser;

import android.content.Context;
import androidx.annotation.NonNull;
import android.util.Base64;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.niklasm.iliasbuddy.handler.IliasBuddyPreferenceHandler;
import com.niklasm.iliasbuddy.objects.IliasRssFeedCredentials;

import java.util.HashMap;
import java.util.Map;

public class IliasRssXmlWebRequester {

    @NonNull
    final private IliasRssXmlWebRequesterInterface CLASS_THAT_IMPLEMENTS_INTERFACE;
    @NonNull
    final private Context CONTEXT;

    public IliasRssXmlWebRequester(
            @NonNull final IliasRssXmlWebRequesterInterface CLASS_THAT_IMPLEMENTS_INTERFACE) {
        this.CLASS_THAT_IMPLEMENTS_INTERFACE = CLASS_THAT_IMPLEMENTS_INTERFACE;
        CONTEXT = (Context) CLASS_THAT_IMPLEMENTS_INTERFACE;
    }

    public void getWebContent() {

        // Get credentials from preferences
        final IliasRssFeedCredentials CREDENTIALS =
                IliasBuddyPreferenceHandler.getCredentials(CONTEXT);

        // Request a string response from the provided URL.
        Volley.newRequestQueue(CONTEXT).add(new StringRequest(Request.Method.GET,
                CREDENTIALS.getUserUrl(), CLASS_THAT_IMPLEMENTS_INTERFACE::processIliasXml,
                error -> {
                    if (error instanceof AuthFailureError) {
                        CLASS_THAT_IMPLEMENTS_INTERFACE
                                .webAuthenticationError((AuthFailureError) error);
                    } else {
                        CLASS_THAT_IMPLEMENTS_INTERFACE.webResponseError(error);
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() {
                final Map<String, String> headers = new HashMap<>();
                final String HEADER_CREDENTIALS = CREDENTIALS.getUserName() + ":" +
                        CREDENTIALS.getUserPassword();
                final String HEADER_AUTHENTICATION_BASE64 = "Basic " +
                        Base64.encodeToString(HEADER_CREDENTIALS.getBytes(), Base64.NO_WRAP);
                headers.put("Content-Type", "application/json; charset=UTF-8");
                headers.put("Authorization", HEADER_AUTHENTICATION_BASE64);
                return headers;
            }
        });
    }

}
