package com.finacial.wealth.api.profiling.market.dto;

import lombok.Data;

@Data
public class AccountProvisionRequest {
    private String customerId;
    private String emailAddress;
    private String phoneNumber;
    private String marketCode;
    private String countryCode;
    private String currencyCode;
    private String correlationId;
}
