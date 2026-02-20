/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.financial.wealth.api.transactions.models;

import java.math.BigDecimal;
import java.time.Instant;
import lombok.Data;

/**
 *
 * @author olufemioshin
 */
@Data
public class FinWalletPaymentTransModel {

    private String receiver;
    private String receiverName;
    private String sender;
    private String senderName;
    private String transactionType;
    private String paymentType;
    private BigDecimal ammount;
    private BigDecimal fees;

    private String transactionId;

    private String transactionDate;
    private Instant createdDate;
    private String receiverBankName;
    private String receiverBankCode;
    private String theNarration;
    private String senderTransactionType;
    private String receiverTransactionType;
    private String currencyCode;
    private String status;           // add this (SUCCESS/FAILED/PENDING/REVERSED)
    private String receiptKid;       // add this
    private String receiptSignature; // add this
  

}
