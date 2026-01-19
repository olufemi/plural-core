/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.finacial.wealth.api.fxpeer.exchange.investment.service;

import com.finacial.wealth.api.fxpeer.exchange.common.NotFoundException;
import com.finacial.wealth.api.fxpeer.exchange.investment.domain.InvestmentProduct;
import com.finacial.wealth.api.fxpeer.exchange.investment.record.InvestmentProductUpsertRequest;
import com.finacial.wealth.api.fxpeer.exchange.investment.repo.InvestmentProductRepository;
import org.springframework.transaction.annotation.Transactional;
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
    public Map<String, Object> create(InvestmentProductUpsertRequest req) {
        if (repo.existsByProductCode(req.getProductCode())) {
            return resp(400, "Product code already exists: " + req.getProductCode(), null);
        }

        InvestmentProduct p = new InvestmentProduct();
        apply(p, req);
        repo.save(p);

        return resp(200, "Product created successfully.", p.getProductCode());
    }

    @Transactional
    public Map<String, Object> update(InvestmentProductUpsertRequest req) {
        InvestmentProduct p = repo.findByProductCode(req.getProductCode())
                .orElseThrow(() -> new NotFoundException("Product not found: " + req.getProductCode()));

        apply(p, req);
        repo.save(p);

        return resp(200, "Product updated successfully.", p.getProductCode());
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

    private Map<String, Object> resp(int code, String desc, Object data) {
        Map<String, Object> m = new HashMap<String, Object>();
        m.put("statusCode", code);
        m.put("description", desc);
        m.put("data", data);
        return m;
    }
}
