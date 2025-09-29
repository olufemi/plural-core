/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.financial.wealth.api.transactions.models.tranfaar.outflow;

/**
 *
 * @author olufemioshin
 */
import com.financial.wealth.api.transactions.models.tranfaar.inflow.*;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class AcceptQuoteWithdrawalResponse {

    private boolean success;
    private String message;

    @JsonProperty("quote_id")
    private String quoteId; // use String for widest compatibility (can be UUID if you prefer)

    private String status; // ACCEPTED, etc.

    @JsonProperty("transaction_id")
    private String transactionId;

    @JsonProperty("transaction_reference")
    private String transactionReference;

    @JsonProperty("payment_instructions")
    private PaymentInstructions paymentInstructions;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PaymentInstructions {

        private String type;     // e.g., INTERAC
        private String email;
        private BigDecimal amount;    // "105.00" -> BigDecimal
        private String currency;

        @JsonProperty("security_question")
        private String securityQuestion;

        @JsonProperty("security_answer")
        private String securityAnswer;

        @JsonProperty("expires_at")
        private String expiresAt;     // keep as String for Java 8 compatibility; parse when needed
    }

}
