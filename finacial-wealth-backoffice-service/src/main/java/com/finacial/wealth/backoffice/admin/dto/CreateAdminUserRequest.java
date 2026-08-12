/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.finacial.wealth.backoffice.admin.dto;

import java.util.Set;

/**
 *
 * @author olufemioshin
 */
public record CreateAdminUserRequest(
        String email,
        String fullName,
        String password,
        String confirmPassword,
        Set<String> roles
) {}
