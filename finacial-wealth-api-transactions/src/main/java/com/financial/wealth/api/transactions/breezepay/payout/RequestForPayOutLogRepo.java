/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.financial.wealth.api.transactions.breezepay.payout;

import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

/**
 *
 * @author olufemioshin
 */
public interface RequestForPayOutLogRepo extends
        CrudRepository<RequestForPayOutLog, String> {

    @Query("SELECT u FROM RequestForPayOutLog u where u.processId = :processId and processIdStatus = :processIdStatus")
    List<RequestForPayOutLog> findByProcesIdProcessStatus(@Param("processId") String processId, @Param("processIdStatus") String processIdStatus);

    @Query("SELECT u FROM RequestForPayOutLog u where u.processId = :processId and processIdStatus = :processIdStatus")
    RequestForPayOutLog findByProcesIdProcessStatusDe(@Param("processId") String processId, @Param("processIdStatus") String processIdStatus);

}
