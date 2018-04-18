package com.example.niklasm.iliasbuddy;

import com.android.volley.AuthFailureError;
import com.android.volley.VolleyError;

public interface IliasXmlWebRequesterInterface {
    void processIliasXml(String xmlData);

    void webAuthenticationError(AuthFailureError authenticationError);

    void webResponseError(VolleyError responseError);
}
