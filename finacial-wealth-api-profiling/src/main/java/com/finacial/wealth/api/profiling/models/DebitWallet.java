/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.finacial.wealth.api.profiling.models;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 *
 * @author olufemioshin
 */
@Data
public class DebitWallet {

    private String fees;
    // private BigDecimal swFees;
    private String transAmount;

    private String finalCHarges;

    private String phoneNumber;
    //private String swRefrenceNumber;
    private String transactionId;
    private String transType;
    private String narration;
    private String productCode;
    private String itsPayOutTransaction;

}
