/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.financial.wealth.api.transactions.repo;


import com.financial.wealth.api.transactions.domain.LocalBeneficiariesIndividual;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

/**
 *
 * @author olufemioshin
 */
public interface LocalBeneficiariesIndividualRepo extends
        CrudRepository<LocalBeneficiariesIndividual, String> {
//SELECT u.beneficiary_name,u.beneficiary_no, count(u.beneficiary_name) as trans_count FROM kuleanpayment.local_t_b_fic_individual u where u.wallet_no = '08071201950' and u.beneficiary_status = '1' and datediff(current_date(), u.created_date) > 20 group by u.beneficiary_name,u.beneficiary_no having trans_count >=3;

    @Query("SELECT u FROM LocalBeneficiariesIndividual u where u.walletNo = :walletNo and u.beneficiaryStatus = :beneficiaryStatus and datediff(current_date(), u.createdDate) < 6 group by u.beneficiaryName having count(u.beneficiaryName) >=3")
    List<LocalBeneficiariesIndividual> findByWalletNoActive(@Param("walletNo") String walletNo, @Param("beneficiaryStatus") String beneficiaryStatus);

    /* @Query("SELECT u FROM LocalBeneficiariesIndividual u where u.walletNo = :walletNo and u.beneficiaryStatus = :beneficiaryStatus")
    List<LocalBeneficiariesIndividual> findByWalletNoActive(@Param("walletNo") String walletNo, @Param("beneficiaryStatus") String beneficiaryStatus);*/
    @Query("SELECT u FROM LocalBeneficiariesIndividual u where u.walletNo = :walletNo and u.beneficiaryNo = :beneficiaryNo and u.beneficiaryStatus = :beneficiaryStatus")
    LocalBeneficiariesIndividual findByWalletNoByBeneficiaryActiveUpdate(@Param("walletNo") String walletNo, @Param("beneficiaryNo") String beneficiaryNo, @Param("beneficiaryStatus") String beneficiaryStatus);
    
    boolean existsByWalletNo(String walletNo);

}
