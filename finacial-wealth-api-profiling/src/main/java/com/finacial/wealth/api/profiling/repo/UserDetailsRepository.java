/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.finacial.wealth.api.profiling.repo;

import com.finacial.wealth.api.profiling.domain.UserDetails;
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
public interface UserDetailsRepository extends
        CrudRepository<UserDetails, String> {

    boolean existsByEmailAddress(String emailAddress);

    boolean existsByUniqueIdentification(String phoneNumber);

    boolean existsByUserName(String userName);

    boolean existsByOneTimePwd(String oneTimePwd);

    @Query("SELECT u FROM UserDetails u where u.userName = :userName")
    Optional<UserDetails> findByUserName(@Param("userName") String userName);

    @Query("SELECT u FROM UserDetails u where u.uniqueIdentification = :uniqueIdentification")
    Optional<UserDetails> findByUserId(@Param("uniqueIdentification") String id);

    @Query("SELECT u FROM UserDetails u where u.emailAddress = :emailAddress")
    Optional<UserDetails> findByUserEmailId(@Param("emailAddress") String emailAddress);

    @Query("SELECT u FROM UserDetails u where u.emailAddress = :emailAddress")
    List<UserDetails> findByUserEmailAddressData(@Param("emailAddress") String emailAddress);

    @Query("SELECT u FROM UserDetails u where u.emailAddress = :emailAddress")
    UserDetails findByUserEmailAddress(@Param("emailAddress") String emailAddress);

    @Query("SELECT u FROM UserDetails u where u.uniqueIdentification = :uniqueIdentification")
    UserDetails findByUniqueIdentification(@Param("uniqueIdentification") String id);

    @Query("SELECT u FROM UserDetails u where u.uniqueIdentification = :uniqueIdentification")
    Optional<UserDetails> findByUniqueId(@Param("uniqueIdentification") String id);

    @Transactional
    @Modifying
    @Query("DELETE FROM UserDetails u where u.id = :id")
    void deleteUser(@Param("id") String id);

    @Query("SELECT g.userGroup FROM UserDetails g where g.uniqueIdentification = :uniqueIdentification")
    public String findUserGroupId(@Param("uniqueIdentification") String id);

    /*
    @Query("select user.firstName, user.lastName from UserEntity user where user.userId = :userId")
List<Object[]> getUserEntityFullNameById(@Param("userId") String userId);
    
    
    @Query("select u.roleId from UserRoles u where u.id = :id")
    public String findUserRoleId(@Param("id") String id);
     */
}
