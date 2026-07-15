package com.finacial.wealth.backoffice.configmanagement.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
@Table(name = "bo_app_config_registry")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BoAppConfigRegistry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "config_name", nullable = false, unique = true)
    private String configName;

    @Column(name = "owner_service", nullable = false)
    private String ownerService;

    @Column(name = "value_type", nullable = false)
    private String valueType;

    @Column(name = "editable", nullable = false)
    private boolean editable;

    @Column(name = "is_sensitive", nullable = false)
    private boolean sensitive;

    @Column(name = "validation_regex")
    private String validationRegex;

    @Column(name = "description")
    private String description;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    void prePersist() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = Instant.now();
    }
}
