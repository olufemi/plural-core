/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.finacial.wealth.api.utility.utils;

import java.security.SecureRandom;
import java.util.List;

import java.util.logging.Logger;

/**
 *
 * @author OSHIN
 */
public class GlobalMethods {

    private static final Logger logger = Logger.getLogger(GlobalMethods.class.getSimpleName());

    public static String generateNUBAN() {
        long number = (long) Math.floor(Math.random() * 9_000_000_000L) + 1_000_000_000L;

        return String.valueOf(number);
    }

    public static String generateMSISDN() {
        long number = (long) Math.floor(Math.random() * 9_000_0000L) + 1_000_0000L;

        return String.valueOf(number);
    }

    public static synchronized long generateTransactionId() {

        String randomString = Long.toString(System.currentTimeMillis()) + Integer.toString(Math.abs(new SecureRandom().nextInt(99999)));
        long transactionId = Long.valueOf(randomString);
        return transactionId;

        /*String randomString = Long.toString(System.currentTimeMillis()) + Integer.toString(Math.abs(new Random().nextInt(99999)));
        long transactionId = Long.valueOf(randomString);
        return transactionId;*/
    }

    public static String generateOTP() {
        int randomPin = (int) (Math.random() * 9000) + 1000;
        String otp = String.valueOf(randomPin);

        return otp;
    }

}
