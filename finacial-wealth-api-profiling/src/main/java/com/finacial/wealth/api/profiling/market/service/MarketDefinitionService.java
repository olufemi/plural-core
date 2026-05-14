package com.finacial.wealth.api.profiling.market.service;

import com.finacial.wealth.api.profiling.market.entities.MarketDefinition;
import java.util.List;
import java.util.Optional;

public interface MarketDefinitionService {

    MarketDefinition getRequiredMarket(String marketCode);

    Optional<MarketDefinition> findByCountryAndCurrency(String countryCode, String currencyCode);

    List<MarketDefinition> getEnabledMarkets();
}
