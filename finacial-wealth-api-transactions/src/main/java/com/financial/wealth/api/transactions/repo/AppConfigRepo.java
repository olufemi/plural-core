/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.financial.wealth.api.transactions.repo;

import com.financial.wealth.api.transactions.domain.AppConfig;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

/**
 *
 * @author OSHIN
 */
@Repository
public interface AppConfigRepo extends JpaRepository<AppConfig, Long> {

    @Query("SELECT config from AppConfig config where config.configValue=:configValue")
    List<AppConfig> findByConfigValue(String configValue);

    @Query("SELECT config from AppConfig config where config.configName=:configName")
    List<AppConfig> findByConfigName(String configName);
}
