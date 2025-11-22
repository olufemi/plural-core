/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.finacial.wealth.api.fxpeer.exchange.investment.repo;

/**
 *
 * @author olufemioshin
 */
import com.finacial.wealth.api.fxpeer.exchange.investment.domain.InvestmentValuationHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.Query;

public interface InvestmentValuationHistoryRepository
        extends JpaRepository<InvestmentValuationHistory, Long> {

    @Query("""
        select v from InvestmentValuationHistory v
        where v.position.id = :positionId
          and v.valuationDate between :start and :end
        order by v.valuationDate asc
        """)
    List<InvestmentValuationHistory> findByPositionAndDateRange(Long positionId,
                                                               LocalDate start,
                                                               LocalDate end);
}
