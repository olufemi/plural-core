package com.finacial.wealth.backoffice.configmanagement.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "app_config")
@Getter
@Setter
public class AppConfigEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "config_name")
    private String configName;

    @Column(name = "config_description")
    private String configDescription;

    @Lob
    @Column(name = "config_value", columnDefinition = "LONGTEXT")
    private String configValue;
}
