/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.finacial.wealth.api.profiling.proxies;

import com.finacial.wealth.api.profiling.models.DecryptRequest;
import com.finacial.wealth.api.profiling.models.DecryptResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

/**
 *
 * @author olufemioshin
 */
@FeignClient(name = "frontPrintProxy", url = "${fin.wealth.foot.print.base.url}")
public interface FrontPrintProxy {

    @PostMapping(value = "/users/{userId}/vault/decrypt", consumes = MediaType.APPLICATION_JSON_VALUE)
    DecryptResponse decryptVaultFields(
            @RequestHeader("X-Footprint-Secret-Key") String secretKey,
            @PathVariable("userId") String userId,
            @RequestBody DecryptRequest request
    );

}
