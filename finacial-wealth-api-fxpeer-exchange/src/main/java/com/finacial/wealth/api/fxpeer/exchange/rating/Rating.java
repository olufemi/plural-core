/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.finacial.wealth.api.fxpeer.exchange.rating;

/**
 *
 * @author olufemioshin
 */
import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "ratings", uniqueConstraints = @UniqueConstraint(name = "uq_order_rater", columnNames = {"order_id", "rater_user_id"}))
public class Rating {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "order_id", nullable = false)
    private Long orderId;
    @Column(name = "seller_user_id", nullable = false)
    private Long sellerUserId; // ratee (target)
    @Column(name = "rater_user_id", nullable = false)
    private Long raterUserId; // must be buyer of the order
    @Column(nullable = false)
    private int score; // 1..5
    @Column(length = 1000)
    private String comment;
    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    public Long getId() {
        return id;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public Long getSellerUserId() {
        return sellerUserId;
    }

    public void setSellerUserId(Long sellerUserId) {
        this.sellerUserId = sellerUserId;
    }

    public Long getRaterUserId() {
        return raterUserId;
    }

    public void setRaterUserId(Long raterUserId) {
        this.raterUserId = raterUserId;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
