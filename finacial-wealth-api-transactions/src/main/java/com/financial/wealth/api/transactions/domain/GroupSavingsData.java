/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.financial.wealth.api.transactions.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.financial.wealth.api.transactions.enumm.ContributionFrequency;
import com.financial.wealth.api.transactions.enumm.PayoutPolicy;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;
import lombok.Data;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

/**
 *
 * @author olufemioshin
 */
@Entity
@Table(name = "GROUP_SAVINGS_DATA")
@Data
public class GroupSavingsData implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final String SEQ_NAME = "GROUP_SAVINGS_DATA_SEQ";
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "pooled")
    @GenericGenerator(name = "pooled",
            strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
            parameters = {
                @Parameter(name = "sequence_name", value = SEQ_NAME),
                @Parameter(name = "initial_value", value = "300"),
                @Parameter(name = "increment_size", value = "1"),
                @Parameter(name = "optimizer", value = "pooled")
            }
    )

    Long id;

    private String groupSavingName;
    @Column(columnDefinition = "TEXT")
    private String formerMembersLog;

    private String groupSavingDescription;

    private int adminPayOutSlot;
    private int[] availablePayOutSlot;

    private BigDecimal groupSavingAmount;
    private BigDecimal groupSavingFinalAmount;

    private String allowPublicToJoin;

    private String transactionId;
    private String transactionStatus;
    private String transactionStatusDesc;
    private String numberOfMembers;
    private String inviteCode;
    private String transactionIdLink;
    private String isTrnsactionDeleted;
    private String isTrnsactionDeletedDesc;
    @Lob
    @Column(name = "ADDED_MEMBERS_MODELS", length = 300000)
    private String addedMembersModels;
    private String emailAddress;
    private String phoneNumber;
    private String walletId;

    // legacy monthly
    private String payOutDateOfTheMonth;
    private int cycleNumber;
    private String contributionDate;       // due/anchor day for the cycle
    private String contributionWindowEnd;  // last acceptable day to pay for that cycle
    private String payoutDate;           // when payout is marked (may be null until finalization)

    // NEW:
    @Enumerated(EnumType.STRING)
    private ContributionFrequency contributionFrequency;

    @Enumerated(EnumType.STRING)
    private PayoutPolicy payoutPolicy;

    /*private LocalDate startDate;                 // anchors schedule
    private Integer contributionDayOfWeek;       // 1..7 for weekly/biweekly
    private Integer contributionDayOfMonth;      // 1..28 for monthly/quarterly
    private LocalDate firstPayoutDate;*/
    @CreatedDate
    @Column(name = "CREATED_DATE", nullable = false, updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    @JsonIgnore
    private Instant createdDate;
    @LastModifiedDate
    @Column(name = "LAST_MODIFIED_DATE", insertable = false, columnDefinition = "TIMESTAMP")
    @JsonIgnore
    private Instant lastModifiedDate;

}
