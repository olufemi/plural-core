/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.finacial.wealth.api.profiling.domain;

import java.time.LocalDateTime;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import lombok.Data;

/**
 *
 * @author olufemioshin
 */
@Entity
@Data
public class FootprintDecryptEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String fpId;
    private String dob;
    private String lastName;
    private String ssn4;
    private String firstName;
    private String country;
    public String mobile;
    public String email;
     private String middleName;
    

    @Column(columnDefinition = "TEXT")
    private String rawJson;

    private LocalDateTime createdAt = LocalDateTime.now();

    // Getters and setters
}
