/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.finacial.wealth.api.profiling.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.Instant;
import java.time.LocalDateTime;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

/**
 *
 * @author olufemioshin
 */
@Entity
@Data
public class FootprintValidation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String fpId;
    private String onboardingId;
    private String status;
    private String token;

    private LocalDateTime createdAt = LocalDateTime.now();

    @Lob
    @Column(name = "FULL_RESPONSE", length = 100000)
    private String fullResponse;

    @CreatedDate
    @Column(name = "CREATED_DATE", nullable = false, updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    @JsonIgnore
    private Instant createdDate;

    @LastModifiedDate
    @Column(name = "LAST_MODIFIED_DATE", insertable = false, columnDefinition = "TIMESTAMP")
    @JsonIgnore
    private Instant lastModifiedDate;
}
