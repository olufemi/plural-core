package com.finacial.wealth.api.profiling.market.dto;

import java.util.Map;
import lombok.Data;

@Data
public class MarketReadinessRequest {
    private String customerId;
    private String emailAddress;
    private String phoneNumber;
    private String marketCode;
    private String countryCode;
    private String currencyCode;
    private String triggerSource;
    private String productType;
    private String productReference;
    private String initiatingService;
    private String correlationId;
    private Map<String, Object> metadata;
}
