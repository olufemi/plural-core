package com.finacial.wealth.backoffice.configmanagement.dto;

public record AppConfigUpdateRequest(
        String configValue,
        String reason
) {
}
