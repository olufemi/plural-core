/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.finacial.wealth.api.profiling.repo;

import com.finacial.wealth.api.profiling.domain.InvestmentPosition;

/**
 *
 * @author olufemioshin
 */
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface InvestmentPositionRepository extends JpaRepository<InvestmentPosition, Long> {

    Optional<InvestmentPosition> findByIdAndEmailAddress(Long id, String emailAddress);

    @Query("select p from InvestmentPosition p "
            + "where p.emailAddress = :emailAddress "
            + "and p.status in ("
            + "  com.finacial.wealth.api.profiling.models.InvestmentPositionStatus.ACTIVE, "
            + "  com.finacial.wealth.api.profiling.models.InvestmentPositionStatus.PARTIALLY_LIQUIDATED"
            + ")")
    List<InvestmentPosition> findActiveByEmailAddress(@Param("emailAddress") String emailAddress);

    @Query("select p from InvestmentPosition p "
            + "where p.emailAddress = :emailAddress "
            + "and p.product.id = :productId "
            + "and p.status = com.finacial.wealth.api.profiling.models.InvestmentPositionStatus.ACTIVE "
            + "and p.orderRef = :orderRef")
    Optional<InvestmentPosition> findActiveByEmailAddressAndProductAndOrderRef(
            @Param("emailAddress") String emailAddress,
            @Param("productId") Long productId,
            @Param("orderRef") String orderRef
    );

    @Query("select p from InvestmentPosition p "
            + "where p.status in ("
            + "  com.finacial.wealth.api.profiling.models.InvestmentPositionStatus.ACTIVE, "
            + "  com.finacial.wealth.api.profiling.models.InvestmentPositionStatus.PARTIALLY_LIQUIDATED"
            + ")")
    List<InvestmentPosition> findAllActivePositions();

    Optional<InvestmentPosition> findByOrderRef(String orderRef);

}
