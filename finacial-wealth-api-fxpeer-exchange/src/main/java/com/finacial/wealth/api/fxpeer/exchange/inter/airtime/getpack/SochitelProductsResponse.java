/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.finacial.wealth.api.fxpeer.exchange.inter.airtime.getpack;

/**
 *
 * @author olufemioshin
 */
import com.fasterxml.jackson.annotation.JsonAlias;

import lombok.Data;

import java.util.List;

/**
 * Top-level response
 */
@Data
public class SochitelProductsResponse {

    @JsonAlias("errno")
    private Integer errno;
    @JsonAlias("error")
    private String error;

    @JsonAlias("sysErr")
    private Integer sysErr;
    @JsonAlias("sysId")
    private String sysId;

    @JsonAlias({"operators", "response"})     // <-- IMPORTANT
    private List<OperatorEntry> operators;

    public Integer getErrno() {
        return errno;
    }

    public String getError() {
        return error;
    }

    public Integer getSysErr() {
        return sysErr;
    }

    public String getSysId() {
        return sysId;
    }

    public List<OperatorEntry> getOperators() {
        return operators;
    }

    public boolean isOk() {
        if (errno != null) {
            return errno == 0;
        }
        if (sysErr != null) {
            return sysErr == 0;
        }
        // if provider omits errno/sysErr but returns data, treat as OK
        return operators != null && !operators.isEmpty();
    }

    public String errorSummary() {
        if (errno != null && errno != 0) {
            return "errno=" + errno + (error != null ? (", error=" + error) : "");
        }
        if (sysErr != null && sysErr != 0) {
            return "sysErr=" + sysErr + (sysId != null ? (", sysId=" + sysId) : "");
        }
        return null;
    }
}
