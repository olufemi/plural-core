/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.finacial.wealth.api.utility.repository;

import com.finacial.wealth.api.utility.domains.RegWalletInfoLogForConfirmation;
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
public interface RegWalletInfoLogForConfirmationRepo extends
        CrudRepository<RegWalletInfoLogForConfirmation, String> {

    boolean existsByPhoneNumber(String phoneNumber);
    
    boolean existsByTransactionId(String transactionId);

    boolean existsByPersonId(String personId);

    boolean existsByAccountNumber(String accountNumber);

    boolean existsByAccountName(String accountName);

    Optional<RegWalletInfoLogForConfirmation> findByPhoneNumber(String phoneNumber);

    Optional<RegWalletInfoLogForConfirmation> findByPersonId(String personId);

    Optional<RegWalletInfoLogForConfirmation> findByTransactionId(String transactionId);
    
    //@Query("select tal1 from WalletFundSucInfo tal1 where tal1.phoneNumber = :phoneNumber and tal1.Created = (select max(tal2.Created) from WalletFundSucInfo tal2 where tal2.phoneNumber = tal1.phoneNumber)")
     //@Query("select tal1 from RegWalletInfoLogForConfirmation tal1 where tal1.phoneNumber = :phoneNumber and tal1.created = (select max(tal2.created) from RegWalletInfoLogForConfirmation tal2 where tal2.phoneNumber = tal1.phoneNumber)")

    @Query("select tal1 from RegWalletInfoLogForConfirmation tal1 where tal1.phoneNumber = :phoneNumber and tal1.created = (select max(tal2.created) from RegWalletInfoLogForConfirmation tal2 where tal2.phoneNumber = tal1.phoneNumber)")
    Optional<RegWalletInfoLogForConfirmation> findDetailsByPhoneNumber(@Param("phoneNumber") String phoneNumber);
    
    /*
     @Query("select d from RegWalletInfoLogForConfirmation d where d.phoneNumber = :phoneNumber and d.transStatuscompleted = :status and d.created = (select max(d.created) from RegWalletInfoLogForConfirmation d2 where d2.phoneNumber = d.phoneNumber and d2.transStatuscompleted = d.transStatuscompleted)")
    Optional<RegWalletInfoLogForConfirmation> findDetailsByPhoneNumber(@Param("phoneNumber") String phoneNumber, @Param("status") boolean status);
    */

}
