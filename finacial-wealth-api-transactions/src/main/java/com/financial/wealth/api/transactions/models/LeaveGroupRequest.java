/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.financial.wealth.api.transactions.models;

import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotNull;
import lombok.Data;

/**
 *
 * @author olufemioshin
 */
@Data
public class LeaveGroupRequest {

    @ApiModelProperty(notes = "The Email Address")
    @NotNull(message = "the field \"emailAddress\" is not nillable")
    private String memberEmailAddress;

    @NotNull(message = "Invitation code is required")
    private String invitationCodeReqId;

    @NotNull(message = "Pin is required")
    private String pin;

    private String memberId;

}
