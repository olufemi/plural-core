package com.finacial.wealth.api.fxpeer.exchange.featured;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.finacial.wealth.api.fxpeer.exchange.common.OfferStatus;
import com.finacial.wealth.api.fxpeer.exchange.common.OrderStatus;
import com.finacial.wealth.api.fxpeer.exchange.domain.AppConfig;
import com.finacial.wealth.api.fxpeer.exchange.domain.AppConfigRepo;
import com.finacial.wealth.api.fxpeer.exchange.investment.domain.InvestmentOrder;
import com.finacial.wealth.api.fxpeer.exchange.investment.domain.InvestmentProduct;
import com.finacial.wealth.api.fxpeer.exchange.investment.ennum.InvestmentOrderStatus;
import com.finacial.wealth.api.fxpeer.exchange.investment.ennum.InvestmentOrderType;
import com.finacial.wealth.api.fxpeer.exchange.investment.record.InvestmentProductRecord;
import com.finacial.wealth.api.fxpeer.exchange.investment.repo.InvestmentOrderRepository;
import com.finacial.wealth.api.fxpeer.exchange.investment.repo.InvestmentProductRepository;
import com.finacial.wealth.api.fxpeer.exchange.investment.service.InvestmentOrderService;
import com.finacial.wealth.api.fxpeer.exchange.markets.MarketFacade;
import com.finacial.wealth.api.fxpeer.exchange.markets.OfferView;
import com.finacial.wealth.api.fxpeer.exchange.model.ApiResponseModel;
import com.finacial.wealth.api.fxpeer.exchange.offer.Offer;
import com.finacial.wealth.api.fxpeer.exchange.offer.OfferRepository;
import com.finacial.wealth.api.fxpeer.exchange.order.Order;
import com.finacial.wealth.api.fxpeer.exchange.order.OrderRepository;
import com.finacial.wealth.api.fxpeer.exchange.rating.model.RatingService;
import com.finacial.wealth.api.fxpeer.exchange.util.AppConfigConUtil;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.StringJoiner;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class FeaturedServicesService {

    private static final Logger logger = LoggerFactory.getLogger(FeaturedServicesService.class);

    private final AppConfigRepo appConfigRepo;
    private final OfferRepository offerRepository;
    private final OrderRepository orderRepository;
    private final MarketFacade marketFacade;
    private final RatingService ratingService;
    private final InvestmentOrderRepository investmentOrderRepository;
    private final InvestmentProductRepository investmentProductRepository;
    private final InvestmentOrderService investmentOrderService;
    private final ObjectMapper objectMapper;

    public FeaturedServicesService(
            AppConfigRepo appConfigRepo,
            OfferRepository offerRepository,
            OrderRepository orderRepository,
            MarketFacade marketFacade,
            RatingService ratingService,
            InvestmentOrderRepository investmentOrderRepository,
            InvestmentProductRepository investmentProductRepository,
            InvestmentOrderService investmentOrderService,
            ObjectMapper objectMapper
    ) {
        this.appConfigRepo = appConfigRepo;
        this.offerRepository = offerRepository;
        this.orderRepository = orderRepository;
        this.marketFacade = marketFacade;
        this.ratingService = ratingService;
        this.investmentOrderRepository = investmentOrderRepository;
        this.investmentProductRepository = investmentProductRepository;
        this.investmentOrderService = investmentOrderService;
        this.objectMapper = objectMapper;
    }

    public ResponseEntity<ApiResponseModel> getFeaturedServices() {
        ApiResponseModel response = new ApiResponseModel();
        try {
            FeaturedServicesConfigRequest config = readConfig();
            List<FeaturedServiceConfigItem> items = config.getItems() == null ? List.of() : config.getItems();

            List<FeaturedServiceCard> cards = items.stream()
                    .filter(java.util.Objects::nonNull)
                    .filter(item -> !Boolean.FALSE.equals(item.getEnabled()))
                    .sorted(Comparator.comparingInt(item -> item.getPriority() == null ? 100 : item.getPriority()))
                    .map(this::safelyResolveCard)
                    .filter(java.util.Objects::nonNull)
                    .toList();

            boolean configEmpty = items.stream()
                    .filter(java.util.Objects::nonNull)
                    .anyMatch(item -> !Boolean.FALSE.equals(item.getEnabled()));

            if (cards.isEmpty()) {
                response.setStatusCode(400);
                response.setDescription(configEmpty
                        ? "No valid featured services available."
                        : "No featured services configured.");
                response.setData(cards);
                return ResponseEntity.badRequest().body(response);
            }

            response.setStatusCode(200);
            response.setDescription("Featured services fetched successfully.");
            response.setData(cards);
        } catch (Exception ex) {
            logger.error("Unable to fetch featured services", ex);
            response.setStatusCode(500);
            response.setDescription("Unable to fetch featured services.");
            response.setData(new ArrayList<>());
        }
        return ResponseEntity.ok(response);
    }

    public ResponseEntity<ApiResponseModel> getFeaturedServicesConfig() {
        ApiResponseModel response = new ApiResponseModel();
        try {
            response.setStatusCode(200);
            response.setDescription("Featured services config fetched successfully.");
            response.setData(readConfig());
        } catch (Exception ex) {
            logger.error("Unable to fetch featured services config", ex);
            response.setStatusCode(500);
            response.setDescription("Unable to fetch featured services config.");
            response.setData(new FeaturedServicesConfigRequest());
        }
        return ResponseEntity.ok(response);
    }

    @Transactional
    public ResponseEntity<ApiResponseModel> saveFeaturedServicesConfig(FeaturedServicesConfigRequest request) {
        ApiResponseModel response = new ApiResponseModel();
        try {
            request = normalizeConfig(request);
            validateRequest(request);
            ValidationOutcome validationOutcome = filterInvalidManualTargets(request);

            if (!validationOutcome.invalidFeatureKeys().isEmpty() && validationOutcome.validRequest().getItems().isEmpty()) {
                response.setStatusCode(400);
                response.setDescription("Invalid feature configuration. manualTargetId not found or not feature-eligible for featureKey(s): "
                        + joinFeatureKeys(validationOutcome.invalidFeatureKeys()));
                response.setData(request);
                return ResponseEntity.ok(response);
            }

            AppConfig config = appConfigRepo.findByConfigName(AppConfigConUtil.SETTING_KEY_FEATURED_SERVICES_CONFIG)
                    .stream()
                    .findFirst()
                    .orElseGet(AppConfig::new);

            config.setConfigName(AppConfigConUtil.SETTING_KEY_FEATURED_SERVICES_CONFIG);
            config.setConfigDescription("Featured services cards configuration");
            config.setConfigValue(objectMapper.writeValueAsString(validationOutcome.validRequest()));
            appConfigRepo.save(config);

            response.setStatusCode(200);
            response.setDescription(validationOutcome.invalidFeatureKeys().isEmpty()
                    ? "Featured services config saved successfully."
                    : "Featured services config saved successfully. Skipped featureKey(s) with manualTargetId not found or not feature-eligible: "
                    + joinFeatureKeys(validationOutcome.invalidFeatureKeys()));
            response.setData(validationOutcome.validRequest());
        } catch (IllegalArgumentException ex) {
            response.setStatusCode(400);
            response.setDescription(ex.getMessage());
            response.setData(request);
        } catch (Exception ex) {
            logger.error("Unable to save featured services config", ex);
            response.setStatusCode(500);
            response.setDescription("Unable to save featured services config.");
            response.setData(request);
        }
        return ResponseEntity.ok(response);
    }

    private FeaturedServiceCard safelyResolveCard(FeaturedServiceConfigItem item) {
        try {
            return resolveCard(item);
        } catch (Exception ex) {
            logger.error("Unable to resolve featured service card for featureKey={}", item == null ? null : item.getFeatureKey(), ex);
            return null;
        }
    }

    private FeaturedServiceCard resolveCard(FeaturedServiceConfigItem item) {
        if (item == null || item.getFeatureGroup() == null || item.getStrategy() == null) {
            return null;
        }
        FeaturedServiceCard card = resolveCard(item, item.getStrategy());
        if (card == null && item.getFallbackStrategy() != null && item.getFallbackStrategy() != item.getStrategy()) {
            card = resolveCard(item, item.getFallbackStrategy());
        }
        if (card == null) {
            return null;
        }

        if (hasText(item.getTitleOverride())) {
            card.setTitle(item.getTitleOverride());
        }
        if (hasText(item.getSubtitleOverride())) {
            card.setSubtitle(item.getSubtitleOverride());
        }
        if (hasText(item.getBadge())) {
            card.setBadge(item.getBadge());
        }
        if (hasText(item.getCtaLabel())) {
            card.setCtaLabel(item.getCtaLabel());
        }
        if (hasText(item.getTargetScreen()) && card.getTarget() != null) {
            card.getTarget().setScreen(item.getTargetScreen());
        }
        card.setFeatureKey(item.getFeatureKey());
        card.setStrategy(item.getStrategy());
        return card;
    }

    private ValidationOutcome filterInvalidManualTargets(FeaturedServicesConfigRequest request) {
        FeaturedServicesConfigRequest validRequest = new FeaturedServicesConfigRequest();
        List<String> invalidFeatureKeys = new ArrayList<>();

        for (FeaturedServiceConfigItem item : request.getItems()) {
            if (item != null && item.getStrategy() == FeatureStrategy.ADMIN_SELECTED && !isManualTargetResolvable(item)) {
                invalidFeatureKeys.add(hasText(item.getFeatureKey()) ? item.getFeatureKey() : "<unknown>");
                continue;
            }
            validRequest.getItems().add(item);
        }

        return new ValidationOutcome(validRequest, invalidFeatureKeys);
    }

    private boolean isManualTargetResolvable(FeaturedServiceConfigItem item) {
        if (item == null || item.getFeatureGroup() == null || !hasText(item.getManualTargetId())) {
            return false;
        }
        return switch (item.getFeatureGroup()) {
            case FX -> resolveManualOffer(item.getManualTargetId()) != null;
            case INVESTMENT -> resolveManualInvestment(item.getManualTargetId()) != null;
        };
    }

    private String joinFeatureKeys(List<String> featureKeys) {
        StringJoiner joiner = new StringJoiner(", ");
        for (String featureKey : featureKeys) {
            joiner.add(featureKey);
        }
        return joiner.toString();
    }

    private FeaturedServiceCard resolveCard(FeaturedServiceConfigItem item, FeatureStrategy strategy) {
        return switch (item.getFeatureGroup()) {
            case FX -> resolveFxCard(item, strategy);
            case INVESTMENT -> resolveInvestmentCard(item, strategy);
        };
    }

    private FeaturedServiceCard resolveFxCard(FeaturedServiceConfigItem item, FeatureStrategy strategy) {
        OfferView offer = switch (strategy) {
            case ADMIN_SELECTED -> resolveManualOffer(item.getManualTargetId());
            case BEST_RATE -> resolveBestRateOffer(item.getFilters());
            case MOST_BOUGHT_OFFER -> resolveMostBoughtOffer(item.getFilters());
            case TOP_SELLER_OFFER -> resolveTopSellerOffer(item.getFilters());
            case NEW_OFFER_BOOST -> resolveNewOfferBoost(item.getFilters());
            case FIRST_TIMER_SELLER_BOOST -> resolveFirstTimerSellerBoost(item.getFilters());
            default -> null;
        };
        if (offer == null) {
            return null;
        }

        FeaturedServiceCard card = new FeaturedServiceCard();
        card.setFeatureGroup(FeatureGroup.FX);
        card.setFeatureType(FeatureType.OFFER);
        card.setTitle("Trade FX");
        card.setSubtitle("Featured FX offer");
        card.setBadge("FEATURED");
        card.setCtaLabel("Trade now");
        card.setTarget(target(item.getTargetScreen(), "FX_OFFER_DETAIL", String.valueOf(offer.offerId())));
        card.setPayload(offer);
        return card;
    }

    private FeaturedServiceCard resolveInvestmentCard(FeaturedServiceConfigItem item, FeatureStrategy strategy) {
        InvestmentProductRecord product = switch (strategy) {
            case ADMIN_SELECTED -> resolveManualInvestment(item.getManualTargetId());
            case HIGHEST_YIELD -> resolveHighestYieldInvestment(item.getFilters());
            case MOST_SUBSCRIBED_PRODUCT -> resolveMostSubscribedProduct(item.getFilters());
            case HIGHEST_SUBSCRIPTION_VOLUME -> resolveHighestSubscriptionVolumeProduct(item.getFilters());
            case NEW_PRODUCT_BOOST -> resolveNewProductBoost(item.getFilters());
            case LOW_RISK_FEATURED -> resolveLowRiskProduct(item.getFilters());
            default -> null;
        };
        if (product == null) {
            return null;
        }

        FeaturedServiceCard card = new FeaturedServiceCard();
        card.setFeatureGroup(FeatureGroup.INVESTMENT);
        card.setFeatureType(FeatureType.PRODUCT);
        card.setTitle("Featured Investment");
        card.setSubtitle("Featured investment product");
        card.setBadge("FEATURED");
        card.setCtaLabel("Invest now");
        card.setTarget(target(item.getTargetScreen(), "INVESTMENT_PRODUCT_DETAIL", product.getProductCode()));
        card.setPayload(product);
        return card;
    }

    private OfferView resolveManualOffer(String manualTargetId) {
        if (!hasText(manualTargetId)) {
            return null;
        }
        Long offerId;
        try {
            offerId = Long.valueOf(manualTargetId);
        } catch (NumberFormatException ex) {
            return null;
        }
        return offerRepository.findUnlockedById(offerId)
                .filter(this::isOfferEligible)
                .map(this::toOfferView)
                .orElse(null);
    }

    private OfferView resolveBestRateOffer(Map<String, Object> filters) {
        String sell = filterValue(filters, "currencySell");
        String receive = filterValue(filters, "currencyReceive");
        var page = marketFacade.browse(
                enumOptional(sell, com.finacial.wealth.api.fxpeer.exchange.common.CurrencyCode.class),
                enumOptional(receive, com.finacial.wealth.api.fxpeer.exchange.common.CurrencyCode.class),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                0,
                1,
                "bestRate"
        );
        return page.getContent().stream().findFirst().orElse(null);
    }

    private OfferView resolveMostBoughtOffer(Map<String, Object> filters) {
        Instant since = since(filters, 7);
        Map<Long, Long> countsByOffer = orderRepository.findAll().stream()
                .filter(order -> order.getStatus() == OrderStatus.RELEASED)
                .filter(order -> order.getOfferId() != null)
                .filter(order -> hasCreatedAtOnOrAfter(order.getCreatedAt(), since))
                .filter(order -> matchesOfferFilters(order.getCurrencySell(), order.getCurrencyReceive(), filters))
                .collect(Collectors.groupingBy(Order::getOfferId, Collectors.counting()));

        return countsByOffer.entrySet().stream()
                .sorted(Map.Entry.<Long, Long>comparingByValue().reversed())
                .map(entry -> resolveManualOffer(String.valueOf(entry.getKey())))
                .filter(java.util.Objects::nonNull)
                .findFirst()
                .orElse(null);
    }

    private OfferView resolveTopSellerOffer(Map<String, Object> filters) {
        Instant since = since(filters, 7);
        Map<Long, Long> countsBySeller = orderRepository.findAll().stream()
                .filter(order -> order.getStatus() == OrderStatus.RELEASED)
                .filter(order -> order.getSellerUserId() != null)
                .filter(order -> hasCreatedAtOnOrAfter(order.getCreatedAt(), since))
                .filter(order -> matchesOfferFilters(order.getCurrencySell(), order.getCurrencyReceive(), filters))
                .collect(Collectors.groupingBy(Order::getSellerUserId, Collectors.counting()));

        Set<Long> rankedSellerIds = countsBySeller.entrySet().stream()
                .sorted(Map.Entry.<Long, Long>comparingByValue().reversed())
                .map(Map.Entry::getKey)
                .collect(Collectors.toCollection(java.util.LinkedHashSet::new));

        for (Long sellerId : rankedSellerIds) {
            Optional<OfferView> offer = offerRepository.findAll().stream()
                    .filter(this::isOfferEligible)
                    .filter(candidate -> sellerId.equals(candidate.getSellerUserId()))
                    .filter(candidate -> matchesOfferFilters(candidate.getCurrencySell(), candidate.getCurrencyReceive(), filters))
                    .sorted(Comparator.comparing(Offer::getRate, Comparator.nullsLast(Comparator.naturalOrder())).reversed()
                            .thenComparing(Offer::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                    .map(this::toOfferView)
                    .findFirst();
            if (offer.isPresent()) {
                return offer.get();
            }
        }
        return null;
    }

    private OfferView resolveNewOfferBoost(Map<String, Object> filters) {
        Instant since = Instant.now().minus(boostHours(filters, 12), ChronoUnit.HOURS);
        return offerRepository.findAll().stream()
                .filter(this::isOfferEligible)
                .filter(offer -> hasCreatedAtOnOrAfter(offer.getCreatedAt(), since))
                .filter(offer -> matchesOfferFilters(offer.getCurrencySell(), offer.getCurrencyReceive(), filters))
                .sorted(Comparator.comparing(Offer::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .map(this::toOfferView)
                .findFirst()
                .orElse(null);
    }

    private OfferView resolveFirstTimerSellerBoost(Map<String, Object> filters) {
        Instant since = Instant.now().minus(boostHours(filters, 12), ChronoUnit.HOURS);
        Map<Long, Long> offersPerSeller = offerRepository.findAll().stream()
                .filter(offer -> offer.getSellerUserId() != null)
                .collect(Collectors.groupingBy(Offer::getSellerUserId, Collectors.counting()));

        return offerRepository.findAll().stream()
                .filter(this::isOfferEligible)
                .filter(offer -> offer.getSellerUserId() != null)
                .filter(offer -> hasCreatedAtOnOrAfter(offer.getCreatedAt(), since))
                .filter(offer -> offersPerSeller.getOrDefault(offer.getSellerUserId(), 0L) == 1L)
                .filter(offer -> matchesOfferFilters(offer.getCurrencySell(), offer.getCurrencyReceive(), filters))
                .sorted(Comparator.comparing(Offer::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .map(this::toOfferView)
                .findFirst()
                .orElse(null);
    }

    private InvestmentProductRecord resolveManualInvestment(String manualTargetId) {
        if (!hasText(manualTargetId)) {
            return null;
        }
        return getInvestmentProducts().stream()
                .filter(product -> manualTargetId.equalsIgnoreCase(product.getProductCode())
                || manualTargetId.equalsIgnoreCase(product.getProductId()))
                .findFirst()
                .orElse(null);
    }

    private InvestmentProductRecord resolveHighestYieldInvestment(Map<String, Object> filters) {
        String currency = filterValue(filters, "currency");
        return getInvestmentProducts().stream()
                .filter(product -> !hasText(currency) || currency.equalsIgnoreCase(product.getCurrency()))
                .filter(product -> product.getYieldPa() != null)
                .max(Comparator.comparing(InvestmentProductRecord::getYieldPa))
                .orElse(null);
    }

    private InvestmentProductRecord resolveMostSubscribedProduct(Map<String, Object> filters) {
        Instant since = since(filters, 30);
        Map<String, Long> countsByProductCode = investmentOrderRepository.findAll().stream()
                .filter(this::isCompletedSubscriptionLike)
                .filter(order -> hasCreatedAtOnOrAfter(order.getCreatedAt(), since))
                .filter(order -> order.getProduct() != null)
                .filter(order -> hasText(order.getProduct().getProductCode()))
                .filter(order -> matchesInvestmentFilters(order.getProduct(), filters))
                .collect(Collectors.groupingBy(order -> order.getProduct().getProductCode(), Collectors.counting()));

        return countsByProductCode.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .map(entry -> resolveManualInvestment(entry.getKey()))
                .filter(java.util.Objects::nonNull)
                .findFirst()
                .orElse(null);
    }

    private InvestmentProductRecord resolveHighestSubscriptionVolumeProduct(Map<String, Object> filters) {
        Instant since = since(filters, 30);
        Map<String, BigDecimal> amountByProductCode = investmentOrderRepository.findAll().stream()
                .filter(this::isCompletedSubscriptionLike)
                .filter(order -> hasCreatedAtOnOrAfter(order.getCreatedAt(), since))
                .filter(order -> order.getProduct() != null)
                .filter(order -> hasText(order.getProduct().getProductCode()))
                .filter(order -> order.getAmount() != null)
                .filter(order -> matchesInvestmentFilters(order.getProduct(), filters))
                .collect(Collectors.groupingBy(
                        order -> order.getProduct().getProductCode(),
                        Collectors.reducing(BigDecimal.ZERO, InvestmentOrder::getAmount, BigDecimal::add)
                ));

        return amountByProductCode.entrySet().stream()
                .sorted(Map.Entry.<String, BigDecimal>comparingByValue().reversed())
                .map(entry -> resolveManualInvestment(entry.getKey()))
                .filter(java.util.Objects::nonNull)
                .findFirst()
                .orElse(null);
    }

    private InvestmentProductRecord resolveNewProductBoost(Map<String, Object> filters) {
        String currency = filterValue(filters, "currency");
        return investmentProductRepository.findAll().stream()
                .filter(this::isInvestmentProductEligible)
                .filter(product -> !hasText(currency) || currency.equalsIgnoreCase(product.getCurrency()))
                .sorted(Comparator.comparing(InvestmentProduct::getId).reversed())
                .map(InvestmentProduct::getProductCode)
                .map(this::resolveManualInvestment)
                .filter(java.util.Objects::nonNull)
                .findFirst()
                .orElse(null);
    }

    private InvestmentProductRecord resolveLowRiskProduct(Map<String, Object> filters) {
        String currency = filterValue(filters, "currency");
        return investmentProductRepository.findAll().stream()
                .filter(this::isInvestmentProductEligible)
                .filter(product -> !hasText(currency) || currency.equalsIgnoreCase(product.getCurrency()))
                .filter(this::isLowRisk)
                .sorted(Comparator.comparing(InvestmentProduct::getYieldPa, Comparator.nullsLast(Comparator.reverseOrder())))
                .map(InvestmentProduct::getProductCode)
                .map(this::resolveManualInvestment)
                .filter(java.util.Objects::nonNull)
                .findFirst()
                .orElse(null);
    }

    private List<InvestmentProductRecord> getInvestmentProducts() {
        Object data = investmentOrderService.getProducts().getBody() == null
                ? null
                : investmentOrderService.getProducts().getBody().getData();
        if (!(data instanceof List<?> items)) {
            return List.of();
        }
        return items.stream()
                .map(this::toInvestmentProductRecord)
                .filter(java.util.Objects::nonNull)
                .toList();
    }

    private InvestmentProductRecord toInvestmentProductRecord(Object item) {
        if (item instanceof InvestmentProductRecord record) {
            return record;
        }
        try {
            return objectMapper.convertValue(item, InvestmentProductRecord.class);
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    private OfferView toOfferView(Offer offer) {
        return new OfferView(
                offer.getId(),
                offer.getCorrelationId(),
                offer.getSellerUserId(),
                offer.getCurrencySell(),
                offer.getCurrencyReceive(),
                offer.getRate(),
                offer.getQtyAvailable(),
                offer.getQtyTotal(),
                ratingService.sellerStats(offer.getSellerUserId())
        );
    }

    private boolean isOfferEligible(Offer offer) {
        if (offer == null || offer.getQtyAvailable() == null) {
            return false;
        }
        return offer.getQtyAvailable().compareTo(BigDecimal.ZERO) > 0
                && (offer.getStatus() == OfferStatus.LIVE || offer.getStatus() == OfferStatus.PARTIALLY_FILLED);
    }

    private boolean isInvestmentProductEligible(InvestmentProduct product) {
        return product != null
                && Boolean.TRUE.equals(product.isActive())
                && "1".equals(product.getEnableProduct());
    }

    private boolean isCompletedSubscriptionLike(InvestmentOrder order) {
        return order != null
                && (order.getType() == InvestmentOrderType.SUBSCRIPTION || order.getType() == InvestmentOrderType.TOPUP)
                && (order.getStatus() == InvestmentOrderStatus.ACTIVE
                || order.getStatus() == InvestmentOrderStatus.SETTLED
                || order.getStatus() == InvestmentOrderStatus.MATURED);
    }

    private boolean matchesOfferFilters(
            com.finacial.wealth.api.fxpeer.exchange.common.CurrencyCode currencySell,
            com.finacial.wealth.api.fxpeer.exchange.common.CurrencyCode currencyReceive,
            Map<String, Object> filters
    ) {
        String sell = filterValue(filters, "currencySell");
        String receive = filterValue(filters, "currencyReceive");
        String sellCode = currencySell == null ? null : currencySell.name();
        String receiveCode = currencyReceive == null ? null : currencyReceive.name();
        return (!hasText(sell) || sell.equalsIgnoreCase(sellCode))
                && (!hasText(receive) || receive.equalsIgnoreCase(receiveCode));
    }

    private boolean matchesInvestmentFilters(InvestmentProduct product, Map<String, Object> filters) {
        String currency = filterValue(filters, "currency");
        return product != null && (!hasText(currency) || currency.equalsIgnoreCase(product.getCurrency()));
    }

    private boolean isLowRisk(InvestmentProduct product) {
        try {
            JsonNode riskLevel = objectMapper.readTree(
                    product.getMetaJson() == null || product.getMetaJson().isBlank() ? "{}" : product.getMetaJson()
            ).path("riskLevel");
            return riskLevel.isTextual() && "LOW".equalsIgnoreCase(riskLevel.asText());
        } catch (Exception ex) {
            return false;
        }
    }

    private FeaturedServicesConfigRequest readConfig() throws Exception {
        AppConfig config = appConfigRepo.findByConfigName(AppConfigConUtil.SETTING_KEY_FEATURED_SERVICES_CONFIG)
                .stream()
                .findFirst()
                .orElse(null);
        if (config == null || !hasText(config.getConfigValue())) {
            return normalizeConfig(new FeaturedServicesConfigRequest());
        }
        return normalizeConfig(objectMapper.readValue(config.getConfigValue(), FeaturedServicesConfigRequest.class));
    }

    private void validateRequest(FeaturedServicesConfigRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Request body is required. featureKey is required; featureGroup must be FX or INVESTMENT; strategy is required; manualTargetId is required only when strategy = ADMIN_SELECTED.");
        }
        request = normalizeConfig(request);
        for (FeaturedServiceConfigItem item : request.getItems()) {
            if (item == null) {
                throw new IllegalArgumentException("items cannot contain null entries. featureKey is required; featureGroup must be FX or INVESTMENT; strategy is required; manualTargetId is required only when strategy = ADMIN_SELECTED.");
            }
            if (item.getFeatureGroup() == null) {
                throw new IllegalArgumentException("featureGroup must be FX or INVESTMENT.");
            }
            if (item.getStrategy() == null) {
                throw new IllegalArgumentException("strategy is required.");
            }
            if (!hasText(item.getFeatureKey())) {
                throw new IllegalArgumentException("featureKey is required.");
            }
            if (item.getStrategy() == FeatureStrategy.ADMIN_SELECTED && !hasText(item.getManualTargetId())) {
                throw new IllegalArgumentException("manualTargetId is required only when strategy = ADMIN_SELECTED.");
            }
        }
    }

    private FeaturedServicesConfigRequest normalizeConfig(FeaturedServicesConfigRequest request) {
        if (request == null) {
            request = new FeaturedServicesConfigRequest();
        }
        if (request.getItems() == null) {
            request.setItems(new ArrayList<>());
        }
        return request;
    }

    private boolean hasCreatedAtOnOrAfter(Instant createdAt, Instant since) {
        return createdAt != null && !createdAt.isBefore(since);
    }

    private record ValidationOutcome(
            FeaturedServicesConfigRequest validRequest,
            List<String> invalidFeatureKeys
    ) {
    }

    private FeaturedServiceTarget target(String configuredScreen, String defaultScreen, String id) {
        FeaturedServiceTarget target = new FeaturedServiceTarget();
        target.setScreen(hasText(configuredScreen) ? configuredScreen : defaultScreen);
        target.setId(id);
        return target;
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private String filterValue(Map<String, Object> filters, String key) {
        if (filters == null) {
            return null;
        }
        Object value = filters.get(key);
        return value == null ? null : String.valueOf(value);
    }

    private Instant since(Map<String, Object> filters, int defaultLookbackDays) {
        return Instant.now().minus(lookbackDays(filters, defaultLookbackDays), ChronoUnit.DAYS);
    }

    private long lookbackDays(Map<String, Object> filters, int defaultValue) {
        return longFilterValue(filters, "lookbackDays", defaultValue);
    }

    private long boostHours(Map<String, Object> filters, int defaultValue) {
        return longFilterValue(filters, "boostHours", defaultValue);
    }

    private long longFilterValue(Map<String, Object> filters, String key, long defaultValue) {
        String value = filterValue(filters, key);
        if (!hasText(value)) {
            return defaultValue;
        }
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException ex) {
            return defaultValue;
        }
    }

    private <T extends Enum<T>> Optional<T> enumOptional(String value, Class<T> type) {
        if (!hasText(value)) {
            return Optional.empty();
        }
        try {
            return Optional.of(Enum.valueOf(type, value));
        } catch (IllegalArgumentException ex) {
            return Optional.empty();
        }
    }
}
