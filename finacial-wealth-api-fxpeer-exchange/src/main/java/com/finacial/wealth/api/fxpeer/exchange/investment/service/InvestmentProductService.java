/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.finacial.wealth.api.fxpeer.exchange.investment.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.finacial.wealth.api.fxpeer.exchange.investment.domain.InvestmentProduct;
import com.finacial.wealth.api.fxpeer.exchange.investment.ennum.LiquidationFeeAppliedTo;
import com.finacial.wealth.api.fxpeer.exchange.investment.ennum.LiquidationFeeType;
import com.finacial.wealth.api.fxpeer.exchange.investment.ennum.ValuationMethod;
import com.finacial.wealth.api.fxpeer.exchange.investment.record.InvestmentProductRecord;
import com.finacial.wealth.api.fxpeer.exchange.investment.record.InvestmentProductUpsertRequest;
import com.finacial.wealth.api.fxpeer.exchange.investment.repo.InvestmentProductRepository;
import com.finacial.wealth.api.fxpeer.exchange.model.ApiResponseModel;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

/**
 *
 * @author olufemioshin
 */
@Service
public class InvestmentProductService {

    private final InvestmentProductRepository repo;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public InvestmentProductService(InvestmentProductRepository repo) {
        this.repo = repo;
    }

    // =========================
    // GET ALL PRODUCTS
    // =========================
    @Transactional(readOnly = true)
    public Map<String, Object> getInvestmentProducts() {
        Map<String, Object> res = new HashMap<String, Object>();

        try {
            List<InvestmentProduct> products = repo.findByActiveTrueOrderByNameAsc();

            res.put("statusCode", 200);
            res.put("description", products.isEmpty()
                    ? "No investment products available."
                    : "Investment products fetched successfully.");
            res.put("data", products);
            return res;

        } catch (Exception ex) {
            ex.printStackTrace();
            res.put("statusCode", 500);
            res.put("description", "Something went wrong");
            res.put("data", Collections.emptyList());
            return res;
        }
    }

    @Transactional(readOnly = true)
    public ApiResponseModel getAdminProducts() {
        try {
            List<Object> products = repo.findAllByOrderByNameAsc().stream()
                    .map(this::toRecord)
                    .collect(java.util.stream.Collectors.toList());

            return resp(200,
                    products.isEmpty() ? "No investment products available." : "Investment products fetched successfully.",
                    products);
        } catch (Exception ex) {
            ex.printStackTrace();
            return resp(500, "Unable to fetch investment products at the moment. Please try again.", Collections.emptyList());
        }
    }

    @Transactional
    public ApiResponseModel create(InvestmentProductUpsertRequest req) {
        try {
            if (repo.existsByProductCode(req.getProductCode())) {
                return resp(400, "Product code already exists: " + req.getProductCode(), null);
            }

            InvestmentProduct p = new InvestmentProduct();
            String validationError = validate(req, null);
            if (validationError != null) {
                return resp(400, validationError, null);
            }

            apply(p, req);
            repo.save(p);

            return resp(200, "Product created successfully.", p.getProductCode());
        } catch (Exception ex) {
            markRollbackOnly();
            ex.printStackTrace();
            return resp(500, "Unable to create product at the moment. Please try again.", null);
        }
    }

    @Transactional
    public ApiResponseModel update(InvestmentProductUpsertRequest req) {
        try {
            InvestmentProduct p = repo.findByProductCode(req.getProductCode()).orElse(null);
            if (p == null) {
                return resp(404, "Product not found: " + req.getProductCode(), null);
            }

            String validationError = validate(req, p);
            if (validationError != null) {
                return resp(400, validationError, null);
            }

            apply(p, req);
            repo.save(p);

            return resp(200, "Product updated successfully.", p.getProductCode());
        } catch (Exception ex) {
            markRollbackOnly();
            ex.printStackTrace();
            return resp(500, "Unable to update product at the moment. Please try again.", null);
        }
    }

