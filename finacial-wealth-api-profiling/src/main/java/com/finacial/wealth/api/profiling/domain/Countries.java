/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.finacial.wealth.api.profiling.domain;

/**
 *
 * @author olufemioshin
 */
import javax.persistence.*;
import lombok.Data;

import javax.persistence.*;

@Entity
@Table(
        name = "COUNTRIES",
        uniqueConstraints = {
            @UniqueConstraint(name = "UK_COUNTRIES_COUNTRY_CODE", columnNames = "COUNTRY_CODE"),
            @UniqueConstraint(name = "UK_COUNTRIES_COUNTRY", columnNames = "COUNTRY")
        }
)
@SequenceGenerator(
        name = "COUNTRIES_SEQ_GEN",
        sequenceName = "COUNTRIES_SEQ", // see DDL below
        allocationSize = 50
)
public class Countries {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "COUNTRIES_SEQ_GEN")
    @Column(name = "ID", nullable = false, updatable = false)
    private Long id;

    // ISO-3166 alpha-2 (e.g., NG, US)
    @Column(name = "COUNTRY_CODE", length = 2, nullable = false)
    private String countryCode;

    // Country display name (e.g., Nigeria, United States)
    @Column(name = "COUNTRY", length = 100, nullable = false)
    private String country;

    // ISO-4217 (e.g., NGN, USD)
    @Column(name = "CURRENCY_CODE", length = 3, nullable = false)
    private String currencyCode;

    // Symbol (e.g., ₦, $, GH₵)
    @Column(name = "CURRENCY_SYMBOL", length = 16)
    private String currencySymbol;

    // getters/setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public void setCurrencyCode(String currencyCode) {
        this.currencyCode = currencyCode;
    }

    public String getCurrencySymbol() {
        return currencySymbol;
    }

    public void setCurrencySymbol(String currencySymbol) {
        this.currencySymbol = currencySymbol;
    }
}
