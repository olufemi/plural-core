/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.finacial.wealth.api.profiling.repo;

import com.finacial.wealth.api.profiling.domain.UserGroup;
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
public interface UserGroupRepository extends
        CrudRepository<UserGroup, String> {

    boolean existsByUserGroupName(String userGroupName);
    
    boolean existsByUserGroupId(String userGroupId);
    @Query("SELECT u FROM UserGroup u where u.id = :id")
    Optional<UserGroup> findAllByGroupId(@Param("id") String id);

    @Transactional
    @Modifying
    @Query("DELETE FROM UserGroup u where u.id = :id")
    void deleteUserGroup(@Param("id") String id);

    @Query("select u.userGroupRoles from UserGroup u where u.userGroupId = :userGroupId")
    public String findUserGroupRoleId(@Param("userGroupId") String id);
    
   
}
