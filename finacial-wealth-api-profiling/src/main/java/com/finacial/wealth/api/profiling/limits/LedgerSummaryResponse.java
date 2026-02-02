/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.finacial.wealth.api.profiling.limits;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author olufemioshin
 */
// --- Response DTO returned by Ledger -> Profiling
public class LedgerSummaryResponse implements Serializable {
private int statusCode;
private String description;
private List<LedgerPeriodSummary> periods;


public LedgerSummaryResponse() {
this.periods = new ArrayList<LedgerPeriodSummary>();
}


public int getStatusCode() { return statusCode; }
public void setStatusCode(int statusCode) { this.statusCode = statusCode; }


public String getDescription() { return description; }
public void setDescription(String description) { this.description = description; }


public List<LedgerPeriodSummary> getPeriods() { return periods; }
public void setPeriods(List<LedgerPeriodSummary> periods) { this.periods = periods; }


}
