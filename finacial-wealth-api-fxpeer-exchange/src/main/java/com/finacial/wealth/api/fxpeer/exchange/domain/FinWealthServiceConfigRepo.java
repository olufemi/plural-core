/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.finacial.wealth.api.fxpeer.exchange.domain;

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
        CrudRepository<FinWealthPayServiceConfig, String> {

    boolean existsByServiceType(String serviceType);

    @Query("SELECT o FROM FinWealthPayServiceConfig o where o.serviceType = :serviceType")
    Optional<FinWealthPayServiceConfig> findAllByServiceType(@Param("serviceType") String serviceType);

    @Query("SELECT o FROM FinWealthPayServiceConfig o where o.serviceType = :serviceType")
    FinWealthPayServiceConfig findByServiceType(@Param("serviceType") String serviceType);

    @Query("SELECT o FROM FinWealthPayServiceConfig o where o.serviceType = :serviceType")
    List<FinWealthPayServiceConfig> findByServiceTypeEnable(@Param("serviceType") String serviceType);

    @Query("SELECT o FROM FinWealthPayServiceConfig o where o.serviceType = :serviceType and o.currencyCode = :currencyCode")
    List<FinWealthPayServiceConfig> findByServiceTypeAndCurrencyCode(@Param("serviceType") String serviceType, @Param("currencyCode") String currencyCode);

}
