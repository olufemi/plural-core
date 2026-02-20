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

@Data
public class ReceiptSignRequest {
    private String txId;
    private String amountMinor;
    private String currency;
    private String senderId;
    private String receiverId;
    private String timestampUtcIso;
    private String status;
}
