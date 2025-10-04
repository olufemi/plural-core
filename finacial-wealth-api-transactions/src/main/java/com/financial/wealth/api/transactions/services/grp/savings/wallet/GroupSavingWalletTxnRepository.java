/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.financial.wealth.api.transactions.services.grp.savings.wallet;

/**
 *
 * @author olufemioshin
 */
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GroupSavingWalletTxnRepository extends JpaRepository<GroupSavingWalletTransaction, String> {

    Optional<GroupSavingWalletTransaction> findByWalletIdAndIdempotencyRefAndType(
            String walletId, String idempotencyRef, GroupSavingWalletTxnType type
    );
}
