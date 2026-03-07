/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.finacial.wealth.api.fxpeer.exchange.repo;

/**
 *
 * @author olufemioshin
 */
public interface ReplayStore {
    /**
     * @return true if the key was newly seen and is now reserved; false if replay.
     */
    boolean markIfNew(String key, long ttlMillis);
}
