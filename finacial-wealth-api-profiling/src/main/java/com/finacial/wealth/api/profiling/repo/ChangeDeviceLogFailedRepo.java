/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.finacial.wealth.api.profiling.repo;


import com.finacial.wealth.api.profiling.domain.ChangeDeviceLogFailed;
import org.springframework.data.repository.CrudRepository;

/**
 *
 * @author olufemioshin
 */
public interface ChangeDeviceLogFailedRepo extends
        CrudRepository<ChangeDeviceLogFailed, String>{
    
}
