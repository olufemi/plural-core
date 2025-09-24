/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.finacial.wealth.api.utility.repository;

import com.finacial.wealth.api.utility.domains.UserServicesDataConfig;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 *
 * @author olufemioshin
 */
public interface UserServicesDataConfigRepo extends JpaRepository<UserServicesDataConfig, Long> {

    @Override
    List<UserServicesDataConfig> findAll();
    
}
