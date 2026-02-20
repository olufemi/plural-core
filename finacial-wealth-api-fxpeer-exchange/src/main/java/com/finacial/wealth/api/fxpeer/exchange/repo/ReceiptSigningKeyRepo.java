/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.finacial.wealth.api.fxpeer.exchange.repo;

/**
 *
 * @author olufemioshin
 */
import com.finacial.wealth.api.fxpeer.exchange.domain.ReceiptSigningKeyEntity;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReceiptSigningKeyRepo extends JpaRepository<ReceiptSigningKeyEntity, String> {

    Optional<ReceiptSigningKeyEntity> findFirstByStatusOrderByCreatedAtDesc(ReceiptSigningKeyEntity.KeyStatus status);

    @Query("select k from ReceiptSigningKeyEntity k where k.status in ('ACTIVE','RETIRED') order by k.createdAt desc")
    List<ReceiptSigningKeyEntity> findAllForDistribution();
}
