package com.finacial.wealth.backoffice.integrations.fxpeer;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@FeignClient(name = "fxpeer-exchange-service", configuration = com.finacial.wealth.backoffice.config.FeignConfig.class)
public interface FxPeerExchangeClient {

  @PostMapping("/api/fxpeer-exchange/offers/update-offer")
  Map<String, Object> updateOffer(@RequestBody Map<String, Object> request);

  @GetMapping("/api/fxpeer-exchange/investments/get-products")
  Map<String, Object> getInvestmentProducts();
}
