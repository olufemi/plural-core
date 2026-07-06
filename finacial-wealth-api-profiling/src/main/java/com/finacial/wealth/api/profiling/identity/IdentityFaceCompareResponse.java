package com.finacial.wealth.api.profiling.identity;

import java.math.BigDecimal;
import java.util.List;
import lombok.Data;

@Data
public class IdentityFaceCompareResponse {

    private boolean success;
    private String code;
    private String message;
    private String correlationId;
    private IdentityFaceCompareData data;

    @Data
    public static class IdentityFaceCompareData {
        private String verificationId;
        private BigDecimal similarityScore;
        private BigDecimal confidenceScore;
        private String matchOutcome;
        private String decision;
        private List<String> reasons;
        private String provider;
        private String providerReference;
    }
}
