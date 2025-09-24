/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.financial.wealth.api.transactions.models;

import io.swagger.annotations.ApiModelProperty;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.Data;

/**
 *
 * @author olufemioshin
 */
@Data
public class InitiateGroupSavings {

    @NotNull(message = "GroupSavingName is required!")
    private String groupSavingName;
    @NotNull(message = "GroupSavingDescription is required!")
    private String groupSavingDescription;
    /*@NotNull(message = "PayOutSlot is required!")
    private int payOutSlot;*/
    @NotNull(message = "GroupSavingAmount is required!")
    private String groupSavingAmount;
    @NotNull(message = "AllowPublicToJoin is required!")
    private boolean allowPublicToJoin;
    @NotBlank(message = "payOutDateOfTheMonth is required! Format: dd/mm/yyyy")
    private String payOutDateOfTheMonth;
    @NotNull(message = "NumberOfMembers is required!")
    private int numberOfMembers;
    @NotNull(message = "EmailAddress is required!")
    private String emailAddress;
    @ApiModelProperty(notes = "The Selected Slot")
    @NotNull(message = "the field \"selectedSlot\" is not nillable")
    private int selectedSlot;

}
