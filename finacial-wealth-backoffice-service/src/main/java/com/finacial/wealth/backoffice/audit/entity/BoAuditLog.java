package com.finacial.wealth.backoffice.audit.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "bo_audit_log")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class BoAuditLog {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private Long adminUserId;

  @Column(nullable = false, length = 120)
  private String action;

  private String entityType;
  private String entityId;

  private String requestId;
  private String ip;

  @Column(length = 255)
  private String userAgent;

  @Column(length = 255)
  private String reason;

  @Lob
  private String beforeJson;

  @Lob
  private String afterJson;

  @Column(nullable = false)
  private LocalDateTime createdAt;

  @PrePersist
  void prePersist() {
    createdAt = LocalDateTime.now();
  }
}
