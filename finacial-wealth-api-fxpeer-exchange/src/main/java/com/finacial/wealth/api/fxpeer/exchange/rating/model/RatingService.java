/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.finacial.wealth.api.fxpeer.exchange.rating.model;

/**
 *
 * @author olufemioshin
 */
import com.finacial.wealth.api.fxpeer.exchange.common.BusinessException;
import com.finacial.wealth.api.fxpeer.exchange.common.NotFoundException;
import com.finacial.wealth.api.fxpeer.exchange.common.OrderStatus;
import com.finacial.wealth.api.fxpeer.exchange.order.Order;
import com.finacial.wealth.api.fxpeer.exchange.order.OrderRepository;
import com.finacial.wealth.api.fxpeer.exchange.rating.Rating;
import com.finacial.wealth.api.fxpeer.exchange.rating.RatingRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class RatingService {

    private final RatingRepository ratings;
    private final OrderRepository orders;

    public RatingService(RatingRepository ratings, OrderRepository orders) {
        this.ratings = ratings;
        this.orders = orders;
    }

    public RatingView addRating(long raterUserId, RatingCreateRq rq) {
        Order o = orders.findById(rq.orderId()).orElseThrow(() -> new NotFoundException("Order not found"));
        if (o.getStatus() != OrderStatus.RELEASED) {
            throw new BusinessException("Order not released yet");
        }
        if (!o.getBuyerUserId().equals(raterUserId)) {
            throw new BusinessException("Only the buyer can rate the seller in this version");
        }
        ratings.findByOrderIdAndRaterUserId(o.getId(), raterUserId).ifPresent(r -> {
            throw new BusinessException("Already rated");
        });

        Rating r = new Rating();
        r.setOrderId(o.getId());
        r.setSellerUserId(o.getSellerUserId());
        r.setRaterUserId(raterUserId);
        r.setScore(rq.score());
        r.setComment(rq.comment());
        Rating saved = ratings.save(r);
        return new RatingView(saved.getId(), saved.getOrderId(), saved.getSellerUserId(), saved.getRaterUserId(), saved.getScore(), saved.getComment(), saved.getCreatedAt());
    }

    public Page<RatingView> listSellerRatings(long sellerId, Pageable pageable) {
        return ratings.findAllBySellerUserId(sellerId, pageable)
                .map(r -> new RatingView(r.getId(), r.getOrderId(), r.getSellerUserId(), r.getRaterUserId(), r.getScore(), r.getComment(), r.getCreatedAt()));
    }

    public SellerStats sellerStats(long sellerId) {
        long totalTrades = orders.countBySellerUserIdAndStatus(sellerId, OrderStatus.RELEASED);
        long tradesLast30d = orders.countBySellerUserIdAndStatusAndCreatedAtAfter(sellerId, OrderStatus.RELEASED, Instant.now().minusSeconds(30L * 24 * 3600));
        var page = ratings.findAllBySellerUserId(sellerId, Pageable.unpaged());
        long ratingCount = page.getTotalElements();
        double average = page.stream().mapToInt(Rating::getScore).average().orElse(0.0);
        return new SellerStats(sellerId, totalTrades, tradesLast30d, ratingCount, average);
    }
}
