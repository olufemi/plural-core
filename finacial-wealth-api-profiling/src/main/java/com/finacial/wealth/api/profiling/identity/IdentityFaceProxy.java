package com.finacial.wealth.api.profiling.identity;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@FeignClient(name = "identity-face-service", url = "${fin.wealth.identity.face.base.url:http://localhost:60008}")
public interface IdentityFaceProxy {

    @RequestMapping(value = "/api/v1/verification/compare", consumes = "application/json", method = RequestMethod.POST)
    IdentityFaceCompareResponse compare(@RequestBody IdentityFaceCompareRequest request);
}
