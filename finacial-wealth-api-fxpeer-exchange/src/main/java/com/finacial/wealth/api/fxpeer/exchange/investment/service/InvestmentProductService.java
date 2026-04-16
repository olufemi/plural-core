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
                || req.getMinLiquidationFee() != null;

        if (!anyLiquidationFeeField) {
            return null;
        }

        if (req.getLiquidationFeeAppliedTo() == null
                || req.getLiquidationFeeType() == null
                || req.getLiquidationFeeRate() == null) {
            return "liquidationFeeAppliedTo, liquidationFeeType and liquidationFeeRate must all be provided together";
        }

        if (req.getLiquidationFeeType() != LiquidationFeeType.RATE) {
            return "Only RATE liquidation fee type is supported for now";
        }

        if (req.getLiquidationFeeRate().compareTo(BigDecimal.ZERO) < 0) {
            return "liquidationFeeRate cannot be negative";
        }

        if (req.getMinLiquidationFee() != null && req.getMinLiquidationFee().compareTo(BigDecimal.ZERO) < 0) {
            return "minLiquidationFee cannot be negative";
        }

        if (valuationMethod == ValuationMethod.UNIT_PRICE
                && req.getLiquidationFeeAppliedTo() != LiquidationFeeAppliedTo.TOTAL_VALUE) {
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
                    || req.getMinLiquidationFee() != null;

            if (!hasLiquidationFeeConfig) {
                return root.isEmpty() ? null : objectMapper.writeValueAsString(root);
            }

            ObjectNode liquidationFee = root.with("liquidationFee");
            liquidationFee.put("appliedTo", req.getLiquidationFeeAppliedTo().name());
            liquidationFee.put("type", req.getLiquidationFeeType().name());
            liquidationFee.put("rate", req.getLiquidationFeeRate());
            if (req.getMinLiquidationFee() != null) {
                liquidationFee.put("minFee", req.getMinLiquidationFee());
            } else {
                liquidationFee.remove("minFee");
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
