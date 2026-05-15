package com.finacial.wealth.api.profiling.referralprogram.model;

import java.math.BigDecimal;
import lombok.Data;

@Data
public class QualifyReferralAttributionRequest {

    private String productType;
    private String transactionId;
    private String correlationId;
    private BigDecimal transactionAmount;
    private String tradeCurrencyCode;
    private Integer completedTransactionCount;
}
