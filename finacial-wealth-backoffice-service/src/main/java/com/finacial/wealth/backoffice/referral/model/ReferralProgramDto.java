package com.finacial.wealth.backoffice.referral.model;

import java.math.BigDecimal;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReferralProgramDto {

    private Long id;
    private String programCode;
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
    private String status;
    private Date startAt;
    private Date endAt;
    private String createdBy;
    private Date createdAt;
    private String updatedBy;
    private Date updatedAt;
}
