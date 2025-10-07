/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.financial.wealth.api.transactions.services.fx.p2.p2.wallet;

import java.math.BigDecimal;
import lombok.Data;

/**
 *
 * @author olufemioshin
 */
@Data
public class WalletInfoValAcctRes {

    private String currencyToSell;
    private String currencyToBuy;
    private BigDecimal balance;
    private BigDecimal available;
    private String walletId;
    private String accountNumber;
    private String correlatoionId;

}
