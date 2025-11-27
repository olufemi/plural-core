/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.finacial.wealth.api.fxpeer.exchange.investment.service;

import com.finacial.wealth.api.fxpeer.exchange.common.BusinessException;
import com.finacial.wealth.api.fxpeer.exchange.common.NotFoundException;
import com.finacial.wealth.api.fxpeer.exchange.domain.AddAccountDetails;
import com.finacial.wealth.api.fxpeer.exchange.domain.AddAccountDetailsRepo;
import com.finacial.wealth.api.fxpeer.exchange.domain.AppConfig;
import com.finacial.wealth.api.fxpeer.exchange.domain.AppConfigRepo;
import com.finacial.wealth.api.fxpeer.exchange.domain.RegWalletInfo;
import com.finacial.wealth.api.fxpeer.exchange.domain.RegWalletInfoRepository;
import com.finacial.wealth.api.fxpeer.exchange.feign.ProfilingProxies;
import com.finacial.wealth.api.fxpeer.exchange.feign.TransactionServiceProxies;
import com.finacial.wealth.api.fxpeer.exchange.fx.p.p.wallet.FinWealthPaymentTransactionRepo;
import com.finacial.wealth.api.fxpeer.exchange.inter.airtime.security.PreDebitResult;
import com.finacial.wealth.api.fxpeer.exchange.inter.airtime.security.ProcSochitelServices;
import com.finacial.wealth.api.fxpeer.exchange.inter.airtime.security.ProcessTrnsactionReq;
import com.finacial.wealth.api.fxpeer.exchange.investment.domain.InvestmentOrder;
import com.finacial.wealth.api.fxpeer.exchange.investment.domain.InvestmentPosition;
import com.finacial.wealth.api.fxpeer.exchange.investment.domain.InvestmentProduct;
import com.finacial.wealth.api.fxpeer.exchange.investment.ennum.InvestmentOrderStatus;
import com.finacial.wealth.api.fxpeer.exchange.investment.ennum.InvestmentOrderType;
import com.finacial.wealth.api.fxpeer.exchange.investment.ennum.InvestmentPositionStatus;
import com.finacial.wealth.api.fxpeer.exchange.investment.ennum.InvestmentType;
import com.finacial.wealth.api.fxpeer.exchange.investment.interfface.WalletClient;
import com.finacial.wealth.api.fxpeer.exchange.investment.record.CreateInvestmentSubscriptionRequest;
import com.finacial.wealth.api.fxpeer.exchange.investment.record.CreateSubscriptionReq;
import com.finacial.wealth.api.fxpeer.exchange.investment.record.InvestmentOrderResponse;
import com.finacial.wealth.api.fxpeer.exchange.investment.record.InvestmentProductRecord;
import com.finacial.wealth.api.fxpeer.exchange.investment.record.LiquidateInvestmentRequest;
import com.finacial.wealth.api.fxpeer.exchange.investment.record.PartnerSubscriptionResponse;
import com.finacial.wealth.api.fxpeer.exchange.investment.record.PreDebitInvestmentResult;
import com.finacial.wealth.api.fxpeer.exchange.investment.record.ProcessDebitWalletForInvestment;
import com.finacial.wealth.api.fxpeer.exchange.investment.repo.InvestmentOrderRepository;
import com.finacial.wealth.api.fxpeer.exchange.investment.repo.InvestmentPositionRepository;
import com.finacial.wealth.api.fxpeer.exchange.investment.repo.InvestmentProductRepository;
import com.finacial.wealth.api.fxpeer.exchange.model.ApiResponseModel;
import com.finacial.wealth.api.fxpeer.exchange.model.BaseResponse;
import com.finacial.wealth.api.fxpeer.exchange.model.CreditWalletCaller;
import com.finacial.wealth.api.fxpeer.exchange.model.DebitWalletCaller;
import com.finacial.wealth.api.fxpeer.exchange.model.ManageFeesConfigReq;
import com.finacial.wealth.api.fxpeer.exchange.model.ValidateCountryCode;
import com.finacial.wealth.api.fxpeer.exchange.model.WalletNo;
import com.finacial.wealth.api.fxpeer.exchange.order.WalletInfoValiAcctBal;
import com.finacial.wealth.api.fxpeer.exchange.util.GlobalMethods;
import com.finacial.wealth.api.fxpeer.exchange.util.UttilityMethods;
import com.google.common.collect.HashBiMap;
import com.google.gson.Gson;
import jakarta.annotation.PostConstruct;
import static jakarta.persistence.GenerationType.UUID;
import jakarta.transaction.Transactional;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 *
 * @author olufemioshin
 */
@Service
@Transactional
public class InvestmentOrderService {

    private static final Logger log = LoggerFactory.getLogger(InvestmentOrderService.class);

    @Value("${jobs.one-shot-delay-ms:10}")
    private long delayMsSet;

    @Value("${jobs.one-shot-delay-minutes:10}")
    private long delayMinutes;

    private final InvestmentProductRepository productRepo;
    private final InvestmentPositionRepository positionRepo;
    private final InvestmentOrderRepository orderRepo;
    // private final WalletClient walletClient;
    // private final InvestmentPartnerClient partnerClient;
    private final ActivityService activityService;

    private final UttilityMethods utilService;
    private final RegWalletInfoRepository regWalletInfoRepository;

    private final FinWealthPaymentTransactionRepo finWealthPaymentTransactionRepo;
    private final TransactionServiceProxies transactionServiceProxies;
    private final AddAccountDetailsRepo addAccountDetailsRepo;
    private final AppConfigRepo appConfigRepo;
    private final ProfilingProxies profilingProxies;
    private final InvestmentHistoryService investmentHistoryService;

