package com.finacial.wealth.backoffice.savedview.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.Map;

public record SavedViewRequest(
        @NotBlank String moduleKey,
        @NotBlank String name,
        @NotNull Map<String, Object> filters,
        boolean defaultView
) {
}
