/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.finacial.wealth.api.utility.repository;

import com.finacial.wealth.api.utility.domains.StaticDataConfig;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

/**
 *
 * @author HRH
 */
public interface StaticDataConfigRepository extends JpaRepository<StaticDataConfig, Long> {

    @Query("select g from StaticDataConfig g where g.groupName=:groupName")
    List<StaticDataConfig> findByGroupName(String groupName);

}
