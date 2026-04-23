package com.finacial.wealth.api.fxpeer.exchange.investment.service;

import com.finacial.wealth.api.fxpeer.exchange.domain.AddAccountDetailsRepo;
import com.finacial.wealth.api.fxpeer.exchange.domain.AppConfigRepo;
import com.finacial.wealth.api.fxpeer.exchange.domain.RegWalletInfoRepository;
import com.finacial.wealth.api.fxpeer.exchange.feign.ProfilingProxies;
import com.finacial.wealth.api.fxpeer.exchange.feign.TransactionServiceProxies;
import com.finacial.wealth.api.fxpeer.exchange.fx.p.p.wallet.FinWealthPaymentTransactionRepo;
import com.finacial.wealth.api.fxpeer.exchange.investment.domain.InvestmentPosition;
import com.finacial.wealth.api.fxpeer.exchange.investment.domain.InvestmentProduct;
import com.finacial.wealth.api.fxpeer.exchange.investment.ennum.InvestmentType;
import com.finacial.wealth.api.fxpeer.exchange.investment.ennum.ValuationMethod;
import com.finacial.wealth.api.fxpeer.exchange.investment.repo.InvestmentOrderRepository;
import com.finacial.wealth.api.fxpeer.exchange.investment.repo.InvestmentPositionHistoryRepository;
import com.finacial.wealth.api.fxpeer.exchange.investment.repo.InvestmentPositionRepository;
import com.finacial.wealth.api.fxpeer.exchange.investment.repo.InvestmentProductRepository;
import com.finacial.wealth.api.fxpeer.exchange.investment.repo.InvestmentRequestGuardRepository;
import com.finacial.wealth.api.fxpeer.exchange.util.UttilityMethods;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

class InvestmentOrderServiceLiquidationFeeTest {

    private static final ZoneId LAGOS = ZoneId.of("Africa/Lagos");

    @Test
    void appliesEarlyLiquidationFeeBeforeLockExpires() {
        InvestmentOrderService service = newService();
        InvestmentPosition position = positionWithMeta(
                """
                {
                  "liquidationFee": {"appliedTo":"TOTAL_VALUE","type":"RATE","rate":1.0},
                  "lockConfig": {"enabled": true, "days": 14},
                  "earlyLiquidationFee": {"appliedTo":"TOTAL_VALUE","type":"RATE","rate":2.0}
                }
                """,
                lagosStartOfDayInstant(13),
                new BigDecimal("1000.00"),
                new BigDecimal("1200.00")
        );

        BigDecimal fee = ReflectionTestUtils.invokeMethod(
                service,
                "computeLiquidationFee",
                position,
                new BigDecimal("1200.00")
        );

        assertEquals(new BigDecimal("24.00"), fee);
    }

    @Test
    void chargesNoFeeForCapitalOnlyWithdrawalWithinLockPeriod() {
        InvestmentOrderService service = newService();
        InvestmentPosition position = positionWithMeta(
                """
                {
                  "liquidationFee": {"appliedTo":"TOTAL_VALUE","type":"RATE","rate":1.0},
                  "lockConfig": {"enabled": true, "days": 14},
                  "earlyLiquidationFee": {"appliedTo":"TOTAL_VALUE","type":"RATE","rate":2.0}
                }
                """,
                lagosStartOfDayInstant(13),
                new BigDecimal("1000.00"),
                new BigDecimal("1200.00")
        );

        BigDecimal fee = ReflectionTestUtils.invokeMethod(
                service,
                "computeLiquidationFee",
                position,
                new BigDecimal("500.00")
        );

        assertEquals(0, fee.compareTo(BigDecimal.ZERO));
    }

