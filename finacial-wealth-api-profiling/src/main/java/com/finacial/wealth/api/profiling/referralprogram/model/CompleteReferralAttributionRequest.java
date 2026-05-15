package com.finacial.wealth.api.profiling.referralprogram.model;

import lombok.Data;

@Data
public class CompleteReferralAttributionRequest {

    private Boolean referrerRewardPaid;
    private Boolean refereeRewardPaid;
    private String referrerPayoutReference;
    private String refereePayoutReference;
}
