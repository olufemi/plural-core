/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.finacial.wealth.api.profiling.models.accounts;

/**
 *
 * @author olufemioshin
 */
// ValidationResponse.java
public class ValidationResponse {

    private int statusCode;          // 200, 400, 500
    private String statusDescription; // e.g. "OK", "Invalid country pair", "Server error"
    private boolean valid;            // true if (code,name) matches a row
    private String hint;              // optional message to guide client

    public ValidationResponse() {
    }

    public ValidationResponse(int statusCode, String statusDescription, boolean valid, String hint) {
        this.statusCode = statusCode;
        this.statusDescription = statusDescription;
        this.valid = valid;
        this.hint = hint;
    }

    public static ValidationResponse ok() {
        return new ValidationResponse(200, "OK", true, null);
    }

    public static ValidationResponse bad(String desc, String hint) {
        return new ValidationResponse(400, desc, false, hint);
    }

    public static ValidationResponse error(String desc) {
        return new ValidationResponse(500, desc, false, null);
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getStatusDescription() {
        return statusDescription;
    }

    public boolean isValid() {
        return valid;
    }

    public String getHint() {
        return hint;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public void setStatusDescription(String statusDescription) {
        this.statusDescription = statusDescription;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public void setHint(String hint) {
        this.hint = hint;
    }
}
