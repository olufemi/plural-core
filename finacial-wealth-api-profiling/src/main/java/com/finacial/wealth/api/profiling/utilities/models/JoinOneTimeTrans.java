/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.finacial.wealth.api.profiling.utilities.models;

import lombok.Data;

/**
 *
 * @author olufemioshin
 */
@Data
public class JoinOneTimeTrans {
    private String destination;
    private String transactionId;
    private String initiatorName;
   // private String initiatorNo;
}
