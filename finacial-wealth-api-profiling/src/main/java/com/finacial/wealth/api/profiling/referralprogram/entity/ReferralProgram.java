package com.finacial.wealth.api.profiling.referralprogram.entity;

import com.finacial.wealth.api.profiling.referralprogram.ennum.ReferralProgramProductType;
import com.finacial.wealth.api.profiling.referralprogram.ennum.ReferralProgramRewardCurrencyMode;
import com.finacial.wealth.api.profiling.referralprogram.ennum.ReferralProgramRewardMode;
import com.finacial.wealth.api.profiling.referralprogram.ennum.ReferralProgramRewardTarget;
import com.finacial.wealth.api.profiling.referralprogram.ennum.ReferralProgramStatus;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import lombok.Data;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
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

@Entity
@Data
@Table(
        name = "referral_programs",
        uniqueConstraints = {
            @UniqueConstraint(name = "uq_referral_program_code", columnNames = {"program_code"})
        },
        indexes = {
            @Index(name = "idx_referral_program_product", columnList = "product_type"),
            @Index(name = "idx_referral_program_status", columnList = "status")
        }
)
public class ReferralProgram implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final String SEQ_NAME = "REFERRAL_PROGRAM_SEQ";

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

    @Column(name = "program_code", nullable = false, length = 80)
    private String programCode;

    @Column(nullable = false, length = 160)
    private String title;

    @Column(length = 800)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "product_type", nullable = false, length = 40)
    private ReferralProgramProductType productType;

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
    @Column(nullable = false, length = 30)
    private ReferralProgramStatus status;

    @Column(name = "start_at")
    private Date startAt;

    @Column(name = "end_at")
    private Date endAt;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    @Column(name = "created_at", nullable = false)
    private Date createdAt;

    @Column(name = "updated_by", length = 100)
    private String updatedBy;

    @Column(name = "updated_at")
    private Date updatedAt;
}
