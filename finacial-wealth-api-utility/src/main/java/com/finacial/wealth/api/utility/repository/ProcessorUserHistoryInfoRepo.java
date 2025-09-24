/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.finacial.wealth.api.utility.repository;

import com.finacial.wealth.api.utility.domains.ProcessorUserHistoryInfo;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 *
 * @author OSHIN
 */
@Repository
public interface ProcessorUserHistoryInfoRepo extends
        CrudRepository<ProcessorUserHistoryInfo, String>{
    
}
