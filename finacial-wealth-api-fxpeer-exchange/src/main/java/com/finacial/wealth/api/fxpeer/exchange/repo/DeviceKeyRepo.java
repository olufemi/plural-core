/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.finacial.wealth.api.fxpeer.exchange.repo;

/**
 *
 * @author olufemioshin
 */
import com.finacial.wealth.api.fxpeer.exchange.domain.DeviceKeyEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;

public interface DeviceKeyRepo extends JpaRepository<DeviceKeyEntity, Long> {

    Optional<DeviceKeyEntity> findFirstByUserIdAndStatusOrderByActivatedAtDesc(
            String userId, DeviceKeyEntity.Status status);

    Optional<DeviceKeyEntity> findByUserIdAndDeviceIdAndKidAndStatus(
            String userId, String deviceId, String kid, DeviceKeyEntity.Status status);

    List<DeviceKeyEntity> findByUserIdAndStatus(
            String userId, DeviceKeyEntity.Status status);

    Optional<DeviceKeyEntity> findByUserIdAndDeviceIdAndStatus(
            String userId, String deviceId, DeviceKeyEntity.Status status);
}
