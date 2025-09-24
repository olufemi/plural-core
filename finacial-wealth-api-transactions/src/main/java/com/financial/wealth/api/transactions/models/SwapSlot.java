/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.financial.wealth.api.transactions.models;

import lombok.Data;

/**
 *
 * @author olufemioshin
 */
@Data
public class SwapSlot {
    private String senderId;
    private String receiverId;
    private int senderSlot;
    private String isSwapActive;
    private int receiverSlot;
    private String isSwapped;
    private String dateSwapped;
}
