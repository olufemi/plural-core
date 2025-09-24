/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.finacial.wealth.api.profiling.repo;

/**
 *
 * @author olufemioshin
 */
import com.finacial.wealth.api.profiling.domain.Countries;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CountriesRepository extends JpaRepository<Countries, Long> {

    boolean existsByCountryCodeIgnoreCaseAndCountryIgnoreCase(String countryCode, String country);

    Optional<Countries> findByCountryCodeIgnoreCase(String countryCode);

    Optional<Countries> findByCountryIgnoreCase(String country);
}
