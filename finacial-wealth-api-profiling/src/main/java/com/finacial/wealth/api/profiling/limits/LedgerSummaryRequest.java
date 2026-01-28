/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.finacial.wealth.api.profiling.limits;

/**
 *
 * @author olufemioshin
 */
import java.io.Serializable;

import java.util.List;

// --- Request DTO sent from Profiling -> Ledger
public class LedgerSummaryRequest implements Serializable {

    private String accountNumber;
    private String productCode;

// If client wants both SEND and RECEIVE tabs, profiling can request both credit+debit in one call.
// Periods requested (DAILY/WEEKLY/MONTHLY etc). Ledger returns aggregates per period.
    private List<LedgerPeriodQuery> periods;

    public LedgerSummaryRequest() {
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getProductCode() {
        return productCode;
    }

    public void setProductCode(String productCode) {
        this.productCode = productCode;
    }

    public List<LedgerPeriodQuery> getPeriods() {
        return periods;
    }

    public void setPeriods(List<LedgerPeriodQuery> periods) {
        this.periods = periods;
    }

    public static LedgerSummaryRequest of(String accountNumber, String productCode, List<LedgerPeriodQuery> periods) {
        LedgerSummaryRequest r = new LedgerSummaryRequest();
        r.setAccountNumber(accountNumber);
        r.setProductCode(productCode);
        r.setPeriods(periods);
        return r;
    }
}
