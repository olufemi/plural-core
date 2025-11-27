/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.finacial.wealth.api.fxpeer.exchange.investment.record;

import java.math.BigDecimal;
import lombok.Data;

/**
 *
 * @author olufemioshin
 */
@Data
public class InvestmentProductRecord {

    private String productId;

    private String productCode;          // internal code

    private String name;

    private String InvestmentType;         // MUTUAL_FUND, BOND, etc.

    private String currency;             // "NGN", "GBP", ...

    private BigDecimal minimumInvestmentAmount;

    private BigDecimal unitPrice;        // optional, for unit-based products

    private BigDecimal yieldPa;          // Annual Yield (Per Annum Yield)

    private BigDecimal yieldYtd;         // Year-To-Date Yield

    private Integer tenorDays;           // optional

    private boolean active = true;

    private String partnerProductCode;   // identifier known by partner

    private BigDecimal percentageCurrValue;

    private Long tenorMinutes = 30L * 24L * 60L; // default 30 days in minutes (editable)

    private Boolean maturityAtEndOfDay = true; // optional behavior

}
