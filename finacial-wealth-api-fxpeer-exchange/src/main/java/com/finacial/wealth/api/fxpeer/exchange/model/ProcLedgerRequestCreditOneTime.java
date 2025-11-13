/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.finacial.wealth.api.fxpeer.exchange.model;

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