/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.finacial.wealth.api.utility.repository;


import com.finacial.wealth.api.utility.domains.CreateSubAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 *
 * @author olufemioshin
 */
public interface CreateSubAccountRepo extends JpaRepository<CreateSubAccount, Long>, 
        JpaSpecificationExecutor<CreateSubAccount> {
    
}
