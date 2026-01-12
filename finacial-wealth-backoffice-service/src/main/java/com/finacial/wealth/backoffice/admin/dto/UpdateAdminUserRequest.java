/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.finacial.wealth.backoffice.admin.dto;

import com.finacial.wealth.backoffice.auth.entity.BoAdminRole;
import java.util.Set;

/**
 *
 * @author olufemioshin
 */
public record UpdateAdminUserRequest(
        String fullName,
        Set<BoAdminRole> roles
) {}
