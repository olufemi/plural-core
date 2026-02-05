/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.finacial.wealth.backoffice;

/**
 *
 * @author olufemioshin
 */
import com.finacial.wealth.backoffice.model.BaseResponse;
import java.security.SecureRandom;
import java.util.regex.Pattern;
import org.springframework.stereotype.Service;
@Service
public class PasswordPolicy {

    int statusCode = 500;
    String description = "Something went wrong!";

    private PasswordPolicy() {
    }

    private static final SecureRandom RANDOM = new SecureRandom();

    private static final int MIN_LENGTH = 12;

    private static final Pattern STRONG_PASSWORD = Pattern.compile(
            "^(?=.*[a-z])"
            + "(?=.*[A-Z])"
            + "(?=.*\\d)"
            + "(?=.*[@$!%*?&#^()_+=\\-{}\\[\\]:;\"'<>,./~`])"
            + "[^\\s]{" + MIN_LENGTH + ",}$"
    );

    private static final String UPPER = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String LOWER = "abcdefghijklmnopqrstuvwxyz";
    private static final String DIGITS = "0123456789";
    private static final String SPECIAL = "@$!%*?&#^()_+=-{}[]:;\"'<>,./~`";
    private static final String ALL = UPPER + LOWER + DIGITS + SPECIAL;

    /* =======================
       VALIDATION
       ======================= */
    public BaseResponse validate(String password) {
        int statusCode = 400;
        BaseResponse res = new BaseResponse();
        if (password == null || password.isBlank()) {
            res.setDescription("Password is required");
            res.setStatusCode(statusCode);
            return res;
        }

        if (!STRONG_PASSWORD.matcher(password).matches()) {
            res.setDescription("Password must be at least " + MIN_LENGTH
                    + " characters and include upper, lower, number, and special character");
            res.setStatusCode(statusCode);
            return res;

        }
        res.setDescription("Password is valid");
        res.setStatusCode(200);
        return res;
    }

    /* =======================
       TEMP PASSWORD GENERATION
       ======================= */
    public static String generateTempPassword(int length) {
        if (length < MIN_LENGTH) {
            throw new IllegalArgumentException("Password length must be >= " + MIN_LENGTH);
        }

        StringBuilder sb = new StringBuilder(length);

        // Guarantee rule coverage
        sb.append(randomChar(UPPER));
        sb.append(randomChar(LOWER));
        sb.append(randomChar(DIGITS));
        sb.append(randomChar(SPECIAL));

        for (int i = 4; i < length; i++) {
            sb.append(randomChar(ALL));
        }

        return shuffle(sb.toString());
    }

    private static char randomChar(String chars) {
        return chars.charAt(RANDOM.nextInt(chars.length()));
    }

    private static String shuffle(String input) {
        char[] chars = input.toCharArray();
        for (int i = chars.length - 1; i > 0; i--) {
            int j = RANDOM.nextInt(i + 1);
            char tmp = chars[i];
            chars[i] = chars[j];
            chars[j] = tmp;
        }
        return new String(chars);
    }
}
