package com.yyy.facedemo.been;

import com.google.gson.annotations.SerializedName;

public class Tokens {
    @SerializedName("refresh_token")
    public String refresh_token;
    @SerializedName("expires_in")
    public String expires_in;
    @SerializedName("session_key")
    public String session_key;
    @SerializedName("access_token")
    public String access_token;
    @SerializedName("scope")
    public String scope;
    @SerializedName("session_secret")
    public String session_secret;

}
