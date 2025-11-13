/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.finacial.wealth.api.fxpeer.exchange.inter.airtime.transaction;

/**
 *
 * @author olufemioshin
 */
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class TopupPurchaseResponse {

    // Accept both classic and gateway-style error fields
    @JsonAlias("errno")
    private Integer errno;
    @JsonAlias("error")
    private String  error;

    @JsonAlias("sysErr")
    private Integer sysErr;
    @JsonAlias("sysId")
    private String  sysId;

    // Use wrappers for nullability
    private Long id;

    private Operator operator;   // nested object
    private String  product;
    private String  recipient;

    private Amount amount;       // nested object
    private String reference;

    private Pin pin;             // nested object
    private String instructions;

    private BigDecimal balance;  // "29.44"
    private Integer status;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Operator {
        private String id;           // "1"
        private String currency;     // "GBP"
        private String reference;    // "opRef123"
        private Boolean hint;        // false
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Amount {
        private BigDecimal user;     // "6.25"
        @JsonProperty("operator")
        private BigDecimal operatorAmount; // "5.00"
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Pin {
        private String number;       // "1234567890123456"
        private String serial;       // "ABCDE123456"
    }
}

