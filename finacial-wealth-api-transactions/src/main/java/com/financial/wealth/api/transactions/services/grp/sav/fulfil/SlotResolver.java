/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.financial.wealth.api.transactions.services.grp.sav.fulfil;

/**
 *
 * @author olufemioshin
 */
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.financial.wealth.api.transactions.domain.GroupSavingsData;
import com.financial.wealth.api.transactions.models.AddMembersModels;
import com.financial.wealth.api.transactions.models.SwapSlot;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class SlotResolver {

    private SlotResolver() {
    }

   /* public static String resolveReceiverWallet(GroupSavingsData group, int cycleNumber) throws JsonProcessingException {
        // List<AddMembersModels> members = YourJsonUtil.parseAddMembers(group.getAddedMembersModels()); // your existing util
        ObjectMapper mapper = new ObjectMapper();
        List<AddMembersModels> members = mapper.readValue(group.getAddedMembersModels(), new TypeReference<List<AddMembersModels>>() {
        });
        Map<Integer, String> slotToWallet = new HashMap<Integer, String>();

        for (AddMembersModels m : members) {
            int effectiveSlot = m.getSlot();
            SwapSlot sw = m.getSwapSlot();
            if (sw != null && "1".equals(sw.getIsSwapped())) {
                // If your business rule adjusts slot here, apply it; otherwise assume m.getSlot() already reflects swap.
            }
            slotToWallet.put(effectiveSlot, m.getMemberId()); // walletId
        }
        return slotToWallet.get(cycleNumber);
    }*/

    public static String resolveReceiverWallet(GroupSavingsData group, int cycleNumber) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            List<AddMembersModels> members = mapper.readValue(
                    group.getAddedMembersModels(),
                    new com.fasterxml.jackson.core.type.TypeReference<List<AddMembersModels>>() {
            }
            );

            // Return the memberId (walletId) whose slot matches cycleNumber
            for (AddMembersModels m : members) {
                if (m.getSlot() == cycleNumber) {
                    return m.getMemberId(); // walletId
                }
            }
            throw new IllegalStateException("No member has slot " + cycleNumber + " for group " + group.getId());
        } catch (Exception e) {
            throw new IllegalStateException("Failed to resolve receiver wallet", e);
        }
    }
}
