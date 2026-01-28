/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.finacial.wealth.backoffice.auth.controller;

import com.finacial.wealth.backoffice.auth.dto.AdminRoleDto;
import com.finacial.wealth.backoffice.auth.service.AdminUserService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author olufemioshin
 */
@RestController
@RequestMapping("/api/admin/roles")
@RequiredArgsConstructor
public class AdminRoleController {

    private final AdminUserService adminUserService;

    @PreAuthorize("hasAuthority('ROLE_SUPER_ADMIN')")
    @GetMapping
    public ResponseEntity<List<AdminRoleDto>> list() {
        return ResponseEntity.ok(adminUserService.listRoles());
    }
}

