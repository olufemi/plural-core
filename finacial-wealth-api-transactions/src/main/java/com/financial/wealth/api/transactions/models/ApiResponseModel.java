/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.financial.wealth.api.transactions.models;

import java.io.Serializable;

import lombok.Data;

/**
 *
 * @author HRH
 */
@Data
public class ApiResponseModel implements Serializable {

    private int statusCode;
    private String description;
    private Object data;
    private String other;

    private String externalRefrence;
    private String benefNarration;
    private String status;
    private String amount;
    private String fees;
    private String requestedAmount;

}
