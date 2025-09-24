/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.finacial.wealth.api.profiling.repo;

import com.finacial.wealth.api.profiling.domain.FootprintResponseLog;
import com.finacial.wealth.api.profiling.domain.FootprintValidationFailed;
import org.springframework.data.repository.CrudRepository;

/**
 *
 * @author olufemioshin
 */
public interface FootprintResponseLogRepo extends
        CrudRepository<FootprintResponseLog, String>{
    
}
