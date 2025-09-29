/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.financial.wealth.api.transactions.services.notify;

import lombok.Data;

/**
 *
 * @author olufemioshin
 */
@Data
public class FcmV1Request {
    public Message message; public boolean validate_only;
}
