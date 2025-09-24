/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.finacial.wealth.api.fxpeer.exchange.order;

/**
 *
 * @author olufemioshin
 */
import com.finacial.wealth.api.fxpeer.exchange.common.OrderStatus;
import java.time.Instant;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {

    long countBySellerUserIdAndStatus(Long sellerUserId, OrderStatus status);

    long countBySellerUserIdAndStatusAndCreatedAtAfter(Long sellerUserId, OrderStatus status, Instant createdAt);
}
