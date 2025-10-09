/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.financial.wealth.api.transactions.breezepay.payout;

/**
 *
 * @author olufemioshin
 */
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface NipBankRepository extends JpaRepository<NipBank, Long> {
    Optional<NipBank> findByBankCode(String bankCode);
    List<NipBank> findAllByOrderByBankNameAsc();
}
