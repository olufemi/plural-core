/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.finacial.wealth.api.fxpeer.exchange.rating.model;

/**
 *
 * @author olufemioshin
 */
import java.time.Instant;

public record RatingView(Long id, Long orderId, Long sellerUserId, Long raterUserId, int score, String comment, Instant createdAt) {

}
