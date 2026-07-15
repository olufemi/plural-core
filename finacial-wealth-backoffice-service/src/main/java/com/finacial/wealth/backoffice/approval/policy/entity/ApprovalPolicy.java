package com.finacial.wealth.backoffice.approval.policy.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "bo_approval_policy")
@Getter
@Setter
@NoArgsConstructor
public class ApprovalPolicy {

    @Id
    @Column(name = "action_code", length = 128, nullable = false)
    private String actionCode;

    @Column(name = "module", length = 64, nullable = false)
    private String module;

    @Column(name = "sub_module", length = 64, nullable = false)
    private String subModule;

    @Column(name = "description", length = 255)
    private String description;

    @Column(name = "approval_required", nullable = false)
    private boolean approvalRequired;

    @Column(name = "threshold_amount", precision = 19, scale = 4)
    private BigDecimal thresholdAmount;

    @Column(name = "threshold_currency", length = 8)
    private String thresholdCurrency;

    @Column(name = "checker_permission", length = 128)
    private String checkerPermission;

    @Column(name = "sla_hours")
    private Integer slaHours;

    @Column(name = "active", nullable = false)
    private boolean active = true;

    @Column(name = "updated_by_admin_id")
    private Long updatedByAdminId;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    void prePersist() {
        Instant now = Instant.now();
        if (createdAt == null) {
            createdAt = now;
        }
        updatedAt = now;
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = Instant.now();
    }
}
