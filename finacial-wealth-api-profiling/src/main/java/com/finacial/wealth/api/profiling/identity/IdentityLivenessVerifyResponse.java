package com.finacial.wealth.api.profiling.identity;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import lombok.Data;

@Data
public class IdentityLivenessVerifyResponse {

    private UUID livenessSessionId;
    private String sessionReference;
    private BigDecimal livenessScore;
    private BigDecimal spoofScore;
    private BigDecimal confidenceScore;
    private String attackType;
    private String decision;
    private List<String> reasons;
    private String provider;
    private String providerReference;
}
