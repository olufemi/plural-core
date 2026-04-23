package com.financial.wealth.api.transactions.models;

import io.swagger.annotations.ApiModelProperty;
import javax.validation.constraints.NotNull;
import lombok.Data;

@Data
public class BatchPostingLegRequest {

    @ApiModelProperty(notes = "The posting direction: DEBIT or CREDIT")
    @NotNull(message = "the field \"direction\" is not nillable")
    private String direction;

    @ApiModelProperty(notes = "Optional reference override for the leg")
    private String requestRef;

    @ApiModelProperty(notes = "Logical user type or GL tag for the leg")
    private String userType;

    @ApiModelProperty(notes = "The Fees")
    @NotNull(message = "the field \"fees\" is not nillable")
    private String fees;

    @ApiModelProperty(notes = "The TransAmount")
    @NotNull(message = "the field \"transAmount\" is not nillable")
    private String transAmount;

    @ApiModelProperty(notes = "The FinalCHarges")
    @NotNull(message = "the field \"finalCHarges\" is not nillable")
    private String finalCHarges;

    @ApiModelProperty(notes = "The Phone-Number / Account Number")
    @NotNull(message = "the field \"phoneNumber\" is not nillable")
    private String phoneNumber;

    @ApiModelProperty(notes = "The TransactionId")
    @NotNull(message = "the field \"transactionId\" is not nillable")
    private String transactionId;

    @ApiModelProperty(notes = "The Narration")
    @NotNull(message = "the field \"narration\" is not nillable")
    private String narration;

    @ApiModelProperty(notes = "Optional auth hint carried from caller")
    private String auth;
}
