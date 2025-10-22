/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.finacial.wealth.api.fxpeer.exchange.domain;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 *
 * @author OSHIN
 */
@Repository
public interface RegWalletInfoRepository extends
        CrudRepository<RegWalletInfo, String> {

    boolean existsByPhoneNumber(String phoneNumber);

    boolean existsByUuid(String uuid);

    boolean existsByEmail(String email);

    boolean existsByPersonId(String personId);

    boolean existsByAccountNumber(String accountNumber);

    boolean existsByAccountName(String accountName);

    @Query("SELECT u FROM RegWalletInfo u where u.phoneNumber = :phoneNumber and u.uuid = :uuid")
    List<RegWalletInfo> findByPhoneNumberByUuid(@Param("phoneNumber") String phoneNumber, @Param("uuid") String uuid);

    @Query("SELECT u FROM RegWalletInfo u where u.walletId = :walletId and u.uuid = :uuid")
    List<RegWalletInfo> findByWalletIdByUuid(@Param("walletId") String walletId, @Param("uuid") String uuid);

    // Optional<RegWalletInfo> findByPhoneNumber(String phoneNumber);
    @Query("SELECT u FROM RegWalletInfo u where u.phoneNumber = :phoneNumber")
    List<RegWalletInfo> findByPhoneNumberData(@Param("phoneNumber") String phoneNumber);

   
    @Query("SELECT u FROM RegWalletInfo u where u.referralCode = :referralCode")
    List<RegWalletInfo> findByReferralCode(@Param("referralCode") String referralCode);

    @Query("SELECT u FROM RegWalletInfo u where u.email = :email")
    List<RegWalletInfo> findByEmailsList(@Param("email") String email);

    @Query("SELECT u FROM RegWalletInfo u where u.walletId = :walletId")
    List<RegWalletInfo> findByWalletIdList(@Param("walletId") String walletId);

    @Query("SELECT u FROM RegWalletInfo u where u.walletId = :walletId")
    RegWalletInfo findByWalletId(@Param("walletId") String walletId);
    
     @Query("SELECT u FROM RegWalletInfo u where u.walletId = :walletId")
    Optional<RegWalletInfo> findByWalletIdOptional(@Param("walletId") String walletId);

    Optional<RegWalletInfo> findByEmail(String email);

    Optional<RegWalletInfo> findByPersonId(String personId);

    @Query("select bs from RegWalletInfo bs where bs.phoneNumber=:phoneNumber")
    RegWalletInfo findByPhoneNumberId(String phoneNumber);

    Optional<RegWalletInfo> findByPhoneNumber(String phoneNumber);

}
