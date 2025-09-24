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
public class AccountStatementAPIReUtil {
    
    private String startDate;
    private String endDate;
    private String userId;
    private String channel;
    
}
