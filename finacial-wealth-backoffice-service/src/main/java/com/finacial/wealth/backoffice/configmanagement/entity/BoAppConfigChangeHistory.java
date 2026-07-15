package com.finacial.wealth.backoffice.configmanagement.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "bo_app_config_change_history")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BoAppConfigChangeHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "app_config_id")
    private Long appConfigId;

    @Column(name = "config_name", nullable = false)
    private String configName;

    @Lob
    @Column(name = "old_value", columnDefinition = "LONGTEXT")
    private String oldValue;

    @Lob
    @Column(name = "new_value", columnDefinition = "LONGTEXT")
    private String newValue;

    @Column(name = "actor_admin_id")
    private Long actorAdminId;

    @Column(name = "reason")
    private String reason;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    void prePersist() {
        createdAt = Instant.now();
    }
}
