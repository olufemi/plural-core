package com.finacial.wealth.api.profiling.identity;

import lombok.Data;

@Data
public class IdentityFaceCompareRequest {

    private String requestReference;
    private String imageABase64;
    private String imageBBase64;
    private String purpose;
}
