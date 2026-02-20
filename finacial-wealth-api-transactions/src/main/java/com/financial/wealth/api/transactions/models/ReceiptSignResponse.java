/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.financial.wealth.api.transactions.models;

/**
 *
 * @author olufemioshin
 */
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class ReceiptSignResponse {
    private int v;
    private String kid;
    private String signature;
    private String alg;
    private String fmt;
}
