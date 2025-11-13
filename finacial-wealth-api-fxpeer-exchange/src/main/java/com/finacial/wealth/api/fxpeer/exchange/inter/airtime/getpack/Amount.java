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

/** Amount object: fixed or range with min/max */
//@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Amount {
    private MinMax min;
    private MinMax max;

    /** "fixed" or "range" */
    private String type;

    public MinMax getMin() {
        return min;
    }

    public void setMin(MinMax min) {
        this.min = min;
    }

    public MinMax getMax() {
        return max;
    }

    public void setMax(MinMax max) {
        this.max = max;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
    
    
    
}
