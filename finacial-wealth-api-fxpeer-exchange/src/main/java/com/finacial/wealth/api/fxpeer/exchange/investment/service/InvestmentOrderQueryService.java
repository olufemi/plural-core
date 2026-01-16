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
import com.finacial.wealth.api.fxpeer.exchange.investment.ennum.InvestmentOrderStatus;
import com.finacial.wealth.api.fxpeer.exchange.investment.record.InvestmentOrderPojo;
import com.finacial.wealth.api.fxpeer.exchange.investment.repo.InvestmentOrderRepository;
import com.finacial.wealth.api.fxpeer.exchange.investment.repo.InvestmentProductRepository;
import com.finacial.wealth.api.fxpeer.exchange.model.ApiResponseModel;
import com.finacial.wealth.api.fxpeer.exchange.util.UttilityMethods;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class InvestmentOrderQueryService {

    private final InvestmentOrderRepository orderRepo;
    private final UttilityMethods utilService; // your JWT claim extractor
    private final InvestmentProductRepository productRepo;

    public InvestmentOrderQueryService(InvestmentOrderRepository orderRepo, UttilityMethods utilService,
            InvestmentProductRepository productRepo) {
        this.orderRepo = orderRepo;
        this.utilService = utilService;
        this.productRepo = productRepo;
    }

    @Transactional(readOnly = true)
    public ResponseEntity<ApiResponseModel> getOrdersByStatusForCustomer(String auth, InvestmentOrderStatus status) {
        ApiResponseModel res = new ApiResponseModel();
        int statusCode = 500;
        String statusMessage = "Something went wrong!";

        try {
            statusCode = 400;

            if (auth == null || auth.trim().isEmpty()) {
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
            }

            List<InvestmentOrder> orders
                    = orderRepo.findByEmailAddressAndStatusOrderByUpdatedAtDesc(email, status);

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
}
