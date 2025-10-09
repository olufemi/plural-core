/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.financial.wealth.api.transactions.breezepay.payout;

/**
 *
 * @author olufemioshin
 */
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

/**
 *
 * @author OSHIN
 */
public interface WToBankTransferRepo extends
        CrudRepository<WToBankTransfer, String> {

    boolean existsBySender(String sender);

    boolean existsByReceiver(String receiver);

    boolean existsByTransactionId(String transactionId);

    Optional<WToBankTransfer> findByTransactionId(String transactionId);
    @Query("SELECT o FROM WToBankTransfer o where o.sender = :sender and o.receiver = :receiver")
    List<WToBankTransfer> findBySenderAndReceiver(@Param("sender") String sender, @Param("receiver") String receiver);

}
