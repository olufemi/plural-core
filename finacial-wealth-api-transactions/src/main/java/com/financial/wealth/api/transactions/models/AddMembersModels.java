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
public class AddMembersModels {

    private String memberId;
    private String memberIdType;
    private String memberName;
    private String memberUserId;
    //private String memberType;
    private String invitationCodeReqId;
    private String adminEmailAddress;
    private String memberEmailAddress;
    private String isAdmin;
    private int slot;
    private String payOutStatus;
    private String payOutStatusDesc;
    private String memberJoined;
    private String currentMonthContributionStatusId;
    private String currentMonthContributionStatusDesc;
    private BigDecimal currentMonthContribution;
    private BigDecimal totalMonthlyContribution;
    private BigDecimal amountToContribute;
    private SwapSlot swapSlot;
    

}
