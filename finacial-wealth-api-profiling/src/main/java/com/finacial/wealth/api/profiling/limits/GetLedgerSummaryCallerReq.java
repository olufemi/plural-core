/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.finacial.wealth.api.profiling.limits;

import lombok.Data;

/**
 *
 * @author olufemioshin
 */
@Data
public class GetLedgerSummaryCallerReq {

    private String accountNumber;
    private String productCode;
    private String period;
}
