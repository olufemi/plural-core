package com.finacial.wealth.api.profiling.identity;

import java.time.Instant;
import java.util.UUID;
import lombok.Data;

@Data
public class IdentityLivenessSessionResponse {

    private UUID livenessSessionId;
    private String sessionReference;
    private String provider;
    private String providerSessionId;
    private Instant expiresAt;
    private String status;
}