    public InvestmentOrderService(AppConfigRepo appConfigRepo,
            AddAccountDetailsRepo addAccountDetailsRepo,
            TransactionServiceProxies transactionServiceProxies,
            InvestmentProductRepository productRepo,
            InvestmentPositionRepository positionRepo,
            InvestmentOrderRepository orderRepo,
            // WalletClient walletClient,
            // InvestmentPartnerClient partnerClient,
            ActivityService activityService,
            RegWalletInfoRepository regWalletInfoRepository,
            UttilityMethods utilService, FinWealthPaymentTransactionRepo finWealthPaymentTransactionRepo,
            ProfilingProxies profilingProxies,
            InvestmentHistoryService investmentHistoryService
    ) {
        this.appConfigRepo = appConfigRepo;
        this.addAccountDetailsRepo = addAccountDetailsRepo;
        this.transactionServiceProxies = transactionServiceProxies;
        this.productRepo = productRepo;
        this.positionRepo = positionRepo;
        this.orderRepo = orderRepo;
        // this.walletClient = walletClient;
        // this.partnerClient = partnerClient;
        this.activityService = activityService;
        this.regWalletInfoRepository = regWalletInfoRepository;
        this.utilService = utilService;
        this.finWealthPaymentTransactionRepo = finWealthPaymentTransactionRepo;
        this.profilingProxies = profilingProxies;
        this.investmentHistoryService = investmentHistoryService;

    }

    @PostConstruct
    public void triggerOnce() {
        log.info("Scheduling onSubscriptionSettled to run in {} minutes", delayMinutes);
        //runOnce(this::onSubscriptionSettled, delayMinutes, TimeUnit.MINUTES);
    }

    public void runOnce(Runnable task, long delay, TimeUnit unit) {
        scheduler.schedule(() -> {
            try {
                log.info("Running one-shot job onSubscriptionSettled...");
                task.run();
            } catch (Exception ex) {
                log.error("Error in one-shot job", ex);
            } finally {
                scheduler.shutdown();
            }
        }, delay, unit);
    }

    public ResponseEntity<ApiResponseModel> getProducts() {
        int statusCode = 500;
        ApiResponseModel responseModel = new ApiResponseModel();
        try {
            statusCode = 400;

            List<Object> mapAll = new ArrayList<Object>();
            for (InvestmentProduct getKul : productRepo.findAll()) {
                if (getKul.getType().equals(InvestmentType.MONEY_MARKET)) {

                    InvestmentProductRecord getK = new InvestmentProductRecord();
                    getK.setActive(getKul.isActive());
                    getK.setCurrency(getKul.getCurrency());
                    getK.setInvestmentType(getKul.getType().toString());
                    getK.setMaturityAtEndOfDay(getKul.getMaturityAtEndOfDay());
                    getK.setMinimumInvestmentAmount(getKul.getMinimumInvestmentAmount());
                    getK.setName(getKul.getName());
                    getK.setPartnerProductCode(getKul.getPartnerProductCode());
                    getK.setPercentageCurrValue(getKul.getPercentageCurrValue());
                    getK.setProductCode(getKul.getProductCode());
                    getK.setProductId(getKul.getId().toString());
                    getK.setTenorDays(getKul.getTenorDays());
                    getK.setTenorMinutes(getKul.getTenorMinutes());
                    getK.setUnitPrice(getKul.getUnitPrice());
                    getK.setYieldPa(getKul.getYieldPa());
                    getK.setYieldYtd(getKul.getYieldYtd());
                    mapAll.add(getK);

                }

            }

            responseModel.setData(mapAll);
            responseModel.setDescription("Products details pulled successfully.");
            responseModel.setStatusCode(200);

        } catch (Exception ex) {
            ex.printStackTrace();
            responseModel.setStatusCode(statusCode);
            responseModel.setDescription("Something went wrong!");
        }

        return ResponseEntity.ok(responseModel);

    }

