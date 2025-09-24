/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.finacial.wealth.api.profiling.repo;

import com.finacial.wealth.api.profiling.domain.DeviceChangeLimitConfig;
import com.finacial.wealth.api.profiling.domain.DeviceDetails;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

/**
 *
 * @author olufemioshin
 */
public interface DeviceDetailsRepo extends
        CrudRepository<DeviceDetails, String> {

}
