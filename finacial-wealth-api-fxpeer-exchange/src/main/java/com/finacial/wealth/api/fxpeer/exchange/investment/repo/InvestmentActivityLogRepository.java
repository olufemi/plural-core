/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.finacial.wealth.api.fxpeer.exchange.investment.repo;

import com.finacial.wealth.api.fxpeer.exchange.investment.domain.InvestmentActivityLog;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;

/**
 *
 * @author olufemioshin
 */
public interface InvestmentActivityLogRepository
        extends JpaRepository<InvestmentActivityLog, Long> {

    List<InvestmentActivityLog> findTop100ByCreatedAtBetweenOrderByCreatedAtDesc(
            LocalDateTime start,
            LocalDateTime end
    );
}