    private PreDebitInvestmentResult debitAndAddToInvestmentWallet(
            ProcessDebitWalletForInvestment rq,
            String auth,
            String email,
            String phoneNumber,
            BaseResponse feeConfig,
            String processId
    ) {
        PreDebitInvestmentResult out = new PreDebitInvestmentResult();
        out.setSuccess(false);

        System.out.println(" debitAndAddToInvestmentWallet rq ::::::::::::::::  %S  " + new Gson().toJson(rq));
        System.out.println(" debitAndAddToInvestmentWallet email ::::::::::::::::  %S  " + new Gson().toJson(email));
        System.out.println(" debitAndAddToInvestmentWallet phoneNumber ::::::::::::::::  %S  " + new Gson().toJson(phoneNumber));
        System.out.println(" debitAndAddToInvestmentWallet feeConfig ::::::::::::::::  %S  " + new Gson().toJson(feeConfig));

        System.out.println(" debitAndAddToInvestmentWallet processId ::::::::::::::::  %S  " + new Gson().toJson(processId));

        try {
            if (feeConfig == null || feeConfig.getStatusCode() != 200 || feeConfig.getData() == null) {
                BaseResponse err = new BaseResponse(404, "Invalid fee configuration");
                if (feeConfig != null && feeConfig.getDescription() != null) {
                    err.setDescription(feeConfig.getDescription());
                }
                out.setError(err);
                return out;
            }

            Optional<RegWalletInfo> regOpt = regWalletInfoRepository.findByPhoneNumber(phoneNumber);
            if (regOpt.isEmpty()) {
                out.setError(new BaseResponse(404, "Wallet not found for user"));
                return out;
            }
            RegWalletInfo reg = regOpt.get();

            String feesStr = String.valueOf(feeConfig.getData().get("fees"));
            BigDecimal fees = new BigDecimal(feesStr);
            BigDecimal receiveAmount = rq.getGrossDebitAmount();
            BigDecimal finCharges = receiveAmount;
            String accountNumber = null;
            String walletId = null;

            if ("CAD".equalsIgnoreCase(rq.getCurrencyCode())) {
                accountNumber = phoneNumber;
                walletId = regOpt.get().getWalletId();

            } else {

                // Resolve debit account (preserve override to phoneNumber)
                List<AddAccountDetails> acctList = addAccountDetailsRepo.findByEmailAddressrData(email);
                if (acctList == null || acctList.isEmpty()) {
                    out.setError(new BaseResponse(404, "No linked accounts for user"));
                    return out;
                }
                accountNumber = acctList.get(0).getAccountNumber();
                walletId = acctList.get(0).getWalletId();
            }

            if (accountNumber == null || accountNumber.isBlank()) {
                out.setError(new BaseResponse(400, "Unable to resolve debit account number"));
                return out;
            }

            WalletInfoValiAcctBal wVal = new WalletInfoValiAcctBal();
            wVal.setAccountNumber(accountNumber);
            wVal.setRequestedAmount(finCharges);
            wVal.setWalletId(reg.getWalletId());

            BaseResponse balRes = transactionServiceProxies.validateAccountBalnce(wVal, auth);
            if (balRes.getStatusCode() != 200) {
                out.setError(new BaseResponse(balRes.getStatusCode(), balRes.getDescription()));
                return out;
            }

            // Debit buyer
            DebitWalletCaller debitBuyer = new DebitWalletCaller();
            debitBuyer.setAuth("Money_Market");
            debitBuyer.setFees(feesStr);
            debitBuyer.setFinalCHarges(finCharges.toString());
            debitBuyer.setNarration(rq.getCurrencyCode() + "_Withdrawal");
            debitBuyer.setPhoneNumber(accountNumber);
            debitBuyer.setTransAmount(finCharges.subtract(new BigDecimal(feesStr)).toString());
            debitBuyer.setTransactionId(processId);

            System.out.println("preDebitAndSettleAirtime req ::::::::::::::::  %S  " + new Gson().toJson(debitBuyer));

            BaseResponse debitBuyerRes = transactionServiceProxies.debitCustomerWithType(debitBuyer, "CUSTOMER", auth);
            System.out.println(" preDebitAndSettleAirtime debitBuyerRes ::::::::::::::::  %S  " + new Gson().toJson(debitBuyerRes));

            if (debitBuyerRes.getStatusCode() != 200) {
                out.setError(new BaseResponse(debitBuyerRes.getStatusCode(), debitBuyerRes.getDescription()));
                return out;
            }
            out.setLegBuyerDebited(true);

            // GL + Seller
            List<AppConfig> confs = appConfigRepo.findByConfigName(rq.getCurrencyCode());
            String GGL_ACCOUNT = null, AIRTIME_GGL_ACCOUNT = null, GGL_CODE = null;
            for (AppConfig c : confs) {
                if (rq.getCurrencyCode().equalsIgnoreCase(c.getConfigName())) {
                    GGL_ACCOUNT = c.getConfigValue();
                    AIRTIME_GGL_ACCOUNT = c.getProductValue();
                    GGL_CODE = rq.getCurrencyCode();
                }
            }
            if (GGL_ACCOUNT == null || AIRTIME_GGL_ACCOUNT == null || GGL_CODE == null) {
                out.setError(new BaseResponse(500, "GL configuration missing for currency " + rq.getCurrencyCode()));
                return out;
            }
            String decryptedGL = utilService.decryptData(GGL_ACCOUNT);
            String sellerAcctNumber = utilService.decryptData(AIRTIME_GGL_ACCOUNT);

            // Debit GL (buyer leg)
            DebitWalletCaller debGLCredit = new DebitWalletCaller();
            debGLCredit.setAuth(rq.getCurrencyCode());
            debGLCredit.setFees("0.00");
            debGLCredit.setFinalCHarges(receiveAmount.toString());
            debGLCredit.setNarration(debitBuyer.getNarration());
            debGLCredit.setPhoneNumber(decryptedGL);
            debGLCredit.setTransAmount(receiveAmount.toString());
            debGLCredit.setTransactionId(processId);

            BaseResponse debitGLRes = transactionServiceProxies.debitCustomerWithType(debGLCredit, rq.getCurrencyCode(), auth);
            System.out.println(" preDebitAndSettleAirtime debitGLRes ::::::::::::::::  %S  " + new Gson().toJson(debitGLRes));

            if (debitGLRes.getStatusCode() != 200) {
                out.setError(new BaseResponse(debitGLRes.getStatusCode(), debitGLRes.getDescription()));
                return out;
            }
            out.setLegGLDebited(true);

            // success payload
            out.setSuccess(true);
            out.setProcessId(processId);
            out.setBuyerAccountNumber(accountNumber);
            out.setGlAccountDecrypted(decryptedGL);
            out.setSellerAccountNumber(sellerAcctNumber);
            out.setGglCode(GGL_CODE);
            out.setFees(fees);
            out.setFinCharges(finCharges);
            out.setReceiveAmount(receiveAmount);
            out.setWalletId(walletId);

            return out;

        } catch (Exception e) {
            out.setError(new BaseResponse(500, "Pre-debit/settlement failed: " + e.getClass().getSimpleName()));
            return out;
        }
    }

