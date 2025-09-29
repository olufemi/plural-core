/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.financial.wealth.api.transactions.domain;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import lombok.Data;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

/**
 *
 * @author olufemioshin
 */
@Data
@Entity
public class UserNotification implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final String SEQ_NAME = "NOTIFICATION_PUSHED_SEQ";
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "pooled")
    @GenericGenerator(
            name = "pooled",
            strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
            parameters = {
                @Parameter(name = "sequence_name", value = SEQ_NAME),
                @Parameter(name = "initial_value", value = "300"),
                @Parameter(name = "increment_size", value = "1"),
                @Parameter(name = "optimizer", value = "pooled")
            }
    )

    private Long id;

    private String userId;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "notification_id")
    private NotificationPushed notification;

    /**
     * NEW, SENT, DELIVERED, READ, FAILED (you choose semantics)
     */
    private String status = "NEW";

    @Temporal(TemporalType.TIMESTAMP)
    private Date sentAt;
    @Temporal(TemporalType.TIMESTAMP)
    private Date deliveredAt;
    @Temporal(TemporalType.TIMESTAMP)
    private Date readAt;

    @Column(length = 1000)
    private String lastError;

}
