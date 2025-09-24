/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.financial.wealth.api.transactions.exceptions;

import java.util.List;
import lombok.Data;

/**
 *
 * @author olufemioshin
 */
@Data
public class ApiErrorResponse {
    private String message;
    private String code;
    private List<String> errors;
}
