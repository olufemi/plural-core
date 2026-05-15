package com.finacial.wealth.api.profiling.referralprogram.entity;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;

@Entity
@Data
@Table(
        name = "referral_program_audit",
        indexes = {
            @Index(name = "idx_referral_program_audit_program", columnList = "program_id")
        }
)
public class ReferralProgramAudit implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @Column(name = "program_id", nullable = false)
    private Long programId;

    @Column(name = "event_type", nullable = false, length = 40)
    private String eventType;

    @Column(nullable = false, length = 100)
    private String actor;

    @Column(length = 800)
    private String note;

    @Column(name = "event_at", nullable = false)
    private Date eventAt;
}
