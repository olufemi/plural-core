/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.financial.wealth.api.transactions.breezepay.payout;

/**
 *
 * @author olufemioshin
 */
import com.financial.wealth.api.transactions.utils.GlobalMethods;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import org.hibernate.validator.constraints.NotBlank;
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
public class WToBankTransfer implements Serializable {

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

    public WToBankTransfer(String transactionId, boolean isCompleted,
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
