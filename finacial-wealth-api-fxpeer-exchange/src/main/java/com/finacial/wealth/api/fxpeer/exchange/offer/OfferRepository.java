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
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OfferRepository extends JpaRepository<Offer, Long>, JpaSpecificationExecutor<Offer> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Offer> findById(Long id);

    Page<Offer> findBySellerUserId(Long sellerId, Pageable pageable);

    Page<Offer> findBySellerUserIdAndStatus(Long sellerId, OfferStatus status, Pageable pageable);

    Optional<Offer> findByIdAndSellerUserId(Long id, Long sellerId);

    @Query("SELECT u FROM Offer u where u.correlationId = :correlationId")
    List<Offer> findByCorrelationIdData(@Param("correlationId") String correlationId);

    @Query("SELECT u FROM Offer u where u.correlationId = :correlationId")
    Offer findByCorrelationIdDataUpdate(@Param("correlationId") String correlationId);

    long countBySellerUserIdAndStatus(Long sellerUserId, OfferStatus status);

    // Flexible JPQL (status optional)
    @Query("""
           select o
           from Offer o
           where o.sellerUserId <> :sellerId
             and (:status is null or o.status = :status)
           """)
    Page<Offer> findMarketExcludingSeller(
            @Param("sellerId") Long sellerId,
            @Param("status") OfferStatus status,
            Pageable pageable);

    // Flexible JPQL (status optional)
    @Query("""
           select o
           from Offer o
           where (:status is null or o.status = :status)
           """)
    Page<Offer> findMarket(
            @Param("status") OfferStatus status,
            Pageable pageable);
    
      // Page in chunks to avoid loading everything at once
    Page<Offer> findByExpiryAtIsNotNullAndExpiryAtLessThanEqualAndStatusIn(
            Instant now, Collection<OfferStatus> statuses, Pageable pageable);

    // Optional: a lightweight count or existence check
    boolean existsByExpiryAtIsNotNullAndExpiryAtLessThanEqualAndStatusIn(
            Instant now, Collection<OfferStatus> statuses);
}
