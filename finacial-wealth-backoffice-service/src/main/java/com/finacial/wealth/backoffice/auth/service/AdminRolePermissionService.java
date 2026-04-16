package com.finacial.wealth.backoffice.auth.service;

import com.finacial.wealth.backoffice.auth.dto.AdminPermissionDto;
import com.finacial.wealth.backoffice.auth.dto.AdminRoleDto;
import com.finacial.wealth.backoffice.auth.dto.CreateRoleRequest;
import com.finacial.wealth.backoffice.auth.dto.UpdateRolePermissionsRequest;
import com.finacial.wealth.backoffice.auth.entity.BoAdminRole;
import com.finacial.wealth.backoffice.auth.entity.BoPermission;
import com.finacial.wealth.backoffice.auth.repo.BoAdminRoleRepository;
import com.finacial.wealth.backoffice.auth.repo.BoPermissionRepository;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminRolePermissionService {

    private final BoAdminRoleRepository roleRepository;
    private final BoPermissionRepository permissionRepository;

    @Transactional(readOnly = true)
    public List<AdminRoleDto> listRoles() {
        return roleRepository.findAllByOrderByNameAsc()
                .stream()
                .map(this::toRoleDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AdminPermissionDto> listPermissions() {
        return permissionRepository.findAllByOrderByModuleAscSubModuleAscActionAsc()
                .stream()
                .map(p -> new AdminPermissionDto(
                        p.getId(),
                        p.getModule(),
                        p.getSubModule(),
                        p.getAction(),
                        p.getCode(),
                        p.getDescription()
                ))
                .toList();
    }

    @Transactional
    public AdminRoleDto createRole(CreateRoleRequest request) {
        String roleName = normalizeRoleName(request.name());
        roleRepository.findByName(roleName).ifPresent(role -> {
            throw new IllegalArgumentException("Role already exists: " + roleName);
        });

        BoAdminRole role = BoAdminRole.builder()
                .name(roleName)
                .permissions(resolvePermissions(request.permissionCodes()))
                .build();
        return toRoleDto(roleRepository.save(role));
    }

    @Transactional
    public AdminRoleDto updatePermissions(Long roleId, UpdateRolePermissionsRequest request) {
        BoAdminRole role = roleRepository.findById(roleId)
                .orElseThrow(() -> new IllegalArgumentException("Role not found"));
        role.setPermissions(resolvePermissions(request.permissionCodes()));
        return toRoleDto(roleRepository.save(role));
    }

    private Set<BoPermission> resolvePermissions(Set<String> permissionCodes) {
        if (permissionCodes == null || permissionCodes.isEmpty()) {
            return new HashSet<>();
        }

        List<String> normalizedCodes = permissionCodes.stream()
                .filter(code -> code != null && !code.isBlank())
                .map(String::trim)
                .distinct()
                .toList();

        List<BoPermission> permissions = permissionRepository.findByCodeIn(normalizedCodes);
        if (permissions.size() != normalizedCodes.size()) {
            Set<String> found = permissions.stream().map(BoPermission::getCode).collect(Collectors.toSet());
            Set<String> missing = normalizedCodes.stream()
                    .filter(code -> !found.contains(code))
                    .collect(Collectors.toSet());
            throw new IllegalArgumentException("Unknown permission codes: " + missing);
        }
        return new HashSet<>(permissions);
    }

    private String normalizeRoleName(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Role name is required");
        }
        return value.trim().toUpperCase(Locale.ROOT).replace(' ', '_');
    }

    private AdminRoleDto toRoleDto(BoAdminRole role) {
        return new AdminRoleDto(
                role.getId(),
                role.getName(),
                role.getPermissions().stream().map(BoPermission::getCode).collect(Collectors.toCollection(java.util.TreeSet::new))
        );
    }
}
