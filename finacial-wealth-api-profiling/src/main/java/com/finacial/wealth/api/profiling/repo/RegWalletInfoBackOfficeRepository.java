/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.finacial.wealth.api.profiling.repo;

import com.finacial.wealth.api.profiling.domain.RegWalletInfo;

import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 *
 * @author olufemioshin
 */
public interface RegWalletInfoBackOfficeRepository extends JpaRepository<RegWalletInfo, Long>, JpaSpecificationExecutor<RegWalletInfo> {


    Optional<RegWalletInfo> findByCustomerId(String customerId);

    Optional<RegWalletInfo> findByUuid(String uuid);

    Optional<RegWalletInfo> findByAccountNumber(String accountNumber);

    Page<RegWalletInfo> findByEmailContainingIgnoreCaseOrPhoneNumberContainingIgnoreCaseOrFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCaseOrCustomerIdContainingIgnoreCaseOrAccountNumberContainingIgnoreCase(
            String email,
            String phoneNumber,
            String firstName,
            String lastName,
            String customerId,
            String accountNumber,
            Pageable pageable
    );

    Page<RegWalletInfo> findByIsUserBlocked(String isUserBlocked, Pageable pageable);

}