    private void apply(InvestmentProduct p, InvestmentProductUpsertRequest r) {
        p.setProductCode(r.getProductCode());
        p.setName(r.getName());
        p.setType(r.getType());
        p.setCurrency(r.getCurrency());
        p.setMinimumInvestmentAmount(r.getMinimumInvestmentAmount());

        if (r.getUnitPrice() != null) {
            p.setUnitPrice(r.getUnitPrice());
        }
        if (r.getYieldPa() != null) {
            p.setYieldPa(r.getYieldPa());
        }
        if (r.getYieldYtd() != null) {
            p.setYieldYtd(r.getYieldYtd());
        }
        if (r.getValuationMethod() != null) {
            p.setValuationMethod(r.getValuationMethod());
        }
        if (r.getTenorDays() != null) {
            p.setTenorDays(r.getTenorDays());
        }
        if (r.getActive() != null) {
            p.setActive(r.getActive());
        }

        if (r.getPartnerProductCode() != null) {
            p.setPartnerProductCode(r.getPartnerProductCode());
        }
        if (r.getProspectusUrl() != null) {
            p.setProspectusUrl(r.getProspectusUrl());
        }
        if (r.getMetaJson() != null) {
            p.setMetaJson(r.getMetaJson());
        }
        if (r.getEnableProduct() != null) {
            p.setEnableProduct(r.getEnableProduct());
        }
        p.setMetaJson(mergeLiquidationFeeConfig(r, p.getMetaJson()));

        if (r.getPercentageCurrValue() != null) {
            p.setPercentageCurrValue(r.getPercentageCurrValue());
        }

        if (r.getScheduleMode() != null) {
            p.setScheduleMode(r.getScheduleMode());
        }
        if (r.getInterestAccrueType() != null) {
            p.setInterestAccrueType(r.getInterestAccrueType());
        }
        if (r.getInterestCapitalization() != null) {
            p.setInterestCapitalization(r.getInterestCapitalization());
        }

        if (r.getSettlementDelayMinutes() != null) {
            p.setSettlementDelayMinutes(r.getSettlementDelayMinutes());
        }
        if (r.getTenorMinutes() != null) {
            p.setTenorMinutes(r.getTenorMinutes());
        }
        if (r.getMaturityAtEndOfDay() != null) {
            p.setMaturityAtEndOfDay(r.getMaturityAtEndOfDay());
        }

        // FIXED mode fields (optional)
        if (r.getSettlementAt() != null) {
            p.setSettlementAt(r.getSettlementAt());
        }
        if (r.getMaturityAt() != null) {
            p.setMaturityAt(r.getMaturityAt());
        }

        p.setSubscriptionCutOffTime(r.getSubscriptionCutOffTime());
    }

    private InvestmentProductRecord toRecord(InvestmentProduct product) {
        InvestmentProductRecord record = new InvestmentProductRecord();
        record.setActive(product.isActive());
        record.setEnableProduct(product.getEnableProduct());
        record.setCurrency(product.getCurrency());
        record.setInvestmentType(product.getType() != null ? product.getType().toString() : null);
        record.setMaturityAtEndOfDay(product.getMaturityAtEndOfDay());
        record.setMinimumInvestmentAmount(product.getMinimumInvestmentAmount());
        record.setName(product.getName());
        record.setPartnerProductCode(product.getPartnerProductCode());
        record.setPercentageCurrValue(product.getPercentageCurrValue());
        record.setProductCode(product.getProductCode());
        record.setProductId(product.getId() == null ? null : product.getId().toString());
        record.setTenorDays(product.getTenorDays());
        record.setTenorMinutes(product.getTenorMinutes());
        record.setUnitPrice(product.getUnitPrice());
        record.setValuationMethod(product.getValuationMethod() != null ? product.getValuationMethod().name() : null);
        record.setLiquidationFeeAppliedTo(readFeeAppliedTo(product.getMetaJson(), "liquidationFee").map(Enum::name).orElse(null));
        record.setLiquidationFeeType(readFeeType(product.getMetaJson(), "liquidationFee").map(Enum::name).orElse(null));
        record.setLiquidationFeeRate(readFeeAmount(product.getMetaJson(), "liquidationFee", "rate"));
        record.setMinLiquidationFee(readFeeAmount(product.getMetaJson(), "liquidationFee", "minFee"));
        record.setLiquidationFeeCap(readFeeAmount(product.getMetaJson(), "liquidationFee", "cap"));
        record.setLockEnabled(readBooleanFromMeta(product.getMetaJson(), "lockConfig", "enabled"));
        record.setLockDays(readIntegerFromMeta(product.getMetaJson(), "lockConfig", "days"));
        record.setEarlyLiquidationFeeAppliedTo(readFeeAppliedTo(product.getMetaJson(), "earlyLiquidationFee").map(Enum::name).orElse(null));
        record.setEarlyLiquidationFeeType(readFeeType(product.getMetaJson(), "earlyLiquidationFee").map(Enum::name).orElse(null));
        record.setEarlyLiquidationFeeRate(readFeeAmount(product.getMetaJson(), "earlyLiquidationFee", "rate"));
        record.setEarlyLiquidationFeeCap(readFeeAmount(product.getMetaJson(), "earlyLiquidationFee", "cap"));
        record.setYieldPa(product.getYieldPa());
        record.setYieldYtd(product.getYieldYtd());
        return record;
    }

