/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.finacial.wealth.backoffice.auth.dto;

/**
 *
 * @author olufemioshin
 */

public class MfaSetupResponse {
    private String qrDataUri;
    private String email;
    private String issuer;

    public MfaSetupResponse(String qrDataUri, String email, String issuer) {
        this.qrDataUri = qrDataUri;
        this.email = email;
        this.issuer = issuer;
    }

    public String getQrDataUri() { return qrDataUri; }
    public String getEmail() { return email; }
    public String getIssuer() { return issuer; }
}
