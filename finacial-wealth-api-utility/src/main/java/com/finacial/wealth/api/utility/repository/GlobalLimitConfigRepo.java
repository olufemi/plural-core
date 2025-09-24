/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.finacial.wealth.api.utility.repository;

import com.finacial.wealth.api.utility.domains.GlobalLimitConfig;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

/**
 *
 * @author olufemioshin
 */
public interface GlobalLimitConfigRepo extends
        CrudRepository<GlobalLimitConfig, String> {

    @Query("SELECT config from GlobalLimitConfig config where config.category=:category")
    List<GlobalLimitConfig> findByLimitCategory(String category);

    public static final String FIND_LIMITS = "SELECT dailyLimit, singleTransactionLimit, maximumBalance, category FROM GlobalLimitConfig";

    // @Query("SELECT config.dailyLimit, config.singleTransactionLimit, config.maximumBalance, config.category FROM GlobalLimitConfig config")
    @Query("SELECT config FROM GlobalLimitConfig config")
    public List<GlobalLimitConfig> findLimits();
}
