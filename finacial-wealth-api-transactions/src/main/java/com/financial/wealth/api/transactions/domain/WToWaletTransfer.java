/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.financial.wealth.api.transactions.domain;

import com.financial.wealth.api.transactions.utils.GlobalMethods;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.validation.constraints.NotBlank;

import javax.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * ≈xx
 *
 * @author OSHIN
 */
@Entity
@Data
@NoArgsConstructor
public class WToWaletTransfer implements Serializable {

    @Id
    @NotBlank
    private String transactionId;
    private String id;
    private boolean isCompleted;
    private String sender;

    private String receiver;

    private String receiverName;

    private BigDecimal amountToSend;

    private BigDecimal amountToBeDebited;

    private BigDecimal amountToBeCredited;

    private BigDecimal chargedAmount;
    @NotNull
    @NotBlank
    private String trasferType;//Direct-Trans
    @Column(insertable = true, updatable = false)
    private LocalDateTime Created;
    private LocalDateTime Modified;

    public WToWaletTransfer(String transactionId, boolean isCompleted,
            String sender, BigDecimal amountToSend, BigDecimal amountToBeDebited, String receiver,
            BigDecimal amountToBeCredited, BigDecimal chargedAmount,
            String description, String trasferType, String receiverName) {
        this.id = String.valueOf(GlobalMethods.generateTransactionId());
        this.transactionId = transactionId;
        this.sender = sender;
        this.receiver = receiver;
        this.amountToSend = amountToSend;

        this.trasferType = trasferType;
        this.amountToBeCredited = amountToBeCredited;
        this.trasferType = trasferType;
        this.amountToBeDebited = amountToBeDebited;

        this.isCompleted = isCompleted;
        this.chargedAmount = chargedAmount;
        this.receiverName = receiverName;

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
