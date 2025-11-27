/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.finacial.wealth.api.fxpeer.exchange.investment.record;

import com.finacial.wealth.api.fxpeer.exchange.investment.service.TimeUnitMinutes;
import lombok.Data;

/**
 *
 * @author olufemioshin
 */
@Data
public class UpdateProductScheduleRq {
    private Long settlementDelayValue;  // 5
    private TimeUnitMinutes.Unit settlementDelayUnit; // HOURS

    private Long tenorValue;            // 30
    private TimeUnitMinutes.Unit tenorUnit; // DAYS
}
