/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.finacial.wealth.api.fxpeer.exchange.investment.repo;

/**
 *
 * @author olufemioshin
 */
import com.finacial.wealth.api.fxpeer.exchange.investment.domain.InvestmentPositionHistory;
import com.finacial.wealth.api.fxpeer.exchange.investment.ennum.InvestmentPositionStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface InvestmentPositionHistoryRepository
        extends JpaRepository<InvestmentPositionHistory, Long> {

    Optional<InvestmentPositionHistory> findByPositionIdAndValuationDate(Long positionId, LocalDate date);

    List<InvestmentPositionHistory> findByPositionIdOrderByValuationDateAsc(Long positionId);

    List<InvestmentPositionHistory> findByPositionIdAndValuationDateBetweenOrderByValuationDateAsc(
            Long positionId, LocalDate start, LocalDate end);

    // NEW: history per customer (email), latest first
    // List<InvestmentPositionHistory> findByEmailAddressOrderByValuationDateDesc(String emailAddress);
    @Query("""
           select h
           from InvestmentPositionHistory h
           join fetch h.position p
           join fetch p.product
           where h.emailAddress = :email
           order by h.valuationDate desc
           """)
    List<InvestmentPositionHistory> findHistoryByEmailAddress(@Param("email") String email);

    @Query("""
       SELECT h
       FROM InvestmentPositionHistory h
       WHERE h.position.status IN :statuses
       AND h.position.emailAddress = :email
       ORDER BY h.valuationDate DESC
       """)
    Page<InvestmentPositionHistory> findLiquidatedByEmail(
            @Param("email") String email,
            @Param("statuses") List<InvestmentPositionStatus> statuses, Pageable pageable
    );

}
