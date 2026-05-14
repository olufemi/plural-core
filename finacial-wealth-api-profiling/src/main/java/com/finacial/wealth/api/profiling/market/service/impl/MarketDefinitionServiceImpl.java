package com.finacial.wealth.api.profiling.market.service.impl;

import com.finacial.wealth.api.profiling.market.entities.MarketDefinition;
import com.finacial.wealth.api.profiling.market.repo.MarketDefinitionRepo;
import com.finacial.wealth.api.profiling.market.service.MarketDefinitionService;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class MarketDefinitionServiceImpl implements MarketDefinitionService {

    private final MarketDefinitionRepo marketDefinitionRepo;

    public MarketDefinitionServiceImpl(MarketDefinitionRepo marketDefinitionRepo) {
        this.marketDefinitionRepo = marketDefinitionRepo;
    }

    @Override
    public MarketDefinition getRequiredMarket(String marketCode) {
        return marketDefinitionRepo.findByMarketCodeIgnoreCase(normalize(marketCode))
                .orElseThrow(() -> new IllegalArgumentException("Unsupported market code: " + marketCode));
    }

    @Override
    public Optional<MarketDefinition> findByCountryAndCurrency(String countryCode, String currencyCode) {
        if (countryCode == null || currencyCode == null) {
            return Optional.empty();
        }
        return marketDefinitionRepo.findByCountryCodeIgnoreCaseAndDefaultCurrencyCodeIgnoreCase(
                normalize(countryCode), normalize(currencyCode)
        );
    }

    @Override
    public List<MarketDefinition> getEnabledMarkets() {
        return marketDefinitionRepo.findByEnabledTrue();
    }

    private String normalize(String value) {
        return value == null ? null : value.trim().toUpperCase(Locale.ENGLISH);
    }
}
