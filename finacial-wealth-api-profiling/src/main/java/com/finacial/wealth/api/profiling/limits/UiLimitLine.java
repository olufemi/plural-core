/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.finacial.wealth.api.profiling.limits;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 *
 * @author olufemioshin
 */
class UiLimitLine implements Serializable {

    private String label; // "Daily Limit", "Weekly Limit"

// currency formatting is done by client; we send numeric in base units.
    private BigDecimal limit; // null if unlimited
    private BigDecimal spent; // never null
    private BigDecimal left; // null if unlimited
    private boolean unlimited;

    public UiLimitLine() {
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public BigDecimal getLimit() {
        return limit;
    }

    public void setLimit(BigDecimal limit) {
        this.limit = limit;
    }

    public BigDecimal getSpent() {
        return spent;
    }

    public void setSpent(BigDecimal spent) {
        this.spent = spent;
    }

    public BigDecimal getLeft() {
        return left;
    }

    public void setLeft(BigDecimal left) {
        this.left = left;
    }

    public boolean isUnlimited() {
        return unlimited;
    }

    public void setUnlimited(boolean unlimited) {
        this.unlimited = unlimited;
    }
}
