package com.finacial.wealth.api.profiling.identity;

import java.time.Instant;
import lombok.Data;

@Data
public class IdentityLivenessApiResponse<T> {

    private boolean success;
    private String code;
    private String message;
    private String correlationId;
    private Instant timestamp;
    private T data;
    private Object error;
}
