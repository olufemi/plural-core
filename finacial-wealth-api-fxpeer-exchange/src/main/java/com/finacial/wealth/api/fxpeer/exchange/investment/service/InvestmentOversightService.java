
package com.finacial.wealth.api.fxpeer.exchange.investment.service;

import com.finacial.wealth.api.fxpeer.exchange.investment.domain.InvestmentOrder;
import com.finacial.wealth.api.fxpeer.exchange.investment.domain.InvestmentPosition;
import com.finacial.wealth.api.fxpeer.exchange.investment.domain.InvestmentProduct;
import com.finacial.wealth.api.fxpeer.exchange.investment.ennum.InvestmentOrderStatus;
import com.finacial.wealth.api.fxpeer.exchange.investment.ennum.InvestmentOrderType;
import com.finacial.wealth.api.fxpeer.exchange.investment.repo.InvestmentOrderRepository;
import com.finacial.wealth.api.fxpeer.exchange.investment.repo.InvestmentPositionRepository;
import com.finacial.wealth.api.fxpeer.exchange.investment.repo.InvestmentProductRepository;
import com.finacial.wealth.api.fxpeer.exchange.model.ApiResponseModel;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class InvestmentOversightService {

    private static final int DEFAULT_ACTION_LIMIT = 20;
    private static final int MAX_ACTION_LIMIT = 200;

    private final InvestmentPositionRepository positionRepository;
    private final InvestmentOrderRepository orderRepository;
    private final InvestmentProductRepository productRepository;

    @Transactional(readOnly = true)
    public ResponseEntity<ApiResponseModel> getDashboard(
            String productCode,
            LocalDate fromDate,
            LocalDate toDate,
            String actionType,
            String status,
            Integer size
    ) {
        ApiResponseModel response = new ApiResponseModel();

        try {
            LocalDate endDate = toDate != null ? toDate : LocalDate.now(ZoneOffset.UTC);
            LocalDate startDate = fromDate != null ? fromDate : endDate.minusMonths(3).plusDays(1);
            if (startDate.isAfter(endDate)) {
                throw new IllegalArgumentException("fromDate cannot be after toDate");
            }

            String normalizedProductCode = normalize(productCode);
            String normalizedActionType = normalize(actionType);
            String normalizedStatus = normalize(status);
            int actionLimit = sanitizeSize(size);

            List<InvestmentPosition> filteredPositions = positionRepository.findAllActivePositions().stream()
                    .filter(position -> matchesProduct(position.getProduct(), normalizedProductCode))
                    .toList();

            BigDecimal fundsUnderInvestment = filteredPositions.stream()
                    .map(position -> nvl(position.getCurrentValue()))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal interestAccrued = filteredPositions.stream()
                    .map(position -> nvl(position.getAccruedInterest()))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            Instant startInstant = startDate.atStartOfDay().toInstant(ZoneOffset.UTC);
            Instant endInstant = endDate.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC).minusNanos(1);

            List<Map<String, Object>> actions = orderRepository.findDashboardOrders(
                            normalizedProductCode,
                            startInstant,
                            endInstant
                    ).stream()
                    .map(this::toActionRow)
                    .filter(row -> matchesActionType(row, normalizedActionType))
                    .filter(row -> matchesStatus(row, normalizedStatus))
                    .limit(actionLimit)
                    .collect(Collectors.toCollection(ArrayList::new));

            List<Map<String, Object>> products = productRepository.findByActiveTrueOrderByNameAsc().stream()
                    .map(this::toProductOption)
                    .collect(Collectors.toCollection(ArrayList::new));

            Map<String, Object> filters = new LinkedHashMap<>();
            filters.put("productCode", normalizedProductCode);
            filters.put("fromDate", startDate);
            filters.put("toDate", endDate);
            filters.put("actionType", normalizedActionType);
            filters.put("status", normalizedStatus);
            filters.put("size", actionLimit);

            Map<String, Object> summary = new LinkedHashMap<>();
            summary.put("fundsUnderInvestment", fundsUnderInvestment);
            summary.put("interestAccrued", interestAccrued);
            summary.put("activePositions", filteredPositions.size());
            summary.put("actionCount", actions.size());

            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("filters", filters);
            payload.put("summary", summary);
            payload.put("actions", actions);
            payload.put("products", products);
            payload.put("actionTypes", List.of("ALL", "ALLOCATION", "TOPUP", "LIQUIDATION"));
            payload.put("statuses", List.of("ALL", "EXECUTED", "PENDING", "FAILED", "CANCELLED"));

            response.setStatusCode(200);
            response.setDescription("Investment oversight dashboard fetched successfully.");
            response.setData(payload);
        } catch (IllegalArgumentException ex) {
            response.setStatusCode(400);
            response.setDescription(ex.getMessage());
            response.setData(Map.of());
        } catch (Exception ex) {
            ex.printStackTrace();
            response.setStatusCode(500);
            response.setDescription("Something went wrong!");
            response.setData(Map.of());
        }

        return ResponseEntity.ok(response);
    }

    private Map<String, Object> toActionRow(InvestmentOrder order) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("reference", order.getOrderRef());
        row.put("productCode", order.getProduct() != null ? order.getProduct().getProductCode() : null);
        row.put("productName", order.getProduct() != null ? order.getProduct().getName() : null);
        row.put("type", displayType(order.getType()));
        row.put("rawType", order.getType() != null ? order.getType().name() : null);
        row.put("amount", nvl(order.getAmount()));
        row.put("returns", resolveReturns(order));
        row.put("date", order.getCreatedAt());
        row.put("status", displayStatus(order.getStatus()));
        row.put("rawStatus", order.getStatus() != null ? order.getStatus().name() : null);
        row.put("walletId", order.getWalletId());
        row.put("customerEmail", order.getEmailAddress());
        return row;
    }

    private Map<String, Object> toProductOption(InvestmentProduct product) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("productCode", product.getProductCode());
        item.put("name", product.getName());
        item.put("currency", product.getCurrency());
        return item;
    }

    private BigDecimal resolveReturns(InvestmentOrder order) {
        if (order.getType() == InvestmentOrderType.LIQUIDATION) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        InvestmentPosition position = order.getPosition();
        if (position == null) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        BigDecimal accrued = position.getTotalAccruedInterest() != null
                ? position.getTotalAccruedInterest()
                : position.getAccruedInterest();
        return nvl(accrued);
    }

    private boolean matchesProduct(InvestmentProduct product, String productCode) {
        if (productCode == null) {
            return true;
        }
        return product != null && Objects.equals(productCode, normalize(product.getProductCode()));
    }

    private boolean matchesActionType(Map<String, Object> row, String actionType) {
        if (actionType == null || "ALL".equals(actionType)) {
            return true;
        }
        return actionType.equals(row.get("type"));
    }

    private boolean matchesStatus(Map<String, Object> row, String status) {
        if (status == null || "ALL".equals(status)) {
            return true;
        }
        return status.equals(row.get("status"));
    }

    private String displayType(InvestmentOrderType type) {
        if (type == null) {
            return "UNKNOWN";
        }
        return switch (type) {
            case SUBSCRIPTION -> "ALLOCATION";
            case TOPUP -> "TOPUP";
            case LIQUIDATION -> "LIQUIDATION";
        };
    }

    private String displayStatus(InvestmentOrderStatus status) {
        if (status == null) {
            return "UNKNOWN";
        }
        return switch (status) {
            case ACTIVE, SETTLED, MATURED -> "EXECUTED";
            case PENDING, HOLD_PLACED, SENT_TO_PARTNER, LIQUIDATION_PENDING_APPROVAL, LIQUIDATION_PROCESSING -> "PENDING";
            case FAILED, LIQUIDATION_FAILED -> "FAILED";
            case CANCELLED -> "CANCELLED";
        };
    }

    private String normalize(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim().toUpperCase(Locale.ROOT);
    }

    private int sanitizeSize(Integer size) {
        if (size == null || size <= 0) {
            return DEFAULT_ACTION_LIMIT;
        }
        return Math.min(size, MAX_ACTION_LIMIT);
    }

    private BigDecimal nvl(BigDecimal value) {
        return value == null
                ? BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP)
                : value.setScale(2, RoundingMode.HALF_UP);
    }
}
