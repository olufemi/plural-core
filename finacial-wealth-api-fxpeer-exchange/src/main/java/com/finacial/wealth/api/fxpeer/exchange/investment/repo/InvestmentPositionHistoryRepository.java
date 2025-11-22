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
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface InvestmentPositionHistoryRepository
        extends JpaRepository<InvestmentPositionHistory, Long> {

    Optional<InvestmentPositionHistory> findByPositionIdAndValuationDate(Long positionId, LocalDate date);

    List<InvestmentPositionHistory> findByPositionIdOrderByValuationDateAsc(Long positionId);

    List<InvestmentPositionHistory> findByPositionIdAndValuationDateBetweenOrderByValuationDateAsc(
            Long positionId, LocalDate start, LocalDate end);
}
