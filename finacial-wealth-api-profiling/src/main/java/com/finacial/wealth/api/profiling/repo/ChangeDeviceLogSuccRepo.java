/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.finacial.wealth.api.profiling.repo;


import com.finacial.wealth.api.profiling.domain.ChangeDeviceLogSucc;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

/**
 *
 * @author olufemioshin
 */
public interface ChangeDeviceLogSuccRepo extends
        CrudRepository<ChangeDeviceLogSucc, String> {

    @Query("SELECT u FROM ChangeDeviceLogSucc u where u.processId = :processId")
    List<ChangeDeviceLogSucc> findByProcessId(@Param("processId") String processId);

}
