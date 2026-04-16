package com.finacial.wealth.backoffice.integrations.fxpeer.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import java.math.BigDecimal;
import java.time.LocalTime;
import org.junit.jupiter.api.Test;

class InvestmentProductUpsertRequestJsonTest {

    private final ObjectMapper objectMapper = JsonMapper.builder()
            .findAndAddModules()
            .build();

    @Test
    void shouldDeserializeBackofficePayloadShape() throws Exception {
        String json = """
                {
                  "productCode": "MMF003",
                  "name": "Prime Money Market Fund",
                  "type": "MONEY_MARKET",
                  "currency": "NGN",
                  "minimumInvestmentAmount": 5000,
                  "valuationMethod": "RATE",
                  "yieldPa": 10.00,
                  "liquidationFeeAppliedTo": "TOTAL_VALUE",
                  "liquidationFeeType": "RATE",
                  "liquidationFeeRate": 1.50,
                  "minLiquidationFee": 1.00,
                  "subscriptionCutOffTime": "00:00:00"
                }
                """;

        InvestmentProductUpsertRequest request =
                objectMapper.readValue(json, InvestmentProductUpsertRequest.class);

        assertEquals("MMF003", request.getProductCode());
        assertEquals("Prime Money Market Fund", request.getName());
        assertEquals(InvestmentType.MONEY_MARKET, request.getType());
        assertEquals("NGN", request.getCurrency());
        assertEquals(0, new BigDecimal("5000").compareTo(request.getMinimumInvestmentAmount()));
        assertEquals(ValuationMethod.RATE, request.getValuationMethod());
        assertEquals(0, new BigDecimal("10.00").compareTo(request.getYieldPa()));
        assertEquals(LiquidationFeeAppliedTo.TOTAL_VALUE, request.getLiquidationFeeAppliedTo());
        assertEquals(LiquidationFeeType.RATE, request.getLiquidationFeeType());
        assertEquals(0, new BigDecimal("1.50").compareTo(request.getLiquidationFeeRate()));
        assertEquals(0, new BigDecimal("1.00").compareTo(request.getMinLiquidationFee()));
        assertEquals(LocalTime.MIDNIGHT, request.getSubscriptionCutOffTime());
    }
}
