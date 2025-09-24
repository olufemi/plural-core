/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.finacial.wealth.api.fxpeer.exchange.escrow;

/**
 *
 * @author olufemioshin
 */
import com.finacial.wealth.api.fxpeer.exchange.common.EscrowStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import jakarta.persistence.LockModeType;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface EscrowRepository extends JpaRepository<Escrow, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Escrow> findById(Long id);

    List<Escrow> findAllByStatusAndExpiresAtBefore(EscrowStatus status, Instant cutoff);
}
