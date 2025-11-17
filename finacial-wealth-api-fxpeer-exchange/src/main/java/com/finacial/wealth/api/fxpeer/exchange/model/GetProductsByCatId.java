/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.finacial.wealth.api.fxpeer.exchange.model;

import lombok.Data;

/**
 *
 * @author olufemioshin
 */
@Data
public class GetProductsByCatId {

    private String currencyCode;
    private String categoryId;
}
