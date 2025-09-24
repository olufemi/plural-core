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
public class ProcLedgerRequestCreditOneTime {

    private BigDecimal kulFees;
    private BigDecimal swFees;
    private BigDecimal transAmount;
    private String phoneNumber;
    private String swRefrenceNumber;
    private String kulTransactionId;
    private String fundingType;
    private String narration;
}
