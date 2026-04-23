package com.finacial.wealth.api.fxpeer.exchange.model;

import lombok.Data;

@Data
public class BatchPostingLegRequest {
    private String direction;
    private String requestRef;
    private String userType;
    private String fees;
    private String transAmount;
    private String finalCHarges;
    private String phoneNumber;
    private String transactionId;
    private String narration;
    private String auth;
}
