/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.financial.wealth.api.transactions.breezepay.payout;

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
 * @author olufemioshin
 */
@Entity
@Data
@NoArgsConstructor
public class PaymentsFailedTransInfo implements Serializable {

    @NotNull
    @Id
    private String id;

    private String actionType;

    private String actionTypeDesc;

    private String transactionId;

    private String userId;

    private String channel;

    private String source;

    @Column(insertable = true, updatable = false)
    private LocalDateTime Created;
    private LocalDateTime Modified;

    public PaymentsFailedTransInfo(String actionType, String actionTypeDesc, String transactionId, String userId, String channel, String source
    ) {
        this.setId(String.valueOf(GlobalMethods.generateTransactionId()));
        this.actionType = actionType;
        this.transactionId = transactionId;
        this.userId = userId;
        this.actionTypeDesc = actionTypeDesc;
        this.channel = channel;
        this.source = source;

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