    private String validate(InvestmentProductUpsertRequest req, InvestmentProduct existingProduct) {
        ValuationMethod valuationMethod = resolveValuationMethod(req, existingProduct);
        BigDecimal yieldPa = req.getYieldPa() != null
                ? req.getYieldPa()
                : existingProduct == null ? null : existingProduct.getYieldPa();
        BigDecimal unitPrice = req.getUnitPrice() != null
                ? req.getUnitPrice()
                : existingProduct == null ? null : existingProduct.getUnitPrice();

        if (valuationMethod == ValuationMethod.RATE) {
            if (yieldPa == null || yieldPa.compareTo(BigDecimal.ZERO) <= 0) {
                return "yieldPa must be provided and greater than 0 when valuationMethod is RATE";
            }
        } else if (unitPrice == null || unitPrice.compareTo(BigDecimal.ZERO) <= 0) {
            return "unitPrice must be provided and greater than 0 when valuationMethod is UNIT_PRICE";
        }

        boolean anyLiquidationFeeField
                = req.getLiquidationFeeAppliedTo() != null
                || req.getLiquidationFeeType() != null
                || req.getLiquidationFeeRate() != null
                || req.getMinLiquidationFee() != null
                || req.getLiquidationFeeCap() != null;

        String liquidationValidationError = validateFeeConfig(
                "liquidation",
                valuationMethod,
                anyLiquidationFeeField,
                req.getLiquidationFeeAppliedTo(),
                req.getLiquidationFeeType(),
                req.getLiquidationFeeRate(),
                req.getMinLiquidationFee(),
                req.getLiquidationFeeCap()
        );
        if (liquidationValidationError != null) {
            return liquidationValidationError;
        }

        boolean anyEarlyLiquidationFeeField
                = req.getEarlyLiquidationFeeAppliedTo() != null
                || req.getEarlyLiquidationFeeType() != null
                || req.getEarlyLiquidationFeeRate() != null
                || req.getEarlyLiquidationFeeCap() != null;

        String earlyLiquidationValidationError = validateFeeConfig(
                "earlyLiquidation",
                valuationMethod,
                anyEarlyLiquidationFeeField,
                req.getEarlyLiquidationFeeAppliedTo(),
                req.getEarlyLiquidationFeeType(),
                req.getEarlyLiquidationFeeRate(),
                null,
                req.getEarlyLiquidationFeeCap()
        );
        if (earlyLiquidationValidationError != null) {
            return earlyLiquidationValidationError;
        }

        Boolean lockEnabled = req.getLockEnabled() != null
                ? req.getLockEnabled()
                : readBooleanFromMeta(existingProduct == null ? null : existingProduct.getMetaJson(), "lockConfig", "enabled");
        Integer lockDays = req.getLockDays() != null
                ? req.getLockDays()
                : readIntegerFromMeta(existingProduct == null ? null : existingProduct.getMetaJson(), "lockConfig", "days");

        boolean anyLockField
                = req.getLockEnabled() != null
                || req.getLockDays() != null
                || anyEarlyLiquidationFeeField;

        if (Boolean.TRUE.equals(lockEnabled)) {
            if (lockDays == null || lockDays <= 0) {
                return "lockDays must be provided and greater than 0 when lockEnabled is true";
            }
        } else if (lockDays != null && lockDays <= 0) {
            return "lockDays must be greater than 0 when provided";
        }

        if (anyEarlyLiquidationFeeField && !Boolean.TRUE.equals(lockEnabled)) {
            return "lockEnabled must be true when early liquidation fee is configured";
        }

        if (anyLockField && req.getLockEnabled() == null && req.getLockDays() == null && existingProduct == null) {
            return "lockEnabled must be provided when lock configuration is supplied on product creation";
        }

        return null;
    }

