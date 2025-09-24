/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.finacial.wealth.api.profiling.services;

import com.finacial.wealth.api.profiling.repo.AddAccountDetailsRepo;
import java.security.SecureRandom;
import org.springframework.stereotype.Service;

/**
 *
 * @author olufemioshin
 */
@Service
public class UniqueIdService {

    private static final int MAX_RETRIES = 50;
    private static final String[] NG_PREFIXES = {"070", "080", "081", "090", "091"};
    private final SecureRandom rng = new SecureRandom();

    private final AddAccountDetailsRepo customerRepo;

    public UniqueIdService(AddAccountDetailsRepo customerRepo) {
        this.customerRepo = customerRepo;

    }

    /**
     * Generate an 11-digit Nigerian-style phone number that does not exist.
     */
    public String nextUniquePhoneNumber() {
        for (int i = 0; i < MAX_RETRIES; i++) {
            String candidate = randomNgPhone();
            if (!customerRepo.existsByAccountNumber(candidate)) {
                return candidate;
            }
        }
        throw new IllegalStateException("Unable to generate unique phoneNumber after " + MAX_RETRIES + " attempts");
    }

    /**
     * Generate a 10-digit walletId (9 random + 1 Luhn check digit) that does
     * not exist.
     */
    public String nextUniqueWalletId() {
        for (int i = 0; i < MAX_RETRIES; i++) {
            String base9 = randomDigits(9);              // may start with 0; that’s OK
            char check = luhnCheckDigit(base9);
            String candidate = base9 + check;            // total 10 digits
            if (!customerRepo.existsByWalletId(candidate)) {
                return candidate;
            }
        }
        throw new IllegalStateException("Unable to generate unique walletId after " + MAX_RETRIES + " attempts");
    }

    // ---------- helpers ----------
    /**
     * e.g., 070xxxxxxxxx, 080xxxxxxxxx, ... total 11 digits
     */
    private String randomNgPhone() {
        String prefix = NG_PREFIXES[rng.nextInt(NG_PREFIXES.length)];
        return prefix + randomDigits(11 - prefix.length());
    }

    /**
     * returns a string of exactly n random digits [0-9], allowing leading zeros
     */
    private String randomDigits(int n) {
        char[] out = new char[n];
        for (int i = 0; i < n; i++) {
            out[i] = (char) ('0' + rng.nextInt(10));
        }
        return new String(out);
    }

    /**
     * Luhn check digit for numeric string (no check yet). Returns the single
     * check digit as a char '0'..'9'.
     */
    private char luhnCheckDigit(String digits) {
        int sum = 0;
        boolean dbl = true; // start doubling from rightmost moving left, but we append check at end => start true
        for (int i = digits.length() - 1; i >= 0; i--) {
            int d = digits.charAt(i) - '0';
            if (dbl) {
                d = d * 2;
                if (d > 9) {
                    d -= 9;
                }
            }
            sum += d;
            dbl = !dbl;
        }
        int mod = sum % 10;
        int check = (10 - mod) % 10;
        return (char) ('0' + check);
    }
}
