/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.financial.wealth.api.transactions.services.grp.savings.wallet;

/**
 *
 * @author olufemioshin
 */
import javax.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(
        name = "wallet_txn",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_wallet_idemp",
                columnNames = {"wallet_id", "idempotency_ref", "type"}
        )
)
public class GroupSavingWalletTransaction {

    @Id
    @Column(length = 64)
    private String id;

    @Column(name = "wallet_id", nullable = false, length = 64)
    private String walletId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private GroupSavingWalletTxnType type;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private GroupSavingWalletTxnStatus status;

    @Column(name = "idempotency_ref", nullable = false, length = 100)
    private String idempotencyRef;
    private String walletPocRef;

    @Column(name = "error_message", length = 512)
    private String errorMessage;

    public String getWalletPocRef() {
        return walletPocRef;
    }

    public void setWalletPocRef(String walletPocRef) {
        this.walletPocRef = walletPocRef;
    }

    // getters/setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getWalletId() {
        return walletId;
    }

    public void setWalletId(String walletId) {
        this.walletId = walletId;
    }

    public GroupSavingWalletTxnType getType() {
        return type;
    }

    public void setType(GroupSavingWalletTxnType type) {
        this.type = type;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public GroupSavingWalletTxnStatus getStatus() {
        return status;
    }

    public void setStatus(GroupSavingWalletTxnStatus status) {
        this.status = status;
    }

    public String getIdempotencyRef() {
        return idempotencyRef;
    }

    public void setIdempotencyRef(String idempotencyRef) {
        this.idempotencyRef = idempotencyRef;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
