/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.finacial.wealth.api.fxpeer.exchange.rating.model;

/**
 *
 * @author olufemioshin
 */
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record RatingCreateRq(
        @NotNull
        Long orderId,
        @Min(1)
        @Max(5)
        int score,
        String comment) {

}
