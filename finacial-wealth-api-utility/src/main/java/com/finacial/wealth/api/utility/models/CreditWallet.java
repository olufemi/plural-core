/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.finacial.wealth.api.utility.models;

import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotNull;
import lombok.Data;

/**
 *
 * @author olufemioshin
 */
@Data
public class CreditWallet {

    @ApiModelProperty(notes = "The Fees")
    @NotNull(message = "the field \"fees\" is not nillable")
    private String fees;
    // private BigDecimal swFees;
    @ApiModelProperty(notes = "The TransAmount")
    @NotNull(message = "the field \"transAmount\" is not nillable")
    private String transAmount;

    @ApiModelProperty(notes = "The FinalCHarges")
    @NotNull(message = "the field \"finalCHarges\" is not nillable")
    private String finalCHarges;

    @ApiModelProperty(notes = "The Phone-Number")
    @NotNull(message = "the field \"phoneNumber\" is not nillable")
    private String phoneNumber;
    //private String swRefrenceNumber;
    @ApiModelProperty(notes = "The TransactionId")
    @NotNull(message = "the field \"transactionId\" is not nillable")
    private String transactionId;
    @ApiModelProperty(notes = "The TransType")
    @NotNull(message = "the field \"transType\" is not nillable")
    private String transType;
    @ApiModelProperty(notes = "The Narration")
    @NotNull(message = "the field \"narration\" is not nillable")
    private String narration;
    @ApiModelProperty(notes = "The ProductCode")
    @NotNull(message = "the field \"productCode\" is not nillable")
    private String productCode;
    // private String productName;
    // private String phoneNumberProductCode;

}
