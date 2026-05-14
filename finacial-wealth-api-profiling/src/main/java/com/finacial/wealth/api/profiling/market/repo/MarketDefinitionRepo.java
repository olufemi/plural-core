package com.finacial.wealth.api.profiling.market.repo;

import com.finacial.wealth.api.profiling.market.entities.MarketDefinition;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MarketDefinitionRepo extends JpaRepository<MarketDefinition, Long> {

    Optional<MarketDefinition> findByMarketCodeIgnoreCase(String marketCode);

    List<MarketDefinition> findByEnabledTrue();

    Optional<MarketDefinition> findByCountryCodeIgnoreCaseAndDefaultCurrencyCodeIgnoreCase(
            String countryCode, String defaultCurrencyCode
    );
}
