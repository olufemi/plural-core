/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.finacial.wealth.api.fxpeer.exchange.investment.repo;

import com.finacial.wealth.api.fxpeer.exchange.domain.InternationalProductCat;
import com.finacial.wealth.api.fxpeer.exchange.investment.domain.InvestmentProduct;
import com.finacial.wealth.api.fxpeer.exchange.investment.ennum.InvestmentType;
import org.springframework.data.repository.CrudRepository;

/**
 *
 * @author olufemioshin
 */
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface InvestmentProductRepository extends JpaRepository<InvestmentProduct, Long> {

    Optional<InvestmentProduct> findByIdAndActiveTrue(Long id);

    @Query("""
        select p from InvestmentProduct p 
        where p.active = true 
          and (:currency is null or p.currency = :currency)
          and (:type is null or p.type = :type)
        """)
    List<InvestmentProduct> findActiveByCurrencyAndType(String currency, InvestmentType type);
    
     List<InvestmentProduct> findByActiveTrue();
}