/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.finacial.wealth.api.fxpeer.exchange.receipt;

/**
 *
 * @author olufemioshin
 */
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;


public interface ReceiptRepository extends JpaRepository<Receipt, Long> {
Optional<Receipt> findByOrderId(Long orderId);
}