    private String validateFeeConfig(
            String label,
            ValuationMethod valuationMethod,
            boolean anyFeeField,
            LiquidationFeeAppliedTo appliedTo,
            LiquidationFeeType feeType,
            BigDecimal feeRate,
            BigDecimal minFee,
            BigDecimal feeCap
    ) {
        if (!anyFeeField) {
            return null;
        }

        if (appliedTo == null || feeType == null || feeRate == null) {
            return label + " fee appliedTo, type and rate must all be provided together";
        }

        if (feeType != LiquidationFeeType.RATE) {
            return "Only RATE liquidation fee type is supported for now";
        }

        if (feeRate.compareTo(BigDecimal.ZERO) < 0) {
            return label + " fee rate cannot be negative";
        }

        if (minFee != null && minFee.compareTo(BigDecimal.ZERO) < 0) {
            return label + " minimum fee cannot be negative";
        }

        if (feeCap != null && feeCap.compareTo(BigDecimal.ZERO) < 0) {
            return label + " fee cap cannot be negative";
        }

        if (minFee != null && feeCap != null && feeCap.compareTo(minFee) < 0) {
            return label + " fee cap cannot be less than the minimum fee";
        }

        if (valuationMethod == ValuationMethod.UNIT_PRICE && appliedTo != LiquidationFeeAppliedTo.TOTAL_VALUE) {
            return "UNIT_PRICE products currently support liquidation fees applied to TOTAL_VALUE only";
        }

        return null;
    }

    private ValuationMethod resolveValuationMethod(InvestmentProductUpsertRequest req, InvestmentProduct existingProduct) {
        if (req.getValuationMethod() != null) {
            return req.getValuationMethod();
        }
        if (existingProduct != null) {
            return existingProduct.resolvedValuationMethod();
        }
        return ValuationMethod.RATE;
    }

    private String mergeLiquidationFeeConfig(InvestmentProductUpsertRequest req, String currentMetaJson) {
        try {
            ObjectNode root = readMetaJson(currentMetaJson);
            boolean hasLiquidationFeeConfig
                    = req.getLiquidationFeeAppliedTo() != null
                    || req.getLiquidationFeeType() != null
                    || req.getLiquidationFeeRate() != null
                    || req.getMinLiquidationFee() != null
                    || req.getLiquidationFeeCap() != null;
            boolean hasLockConfig
                    = req.getLockEnabled() != null
                    || req.getLockDays() != null;
            boolean hasEarlyLiquidationFeeConfig
                    = req.getEarlyLiquidationFeeAppliedTo() != null
                    || req.getEarlyLiquidationFeeType() != null
                    || req.getEarlyLiquidationFeeRate() != null
                    || req.getEarlyLiquidationFeeCap() != null;

            if (!hasLiquidationFeeConfig && !hasLockConfig && !hasEarlyLiquidationFeeConfig) {
                return root.isEmpty() ? null : objectMapper.writeValueAsString(root);
            }

            if (hasLiquidationFeeConfig) {
                ObjectNode liquidationFee = root.with("liquidationFee");
                liquidationFee.put("appliedTo", req.getLiquidationFeeAppliedTo().name());
                liquidationFee.put("type", req.getLiquidationFeeType().name());
                liquidationFee.put("rate", req.getLiquidationFeeRate());
                if (req.getMinLiquidationFee() != null) {
                    liquidationFee.put("minFee", req.getMinLiquidationFee());
                } else {
                    liquidationFee.remove("minFee");
                }
                if (req.getLiquidationFeeCap() != null) {
                    liquidationFee.put("cap", req.getLiquidationFeeCap());
                } else {
                    liquidationFee.remove("cap");
                }
            }

            if (hasLockConfig) {
                ObjectNode lockConfig = root.with("lockConfig");
                if (req.getLockEnabled() != null) {
                    lockConfig.put("enabled", req.getLockEnabled());
                }
                if (req.getLockDays() != null) {
                    lockConfig.put("days", req.getLockDays());
                } else if (Boolean.FALSE.equals(req.getLockEnabled())) {
                    lockConfig.remove("days");
                } else {
                    // preserve existing lock days on unrelated updates
                }
            }

            if (hasEarlyLiquidationFeeConfig) {
                ObjectNode earlyLiquidationFee = root.with("earlyLiquidationFee");
                earlyLiquidationFee.put("appliedTo", req.getEarlyLiquidationFeeAppliedTo().name());
                earlyLiquidationFee.put("type", req.getEarlyLiquidationFeeType().name());
                earlyLiquidationFee.put("rate", req.getEarlyLiquidationFeeRate());
                if (req.getEarlyLiquidationFeeCap() != null) {
                    earlyLiquidationFee.put("cap", req.getEarlyLiquidationFeeCap());
                } else {
                    earlyLiquidationFee.remove("cap");
                }
            } else if (Boolean.FALSE.equals(req.getLockEnabled())) {
                root.remove("earlyLiquidationFee");
            }

            return objectMapper.writeValueAsString(root);
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to process liquidation fee configuration", ex);
        }
    }

