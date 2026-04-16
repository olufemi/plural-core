package com.finacial.wealth.backoffice.auth.dto;

import java.util.Set;

public record UpdateRolePermissionsRequest(
        Set<String> permissionCodes
) {
}
