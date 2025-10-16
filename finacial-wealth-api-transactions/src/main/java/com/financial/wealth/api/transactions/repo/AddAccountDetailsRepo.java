/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.financial.wealth.api.transactions.repo;

import com.financial.wealth.api.transactions.breezepay.payout.AddAccountDetails;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 *
 * @author olufemioshin
 */
public interface AddAccountDetailsRepo extends JpaRepository<AddAccountDetails, Long> {

    boolean existsByAccountNumber(String phoneNumber);

    boolean existsByWalletId(String walletId);

    @Query("SELECT u FROM AddAccountDetails u where u.countryCode = :countryCode and u.accountNumber = :accountNumber")
    List<AddAccountDetails> findByCountryCodeByAccountNumber(@Param("countryCode") String countryCode, @Param("accountNumber") String accountNumber);

    @Query("SELECT u FROM AddAccountDetails u where u.countryCode = :countryCode and u.emailAddress = :emailAddress")
    List<AddAccountDetails> findByCountryCodeByEmailAddress(@Param("countryCode") String countryCode, @Param("emailAddress") String emailAddress);

    @Query("SELECT u FROM AddAccountDetails u where  u.emailAddress = :emailAddress")
    List<AddAccountDetails> findByEmailAddress(@Param("emailAddress") String emailAddress);

    @Query("select bs from AddAccountDetails bs where bs.walletId=:walletId")
    List<AddAccountDetails> findByWalletId(String walletId);

    @Query("select bs from AddAccountDetails bs where bs.virtualAccountNumber=:virtualAccountNumber")
    List<AddAccountDetails> findByVirtualAccountNumberList(String virtualAccountNumber);

    @Query("select bs from AddAccountDetails bs where bs.accountNumber=:accountNumber")
    List<AddAccountDetails> findByAccountNumberList(String accountNumber);

}
