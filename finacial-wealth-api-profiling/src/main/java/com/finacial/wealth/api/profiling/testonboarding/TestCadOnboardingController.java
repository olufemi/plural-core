package com.finacial.wealth.api.profiling.testonboarding;

import com.finacial.wealth.api.profiling.response.BaseResponse;
import java.util.Arrays;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/test/onboarding")
public class TestCadOnboardingController {

    private final TestCadOnboardingService testCadOnboardingService;
    private final Environment environment;

    @Value("${test.onboarding.bypass.enabled:false}")
    private boolean bypassEnabled;

    @Value("${test.onboarding.bypass.key:}")
    private String bypassKey;

    public TestCadOnboardingController(TestCadOnboardingService testCadOnboardingService, Environment environment) {
        this.testCadOnboardingService = testCadOnboardingService;
        this.environment = environment;
    }

    @PostMapping("/cad-user")
    public ResponseEntity<BaseResponse> createCadUser(
            @RequestHeader(value = "X-Test-Bypass-Key", required = false) String requestKey,
            @RequestBody TestCadOnboardingRequest request) {
        if (!authorized(requestKey)) {
            BaseResponse response = new BaseResponse(403, "Test onboarding bypass is disabled or unauthorized.");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
        }
        try {
            TestCadOnboardingResponse result = testCadOnboardingService.createCadUser(request);
            BaseResponse response = new BaseResponse(200, "Test CAD user created successfully.");
            response.addData("customer", result);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException ex) {
            BaseResponse response = new BaseResponse(400, ex.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    private boolean authorized(String requestKey) {
        return bypassEnabled
                && isAllowedProfile()
                && StringUtils.hasText(bypassKey)
                && StringUtils.hasText(requestKey)
                && bypassKey.equals(requestKey);
    }

    private boolean isAllowedProfile() {
        return Arrays.stream(environment.getActiveProfiles())
                .anyMatch(profile -> "dev".equalsIgnoreCase(profile) || "local".equalsIgnoreCase(profile));
    }
}
