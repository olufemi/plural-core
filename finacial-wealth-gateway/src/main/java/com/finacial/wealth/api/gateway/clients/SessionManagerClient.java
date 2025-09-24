package com.finacial.wealth.api.gateway.clients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

import static com.finacial.wealth.api.gateway.config.Routes.SESSION_MANAGER_VERIFY_TOKEN;

import java.util.Map;

@Component
@FeignClient(name = "session-manager-service")
public interface SessionManagerClient {

    @GetMapping(SESSION_MANAGER_VERIFY_TOKEN)
    Map<String, Object> verifyBearerToken(@RequestHeader("Authorization") String authorization) throws Exception;

}
