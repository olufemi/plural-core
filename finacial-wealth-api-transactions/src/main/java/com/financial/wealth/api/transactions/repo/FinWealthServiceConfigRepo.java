/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.financial.wealth.api.transactions.repo;

import com.financial.wealth.api.transactions.domain.FinWealthServiceConfig;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

/**
 *
 * @author olufemioshin
 */
public interface FinWealthServiceConfigRepo extends
        CrudRepository<FinWealthServiceConfig, String> {

    boolean existsByServiceType(String serviceType);

    @Query("SELECT o FROM FinWealthServiceConfig o where o.serviceType = :serviceType")
    Optional<FinWealthServiceConfig> findAllByServiceType(@Param("serviceType") String serviceType);

    @Query("SELECT o FROM FinWealthServiceConfig o where o.serviceType = :serviceType")
    FinWealthServiceConfig findByServiceType(@Param("serviceType") String serviceType);

    @Query("SELECT o FROM FinWealthServiceConfig o where o.serviceType = :serviceType")
    List<FinWealthServiceConfig> findByServiceTypeEnable(@Param("serviceType") String serviceType);

}
