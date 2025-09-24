/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.financial.wealth.api.transactions.tranfaar.services;

/**
 *
 * @author olufemioshin
 */
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class PaymentNotificationResponse {

    @JsonProperty("quote_id")
    private String quoteId;
    @JsonProperty("payment_type")
    private String paymentType; // e.g. DEPOSIT
    private String currency;
    private String email;
    private String status; // e.g. ACCEPTED
    private boolean success; // if you want this too
     private String description;
}