    public BaseResponse createSubscriptionCaller(CreateSubscriptionReq rq, String auth) throws IOException {
        BaseResponse resp = new BaseResponse();
        int statusCode = 500;
        String description = "Something went wrong, please retry in a moment.";

        //final String processId = String.valueOf(GlobalMethods.generateTransactionId());
        String processId = "INV-" + String.valueOf(GlobalMethods.generateTransactionId()).replace("-", "").substring(0, 12).toUpperCase();

        try {

            final String phoneNumber = utilService.getClaimFromJwt(auth, "phoneNumber");
            final String email = utilService.getClaimFromJwt(auth, "emailAddress");
            var product = productRepo.findByIdAndActiveTrue(Long.valueOf(rq.getProductId()))
                    .orElseThrow(() -> new NotFoundException("Investment product not found"));

            Optional<RegWalletInfo> getRec = regWalletInfoRepository.findByPhoneNumber(phoneNumber);

            if (rq.getPin() == null || rq.getPin().isBlank()) {
                resp.setStatusCode(400);
                resp.setDescription("Pin is required");
                return resp;
            }

            BaseResponse bResPin = new BaseResponse();
            WalletNo wSend = new WalletNo();
            wSend.setPin(rq.getPin());

            wSend.setWalletId(getRec.get().getWalletId());
            bResPin = transactionServiceProxies.validatePin(wSend, auth);
            if (bResPin.getStatusCode() != 200) {
                resp.setStatusCode(bResPin.getStatusCode());
                resp.setDescription(bResPin.getDescription());
                return resp;
            }

            if (rq == null || rq.getCurrencyCode() == null || rq.getCurrencyCode().trim().isEmpty()) {
                resp.setStatusCode(400);
                resp.setDescription("CurrencyCode is required");
                return resp;
            }
            final String currency = rq.getCurrencyCode().trim().toUpperCase(Locale.ROOT);

            if (rq.getAmount() == null || rq.getAmount().trim().isEmpty()
                    || rq.getCurrencyCode() == null || rq.getCurrencyCode().trim().isEmpty()) {
                resp.setStatusCode(400);
                resp.setDescription("Amount is required!");
                return resp;
            }

            ValidateCountryCode rqq = new ValidateCountryCode();
            rqq.setCurrencyCode(currency);
            BaseResponse valCode = profilingProxies.validateCountryCode(rqq, auth);
            if (valCode == null || valCode.getStatusCode() != 200) {
                resp.setStatusCode(400);
                resp.setDescription(valCode != null && valCode.getDescription() != null ? valCode.getDescription() : "validateCountryCode failed");
                return resp;
            }

            ManageFeesConfigReq mFeee = new ManageFeesConfigReq();
            mFeee.setAmount(rq.getAmount());
            mFeee.setTransType("moneymarket");
            mFeee.setCurrencyCode(rq.getCurrencyCode());

            BaseResponse mConfig = utilService.getFeesConfig(mFeee);
            if (mConfig.getStatusCode() != 200) {
                resp.setStatusCode(mConfig.getStatusCode());
                resp.setDescription(mConfig.getDescription());
                return resp;
            }

            // Compute fees (simple example: flat % from config or product meta)
            BigDecimal amount = new BigDecimal(rq.getAmount());
            // ---------- Business validations ----------

            if (amount.compareTo(product.getMinimumInvestmentAmount()) < 0) {
                throw new BusinessException(
                        "Minimum for this investment is " + product.getMinimumInvestmentAmount()
                        + ". Please increase the amount.");
            }


            /*if (grossDebit.compareTo(available) > 0) {
                throw new BusinessException(
                        "You have " + available + " available. Enter an amount ≤ " + available + ".");
            }*/
            BigDecimal units = computeUnits(product, amount);
            // String fees = (String) mConfig.getData().get("fees");
            Object rawFees = mConfig.getData().get("fees");

            BigDecimal fees = null;
            if (rawFees instanceof BigDecimal) {
                fees = (BigDecimal) rawFees;
            } else if (rawFees instanceof Number) {
                fees = BigDecimal.valueOf(((Number) rawFees).doubleValue());
            } else if (rawFees instanceof String) {
                fees = new BigDecimal((String) rawFees);
            } else if (rawFees == null) {
                fees = BigDecimal.ZERO; // or handle as you want
            } else {
                throw new IllegalArgumentException("Unsupported fees type: " + rawFees.getClass());
            }
            // Compute fees (simple example: flat % from config or product meta)
            // BigDecimal fees = computeSubscriptionFees(product, amount);
            BigDecimal bigDecFees = fees;
            BigDecimal grossDebit = amount.add(bigDecFees);
            ProcessDebitWalletForInvestment pRe = new ProcessDebitWalletForInvestment();
            pRe.setCurrencyCode(currency);
            pRe.setEmailAddress(email);
            pRe.setFees(bigDecFees);
            pRe.setGrossDebitAmount(grossDebit);
            pRe.setIdempotencyKey(processId);
            pRe.setPhoneNumber(phoneNumber);
            PreDebitInvestmentResult pre = debitAndAddToInvestmentWallet(pRe, auth, email, phoneNumber, mConfig, processId);
            System.out.println("[debitAndAddToInvestmentWallet response ::::::::::::::: " + pre);

            if (!pre.isSuccess()) {
                resp.setStatusCode(pre.getError().getStatusCode());
                resp.setDescription(pre.getError().getDescription());
                return resp;
            }

            // Credit Investment Wallet
            //CreateInvestmentSubscriptionRequest
            CreateInvestmentSubscriptionRequest cIn
                    = new CreateInvestmentSubscriptionRequest(
                            pre.getWalletId(),
                            Long.valueOf(rq.getProductId()),
                            bigDecFees,
                            units,
                            grossDebit,
                            amount,
                            processId,
                            email,
                            phoneNumber,
                            product,
                            processId
                    );

            InvestmentOrderResponse makeSub = createSubscription(cIn);
            Map mp = new HashMap();
            mp.put("processId", processId);
            mp.put("grossDebit", grossDebit);
            mp.put("amount", amount);
            mp.put("units", units);
            mp.put("emailAddress", email);
            mp.put("productId", rq.getProductId());

            resp.setDescription("Subscription was successful");
            resp.setStatusCode(200);
            resp.setData(mp);

            return resp;
        } catch (Exception ex) {
            log.error("processTrnsaction error", ex);
            resp.setStatusCode(statusCode);
            resp.setDescription(description);
            return resp;
        }
    }

