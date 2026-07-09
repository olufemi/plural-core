package com.finacial.wealth.api.profiling.identity;

import javax.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class IdentityLivenessSessionRequest {

    @NotBlank
    private String subjectReference;
    @NotBlank
    private String requestReference;
    private String consentReference;
    private String channel;
}
