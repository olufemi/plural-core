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
public class UserRoles implements Serializable {

  
    private String roleId;
    @NotNull
    @Id
    private String userRole;
  
    private String userRoleDec;
   
    private String transactionId;
      private String createdBy;

    @Column(insertable = true, updatable = false)
    private LocalDateTime Created;
    private LocalDateTime Modified;

    public UserRoles(String userRole, String userRoleDec,String createdBy) {

        this.roleId = String.valueOf(GlobalMethods.generateNUBAN());
        this.userRole = userRole;
        this.userRoleDec = userRoleDec;
        this.transactionId = this.transactionId = String.valueOf(GlobalMethods.generateTransactionId());
        this.createdBy =createdBy;
    }

    @PrePersist
    void onCreate() {

        this.setCreated(LocalDateTime.now());
        this.setModified(LocalDateTime.now());
    }

    @PreUpdate
    void onUpdate() {
        this.roleId = String.valueOf(GlobalMethods.generateNUBAN());
        this.transactionId = this.transactionId = String.valueOf(GlobalMethods.generateTransactionId());
        this.setModified(LocalDateTime.now());
    }

}
