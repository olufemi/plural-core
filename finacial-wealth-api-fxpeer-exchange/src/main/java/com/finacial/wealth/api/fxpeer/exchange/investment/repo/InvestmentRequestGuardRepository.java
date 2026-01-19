/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.finacial.wealth.api.fxpeer.exchange.investment.repo;

import com.finacial.wealth.api.fxpeer.exchange.investment.domain.InvestmentRequestGuard;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

/**
 *
 * @author olufemioshin
 */
public interface InvestmentRequestGuardRepository extends JpaRepository<InvestmentRequestGuard, Long> {

    Optional<InvestmentRequestGuard> findByIdempotencyKey(String idempotencyKey);

    @Query("""
        select g from InvestmentRequestGuard g
        where g.emailAddress = :email
          and g.orderRef = :orderRef
          and g.requestType = :type
          and g.createdAt >= :since
        order by g.createdAt desc
    """)
    List<InvestmentRequestGuard> findRecent(String email, String orderRef, String type, Instant since);
}

