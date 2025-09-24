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
import com.finacial.wealth.api.profiling.repo.CountriesRepository;
import java.util.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class CountryDataLoader implements CommandLineRunner {

    private final CountriesRepository repo;

    public CountryDataLoader(CountriesRepository repo) {
        this.repo = repo;
    }

    @Override
    public void run(String... args) {
        seedIfEmpty();
    }

    @Transactional
    public void seedIfEmpty() {
        if (repo.count() > 0) return;

        List<Countries> batch = new ArrayList<>();
        Set<String> seen = new HashSet<>();

        for (String iso2 : Locale.getISOCountries()) {
            Locale loc = new Locale("", iso2);
            String name = loc.getDisplayCountry(Locale.ENGLISH);
            if (name == null || name.trim().isEmpty()) continue;

            Currency cur = safeCurrency(loc);
            if (cur == null) continue; // territories without legal tender

            String code2 = iso2.toUpperCase(Locale.ROOT);
            if (!seen.add(code2)) continue;

            String cc = cur.getCurrencyCode();
            String sym = cur.getSymbol(new Locale("en", iso2)); // local symbol if available

            Countries c = new Countries();
            c.setCountryCode(code2);
            c.setCountry(name);
            c.setCurrencyCode(cc);
            c.setCurrencySymbol(sym);

            batch.add(c);
        }

        repo.saveAll(batch);
    }

    private Currency safeCurrency(Locale locale) {
        try {
            return Currency.getInstance(locale);
        } catch (Exception ex) {
            return null;
        }
    }
}
