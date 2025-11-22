/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.finacial.wealth.api.fxpeer.exchange.investment.record;

import java.math.BigDecimal;
import java.util.Optional;

/**
 *
 * @author olufemioshin
 */
public record WalletSummary(
        BigDecimal currentBalance,
        BigDecimal scheduledContributionsTotal
) {
    public BigDecimal availableToInvest() {
        return currentBalance.subtract(
            Optional.ofNullable(scheduledContributionsTotal).orElse(BigDecimal.ZERO)
        );
    }
}

