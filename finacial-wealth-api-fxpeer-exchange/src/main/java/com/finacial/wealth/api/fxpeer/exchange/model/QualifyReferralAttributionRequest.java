package com.finacial.wealth.api.fxpeer.exchange.model;

import java.math.BigDecimal;

public class QualifyReferralAttributionRequest {

    private String productType;
    private String transactionId;
    private String correlationId;
    private BigDecimal transactionAmount;
    private String tradeCurrencyCode;
    private Integer completedTransactionCount;

    public String getProductType() {
        return productType;
    }

    public void setProductType(String productType) {
        this.productType = productType;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }

    public BigDecimal getTransactionAmount() {
        return transactionAmount;
    }

    public void setTransactionAmount(BigDecimal transactionAmount) {
        this.transactionAmount = transactionAmount;
    }

    public String getTradeCurrencyCode() {
        return tradeCurrencyCode;
    }

    public void setTradeCurrencyCode(String tradeCurrencyCode) {
        this.tradeCurrencyCode = tradeCurrencyCode;
    }

    public Integer getCompletedTransactionCount() {
        return completedTransactionCount;
    }

    public void setCompletedTransactionCount(Integer completedTransactionCount) {
        this.completedTransactionCount = completedTransactionCount;
    }
}
