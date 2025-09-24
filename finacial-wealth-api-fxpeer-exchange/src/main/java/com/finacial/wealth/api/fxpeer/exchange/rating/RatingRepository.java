/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.finacial.wealth.api.fxpeer.exchange.rating;

/**
 *
 * @author olufemioshin
 */
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;


import java.util.Optional;


public interface RatingRepository extends JpaRepository<Rating, Long> {
Optional<Rating> findByOrderIdAndRaterUserId(Long orderId, Long raterUserId);
Page<Rating> findAllBySellerUserId(Long sellerUserId, Pageable pageable);
}
