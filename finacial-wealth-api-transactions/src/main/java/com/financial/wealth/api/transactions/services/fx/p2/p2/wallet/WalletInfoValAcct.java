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
public class WalletInfoValAcct {

    private String currencyToSell;
    private String currencyToBuy;
    private String accountNumber;
    private String walletId;
    private BigDecimal transactionAmmount;
    private String correlationId;

}
