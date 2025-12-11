/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.finacial.wealth.api.profiling.repo;


import com.finacial.wealth.api.profiling.domain.InvestmentProduct;


/**
 *
 * @author olufemioshin
 */
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface InvestmentProductRepository extends JpaRepository<InvestmentProduct, Long> {

    Optional<InvestmentProduct> findByIdAndActiveTrue(Long id);
    
  ;


}