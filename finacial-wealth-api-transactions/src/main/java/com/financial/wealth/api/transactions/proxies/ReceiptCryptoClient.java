/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.financial.wealth.api.transactions.proxies;

/**
 *
 * @author olufemioshin
 */
import com.financial.wealth.api.transactions.models.ReceiptSignRequest;
import com.financial.wealth.api.transactions.models.ReceiptSignResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "fxpeer-exchange-service", path = "/canonical/crypto")
public interface ReceiptCryptoClient {

    @PostMapping("/sign")
    ReceiptSignResponse sign(@RequestBody ReceiptSignRequest req);
}