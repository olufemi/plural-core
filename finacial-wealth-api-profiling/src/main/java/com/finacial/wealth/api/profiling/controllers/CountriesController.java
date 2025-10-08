/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.finacial.wealth.api.profiling.controllers;

/**
 *
 * @author olufemioshin
 */
import com.finacial.wealth.api.profiling.domain.Countries;
import com.finacial.wealth.api.profiling.models.accounts.CountryCurrencyDto;
import com.finacial.wealth.api.profiling.models.accounts.CountryDto;
import com.finacial.wealth.api.profiling.models.accounts.ValidationResponse;
import com.finacial.wealth.api.profiling.services.CountryService;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/countries")
public class CountriesController {

    private final CountryService service;

    public CountriesController(CountryService service) {
        this.service = service;
    }

    @GetMapping("/all/existing")
    public List<Countries> listAllExistingCountries() {
        return service.getAllExistingCountries();
    }

    @GetMapping("/without-currency")
    public List<CountryDto> list() {
        return service.listCountries(); // minimal two-field list from DB
    }

    /*@GetMapping("/with-currency")
    public List<CountryCurrencyDto> listWithCurrency() {
        // triggers lazy-seeding if DB is missing any country rows
        return service.listCountriesWithCurrency();
    }*/

    @PostMapping("/validate")
    public ResponseEntity<ValidationResponse> validate(@RequestParam("countryCode") String countryCode,
            @RequestParam("country") String country) {
        ValidationResponse resp = service.validateCountryPair(countryCode, country);
        HttpStatus http;
        switch (resp.getStatusCode()) {
            case 200:
                http = HttpStatus.OK;
                break;
            case 400:
                http = HttpStatus.BAD_REQUEST;
                break;
            case 500:
                http = HttpStatus.INTERNAL_SERVER_ERROR;
                break;
            default:
                http = HttpStatus.BAD_REQUEST;
        }
        return new ResponseEntity<>(resp, http);
    }
}
