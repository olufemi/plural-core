/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.finacial.wealth.api.fxpeer.exchange.inter.airtime.getpack;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

/**
 *
 * @author olufemioshin
 */

/** Product inside an operator (airtime/data plan, etc.) */
//@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Product {
    private String id;
    private String name;

    /** appears as "type": "1" or "4" in the payload */
    private String type;

    /** appears as "category": "1.0", "4.1", etc. */
    private String category;

    private Amount amount;

    /** "extraParameters": false */
    private boolean extraParameters;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Amount getAmount() {
        return amount;
    }

    public void setAmount(Amount amount) {
        this.amount = amount;
    }

    public boolean isExtraParameters() {
        return extraParameters;
    }

    public void setExtraParameters(boolean extraParameters) {
        this.extraParameters = extraParameters;
    }
    
    
    
}
