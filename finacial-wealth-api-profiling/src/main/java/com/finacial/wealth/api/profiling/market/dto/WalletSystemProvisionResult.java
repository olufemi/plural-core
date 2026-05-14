package com.finacial.wealth.api.profiling.market.dto;

import java.util.Map;
import lombok.Data;

@Data
public class WalletSystemProvisionResult {
    private boolean provisioned;
    private String walletId;
    private String status;
    private String message;
    private Map<String, Object> metadata;
}