    public InvestmentOrderResponse createSubscription(
            CreateInvestmentSubscriptionRequest rq) {
        Optional<RegWalletInfo> getRec = regWalletInfoRepository.findByPhoneNumber(rq.phoneNumber());

        /*var product = productRepo.findByIdAndActiveTrue(rq.productId())
                .orElseThrow(() -> new NotFoundException("Investment product not found"));*/
        // ---------- Idempotency ----------
        var existing = orderRepo.findByIdempotencyKey(rq.idempotencyKey());
        if (existing.isPresent()) {
            var ord = existing.get();
            // duplicate order message
            throw new BusinessException(
                    "Duplicate order: This order was just submitted. Check Activity for the status.");
        }

        // ---------- Wallet & availableToInvest ----------
        /* var walletSummary = walletClient.getContributionWalletSummary(rq.emailAddress(), rq.walletId());
        var available = walletSummary.availableToInvest();

        if (available.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("You have 0 available. Enter an amount ≤ 0.");
        }*/
        // ---------- Create order (PENDING) ----------
        InvestmentOrder order = new InvestmentOrder();
        order.setOrderRef(rq.orderRef());
        order.setIdempotencyKey(rq.idempotencyKey());
        order.setEmailAddress(rq.emailAddress());
        order.setWalletId(rq.walletId());
        order.setProduct(rq.product());
        order.setType(InvestmentOrderType.SUBSCRIPTION);
        order.setStatus(InvestmentOrderStatus.PENDING);
        order.setAmount(rq.amount());
        order.setAmountBalance(rq.amount());
        order.setUnits(rq.units());
        order.setFees(rq.fees());
        order.setNetAmount(rq.grossDebit()); // user amount; debit( amount + fees )
        order.setCreatedAt(Instant.now());
        order.setUpdatedAt(Instant.now());

        orderRepo.save(order);

        // ---------- Place wallet hold ----------
        try {
            //walletClient.placeInvestmentHold(rq.walletId(), orderRef, grossDebit);
            // order.setStatus(InvestmentOrderStatus.HOLD_PLACED);
            // order.setUpdatedAt(Instant.now());
        } catch (Exception ex) {
            // Do not proceed to partner if hold fails
            order.setStatus(InvestmentOrderStatus.FAILED);
            order.setFailureReason("Failed to place wallet hold: " + ex.getMessage());
            orderRepo.save(order);
            throw new BusinessException("Service is currently unavailable. Please try again shortly.");
        }

        orderRepo.save(order);

        // ---------- Call partner ----------
        /* try {
            var partnerResponse = partnerClient.subscribe(product.getPartnerProductCode(), amount, emailAddress, orderRef);
            order.setStatus(InvestmentOrderStatus.SENT_TO_PARTNER);
            order.setPartnerOrderId(partnerResponse.partnerOrderId());
            order.setUpdatedAt(Instant.now());
            orderRepo.save(order);

            // Depending on partner model, settlement may be:
            //  - synchronous (immediate success)
            //  - asynchronous (callback/webhook)
            if (partnerResponse.settled()) {
                onSubscriptionSettled(orderRef, partnerResponse);
            }

        } catch (Exception ex) {
            // release hold on failure & mark failed
            walletClient.releaseInvestmentHold(rq.walletId(), orderRef);
            order.setStatus(InvestmentOrderStatus.FAILED);
            order.setFailureReason("Partner service error: " + ex.getMessage());
            order.setUpdatedAt(Instant.now());
            orderRepo.save(order);
            throw new BusinessException("Service is currently unavailable. Please try again shortly.");
        }*/
        return new InvestmentOrderResponse(
                rq.orderRef(),
                rq.product().getName(),
                rq.amount(),
                rq.fees(),
                rq.amount().add(rq.fees()),
                order.getStatus()
        );
    }
    public static final ScheduledExecutorService scheduler
            = Executors.newSingleThreadScheduledExecutor();

