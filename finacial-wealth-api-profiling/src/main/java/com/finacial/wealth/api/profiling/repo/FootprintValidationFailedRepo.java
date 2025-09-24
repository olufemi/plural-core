/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.finacial.wealth.api.profiling.repo;

import com.finacial.wealth.api.profiling.domain.FootprintValidationFailed;
import com.finacial.wealth.api.profiling.domain.PinActFailedTransLog;
import org.springframework.data.repository.CrudRepository;

/**
 *
 * @author olufemioshin
 */
public interface FootprintValidationFailedRepo extends
        CrudRepository<FootprintValidationFailed, String>{
    
}
