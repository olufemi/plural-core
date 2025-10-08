/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.finacial.wealth.api.fxpeer.exchange.order;

import lombok.Data;

/**
 *
 * @author olufemioshin
 */
@Data
public class BuyOfferNow {
    private String amount;
    private String pin;;
    private String referralCode;
    private String offerCorrelationId;
}
