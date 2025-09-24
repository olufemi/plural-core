/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.finacial.wealth.api.utility.repository;

import com.finacial.wealth.api.utility.domains.BvnNumberLog;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

/**
 *
 * @author olufemioshin
 */
public interface BvnNumberLogRepo extends JpaRepository<BvnNumberLog, Long> {

    @Query("select bs from BvnNumberLog bs where bs.id=:id")
    BvnNumberLog findByBvnLogId(Long id);

    @Query("select bs from BvnNumberLog bs where bs.requestId=:requestId")
    List<BvnNumberLog> findByRequestId(String requestId);

    @Query("select ud from BvnNumberLog ud where ud.bvn=:bvn")
    List<BvnNumberLog> findByBvn(String bvn);
}
