/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.financial.wealth.api.transactions.repo;

import com.financial.wealth.api.transactions.domain.ConfiguedMembersNumber;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 *
 * @author olufemioshin
 */
public interface ConfiguedMembersNumberRepo extends JpaRepository<ConfiguedMembersNumber, Long> {

    @Override
    List<ConfiguedMembersNumber> findAll();
    
    
}
