/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.finacial.wealth.api.fxpeer.exchange.markets;

/**
 *
 * @author olufemioshin
 */
import com.finacial.wealth.api.fxpeer.exchange.common.NegotiationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NegotiationRepository extends JpaRepository<Negotiation, Long> {

    long countByBuyerUserIdAndStatus(Long buyerUserId, NegotiationStatus status);
}
