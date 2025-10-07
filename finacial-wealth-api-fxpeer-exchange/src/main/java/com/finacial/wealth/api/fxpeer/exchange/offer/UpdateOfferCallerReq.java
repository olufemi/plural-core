/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.finacial.wealth.api.fxpeer.exchange.offer;

import lombok.Data;

/**
 *
 * @author olufemioshin
 */
@Data
public class UpdateOfferCallerReq {
    private String newRate;
    private String pin;
    private String correlationId;
}
