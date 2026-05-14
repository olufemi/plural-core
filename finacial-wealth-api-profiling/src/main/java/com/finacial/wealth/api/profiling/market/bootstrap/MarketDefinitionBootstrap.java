package com.finacial.wealth.api.profiling.market.bootstrap;

import com.finacial.wealth.api.profiling.market.entities.MarketDefinition;
import com.finacial.wealth.api.profiling.market.repo.MarketDefinitionRepo;
import java.util.Locale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class MarketDefinitionBootstrap implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(MarketDefinitionBootstrap.class);

    private final MarketDefinitionRepo marketDefinitionRepo;

    public MarketDefinitionBootstrap(MarketDefinitionRepo marketDefinitionRepo) {
        this.marketDefinitionRepo = marketDefinitionRepo;
    }

    @Override
    @Transactional
    public void run(String... args) {
        upsertCanadaRetail();
        upsertNigeriaRetail();
    }

    private void upsertCanadaRetail() {
        MarketDefinition market = marketDefinitionRepo.findByMarketCodeIgnoreCase("CA_RETAIL")
                .orElseGet(MarketDefinition::new);
        market.setMarketCode(normalize("CA_RETAIL"));
        market.setCountryCode(normalize("CA"));
        market.setDefaultCurrencyCode(normalize("CAD"));
        market.setDisplayName("Canada Retail");
        market.setOnboardingType("SDK");
        market.setKycProviderType("SDK");
        market.setAccountProviderType("SMART_CORE");
        market.setWalletProvisioningType("PRIMARY_WALLET");
        market.setSmartCoreProfileType("CANADA");
        market.setRequiresPrimaryOnboarding(Boolean.TRUE);
        market.setRequiresBvn(Boolean.FALSE);
        market.setRequiresSdkCompletion(Boolean.TRUE);
        market.setSupportsFxPeer(Boolean.TRUE);
        market.setSupportsInvestment(Boolean.TRUE);
        market.setSupportsAirtime(Boolean.FALSE);
        market.setSupportsFunding(Boolean.TRUE);
        market.setSupportsWithdrawal(Boolean.TRUE);
        market.setEnabled(Boolean.TRUE);
        market.setMetadataJson("{\"entryMode\":\"SDK\",\"countryIso\":\"CA\",\"currencyCode\":\"CAD\"}");
        marketDefinitionRepo.save(market);
        logger.info("market bootstrap ensured marketCode={} countryCode={} currencyCode={}",
                market.getMarketCode(), market.getCountryCode(), market.getDefaultCurrencyCode());
    }

    private void upsertNigeriaRetail() {
        MarketDefinition market = marketDefinitionRepo.findByMarketCodeIgnoreCase("NG_RETAIL")
                .orElseGet(MarketDefinition::new);
        market.setMarketCode(normalize("NG_RETAIL"));
        market.setCountryCode(normalize("NG"));
        market.setDefaultCurrencyCode(normalize("NGN"));
        market.setDisplayName("Nigeria Retail");
        market.setOnboardingType("SECONDARY_ACCOUNT");
        market.setKycProviderType("IDENTITY_PROVIDER");
        market.setAccountProviderType("BREEZEPAY");
        market.setWalletProvisioningType("ADDITIONAL_ACCOUNT");
        market.setSmartCoreProfileType("NIGERIA");
        market.setRequiresPrimaryOnboarding(Boolean.TRUE);
        market.setRequiresBvn(Boolean.TRUE);
        market.setRequiresSdkCompletion(Boolean.FALSE);
        market.setSupportsFxPeer(Boolean.TRUE);
        market.setSupportsInvestment(Boolean.TRUE);
        market.setSupportsAirtime(Boolean.TRUE);
        market.setSupportsFunding(Boolean.TRUE);
        market.setSupportsWithdrawal(Boolean.TRUE);
        market.setEnabled(Boolean.TRUE);
        market.setMetadataJson("{\"verificationType\":\"BVN\",\"countryIso\":\"NG\",\"currencyCode\":\"NGN\"}");
        marketDefinitionRepo.save(market);
        logger.info("market bootstrap ensured marketCode={} countryCode={} currencyCode={}",
                market.getMarketCode(), market.getCountryCode(), market.getDefaultCurrencyCode());
    }

    private String normalize(String value) {
        return value == null ? null : value.trim().toUpperCase(Locale.ENGLISH);
    }
}