    /*public static void runOnce(Runnable task, long delayMs) {
        scheduler.schedule(() -> {
            try {
                task.run();
            } finally {
                scheduler.shutdown(); // shuts down after running once
            }
        }, delayMs, TimeUnit.MILLISECONDS);
    }

    public void triggerOnce() {
        this.runOnce(this::onSubscriptionSettled, delayMsSet);
    }*/
    /**
     * Called when partner confirms settlement (sync or webhook). - Debits
     * wallet (confirm hold) - Creates/updates InvestmentPosition - Logs
     * activity
     */
    //@Scheduled(cron = "0 55 23 * * *", zone = "Africa/Lagos")
    @Transactional
    @Scheduled(cron = "${fx.investment.run.approve.subscription.cron}")
    public void onSubscriptionSettled() {
        System.out.println(" ****** Investment Checking and Processing OnSubscription to ACTIVATE  >>>>>>>>>>>>>   *********** ");

        List<InvestmentOrder> getOrderr = orderRepo.findAllByStatus(InvestmentOrderStatus.PENDING);

        for (InvestmentOrder getRec : getOrderr) {
            // Confirm wallet debit for amount + fees
            BigDecimal grossDebit = getRec.getAmount().add(getRec.getFees());
            //walletClient.confirmInvestmentDebit(order.getWalletId(), orderRef);

            // Create or update position
            var existingPositionOpt = positionRepo.findActiveByEmailAddressAndProduct(getRec.getEmailAddress(), getRec.getProduct().getId());

            InvestmentPosition position = existingPositionOpt.orElseGet(() -> {
                InvestmentPosition p = new InvestmentPosition();
                p.setEmailAddress(getRec.getEmailAddress());
                p.setWalletId(getRec.getWalletId());
                p.setProduct(getRec.getProduct());
                p.setUnits(BigDecimal.ZERO);
                p.setInvestedAmount(BigDecimal.ZERO);
                p.setCurrentValue(BigDecimal.ZERO);
                p.setAccruedInterest(BigDecimal.ZERO);
                p.setStatus(InvestmentPositionStatus.ACTIVE);
                p.setCreatedAt(Instant.now());
                p.setUpdatedAt(Instant.now());
                p.setOrderRef(getOrderr.get(0).getOrderRef());
                return p;
            });

            position.setUnits(getRec.getUnits());
            position.setInvestedAmount(getRec.getAmount());
            position.setCurrentValue(getRec.getAmount());

            position.setUpdatedAt(Instant.now());
            position.setAccruedInterest(getRec.getAmountBalance());

            Instant subscribedAt = Instant.now();
            Instant settlementAt = TimeUnitMinutes.computeSettlementAt(getRec.getProduct(), subscribedAt);
            Instant maturityAt = TimeUnitMinutes.computeMaturityAt(getRec.getProduct(), settlementAt);

            position.setSettlementAt(settlementAt);
            position.setEmailAddress(getRec.getEmailAddress());
            position.setMaturityAt(maturityAt);
            position.setStatus(InvestmentPositionStatus.ACTIVE);
            position.setOrderRef(getOrderr.get(0).getOrderRef());
            positionRepo.save(position);

            getRec.setPosition(position);
            getRec.setStatus(InvestmentOrderStatus.ACTIVE);
            getRec.setUpdatedAt(Instant.now());
            orderRepo.save(getRec);

            // ✅ create day-0 history snapshot
            investmentHistoryService.createInitialHistory(position,getRec.getEmailAddress(),getRec.getProduct().getMaturityAt(),getRec.getOrderRef());

            // Activity + notification
            activityService.logInvestmentSubscription(getRec, position, grossDebit);

            // optional: emit event for realtime UI updates
        }

    }

