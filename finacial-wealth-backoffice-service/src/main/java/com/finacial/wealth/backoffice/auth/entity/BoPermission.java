package com.finacial.wealth.backoffice.auth.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "bo_permission")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BoPermission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 64)
    private String module;

    @Column(nullable = false, length = 64)
    private String subModule;

    @Column(nullable = false, length = 64)
    private String action;

    @Column(nullable = false, unique = true, length = 128)
    private String code;

    @Column(length = 255)
    private String description;

    public String getCode() {
        return code;
    }
}
