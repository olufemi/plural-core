/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.financial.wealth.api.transactions.repo;

import com.financial.wealth.api.transactions.domain.FinWealthPaymentTransReport;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

/**
 *
 * @author olufemioshin
 */
public interface FinWealthPaymentTransReportRepo extends
        CrudRepository<FinWealthPaymentTransReport, String> {

    FinWealthPaymentTransReport findTopByOrderByIdDesc();

    @Query("SELECT o FROM FinWealthPaymentTransReport o where o.sender = :walletNo OR o.receiver = :walletNo")
    List<FinWealthPaymentTransReport> findByWalletNoList(@Param("walletNo") String walletNo);

    @Query("select tal1 from FinWealthPaymentTransReport tal1 where tal1.walletNo = :walletNo and tal1.createdDate = (select max(tal2.createdDate) from FinWealthPaymentTransReport tal2 where tal2.walletNo = tal1.walletNo)")
    Optional<FinWealthPaymentTransReport> findByWalletNo(@Param("walletNo") String walletNo);

    boolean existsByWalletNo(String walletNo);

    boolean existsByPaymentType(String paymentType);

    @Query("select tal1 from FinWealthPaymentTransReport tal1 where tal1.paymentType = :paymentType and tal1.createdDate = (select max(tal2.createdDate) from FinWealthPaymentTransReport tal2 where tal2.paymentType = tal1.paymentType)")
    Optional<FinWealthPaymentTransReport> findByPaymenttype(@Param("paymentType") String paymentType);

}
