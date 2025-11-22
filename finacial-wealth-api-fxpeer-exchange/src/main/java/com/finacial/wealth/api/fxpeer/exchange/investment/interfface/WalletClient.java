/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.finacial.wealth.api.fxpeer.exchange.investment.interfface;

import com.finacial.wealth.api.fxpeer.exchange.investment.record.WalletSummary;
import java.math.BigDecimal;

/**
 *
 * @author olufemioshin
 */
public interface WalletClient {

    WalletSummary getContributionWalletSummary(String emailAddress, String walletId);

    void placeInvestmentHold(String walletId, String orderRef, BigDecimal amount);

    void releaseInvestmentHold(String walletId, String orderRef);

    void confirmInvestmentDebit(String walletId, String orderRef);

    void creditInvestmentLiquidation(String walletId, String orderRef, BigDecimal netAmount);
}

