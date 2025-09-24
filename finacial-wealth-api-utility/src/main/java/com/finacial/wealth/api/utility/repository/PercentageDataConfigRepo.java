/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.finacial.wealth.api.utility.repository;

import com.finacial.wealth.api.utility.domains.PercentageDataConfig;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 *
 * @author olufemioshin
 */
public interface PercentageDataConfigRepo extends JpaRepository<PercentageDataConfig, Long> {

    @Override
    List<PercentageDataConfig> findAll();

}
