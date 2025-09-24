/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.financial.wealth.api.transactions.repo;

import com.financial.wealth.api.transactions.domain.AcceptQuoteResponseFailed;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 *
 * @author olufemioshin
 */
public interface AcceptQuoteResponseFailedRepo extends JpaRepository<AcceptQuoteResponseFailed, Long> {
    
}
