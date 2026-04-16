package com.finacial.wealth.backoffice.auth.dto;

import java.util.Set;

public record CreateRoleRequest(
        String name,
        Set<String> permissionCodes
) {
}
