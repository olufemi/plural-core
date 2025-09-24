/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.finacial.wealth.api.profiling.repo;
import com.finacial.wealth.api.profiling.domain.VerifyReqIdDetailsAuth;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

/**
 *
 * @author olufemioshin
 */
public interface VerifyReqIdDetailsAuthRepo extends
        CrudRepository<VerifyReqIdDetailsAuth, String> {

    @Query("select ud from VerifyReqIdDetailsAuth ud where ud.requestId=:requestId")
    List<VerifyReqIdDetailsAuth> findByRequestId(String requestId);

    @Query("select ud from VerifyReqIdDetailsAuth ud where ud.processId=:processId")
    VerifyReqIdDetailsAuth findByProcessIdList(String processId);

    @Query("select ud from VerifyReqIdDetailsAuth ud where ud.processId=:processId")
    List<VerifyReqIdDetailsAuth> findByProcIdList(String processId);

    @Query("select bs from VerifyReqIdDetailsAuth bs where bs.requestId=:requestId")
    VerifyReqIdDetailsAuth findByRequestIdList(String requestId);

    boolean existsByRequestId(String requestId);

}
