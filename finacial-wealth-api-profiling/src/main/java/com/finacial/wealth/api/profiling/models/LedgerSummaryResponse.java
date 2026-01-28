/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.finacial.wealth.api.profiling.models;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

/**
 *
 * @author olufemioshin
 */
@Data
public class LedgerSummaryResponse {

    private String accountNumber;
    private String productCode;
    private String period; // DAILY | WEEKLY | MONTHLY | YEARLY
    private LocalDateTime from;
    private LocalDateTime to;

    private Summary summary;

    @Data
    public static class Summary {

        private Side credit;
        private Side debit;
    }

    @Data
    public static class Side {

        private long count;
        private BigDecimal amount;
    }
}
