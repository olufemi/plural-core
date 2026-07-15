package com.finacial.wealth.backoffice.configmanagement.dto;

public record AppConfigResponse(
        Long id,
        String configName,
        String configDescription,
        String configValue,
        boolean registered,
        boolean editable,
        boolean sensitive,
        String ownerService,
        String valueType
) {
}
