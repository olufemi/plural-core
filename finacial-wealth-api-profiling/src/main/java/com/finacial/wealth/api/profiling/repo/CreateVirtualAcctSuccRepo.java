/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.finacial.wealth.api.profiling.repo;

import com.finacial.wealth.api.profiling.domain.CreateVirtualAcctSucc;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

/**
 *
 * @author olufemioshin
 */
public interface CreateVirtualAcctSuccRepo extends JpaRepository<CreateVirtualAcctSucc, Long> {

    @Query("select bs from CreateVirtualAcctSucc bs where bs.refrence=:refrence")
    CreateVirtualAcctSucc findByRefrence(String refrence);

    boolean existsByWalletNo(String walletNo);

    @Query("select bs from CreateVirtualAcctSucc bs where bs.refrence=:refrence and bs.accountNumber=:accountNumber")
    List<CreateVirtualAcctSucc> findByRefrenceAndVirtAcctNo(String refrence, String accountNumber);

    @Query("select bs from CreateVirtualAcctSucc bs where bs.walletNo=:walletNo ")
    List<CreateVirtualAcctSucc> findByWallettNoList(String walletNo);
    
    Optional<CreateVirtualAcctSucc> findByWalletNo(String wallettNo);

}
