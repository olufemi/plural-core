/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.financial.wealth.api.transactions.repo;

import com.financial.wealth.api.transactions.domain.OtherBankBeneficiariesInd;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 *
 * @author olufemioshin
 */
public interface OtherBankBeneficiariesIndRepo extends JpaRepository<OtherBankBeneficiariesInd, Long> {

    @Query("SELECT u FROM OtherBankBeneficiariesInd u where u.walletNo = :walletNo and u.beneficiaryStatus = :beneficiaryStatus and datediff(current_date(), u.createdDate) < 6 group by u.beneficiaryName having count(u.beneficiaryName) >=3")
    List<OtherBankBeneficiariesInd> findByWalletNoActive(@Param("walletNo") String walletNo, @Param("beneficiaryStatus") String beneficiaryStatus);

    boolean existsByWalletNo(String walletNo);

}
