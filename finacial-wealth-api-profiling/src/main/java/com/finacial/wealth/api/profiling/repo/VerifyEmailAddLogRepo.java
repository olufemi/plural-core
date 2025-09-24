/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.finacial.wealth.api.profiling.repo;


import com.finacial.wealth.api.profiling.domain.VerifyEmailAddLog;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

/**
 *
 * @author olufemioshin
 */
public interface VerifyEmailAddLogRepo extends
        CrudRepository<VerifyEmailAddLog, String> {

    @Query("select ud from VerifyEmailAddLog ud where ud.requestId=:requestId")
    List<VerifyEmailAddLog> findByRequestId(String requestId);

    @Query("select bs from VerifyEmailAddLog bs where bs.requestId=:requestId")
    VerifyEmailAddLog findByRequestIdList(String requestId);

    boolean existsByRequestId(String requestId);

}
