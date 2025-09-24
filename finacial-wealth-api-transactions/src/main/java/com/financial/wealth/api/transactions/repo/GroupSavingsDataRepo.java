/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.financial.wealth.api.transactions.repo;

import com.financial.wealth.api.transactions.domain.GroupSavingsData;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

/**
 *
 * @author olufemioshin
 */
@Repository
public interface GroupSavingsDataRepo extends JpaRepository<GroupSavingsData, Long> {

    /*@Query("select ud from GroupSavingsData ud where ud.initiator=:initiator and ud.emailAddress=:emailAddress")
    List<GroupSavingsData> findByInitiatorAndReceiverEmailAddress(String initiator, String emailAddress);*/

    @Query("select ud from GroupSavingsData ud where ud.emailAddress=:emailAddress")
    List<GroupSavingsData> findByEmailAddress(String emailAddress);

    @Query("select ud from GroupSavingsData ud where ud.emailAddress=:emailAddress order by ud.id desc")
    List<GroupSavingsData> findByEmailAddressOrdered(String emailAddress);

    @Query("select ud from GroupSavingsData ud where ud.phoneNumber=:phoneNumber order by ud.id desc")
    List<GroupSavingsData> findByPhoneNumberOrdered(String phoneNumber);

    @Query("select ud from GroupSavingsData ud where ud.transactionId=:transactionId")
    List<GroupSavingsData> findByTransactionId(String transactionId);

    @Query("select bs from GroupSavingsData bs where bs.transactionId=:transactionId")
    GroupSavingsData findByTransactionIdDe(String transactionId);

    @Query("select ud from GroupSavingsData ud where ud.inviteCode=:inviteCode")
    List<GroupSavingsData> findByInviteCode(String inviteCode);
    
     @Query("select ud from GroupSavingsData ud where ud.inviteCode=:inviteCode and ud.emailAddress=:emailAddress")
    List<GroupSavingsData> findByInviteCodeAndEmailAddress(String inviteCode, String emailAddress);

    @Query("select bs from GroupSavingsData bs where bs.inviteCode=:inviteCode")
    GroupSavingsData findByInviteCodeDe(String inviteCode);
    
     @Query("select ud from GroupSavingsData ud where ud.transactionIdLink=:transactionIdLink")
    List<GroupSavingsData> findByTransactionIdLink(String transactionIdLink);


    @Query("select ud from GroupSavingsData ud where ud.transactionIdLink=:transactionIdLink and ud.emailAddress=:emailAddress")
    List<GroupSavingsData> findByTransactionIdLinkAndEmailAddress(String transactionIdLink, String emailAddress);

    @Query("select bs from GroupSavingsData bs where bs.transactionIdLink=:transactionIdLink")
    GroupSavingsData findByTransactionIdLinkDe(String transactionIdLink);

}
