/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.finacial.wealth.api.fxpeer.exchange.investment.repo;

import com.finacial.wealth.api.fxpeer.exchange.investment.domain.InvestmentOrder;
import com.finacial.wealth.api.fxpeer.exchange.investment.domain.InvestmentPosition;
import com.finacial.wealth.api.fxpeer.exchange.investment.domain.InvestmentProduct;
import com.finacial.wealth.api.fxpeer.exchange.investment.domain.InvestmentRequestGuard;
import com.finacial.wealth.api.fxpeer.exchange.investment.ennum.InvestmentOrderStatus;
import com.finacial.wealth.api.fxpeer.exchange.investment.ennum.InvestmentOrderType;
import com.finacial.wealth.api.fxpeer.exchange.offer.Offer;
import jakarta.persistence.LockModeType;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Lock;
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

    @Query("SELECT u FROM InvestmentOrder u where u.orderRef = :orderRef and u.emailAddress = :emailAddress")
    InvestmentOrder findByOrderRefAndEmailAddressUpdate(@Param("orderRef") String orderRef, @Param("emailAddress") String emailAddress);

    /*@Query("""
        select p from InvestmentOrder p
        where p.emailAddress = :emailAddress 
          and p.status in (com.finacial.wealth.api.fxpeer.exchange.investment.ennum.InvestmentOrderStatus.ACTIVE)
        """)
   List<InvestmentOrder> findActiveByEmailAddress(String emailAddress);*/
    List<InvestmentOrder> findByEmailAddressAndTypeAndStatusOrderByUpdatedAtDesc(
            String emailAddress,
            InvestmentOrderType type,
            InvestmentOrderStatus status
    );

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

    @Query("""
    select g from InvestmentOrder g
    where g.emailAddress = :email
      and g.parentOrderRef = :orderRef
      and g.type in :types
      and g.createdAt >= :since
    order by g.createdAt desc
""")
    List<InvestmentOrder> findRecent(
            @Param("email") String email,
            @Param("orderRef") String orderRef,
            @Param("types") List<InvestmentOrderType> types,
            @Param("since") Instant since
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select o from InvestmentOrder o where o.orderRef = :orderRef")
    Optional<InvestmentOrder> lockByOrderRef(@Param("orderRef") String orderRef);

    // ===== LIQUIDATION HISTORY FOR A POSITION (parentOrderRef) =====
// 1) All liquidations for a subscription/position (history)
    List<InvestmentOrder> findByParentOrderRefAndTypeOrderByCreatedAtDesc(
            String parentOrderRef,
            InvestmentOrderType type
    );

// 2) Latest liquidation for a subscription/position
    Optional<InvestmentOrder> findTopByParentOrderRefAndTypeOrderByCreatedAtDesc(
            String parentOrderRef,
            InvestmentOrderType type
    );

// 3) Open liquidations (pending approval + processing) for a position
    List<InvestmentOrder> findByParentOrderRefAndTypeAndStatusInOrderByCreatedAtDesc(
            String parentOrderRef,
            InvestmentOrderType type,
            List<InvestmentOrderStatus> statuses
    );

// 4) Fast “exists” check to prevent too many concurrent liquidations (optional rule)
    boolean existsByParentOrderRefAndTypeAndStatusIn(
            String parentOrderRef,
            InvestmentOrderType type,
            List<InvestmentOrderStatus> statuses
    );

    @Query("""
       SELECT o
       FROM InvestmentOrder o
       WHERE o.emailAddress = :email
         AND o.status = :status
         AND o.type = :type
         AND (:parentOrderRef IS NULL OR o.parentOrderRef = :parentOrderRef)
       ORDER BY o.updatedAt DESC
       """)
    Page<InvestmentOrder> findSettledLiquidationsByEmail(
            @Param("email") String email,
            @Param("status") InvestmentOrderStatus status,
            @Param("type") InvestmentOrderType type,
            @Param("parentOrderRef") String parentOrderRef,
            Pageable pageable
    );

    boolean existsByEmailAddressAndParentOrderRefAndTypeAndStatus(
            String emailAddress,
            String parentOrderRef,
            InvestmentOrderType type,
            InvestmentOrderStatus status
    );

    boolean existsByEmailAddressAndParentOrderRefAndTypeAndStatusIn(
            String emailAddress,
            String parentOrderRef,
            InvestmentOrderType type,
            List<InvestmentOrderStatus> statuses
    );

    @Query("""
       SELECT o
       FROM InvestmentOrder o
       WHERE o.emailAddress = :email
         AND o.status IN :statuses
         AND o.type = :type
         AND (:parentOrderRef IS NULL OR o.parentOrderRef = :parentOrderRef)
       ORDER BY o.updatedAt DESC
       """)
    Page<InvestmentOrder> findLiquidationsByEmail(
            @Param("email") String email,
            @Param("statuses") List<InvestmentOrderStatus> statuses,
            @Param("type") InvestmentOrderType type,
            @Param("parentOrderRef") String parentOrderRef,
            Pageable pageable
    );

    List<InvestmentOrder> findByTypeAndStatusIn(
            InvestmentOrderType type,
            List<InvestmentOrderStatus> statuses
    );

    @Query("""
       SELECT o
       FROM InvestmentOrder o
       JOIN o.product p
       WHERE o.type = com.finacial.wealth.api.fxpeer.exchange.investment.ennum.InvestmentOrderType.LIQUIDATION
         AND o.status IN :statuses
         AND (:productCode IS NULL OR p.productCode = :productCode)
         AND (:fromDate IS NULL OR o.createdAt >= :fromDate)
         AND (:toDate IS NULL OR o.createdAt < :toDate)
       ORDER BY o.createdAt DESC
       """)
    List<InvestmentOrder> findAdminLiquidations(
            @Param("statuses") List<InvestmentOrderStatus> statuses,
            @Param("productCode") String productCode,
            @Param("fromDate") Instant fromDate,
            @Param("toDate") Instant toDate
    );

    @Query("""
       SELECT o
       FROM InvestmentOrder o
       JOIN o.product p
       WHERE o.type IN :types
         AND o.status IN :statuses
         AND (:productCode IS NULL OR p.productCode = :productCode)
         AND (:fromDate IS NULL OR o.createdAt >= :fromDate)
         AND (:toDate IS NULL OR o.createdAt < :toDate)
       ORDER BY o.createdAt DESC
       """)
    List<InvestmentOrder> findAdminOrders(
            @Param("types") List<InvestmentOrderType> types,
            @Param("statuses") List<InvestmentOrderStatus> statuses,
            @Param("productCode") String productCode,
            @Param("fromDate") Instant fromDate,
            @Param("toDate") Instant toDate
    );

    @Query("""
       SELECT o
       FROM InvestmentOrder o
       JOIN o.product p
       WHERE o.emailAddress = :email
         AND o.type = com.finacial.wealth.api.fxpeer.exchange.investment.ennum.InvestmentOrderType.LIQUIDATION
         AND o.status IN :statuses
       ORDER BY o.createdAt DESC
       """)
    List<InvestmentOrder> findAdminCustomerLiquidations(
            @Param("email") String email,
            @Param("statuses") List<InvestmentOrderStatus> statuses
    );

    @Query("""
       SELECT o
       FROM InvestmentOrder o
       JOIN o.product p
       WHERE o.emailAddress = :email
         AND o.type IN :types
         AND o.status IN :statuses
       ORDER BY o.createdAt DESC
       """)
    List<InvestmentOrder> findAdminCustomerOrders(
            @Param("email") String email,
            @Param("types") List<InvestmentOrderType> types,
            @Param("statuses") List<InvestmentOrderStatus> statuses
    );
}
