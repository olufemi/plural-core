/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.finacial.wealth.api.profiling.repo;
;
import com.finacial.wealth.api.profiling.domain.ReferralsLog;
import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

/**
 *
 * @author olufemioshin
 */
public interface ReferralsLogRepo extends
        CrudRepository<ReferralsLog, String> {

    @Query("select bs from ReferralsLog bs where bs.receiverNumber=:receiverNumber")
    ReferralsLog findByReceiverNumberDe(String receiverNumber);

    @Query("select bs from ReferralsLog bs where bs.receiverNumber=:receiverNumber")
    List<ReferralsLog> findByReceiverNumber(String receiverNumber);

}
