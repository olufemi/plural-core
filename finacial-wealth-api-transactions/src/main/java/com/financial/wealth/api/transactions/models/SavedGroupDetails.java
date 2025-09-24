/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.financial.wealth.api.transactions.models;

import java.math.BigDecimal;
import java.util.List;
import lombok.Data;

/**
 *
 * @author olufemioshin
 */
@Data
public class SavedGroupDetails {

    private String transactionDate;
    private String groupSavingName;

    private String groupSavingDescription;

    //private int payOutSlot;
    
    private int[] adminPayOutSlot;

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
    private List<AddMembersModels> addMembersModels;
    private int[] availablePayOutSlot;
    private List<AddMembersModelsOthers> addMembersModelsOthers;

}
