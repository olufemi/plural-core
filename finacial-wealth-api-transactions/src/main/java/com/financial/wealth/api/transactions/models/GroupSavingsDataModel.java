/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.financial.wealth.api.transactions.models;

import java.math.BigDecimal;

import lombok.Data;

/**
 *
 * @author olufemioshin
 */
@Data
public class GroupSavingsDataModel {

    private String groupSavingName;
    private String transactionDate;

    private String groupSavingDescription;

    private int adminPayOutSlot;
    private int[] availablePayOutSlot;

    private BigDecimal groupSavingAmount;
    private BigDecimal groupSavingFinalAmount;

    private String allowPublicToJoin;

    private String payOutDateOfTheMonth;

    private String transactionId;
    private String transactionStatus;
    private String transactionStatusDesc;
    private String numberOfMembers;
    private String inviteCode;
    private String transactionIdLink;
    private String isTrnsactionDeleted;
    private String isTrnsactionDeletedDesc;

    private String addedMembersModels;
    private String emailAddress;
    private String phoneNumber;
    private String walletId;
}
