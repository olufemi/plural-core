/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.finacial.wealth.api.fxpeer.exchange.repo;

/**
 *
 * @author olufemioshin
 */
import com.finacial.wealth.api.fxpeer.exchange.domain.DeviceChallengeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface DeviceChallengeRepo extends JpaRepository<DeviceChallengeEntity, String> {
    Optional<DeviceChallengeEntity> findByIdAndUserId(String id, String userId);
}
