package com.finacial.wealth.api.profiling.testonboarding;

import java.util.Arrays;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class TestOnboardingBypassGuard implements ApplicationRunner {

    private final Environment environment;

    @Value("${test.onboarding.bypass.enabled:false}")
    private boolean bypassEnabled;

    @Value("${test.onboarding.bypass.key:}")
    private String bypassKey;

    public TestOnboardingBypassGuard(Environment environment) {
        this.environment = environment;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (!bypassEnabled) {
            return;
        }
        if (!StringUtils.hasText(bypassKey)) {
            throw new IllegalStateException("test.onboarding.bypass.key must be set when test onboarding bypass is enabled");
        }
        boolean allowedProfile = Arrays.stream(environment.getActiveProfiles())
                .anyMatch(profile -> "dev".equalsIgnoreCase(profile) || "local".equalsIgnoreCase(profile));
        if (!allowedProfile) {
            throw new IllegalStateException("Test onboarding bypass can only be enabled with dev or local Spring profiles");
        }
    }
}
