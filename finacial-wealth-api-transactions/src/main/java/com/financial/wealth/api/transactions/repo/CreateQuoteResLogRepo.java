/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.financial.wealth.api.transactions.repo;

import com.financial.wealth.api.transactions.domain.CreateQuoteResLog;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

/**
 *
 * @author olufemioshin
 */
public interface CreateQuoteResLogRepo extends
        CrudRepository<CreateQuoteResLog, String> {

    @Query("SELECT u FROM CreateQuoteResLog u where u.quoteId = :quoteId")
    List<CreateQuoteResLog> findByQuoteId(@Param("quoteId") String quoteId);
    
    @Query("SELECT u FROM CreateQuoteResLog u where u.quoteId = :quoteId")
    CreateQuoteResLog findByQuoteIdUpdate(@Param("quoteId") String quoteId);
    
    Optional<CreateQuoteResLog> findFirstByQuoteIdAndStatusAndIsAcceptedAndCreateQuoteResponseIsNotNull(
            String quoteId, String status, String isAccepted
    );
    
     Page<CreateQuoteResLog> findByEmailIgnoreCaseAndStatusAndIsAcceptedAndCreateQuoteResponseIsNotNull(
            String email, String status, String isAccepted, Pageable pageable
    );

}
