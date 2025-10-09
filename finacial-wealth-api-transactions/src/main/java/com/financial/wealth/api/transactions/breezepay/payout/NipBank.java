/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.financial.wealth.api.transactions.breezepay.payout;

/**
 *
 * @author olufemioshin
 */
import javax.persistence.*;
import java.time.LocalDateTime;
import lombok.Data;

@Entity
@Data
@Table(name = "nip_banks",
       uniqueConstraints = @UniqueConstraint(name = "UK_NIP_BANKS_CODE", columnNames = "bank_code"))
public class NipBank {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "bank_code", nullable = false, length = 8)
    private String bankCode;

    @Column(name = "bank_name", nullable = false, length = 160)
    private String bankName;

    @Column(name = "source", length = 32)
    private String source;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) createdAt = LocalDateTime.now();
        if (source == null) source = "ACCESS_NIP_API";
    }

    // getters/setters
}
