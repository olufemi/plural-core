package com.finacial.wealth.backoffice.approval.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Lob;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
        name = "bo_approval_request",
        indexes = {
                @Index(name = "idx_bo_approval_status", columnList = "status,createdAt"),
                @Index(name = "idx_bo_approval_module", columnList = "module,subModule,status"),
                @Index(name = "idx_bo_approval_entity", columnList = "entityType,entityRef", unique = true)
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BoApprovalRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private ApprovalModule module;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private ApprovalSubModule subModule;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private ApprovalEntityType entityType;

    @Column(nullable = false, length = 128)
    private String entityRef;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private ApprovalActionType actionType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private ApprovalStatus status;

    private Long makerAdminId;
    private Long checkerAdminId;
    private Long currentAssigneeAdminId;

    @Column(length = 190)
    private String requesterEmail;

    @Column(length = 255)
    private String rejectionReason;

    @Column(length = 255)
    private String remediationNotes;

    @Lob
    private String payloadJson;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    private Instant submittedAt;
    private Instant approvedAt;
    private Instant rejectedAt;
    private Instant resubmittedAt;

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
