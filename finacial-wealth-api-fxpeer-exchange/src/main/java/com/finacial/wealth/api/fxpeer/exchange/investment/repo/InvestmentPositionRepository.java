/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.finacial.wealth.api.fxpeer.exchange.investment.repo;

import com.finacial.wealth.api.fxpeer.exchange.investment.domain.InvestmentPosition;
import com.finacial.wealth.api.fxpeer.exchange.investment.domain.InvestmentProduct;
import org.springframework.data.repository.CrudRepository;

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

    @Query("""
        select p from InvestmentPosition p
        where p.emailAddress = :emailAddress 
          and p.status in (com.finacial.wealth.api.fxpeer.exchange.investment.ennum.InvestmentPositionStatus.ACTIVE,
                           com.finacial.wealth.api.fxpeer.exchange.investment.ennum.InvestmentPositionStatus.PARTIALLY_LIQUIDATED)
        """)
    List<InvestmentPosition> findActiveByEmailAddress(String emailAddress);

    @Query("""
    select p from InvestmentPosition p
    where p.emailAddress = :emailAddress
      and p.product.id = :productId
      and p.status = com.finacial.wealth.api.fxpeer.exchange.investment.ennum.InvestmentPositionStatus.ACTIVE
      and p.orderRef= :orderRef
""")
    Optional<InvestmentPosition> findActiveByEmailAddressAndProductAndOrderRef(
            @Param("emailAddress") String emailAddress,
            @Param("productId") Long productId, @Param("orderRef") String orderRef
    );

    @Query("""
    select p from InvestmentPosition p
    where p.status in (
       com.finacial.wealth.api.fxpeer.exchange.investment.ennum.InvestmentPositionStatus.ACTIVE,
       com.finacial.wealth.api.fxpeer.exchange.investment.ennum.InvestmentPositionStatus.PARTIALLY_LIQUIDATED
    )
""")
    List<InvestmentPosition> findAllActivePositions();

    Optional<InvestmentPosition> findByOrderRef(String orderRef);
    
   

}
