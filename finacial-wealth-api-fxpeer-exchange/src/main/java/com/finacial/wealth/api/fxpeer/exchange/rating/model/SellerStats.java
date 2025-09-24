/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.finacial.wealth.api.fxpeer.exchange.rating.model;

/**
 *
 * @author olufemioshin
 */
public record SellerStats(Long sellerId, long totalTrades, long tradesLast30d, long ratingCount, double averageRating) {

}
