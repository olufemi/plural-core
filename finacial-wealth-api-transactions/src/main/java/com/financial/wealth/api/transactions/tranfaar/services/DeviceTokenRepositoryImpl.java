/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.financial.wealth.api.transactions.tranfaar.services;

/**
 *
 * @author olufemioshin
 */
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import org.springframework.stereotype.Repository;

import com.financial.wealth.api.transactions.domain.DeviceDetails;

import com.financial.wealth.api.transactions.domain.DeviceDetails;
import com.financial.wealth.api.transactions.repo.DeviceTokenRepositoryCustom;

@Repository
@Transactional
public class DeviceTokenRepositoryImpl implements DeviceTokenRepositoryCustom {

    @PersistenceContext
    private EntityManager em;

    @Override
    public void upsert(String walletId, String platform, String deviceToken) {
        DeviceDetails existing = em.createQuery(
                "select d from DeviceToken d where d.token = :t", DeviceDetails.class)
                .setParameter("t", deviceToken)
                .getResultStream().findFirst().orElse(null);

        if (existing != null) {
            // update owner or platform if changed
            boolean dirty = false;
            if (!walletId.equals(existing.getWalletId())) {
                existing.setWalletId(walletId);
                dirty = true;
            }
            DeviceDetails.Platform p = DeviceDetails.Platform.from(platform);
            if (existing.getPlatform() != p) {
                existing.setPlatform(p);
                dirty = true;
            }
            if (dirty) {
                em.merge(existing);
            }
        } else {
            DeviceDetails dt = new DeviceDetails();
            dt.setWalletId(walletId);
            dt.setPlatform(DeviceDetails.Platform.from(platform));
            dt.setToken(deviceToken);
            em.persist(dt);
        }
    }
}
