/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.finacial.wealth.api.fxpeer.exchange.service.canonical;

import org.springframework.stereotype.Component;

/**
 *
 * @author olufemioshin
 */
@Component
public class ReplayNonceStore {
    private final java.util.concurrent.ConcurrentHashMap<String, Long> seen = new java.util.concurrent.ConcurrentHashMap<>();

    public boolean markIfNew(String key, long nowMs, long ttlMs) {
        Long prev = seen.putIfAbsent(key, nowMs);
        // basic cleanup occasionally (not perfect, but ok for MVP)
        if (seen.size() > 100_000) {
            long cutoff = nowMs - ttlMs;
            seen.entrySet().removeIf(e -> e.getValue() < cutoff);
        }
        return prev == null;
    }
}
