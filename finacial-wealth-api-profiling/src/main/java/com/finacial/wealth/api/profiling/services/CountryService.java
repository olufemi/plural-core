/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.finacial.wealth.api.profiling.services;

/**
 *
 * @author olufemioshin
 */
import com.finacial.wealth.api.profiling.domain.Countries;
import com.finacial.wealth.api.profiling.models.accounts.CountryCurrencyDto;
import com.finacial.wealth.api.profiling.models.accounts.CountryDto;
import com.finacial.wealth.api.profiling.models.accounts.ValidationResponse;
import com.finacial.wealth.api.profiling.repo.CountriesRepository;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Currency;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;


import java.util.*;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class CountryService {

    private final CountriesRepository repo;

    public CountryService(CountriesRepository repo) {
        this.repo = repo;
    }

    /**
     * Existing: list minimal fields from DB
     */
    public List<CountryDto> listCountries() {
        return repo.findAll()
                .stream()
                .map(c -> new CountryDto(c.getCountryCode(), c.getCountry()))
                .collect(Collectors.toList());
    }

    /**
     * New: ensure DB has complete country+currency data. - Inserts any missing
     * countries from JDK Locale/Currency - Updates name/currency fields if they
     * differ - Returns DB-backed list (sorted)
     */
    @Transactional // write ops allowed inside
    public List<CountryCurrencyDto> listCountriesWithCurrency() {
        // Build reference data from JDK
        List<CountryCurrencyDto> ref = generateFromJdk(Locale.ENGLISH);

        // Index DB by country code (upper)
        List<Countries> existing = repo.findAll();
        Map<String, Countries> dbByCode = new HashMap<>();
        for (Countries c : existing) {
            if (c.getCountryCode() != null) {
                dbByCode.put(c.getCountryCode().toUpperCase(Locale.ROOT), c);
            }
        }

        List<Countries> toSaveOrUpdate = new ArrayList<>();

        for (CountryCurrencyDto dto : ref) {
            String code2 = dto.getCountryCode().toUpperCase(Locale.ROOT);
            Countries row = dbByCode.get(code2);

            if (row == null) {
                // insert missing
                Countries n = new Countries();
                n.setCountryCode(code2);
                n.setCountry(dto.getCountry());
                n.setCurrencyCode(dto.getCurrencyCode());
                n.setCurrencySymbol(dto.getCurrencySymbol());
                toSaveOrUpdate.add(n);
            } else {
                // update if any field differs (case-sensitive compare for cleanliness)
                boolean changed = false;

                String wantedName = dto.getCountry();
                String wantedCur = dto.getCurrencyCode();
                String wantedSym = dto.getCurrencySymbol();

                if (!safeEq(row.getCountry(), wantedName)) {
                    row.setCountry(wantedName);
                    changed = true;
                }
                if (!safeEq(row.getCurrencyCode(), wantedCur)) {
                    row.setCurrencyCode(wantedCur);
                    changed = true;
                }
                if (!safeEq(row.getCurrencySymbol(), wantedSym)) {
                    row.setCurrencySymbol(wantedSym);
                    changed = true;
                }

                if (changed) {
                    toSaveOrUpdate.add(row);
                }
            }
        }

        if (!toSaveOrUpdate.isEmpty()) {
            repo.saveAll(toSaveOrUpdate);
        }

        // Return canonical list from DB
        return repo.findAll().stream()
                .sorted(Comparator.comparing(Countries::getCountry, Comparator.nullsLast(String::compareTo)))
                .map(c -> new CountryCurrencyDto(
                c.getCountry(),
                c.getCountryCode(),
                c.getCurrencyCode(),
                c.getCurrencySymbol()))
                .collect(Collectors.toList());
    }

    // ---- Your existing validator (unchanged) ----
    public ValidationResponse validateCountryPair(String code, String name) {
        try {
            if (code == null || code.trim().isEmpty() || name == null || name.trim().isEmpty()) {
                return ValidationResponse.bad(
                        "countryCode and country are required.",
                        "Provide both fields; examples: code='NG', country='Nigeria'."
                );
            }
            final String c = code.trim();
            final String n = name.trim();

            boolean exists = repo.existsByCountryCodeIgnoreCaseAndCountryIgnoreCase(c, n);
            if (exists) {
                return ValidationResponse.ok();
            }

            StringBuilder hint = new StringBuilder();

            repo.findByCountryCodeIgnoreCase(c).ifPresent(found -> {
                hint.append("Code '").append(c).append("' maps to '")
                        .append(found.getCountry()).append("'. ");
            });

            repo.findByCountryIgnoreCase(n).ifPresent(found -> {
                hint.append("Name '").append(n).append("' maps to code '")
                        .append(found.getCountryCode()).append("'. ");
            });

            String desc = "Invalid country pair: code=" + c + ", country=" + n + ".";
            return ValidationResponse.bad(desc, hint.length() == 0 ? null : hint.toString().trim());

        } catch (Exception e) {
            return ValidationResponse.error("Server error during country validation.");
        }
    }

    // ---- Helpers ----
    private boolean safeEq(String a, String b) {
        if (a == null) {
            return b == null;
        }
        return a.equals(b);
    }

    /**
     * Build reference list from JDK (what you asked for, now reused for
     * seeding)
     */
    private List<CountryCurrencyDto> generateFromJdk(Locale symbolLocale) {
        if (symbolLocale == null) {
            symbolLocale = Locale.ENGLISH;
        }

        Set<String> seen = new HashSet<>();
        List<CountryCurrencyDto> result = new ArrayList<>();

        for (String iso : Locale.getISOCountries()) {
            Locale locale = new Locale("", iso);
            String countryName = locale.getDisplayCountry(Locale.ENGLISH);
            if (countryName == null || countryName.trim().isEmpty()) {
                continue;
            }

            Currency currency = safeCurrency(locale);
            if (currency == null) {
                continue; // territories without legal tender
            }
            String code = iso.toUpperCase(Locale.ROOT); // ISO-3166 alpha-2
            if (!seen.add(code)) {
                continue;
            }

            String currencyCode = currency.getCurrencyCode();
            String symbol = currency.getSymbol(new Locale("en", iso)); // local symbol if available

            result.add(new CountryCurrencyDto(countryName, code, currencyCode, symbol));
        }

        // sort by country name
        Collections.sort(result, new Comparator<CountryCurrencyDto>() {
            @Override
            public int compare(CountryCurrencyDto o1, CountryCurrencyDto o2) {
                String a = o1.getCountry() == null ? "" : o1.getCountry();
                String b = o2.getCountry() == null ? "" : o2.getCountry();
                return a.compareTo(b);
            }
        });
        return result;
    }

    private Currency safeCurrency(Locale locale) {
        try {
            return Currency.getInstance(locale);
        } catch (Exception e) {
            return null;
        }
    }
}
