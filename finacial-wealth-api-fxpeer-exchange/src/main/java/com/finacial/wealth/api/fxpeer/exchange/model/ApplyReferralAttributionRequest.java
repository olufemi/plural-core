package com.finacial.wealth.api.fxpeer.exchange.model;

public class ApplyReferralAttributionRequest {

    private String productType;
    private String referralCode;

    public String getProductType() {
        return productType;
    }

    public void setProductType(String productType) {
        this.productType = productType;
    }

    public String getReferralCode() {
        return referralCode;
    }

    public void setReferralCode(String referralCode) {
        this.referralCode = referralCode;
    }
}
