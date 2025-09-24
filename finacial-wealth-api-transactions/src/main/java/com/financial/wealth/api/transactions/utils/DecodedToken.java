package com.financial.wealth.api.transactions.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.commons.codec.binary.Base64;

import java.io.UnsupportedEncodingException;

public class DecodedToken {

    public String sub;
    public String name;
    public String userId;
    public Boolean admin;
    public String customerId;
    public Long exp;
    private static final String AUTHENTICATION_SCHEME = "Bearer";

    public static DecodedToken getDecoded(String encodedToken) throws UnsupportedEncodingException {
        String token = encodedToken.substring(AUTHENTICATION_SCHEME.length()).trim();
        String[] pieces = token.split("\\.");
        String b64payload = pieces[1];
        String jsonString = new String(Base64.decodeBase64(b64payload), "UTF-8");
        return new Gson().fromJson(jsonString, DecodedToken.class);
    }

    public String toString() {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return gson.toJson(this);
    }

}
