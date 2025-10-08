/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.finacial.wealth.api.profiling.breezpay.virt.get.bvn;

/**
 *
 * @author olufemioshin
 */
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface BvnLookupRepository extends JpaRepository<BvnLookup, Long> {
    Optional<BvnLookup> findByBvn(String bvn);
}
