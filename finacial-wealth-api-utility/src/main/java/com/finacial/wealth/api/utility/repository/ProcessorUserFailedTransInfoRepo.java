/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.finacial.wealth.api.utility.repository;

import com.finacial.wealth.api.utility.domains.ProcessorUserFailedTransInfo;
import org.springframework.data.repository.CrudRepository;

/**
 *
 * @author olufemioshin
 */
public interface ProcessorUserFailedTransInfoRepo  extends
        CrudRepository<ProcessorUserFailedTransInfo, String>{
    
}
