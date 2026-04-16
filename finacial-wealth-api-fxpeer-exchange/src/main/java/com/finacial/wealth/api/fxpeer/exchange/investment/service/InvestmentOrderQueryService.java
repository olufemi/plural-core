/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.finacial.wealth.api.fxpeer.exchange.investment.service;

/**
 *
 * @author olufemioshin
 */
import com.finacial.wealth.api.fxpeer.exchange.common.NotFoundException;
import com.finacial.wealth.api.fxpeer.exchange.investment.domain.InvestmentOrder;
import com.finacial.wealth.api.fxpeer.exchange.investment.domain.InvestmentProduct;
import com.finacial.wealth.api.fxpeer.exchange.investment.domain.InvestmentPosition;
import com.finacial.wealth.api.fxpeer.exchange.investment.ennum.InvestmentOrderStatus;
import com.finacial.wealth.api.fxpeer.exchange.investment.ennum.InvestmentOrderType;
import com.finacial.wealth.api.fxpeer.exchange.investment.record.InvestmentOrderPojo;
import com.finacial.wealth.api.fxpeer.exchange.investment.repo.InvestmentOrderRepository;
import com.finacial.wealth.api.fxpeer.exchange.investment.repo.InvestmentPositionRepository;
import com.finacial.wealth.api.fxpeer.exchange.investment.repo.InvestmentProductRepository;
import com.finacial.wealth.api.fxpeer.exchange.model.ApiResponseModel;
import com.finacial.wealth.api.fxpeer.exchange.util.UttilityMethods;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class InvestmentOrderQueryService {
    private static final ZoneId LAGOS = ZoneId.of("Africa/Lagos");

    private final InvestmentOrderRepository orderRepo;
    private final UttilityMethods utilService; // your JWT claim extractor
    private final InvestmentProductRepository productRepo;
    private final InvestmentPositionRepository positionRepo;

    public InvestmentOrderQueryService(InvestmentOrderRepository orderRepo, UttilityMethods utilService,
            InvestmentProductRepository productRepo,
            InvestmentPositionRepository positionRepo) {
        this.orderRepo = orderRepo;
        this.utilService = utilService;
        this.productRepo = productRepo;
        this.positionRepo = positionRepo;
    }

    @Transactional(readOnly = true)
    public ResponseEntity<ApiResponseModel> getOrdersByStatusForCustomer(InvestmentOrderStatus status) {
        ApiResponseModel res = new ApiResponseModel();
        int statusCode = 500;
        String statusMessage = "Something went wrong!";

        try {
            statusCode = 400;

            /*if (auth == null || auth.trim().isEmpty()) {
                res.setStatusCode(statusCode);
                res.setDescription("Missing Authorization header");
                res.setData(Collections.emptyList());
                return ResponseEntity.ok(res);
            }

            final String email = utilService.getClaimFromJwt(auth, "emailAddress");
            if (email == null || email.trim().isEmpty()) {
                res.setStatusCode(statusCode);
                res.setDescription("Invalid token: emailAddress claim missing");
                res.setData(Collections.emptyList());
                return ResponseEntity.ok(res);
            }*/

            List<InvestmentOrder> orders
                    = orderRepo.findByStatusOrderByUpdatedAtDesc(status);

            if (orders.size() <= 0) {
                res.setStatusCode(statusCode);
                res.setDescription("Customer has no pening liquidation!");

                return ResponseEntity.ok(res);
            }

            List<InvestmentOrderPojo> items = new ArrayList<InvestmentOrderPojo>();
            long counter = 1L;
            for (InvestmentOrder o : orders) {
                var product = productRepo.findByIdAndActiveTrue(Long.valueOf(orders.get(0).getProduct().getId()))
                        .orElseThrow(() -> new NotFoundException("Investment product not found"));

                InvestmentOrderPojo p = toPojo(o, product);
                p.setId(counter++);
                items.add(p);
            }

            res.setStatusCode(200);
            res.setData(items);
            res.setDescription(items.isEmpty()
                    ? "No " + status + " orders found for this customer."
                    : status + " orders fetched successfully.");

        } catch (Exception ex) {
            ex.printStackTrace();
            res.setStatusCode(statusCode);
            res.setDescription(statusMessage);
            res.setData(Collections.emptyList());
        }

        return ResponseEntity.ok(res);
    }

    private InvestmentOrderPojo toPojo(InvestmentOrder o, InvestmentProduct pr) {
        InvestmentOrderPojo p = new InvestmentOrderPojo();
        p.setOrderRef(o.getOrderRef());
        p.setEmailAddress(o.getEmailAddress());
        p.setAmount(o.getAmount());
        p.setCurrency(pr.getCurrency());
        p.setStatus(o.getStatus());
        p.setCreatedAt(o.getCreatedAt());
        p.setUpdatedAt(o.getUpdatedAt());
        return p;
    }

    @Transactional(readOnly = true)
    public ResponseEntity<ApiResponseModel> getAdminLiquidations(
            String status,
            String productCode,
            LocalDate fromDate,
            LocalDate toDate,
            Integer page,
            Integer size,
            boolean history
    ) {
        ApiResponseModel res = new ApiResponseModel();

        try {
            List<InvestmentOrderStatus> statuses = resolveLiquidationStatuses(status, history);
            List<InvestmentOrder> orders = orderRepo.findAdminLiquidations(
                    statuses,
                    normalize(productCode),
                    toStartOfDay(fromDate),
                    toExclusiveEndOfDay(toDate)
            );

            Map<String, Object> data = paginate(
                    orders.stream().map(this::toAdminLiquidationRow).toList(),
                    page,
                    size
            );

            res.setStatusCode(200);
            res.setDescription(history
                    ? "Historical liquidations fetched successfully."
                    : "Liquidation queue fetched successfully.");
            res.setData(data);
        } catch (IllegalArgumentException ex) {
            res.setStatusCode(400);
            res.setDescription(ex.getMessage());
            res.setData(emptyPage(page, size));
        } catch (Exception ex) {
            ex.printStackTrace();
            res.setStatusCode(500);
            res.setDescription("Something went wrong!");
            res.setData(emptyPage(page, size));
        }

        return ResponseEntity.ok(res);
    }

    @Transactional(readOnly = true)
    public ResponseEntity<ApiResponseModel> getAdminOrders(
            String type,
            String status,
            String productCode,
            String cutoffBucket,
            LocalDate fromDate,
            LocalDate toDate,
            Integer page,
            Integer size
    ) {
        ApiResponseModel res = new ApiResponseModel();

        try {
            List<InvestmentOrderType> types = resolveOrderTypes(type);
            List<InvestmentOrderStatus> statuses = resolveOrderStatuses(status);

            List<Map<String, Object>> rows = orderRepo.findAdminOrders(
                    types,
                    statuses,
                    normalize(productCode),
                    toStartOfDay(fromDate),
                    toExclusiveEndOfDay(toDate)
            ).stream()
                    .map(this::toAdminOrderRow)
                    .filter(row -> matchesCutoffBucket(row, cutoffBucket))
                    .toList();

            res.setStatusCode(200);
            res.setDescription("Investment and top-up orders fetched successfully.");
            res.setData(paginate(rows, page, size));
        } catch (IllegalArgumentException ex) {
            res.setStatusCode(400);
            res.setDescription(ex.getMessage());
            res.setData(emptyPage(page, size));
        } catch (Exception ex) {
            ex.printStackTrace();
            res.setStatusCode(500);
            res.setDescription("Something went wrong!");
            res.setData(emptyPage(page, size));
        }

        return ResponseEntity.ok(res);
    }

    @Transactional(readOnly = true)
    public ResponseEntity<ApiResponseModel> getAdminCustomerOrders(
            String email,
            String type,
            String status,
            Integer page,
            Integer size
    ) {
        ApiResponseModel res = new ApiResponseModel();

        try {
            String normalizedEmail = requireEmail(email);
            List<InvestmentOrderType> types = resolveOrderTypes(type);
            List<InvestmentOrderStatus> statuses = resolveOrderStatuses(status);
            List<Map<String, Object>> rows = orderRepo.findAdminCustomerOrders(normalizedEmail, types, statuses)
                    .stream()
                    .map(this::toAdminOrderRow)
                    .toList();

            res.setStatusCode(200);
            res.setDescription("Customer investment and top-up orders fetched successfully.");
            res.setData(paginate(rows, page, size));
        } catch (IllegalArgumentException ex) {
            res.setStatusCode(400);
            res.setDescription(ex.getMessage());
            res.setData(emptyPage(page, size));
        } catch (Exception ex) {
            ex.printStackTrace();
            res.setStatusCode(500);
            res.setDescription("Something went wrong!");
            res.setData(emptyPage(page, size));
        }

        return ResponseEntity.ok(res);
    }

    @Transactional(readOnly = true)
    public ResponseEntity<ApiResponseModel> getAdminCustomerLiquidations(
            String email,
            String status,
            Integer page,
            Integer size
    ) {
        ApiResponseModel res = new ApiResponseModel();

        try {
            String normalizedEmail = requireEmail(email);
            List<InvestmentOrderStatus> statuses = resolveCustomerLiquidationStatuses(status);
            List<Map<String, Object>> rows = orderRepo.findAdminCustomerLiquidations(normalizedEmail, statuses)
                    .stream()
                    .map(this::toAdminLiquidationRow)
                    .toList();

            res.setStatusCode(200);
            res.setDescription("Customer liquidation requests fetched successfully.");
            res.setData(paginate(rows, page, size));
        } catch (IllegalArgumentException ex) {
            res.setStatusCode(400);
            res.setDescription(ex.getMessage());
            res.setData(emptyPage(page, size));
        } catch (Exception ex) {
            ex.printStackTrace();
            res.setStatusCode(500);
            res.setDescription("Something went wrong!");
            res.setData(emptyPage(page, size));
        }

        return ResponseEntity.ok(res);
    }

    @Transactional(readOnly = true)
    public ResponseEntity<ApiResponseModel> getAdminCustomerPositions(
            String email,
            Integer page,
            Integer size
    ) {
        ApiResponseModel res = new ApiResponseModel();

        try {
            String normalizedEmail = requireEmail(email);
            List<Map<String, Object>> rows = positionRepo.findPositionsByEmailAddress(normalizedEmail)
                    .stream()
                    .map(this::toAdminPositionRow)
                    .toList();

            res.setStatusCode(200);
            res.setDescription("Customer investment positions fetched successfully.");
            res.setData(paginate(rows, page, size));
        } catch (IllegalArgumentException ex) {
            res.setStatusCode(400);
            res.setDescription(ex.getMessage());
            res.setData(emptyPage(page, size));
        } catch (Exception ex) {
            ex.printStackTrace();
            res.setStatusCode(500);
            res.setDescription("Something went wrong!");
            res.setData(emptyPage(page, size));
        }

        return ResponseEntity.ok(res);
    }

    private Map<String, Object> toAdminLiquidationRow(InvestmentOrder order) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("requester", order.getEmailAddress());
        item.put("orderRef", order.getOrderRef());
        item.put("parentOrderRef", order.getParentOrderRef());
        item.put("productCode", order.getProduct() != null ? order.getProduct().getProductCode() : null);
        item.put("productName", order.getProduct() != null ? order.getProduct().getName() : null);
        item.put("amount", order.getAmount());
        item.put("currency", order.getProduct() != null ? order.getProduct().getCurrency() : null);
        item.put("requestedAt", order.getCreatedAt());
        item.put("updatedAt", order.getUpdatedAt());
        item.put("status", order.getStatus() != null ? order.getStatus().name() : null);
        item.put("liquidationType", determineLiquidationType(order));
        item.put("fees", order.getFees());
        item.put("netAmount", order.getNetAmount());
        return item;
    }

    private Map<String, Object> toAdminOrderRow(InvestmentOrder order) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("requester", order.getEmailAddress());
        item.put("orderRef", order.getOrderRef());
        item.put("parentOrderRef", order.getParentOrderRef());
        item.put("type", order.getType() != null ? order.getType().name() : null);
        item.put("productCode", order.getProduct() != null ? order.getProduct().getProductCode() : null);
        item.put("productName", order.getProduct() != null ? order.getProduct().getName() : null);
        item.put("amount", order.getAmount());
        item.put("currency", order.getProduct() != null ? order.getProduct().getCurrency() : null);
        item.put("requestedAt", order.getCreatedAt());
        item.put("updatedAt", order.getUpdatedAt());
        item.put("status", order.getStatus() != null ? order.getStatus().name() : null);
        item.put("cutoffBucket", computeCutoffBucket(order));
        item.put("effectiveBusinessDate", computeEffectiveBusinessDate(order));
        return item;
    }

    private Map<String, Object> toAdminPositionRow(InvestmentPosition position) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("requester", position.getEmailAddress());
        item.put("orderRef", position.getOrderRef());
        item.put("productCode", position.getProduct() != null ? position.getProduct().getProductCode() : null);
        item.put("productName", position.getProductName() != null ? position.getProductName()
                : position.getProduct() != null ? position.getProduct().getName() : null);
        item.put("walletId", position.getWalletId());
        item.put("units", position.getUnits());
        item.put("investedAmount", position.getInvestedAmount());
        item.put("currentValue", position.getCurrentValue());
        item.put("accruedInterest", position.getAccruedInterest());
        item.put("totalAccruedInterest", position.getTotalAccruedInterest());
        item.put("reservedLiquidationAmount", position.getReservedLiquidationAmount());
        item.put("status", position.getStatus() != null ? position.getStatus().name() : null);
        item.put("interestStartDate", position.getInterestStartDate());
        item.put("createdAt", position.getCreatedAt());
        item.put("updatedAt", position.getUpdatedAt());
        item.put("maturityAt", position.getMaturityAt());
        item.put("settlementAt", position.getSettlementAt());
        return item;
    }

    private List<InvestmentOrderStatus> resolveLiquidationStatuses(String status, boolean history) {
        if (status != null && !status.isBlank()) {
            return List.of(InvestmentOrderStatus.valueOf(status.trim().toUpperCase(Locale.ROOT)));
        }

        if (history) {
            return List.of(
                    InvestmentOrderStatus.SETTLED,
                    InvestmentOrderStatus.LIQUIDATION_FAILED,
                    InvestmentOrderStatus.CANCELLED,
                    InvestmentOrderStatus.FAILED
            );
        }

        return List.of(
                InvestmentOrderStatus.LIQUIDATION_PENDING_APPROVAL,
                InvestmentOrderStatus.LIQUIDATION_PROCESSING
        );
    }

    private List<InvestmentOrderType> resolveOrderTypes(String type) {
        if (type == null || type.isBlank()) {
            return List.of(InvestmentOrderType.SUBSCRIPTION, InvestmentOrderType.TOPUP);
        }

        List<InvestmentOrderType> types = Arrays.stream(type.split(","))
                .map(value -> InvestmentOrderType.valueOf(value.trim().toUpperCase(Locale.ROOT)))
                .filter(orderType -> orderType == InvestmentOrderType.SUBSCRIPTION || orderType == InvestmentOrderType.TOPUP)
                .distinct()
                .collect(Collectors.toList());

        if (types.isEmpty()) {
            throw new IllegalArgumentException("At least one valid order type is required. Supported values: SUBSCRIPTION, TOPUP");
        }

        return types;
    }

    private List<InvestmentOrderStatus> resolveCustomerLiquidationStatuses(String status) {
        if (status != null && !status.isBlank()) {
            return Arrays.stream(status.split(","))
                    .map(value -> InvestmentOrderStatus.valueOf(value.trim().toUpperCase(Locale.ROOT)))
                    .distinct()
                    .collect(Collectors.toList());
        }

        return List.of(
                InvestmentOrderStatus.LIQUIDATION_PENDING_APPROVAL,
                InvestmentOrderStatus.LIQUIDATION_PROCESSING,
                InvestmentOrderStatus.SETTLED,
                InvestmentOrderStatus.CANCELLED,
                InvestmentOrderStatus.LIQUIDATION_FAILED,
                InvestmentOrderStatus.FAILED
        );
    }

    private List<InvestmentOrderStatus> resolveOrderStatuses(String status) {
        if (status != null && !status.isBlank()) {
            return Arrays.stream(status.split(","))
                    .map(value -> InvestmentOrderStatus.valueOf(value.trim().toUpperCase(Locale.ROOT)))
                    .distinct()
                    .collect(Collectors.toList());
        }

        return List.of(
                InvestmentOrderStatus.PENDING,
                InvestmentOrderStatus.HOLD_PLACED,
                InvestmentOrderStatus.SENT_TO_PARTNER,
                InvestmentOrderStatus.ACTIVE,
                InvestmentOrderStatus.MATURED,
                InvestmentOrderStatus.SETTLED,
                InvestmentOrderStatus.CANCELLED,
                InvestmentOrderStatus.FAILED
        );
    }

    private String determineLiquidationType(InvestmentOrder order) {
        InvestmentPosition position = order.getPosition();
        if (position == null || position.getCurrentValue() == null || order.getAmount() == null) {
            return "PARTIAL";
        }

        return order.getAmount().compareTo(position.getCurrentValue()) >= 0 ? "FULL" : "PARTIAL";
    }

    private String computeCutoffBucket(InvestmentOrder order) {
        if (order.getCreatedAt() == null || order.getProduct() == null || order.getProduct().getSubscriptionCutOffTime() == null) {
            return "BEFORE_CUTOFF";
        }

        LocalDateTime requestedAt = LocalDateTime.ofInstant(order.getCreatedAt(), LAGOS);
        LocalTime cutoff = order.getProduct().getSubscriptionCutOffTime();
        return requestedAt.toLocalTime().isBefore(cutoff) ? "BEFORE_CUTOFF" : "AFTER_CUTOFF_NEXT_DAY";
    }

    private LocalDate computeEffectiveBusinessDate(InvestmentOrder order) {
        if (order.getCreatedAt() == null) {
            return null;
        }

        LocalDateTime requestedAt = LocalDateTime.ofInstant(order.getCreatedAt(), LAGOS);
        LocalDate requestedDate = requestedAt.toLocalDate();
        return "AFTER_CUTOFF_NEXT_DAY".equals(computeCutoffBucket(order))
                ? requestedDate.plusDays(1)
                : requestedDate;
    }

    private boolean matchesCutoffBucket(Map<String, Object> row, String cutoffBucket) {
        if (cutoffBucket == null || cutoffBucket.isBlank()) {
            return true;
        }

        return cutoffBucket.trim().equalsIgnoreCase(String.valueOf(row.get("cutoffBucket")));
    }

    private Map<String, Object> paginate(List<Map<String, Object>> rows, Integer page, Integer size) {
        int safePage = page == null ? 0 : Math.max(page, 0);
        int safeSize = size == null ? 20 : Math.max(size, 1);

        int fromIndex = Math.min(safePage * safeSize, rows.size());
        int toIndex = Math.min(fromIndex + safeSize, rows.size());

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("content", rows.subList(fromIndex, toIndex));
        data.put("page", safePage);
        data.put("size", safeSize);
        data.put("totalElements", rows.size());
        data.put("totalPages", rows.isEmpty() ? 0 : (int) Math.ceil((double) rows.size() / safeSize));
        return data;
    }

    private Map<String, Object> emptyPage(Integer page, Integer size) {
        return paginate(Collections.emptyList(), page, size);
    }

    private String normalize(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private String requireEmail(String email) {
        String normalized = normalize(email);
        if (normalized == null) {
            throw new IllegalArgumentException("Customer email is required.");
        }
        return normalized;
    }

    private Instant toStartOfDay(LocalDate date) {
        return date == null ? null : date.atStartOfDay(LAGOS).toInstant();
    }

    private Instant toExclusiveEndOfDay(LocalDate date) {
        return date == null ? null : date.plusDays(1).atStartOfDay(LAGOS).toInstant();
    }
}
