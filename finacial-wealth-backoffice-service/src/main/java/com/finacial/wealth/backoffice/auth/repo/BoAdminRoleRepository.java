/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.finacial.wealth.backoffice.auth.repo;

/**
 *
 * @author olufemioshin
 */

import com.finacial.wealth.backoffice.auth.entity.BoAdminRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BoAdminRoleRepository extends JpaRepository<BoAdminRole, Long> {
    Optional<BoAdminRole> findByName(String name);
}
