package com.finacial.wealth.backoffice.configmanagement.dto;

public record AppConfigRegistryRequest(
        String ownerService,
        String valueType,
        Boolean editable,
        Boolean sensitive,
        String validationRegex,
        String description
) {
}