    private BigDecimal computeSubscriptionFees(InvestmentProduct product, BigDecimal amount) {
        // read config from product.getMetaJson() or app config
        // For example: 1.5% subscription fee
        BigDecimal feeRate = new BigDecimal("0.015");
        return amount.multiply(feeRate).setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal computeUnits(InvestmentProduct product, BigDecimal amount) {
        if (product.getUnitPrice() == null || BigDecimal.ZERO.compareTo(product.getUnitPrice()) == 0) {
            // Money market case – units = 1, amount drives value
            return BigDecimal.ONE;
        }
        return amount.divide(product.getUnitPrice(), 8, RoundingMode.HALF_UP);
    }

    private BigDecimal computeLiquidationFees(InvestmentProduct product, BigDecimal liquidationAmount) {
        // Example rule:
        // - Money market: 0.5%
        // - Mutual fund: 1.0%
        // - Bond: 0.3%
        BigDecimal rate;

        switch (product.getType()) {
            case MONEY_MARKET ->
                rate = new BigDecimal("0.005"); // 0.5%
            case MUTUAL_FUND ->
                rate = new BigDecimal("0.010");  // 1.0%
            case BOND ->
                rate = new BigDecimal("0.003");         // 0.3%
            default ->
                rate = new BigDecimal("0.0075");          // 0.75% fallback
        }

        return liquidationAmount
                .multiply(rate)
                .setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal computeTax(InvestmentProduct product, BigDecimal liquidationAmount) {
        // Example rule:
        // - Only apply withholding tax on interest-bearing products
        // - Assume 10% of gain is taxed (not of total capital)
        BigDecimal taxRateOnGain = new BigDecimal("0.10"); // 10%

        // If product says no tax, return zero
        if (product.getType() == InvestmentType.MONEY_MARKET || product.getType() == InvestmentType.BOND) {
            // You need gain to compute tax properly; if not available here,
            // tax can be computed later at settlement using actual gain.
            // For now: 0 tax pre-confirmation.
            return BigDecimal.ZERO;
        }
        return BigDecimal.ZERO;
    }

    public BaseResponse requestLiquidation(
            LiquidateInvestmentRequest rq, String auth) {
        BaseResponse res = new BaseResponse();
        try {
            String emailAddress = utilService.getClaimFromJwt(auth, "emailAddress");
            var order = orderRepo.findByOrderRefAndEmailAddress(rq.orderId(), emailAddress)
                    .orElseThrow(() -> new NotFoundException("Investment position not found"));

            if (order.getStatus() != InvestmentOrderStatus.ACTIVE // && position.getStatus() != InvestmentPositionStatus.PARTIALLY_LIQUIDATED
                    ) {
                throw new BusinessException("Order is not eligible for liquidation.");
            }

            // Idempotency
            /*orderRepo.findByOrderRef(rq.orderId()).ifPresent(o -> {
            throw new BusinessException(
                    "This order was just submitted. Check Activity for the status.");
        });*/
            Optional<InvestmentPosition> position = positionRepo.findByOrderRef(order.getOrderRef());

            /*BigDecimal currentValue = position.getCurrentValue();
        BigDecimal liquidationAmount = Optional.ofNullable(rq.liquidationAmount()).orElse(currentValue);*/
            BigDecimal liquidationAmount = order.getAmountBalance();
            if (liquidationAmount.compareTo(position.get().getCurrentValue()) > 0) {
                throw new BusinessException("Liquidation amount cannot exceed current position value.");
            }

            if (liquidationAmount.compareTo(BigDecimal.ZERO) <= 0) {
                throw new BusinessException("Liquidation amount must be greater than 0.");
            }

            // Fees + tax at execution
            BigDecimal fees = computeLiquidationFees(position.get().getProduct(), liquidationAmount);
            BigDecimal tax = computeTax(position.get().getProduct(), liquidationAmount);
            BigDecimal netAmount = liquidationAmount.subtract(fees).subtract(tax);

            if (netAmount.compareTo(BigDecimal.ZERO) <= 0) {
                throw new BusinessException("Net amount after fees and tax is not positive.");
            }

            //String orderRef = "LIQ-" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();
            InvestmentOrder orderUp = orderRepo.findByOrderRefDataUpdate(emailAddress);
            // order.setOrderRef(orderRef);
            //order.setIdempotencyKey(rq.idempotencyKey());
            // order.setEmailAddress(emailAddress);
            // order.setWalletId(position.getWalletId());
            //order.setProduct(position.getProduct());
            orderUp.setPosition(position.get());
            orderUp.setType(InvestmentOrderType.LIQUIDATION);
            //orderUp.setStatus(InvestmentOrderStatus.SENT_TO_PARTNER);
            orderUp.setFees(fees);
            orderUp.setTax(tax);
            orderUp.setNetAmount(netAmount);
            orderUp.setAmountBalance(liquidationAmount.subtract(netAmount));
            orderUp.setCreatedAt(Instant.now());
            orderUp.setUpdatedAt(Instant.now());
            orderRepo.save(orderUp);

            // Call partner for liquidation (no wallet hold needed; credit occurs on settlement):
            /*var partnerResponse = partnerClient.liquidate(
                    position.getProduct().getPartnerProductCode(),
                    liquidationAmount,
                    userId,
                    orderRef
            );*/
 /*order.setStatus(InvestmentOrderStatus.SENT_TO_PARTNER);
            order.setPartnerOrderId("");
            order.setUpdatedAt(Instant.now());
            orderRepo.save(order);*/
            onLiquidationSettled(rq.orderId());

            /* if (partnerResponse.settled()) {
                onLiquidationSettled(orderRef, partnerResponse);
            }*/
 /*return new InvestmentOrderResponse(
                    rq.orderId(),
                    position.get().getProduct().getName(),
                    liquidationAmount,
                    fees,
                    netAmount,
                    order.getStatus()
            );*/
            Map mp = new HashMap();
            mp.put("orderId", rq.orderId());
            mp.put("productName", position.get().getProduct().getName());
            mp.put("liquidationAmount", liquidationAmount);
            mp.put("fees", fees);
            mp.put("netAmount", netAmount);
            mp.put("status", order.getStatus());
            res.setDescription("Liquidation was Successful.");
            res.setStatusCode(200);

        } catch (Exception ex) {
            //order.setStatus(InvestmentOrderStatus.FAILED);
            //order.setFailureReason("Partner liquidation error: " + ex.getMessage());
            //order.setUpdatedAt(Instant.now());
            //orderRepo.save(order);
            throw new BusinessException("We couldn’t process your request. Please try again shortly.");
        }

        return res;
    }

    public BaseResponse onLiquidationSettled(String orderRef) {
        BaseResponse res = new BaseResponse();
        var order = orderRepo.findByOrderRef(orderRef)
                .orElseThrow(() -> new NotFoundException("Liquidation order not found"));

        if (order.getStatus() == InvestmentOrderStatus.SETTLED) {
            res.setStatusCode(400);
            return res; // already processed
        }

        var position = order.getPosition();
        if (position == null) {
            throw new IllegalStateException("Liquidation order has no linked position.");
        }

        BigDecimal liquidationAmount = order.getNetAmount();       // amount cashed out
        BigDecimal currentValue = position.getCurrentValue();    // full position value before liquidation
        BigDecimal units = position.getUnits();                  // full units before liquidation

        // ===============  FULL LIQUIDATION ==================
        if (liquidationAmount.compareTo(currentValue) >= 0) {
            BaseResponse procCredit = processCredit(orderRef, order.getEmailAddress());

            if (procCredit.getStatusCode() != 200) {
                res.setStatusCode(400);
                res.setDescription(procCredit.getDescription());
                return res;
            }

            position.setUnits(BigDecimal.ZERO);
            position.setCurrentValue(BigDecimal.ZERO);
            position.setStatus(InvestmentPositionStatus.FULLY_LIQUIDATED);
            position.setUpdatedAt(Instant.now());
            positionRepo.save(position);

            // Credit wallet
            /* walletClient.creditInvestmentLiquidation(
                    order.getWalletId(),
                    order.getOrderRef(),
                    order.getNetAmount()
            );*/
            order.setAmountBalance(BigDecimal.ZERO);
            order.setStatus(InvestmentOrderStatus.SETTLED);
            //order.setStatus(InvestmentOrderStatus.SETTLED);
            order.setUpdatedAt(Instant.now());
            orderRepo.save(order);

            activityService.logInvestmentLiquidation(order, position);

        }

        // ===============  PARTIAL LIQUIDATION ==================
        // 1. remaining portfolio value
        BigDecimal remainingValue = currentValue.subtract(liquidationAmount);

        // 2. proportional units to keep (units × remainingValue/currentValue)
        BigDecimal remainingUnits = units.multiply(
                remainingValue.divide(currentValue, 8, RoundingMode.HALF_UP)
        );

        // update position
        position.setUnits(remainingUnits);
        position.setCurrentValue(remainingValue);
        position.setStatus(InvestmentPositionStatus.PARTIALLY_LIQUIDATED);
        position.setUpdatedAt(Instant.now());
        positionRepo.save(position);

        BaseResponse procCredit = processCredit(orderRef, order.getEmailAddress());

        if (procCredit.getStatusCode() != 200) {
            res.setStatusCode(400);
            res.setDescription(procCredit.getDescription());
            return res;
        }

        // credit wallet
        /*walletClient.creditInvestmentLiquidation(
                order.getWalletId(),
                order.getOrderRef(),
                order.getNetAmount()
        );*/
        // update order
        order.setAmountBalance(order.getAmountBalance().subtract(liquidationAmount));
        //order.setStatus(InvestmentOrderStatus.SETTLED);
        order.setUpdatedAt(Instant.now());
        orderRepo.save(order);

        activityService.logInvestmentLiquidation(order, position);
        res.setStatusCode(200);
        res.setDescription("Sucess");
        return res;
    }

    public BaseResponse processCredit(String orderId, String emailAddress) {
        int statusCode = 500;
        final BaseResponse res = new BaseResponse();

        try {
            statusCode = 400;

            CreditWalletCaller rqC = new CreditWalletCaller();
            var order = orderRepo.findByOrderRefAndEmailAddress(orderId, emailAddress)
                    .orElseThrow(() -> new NotFoundException("Investment position not found"));

            if (order.getProduct().getCurrency().equals("CAD")) {
                Optional<RegWalletInfo> getRec = regWalletInfoRepository.findByEmail(emailAddress);

                rqC.setPhoneNumber(getRec.get().getPhoneNumber());

            } else {
                List<AddAccountDetails> getSellerAcct = addAccountDetailsRepo.findByEmailAddressrData(emailAddress);
                rqC.setPhoneNumber(getSellerAcct.get(0).getAccountNumber());
                Optional<RegWalletInfo> getRec = regWalletInfoRepository.findByEmail(emailAddress);

            }

            rqC.setAuth("Liquidation");
            rqC.setFees("00");
            rqC.setFinalCHarges(order.getNetAmount().toString());
            rqC.setNarration(order.getProduct().getCurrency() + "_Reversal");
            rqC.setTransAmount(order.getNetAmount().toString());
            rqC.setTransactionId(orderId);

            System.out.println(" creditSellerAcct for Reversal REQ  ::::::::::::::::  %S  " + new Gson().toJson(rqC));

            BaseResponse creditSellerAcct = transactionServiceProxies.creditCustomerWithType(rqC, "CUSTOMER", "");

            System.out.println(" creditSellerAcct Response from core ::::::::::::::::  %S  " + new Gson().toJson(creditSellerAcct));
            if (creditSellerAcct.getStatusCode() == 200) {

                // Credit BAAS NGN_GL
                CreditWalletCaller GLCredit = new CreditWalletCaller();
                GLCredit.setAuth("Receiver");
                GLCredit.setFees("0.00");
                GLCredit.setFinalCHarges(rqC.getFinalCHarges());
                GLCredit.setNarration(rqC.getNarration());
                String GGL_ACCOUNT = null;

                List<AppConfig> getAppConf = appConfigRepo.findByConfigName(order.getProduct().getCurrency());

                for (AppConfig getConfDe : getAppConf) {
                    if (getConfDe.getConfigName().equals(order.getProduct().getCurrency())) {
                        GGL_ACCOUNT = getConfDe.getConfigValue();

                    }
                }
                //GLCredit.setPhoneNumber(utilService.decryptData(utilService.getSETTING_KEY_WALLET_SYSTEM_SYSTEM_GG_CAD()));
                GLCredit.setPhoneNumber(utilService.decryptData(GGL_ACCOUNT));
                GLCredit.setTransAmount(rqC.getFinalCHarges());
                GLCredit.setTransactionId(rqC.getTransactionId());

                System.out.println(" creditAcct_GL seller for Reversal REQ  ::::::::::::::::  %S  " + new Gson().toJson(GLCredit));

                BaseResponse creditAcct_GL = transactionServiceProxies.creditCustomerWithType(GLCredit, order.getProduct().getCurrency(), "");

                System.out.println(" credit GL for seller for Reversal Response from core ::::::::::::::::  %S  " + new Gson().toJson(creditAcct_GL));

            }

            res.setStatusCode(200);

        } catch (Exception ex) {
            ex.printStackTrace();
            res.setStatusCode(statusCode);
        }

        return res;

    }

}
