/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.finacial.wealth.api.utility.repository;

import com.finacial.wealth.api.utility.domains.UserRoles;
import java.util.List;
import java.util.Optional;
import javax.transaction.Transactional;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

/**
 *
 * @author OSHIN
 */
public interface UserRolesRepository extends
        CrudRepository<UserRoles, String> {

    Optional<UserRoles> findByUserRole(String userRole);

    boolean existsByUserRole(String userRole);

    @Query("select userR.roleId from UserRoles userR")
    public List<String> findRoleId();

    @Query("SELECT u FROM UserRoles u where u.roleId = :roleId")
    Optional<UserRoles> findAllByRoleId(@Param("roleId") String roleId);

    @Query("select u.roleId from UserRoles u where u.id = :id")
    public String findUserRoleId(@Param("id") String id);

    @Transactional
    @Modifying
    @Query("DELETE FROM UserRoles u where u.roleId = :roleId")
    void deleteUserRole(@Param("roleId") String roleId);

}
