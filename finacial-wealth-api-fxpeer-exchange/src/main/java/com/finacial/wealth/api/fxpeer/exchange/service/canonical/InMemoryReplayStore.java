/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.finacial.wealth.api.fxpeer.exchange.service.canonical;

/**
 *
 * @author olufemioshin
 */
import com.finacial.wealth.api.fxpeer.exchange.repo.ReplayStore;
import org.springframework.stereotype.Component;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class InMemoryReplayStore implements ReplayStore {
    private final ConcurrentHashMap<String, Long> seen = new ConcurrentHashMap<>();

    @Override
    public boolean markIfNew(String key, long ttlMillis) {
        long now = System.currentTimeMillis();
        Long prev = seen.putIfAbsent(key, now);
        // light cleanup
        if (seen.size() > 200_000) {
            long cutoff = now - ttlMillis;
            seen.entrySet().removeIf(e -> e.getValue() < cutoff);
        }
        return prev == null;
    }
}
