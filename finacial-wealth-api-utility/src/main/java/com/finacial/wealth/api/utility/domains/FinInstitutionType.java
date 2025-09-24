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
public class FinInstitutionType implements Serializable {

    @NotNull
    @Id
    private String finTypeId;
  
    private String institutionTypeName;
  
    private String institutionTypeDesc;
   
    private String transactionId;
    

    @Column(insertable = true, updatable = false)
    private LocalDateTime Created;
    private LocalDateTime Modified;

    public FinInstitutionType(String institutionTypeName, String institutionTypeDesc) {
        this.finTypeId = String.valueOf(GlobalMethods.generateNUBAN());
        this.institutionTypeName = institutionTypeName;
        this.institutionTypeDesc = institutionTypeDesc;
        this.transactionId = String.valueOf(GlobalMethods.generateTransactionId());
    }

    @PrePersist
    void onCreate() {
        this.setCreated(LocalDateTime.now());
        this.setModified(LocalDateTime.now());
    }

    @PreUpdate
    void onUpdate() {
        this.transactionId = String.valueOf(GlobalMethods.generateTransactionId());
        this.finTypeId = String.valueOf(GlobalMethods.generateNUBAN());
        this.setModified(LocalDateTime.now());
    }

}
