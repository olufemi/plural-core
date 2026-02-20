/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.finacial.wealth.api.utility.services;

/**
 *
 * @author olufemioshin
 */
import com.finacial.wealth.api.utility.enumm.NotificationChannel;
import com.finacial.wealth.api.utility.enumm.NotificationModule;
import com.finacial.wealth.api.utility.enumm.NotificationProcess;
import java.util.EnumSet;
import java.util.Set;

public class NotificationChannelPolicy {

    public static Set<NotificationChannel> channels(NotificationModule module, NotificationProcess process) {

        // Onboarding
        if (module == NotificationModule.ONBOARDING && process == NotificationProcess.SUCCESSFUL_SIGNUP) {
            return EnumSet.of(NotificationChannel.EMAIL);
        }

        // Authentication
        if (module == NotificationModule.AUTHENTICATION && process == NotificationProcess.LOGIN) {
            return EnumSet.of(NotificationChannel.EMAIL);
        }

        // Group Savings - Email + Push
        if (module == NotificationModule.GROUP_SAVINGS) {
            return EnumSet.of(NotificationChannel.EMAIL, NotificationChannel.PUSH);
        }

        // Wallet - Email + Push
        if (module == NotificationModule.WALLET) {
            return EnumSet.of(NotificationChannel.EMAIL, NotificationChannel.PUSH);
        }

        // Investment specifics (based on your table)
        if (module == NotificationModule.INVESTMENT) {
            if (process == NotificationProcess.SUCCESSFUL_PLACEMENT_NOTIFICATION
                    || process == NotificationProcess.DAILY_INTEREST) {
                return EnumSet.of(NotificationChannel.PUSH);
            }
            return EnumSet.of(NotificationChannel.EMAIL, NotificationChannel.PUSH);
        }

        // FX (based on your snippet)
        if (module == NotificationModule.FX) {
            if (process == NotificationProcess.LISTING_PURCHASE) {
                return EnumSet.of(NotificationChannel.PUSH);
            }
            return EnumSet.of(NotificationChannel.EMAIL, NotificationChannel.PUSH);
        }

        // default safe
        return EnumSet.of(NotificationChannel.PUSH);
    }
}
