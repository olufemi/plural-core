/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.finacial.wealth.api.fxpeer.exchange.fx.p.p.wallet;

import java.math.BigDecimal;
import java.util.Date;
import lombok.Data;

/**
 *
 * @author olufemioshin
 */
@Data
public class WalletIndivTransactionsDetailsPojo {

    private String currencyToSell;
    private String currencyToBuy;
    private BigDecimal quantityPurchased;
    private BigDecimal availableQuantity;
    private BigDecimal totalQuantityCreated;
    private String sellerId;
    private String sellerName;
    private String accountNumber;
    private String correlationId;
    private String transactionId;
    private String buyerId;
    private String buyerAccount;
    private String buyerName;
    private BigDecimal receiverAmount;
    private String txnDateTime;   // <-- add this
    // getters/setters

   

}