    @Test
    void chargesNoFeeWhenLockHasExpiredEvenForFullLiquidation() {
        InvestmentOrderService service = newService();
        InvestmentPosition position = positionWithMeta(
                """
                {
                  "liquidationFee": {"appliedTo":"TOTAL_VALUE","type":"RATE","rate":1.0},
                  "lockConfig": {"enabled": true, "days": 14},
                  "earlyLiquidationFee": {"appliedTo":"TOTAL_VALUE","type":"RATE","rate":2.0}
                }
                """,
                lagosStartOfDayInstant(14),
                new BigDecimal("1000.00"),
                new BigDecimal("1200.00")
        );

        BigDecimal fee = ReflectionTestUtils.invokeMethod(
                service,
                "computeLiquidationFee",
                position,
                new BigDecimal("1200.00")
        );

        assertEquals(0, fee.compareTo(BigDecimal.ZERO));
    }

    @Test
    void appliesCapWhenCalculatedEarlyFullLiquidationFeeIsHigher() {
        InvestmentOrderService service = newService();
        InvestmentPosition position = positionWithMeta(
                """
                {
                  "lockConfig": {"enabled": true, "days": 14},
                  "earlyLiquidationFee": {"appliedTo":"TOTAL_VALUE","type":"RATE","rate":10.0,"cap":20.0}
                }
                """,
                lagosStartOfDayInstant(13),
                new BigDecimal("1000.00"),
                new BigDecimal("1200.00")
        );

        BigDecimal fee = ReflectionTestUtils.invokeMethod(
                service,
                "computeLiquidationFee",
                position,
                new BigDecimal("1200.00")
        );

        assertEquals(new BigDecimal("20.00"), fee);
    }

    @Test
    void usesStandardLiquidationPolicyWhenLockIsDisabled() {
        InvestmentOrderService service = newService();
        InvestmentPosition position = positionWithMeta(
                """
                {
                  "liquidationFee": {"appliedTo":"TOTAL_VALUE","type":"RATE","rate":1.5,"cap":20.0},
                  "lockConfig": {"enabled": false, "days": 14},
                  "earlyLiquidationFee": {"appliedTo":"TOTAL_VALUE","type":"RATE","rate":10.0}
                }
                """,
                lagosStartOfDayInstant(3),
                new BigDecimal("1000.00"),
                new BigDecimal("1200.00")
        );

        BigDecimal fee = ReflectionTestUtils.invokeMethod(
                service,
                "computeLiquidationFee",
                position,
                new BigDecimal("500.00")
        );

        assertEquals(new BigDecimal("7.50"), fee);
    }

    private InvestmentOrderService newService() {
        return new InvestmentOrderService(
                mock(AppConfigRepo.class),
                mock(AddAccountDetailsRepo.class),
                mock(TransactionServiceProxies.class),
                mock(InvestmentProductRepository.class),
                mock(InvestmentPositionRepository.class),
                mock(InvestmentOrderRepository.class),
                mock(ActivityService.class),
                mock(RegWalletInfoRepository.class),
                mock(UttilityMethods.class),
                mock(FinWealthPaymentTransactionRepo.class),
                mock(ProfilingProxies.class),
                mock(InvestmentHistoryService.class),
                mock(InvestmentPositionHistoryRepository.class),
                mock(InvestmentRequestGuardRepository.class),
                mock(TransactionHistoryClientLocalT.class)
        );
    }

    private InvestmentPosition positionWithMeta(
            String metaJson,
            Instant settlementAt,
            BigDecimal investedAmount,
            BigDecimal currentValue
    ) {
        InvestmentProduct product = new InvestmentProduct();
        product.setType(InvestmentType.MONEY_MARKET);
        product.setValuationMethod(ValuationMethod.RATE);
        product.setMetaJson(metaJson);

        InvestmentPosition position = new InvestmentPosition();
        position.setProduct(product);
        position.setSettlementAt(settlementAt);
        position.setInvestedAmount(investedAmount);
        position.setCurrentValue(currentValue);
        position.setAccruedInterest(currentValue.subtract(investedAmount));
        position.setUnits(BigDecimal.ZERO);
        return position;
    }

    private Instant lagosStartOfDayInstant(long daysAgo) {
        return LocalDate.now(LAGOS)
                .minusDays(daysAgo)
                .atStartOfDay(LAGOS)
                .toInstant();
    }
}
