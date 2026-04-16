package com.finacial.wealth.backoffice.auth.dto;

public record AdminPermissionDto(
        Long id,
        String module,
        String subModule,
        String action,
        String code,
        String description
) {
}
