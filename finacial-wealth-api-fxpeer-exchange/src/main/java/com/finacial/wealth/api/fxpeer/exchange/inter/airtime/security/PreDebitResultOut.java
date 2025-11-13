/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.finacial.wealth.api.fxpeer.exchange.inter.airtime.security;

import java.math.BigDecimal;
import lombok.Data;

/**
 *
 * @author olufemioshin
 */
@Data
public class PreDebitResultOut {
    
        boolean success;

        // context
        String processId;
        String buyerAccountNumber;      // the setPhoneNumber you debited
        String glAccountDecrypted;      // decrypted GL account
        String sellerAccountNumber;     // AIRTIME_GGL_ACCOUNT
        String gglCode;                 // e.g., "NGN", used as auth/route key
        BigDecimal finCharges;          // buyer debit amount
        BigDecimal receiveAmount;       // amount moved through GL/seller

        // which legs applied
        boolean legBuyerDebited;        // debit CUSTOMER (buyer) - finCharges
        boolean legGLDebited;           // debit GL (buyer leg)    - receiveAmount
        boolean legSellerCredited;      // credit seller           - receiveAmount
        boolean legGLCredited;          // credit GL (seller leg)  - receiveAmount
}
