/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.finacial.wealth.api.fxpeer.exchange.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

/**
 *
 * @author olufemioshin
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class GetProductsByCountry {

    @JsonAlias({"countryCode"})
    private String currencyCode;
    
}