    private ObjectNode readMetaJson(String metaJson) {
        try {
            if (metaJson == null || metaJson.isBlank()) {
                return objectMapper.createObjectNode();
            }
            JsonNode parsed = objectMapper.readTree(metaJson);
            if (parsed instanceof ObjectNode objectNode) {
                return objectNode.deepCopy();
            }
            return objectMapper.createObjectNode();
        } catch (Exception ex) {
            return objectMapper.createObjectNode();
        }
    }

    private Boolean readBooleanFromMeta(String metaJson, String nodeName, String fieldName) {
        JsonNode value = readMetaJson(metaJson).path(nodeName).path(fieldName);
        if (value.isMissingNode() || value.isNull()) {
            return null;
        }
        return value.asBoolean();
    }

    private Integer readIntegerFromMeta(String metaJson, String nodeName, String fieldName) {
        JsonNode value = readMetaJson(metaJson).path(nodeName).path(fieldName);
        if (value.isMissingNode() || value.isNull() || value.asText().isBlank()) {
            return null;
        }
        return value.asInt();
    }

    private java.util.Optional<LiquidationFeeAppliedTo> readFeeAppliedTo(String metaJson, String nodeName) {
        String value = readTextFromMeta(metaJson, nodeName, "appliedTo");
        if (value == null) {
            return java.util.Optional.empty();
        }
        try {
            return java.util.Optional.of(LiquidationFeeAppliedTo.valueOf(value));
        } catch (IllegalArgumentException ex) {
            return java.util.Optional.empty();
        }
    }

    private java.util.Optional<LiquidationFeeType> readFeeType(String metaJson, String nodeName) {
        String value = readTextFromMeta(metaJson, nodeName, "type");
        if (value == null) {
            return java.util.Optional.empty();
        }
        try {
            return java.util.Optional.of(LiquidationFeeType.valueOf(value));
        } catch (IllegalArgumentException ex) {
            return java.util.Optional.empty();
        }
    }

    private BigDecimal readFeeAmount(String metaJson, String nodeName, String fieldName) {
        JsonNode value = readMetaJson(metaJson).path(nodeName).path(fieldName);
        if (value.isMissingNode() || value.isNull() || value.asText().isBlank()) {
            return null;
        }
        try {
            return value.decimalValue();
        } catch (Exception ex) {
            return null;
        }
    }

    private String readTextFromMeta(String metaJson, String nodeName, String fieldName) {
        JsonNode value = readMetaJson(metaJson).path(nodeName).path(fieldName);
        if (value.isMissingNode() || value.isNull() || value.asText().isBlank()) {
            return null;
        }
        return value.asText();
    }

    private void markRollbackOnly() {
        try {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
        } catch (Exception ignored) {
        }
    }

    private ApiResponseModel resp(int code, String desc, Object data) {
        ApiResponseModel response = new ApiResponseModel();
        response.setStatusCode(code);
        response.setDescription(desc);
        response.setData(data);
        return response;
    }
}
