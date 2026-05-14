package com.finacial.wealth.api.profiling.market.dto;

import java.util.Map;
import lombok.Data;

@Data
public class KycResolutionResult {
    private boolean satisfied;
    private String kycStatus;
    private String nextAction;
    private String message;
    private Map<String, Object> metadata;
}
