package com.finacial.wealth.api.utility.utils;

import java.io.UnsupportedEncodingException;

import org.apache.commons.codec.binary.Base64;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class DecodedJWTToken {

    public String merchantId;
    public String bvn;
    public String emailAddress;
    public Boolean isStaff;
    public String organizationId;
    public String userId;
    public String phoneNumber;
    public String uniqueIdentificationNo;

    private static final String AUTHENTICATION_SCHEME = "Bearer";

    public static DecodedJWTToken getDecoded(String encodedToken) throws UnsupportedEncodingException {
        String token = encodedToken.substring(AUTHENTICATION_SCHEME.length()).trim();
        String[] pieces = token.split("\\.");
        String b64payload = pieces[1];
        String jsonString = new String(Base64.decodeBase64(b64payload), "UTF-8");
        return new Gson().fromJson(jsonString, DecodedJWTToken.class);
    }

    public String toString() {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return gson.toJson(this);
    }

}
