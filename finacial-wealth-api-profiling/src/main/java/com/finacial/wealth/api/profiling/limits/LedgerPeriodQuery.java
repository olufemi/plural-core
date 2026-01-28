/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.finacial.wealth.api.profiling.limits;

import java.io.Serializable;
import java.time.LocalDate;

/**
 *
 * @author olufemioshin
 */
// per-period query. Profiling usually calls for: TODAY, THIS_WEEK, THIS_MONTH, THIS_YEAR
// (calendar periods). Ledger calculates between startDate..endDate inclusive.
class LedgerPeriodQuery implements Serializable {

    private String code; // e.g. "DAILY", "WEEKLY", "MONTHLY", "YEARLY" (or TODAY/THIS_MONTH)
    private LocalDate startDate;
    private LocalDate endDate;

    public LedgerPeriodQuery() {
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public static LedgerPeriodQuery of(String code, LocalDate start, LocalDate end) {
        LedgerPeriodQuery q = new LedgerPeriodQuery();
        q.setCode(code);
        q.setStartDate(start);
        q.setEndDate(end);
        return q;
    }
}
