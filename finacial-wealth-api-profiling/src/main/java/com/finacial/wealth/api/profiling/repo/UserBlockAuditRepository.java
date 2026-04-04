/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.finacial.wealth.api.profiling.repo;

/**
 *
 * @author olufemioshin
 */

import com.finacial.wealth.api.profiling.service.profiling.bo.UserBlockAudit;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserBlockAuditRepository extends JpaRepository<UserBlockAudit, Long> {
}
