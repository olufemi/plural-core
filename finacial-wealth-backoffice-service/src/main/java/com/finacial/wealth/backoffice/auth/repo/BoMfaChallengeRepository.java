/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.finacial.wealth.backoffice.auth.repo;

import com.finacial.wealth.backoffice.auth.entity.BoMfaChallenge;
import feign.Param;
import java.time.Instant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

/**
 *
 * @author olufemioshin
 */
public interface BoMfaChallengeRepository extends JpaRepository<BoMfaChallenge, String> {

    @Modifying
    @Query("delete from BoMfaChallenge c where c.expiresAt < :now or c.used = true")
    int cleanup(@Param("now") Instant now);
}
