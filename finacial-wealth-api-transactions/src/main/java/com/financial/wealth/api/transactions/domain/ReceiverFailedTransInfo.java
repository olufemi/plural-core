/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.financial.wealth.api.transactions.domain;

import com.financial.wealth.api.transactions.utils.GlobalMethods;
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
public class ReceiverFailedTransInfo implements Serializable {

    @NotNull
    @Id
    private String id;

    private String ActionType;

    private String ActionTypeDesc;

    private String TransactionId;

    private String PhoneNumber;

    private String RegPoint;

    private String RegPointCallerId;

    @Column(insertable = true, updatable = false)
    private LocalDateTime Created;
    private LocalDateTime Modified;

    public ReceiverFailedTransInfo(String ActionType, String ActionTypeDesc, String TransactionId, String RegPoint, String RegPointCallerId,
            String PhoneNumber
    ) {
        this.setId(String.valueOf(GlobalMethods.generateTransactionId()));
        this.ActionType = ActionType;

        this.TransactionId = TransactionId;
        this.PhoneNumber = PhoneNumber;

        this.ActionTypeDesc = ActionTypeDesc;
        this.RegPoint = RegPoint;
        this.RegPointCallerId = RegPointCallerId;

    }

    @PrePersist
    void onCreate() {
        this.setCreated(LocalDateTime.now());
        this.setModified(LocalDateTime.now());
    }

    @PreUpdate
    void onUpdate() {
        this.setModified(LocalDateTime.now());
    }
}
