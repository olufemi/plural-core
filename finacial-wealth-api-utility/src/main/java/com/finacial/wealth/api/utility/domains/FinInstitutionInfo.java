/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.finacial.wealth.api.utility.domains;

import com.finacial.wealth.api.utility.utils.GlobalMethods;
import java.io.Serializable;
import java.time.LocalDateTime;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 *
 * @author OSHIN
 */
@Entity
@Data
@NoArgsConstructor
public class FinInstitutionInfo implements Serializable {

    @NotNull
    @Id
    private String finInstitutionId;

    private String finInstitutionTypeId;

    private String finInstitutionName;

    private String finInstitutionDesc;

    private String transactionId;

    private String phoneNumber;

    private String emailAddress;

    private String street;

    private String lga;

    private String stateOfResidence;

    private String nationality;

    @Column(insertable = true, updatable = false)
    private LocalDateTime Created;
    private LocalDateTime Modified;

    public FinInstitutionInfo(String finInstitutionTypeId, String finInstitutionName, String finInstitutionDesc, String phoneNumber,
            String emailAddress, String street, String lga, String stateOfResidence, String nationality) {
        this.finInstitutionId = String.valueOf(GlobalMethods.generateNUBAN());
        this.transactionId = String.valueOf(GlobalMethods.generateTransactionId());
        this.finInstitutionDesc = finInstitutionDesc;
        this.finInstitutionName = finInstitutionName;
        this.finInstitutionTypeId = finInstitutionTypeId;
        this.phoneNumber = phoneNumber;
        this.emailAddress = emailAddress;
        this.street = street;
        this.lga = lga;
        this.stateOfResidence = stateOfResidence;
        this.nationality = nationality;
    }

    @PrePersist
    void onCreate() {
        this.setCreated(LocalDateTime.now());
        this.setModified(LocalDateTime.now());
    }

    @PreUpdate
    void onUpdate() {
        this.finInstitutionId = String.valueOf(GlobalMethods.generateNUBAN());
        this.transactionId = String.valueOf(GlobalMethods.generateTransactionId());
        this.setModified(LocalDateTime.now());
    }

}
