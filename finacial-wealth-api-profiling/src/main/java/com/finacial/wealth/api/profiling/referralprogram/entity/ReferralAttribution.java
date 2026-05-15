package com.finacial.wealth.api.profiling.referralprogram.entity;

import com.finacial.wealth.api.profiling.referralprogram.ennum.ReferralAttributionStatus;
import com.finacial.wealth.api.profiling.referralprogram.ennum.ReferralProgramProductType;
import com.finacial.wealth.api.profiling.referralprogram.ennum.ReferralProgramRewardCurrencyMode;
import com.finacial.wealth.api.profiling.referralprogram.ennum.ReferralProgramRewardMode;
import com.finacial.wealth.api.profiling.referralprogram.ennum.ReferralProgramRewardTarget;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import lombok.Data;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

@Entity
@Data
@Table(
        name = "referral_attributions",
        uniqueConstraints = {
            @UniqueConstraint(name = "uq_referral_attr_referee_product", columnNames = {"referee_wallet_id", "product_type"})
        },
        indexes = {
            @Index(name = "idx_referral_attr_status", columnList = "status"),
            @Index(name = "idx_referral_attr_referee", columnList = "referee_wallet_id"),
            @Index(name = "idx_referral_attr_referrer", columnList = "referrer_wallet_id")
        }
)
public class ReferralAttribution implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final String SEQ_NAME = "REFERRAL_ATTRIBUTION_SEQ";

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "pooled")
    @GenericGenerator(name = "pooled", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator", parameters = {
        @Parameter(name = "sequence_name", value = SEQ_NAME),
        @Parameter(name = "initial_value", value = "1"),
        @Parameter(name = "increment_size", value = "1"),
        @Parameter(name = "optimizer", value = "pooled")
    })
    @Column(name = "ID")
    private Long id;

    @Column(name = "program_id", nullable = false)
    private Long programId;

    @Column(name = "program_code", nullable = false, length = 80)
    private String programCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "product_type", nullable = false, length = 40)
    private ReferralProgramProductType productType;

    @Column(name = "referrer_wallet_id", nullable = false, length = 120)
    private String referrerWalletId;

    @Column(name = "referrer_email", nullable = false, length = 160)
    private String referrerEmail;

    @Column(name = "referrer_name", length = 160)
    private String referrerName;

    @Column(name = "referrer_code", nullable = false, length = 120)
    private String referrerCode;

    @Column(name = "referee_wallet_id", nullable = false, length = 120)
    private String refereeWalletId;

    @Column(name = "referee_email", nullable = false, length = 160)
    private String refereeEmail;

    @Column(name = "referee_name", length = 160)
    private String refereeName;

    @Enumerated(EnumType.STRING)
    @Column(name = "reward_target", nullable = false, length = 40)
    private ReferralProgramRewardTarget rewardTarget;

    @Enumerated(EnumType.STRING)
    @Column(name = "reward_mode", nullable = false, length = 60)
    private ReferralProgramRewardMode rewardMode;

    @Column(name = "reward_value", nullable = false, precision = 19, scale = 4)
    private BigDecimal rewardValue;

    @Enumerated(EnumType.STRING)
    @Column(name = "reward_currency_mode", nullable = false, length = 40)
    private ReferralProgramRewardCurrencyMode rewardCurrencyMode;

    @Column(name = "fixed_currency_code", length = 10)
    private String fixedCurrencyCode;

    @Column(name = "min_qualifying_amount", precision = 19, scale = 4)
    private BigDecimal minQualifyingAmount;

    @Column(name = "min_reward_amount", precision = 19, scale = 4)
    private BigDecimal minRewardAmount;

    @Column(name = "max_reward_amount", precision = 19, scale = 4)
    private BigDecimal maxRewardAmount;

    @Column(name = "qualifying_transaction_count", nullable = false)
    private Integer qualifyingTransactionCount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private ReferralAttributionStatus status;

    @Column(name = "applied_at", nullable = false)
    private Date appliedAt;

    @Column(name = "qualified_at")
    private Date qualifiedAt;

    @Column(name = "rewarded_at")
    private Date rewardedAt;

    @Column(name = "qualified_transaction_id", length = 120)
    private String qualifiedTransactionId;

    @Column(name = "qualified_correlation_id", length = 120)
    private String qualifiedCorrelationId;

    @Column(name = "qualified_amount", precision = 19, scale = 4)
    private BigDecimal qualifiedAmount;

    @Column(name = "qualified_currency_code", length = 10)
    private String qualifiedCurrencyCode;

    @Column(name = "reward_currency_code", length = 10)
    private String rewardCurrencyCode;

    @Column(name = "referrer_reward_amount", precision = 19, scale = 4)
    private BigDecimal referrerRewardAmount;

    @Column(name = "referee_reward_amount", precision = 19, scale = 4)
    private BigDecimal refereeRewardAmount;

    @Column(name = "referrer_reward_paid")
    private Boolean referrerRewardPaid;

    @Column(name = "referee_reward_paid")
    private Boolean refereeRewardPaid;

    @Column(name = "referrer_payout_reference", length = 160)
    private String referrerPayoutReference;

    @Column(name = "referee_payout_reference", length = 160)
    private String refereePayoutReference;

    @Column(name = "created_at", nullable = false)
    private Date createdAt;

    @Column(name = "updated_at")
    private Date updatedAt;
}
