package com.finacial.wealth.api.profiling.identity;

import javax.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class IdentityLivenessVerifyRequest {

    @NotBlank
    private String sessionReference;
    @NotBlank
    private String requestReference;
    private String subjectReference;
    private String base64Image;
    private String imageReference;
}
