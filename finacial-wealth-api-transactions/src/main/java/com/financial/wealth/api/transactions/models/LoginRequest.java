/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.financial.wealth.api.transactions.models;

import lombok.AllArgsConstructor;

/**
 *
 * @author olufemioshin
 */
import lombok.Data;

@Data
@AllArgsConstructor
public class LoginRequest {

    private String email;
    private String password;
}
