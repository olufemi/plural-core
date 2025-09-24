/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.finacial.wealth.api.profiling.models.accounts;

/**
 *
 * @author olufemioshin
 */
public class CountryDto {

    private String countryCode;
    private String country;

    public CountryDto() {
    }

    public CountryDto(String countryCode, String country) {
        this.countryCode = countryCode;
        this.country = country;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public String getCountry() {
        return country;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public void setCountry(String country) {
        this.country = country;
    }
}
