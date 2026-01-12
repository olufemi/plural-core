/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.finacial.wealth.backoffice.admin.dto;

import com.finacial.wealth.backoffice.auth.entity.BoAdminRole;
import com.finacial.wealth.backoffice.auth.entity.BoAdminUser;
import java.util.Set;

/**
 *
 * @author olufemioshin
 */
public record AdminUserResponse(
        Long id,
        String email,
        String fullName,
        BoAdminUser.Status status,
        boolean mfaEnabled,
        Set<BoAdminRole> roles
) {}
