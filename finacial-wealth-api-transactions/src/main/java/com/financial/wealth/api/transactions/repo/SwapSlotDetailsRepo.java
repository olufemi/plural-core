/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.financial.wealth.api.transactions.repo;

import com.financial.wealth.api.transactions.domain.GroupSavingsData;
import com.financial.wealth.api.transactions.domain.SettlementFailureLog;
import com.financial.wealth.api.transactions.domain.SwapSlotDetails;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

/**
 *
 * @author olufemioshin
 */
public interface SwapSlotDetailsRepo extends
        CrudRepository<SwapSlotDetails, String> {

    @Query("select ud from SwapSlotDetails ud where ud.receiverEmailAddress=:receiverEmailAddress")
    List<SwapSlotDetails> findByReceiverEmailAddress(String receiverEmailAddress);

    @Query("select ud from SwapSlotDetails ud where ud.invitationCodeReqId=:invitationCodeReqId")
    List<SwapSlotDetails> findByInvitationCodeReqId(String invitationCodeReqId);

    @Query("select ud from SwapSlotDetails ud where ud.receiverEmailAddress=:receiverEmailAddress and ud.invitationCodeReqId=:invitationCodeReqId")
    List<SwapSlotDetails> findByReceiverEmailAddressAndInvitationCodeReqId(String receiverEmailAddress, String invitationCodeReqId);

    @Query("select ud from SwapSlotDetails ud where ud.senderEmailAddress=:senderEmailAddress and ud.memberId=:memberId and ud.invitationCodeReqId=:invitationCodeReqId")
    List<SwapSlotDetails> findBySenderEmailAddressAndMemberIdAndInvitationCodeReqId(String senderEmailAddress, String memberId, String invitationCodeReqId);

    @Query("select ud from SwapSlotDetails ud where ud.senderEmailAddress=:senderEmailAddress and ud.memberId=:memberId and ud.invitationCodeReqId=:invitationCodeReqId")
    SwapSlotDetails findBySenderEmailAddressAndMemberIdAndInvitationCodeReqIdUpdate(String senderEmailAddress, String memberId, String invitationCodeReqId);

}
