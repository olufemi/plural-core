/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.finacial.wealth.api.utility.repository;

import com.finacial.wealth.api.utility.domains.RegWalletCheckLog;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

/**
 *
 * @author olufemioshin
 */
public interface RegWalletCheckLogRepo extends
        CrudRepository<RegWalletCheckLog, String> {

    @Query("select ud from RegWalletCheckLog ud where ud.custID=:custID")
    List<RegWalletCheckLog> findByCusId(String custID);

    @Query("select bs from RegWalletCheckLog bs where bs.custID=:custID")
    RegWalletCheckLog findByWalletcustID(String custID);

}
