/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.finacial.wealth.api.utility.repository;


import com.finacial.wealth.api.utility.domains.ScheduleTransacions;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

/**
 *
 * @author olufemioshin
 */
public interface ScheduleTransacionsRepo extends
        CrudRepository<ScheduleTransacions, String> {

    @Query("select config from ScheduleTransacions config where config.signature=:signature")
    List<ScheduleTransacions> findBySignature(String signature);

    @Query("select config from ScheduleTransacions config where config.walletNo=:walletNo")
    List<ScheduleTransacions> findByWalletNo(String walletNo);

    @Query("select config from ScheduleTransacions config where config.walletNo=:walletNo")
    ScheduleTransacions findByWalletNoDe(String walletNo);

    @Query("select config from ScheduleTransacions config where config.walletNo=:walletNo and config.processId=:processId")
    List<ScheduleTransacions> findByWalletNoAndProcessId(String walletNo, String processId);

    @Query("select config from ScheduleTransacions config where config.walletNo=:walletNo and config.processId=:processId")
    ScheduleTransacions findByWalletNoAndProcessIdDe(String walletNo, String processId);

    @Query("select bs from ScheduleTransacions bs where bs.status=:status")
    List<ScheduleTransacions> findByStatus(String status);

    @Query("select bs from ScheduleTransacions bs where bs.processId=:processId")
    ScheduleTransacions findByProcessId(String processId);

}
