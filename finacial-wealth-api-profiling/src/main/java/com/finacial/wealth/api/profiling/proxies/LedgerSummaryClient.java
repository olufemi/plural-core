/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.finacial.wealth.api.profiling.proxies;

import com.finacial.wealth.api.profiling.models.LedgerSummaryResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 *
 * @author olufemioshin
 */
@FeignClient(name = "ledger-service")
public interface LedgerSummaryClient {

    @GetMapping("/v2/ledger/summary")
    LedgerSummaryResponse summary(
            @RequestParam("accountNumber") String accountNumber,
            @RequestParam("productCode") String productCode,
            @RequestParam("period") String period
    );
}
