/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.finacial.wealth.api.fxpeer.exchange.inter.airtime.security;

import com.finacial.wealth.api.fxpeer.exchange.model.BaseResponse;
import java.math.BigDecimal;
import lombok.Data;

/**
 *
 * @author olufemioshin
 */
import java.math.BigDecimal;
import lombok.Data;

@Data
public class PreDebitResult {
    // overall
    private boolean success;
    private BaseResponse error;     // set when !success

    // context
    private String processId;
    private String buyerAccountNumber;   // the setPhoneNumber you debited
    private String glAccountDecrypted;   // decrypted GL account
    private String sellerAccountNumber;  // AIRTIME_GGL_ACCOUNT
    private String gglCode;              // e.g., "NGN", used as auth/route key

    // amounts
    private BigDecimal fees;             // fee amount
    private BigDecimal finCharges;       // buyer debit amount
    private BigDecimal receiveAmount;    // amount moved through GL/seller

    // which legs applied (used by rollback)
    private boolean legBuyerDebited;     // debit CUSTOMER (buyer) - finCharges
    private boolean legGLDebited;        // debit GL (buyer leg)    - receiveAmount
    private boolean legSellerCredited;   // credit seller           - receiveAmount
    private boolean legGLCredited;       // credit GL (seller leg)  - receiveAmount
}