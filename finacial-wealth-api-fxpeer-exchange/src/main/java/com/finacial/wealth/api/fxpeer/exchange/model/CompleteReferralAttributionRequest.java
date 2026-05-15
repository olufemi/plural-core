package com.finacial.wealth.api.fxpeer.exchange.model;

public class CompleteReferralAttributionRequest {

    private Boolean referrerRewardPaid;
    private Boolean refereeRewardPaid;
    private String referrerPayoutReference;
    private String refereePayoutReference;

    public Boolean getReferrerRewardPaid() {
        return referrerRewardPaid;
    }

    public void setReferrerRewardPaid(Boolean referrerRewardPaid) {
        this.referrerRewardPaid = referrerRewardPaid;
    }

    public Boolean getRefereeRewardPaid() {
        return refereeRewardPaid;
    }

    public void setRefereeRewardPaid(Boolean refereeRewardPaid) {
        this.refereeRewardPaid = refereeRewardPaid;
    }

    public String getReferrerPayoutReference() {
        return referrerPayoutReference;
    }

    public void setReferrerPayoutReference(String referrerPayoutReference) {
        this.referrerPayoutReference = referrerPayoutReference;
    }

    public String getRefereePayoutReference() {
        return refereePayoutReference;
    }

    public void setRefereePayoutReference(String refereePayoutReference) {
        this.refereePayoutReference = refereePayoutReference;
    }
}
