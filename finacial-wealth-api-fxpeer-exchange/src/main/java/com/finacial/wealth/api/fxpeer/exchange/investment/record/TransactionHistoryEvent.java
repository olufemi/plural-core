/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.finacial.wealth.api.fxpeer.exchange.investment.record;

/**
 *
 * @author olufemioshin
 */
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import lombok.Data;

@Data
public class TransactionHistoryEvent implements Serializable {
 private String walletNo;

    private String receiver;
    private String receiverName;

    private String senderName;
    private String sender;

    private String receiverBankName;
    private String receiverBankCode;

    private String transactionType;        // optional
    private String paymentType;

    private BigDecimal paymentTypeFeeCum;  // optional
    private BigDecimal cusPaymentTypeFeeCum; // optional

    private BigDecimal ammount;            // keep spelling for compatibility
    private BigDecimal ammountCum;         // optional
    private BigDecimal cusAmountCum;       // optional

    private BigDecimal fees;

    private String transactionId;          // IMPORTANT (idempotency key)

    private String sentAmount;             // if you must keep it, keep it (string)
    private String theNarration;

    private String sourceAccount;

    private String senderTransactionType;  // Withdrawal
    private String receiverTransactionType;// Deposit

    private String reversals;              // optional

    private String createdBy;
    private String emailAddress;

    private String requestType;

    private String airtimeReceiver;
    private String dataReceiver;
    private String billsToken;

    private String currencyCode;

    private Instant eventTime;  
}
