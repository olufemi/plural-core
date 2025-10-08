/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.finacial.wealth.api.fxpeer.exchange.model;

import jakarta.validation.constraints.NotNull;

/**
 *
 * @author olufemioshin
 */
public class DebitWalletCaller {

    @NotNull(message = "the field \"fees\" is not nillable")
    private String fees;
    // private BigDecimal swFees;

    @NotNull(message = "the field \"transAmount\" is not nillable")
    private String transAmount;

    @NotNull(message = "the field \"finalCHarges\" is not nillable")
    private String finalCHarges;

    @NotNull(message = "the field \"phoneNumber\" is not nillable")
    private String phoneNumber;
    //private String swRefrenceNumber;

    @NotNull(message = "the field \"transactionId\" is not nillable")
    private String transactionId;

    @NotNull(message = "the field \"narration\" is not nillable")
    private String narration;

    @NotNull(message = "the field \"auth\" is not nillable")
    private String auth;

    public String getFees() {
        return fees;
    }

    public void setFees(String fees) {
        this.fees = fees;
    }

    public String getTransAmount() {
        return transAmount;
    }

    public void setTransAmount(String transAmount) {
        this.transAmount = transAmount;
    }

    public String getFinalCHarges() {
        return finalCHarges;
    }

    public void setFinalCHarges(String finalCHarges) {
        this.finalCHarges = finalCHarges;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getNarration() {
        return narration;
    }

    public void setNarration(String narration) {
        this.narration = narration;
    }

    public String getAuth() {
        return auth;
    }

    public void setAuth(String auth) {
        this.auth = auth;
    }

}
