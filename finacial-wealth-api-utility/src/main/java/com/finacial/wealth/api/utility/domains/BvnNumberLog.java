/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.finacial.wealth.api.utility.domains;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.io.Serializable;
import java.time.Instant;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

/**
 *
 * @author olufemioshin
 */
@Entity
@Table(name = "REG_BVN_NUMBER_LOG")
@Data
public class BvnNumberLog implements Serializable {

    private static final String SEQ_NAME = "REG_WALLET_CHECK_SEQ";
    @NotNull
    @Id
    Long id;
    @Column(name = "BVN", unique = true)
    private String bvn;
    @Lob
    @Column(name = "BASE_64_IMAGE", length = 100000)
    private String base64Image;

    private String firstName;
    private String middleName;
    private String lastName;
    private String phoneNumber;
    private String phoneNumber2;
    private String dateOfBirth;
    private String registrationDate;
    private String email;
    private String gender;
    private String lgaOfOrigin;
    private String lgaOfResidence;
    private String maritalStatus;
    private String nationality;
    private String residentialAddress;
    private String stateOfOrigin;
    private String stateOfResidence;
    @Column(name = "REQUEST_ID")
    private String requestId;
    @CreatedDate
    @Column(name = "CREATED_DATE", nullable = false, updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    @JsonIgnore
    private Instant createdDate;
    @LastModifiedDate
    @Column(name = "LAST_MODIFIED_DATE", insertable = false, columnDefinition = "TIMESTAMP")
    @JsonIgnore
    private Instant lastModifiedDate;

}
