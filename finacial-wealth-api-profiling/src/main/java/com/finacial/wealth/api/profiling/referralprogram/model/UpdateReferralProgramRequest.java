package com.finacial.wealth.api.profiling.referralprogram.model;

import java.math.BigDecimal;
import java.util.Date;
import lombok.Data;

@Data
public class UpdateReferralProgramRequest {
    private String title;
    private String description;
    private String productType;
    private String rewardTarget;
    private String rewardMode;
    private BigDecimal rewardValue;
    private String rewardCurrencyMode;
    private String fixedCurrencyCode;
    private BigDecimal minQualifyingAmount;
    private BigDecimal minRewardAmount;
    private BigDecimal maxRewardAmount;
    private Integer qualifyingTransactionCount;
    private Date startAt;
    private Date endAt;
}
