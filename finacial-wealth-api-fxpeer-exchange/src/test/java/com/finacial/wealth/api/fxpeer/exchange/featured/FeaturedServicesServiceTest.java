package com.finacial.wealth.api.fxpeer.exchange.featured;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.finacial.wealth.api.fxpeer.exchange.common.CurrencyCode;
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
import com.finacial.wealth.api.fxpeer.exchange.model.ApiResponseModel;
import com.finacial.wealth.api.fxpeer.exchange.offer.Offer;
import com.finacial.wealth.api.fxpeer.exchange.offer.OfferRepository;
import com.finacial.wealth.api.fxpeer.exchange.order.Order;
import com.finacial.wealth.api.fxpeer.exchange.order.OrderRepository;
import com.finacial.wealth.api.fxpeer.exchange.rating.model.RatingService;
import com.finacial.wealth.api.fxpeer.exchange.util.AppConfigConUtil;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class FeaturedServicesServiceTest {

    @Test
    void fallsBackToBestRateWhenManualTargetCannotBeResolved() throws Exception {
        AppConfigRepo appConfigRepo = mock(AppConfigRepo.class);
        OfferRepository offerRepository = mock(OfferRepository.class);
        OrderRepository orderRepository = mock(OrderRepository.class);
        MarketFacade marketFacade = mock(MarketFacade.class);
        RatingService ratingService = mock(RatingService.class);
        InvestmentOrderRepository investmentOrderRepository = mock(InvestmentOrderRepository.class);
        InvestmentProductRepository investmentProductRepository = mock(InvestmentProductRepository.class);
        InvestmentOrderService investmentOrderService = mock(InvestmentOrderService.class);

        FeaturedServicesConfigRequest config = new FeaturedServicesConfigRequest();
        FeaturedServiceConfigItem item = new FeaturedServiceConfigItem();
        item.setFeatureKey("fx-home-card");
        item.setFeatureGroup(FeatureGroup.FX);
        item.setStrategy(FeatureStrategy.ADMIN_SELECTED);
        item.setFallbackStrategy(FeatureStrategy.MOST_BOUGHT_OFFER);
        item.setManualTargetId("999");
        item.setTitleOverride("Trade FX");
        config.setItems(List.of(item));

        when(appConfigRepo.findByConfigName(AppConfigConUtil.SETTING_KEY_FEATURED_SERVICES_CONFIG))
                .thenReturn(List.of(configRecord(config)));
        when(offerRepository.findById(999L)).thenReturn(Optional.empty());
        when(orderRepository.findAll()).thenReturn(List.of(
                releasedOrder(11L, 700L, 1),
                releasedOrder(11L, 700L, 2),
                releasedOrder(12L, 701L, 1)
        ));
        when(offerRepository.findById(11L)).thenReturn(Optional.of(
                offer(11L, 700L, new BigDecimal("1150.00"), Instant.now().minus(1, ChronoUnit.HOURS))
        ));
        when(offerRepository.findById(12L)).thenReturn(Optional.of(
                offer(12L, 701L, new BigDecimal("1130.00"), Instant.now().minus(2, ChronoUnit.HOURS))
        ));
        when(ratingService.sellerStats(anyLong())).thenReturn(null);
        stubEmptyInvestmentProducts(investmentOrderService);

        FeaturedServicesService service = new FeaturedServicesService(
                appConfigRepo,
                offerRepository,
                orderRepository,
                marketFacade,
                ratingService,
                investmentOrderRepository,
                investmentProductRepository,
                investmentOrderService,
                new ObjectMapper()
        );

        ResponseEntity<ApiResponseModel> response = service.getFeaturedServices();

        assertEquals(200, response.getBody().getStatusCode());
        List<?> cards = (List<?>) response.getBody().getData();
        assertEquals(1, cards.size());
        FeaturedServiceCard card = (FeaturedServiceCard) cards.get(0);
        assertEquals(FeatureStrategy.ADMIN_SELECTED, card.getStrategy());
        assertEquals("11", card.getTarget().getId());
    }

    @Test
    void selectsMostBoughtOfferUsingReleasedOrdersOnly() throws Exception {
        AppConfigRepo appConfigRepo = mock(AppConfigRepo.class);
        OfferRepository offerRepository = mock(OfferRepository.class);
        OrderRepository orderRepository = mock(OrderRepository.class);
        MarketFacade marketFacade = mock(MarketFacade.class);
        RatingService ratingService = mock(RatingService.class);
        InvestmentOrderRepository investmentOrderRepository = mock(InvestmentOrderRepository.class);
        InvestmentProductRepository investmentProductRepository = mock(InvestmentProductRepository.class);
        InvestmentOrderService investmentOrderService = mock(InvestmentOrderService.class);

        FeaturedServicesConfigRequest config = new FeaturedServicesConfigRequest();
        FeaturedServiceConfigItem item = new FeaturedServiceConfigItem();
        item.setFeatureKey("fx-home-card");
        item.setFeatureGroup(FeatureGroup.FX);
        item.setStrategy(FeatureStrategy.MOST_BOUGHT_OFFER);
        config.setItems(List.of(item));

        when(appConfigRepo.findByConfigName(AppConfigConUtil.SETTING_KEY_FEATURED_SERVICES_CONFIG))
                .thenReturn(List.of(configRecord(config)));
        when(orderRepository.findAll()).thenReturn(List.of(
                releasedOrder(11L, 700L, 1),
                releasedOrder(11L, 700L, 2),
                releasedOrder(12L, 701L, 1),
                pendingOrder(12L, 701L)
        ));
        when(offerRepository.findById(11L)).thenReturn(Optional.of(
                offer(11L, 700L, new BigDecimal("1150.00"), Instant.now().minus(2, ChronoUnit.HOURS))
        ));
        when(offerRepository.findById(12L)).thenReturn(Optional.of(
                offer(12L, 701L, new BigDecimal("1140.00"), Instant.now().minus(3, ChronoUnit.HOURS))
        ));
        when(ratingService.sellerStats(anyLong())).thenReturn(null);
        stubEmptyInvestmentProducts(investmentOrderService);

        FeaturedServicesService service = new FeaturedServicesService(
                appConfigRepo,
                offerRepository,
                orderRepository,
                marketFacade,
                ratingService,
                investmentOrderRepository,
                investmentProductRepository,
                investmentOrderService,
                new ObjectMapper()
        );

        FeaturedServiceCard card = firstCard(service.getFeaturedServices());

        assertEquals("11", card.getTarget().getId());
        assertEquals(FeatureType.OFFER, card.getFeatureType());
    }

    @Test
    void selectsNewestOfferInsideBoostWindow() throws Exception {
        AppConfigRepo appConfigRepo = mock(AppConfigRepo.class);
        OfferRepository offerRepository = mock(OfferRepository.class);
        OrderRepository orderRepository = mock(OrderRepository.class);
        MarketFacade marketFacade = mock(MarketFacade.class);
        RatingService ratingService = mock(RatingService.class);
        InvestmentOrderRepository investmentOrderRepository = mock(InvestmentOrderRepository.class);
        InvestmentProductRepository investmentProductRepository = mock(InvestmentProductRepository.class);
        InvestmentOrderService investmentOrderService = mock(InvestmentOrderService.class);

        FeaturedServicesConfigRequest config = new FeaturedServicesConfigRequest();
        FeaturedServiceConfigItem item = new FeaturedServiceConfigItem();
        item.setFeatureKey("fx-home-card");
        item.setFeatureGroup(FeatureGroup.FX);
        item.setStrategy(FeatureStrategy.NEW_OFFER_BOOST);
        item.setFilters(Map.of("boostHours", 12));
        config.setItems(List.of(item));

        when(appConfigRepo.findByConfigName(AppConfigConUtil.SETTING_KEY_FEATURED_SERVICES_CONFIG))
                .thenReturn(List.of(configRecord(config)));
        when(offerRepository.findAll()).thenReturn(List.of(
                offer(21L, 800L, new BigDecimal("1100.00"), Instant.now().minus(2, ChronoUnit.HOURS)),
                offer(22L, 801L, new BigDecimal("1110.00"), Instant.now().minus(13, ChronoUnit.HOURS)),
                offer(23L, 802L, new BigDecimal("1120.00"), Instant.now().minus(1, ChronoUnit.HOURS))
        ));
        when(ratingService.sellerStats(anyLong())).thenReturn(null);
        stubEmptyInvestmentProducts(investmentOrderService);

        FeaturedServicesService service = new FeaturedServicesService(
                appConfigRepo,
                offerRepository,
                orderRepository,
                marketFacade,
                ratingService,
                investmentOrderRepository,
                investmentProductRepository,
                investmentOrderService,
                new ObjectMapper()
        );

        FeaturedServiceCard card = firstCard(service.getFeaturedServices());

        assertEquals("23", card.getTarget().getId());
    }

    @Test
    void selectsLowRiskInvestmentAmongEligibleProducts() throws Exception {
        AppConfigRepo appConfigRepo = mock(AppConfigRepo.class);
        OfferRepository offerRepository = mock(OfferRepository.class);
        OrderRepository orderRepository = mock(OrderRepository.class);
        MarketFacade marketFacade = mock(MarketFacade.class);
        RatingService ratingService = mock(RatingService.class);
        InvestmentOrderRepository investmentOrderRepository = mock(InvestmentOrderRepository.class);
        InvestmentProductRepository investmentProductRepository = mock(InvestmentProductRepository.class);
        InvestmentOrderService investmentOrderService = mock(InvestmentOrderService.class);

        FeaturedServicesConfigRequest config = new FeaturedServicesConfigRequest();
        FeaturedServiceConfigItem item = new FeaturedServiceConfigItem();
        item.setFeatureKey("investment-home-card");
        item.setFeatureGroup(FeatureGroup.INVESTMENT);
        item.setStrategy(FeatureStrategy.LOW_RISK_FEATURED);
        item.setFilters(Map.of("currency", "NGN"));
        config.setItems(List.of(item));

        when(appConfigRepo.findByConfigName(AppConfigConUtil.SETTING_KEY_FEATURED_SERVICES_CONFIG))
                .thenReturn(List.of(configRecord(config)));
        when(investmentProductRepository.findAll()).thenReturn(List.of(
                investmentProduct(301L, "MMF001", "NGN", true, "1", new BigDecimal("0.12"), "{\"riskLevel\":\"LOW\"}"),
                investmentProduct(302L, "MF001", "NGN", true, "1", new BigDecimal("0.18"), "{\"riskLevel\":\"MEDIUM\"}"),
                investmentProduct(303L, "MMF002", "USD", true, "1", new BigDecimal("0.22"), "{\"riskLevel\":\"LOW\"}")
        ));
        when(investmentOrderService.getProducts()).thenReturn(ResponseEntity.ok(apiResponse(List.of(
                investmentRecord("301", "MMF001", "NGN", new BigDecimal("0.12")),
                investmentRecord("302", "MF001", "NGN", new BigDecimal("0.18")),
                investmentRecord("303", "MMF002", "USD", new BigDecimal("0.22"))
        ))));

        FeaturedServicesService service = new FeaturedServicesService(
                appConfigRepo,
                offerRepository,
                orderRepository,
                marketFacade,
                ratingService,
                investmentOrderRepository,
                investmentProductRepository,
                investmentOrderService,
                new ObjectMapper()
        );

        FeaturedServiceCard card = firstCard(service.getFeaturedServices());

        assertEquals("MMF001", card.getTarget().getId());
        assertEquals(FeatureType.PRODUCT, card.getFeatureType());
        assertInstanceOf(InvestmentProductRecord.class, card.getPayload());
    }

    @Test
    void selectsMostSubscribedInvestmentByCompletedOrders() throws Exception {
        AppConfigRepo appConfigRepo = mock(AppConfigRepo.class);
        OfferRepository offerRepository = mock(OfferRepository.class);
        OrderRepository orderRepository = mock(OrderRepository.class);
        MarketFacade marketFacade = mock(MarketFacade.class);
        RatingService ratingService = mock(RatingService.class);
        InvestmentOrderRepository investmentOrderRepository = mock(InvestmentOrderRepository.class);
        InvestmentProductRepository investmentProductRepository = mock(InvestmentProductRepository.class);
        InvestmentOrderService investmentOrderService = mock(InvestmentOrderService.class);

        FeaturedServicesConfigRequest config = new FeaturedServicesConfigRequest();
        FeaturedServiceConfigItem item = new FeaturedServiceConfigItem();
        item.setFeatureKey("investment-home-card");
        item.setFeatureGroup(FeatureGroup.INVESTMENT);
        item.setStrategy(FeatureStrategy.MOST_SUBSCRIBED_PRODUCT);
        item.setFilters(Map.of("currency", "NGN", "lookbackDays", 30));
        config.setItems(List.of(item));

        when(appConfigRepo.findByConfigName(AppConfigConUtil.SETTING_KEY_FEATURED_SERVICES_CONFIG))
                .thenReturn(List.of(configRecord(config)));
        InvestmentProduct productA = investmentProduct(401L, "MMF001", "NGN", true, "1", new BigDecimal("0.14"), "{\"riskLevel\":\"LOW\"}");
        InvestmentProduct productB = investmentProduct(402L, "BOND001", "NGN", true, "1", new BigDecimal("0.16"), "{\"riskLevel\":\"LOW\"}");
        when(investmentOrderRepository.findAll()).thenReturn(List.of(
                investmentOrder(productA, InvestmentOrderType.SUBSCRIPTION, InvestmentOrderStatus.ACTIVE, new BigDecimal("10000"), 2),
                investmentOrder(productA, InvestmentOrderType.TOPUP, InvestmentOrderStatus.SETTLED, new BigDecimal("5000"), 3),
                investmentOrder(productB, InvestmentOrderType.SUBSCRIPTION, InvestmentOrderStatus.MATURED, new BigDecimal("8000"), 5),
                investmentOrder(productB, InvestmentOrderType.SUBSCRIPTION, InvestmentOrderStatus.PENDING, new BigDecimal("9000"), 1)
        ));
        when(investmentOrderService.getProducts()).thenReturn(ResponseEntity.ok(apiResponse(List.of(
                investmentRecord("401", "MMF001", "NGN", new BigDecimal("0.14")),
                investmentRecord("402", "BOND001", "NGN", new BigDecimal("0.16"))
        ))));

        FeaturedServicesService service = new FeaturedServicesService(
                appConfigRepo,
                offerRepository,
                orderRepository,
                marketFacade,
                ratingService,
                investmentOrderRepository,
                investmentProductRepository,
                investmentOrderService,
                new ObjectMapper()
        );

        FeaturedServiceCard card = firstCard(service.getFeaturedServices());

        assertEquals("MMF001", card.getTarget().getId());
        assertNotNull(card.getPayload());
    }

    private void stubEmptyInvestmentProducts(InvestmentOrderService investmentOrderService) {
        when(investmentOrderService.getProducts()).thenReturn(ResponseEntity.ok(apiResponse(List.of())));
    }

    private ApiResponseModel apiResponse(Object data) {
        ApiResponseModel response = new ApiResponseModel();
        response.setStatusCode(200);
        response.setDescription("ok");
        response.setData(data);
        return response;
    }

    private FeaturedServiceCard firstCard(ResponseEntity<ApiResponseModel> response) {
        List<?> cards = (List<?>) response.getBody().getData();
        assertEquals(1, cards.size());
        return (FeaturedServiceCard) cards.get(0);
    }

    private AppConfig configRecord(FeaturedServicesConfigRequest request) throws Exception {
        AppConfig config = new AppConfig();
        config.setConfigName(AppConfigConUtil.SETTING_KEY_FEATURED_SERVICES_CONFIG);
        config.setConfigValue(new ObjectMapper().writeValueAsString(request));
        return config;
    }

    private Offer offer(Long id, Long sellerUserId, BigDecimal rate, Instant createdAt) {
        Offer offer = new Offer();
        offer.setSellerUserId(sellerUserId);
        offer.setCurrencySell(CurrencyCode.CAD);
        offer.setCurrencyReceive(CurrencyCode.NGN);
        offer.setRate(rate);
        offer.setQtyAvailable(new BigDecimal("1000.00"));
        offer.setQtyTotal(new BigDecimal("1000.00"));
        offer.setStatus(OfferStatus.LIVE);
        ReflectionTestUtils.setField(offer, "id", id);
        ReflectionTestUtils.setField(offer, "createdAt", createdAt);
        return offer;
    }

    private Order releasedOrder(Long offerId, Long sellerUserId, long daysAgo) {
        Order order = new Order();
        order.setOfferId(offerId);
        order.setSellerUserId(sellerUserId);
        order.setCurrencySell(CurrencyCode.CAD);
        order.setCurrencyReceive(CurrencyCode.NGN);
        order.setStatus(OrderStatus.RELEASED);
        ReflectionTestUtils.setField(order, "createdAt", Instant.now().minus(daysAgo, ChronoUnit.DAYS));
        return order;
    }

    private Order pendingOrder(Long offerId, Long sellerUserId) {
        Order order = new Order();
        order.setOfferId(offerId);
        order.setSellerUserId(sellerUserId);
        order.setCurrencySell(CurrencyCode.CAD);
        order.setCurrencyReceive(CurrencyCode.NGN);
        order.setStatus(OrderStatus.PENDING_ESCROW);
        ReflectionTestUtils.setField(order, "createdAt", Instant.now().minus(1, ChronoUnit.DAYS));
        return order;
    }

    private InvestmentProduct investmentProduct(
            Long id,
            String productCode,
            String currency,
            Boolean active,
            String enabled,
            BigDecimal yieldPa,
            String metaJson
    ) {
        InvestmentProduct product = new InvestmentProduct();
        product.setId(id);
        product.setProductCode(productCode);
        product.setCurrency(currency);
        product.setActive(active);
        product.setEnableProduct(enabled);
        product.setYieldPa(yieldPa);
        product.setMetaJson(metaJson);
        return product;
    }

    private InvestmentProductRecord investmentRecord(String productId, String productCode, String currency, BigDecimal yieldPa) {
        InvestmentProductRecord record = new InvestmentProductRecord();
        record.setProductId(productId);
        record.setProductCode(productCode);
        record.setCurrency(currency);
        record.setYieldPa(yieldPa);
        return record;
    }

    private InvestmentOrder investmentOrder(
            InvestmentProduct product,
            InvestmentOrderType type,
            InvestmentOrderStatus status,
            BigDecimal amount,
            long daysAgo
    ) {
        InvestmentOrder order = new InvestmentOrder();
        order.setProduct(product);
        order.setType(type);
        order.setStatus(status);
        order.setAmount(amount);
        order.setCreatedAt(Instant.now().minus(daysAgo, ChronoUnit.DAYS));
        return order;
    }
}
