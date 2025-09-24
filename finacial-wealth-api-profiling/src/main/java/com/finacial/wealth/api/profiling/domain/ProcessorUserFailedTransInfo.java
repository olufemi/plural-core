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
public class ProcessorUserFailedTransInfo implements Serializable {

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

    public ProcessorUserFailedTransInfo(String actionType, String actionTypeDesc, String transactionId, String userId, String channel, String source
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
