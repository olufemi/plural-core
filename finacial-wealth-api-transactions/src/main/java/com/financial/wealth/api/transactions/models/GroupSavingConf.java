/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
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
public class GroupSavingConf {

    @ApiModelProperty(notes = "The InvitationCodeReqId")
    @NotNull(message = "the field \"invitationCodeReqId\" is not nillable")
    private String invitationCodeReqId;
    @ApiModelProperty(notes = "The Email Address")
    @NotNull(message = "the field \"emailAddress\" is not nillable")
    private String emailAddress;
    @ApiModelProperty(notes = "The pin")
    @NotNull(message = "the field \"pin\" is not nillable")
    private String pin;
  
}
