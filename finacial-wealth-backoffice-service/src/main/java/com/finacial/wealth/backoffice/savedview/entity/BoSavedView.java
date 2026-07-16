package com.finacial.wealth.backoffice.savedview.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Lob;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(
        name = "bo_saved_view",
        indexes = {
                @Index(name = "idx_bo_saved_view_admin_module", columnList = "admin_user_id,module_key"),
                @Index(name = "idx_bo_saved_view_default", columnList = "admin_user_id,module_key,default_view")
        }
)
@Getter
@Setter
public class BoSavedView {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "admin_user_id", nullable = false)
    private Long adminUserId;

    @Column(name = "module_key", nullable = false, length = 80)
    private String moduleKey;

    @Column(nullable = false, length = 120)
    private String name;

    @Lob
    @Column(name = "filters_json", nullable = false)
    private String filtersJson;

    @Column(name = "default_view", nullable = false)
    private boolean defaultView;

    @Column(name = "created_at", nullable = false)
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
