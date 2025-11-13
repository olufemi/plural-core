/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.finacial.wealth.api.fxpeer.exchange.inter.airtime.getpack;

import lombok.Data;

/**
 *
 * @author olufemioshin
 */
/** Optional: model for constructing the signed GET request headers */
//@Data
public class SochitelSignatureHeaders {
    /** e.g., "clientportal.sochitel.com" */
    private String host;

    /** RFC1123 date, e.g., "Wed, 29 Oct 2025 12:55:49 +0000" */
    private String date;

    /** Unique per request */
    private String nonce;

    /** e.g., "SHA-256=47DEQpj8HBSa+/TImW+5JCeuQeRkm5NMpJWZG3hSuFU=" */
    private String digest;

    /** Full HTTP Signature header value */
    private String authorization;

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getNonce() {
        return nonce;
    }

    public void setNonce(String nonce) {
        this.nonce = nonce;
    }

    public String getDigest() {
        return digest;
    }

    public void setDigest(String digest) {
        this.digest = digest;
    }

    public String getAuthorization() {
        return authorization;
    }

    public void setAuthorization(String authorization) {
        this.authorization = authorization;
    }
    
    
    
}
