package com.finacial.wealth.api.fxpeer.exchange.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.UnsupportedEncodingException;
import lombok.Data;

import org.apache.tomcat.util.codec.binary.Base64;

@Data
public class DecodedJWTToken {

    public String merchantId;
    public String bvn;
    public String emailAddress;
    public Boolean isStaff;
    public String organizationId;
    public String userId;
    public String phoneNumber;
    public String accountNo;
    public String firstName;
    public String uniqueIdentificationNo;

    private static final String AUTHENTICATION_SCHEME = "Bearer";

    public static DecodedJWTToken getDecoded(String encodedToken) throws UnsupportedEncodingException {
        String token = encodedToken.substring(AUTHENTICATION_SCHEME.length()).trim();
        String[] pieces = token.split("\\.");
        String b64payload = pieces[1];
        String jsonString = new String(Base64.decodeBase64(b64payload), "UTF-8");
        return new Gson().fromJson(jsonString, DecodedJWTToken.class);
    }

    @Override
    public String toString() {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return gson.toJson(this);
    }

}
