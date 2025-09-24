/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.finacial.wealth.api.profiling.repo;

import com.finacial.wealth.api.profiling.domain.AddAccountDetails;
import com.finacial.wealth.api.profiling.domain.AppConfig;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 *
 * @author olufemioshin
 */
public interface AddAccountDetailsRepo extends JpaRepository<AddAccountDetails, Long> {
    
      boolean existsByAccountNumber(String phoneNumber);
      boolean existsByWalletId(String walletId);
    
}
