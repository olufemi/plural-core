/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.finacial.wealth.api.fxpeer.exchange.investment.record;

import com.finacial.wealth.api.fxpeer.exchange.investment.domain.InvestmentProduct;
import java.math.BigDecimal;
import lombok.Data;

/**
 *
 * @author olufemioshin
 */
@Data
public class ProcessDebitWalletForInvestment {

    private BigDecimal grossDebitAmount;
    private BigDecimal fees;
    private String idempotencyKey;     // e.g. from UI or UUID on client
    private String emailAddress;
    private String phoneNumber;
    private String currencyCode;

}
