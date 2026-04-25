package com.finacial.wealth.api.fxpeer.exchange.investment.service;

import com.finacial.wealth.api.fxpeer.exchange.investment.domain.InvestmentActivityLog;
import com.finacial.wealth.api.fxpeer.exchange.investment.domain.InvestmentPosition;
import com.finacial.wealth.api.fxpeer.exchange.investment.domain.InvestmentPositionHistory;
import com.finacial.wealth.api.fxpeer.exchange.investment.domain.InvestmentProduct;
import com.finacial.wealth.api.fxpeer.exchange.investment.repo.InvestmentActivityLogRepository;
import com.finacial.wealth.api.fxpeer.exchange.investment.repo.InvestmentPositionHistoryRepository;
import com.finacial.wealth.api.fxpeer.exchange.investment.repo.InvestmentPositionRepository;
import com.finacial.wealth.api.fxpeer.exchange.investment.repo.InvestmentProductRepository;
import com.finacial.wealth.api.fxpeer.exchange.model.ApiResponseModel;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class InvestmentPerformanceService {

    private final InvestmentPositionHistoryRepository historyRepository;
    private final InvestmentActivityLogRepository activityLogRepository;
    private final InvestmentPositionRepository positionRepository;
    private final InvestmentProductRepository productRepository;

    @Transactional(readOnly = true)
    public ResponseEntity<ApiResponseModel> getDashboard(
            String productCode,
            LocalDate fromDate,
            LocalDate toDate
    ) {
        ApiResponseModel response = new ApiResponseModel();

        try {
            LocalDate end = toDate == null ? LocalDate.now() : toDate;
            LocalDate start = fromDate == null ? end.minusMonths(3).plusDays(1) : fromDate;
            if (start.isAfter(end)) {
                throw new IllegalArgumentException("fromDate cannot be after toDate");
            }

            String normalizedProductCode = normalize(productCode);
            List<InvestmentPositionHistory> histories = historyRepository.findDashboardHistory(
                    normalizedProductCode,
                    start,
                    end
            );

            Map<LocalDate, BigDecimal> aumByDate = aggregateAumByDate(histories);
            List<Map<String, Object>> aumTrend = toTrendRows(aumByDate);
            List<Map<String, Object>> productSnapshots = buildProductSnapshots(histories);
            List<Map<String, Object>> recentActivity = buildRecentActivity(normalizedProductCode, start, end);

            Map<String, Object> filters = new LinkedHashMap<>();
            filters.put("productCode", normalizedProductCode);
            filters.put("fromDate", start);
            filters.put("toDate", end);

            Map<String, Object> data = new LinkedHashMap<>();
            data.put("filters", filters);
            data.put("summary", buildSummary(aumTrend));
            data.put("aumTrend", aumTrend);
            data.put("productSnapshots", productSnapshots);
            data.put("recentActivity", recentActivity);
            data.put("products", buildProductOptions());

            response.setStatusCode(200);
            response.setDescription("Performance dashboard fetched successfully.");
            response.setData(data);
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

    private Map<LocalDate, BigDecimal> aggregateAumByDate(List<InvestmentPositionHistory> histories) {
        Map<LocalDate, BigDecimal> aumByDate = new LinkedHashMap<>();
        for (InvestmentPositionHistory history : histories) {
            LocalDate date = history.getValuationDate();
            BigDecimal current = aumByDate.getOrDefault(date, BigDecimal.ZERO);
            aumByDate.put(date, current.add(nvl(history.getMarketValue())));
        }
        return aumByDate;
    }

    private List<Map<String, Object>> toTrendRows(Map<LocalDate, BigDecimal> aumByDate) {
        List<Map<String, Object>> rows = new ArrayList<>();
        for (Map.Entry<LocalDate, BigDecimal> entry : aumByDate.entrySet()) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("date", entry.getKey());
            row.put("aum", scale(entry.getValue()));
            rows.add(row);
        }
        return rows;
    }

    private Map<String, Object> buildSummary(List<Map<String, Object>> aumTrend) {
        BigDecimal startAum = aumTrend.isEmpty() ? BigDecimal.ZERO : decimalValue(aumTrend.get(0).get("aum"));
        BigDecimal endAum = aumTrend.isEmpty() ? BigDecimal.ZERO : decimalValue(aumTrend.get(aumTrend.size() - 1).get("aum"));
        BigDecimal netChange = endAum.subtract(startAum);
        BigDecimal netChangePct = startAum.compareTo(BigDecimal.ZERO) == 0
                ? BigDecimal.ZERO
                : netChange.multiply(BigDecimal.valueOf(100)).divide(startAum, 2, RoundingMode.HALF_UP);

        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("aum", scale(endAum));
        summary.put("startAum", scale(startAum));
        summary.put("netChange", scale(netChange));
        summary.put("netChangePct", netChangePct);
        return summary;
    }

    private List<Map<String, Object>> buildProductSnapshots(List<InvestmentPositionHistory> histories) {
        Map<String, List<InvestmentPositionHistory>> grouped = histories.stream()
                .filter(history -> history.getPosition() != null && history.getPosition().getProduct() != null)
                .collect(Collectors.groupingBy(
                        history -> history.getPosition().getProduct().getProductCode(),
                        LinkedHashMap::new,
                        Collectors.toList()
                ));

        List<Map<String, Object>> rows = new ArrayList<>();
        for (Map.Entry<String, List<InvestmentPositionHistory>> entry : grouped.entrySet()) {
            List<InvestmentPositionHistory> productHistory = entry.getValue().stream()
                    .sorted(Comparator.comparing(InvestmentPositionHistory::getValuationDate).thenComparing(InvestmentPositionHistory::getId))
                    .toList();
            InvestmentPositionHistory first = productHistory.get(0);
            InvestmentPositionHistory last = productHistory.get(productHistory.size() - 1);
            BigDecimal startAum = aggregateAum(productHistory, first.getValuationDate());
            BigDecimal endAum = aggregateAum(productHistory, last.getValuationDate());
            BigDecimal netChange = endAum.subtract(startAum);
            BigDecimal netChangePct = startAum.compareTo(BigDecimal.ZERO) == 0
                    ? BigDecimal.ZERO
                    : netChange.multiply(BigDecimal.valueOf(100)).divide(startAum, 2, RoundingMode.HALF_UP);

            Map<String, Object> row = new LinkedHashMap<>();
            row.put("productCode", entry.getKey());
            row.put("productName", first.getPosition().getProduct().getName());
            row.put("startAum", scale(startAum));
            row.put("aum", scale(endAum));
            row.put("netChange", scale(netChange));
            row.put("netChangePct", netChangePct);
            row.put("yieldPa", first.getPosition().getProduct().getYieldPa());
            row.put("yieldYtd", first.getPosition().getProduct().getYieldYtd());
            rows.add(row);
        }
        rows.sort(Comparator.comparing(row -> String.valueOf(row.get("productName")), String.CASE_INSENSITIVE_ORDER));
        return rows;
    }

    private BigDecimal aggregateAum(List<InvestmentPositionHistory> histories, LocalDate date) {
        return histories.stream()
                .filter(history -> Objects.equals(history.getValuationDate(), date))
                .map(InvestmentPositionHistory::getMarketValue)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private List<Map<String, Object>> buildRecentActivity(String productCode, LocalDate start, LocalDate end) {
        List<InvestmentActivityLog> activities = activityLogRepository.findTop100ByCreatedAtBetweenOrderByCreatedAtDesc(
                start.atStartOfDay(),
                end.plusDays(1).atStartOfDay().minusNanos(1)
        );
        if (activities.isEmpty()) {
            return List.of();
        }

        Set<Long> positionIds = activities.stream()
                .map(InvestmentActivityLog::getPositionId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Long, InvestmentPosition> positionsById = positionRepository.findAllById(positionIds).stream()
                .collect(Collectors.toMap(InvestmentPosition::getId, position -> position));

        return activities.stream()
                .map(activity -> toActivityRow(activity, positionsById.get(activity.getPositionId())))
                .filter(Objects::nonNull)
                .filter(row -> productCode == null || productCode.equals(row.get("productCode")))
                .limit(10)
                .toList();
    }

    private Map<String, Object> toActivityRow(InvestmentActivityLog activity, InvestmentPosition position) {
        if (position == null || position.getProduct() == null) {
            return null;
        }

        Map<String, Object> row = new LinkedHashMap<>();
        row.put("activityType", activity.getActivityType() == null ? null : activity.getActivityType().name());
        row.put("productCode", position.getProduct().getProductCode());
        row.put("productName", position.getProduct().getName());
        row.put("orderRef", activity.getOrderRef());
        row.put("amount", activity.getAmount());
        row.put("description", activity.getDescription());
        row.put("createdAt", activity.getCreatedAt());
        row.put("positionStatus", activity.getInvestmentPositionStatus() == null ? null : activity.getInvestmentPositionStatus().name());
        return row;
    }

    private List<Map<String, Object>> buildProductOptions() {
        return productRepository.findByActiveTrueOrderByNameAsc().stream()
                .map(this::toProductOption)
                .toList();
    }

    private Map<String, Object> toProductOption(InvestmentProduct product) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("productCode", product.getProductCode());
        row.put("productName", product.getName());
        row.put("currency", product.getCurrency());
        row.put("yieldPa", product.getYieldPa());
        row.put("yieldYtd", product.getYieldYtd());
        return row;
    }

    private String normalize(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private BigDecimal nvl(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private BigDecimal scale(BigDecimal value) {
        return nvl(value).setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal decimalValue(Object value) {
        if (value instanceof BigDecimal decimal) {
            return decimal;
        }
        if (value instanceof Number number) {
            return BigDecimal.valueOf(number.doubleValue());
        }
        return BigDecimal.ZERO;
    }
}
