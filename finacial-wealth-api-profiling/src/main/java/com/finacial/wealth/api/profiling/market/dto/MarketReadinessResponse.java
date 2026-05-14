package com.finacial.wealth.api.profiling.market.dto;

import java.util.Map;
import lombok.Data;

@Data
public class MarketReadinessResponse {
    private String marketCode;
    private String countryCode;
    private String currencyCode;
    private String status;
    private String nextAction;
    private String message;
    private String accountNumber;
    private String walletId;
    private String providerReference;
    private Boolean active;
    private Map<String, Object> metadata;
}
