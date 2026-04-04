/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.finacial.wealth.api.profiling.service.profiling.bo;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 *
 * @author olufemioshin
 */
public interface RegWalletInfoBackofficeServiceInter {
    Page<RegWalletInfoBackofficeResponse> getAll(Pageable pageable);

    RegWalletInfoBackofficeResponse getById(Long id);

    RegWalletInfoBackofficeResponse getByCustomerId(String customerId);

    RegWalletInfoBackofficeResponse getByUuid(String uuid);

    Page<RegWalletInfoBackofficeResponse> filter(
            String keyword,
            String customerId,
            String email,
            String phoneNumber,
            String accountNumber,
            String isUserBlocked,
            Pageable pageable
    );

    RegWalletInfoBackofficeResponse blockUser(Long id, BlockUserRequest request);

    RegWalletInfoBackofficeResponse unblockUser(Long id, BlockUserRequest request);
}
