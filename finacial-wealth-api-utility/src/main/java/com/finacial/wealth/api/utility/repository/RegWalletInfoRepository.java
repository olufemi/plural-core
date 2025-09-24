/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.finacial.wealth.api.utility.repository;

import com.finacial.wealth.api.utility.domains.RegWalletInfo;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 *
 * @author OSHIN
 */
@Repository
public interface RegWalletInfoRepository extends
        CrudRepository<RegWalletInfo, String> {

    boolean existsByPhoneNumber(String phoneNumber);

    boolean existsByPersonId(String personId);

    boolean existsByAccountNumber(String accountNumber);

    boolean existsByAccountName(String accountName);

    Optional<RegWalletInfo> findByPhoneNumber(String phoneNumber);

    Optional<RegWalletInfo> findByPersonId(String personId);

    boolean existsByUuid(String uuid);

    @Query("select bs from RegWalletInfo bs where bs.phoneNumber=:phoneNumber")
    RegWalletInfo findByPhoneNumberId(String phoneNumber);

    @Query("select bs from RegWalletInfo bs where bs.email=:email")
    List<RegWalletInfo> findByEmail(String email);

}
