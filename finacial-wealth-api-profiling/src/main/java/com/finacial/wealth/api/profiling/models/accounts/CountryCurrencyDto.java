/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.finacial.wealth.api.profiling.models.accounts;

/**
 *
 * @author olufemioshin
 */
// CountryCurrencyDto.java


public class CountryCurrencyDto {
    private String country;        // e.g., "Nigeria"
    private String countryCode;    // e.g., "NG"
    private String currencyCode;   // e.g., "NGN"
    private String currencySymbol; // e.g., "₦"

    public CountryCurrencyDto() {}
    public CountryCurrencyDto(String country, String countryCode, String currencyCode, String currencySymbol) {
        this.country = country;
        this.countryCode = countryCode;
        this.currencyCode = currencyCode;
        this.currencySymbol = currencySymbol;
    }
    public String getCountry() { return country; }
    public String getCountryCode() { return countryCode; }
    public String getCurrencyCode() { return currencyCode; }
    public String getCurrencySymbol() { return currencySymbol; }
    public void setCountry(String country) { this.country = country; }
    public void setCountryCode(String countryCode) { this.countryCode = countryCode; }
    public void setCurrencyCode(String currencyCode) { this.currencyCode = currencyCode; }
    public void setCurrencySymbol(String currencySymbol) { this.currencySymbol = currencySymbol; }
}

