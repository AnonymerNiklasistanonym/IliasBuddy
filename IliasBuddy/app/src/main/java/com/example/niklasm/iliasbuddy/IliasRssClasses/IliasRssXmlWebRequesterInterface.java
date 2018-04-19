package com.example.niklasm.iliasbuddy.IliasRssClasses;

import com.android.volley.AuthFailureError;
import com.android.volley.VolleyError;

public interface IliasRssXmlWebRequesterInterface {
    void processIliasXml(String xmlData);

    void webAuthenticationError(AuthFailureError authenticationError);

    void webResponseError(VolleyError responseError);
}
