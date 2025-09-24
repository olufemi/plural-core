/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.finacial.wealth.api.sessionmanager.request;

import lombok.Data;

/**
 *
 * @author olufemioshin
 */
@Data
public class EmailRequestKulean {
    
    public String to;
    public String body;
    public String subject;
    
}
