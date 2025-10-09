/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.financial.wealth.api.transactions.repo;

import com.financial.wealth.api.transactions.domain.OtherBankBeneficiaries;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

/**
 *
 * @author olufemioshin
 */
public interface OtherBankBeneficiariesRepo extends
        CrudRepository<OtherBankBeneficiaries, String> {

    boolean existsByBeneficiaryNo(String beneficiaryNo);

    @Query("SELECT u FROM OtherBankBeneficiaries u where u.walletNo = :walletNo and u.beneficiaryNo = :beneficiaryNo and u.beneficiaryStatus = :beneficiaryStatus")
    List<OtherBankBeneficiaries> findByWalletNoByBeneficiaryActive(@Param("walletNo") String walletNo, @Param("beneficiaryNo") String beneficiaryNo, @Param("beneficiaryStatus") String beneficiaryStatus);

    @Query("SELECT u FROM OtherBankBeneficiaries u where u.walletNo = :walletNo and u.beneficiaryStatus = :beneficiaryStatus")
    List<OtherBankBeneficiaries> findByWalletNoActive(@Param("walletNo") String walletNo, @Param("beneficiaryStatus") String beneficiaryStatus);

    @Query("SELECT u FROM OtherBankBeneficiaries u where u.walletNo = :walletNo and u.beneficiaryNo = :beneficiaryNo and u.beneficiaryStatus = :beneficiaryStatus")
    OtherBankBeneficiaries findByWalletNoByBeneficiaryActiveUpdate(@Param("walletNo") String walletNo, @Param("beneficiaryNo") String beneficiaryNo, @Param("beneficiaryStatus") String beneficiaryStatus);

}
