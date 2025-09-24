/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.finacial.wealth.api.profiling.utilities.models;

import java.util.List;
import lombok.Data;

/**
 *
 * @author olufemioshin
 */

public class SMSResponseKuleanModel {
    List<SMSResponseMonty> sMSResponseKulean;

    public List<SMSResponseMonty> getsMSResponseKulean() {
        return sMSResponseKulean;
    }

    public void setsMSResponseKulean(List<SMSResponseMonty> sMSResponseKulean) {
        this.sMSResponseKulean = sMSResponseKulean;
    }

   
    
    
}
