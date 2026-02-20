/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.finacial.wealth.api.sessionmanager.proxy;

/**
 *
 * @author olufemioshin
 */
import com.finacial.wealth.api.sessionmanager.response.ReceiptKeysResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(name = "fxpeer-exchange-service", path = "/canonical/crypto")
public interface ReceiptKeysClient {

    @GetMapping("/keys")
    ReceiptKeysResponse getReceiptKeys();
}
