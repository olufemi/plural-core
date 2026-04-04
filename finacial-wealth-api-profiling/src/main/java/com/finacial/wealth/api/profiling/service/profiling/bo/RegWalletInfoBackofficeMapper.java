/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.finacial.wealth.api.profiling.service.profiling.bo;

/**
 *
 * @author olufemioshin
 */
import com.finacial.wealth.api.profiling.domain.RegWalletInfo;
import org.springframework.stereotype.Component;

@Component
public class RegWalletInfoBackofficeMapper {

    public RegWalletInfoBackofficeResponse toResponse(RegWalletInfo entity) {
        if (entity == null) {
            return null;
        }

        RegWalletInfoBackofficeResponse response = new RegWalletInfoBackofficeResponse();
        response.setId(entity.getId());
        response.setPersonId(entity.getPersonId());
        response.setFirstName(entity.getFirstName());
        response.setLastName(entity.getLastName());
        response.setMiddleName(entity.getMiddleName());
        response.setFullName(entity.getFullName());
        response.setEmail(entity.getEmail());
        response.setPhoneNumber(entity.getPhoneNumber());
        response.setIsOnboarded(entity.getIsOnboarded());
        response.setActivation(entity.isActivation());
        response.setAccountBankCode(entity.getAccountBankCode());
        response.setBankName(entity.getBankName());
        response.setBvnNumber(entity.getBvnNumber());
        response.setDateOfBirth(entity.getDateOfBirth());
        response.setClient(entity.getClient());
        response.setCustomerId(entity.getCustomerId());
        response.setUuid(entity.getUuid());
        response.setUserName(entity.getUserName());
        response.setEmailVerification(entity.isEmailVerification());
        response.setEmailCreation(entity.getEmailCreation());
        response.setLivePhotoUpload(entity.getLivePhotoUpload());
        response.setPhoneVerification(entity.getPhoneVerification());
        response.setWalletTier(entity.getWalletTier());
        response.setJoinTransactionId(entity.getJoinTransactionId());
        response.setUuidAllowUser(entity.getUuidAllowUser());
        response.setAccountName(entity.getAccountName());
        response.setWalletId(entity.getWalletId());
        response.setAccountNumber(entity.getAccountNumber());
        response.setCreated(entity.getCreated());
        response.setModified(entity.getModified());
        response.setCompleted(entity.isCompleted());
        response.setReferralCode(entity.getReferralCode());
        response.setReferralCodeLink(entity.getReferralCodeLink());
        response.setIsUserBlocked(entity.getIsUserBlocked());
        response.setCreatedDate(entity.getCreatedDate());
        response.setLastModifiedDate(entity.getLastModifiedDate());

        return response;
    }
}
