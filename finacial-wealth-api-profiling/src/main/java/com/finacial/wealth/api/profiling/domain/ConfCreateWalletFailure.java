/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.finacial.wealth.api.profiling.domain;

import com.finacial.wealth.api.profiling.utils.GlobalMethods;
import java.io.Serializable;
import java.time.LocalDateTime;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.PrePersist;
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
public class ConfCreateWalletFailure implements Serializable {

    @NotNull
    @Id
    private String id;

    private String transactionId;

    private String phoneNumber;

    private String description;
    @Column(insertable = true, updatable = false)
    private LocalDateTime Created;

    public ConfCreateWalletFailure(String transactionId, String phoneNumber, String description) {
        this.id = String.valueOf(GlobalMethods.generateTransactionId());
        this.transactionId = transactionId;
        this.phoneNumber = phoneNumber;
        this.description = description;
    }

    @PrePersist
    void onCreate() {
        this.setCreated(LocalDateTime.now());

    }
}
