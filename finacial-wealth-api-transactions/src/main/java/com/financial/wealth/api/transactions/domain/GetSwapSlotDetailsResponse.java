/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.financial.wealth.api.transactions.domain;

import lombok.Data;

/**
 *
 * @author olufemioshin
 */
@Data
public class GetSwapSlotDetailsResponse {

    private String memberId;
    private String memberType;
    private int senderSlot;
    private int receiverSlot;

    private String invitationCodeReqId;
    private String senderEmailAddress;
    private String senderFullName;
    private String receiverEmailAddress;
    private String isSlotSwappedStatus;
    private String notificationDate;
     private String groupName;

}
