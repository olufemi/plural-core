/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.financial.wealth.api.transactions.proxies;

/**
 *
 * @author olufemioshin
 */
import com.financial.wealth.api.transactions.models.BaseResponse;
import com.financial.wealth.api.transactions.models.ConsentVerifyRequest;
import com.financial.wealth.api.transactions.models.ConsentVerifyResponse;
import com.financial.wealth.api.transactions.models.ReceiptSignRequest;
import com.financial.wealth.api.transactions.models.ReceiptSignResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "fxpeer-exchange-service")
public interface FxPeerClient {

    @PostMapping("/canonical/crypto/sign")
    ReceiptSignResponse sign(@RequestBody ReceiptSignRequest req);

    @PostMapping(
            value = "/canonical/crypto/consent/verify",
            consumes = "application/json"
    )
    BaseResponse verify(
            @RequestHeader("Authorization") String authorization,
            @RequestHeader("X-Device-Id") String deviceId,
            @RequestHeader("X-Device-Kid") String deviceKid,
            @RequestHeader("X-Consent-Ts") Long ts,
            @RequestHeader("X-Consent-Nonce") String nonce,
            @RequestHeader("X-Consent-Sig") String sigB64,
            @RequestBody ConsentVerifyRequest body
    );
}
