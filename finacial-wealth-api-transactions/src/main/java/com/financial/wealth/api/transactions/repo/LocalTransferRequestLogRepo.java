/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.financial.wealth.api.transactions.repo;

import com.financial.wealth.api.transactions.domain.LocalTransferRequestLog;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

/**
 *
 * @author olufemioshin
 */
public interface LocalTransferRequestLogRepo  extends
        CrudRepository<LocalTransferRequestLog, String> {

    @Query("SELECT u FROM LocalTransferRequestLog u where u.processId = :processId and processIdStatus = :processIdStatus")
    List<LocalTransferRequestLog> findByProcesIdProcessStatus(@Param("processId") String processId, @Param("processIdStatus") String processIdStatus);

    @Query("SELECT u FROM LocalTransferRequestLog u where u.processId = :processId and processIdStatus = :processIdStatus")
    LocalTransferRequestLog findByProcesIdProcessStatusDe(@Param("processId") String processId, @Param("processIdStatus") String processIdStatus);


    
}
