/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.finacial.wealth.api.fxpeer.exchange.investment.repo;

import com.finacial.wealth.api.fxpeer.exchange.investment.domain.InvestmentOrder;
import com.finacial.wealth.api.fxpeer.exchange.investment.domain.InvestmentPosition;
import com.finacial.wealth.api.fxpeer.exchange.investment.domain.InvestmentProduct;
import com.finacial.wealth.api.fxpeer.exchange.investment.ennum.InvestmentOrderStatus;
import com.finacial.wealth.api.fxpeer.exchange.offer.Offer;
import org.springframework.data.repository.CrudRepository;

/**
 *
 * @author olufemioshin
 */
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface InvestmentOrderRepository extends JpaRepository<InvestmentOrder, Long> {

    Optional<InvestmentOrder> findByOrderRef(String orderRef);

    Optional<InvestmentOrder> findByIdempotencyKey(String idempotencyKey);

    @Query("""
        select o from InvestmentOrder o
        where o.emailAddress = :emailAddress
          and o.createdAt between :start and :end
        """)
    List<InvestmentOrder> findByEmailAddressAndDateRange(String emailAddress, Instant start, Instant end);

    @Query("SELECT u FROM InvestmentOrder u where u.orderRef = :orderRef")
    InvestmentOrder findByOrderRefDataUpdate(@Param("orderRef") String orderRef);

    @Query("""
    select p from InvestmentOrder p
    where p.status = :status
""")
    List<InvestmentOrder> findAllByStatus(@Param("status") InvestmentOrderStatus status);

    Optional<InvestmentOrder> findByOrderRefAndEmailAddress(String orderRef, String emailAddress);

    @Query("""
        select p from InvestmentOrder p
        where p.emailAddress = :emailAddress 
          and p.status in (com.finacial.wealth.api.fxpeer.exchange.investment.ennum.InvestmentOrderStatus.ACTIVE)
        """)
    List<InvestmentOrder> findActiveByEmailAddress(String emailAddress);

    List<InvestmentOrder> findByProductAndStatus(
            InvestmentProduct product,
            InvestmentOrderStatus status
    );

    List<InvestmentOrder> findByEmailAddressAndStatusOrderByUpdatedAtDesc(String emailAddress,
            InvestmentOrderStatus status);

    // Optional: if you want all customers (admin)
    List<InvestmentOrder> findByStatusOrderByUpdatedAtDesc(InvestmentOrderStatus status);

    // Optional: filter by multiple statuses
    List<InvestmentOrder> findByEmailAddressAndStatusInOrderByUpdatedAtDesc(String emailAddress,
            List<InvestmentOrderStatus> statuses);

    Optional<InvestmentOrder> findByIdempotencyKeyAndEmailAddress(String key, String email);

}
