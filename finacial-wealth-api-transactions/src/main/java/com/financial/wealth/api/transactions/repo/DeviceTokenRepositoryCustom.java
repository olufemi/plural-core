/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.financial.wealth.api.transactions.repo;

/**
 *
 * @author olufemioshin
 */
public interface DeviceTokenRepositoryCustom {
    void upsert(String walletId, String platform, String token);
}
