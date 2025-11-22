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
import com.finacial.wealth.api.fxpeer.exchange.investment.record.LiquidateInvestmentRequest;
import com.finacial.wealth.api.fxpeer.exchange.investment.record.PartnerSubscriptionResponse;
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
import com.google.gson.Gson;
import static jakarta.persistence.GenerationType.UUID;
import jakarta.transaction.Transactional;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 *
 * @author olufemioshin
 */
@Service
@Transactional
public class InvestmentOrderService {

    private static final Logger log = LoggerFactory.getLogger(InvestmentOrderService.class);

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
            InvestmentHistoryService investmentHistoryService) {
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

    private PreDebitResult debitAndAddToInvestmentWallet(
            ProcessDebitWalletForInvestment rq,
            String auth,
            String email,
            String phoneNumber,
            BaseResponse feeConfig,
            String processId
    ) {
        PreDebitResult out = new PreDebitResult();
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
            BigDecimal finCharges = receiveAmount.add(fees);
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
            debitBuyer.setTransAmount(finCharges.toString());
            debitBuyer.setTransactionId(processId);

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

    public ApiResponseModel createSubscriptionCaller(CreateSubscriptionReq rq, String auth) throws IOException {
        ApiResponseModel resp = new ApiResponseModel();
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
                resp.setDescription("currencyCode is required");
                return resp;
            }
            final String currency = rq.getCurrencyCode().trim().toUpperCase(Locale.ROOT);

            if (rq.getAmount() == null || rq.getAmount().trim().isEmpty()
                    || rq.getCurrencyCode() == null || rq.getCurrencyCode().trim().isEmpty()) {
                resp.setStatusCode(400);
                resp.setDescription("operator, product, recipient and amount are required");
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
            String fees = (String) mConfig.getData().get("fees");
            // Compute fees (simple example: flat % from config or product meta)
            // BigDecimal fees = computeSubscriptionFees(product, amount);
            BigDecimal bigDecFees = new BigDecimal(fees);
            BigDecimal grossDebit = amount.add(bigDecFees);
            ProcessDebitWalletForInvestment pRe = new ProcessDebitWalletForInvestment();
            pRe.setCurrencyCode(currency);
            pRe.setEmailAddress(email);
            pRe.setFees(bigDecFees);
            pRe.setGrossDebitAmount(grossDebit);
            pRe.setIdempotencyKey(processId);
            pRe.setPhoneNumber(phoneNumber);
            PreDebitResult pre = debitAndAddToInvestmentWallet(pRe, auth, email, phoneNumber, mConfig, processId);
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

    /**
     * Called when partner confirms settlement (sync or webhook). - Debits
     * wallet (confirm hold) - Creates/updates InvestmentPosition - Logs
     * activity
     */
    public void onSubscriptionSettled(String orderRef, PartnerSubscriptionResponse partnerResponse) {
        var order = orderRepo.findByOrderRef(orderRef)
                .orElseThrow(() -> new NotFoundException("Investment order not found"));

        if (order.getStatus() == InvestmentOrderStatus.SETTLED) {
            return; // already processed
        }

        // Confirm wallet debit for amount + fees
        BigDecimal grossDebit = order.getAmount().add(order.getFees());
        //walletClient.confirmInvestmentDebit(order.getWalletId(), orderRef);

        // Create or update position
        var existingPositionOpt = positionRepo.findActiveByEmailAddressAndProduct(order.getEmailAddress(), order.getProduct().getId());

        InvestmentPosition position = existingPositionOpt.orElseGet(() -> {
            InvestmentPosition p = new InvestmentPosition();
            p.setEmailAddress(order.getEmailAddress());
            p.setWalletId(order.getWalletId());
            p.setProduct(order.getProduct());
            p.setUnits(BigDecimal.ZERO);
            p.setInvestedAmount(BigDecimal.ZERO);
            p.setCurrentValue(BigDecimal.ZERO);
            p.setAccruedInterest(BigDecimal.ZERO);
            p.setStatus(InvestmentPositionStatus.ACTIVE);
            p.setCreatedAt(Instant.now());
            p.setUpdatedAt(Instant.now());
            return p;
        });

        position.setUnits(position.getUnits().add(order.getUnits()));
        position.setInvestedAmount(position.getInvestedAmount().add(order.getAmount()));
        position.setCurrentValue(position.getCurrentValue().add(order.getAmount()));
        position.setUpdatedAt(Instant.now());
        positionRepo.save(position);

        order.setPosition(position);
        order.setStatus(InvestmentOrderStatus.ACTIVE);
        order.setUpdatedAt(Instant.now());
        orderRepo.save(order);

        // ✅ create day-0 history snapshot
        investmentHistoryService.createInitialHistory(position);

        // Activity + notification
        activityService.logInvestmentSubscription(order, position, grossDebit);

        // optional: emit event for realtime UI updates
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

    public InvestmentOrderResponse requestLiquidation(String emailAddress,
            LiquidateInvestmentRequest rq) {
        var position = positionRepo.findByIdAndEmailAddress(rq.positionId(), emailAddress)
                .orElseThrow(() -> new NotFoundException("Investment position not found"));

        if (position.getStatus() != InvestmentPositionStatus.ACTIVE
                && position.getStatus() != InvestmentPositionStatus.PARTIALLY_LIQUIDATED) {
            throw new BusinessException("Position is not eligible for liquidation.");
        }

        // Idempotency
        orderRepo.findByOrderRef(rq.orderId()).ifPresent(o -> {
            throw new BusinessException(
                    "This order was just submitted. Check Activity for the status.");
        });

        BigDecimal currentValue = position.getCurrentValue();
        BigDecimal liquidationAmount = Optional.ofNullable(rq.liquidationAmount()).orElse(currentValue);

        if (liquidationAmount.compareTo(currentValue) > 0) {
            throw new BusinessException("Liquidation amount cannot exceed current position value.");
        }

        if (liquidationAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("Liquidation amount must be greater than 0.");
        }

        // Fees + tax at execution
        BigDecimal fees = computeLiquidationFees(position.getProduct(), liquidationAmount);
        BigDecimal tax = computeTax(position.getProduct(), liquidationAmount);
        BigDecimal netAmount = liquidationAmount.subtract(fees).subtract(tax);

        if (netAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("Net amount after fees and tax is not positive.");
        }

        //String orderRef = "LIQ-" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();
        InvestmentOrder order = orderRepo.findByOrderRefDataUpdate(emailAddress);
        // order.setOrderRef(orderRef);
        //order.setIdempotencyKey(rq.idempotencyKey());
        // order.setEmailAddress(emailAddress);
        // order.setWalletId(position.getWalletId());
        //order.setProduct(position.getProduct());
        order.setPosition(position);
        order.setType(InvestmentOrderType.LIQUIDATION);
        order.setStatus(InvestmentOrderStatus.PENDING);
        order.setAmount(liquidationAmount);
        order.setFees(fees);
        order.setTax(tax);
        order.setNetAmount(netAmount);
        order.setCreatedAt(Instant.now());
        order.setUpdatedAt(Instant.now());
        orderRepo.save(order);

        // Call partner for liquidation (no wallet hold needed; credit occurs on settlement):
        try {
            /*var partnerResponse = partnerClient.liquidate(
                    position.getProduct().getPartnerProductCode(),
                    liquidationAmount,
                    userId,
                    orderRef
            );*/
            order.setStatus(InvestmentOrderStatus.SENT_TO_PARTNER);
            order.setPartnerOrderId("");
            order.setUpdatedAt(Instant.now());
            orderRepo.save(order);

            onLiquidationSettled(rq.orderId());

            /* if (partnerResponse.settled()) {
                onLiquidationSettled(orderRef, partnerResponse);
            }*/
        } catch (Exception ex) {
            order.setStatus(InvestmentOrderStatus.FAILED);
            order.setFailureReason("Partner liquidation error: " + ex.getMessage());
            order.setUpdatedAt(Instant.now());
            orderRepo.save(order);
            throw new BusinessException("We couldn’t process your request. Please try again shortly.");
        }

        return new InvestmentOrderResponse(
                rq.orderId(),
                position.getProduct().getName(),
                liquidationAmount,
                fees,
                netAmount,
                order.getStatus()
        );
    }

    public void onLiquidationSettled(String orderRef) {
        var order = orderRepo.findByOrderRef(orderRef)
                .orElseThrow(() -> new NotFoundException("Liquidation order not found"));

        if (order.getStatus() == InvestmentOrderStatus.SETTLED) {
            return; // already processed
        }

        var position = order.getPosition();
        if (position == null) {
            throw new IllegalStateException("Liquidation order has no linked position.");
        }

        BigDecimal liquidationAmount = order.getAmount();       // amount cashed out
        BigDecimal currentValue = position.getCurrentValue();    // full position value before liquidation
        BigDecimal units = position.getUnits();                  // full units before liquidation

        // ===============  FULL LIQUIDATION ==================
        if (liquidationAmount.compareTo(currentValue) >= 0) {
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
            //order.setStatus(InvestmentOrderStatus.SETTLED);
            order.setUpdatedAt(Instant.now());
            orderRepo.save(order);

            activityService.logInvestmentLiquidation(order, position);
            return;
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
    }

}
