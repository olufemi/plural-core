/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.finacial.wealth.api.profiling.models;

import java.util.List;

/**
 *
 * @author olufemioshin
 */
public class DecryptRequest {

    private List<String> fields;
    private String reason;

    // Constructors
    public DecryptRequest() {
    }

    public DecryptRequest(List<String> fields, String reason) {
        this.fields = fields;
        this.reason = reason;
    }

    // Getters and Setters
    public List<String> getFields() {
        return fields;
    }

    public void setFields(List<String> fields) {
        this.fields = fields;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
