/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.finacial.wealth.api.utility.repository;

import com.finacial.wealth.api.utility.domains.CreateProvidusVirtAccount;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

/**
 *
 * @author olufemioshin
 */
public interface CreateProvidusVirtAccountRepo extends JpaRepository<CreateProvidusVirtAccount, Long> {

    @Query("select bs from CreateProvidusVirtAccount bs where bs.accountNumber=:accountNumber")
    CreateProvidusVirtAccount findByAccountNumber(String accountNumber);
    
    @Query("select bs from CreateProvidusVirtAccount bs where bs.walletNo=:walletNo")
    List<CreateProvidusVirtAccount> findByWalletNo(String walletNo);
    
}
