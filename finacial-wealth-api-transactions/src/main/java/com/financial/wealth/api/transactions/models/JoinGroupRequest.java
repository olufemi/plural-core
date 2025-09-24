/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.financial.wealth.api.transactions.models;

import lombok.Data;

/**
 *
 * @author olufemioshin
 */
@Data
public class JoinGroupRequest {

    private String memberId;
    private String memberType;
    private String invitationCodeReqId;
    private String memberEmailAddress;
    private int selectedSlot;
    private String pin;

}
