package com.finacial.wealth.api.profiling.market.dto;

import java.util.Map;
import lombok.Data;

@Data
public class AccountProvisionResult {
    private boolean provisioned;
    private String accountNumber;
    private String virtualAccountNumber;
    private String providerReference;
    private String status;
    private String message;
    private Map<String, Object> metadata;
}
