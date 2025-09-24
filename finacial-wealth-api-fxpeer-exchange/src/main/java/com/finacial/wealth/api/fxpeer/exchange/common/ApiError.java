/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.finacial.wealth.api.fxpeer.exchange.common;

/**
 *
 * @author olufemioshin
 */
import java.time.Instant;


public record ApiError(String message, String path, Instant timestamp) { }
