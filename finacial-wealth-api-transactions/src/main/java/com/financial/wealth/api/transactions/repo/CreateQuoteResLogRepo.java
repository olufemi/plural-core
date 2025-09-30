/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.financial.wealth.api.transactions.repo;

import com.financial.wealth.api.transactions.domain.CreateQuoteResLog;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

/**
 *
 * @author olufemioshin
 */
public interface CreateQuoteResLogRepo extends
        CrudRepository<CreateQuoteResLog, String> {

    @Query("SELECT u FROM CreateQuoteResLog u where u.quoteId = :quoteId")
    List<CreateQuoteResLog> findByQuoteId(@Param("quoteId") String quoteId);

    @Query("SELECT u FROM CreateQuoteResLog u where u.quoteId = :quoteId")
    CreateQuoteResLog findByQuoteIdUpdate(@Param("quoteId") String quoteId);

    Optional<CreateQuoteResLog> findFirstByQuoteIdAndStatusAndIsAcceptedAndCreateQuoteResponseIsNotNull(
            String quoteId, String status, String isAccepted
    );

    Page<CreateQuoteResLog> findByEmailIgnoreCaseAndStatusAndIsAcceptedAndCreateQuoteResponseIsNotNull(
            String email, String status, String isAccepted, Pageable pageable
    );

    // List (ordered newest first)
    @Query("select c "
            + "from CreateQuoteResLog c "
            + "where c.quoteId is not null "
            + // not null (add trim check below if you want non-empty)
            "  and (c.isDebited is null or c.isDebited <> '1') "
            + "  and (c.isDebitedDescription is null or upper(c.isDebitedDescription) <> 'PROCESSED') "
            + "  and c.isAccepted = '1' "
            + "  and upper(c.paymentType) = 'WITHDRWAWAL' "
            + "order by c.id desc")
    List<CreateQuoteResLog> findDebitPendingAndAccepted();

    // 1) Strict equality version (use when createQuoteResponse is a plain status field)
    @Query("select c "
            + "from CreateQuoteResLog c "
            + "where c.quoteId is not null "
            + "  and c.isAccepted = '1' "
            + "  and upper(c.status) = 'PENDING' "
            + "  and upper(c.paymentType) = 'DEPOSIT' "
            + "  and c.createQuoteResponse = 'PENDING' "
            + "order by c.id desc")
    List<CreateQuoteResLog> findAcceptedPendingDepositWithResponsePending();

}
