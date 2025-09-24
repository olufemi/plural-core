/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.finacial.wealth.api.profiling.repo;

import com.finacial.wealth.api.profiling.domain.ReceiverHistoryInfo;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 *
 * @author OSHIN
 */
@Repository
public interface WalletHistoryInfoRepository extends
        CrudRepository<ReceiverHistoryInfo, String> {

}
