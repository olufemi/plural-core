/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.financial.wealth.api.transactions.config;

/**
 *
 * @author olufemioshin
 */
public class ApiClientException extends Exception {
     public ApiClientException(String message) { super(message); }
    public ApiClientException(String message, Throwable cause) { super(message, cause); }
}

