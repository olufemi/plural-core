/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.finacial.wealth.api.fxpeer.exchange.offer;

/**
 *
 * @author olufemioshin
 */
import com.finacial.wealth.api.fxpeer.exchange.common.OfferStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import jakarta.persistence.LockModeType;
import java.util.Optional;

public interface OfferRepository extends JpaRepository<Offer, Long>, JpaSpecificationExecutor<Offer> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Offer> findById(Long id);

    long countBySellerUserIdAndStatus(Long sellerUserId, OfferStatus status);
}
