/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.finacial.wealth.backoffice.audit.entity;

/**
 *
 * @author olufemioshin
 */


import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
@Entity
@Table(name = "bo_admin_audit_log", indexes = {
        @Index(name = "idx_audit_created_at", columnList = "createdAt"),
        @Index(name = "idx_audit_actor", columnList = "actorAdminId"),
        @Index(name = "idx_audit_target", columnList = "targetType,targetId")
})
public class AdminAuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long actorAdminId;

    @Column(nullable = false, length = 64)
    private String action; // e.g. ADMIN_CREATE, ADMIN_DEACTIVATE

    @Column(nullable = false, length = 64)
    private String targetType; // e.g. "BoAdminUser"

    private Long targetId;

    @Column(columnDefinition = "json")
    private String metadataJson; // safe, minimal details (no secrets)

    @Column(length = 64)
    private String ip;

    @Column(length = 255)
    private String userAgent;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    void prePersist() {
        if (createdAt == null) createdAt = Instant.now();
    }

    // Make “immutable” in practice: do not expose repository save for updates; never update.
}

