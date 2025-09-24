/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.financial.wealth.api.transactions.repo;

import com.financial.wealth.api.transactions.domain.WToWaletTransfer;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

/**
 *
 * @author OSHIN
 */
public interface WToWaletTransferRepo extends
        CrudRepository<WToWaletTransfer, String> {

    boolean existsBySender(String sender);

    boolean existsByReceiver(String receiver);

    boolean existsByTransactionId(String transactionId);

    Optional<WToWaletTransfer> findByTransactionId(String transactionId);

    @Query("SELECT o FROM WToWaletTransfer o where o.sender = :sender and o.receiver = :receiver")
    List<WToWaletTransfer> findBySenderAndReceiver(@Param("sender") String sender, @Param("receiver") String receiver);

}
