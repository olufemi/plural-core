/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.finacial.wealth.api.fxpeer.exchange.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Base64;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.FileUtils;

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

        /*
         * String randomString = Long.toString(System.currentTimeMillis()) +
         * Integer.toString(Math.abs(new Random().nextInt(99999))); long
         * transactionId = Long.valueOf(randomString); return transactionId;
         */
    }

    public static String generateOTP() {
        int randomPin = (int) (Math.random() * 9000) + 1000;
        String otp = String.valueOf(randomPin);

        return otp;
    }

    public static String generate6Digits() {
        int randomPin = (int) (Math.random() * 900000) + 100000;
        String otp = String.valueOf(randomPin);

        return otp;
    }

    public static boolean isValidFormat(String format, String value, Locale locale) {
        java.time.LocalDateTime ldt = null;
        DateTimeFormatter fomatter = DateTimeFormatter.ofPattern(format, locale);

        try {
            ldt = java.time.LocalDateTime.parse(value, fomatter);
            String result = ldt.format(fomatter);
            return result.equals(value);
        } catch (DateTimeParseException e) {
            try {
                LocalDate ld = LocalDate.parse(value, fomatter);
                String result = ld.format(fomatter);
                return result.equals(value);
            } catch (DateTimeParseException exp) {
                try {
                    LocalTime lt = LocalTime.parse(value, fomatter);
                    String result = lt.format(fomatter);
                    return result.equals(value);
                } catch (DateTimeParseException e2) {
                }
            }
        }

        return false;
    }

    public static String getByteArrayFromImageURL(String url) {

        try {
            URL imageUrl = new URL(url);
            URLConnection ucon = imageUrl.openConnection();
            InputStream is = ucon.getInputStream();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int read = 0;
            while ((read = is.read(buffer, 0, buffer.length)) != -1) {
                baos.write(buffer, 0, read);
            }
            baos.flush();
            return Base64
                    .getEncoder().encodeToString(baos.toByteArray());
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error invoking getByteArrayFromImageURL: " + url, e);
        }
        return null;
    }

    public String base64ImagetoFile(String livePhotoBase64Image) throws IOException {
        String inputFilePath = "dummy.jpg";
        String outputFilePath = "test_image.jpg";

        ClassLoader classLoader = getClass().getClassLoader();
        File inputFile = new File(classLoader
                .getResource(inputFilePath)
                .getFile());

        //create output file
        File outputFile = new File(inputFile
                .getParentFile()
                .getAbsolutePath() + File.pathSeparator + outputFilePath);

        // decode the string and write to file
        byte[] decodedBytes = Base64
                .getDecoder()
                .decode(livePhotoBase64Image);
        FileUtils.writeByteArrayToFile(outputFile, decodedBytes);
        return "";
    }

    public static boolean isTenDigits(String input) {
        boolean isTenD = false;
        if (input != null && input.matches("\\d{10}")) {
            isTenD = true;

        }

        System.out.println("isTenDigits" + "  :::::::::::::::::::::   " + isTenD);

        return isTenD;

    }

    public static boolean isElevenDigits(String input) {
        boolean isElevenD = false;
        if (input != null && input.matches("\\d{11}")) {
            isElevenD = true;

        }

        System.out.println("isElevenDigits" + "  :::::::::::::::::::::   " + isElevenD);

        return isElevenD;

    }

}
