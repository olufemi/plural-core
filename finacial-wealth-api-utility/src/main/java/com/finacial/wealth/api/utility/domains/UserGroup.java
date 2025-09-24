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
public class UserGroup implements Serializable {

    @NotNull
    @Id
    private String userGroupId;
    @Column(unique = true)
    private String userGroupName;
    @Column(length = 1555)
    private String userGroupRoles;
    private String userGroupType;// Client, Admin, Customer
    private String createdBy;

    private String transactionId;

    @Column(insertable = true, updatable = false)
    private LocalDateTime Created;
    private LocalDateTime Modified;

    public UserGroup(String userGroupId, String userGroupName, String userGroupRoles, String createdBy) {

        this.userGroupId = userGroupId;
        this.userGroupName = userGroupName;
        this.userGroupRoles = userGroupRoles;
        this.transactionId = String.valueOf(GlobalMethods.generateTransactionId());
        this.createdBy = createdBy;

    }

    @PrePersist
    void onCreate() {
        this.setCreated(LocalDateTime.now());
        this.setModified(LocalDateTime.now());
    }

    @PreUpdate
    void onUpdate() {
        this.userGroupId = String.valueOf(GlobalMethods.generateNUBAN());
        this.transactionId = String.valueOf(GlobalMethods.generateTransactionId());
        this.setModified(LocalDateTime.now());
    }
}
