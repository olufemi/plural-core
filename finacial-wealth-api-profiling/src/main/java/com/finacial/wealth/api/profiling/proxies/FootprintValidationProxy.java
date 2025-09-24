/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.finacial.wealth.api.profiling.proxies;

import com.finacial.wealth.api.profiling.config.FootprinValidationtFeignConfig;
import java.util.Map;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 *
 * @author olufemioshin
 */


@FeignClient(name = "footprintValidationProxy", url = "${fin.wealth.foot.print.base.url}", configuration = FootprinValidationtFeignConfig.class)
public interface FootprintValidationProxy {

    @PostMapping("/onboarding/session/validate")
    String validateToken(@RequestBody Map<String, String> body);
}
