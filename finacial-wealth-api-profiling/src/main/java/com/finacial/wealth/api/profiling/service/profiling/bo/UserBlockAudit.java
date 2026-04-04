/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.finacial.wealth.api.profiling.service.profiling.bo;

/**
 *
 * @author olufemioshin
 */


import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;
import java.io.Serializable;
import java.time.Instant;

@Entity
@Table(name = "USER_BLOCK_AUDIT")
@Data
@EqualsAndHashCode(of = "id")
public class UserBlockAudit implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "REG_WALLET_INFO_ID", nullable = false)
    private Long regWalletInfoId;

    @Column(name = "CUSTOMER_ID")
    private String customerId;

    @Column(name = "PREVIOUS_STATUS", length = 5)
    private String previousStatus;

    @Column(name = "NEW_STATUS", length = 5)
    private String newStatus;

    @Column(name = "ACTION", nullable = false, length = 20)
    private String action; // BLOCK / UNBLOCK

    @Column(name = "PERFORMED_BY")
    private String performedBy;

    @Column(name = "REASON", length = 500)
    private String reason;

    @Column(name = "ACTION_DATE", nullable = false)
    private Instant actionDate;
}
