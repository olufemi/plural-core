/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.finacial.wealth.api.fxpeer.exchange.model;

/**
 *
 * @author olufemioshin
 */
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class SochitelAccountLookupResponse {
    private Integer errno;          // 0 == success
    private String error;           // "Success"
    private String accountId;       // "ctv123456"
    private String accountStatus;   // "VALID"
    private String customerNumber;  // "ctv123456 Doe"
    private String customerName;    // may be present per docs
}

