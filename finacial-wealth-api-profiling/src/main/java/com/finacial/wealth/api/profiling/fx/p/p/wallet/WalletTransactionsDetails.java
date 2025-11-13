/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.finacial.wealth.api.profiling.fx.p.p.wallet;

import com.finacial.wealth.api.profiling.domain.AbstractAuditingEntity;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.math.BigDecimal;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;


/**
 *
 * @author olufemioshin
 */
@Entity
//@Data
@Table(name = "Wallet_Trans_Details")
//@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class WalletTransactionsDetails extends AbstractAuditingEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final String SEQ_NAME = "WalletTransDetails_SEQ";
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "pooled")
    @GenericGenerator(name = "pooled",
            strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
            parameters = {
                @Parameter(name = "sequence_name", value = SEQ_NAME),
                @Parameter(name = "initial_value", value = "300"),
                @Parameter(name = "increment_size", value = "1"),
                @Parameter(name = "optimizer", value = "pooled")
            }
    )
    @Column(name = "ID")
    Long id;

    private String currencyToSell;
    private String currencyToBuy;
    private BigDecimal totalQuantityCreated;
    private BigDecimal availableQuantity;
    private String sellerId;
    private String sellerName;
    private String accountNumber;
    private String correlationId;

    private String buyerId;
    private String buyerAccount;
    private String buyerName;
    private String emailAddress;

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    //private BigDecimal amountPurchased;
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCurrencyToSell() {
        return currencyToSell;
    }

    public void setCurrencyToSell(String currencyToSell) {
        this.currencyToSell = currencyToSell;
    }

    public String getCurrencyToBuy() {
        return currencyToBuy;
    }

    public void setCurrencyToBuy(String currencyToBuy) {
        this.currencyToBuy = currencyToBuy;
    }

    public BigDecimal getTotalQuantityCreated() {
        return totalQuantityCreated;
    }

    public void setTotalQuantityCreated(BigDecimal totalQuantityCreated) {
        this.totalQuantityCreated = totalQuantityCreated;
    }

    public BigDecimal getAvailableQuantity() {
        return availableQuantity;
    }

    public void setAvailableQuantity(BigDecimal availableQuantity) {
        this.availableQuantity = availableQuantity;
    }

    public String getSellerId() {
        return sellerId;
    }

    public void setSellerId(String sellerId) {
        this.sellerId = sellerId;
    }

    public String getSellerName() {
        return sellerName;
    }

    public void setSellerName(String sellerName) {
        this.sellerName = sellerName;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }

    public String getBuyerId() {
        return buyerId;
    }

    public void setBuyerId(String buyerId) {
        this.buyerId = buyerId;
    }

    public String getBuyerAccount() {
        return buyerAccount;
    }

    public void setBuyerAccount(String buyerAccount) {
        this.buyerAccount = buyerAccount;
    }

    public String getBuyerName() {
        return buyerName;
    }

    public void setBuyerName(String buyerName) {
        this.buyerName = buyerName;
    }

}
