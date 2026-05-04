/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.finacial.wealth.api.fxpeer.exchange.order;

/**
 *
 * @author olufemioshin
 */
import com.fasterxml.jackson.databind.ObjectMapper;
import com.finacial.wealth.api.fxpeer.exchange.common.BusinessException;
import com.finacial.wealth.api.fxpeer.exchange.common.NotFoundException;
import com.finacial.wealth.api.fxpeer.exchange.common.OfferStatus;
import com.finacial.wealth.api.fxpeer.exchange.domain.AddAccountDetails;
import com.finacial.wealth.api.fxpeer.exchange.domain.AddAccountDetailsRepo;
import com.finacial.wealth.api.fxpeer.exchange.domain.AppConfig;
import com.finacial.wealth.api.fxpeer.exchange.domain.AppConfigRepo;
import com.finacial.wealth.api.fxpeer.exchange.domain.FinWealthPaymentTransaction;
import com.finacial.wealth.api.fxpeer.exchange.domain.PeerToPeerFxReferral;
import com.finacial.wealth.api.fxpeer.exchange.domain.RegWalletInfo;
import com.finacial.wealth.api.fxpeer.exchange.domain.RegWalletInfoRepository;
import com.finacial.wealth.api.fxpeer.exchange.feign.ProfilingProxies;
import com.finacial.wealth.api.fxpeer.exchange.feign.TransactionServiceProxies;
import com.finacial.wealth.api.fxpeer.exchange.investment.service.TransactionHistoryClientLocalT;
import com.finacial.wealth.api.fxpeer.exchange.fx.p.p.wallet.FinWealthPaymentTransactionRepo;
import com.finacial.wealth.api.fxpeer.exchange.model.AddAccountObj;
import com.finacial.wealth.api.fxpeer.exchange.model.ApiResponseModel;
import com.finacial.wealth.api.fxpeer.exchange.model.BaseResponse;
import com.finacial.wealth.api.fxpeer.exchange.model.CreditWalletCaller;
import com.finacial.wealth.api.fxpeer.exchange.model.DebitWalletCaller;
import com.finacial.wealth.api.fxpeer.exchange.model.ManageFeesConfigReq;
import com.finacial.wealth.api.fxpeer.exchange.model.MarketReadinessRequest;
import com.finacial.wealth.api.fxpeer.exchange.model.WalletNo;
import com.finacial.wealth.api.fxpeer.exchange.offer.CreateOfferCaller;
import com.finacial.wealth.api.fxpeer.exchange.offer.Offer;
import com.finacial.wealth.api.fxpeer.exchange.offer.OfferRepository;
import com.finacial.wealth.api.fxpeer.exchange.repo.PeerToPeerFxReferralRepo;
import com.finacial.wealth.api.fxpeer.exchange.util.GlobalMethods;
import com.finacial.wealth.api.fxpeer.exchange.util.UttilityMethods;
import com.finacial.wealth.api.fxpeer.exchange.fx.p.p.wallet.WalletIndivTransactionsDetails;
import com.finacial.wealth.api.fxpeer.exchange.fx.p.p.wallet.WalletIndivTransactionsDetailsPojo;
import com.finacial.wealth.api.fxpeer.exchange.fx.p.p.wallet.WalletIndivTransactionsDetailsRepo;
import com.finacial.wealth.api.fxpeer.exchange.fx.p.p.wallet.WalletTransactionsDetails;
import com.finacial.wealth.api.fxpeer.exchange.fx.p.p.wallet.WalletTransactionsDetailsRepo;

import com.google.gson.Gson;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@Service
public class OrderService {

    private final OrderRepository orders;
    private final OfferRepository offers;
    private final TransactionServiceProxies transactionServiceProxies;
    private final UttilityMethods utilService;
    private final RegWalletInfoRepository regWalletInfoRepository;
    private final AddAccountDetailsRepo addAccountDetailsRepo;
    private final ProfilingProxies profilingProxies;
    private final Logger logger = LoggerFactory.getLogger(OrderService.class);
    private static final int STATUS_CODE_NIGERIA_ONBOARDING_FLOW_CODE = 58;
    private static final String STATUS_CODE_NIGERIA_ONBOARDING_FLOW_DESCRIPTION = "Please validate bvn";
    private final WalletTransactionsDetailsRepo walletTransactionsDetailsRepo;
    private final WalletIndivTransactionsDetailsRepo walletIndivTransactionsDetailsRepo;
    private final AppConfigRepo appConfigRepo;
    private final PeerToPeerFxReferralRepo peerToPeerFxReferralRepo;
    private final ObjectMapper mapper;
    private final FinWealthPaymentTransactionRepo finWealthPaymentTransactionRepo;
    private final TransactionHistoryClientLocalT transactionHistoryClientLocalT;

    public OrderService(FinWealthPaymentTransactionRepo finWealthPaymentTransactionRepo, OrderRepository orders, OfferRepository offers,
            TransactionServiceProxies transactionServiceProxies,
            UttilityMethods utilService, RegWalletInfoRepository regWalletInfoRepository,
            AddAccountDetailsRepo addAccountDetailsRepo,
            ProfilingProxies profilingProxies, WalletTransactionsDetailsRepo walletTransactionsDetailsRepo,
            WalletIndivTransactionsDetailsRepo walletIndivTransactionsDetailsRepo,
            AppConfigRepo appConfigRepo, PeerToPeerFxReferralRepo peerToPeerFxReferralRepo,
            ObjectMapper mapper, TransactionHistoryClientLocalT transactionHistoryClientLocalT) {
        this.finWealthPaymentTransactionRepo = finWealthPaymentTransactionRepo;
        this.orders = orders;
        this.offers = offers;
        this.transactionServiceProxies = transactionServiceProxies;
        this.utilService = utilService;
        this.regWalletInfoRepository = regWalletInfoRepository;
        this.addAccountDetailsRepo = addAccountDetailsRepo;
        this.profilingProxies = profilingProxies;
        this.walletTransactionsDetailsRepo = walletTransactionsDetailsRepo;
        this.walletIndivTransactionsDetailsRepo = walletIndivTransactionsDetailsRepo;
        this.appConfigRepo = appConfigRepo;
        this.peerToPeerFxReferralRepo = peerToPeerFxReferralRepo;
        this.mapper = mapper;
        this.transactionHistoryClientLocalT = transactionHistoryClientLocalT;
    }

    private ResponseEntity<ApiResponseModel> bad(ApiResponseModel res, String msg, int statusCode) {
        res.setStatusCode(statusCode);
        res.setDescription(msg);

        return new ResponseEntity<>(res, HttpStatus.OK);

        // return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(res);
    }

    private String ensureAccountNumberForMarket(String auth, RegWalletInfo walletInfo, AddAccountDetails accountDetails,
            String requiredCurrencyCode, String fallbackPhoneNumber, String correlationId, ApiResponseModel res) {
        BaseResponse readinessResponse = tryEnsureMarketReady(walletInfo, accountDetails, requiredCurrencyCode,
                fallbackPhoneNumber, correlationId);
        if (readinessResponse != null && readinessResponse.getStatusCode() == 200) {
            Object active = readinessResponse.getData().get("active");
            Object readyAccountNumber = readinessResponse.getData().get("accountNumber");
            if (isTruthy(active) && readyAccountNumber != null) {
                return String.valueOf(readyAccountNumber);
            }
        }

        AddAccountObj fallbackRequest = new AddAccountObj();
        fallbackRequest.setCountry(accountDetails.getCountryName());
        fallbackRequest.setCountryCode(accountDetails.getCountryCode());
        fallbackRequest.setWalletId(walletInfo.getWalletId());
        BaseResponse fallbackResponse = profilingProxies.addOtherAccount(fallbackRequest, auth);
        if (fallbackResponse.getStatusCode() != 200) {
            res.setStatusCode(fallbackResponse.getStatusCode());
            res.setDescription(fallbackResponse.getDescription());
            return null;
        }
        Object accountNumber = fallbackResponse.getData().get("accountNumber");
        return accountNumber == null ? null : String.valueOf(accountNumber);
    }

    private BaseResponse tryEnsureMarketReady(RegWalletInfo walletInfo, AddAccountDetails accountDetails,
            String requiredCurrencyCode, String fallbackPhoneNumber, String correlationId) {
        String marketCode = resolveMarketCode(accountDetails.getCountryCode(), requiredCurrencyCode);
        if (marketCode == null) {
            return null;
        }
        MarketReadinessRequest request = new MarketReadinessRequest();
        request.setCustomerId(walletInfo.getCustomerId() != null && !walletInfo.getCustomerId().trim().isEmpty()
                ? walletInfo.getCustomerId() : walletInfo.getWalletId());
        request.setEmailAddress(walletInfo.getEmail());
        request.setPhoneNumber(fallbackPhoneNumber != null ? fallbackPhoneNumber : walletInfo.getPhoneNumber());
        request.setMarketCode(marketCode);
        request.setCountryCode(accountDetails.getCountryCode());
        request.setCurrencyCode(requiredCurrencyCode);
        request.setTriggerSource("FXPEER");
        request.setProductType("OFFER");
        request.setProductReference(correlationId);
        request.setInitiatingService("fxpeer");
        request.setCorrelationId(correlationId);
        try {
            return profilingProxies.ensureMarketReady(request);
        } catch (Exception ex) {
            logger.warn("ensureMarketReady fallback triggered for marketCode={} currencyCode={} countryCode={}",
                    marketCode, requiredCurrencyCode, accountDetails.getCountryCode(), ex);
            return null;
        }
    }

    private String resolveMarketCode(String countryCode, String currencyCode) {
        if (countryCode == null || currencyCode == null) {
            return null;
        }
        String normalizedCountry = countryCode.trim().toUpperCase();
        String normalizedCurrency = currencyCode.trim().toUpperCase();
        if ("NG".equals(normalizedCountry) && "NGN".equals(normalizedCurrency)) {
            return "NG_RETAIL";
        }
        if ("CA".equals(normalizedCountry) && "CAD".equals(normalizedCurrency)) {
            return "CA_RETAIL";
        }
        return null;
    }

    private boolean isTruthy(Object value) {
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        return value != null && "true".equalsIgnoreCase(String.valueOf(value));
    }

    private void publishCanonicalHistory(FinWealthPaymentTransaction source,
            String sender, String receiver, String walletNo, String senderName, String receiverName) {
        FinWealthPaymentTransaction history = new FinWealthPaymentTransaction();
        history.setAmmount(source.getAmmount());
        history.setCreatedDate(source.getCreatedDate() != null ? source.getCreatedDate() : Instant.now());
        history.setFees(source.getFees());
        history.setPaymentType(source.getPaymentType());
        history.setReceiver(receiver);
        history.setSender(sender);
        history.setTransactionId(source.getTransactionId());
        history.setSenderTransactionType(source.getSenderTransactionType());
        history.setReceiverTransactionType(source.getReceiverTransactionType());
        history.setReceiverBankName(source.getReceiverBankName());
        history.setWalletNo(walletNo);
        history.setReceiverName(receiverName);
        history.setSenderName(senderName);
        history.setSentAmount(source.getSentAmount());
        history.setTheNarration(source.getTheNarration());
        history.setCurrencyCode(source.getCurrencyCode());
        transactionHistoryClientLocalT.publishFromTxn(history);
    }

    public BaseResponse validateAmountInRange(String amountStr,
            BigDecimal minAmount,
            BigDecimal maxAmount) {
        BaseResponse bRes = new BaseResponse();

        if (amountStr == null || amountStr.trim().isEmpty()) {
            bRes.setStatusCode(400);
            bRes.setDescription("amount is required");
            // throw new IllegalArgumentException("amount is required");
        }

        //final 
        BigDecimal amount = BigDecimal.ZERO;
        try {
            amount = new BigDecimal(amountStr.trim())
                    .setScale(2, RoundingMode.HALF_UP); // optional: enforce 2dp for currency
        } catch (NumberFormatException nfe) {
            bRes.setStatusCode(400);
            bRes.setDescription("amount must be a valid number");
            return bRes;

            //throw new IllegalArgumentException("amount must be a valid number", nfe);
        }

        if (minAmount != null && amount.compareTo(minAmount) < 0) {
            // throw new IllegalArgumentException("amount must be >= " + minAmount);
            bRes.setStatusCode(400);
            bRes.setDescription("amount must be equal or more than minimum to sell: " + minAmount);
            return bRes;

        }
        if (maxAmount != null && amount.compareTo(maxAmount) > 0) {
            bRes.setStatusCode(400);
            bRes.setDescription("amount must be less or equal to maximum to sell: " + maxAmount);
            return bRes;

            // throw new IllegalArgumentException("amount must be <= " + maxAmount);
        }

        bRes.setStatusCode(200);

        return bRes;
    }

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    /**
     * Map entity → DTO without reusing the same DTO instance. Null-safe.
     */
    private WalletIndivTransactionsDetailsPojo toPojoSafely(WalletIndivTransactionsDetails e) {
        WalletIndivTransactionsDetailsPojo p = new WalletIndivTransactionsDetailsPojo();
        // map fields; NEVER do getList.get(0) inside a loop
        p.setAccountNumber(e.getAccountNumber());
        p.setAvailableQuantity(e.getAvailableQuantity());
        p.setBuyerAccount(e.getBuyerAccount());
        p.setBuyerId(e.getBuyerId());
        p.setBuyerName(e.getBuyerName());
        p.setCorrelationId(e.getCorrelationId());
        p.setCurrencyToBuy(e.getCurrencyToBuy());
        p.setCurrencyToSell(e.getCurrencyToSell());
        p.setQuantityPurchased(e.getQuantityPurchased());
        p.setReceiverAmount(e.getReceiverAmount());
        p.setSellerId(e.getSellerId());
        p.setSellerName(e.getSellerName());
        p.setTotalQuantityCreated(e.getTotalQuantityCreated());
        p.setTransactionId(e.getTransactionId());
        //Date date = Date.from(e.getCreatedDate());
        p.setTxnDateTime(formDate(e.getCreatedDate()));
        // map more fields here as needed:
        // p.setAmount(e.getAmount());
        // p.setTxnRef(e.getTxnRef());
        // p.setTxnDateTime(e.getTxnDateTime());
        return p;
    }

    private String formDate(Instant datte) {

        LocalDateTime datetime = LocalDateTime.ofInstant(datte, ZoneOffset.UTC);
        String formatted = DateTimeFormatter.ofPattern("MMM dd, yyyy").format(datetime);
        //System.out.println(formatted);

        return formatted;
    }

    public ResponseEntity<ApiResponseModel> getUserTransactionsHistory(String auth) {
        ApiResponseModel resp = new ApiResponseModel();

        // 1) Auth / JWT
        final String phoneNumber;
        try {
            phoneNumber = utilService.getClaimFromJwt(auth, "phoneNumber");
            if (phoneNumber == null || phoneNumber.isBlank()) {
                return bad(resp, "Invalid token: phoneNumber claim missing", 400);
            }
        } catch (Exception e) {
            return bad(resp, "Unable to parse token", 400);
        }

        // 2) User lookup
        RegWalletInfo walletInfo = regWalletInfoRepository
                .findByPhoneNumber(phoneNumber)
                .orElse(null);
        if (walletInfo == null) {
            return bad(resp, "Customer not found for phone number", 400);
        }

        final String email = walletInfo.getEmail();
        if (email == null || email.isBlank()) {
            return bad(resp, "Customer email is missing", 400);
        }

        // 3) Fetch transactions (both roles)
        List<WalletIndivTransactionsDetails> asBuyer
                = walletIndivTransactionsDetailsRepo.findByBuyerEmailAddress(email);
        List<WalletIndivTransactionsDetails> asSeller
                = walletIndivTransactionsDetailsRepo.findBySellerEmailAddress(email);

        // 4) Merge, de-duplicate (by id/txnId), sort (newest first), map to DTOs
        List<WalletIndivTransactionsDetailsPojo> items
                = java.util.stream.Stream.concat(asBuyer.stream(), asSeller.stream())
                        .filter(java.util.Objects::nonNull)
                        // .distinct()  // only works if entity equals/hashCode are set
                        .collect(java.util.stream.Collectors.toMap(
                                // key: prefer a unique key you have; using getId() here
                                WalletIndivTransactionsDetails::getId,
                                this::toPojoSafely, // value
                                (a, b) -> a, // on duplicate key, keep first
                                java.util.LinkedHashMap::new))
                        .values().stream()
                        .sorted(java.util.Comparator.comparing(
                                WalletIndivTransactionsDetailsPojo::getTxnDateTime, // adjust to your field
                                java.util.Comparator.nullsLast(java.util.Comparator.naturalOrder()))
                                .reversed())
                        .collect(java.util.stream.Collectors.toList());

        // 5) Response
        resp.setStatusCode(200);
        resp.setDescription(items.isEmpty()
                ? "No transactions for this customer."
                : "Customer transactions pulled successfully.");
        resp.setData(items);

        return ResponseEntity.ok(resp);
    }

    public ResponseEntity<ApiResponseModel> createOrderCaller(BuyOfferNow rq, String auth) {
        final ApiResponseModel res = new ApiResponseModel();

        try {

            String phoneNumber = utilService.getClaimFromJwt(auth, "phoneNumber"); // preferred if your JWT has sellerId
            Optional<RegWalletInfo> getRec = regWalletInfoRepository.findByPhoneNumber(phoneNumber);

            String refCode = rq.getReferralCode() == null ? "0" : rq.getReferralCode();

            List<PeerToPeerFxReferral> getRefCode = peerToPeerFxReferralRepo.findByReferralCode(refCode);

            boolean refCodeExists = false;
            String refererName = null;

            if (getRefCode.size() > 0) {

                if (getRefCode.get(0).getEmailAddress().equals(getRec.get().getEmail())) {
                    return bad(res, "Referral code mismatched!", 400);
                }

                refererName = getRefCode.get(0).getReferrer();

                refCodeExists = true;
            }

            //validate pin
            /*BaseResponse bResPin = new BaseResponse();
            WalletNo wSend = new WalletNo();
            wSend.setPin(rq.getPin());

            wSend.setWalletId(getRec.get().getWalletId());
            bResPin = transactionServiceProxies.validatePin(wSend, auth);
            if (bResPin.getStatusCode() != 200) {
                return bad(res, bResPin.getDescription(), bResPin.getStatusCode());
            }*/

            //check if cus has currency currency
            List<AddAccountDetails> getAdDe = addAccountDetailsRepo.findByEmailAddressrData(getRec.get().getEmail());

            if (getAdDe.size() <= 0) {
                return bad(res, "Invalid User!", 400);
            }

            BaseResponse bRes = new BaseResponse();
            List<Offer> off = offers.findByCorrelationIdData(rq.getOfferCorrelationId());
            if (off.size() <= 0) {
                return bad(res, "Invalid Offer CorrelationId!", 400);
            }
            if (!off.get(0).getStatus().equals(OfferStatus.LIVE)) {
                return bad(res, "Offer no longer available!", 400);
            }

            List<WalletTransactionsDetails> getTransDe = walletTransactionsDetailsRepo.findByCorrelationId(rq.getOfferCorrelationId());

            //System.out.println(" getTransDe::::::::::::::::  %S  " + new Gson().toJson(getTransDe));
            logger.info("getTransDe: {}", mapper.writeValueAsString(getTransDe));

            System.out.println("  rq.getOfferCorrelationId() ::::::::::::::::  %S  " + rq.getOfferCorrelationId());

            if (getTransDe.size() <= 0) {
                // logger.info(" correlationId does not exist in WalletTransactionsDetails {}  ::::::::::::::::::::: ", rq.getOfferCorrelationId());

                return bad(res, "Invalid Offer CorrelationId!", 400);
            }

            System.out.println(" List<AddAccountDetails>  ::::::::::::::::  %S  " + getAdDe);
            String accountNumber = null;

            for (AddAccountDetails getWa : getAdDe) {

                // logger.info(" off.get(0).getCurrencyReceive() {}  ::::::::::::::::::::: ", off.get(0).getCurrencyReceive());
                //  logger.info(" off.get(0).getCurrencySell() {}  ::::::::::::::::::::: ", off.get(0).getCurrencySell());
                // System.out.println(" getWa.getCurrencyCode() ::::::::::::::::  %S  " + getWa.getCurrencyCode());
                String wallCurrencyCode = getWa.getCurrencyCode();
                System.out.println("wallCurrencyCode::::::::::::::::  %S  " + wallCurrencyCode);

                // logger.info(" wallCurrencyCode ::::::::::::::::::::: ", wallCurrencyCode);
                if (!wallCurrencyCode.equals(off.get(0).getCurrencyReceive().toString())) {
                    System.out.println("entered first if::::::::::::::::  %S  ");

                    if (!"CAD".equals(off.get(0).getCurrencyReceive().toString())) {
                        System.out.println("entered second if::::::::::::::::  %S  ");
                        //create account
                        accountNumber = ensureAccountNumberForMarket(auth, getRec.get(), getWa,
                                off.get(0).getCurrencyReceive().toString(), phoneNumber, rq.getOfferCorrelationId(), res);
                        if (accountNumber == null) {
                            return bad(res, res.getDescription() == null ? "Unable to prepare market account" : res.getDescription(),
                                    res.getStatusCode() == 0 ? 400 : res.getStatusCode());
                        }
                    } else {

                        accountNumber = phoneNumber;
                    }
                    System.out.println("accountNumber IN  ::::::::::::::::  %S  " + accountNumber);

                } else {

                    accountNumber = getWa.getAccountNumber();

                }

                if (!wallCurrencyCode.equals(off.get(0).getCurrencySell().toString())) {
                    System.out.println("entered third if::::::::::::::::  %S  ");
                    if (!"CAD".equals(off.get(0).getCurrencySell().toString())) {
                        System.out.println("entered first if::::::::::::::::  %S  ");
                        //create account
                        accountNumber = ensureAccountNumberForMarket(auth, getRec.get(), getWa,
                                off.get(0).getCurrencySell().toString(), phoneNumber, rq.getOfferCorrelationId(), res);
                        if (accountNumber == null) {
                            return bad(res, res.getDescription() == null ? "Unable to prepare market account" : res.getDescription(),
                                    res.getStatusCode() == 0 ? 400 : res.getStatusCode());
                        }
                    }
                }

            }

            String sellerAcctNumber = null;
            String sellerAcctNumberName = null;

            System.out.println("accountNumber OUT  ::::::::::::::::  %S  " + accountNumber);

            Optional<RegWalletInfo> getRecCheSeller = null;

            if (off.get(0).getCurrencySell().toString().equals("CAD")) {
                getRecCheSeller = regWalletInfoRepository.findByWalletIdOptional(getTransDe.get(0).getSellerId());

                System.out.println(" getRecCheSeller::::::::::::::::  %S  " + getRecCheSeller);

                System.out.println(" getRecCheSeller.get().getPhoneNumber() ::::::::::::::::  %S  " + getRecCheSeller.get().getPhoneNumber());

                System.out.println("phoneNumber ::::::::::::::::  %S  " + phoneNumber);

                if (getRecCheSeller.get().getPhoneNumber().equals(phoneNumber)) {

                    System.out.println(" :::::::::::::::: An offer creator cannot buy same offer!  ");

                    return bad(res, "An offer creator cannot buy same offer!", 400);

                }

                // if (off.get(0).getCurrencySell().equals("CAD")) then account to receive on is in AddAccountDetails
                //  List<WalletTransactionsDetails> getWalSeller = walletTransactionsDetailsRepo.findByCorrelationId(rq.getOfferCorrelationId());
                List<AddAccountDetails> getSellerAcct = addAccountDetailsRepo.findByEmailAddressrData(getRecCheSeller.get().getEmail());

                for (AddAccountDetails getWa : getSellerAcct) {
                    if (getWa.getCurrencyCode().equals(off.get(0).getCurrencyReceive().toString())) {
                        sellerAcctNumber = getWa.getAccountNumber();
                    }
                    /*else {
                        if (!"CAD".equals(off.get(0).getCurrencyReceive().toString())) {
                            return bad(res, "Seller do not have the Currency account to receive payment!", 400);
                        }
                        sellerAcctNumber = getRecCheSeller.get().getPhoneNumber();
                    }*/
                }
                // sellerAcctNumber = phoneNumber;

            } else {

                List<WalletTransactionsDetails> getWalSeller = walletTransactionsDetailsRepo.findByCorrelationId(rq.getOfferCorrelationId());

                List<AddAccountDetails> getSellerAcct = addAccountDetailsRepo.findByWalletIdrData1(getWalSeller.get(0).getSellerId());
                getRecCheSeller = regWalletInfoRepository.findByEmail(getSellerAcct.get(0).getEmailAddress());

                if ("CAD".equals(off.get(0).getCurrencyReceive().toString())) {

                    sellerAcctNumber = getRecCheSeller.get().getPhoneNumber();
                    sellerAcctNumberName = getRecCheSeller.get().getAccountName();

                } else {
                    for (AddAccountDetails getWa : getSellerAcct) {
                        if (getWa.getCurrencyCode().equals(off.get(0).getCurrencyReceive().toString())) {
                            sellerAcctNumber = getWa.getAccountNumber();
                            sellerAcctNumberName = getRecCheSeller.get().getAccountName();
                        }
                    }
                }

                /*List<AddAccountDetails> sellDe = addAccountDetailsRepo.findByWalletIdrData1(getTransDe.get(0).getSellerId());
                getRecCheSeller = regWalletInfoRepository.findByEmail(sellDe.get(0).getEmailAddress());

                if (getRecCheSeller.get().getPhoneNumber().equals(phoneNumber)) {

                    System.out.println(" :::::::::::::::: An offer creator cannot buy same offer!  ");

                    return bad(res, "An offer creator cannot buy same offer!", 400);

                }*/
                //sellerAcctNumber = getRecCheSeller.get().getPhoneNumber();
            }

            String walletId = getRec.get().getWalletId();
            // 0) Basic request checks
            if (rq == null) {
                return bad(res, "Empty request.", 400);
            }
            if (isBlank(rq.getAmount())) {
                return bad(res, "amount is required!", 400);
            }
            BigDecimal setAmount = new BigDecimal(rq.getAmount());

            if (setAmount.compareTo(getTransDe.get(0).getAvailableQuantity()) > 0) {
                System.out.println("Amount to buy is more than available quantity for sales!");
                return bad(res, "Amount to buy is more than available quantity for sales!", 400);
            }
            /*BaseResponse getRess = this.validateAmountInRange(rq.getAmount(), off.get(0).getMinAmount(), off.get(0).getMaxAmount());
            if (getRess.getStatusCode() != 200) {
                return bad(res, getRess.getDescription(), getRess.getStatusCode());
            }*/

 /*if (setAmount.compareTo(off.get(0).getQtyAvailable()) > 0) {
                String reDec = "Requested amount: " + setAmount + " is more than available quantity: " + off.get(0).getQtyAvailable();
                //bRes.setDescription("amount must be <= " + maxAmount);
                return bad(res, reDec, 400);

            }*/
            // List<AddAccountDetails> getAdDeNew = addAccountDetailsRepo.findByWalletIdrData1(walletId);
            //validate account balance
            //accountNumber = getWa.getAccountNumber();
            System.out.println("accountNumber b4 check account  ::::::::::::::::  %S  " + accountNumber);

            System.out.println("accountNumber to DEBIT ::::::::::::::::  %S  " + accountNumber);

            String transactionId = String.valueOf(GlobalMethods.generateTransactionId());

            ManageFeesConfigReq mFeee = new ManageFeesConfigReq();
            mFeee.setAmount(rq.getAmount());
            mFeee.setTransType("buylisting");
            mFeee.setCurrencyCode(off.get(0).getCurrencyReceive().toString());

            BaseResponse mConfig = utilService.getFeesConfig(mFeee);

            if (mConfig.getStatusCode() != 200) {
                return bad(res, mConfig.getDescription(), mConfig.getStatusCode());
            }

            System.out.println("mConfig::::::::::::::::  %S  " + mConfig);

            System.out.println(" mConfig::::::::::::::::  %S  " + new Gson().toJson(mConfig));

            /*
            Object val = data.get("fees");
            BigDecimal fees = (val instanceof BigDecimal)
        ? (BigDecimal) val
        : new BigDecimal(String.valueOf(val));
             */
            // String pFeesString = (String) mConfig.getData().get("fees");
            String pFeesString = String.valueOf(mConfig.getData().get("fees"));

            BigDecimal receiveAmount = setAmount.multiply(off.get(0).getRate()).setScale(2, RoundingMode.HALF_UP);

            BigDecimal finCharges = receiveAmount.add(new BigDecimal(pFeesString));
            WalletInfoValiAcctBal wVal = new WalletInfoValiAcctBal();
            wVal.setAccountNumber(accountNumber);
            wVal.setRequestedAmount(finCharges);
            wVal.setWalletId(getRec.get().getWalletId());
            System.out.println(" validateAccountBalnce req ::::::::::::::::  %S  " + new Gson().toJson(wVal));

            BaseResponse bResss = transactionServiceProxies.validateAccountBalnce(wVal, auth);

            System.out.println(" validateAccountBalnce res ::::::::::::::::  %S  " + new Gson().toJson(bResss));

            if (bResss.getStatusCode() != 200) {
                return bad(res, bResss.getDescription(), bResss.getStatusCode());
            }

            // debit the buyer first, then keep the seller-facing credit until the seller GL leg is ready.
            // This keeps the risk concentrated on system-controlled accounts and makes compensation saner.
            DebitWalletCaller rqD = new DebitWalletCaller();
            rqD.setAuth("Buyer");
            rqD.setFees(pFeesString);
            rqD.setFinalCHarges(finCharges.toString());
            rqD.setNarration(off.get(0).getCurrencyReceive() + "_Withdrawal");
            rqD.setPhoneNumber(accountNumber);
            rqD.setTransAmount(finCharges.toString());
            rqD.setTransactionId(transactionId);

            System.out.println(" debitBuyerAcct REQ ::::::::::::::::  %S  " + new Gson().toJson(rqD));

            BaseResponse debitBuyerAcct = transactionServiceProxies.debitCustomerWithType(rqD, "CUSTOMER", auth);

            System.out.println(" debitBuyerAcct RESPONSE ::::::::::::::::  %S  " + new Gson().toJson(debitBuyerAcct));

            if (debitBuyerAcct.getStatusCode() != 200) {
                return bad(res, debitBuyerAcct.getDescription(), debitBuyerAcct.getStatusCode());
            }

            List<AppConfig> getAppConf = appConfigRepo.findByConfigName(off.get(0).getCurrencyReceive().toString());
            String GGL_ACCOUNT = null;
            String GGL_CODE = null;
            for (AppConfig getConfDe : getAppConf) {
                if (getConfDe.getConfigName().equals(off.get(0).getCurrencyReceive().toString())) {
                    GGL_ACCOUNT = getConfDe.getConfigValue();
                    GGL_CODE = off.get(0).getCurrencyReceive().toString();
                }
            }

            if (GGL_ACCOUNT == null || GGL_CODE == null) {
                BaseResponse buyerRefund = creditAccount(auth, accountNumber, finCharges,
                        transactionId + "-BUYER-REFUND",
                        off.get(0).getCurrencyReceive() + "_Withdrawal_Reversal",
                        "CUSTOMER", "Buyer");
                String msg = "Buyer debit posted but GL configuration is missing for " + off.get(0).getCurrencyReceive();
                if (buyerRefund.getStatusCode() == 200) {
                    return bad(res, msg + ". Buyer debit has been reversed.", 500);
                }
                return bad(res, msg + ". Manual intervention required.", 500);
            }

            String decryptedGglAccount = utilService.decryptData(GGL_ACCOUNT);

            DebitWalletCaller debGLCredit = new DebitWalletCaller();
            debGLCredit.setAuth(off.get(0).getCurrencyReceive().toString());
            debGLCredit.setFees("0.00");
            debGLCredit.setFinalCHarges(receiveAmount.toString());
            debGLCredit.setNarration(rqD.getNarration());
            debGLCredit.setPhoneNumber(decryptedGglAccount);
            debGLCredit.setTransAmount(receiveAmount.toString());
            debGLCredit.setTransactionId(transactionId + "-" + GGL_CODE + "-BUYER-GL-DR");

            System.out.println(" debitAcct_GL REQUEST ::::::::::::::::  %S  " + new Gson().toJson(debGLCredit));

            BaseResponse debitAcct_GLRes = transactionServiceProxies.debitCustomerWithType(debGLCredit, off.get(0).getCurrencyReceive().toString(), auth);
            System.out.println(" debitAcct_GLRes RESPONSE ::::::::::::::::  %S  " + new Gson().toJson(debitAcct_GLRes));

            if (debitAcct_GLRes.getStatusCode() != 200) {
                BaseResponse buyerRefund = creditAccount(auth, accountNumber, finCharges,
                        transactionId + "-BUYER-REFUND",
                        off.get(0).getCurrencyReceive() + "_Withdrawal_Reversal",
                        "CUSTOMER", "Buyer");
                if (buyerRefund.getStatusCode() == 200) {
                    return bad(res, "Buyer debit posted but buyer GL debit failed. Buyer debit has been reversed.", 500);
                }
                return bad(res, "Buyer debit posted but buyer GL debit failed, and buyer reversal also failed. Manual intervention required.", 500);
            }

            CreditWalletCaller sellerGlCredit = new CreditWalletCaller();
            sellerGlCredit.setAuth(off.get(0).getCurrencyReceive().toString());
            sellerGlCredit.setFees("0.00");
            sellerGlCredit.setFinalCHarges(receiveAmount.toString());
            sellerGlCredit.setNarration(off.get(0).getCurrencyReceive() + "_Deposit");
            sellerGlCredit.setPhoneNumber(decryptedGglAccount);
            sellerGlCredit.setTransAmount(receiveAmount.toString());
            sellerGlCredit.setTransactionId(transactionId + "-" + GGL_CODE + "-SELLER-GL-CR");

            System.out.println(" CREDIT GL SELLER LEG REQUEST  ::::::::::::::::  %S  " + new Gson().toJson(sellerGlCredit));

            BaseResponse creditAcctNGN_GL = transactionServiceProxies.creditCustomerWithType(sellerGlCredit, GGL_CODE + "_GL", auth);

            System.out.println(" CREDIT GL SELLER LEG Response from core ::::::::::::::::  %S  " + new Gson().toJson(creditAcctNGN_GL));

            if (creditAcctNGN_GL.getStatusCode() != 200) {
                BaseResponse reverseBuyerGl = creditAccount(auth, decryptedGglAccount, receiveAmount,
                        transactionId + "-BUYER-GL-REVERSAL",
                        off.get(0).getCurrencyReceive() + "_GL_Debit_Reversal",
                        off.get(0).getCurrencyReceive().toString(),
                        off.get(0).getCurrencyReceive().toString());
                BaseResponse buyerRefund = creditAccount(auth, accountNumber, finCharges,
                        transactionId + "-BUYER-REFUND",
                        off.get(0).getCurrencyReceive() + "_Withdrawal_Reversal",
                        "CUSTOMER", "Buyer");
                if (reverseBuyerGl.getStatusCode() == 200 && buyerRefund.getStatusCode() == 200) {
                    return bad(res, "Seller GL credit failed. Buyer debit and buyer GL debit have been reversed.", 500);
                }
                return bad(res, "Seller GL credit failed and compensation was incomplete. Manual intervention required.", 500);
            }

            CreditWalletCaller rqC = new CreditWalletCaller();
            rqC.setAuth("Seller");
            rqC.setFees("00");
            rqC.setFinalCHarges(receiveAmount.toString());
            rqC.setNarration(off.get(0).getCurrencyReceive() + "_Deposit");
            rqC.setPhoneNumber(sellerAcctNumber);
            rqC.setTransAmount(receiveAmount.toString());
            rqC.setTransactionId(transactionId + "-SELLER-CR");

            System.out.println(" CREDIT SELLER Credit REQUEST  ::::::::::::::::  %S  " + new Gson().toJson(rqC));

            BaseResponse creditSellerAcct = transactionServiceProxies.creditCustomerWithType(rqC, "CUSTOMER", auth);

            System.out.println(" CREDIT SELLER Credit Response from core ::::::::::::::::  %S  " + new Gson().toJson(creditSellerAcct));
            if (creditSellerAcct.getStatusCode() != 200) {
                BaseResponse reverseSellerGl = debitAccount(auth, decryptedGglAccount, receiveAmount,
                        transactionId + "-SELLER-GL-REVERSAL",
                        off.get(0).getCurrencyReceive() + "_Deposit_Reversal",
                        GGL_CODE + "_GL",
                        off.get(0).getCurrencyReceive().toString());
                BaseResponse reverseBuyerGl = creditAccount(auth, decryptedGglAccount, receiveAmount,
                        transactionId + "-BUYER-GL-REVERSAL",
                        off.get(0).getCurrencyReceive() + "_GL_Debit_Reversal",
                        off.get(0).getCurrencyReceive().toString(),
                        off.get(0).getCurrencyReceive().toString());
                BaseResponse buyerRefund = creditAccount(auth, accountNumber, finCharges,
                        transactionId + "-BUYER-REFUND",
                        off.get(0).getCurrencyReceive() + "_Withdrawal_Reversal",
                        "CUSTOMER", "Buyer");
                if (reverseSellerGl.getStatusCode() == 200 && reverseBuyerGl.getStatusCode() == 200 && buyerRefund.getStatusCode() == 200) {
                    return bad(res, "Seller credit failed. Prior ledger legs have been reversed.", 500);
                }
                return bad(res, "Seller credit failed and compensation was incomplete. Manual intervention required.", 500);
            }

                String sellerPhoneForHistory = getRecCheSeller.get().getPhoneNumber();
                String buyerPhoneForHistory = phoneNumber;

                FinWealthPaymentTransaction kTrans2b = new FinWealthPaymentTransaction();
                kTrans2b.setAmmount(new BigDecimal(rqC.getFinalCHarges()));
                kTrans2b.setCreatedDate(Instant.now().plusSeconds(1));
                kTrans2b.setFees(new BigDecimal(rqC.getFees()));
                kTrans2b.setPaymentType("FxPeer Transfer");
                kTrans2b.setReceiver(sellerPhoneForHistory != null && !sellerPhoneForHistory.trim().isEmpty() ? sellerPhoneForHistory : sellerAcctNumber);
                kTrans2b.setSender(buyerPhoneForHistory != null && !buyerPhoneForHistory.trim().isEmpty() ? buyerPhoneForHistory : accountNumber);
                kTrans2b.setTransactionId(transactionId);
                kTrans2b.setSenderTransactionType("Withdrawal");
                kTrans2b.setReceiverTransactionType("Deposit");
                kTrans2b.setReceiverBankName(sellerAcctNumberName);
                kTrans2b.setWalletNo(buyerPhoneForHistory != null && !buyerPhoneForHistory.trim().isEmpty() ? buyerPhoneForHistory : accountNumber);
                kTrans2b.setReceiverName(sellerAcctNumberName);
                kTrans2b.setSenderName(getRec.get().getFullName());
                kTrans2b.setSentAmount(rqC.getFinalCHarges());
                kTrans2b.setTheNarration("Fx Peer-Peer Transfer");
                kTrans2b.setCurrencyCode(off.get(0).getCurrencyReceive().toString());
                finWealthPaymentTransactionRepo.save(kTrans2b);
                // publishCanonicalHistory(kTrans2b, phoneNumber, getRecCheSeller.get().getPhoneNumber(), phoneNumber, getRec.get().getFullName(), sellerAcctNumberName);

                System.out.println("ABOUT TO PUBLISH TXN HISTORY txId=" + kTrans2b.getTransactionId()
                        + " walletNo=" + kTrans2b.getWalletNo()
                        + " paymentType=" + kTrans2b.getPaymentType()
                        + " amount=" + kTrans2b.getAmmount());
                //augument WalletTransactionsDetails
                WalletTransactionsDetails getWalDeupdate = walletTransactionsDetailsRepo.findByCorrelationIdUpdated(rq.getOfferCorrelationId());
                BigDecimal availableQuantity = getWalDeupdate.getAvailableQuantity();

                System.out.println(" availableQuantity before removing request amount ::::::::::::::::  %S  " + availableQuantity);

                getWalDeupdate.setLastModifiedDate(Instant.now());
                getWalDeupdate.setBuyerId(walletId);
                getWalDeupdate.setBuyerName(getRec.get().getFullName());

                getWalDeupdate.setAvailableQuantity(availableQuantity.subtract(setAmount));
                walletTransactionsDetailsRepo.save(getWalDeupdate);

                List<WalletTransactionsDetails> getWalList = walletTransactionsDetailsRepo.findByCorrelationId(rq.getOfferCorrelationId());
                BigDecimal avail = getWalList.get(0).getAvailableQuantity();

                System.out.println(" availableQuantity after removing request amount   ::::::::::::::::  %S  " + avail);

                System.out.println(" AvailableQuantity after sales ::::::::::::::::   " + avail);
                List<Offer> offerDe = offers.findByCorrelationIdData(rq.getOfferCorrelationId());
                Offer offerDeUp = offers.findByCorrelationIdDataUpdate(rq.getOfferCorrelationId());

                if (avail == null || avail.compareTo(BigDecimal.ZERO) <= 0) {
                    System.out.println(" AvailableQuantity is less or equal 0 ::::::::::::::::   ");

                    if (offerDe.size() > 0) {

                        System.out.println(" Update offer to SOLDOUT ::::::::::::::::   ");

                        offerDeUp.setStatus(OfferStatus.SOLDOUT);
                        offerDeUp.setUpdatedNow();
                        //offerDeUp.setQtyAvailable(avail);
                        offers.save(offerDeUp);

                    }

                } else {

                    System.out.println(" Upadate offer ::::::::::::::::   ");

                    offerDeUp.setUpdatedNow();
                    //offerDeUp.setQtyAvailable(avail);
                    offers.save(offerDeUp);
                }

                WalletIndivTransactionsDetails wallInd = new WalletIndivTransactionsDetails();
                wallInd.setAvailableQuantity(avail);
                wallInd.setTotalQuantityCreated(getTransDe.get(0).getTotalQuantityCreated());
                wallInd.setBuyerAccount(accountNumber);
                wallInd.setBuyerId(getWalDeupdate.getBuyerId());
                wallInd.setBuyerName(getWalDeupdate.getBuyerName());
                wallInd.setCorrelationId(rq.getOfferCorrelationId());
                wallInd.setCreatedBy("System");
                wallInd.setCreatedDate(Instant.now());
                wallInd.setCurrencyToBuy(getWalDeupdate.getCurrencyToBuy());
                wallInd.setCurrencyToSell(getWalDeupdate.getCurrencyToSell());
                wallInd.setSellerName(getWalDeupdate.getSellerName());
                wallInd.setTransactionId(transactionId);
                wallInd.setSellerId(getWalDeupdate.getSellerId());
                wallInd.setReceiverAmount(receiveAmount);
                wallInd.setCorrelationId(getWalDeupdate.getCorrelationId());
                wallInd.setBuyerEmailAddress(getRec.get().getEmail());
                wallInd.setSellerEmailAddress(getRecCheSeller.get().getEmail());
                wallInd.setQuantityPurchased(setAmount);

                walletIndivTransactionsDetailsRepo.save(wallInd);
                Order orddd = buyNow(getRec.get().getFirstName(), sellerAcctNumberName, off.get(0).getId(), setAmount, walletId, 0L, rq.getOfferCorrelationId(),
                        String.valueOf(off.get(0).getSellerUserId()), "0.00", transactionId, auth);

                String referralSharingPayment;

                /*List<PeerToPeerFxReferral> getRef = peerToPeerFxReferralRepo.findByEmailAddress(getRec.get().getEmail());
                if (getRef.size() <= 0) {

                    if (refCodeExists == true) {

                        //  referralSharingPayment = getRef.get(0).getReferralSharingPayment() == null ? "0" : getRef.get(0).getReferralSharingPayment();
                        //save and generate the customer referral code 
                        PeerToPeerFxReferral refGen = new PeerToPeerFxReferral();
                        refGen.setCreatedDate(Instant.now());
                        refGen.setEmailAddress(getRec.get().getEmail());
                        refGen.setReferee(getRec.get().getFirstName() + " " + getRec.get().getLastName());
                        refGen.setRefereeCode(GlobalMethods.generateReferal(getRec.get().getFirstName()));
                        refGen.setReferrer(refererName);
                        refGen.setReferralCode(rq.getReferralCode());
                        refGen.setReferralSharingPayment("1");
                        //peerToPeerFxReferralRepo.save(refGen);

                        //give buyer payment in wallet -- this depends on currency to SELL if naira (fillNig08006763319) in appConfig table
                        ManageFeesConfigReq getBonus = new ManageFeesConfigReq();
                        getBonus.setAmount(receiveAmount.toString());
                        getBonus.setCurrencyCode(refCode);
                        if (getBonus.getCurrencyCode().equals("NGN")) {
                            getBonus.setTransType("fxbonusnigeria");
                        }
                        if (getBonus.getCurrencyCode().equals("CAD")) {
                            getBonus.setTransType("fxbonuscanada");
                        }

                        BaseResponse getBonusRes = utilService.getFeesConfig(getBonus);

                        //give buyer payment in wallet -- this depends on currency to SELL if dollar () in appConfig table
                        //give seller payment in wallet -- this depends on currency to RECEIVE if naira (fillNig08006763319) in appConfig table
                        //give buyer payment in wallet -- this depends on currency to RECEIVE if dollar () in appConfig table
                        //give the Referral payemnt in wallet
                    }

                } else {
                    referralSharingPayment = getRef.get(0).getReferralSharingPayment();
                    if (!referralSharingPayment.equals("1")) {

                    }
                }*/

 /*
                 buyNow(long offerId, BigDecimal amount, long buyerId, long lockTtlSeconds,
            String correlId, String sellerId, String fees, String transactionId)
                 */
                // 4) Success
                res.setStatusCode(200);
                res.setDescription("Trade Fx Purchase was successful.");
                res.setData(orddd); // or map to a lightweight DTO

            return ResponseEntity.ok(res);

        } /*catch (BusinessException be) {
            be.printStackTrace();
            return bad(res, be.getMessage(), 500);
        } */ catch (Exception ex) {
            logger.error("createOrderCaller failed offerCorrelationId={} amount={} authPresent={}",
                    rq != null ? rq.getOfferCorrelationId() : null,
                    rq != null ? rq.getAmount() : null,
                    auth != null,
                    ex);
            return bad(res, "An error occurred, please try again.", 500);
        }
    }

    private BaseResponse creditAccount(String auth, String phoneNumber, BigDecimal amount, String transactionId,
            String narration, String userType, String actor) {
        CreditWalletCaller rq = new CreditWalletCaller();
        rq.setAuth(actor);
        rq.setFees("0.00");
        rq.setFinalCHarges(amount.toString());
        rq.setNarration(narration);
        rq.setPhoneNumber(phoneNumber);
        rq.setTransAmount(amount.toString());
        rq.setTransactionId(transactionId);
        return transactionServiceProxies.creditCustomerWithType(rq, userType, auth);
    }

    private BaseResponse debitAccount(String auth, String phoneNumber, BigDecimal amount, String transactionId,
            String narration, String userType, String actor) {
        DebitWalletCaller rq = new DebitWalletCaller();
        rq.setAuth(actor);
        rq.setFees("0.00");
        rq.setFinalCHarges(amount.toString());
        rq.setNarration(narration);
        rq.setPhoneNumber(phoneNumber);
        rq.setTransAmount(amount.toString());
        rq.setTransactionId(transactionId);
        return transactionServiceProxies.debitCustomerWithType(rq, userType, auth);
    }

    @Transactional
    public Order buyNow(String receiverAcctName, String senderName, long offerId, BigDecimal amount, String buyerId, long lockTtlSeconds,
            String correlId, String sellerId, String fees, String transactionId, String auth)
            throws NoSuchAlgorithmException, NoSuchPaddingException,
            InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        //Offer off = offers.findById(offerId).orElseThrow(() -> new NotFoundException("Offer not found"));
        List<Offer> off = offers.findByCorrelationIdData(correlId);
        if (off.size() <= 0) {
            // return bad(res, "Invalid Offer CorrelationId!", 400);
            throw new BusinessException("Invalid Offer CorrelationId!");
        }

        System.out.println(" buyerId ::::::::::::::::  %S  " + buyerId);

        List<AddAccountDetails> getSellerAcct;

        String getSellerAccttAcct = null;

        if ("CAD".equals(off.get(0).getCurrencySell().toString())) {

            List<RegWalletInfo> getRecord = regWalletInfoRepository.findByWalletIdList(sellerId);

            getSellerAccttAcct = getRecord.get(0).getPhoneNumber();

        } else {
            getSellerAcct = addAccountDetailsRepo.findByWalletIdrData1(String.valueOf(sellerId));

            for (AddAccountDetails getWa : getSellerAcct) {
                if (getWa.getCurrencyCode().equals(off.get(0).getCurrencySell().toString())) {

                    getSellerAccttAcct = getWa.getAccountNumber();

                }
            }
        }

        List<AppConfig> getAppConf = appConfigRepo.findByConfigName(off.get(0).getCurrencySell().toString());
        String GGL_ACCOUNT = null;
        String GGL_CODE = null;
        for (AppConfig getConfDe : getAppConf) {
            if (getConfDe.getConfigName().equals(off.get(0).getCurrencySell().toString())) {
                GGL_ACCOUNT = getConfDe.getConfigValue();
                GGL_CODE = off.get(0).getCurrencyReceive().toString();
            }
        }

        //debit the seller
        DebitWalletCaller rqD = new DebitWalletCaller();
        rqD.setAuth("Seller");
        rqD.setFees(fees);
        rqD.setFinalCHarges(amount.toString());
        rqD.setNarration(off.get(0).getCurrencySell() + "_Withdrawal");
        rqD.setPhoneNumber(getSellerAccttAcct);
        rqD.setTransAmount(amount.toString());
        rqD.setTransactionId(transactionId);
        // System.out.println(" debitSellerAcct REQ  ::::::::::::::::  %S  " + new Gson().toJson(rqD));

        //  BaseResponse debitSellerAcct = transactionServiceProxies.debitCustomerWithType(rqD, "CUSTOMER", auth);
        //   System.out.println(" debitSellerAcct RESPONSE from core ::::::::::::::::  %S  " + new Gson().toJson(debitSellerAcct));
        //logger.info(" debitSellerAcct   response ::::::::::::::::::::: ", debitSellerAcct);
        // if (debitSellerAcct.getStatusCode() == 200) {
        DebitWalletCaller debGLCredit = new DebitWalletCaller();
        debGLCredit.setAuth("Debit_GL");
        debGLCredit.setFees("0.00");
        debGLCredit.setFinalCHarges(rqD.getFinalCHarges());
        debGLCredit.setNarration(rqD.getNarration());
        // debGLCredit.setPhoneNumber(utilService.decryptData(utilService.getSETTING_KEY_WALLET_SYSTEM_SYSTEM_GG_CAD()));
        debGLCredit.setPhoneNumber(utilService.decryptData(GGL_ACCOUNT));
        debGLCredit.setTransAmount(rqD.getFinalCHarges());
        debGLCredit.setTransactionId(transactionId + "-DEBIT");

        //   System.out.println(" debGLCredit REQ  ::::::::::::::::  %S  " + new Gson().toJson(debGLCredit));
        //    BaseResponse debit_GL = transactionServiceProxies.debitCustomerWithType(debGLCredit, off.get(0).getCurrencySell().toString(), auth);
        //  System.out.println(" debit_GL RESPONSE  ::::::::::::::::  %S  " + new Gson().toJson(debit_GL));
//
        //logger.info(" debit GL for seller leg  response ::::::::::::::::::::: ", debit_GL);
        Offer offUp = offers.findByCorrelationIdDataUpdate(correlId);

// Reserve available
        offUp.setQtyAvailable(off.get(0).getQtyAvailable().subtract(amount));
        offers.save(offUp);

// Compute receive = amount * rate
        BigDecimal receive = amount.multiply(off.get(0).getRate()).setScale(2, RoundingMode.HALF_UP);

        //CREDIT BUYER
        String getBuyyAcctAcct = null;
        List<AddAccountDetails> getBuyyAcct;
        List<RegWalletInfo> getRecord;
        getRecord = regWalletInfoRepository.findByWalletIdList(buyerId);

        if ("CAD".equals(off.get(0).getCurrencySell().toString())) {

            getBuyyAcctAcct = getRecord.get(0).getPhoneNumber();

        } else {

            getBuyyAcct = addAccountDetailsRepo.findByEmailAddressrData(getRecord.get(0).getEmail());

            for (AddAccountDetails getWa : getBuyyAcct) {
                if (getWa.getCurrencyCode().equals(off.get(0).getCurrencySell().toString())) {
                    getBuyyAcctAcct = getWa.getAccountNumber();

                }
            }
        }

        CreditWalletCaller rqC = new CreditWalletCaller();
        rqC.setAuth("Buyer");
        rqC.setFees("00");
        rqC.setFinalCHarges(amount.toString());
        rqC.setNarration(off.get(0).getCurrencySell() + "_Deposit");
        rqC.setPhoneNumber(getBuyyAcctAcct);
        rqC.setTransAmount(amount.toString());
        rqC.setTransactionId(transactionId + "-DEPOSIT" + "2");

        System.out.println(" creditBuyerAcct REQ  ::::::::::::::::  %S  " + new Gson().toJson(rqC));

        CreditWalletCaller glCredit = new CreditWalletCaller();
        glCredit.setAuth("Receiver");
        glCredit.setFees("0.00");
        glCredit.setFinalCHarges(rqC.getFinalCHarges());
        glCredit.setNarration(rqC.getNarration());
        glCredit.setPhoneNumber(utilService.decryptData(GGL_ACCOUNT));
        glCredit.setTransAmount(rqC.getFinalCHarges());
        glCredit.setTransactionId(transactionId + "-BUYER-GL-CR");

        System.out.println(" creditAcct_GL buyer legCredit REQ  ::::::::::::::::  %S  " + new Gson().toJson(glCredit));

        BaseResponse creditAcct_GL = transactionServiceProxies.creditCustomerWithType(glCredit, off.get(0).getCurrencySell().toString(), auth);

        System.out.println(" credit GL for buyer legCredit Response from core ::::::::::::::::  %S  " + new Gson().toJson(creditAcct_GL));

        if (creditAcct_GL.getStatusCode() != 200) {
            throw new BusinessException("Buyer GL credit failed");
        }

        BaseResponse creditBuyerAcct = transactionServiceProxies.creditCustomerWithType(rqC, "CUSTOMER", auth);

        System.out.println(" creditBuyerAcct Response from core ::::::::::::::::  %S  " + new Gson().toJson(creditBuyerAcct));
        if (creditBuyerAcct.getStatusCode() != 200) {
            BaseResponse reverseBuyerGl = debitAccount(auth, utilService.decryptData(GGL_ACCOUNT), new BigDecimal(rqC.getFinalCHarges()),
                    transactionId + "-BUYER-GL-CR-REVERSAL", rqC.getNarration() + "_REVERSAL",
                    off.get(0).getCurrencySell().toString(), "Receiver");
            if (reverseBuyerGl.getStatusCode() != 200) {
                throw new BusinessException("Buyer wallet credit failed after GL release; manual intervention required");
            }
            throw new BusinessException("Buyer wallet credit failed");
        }

        List<RegWalletInfo> sellerHistoryRecords = regWalletInfoRepository.findByWalletIdList(String.valueOf(sellerId));
        String sellerPhoneForHistory = sellerHistoryRecords != null && !sellerHistoryRecords.isEmpty()
                ? sellerHistoryRecords.get(0).getPhoneNumber() : null;
        String buyerPhoneForHistory = getRecord != null && !getRecord.isEmpty()
                ? getRecord.get(0).getPhoneNumber() : null;

        FinWealthPaymentTransaction kTrans2b = new FinWealthPaymentTransaction();
        kTrans2b.setAmmount(new BigDecimal(rqC.getFinalCHarges()));
        kTrans2b.setCreatedDate(Instant.now().plusSeconds(1));
        kTrans2b.setFees(new BigDecimal(rqC.getFees()));
        kTrans2b.setPaymentType("FxPeer Transfer");
        kTrans2b.setReceiver(buyerPhoneForHistory != null && !buyerPhoneForHistory.trim().isEmpty() ? buyerPhoneForHistory : getBuyyAcctAcct);
        kTrans2b.setSender(sellerPhoneForHistory != null && !sellerPhoneForHistory.trim().isEmpty() ? sellerPhoneForHistory : getSellerAccttAcct);
        kTrans2b.setTransactionId(transactionId);
        kTrans2b.setSenderTransactionType("Withdrawal");
        kTrans2b.setReceiverTransactionType("Deposit");
        kTrans2b.setReceiverBankName(receiverAcctName);
        kTrans2b.setWalletNo(sellerPhoneForHistory != null && !sellerPhoneForHistory.trim().isEmpty() ? sellerPhoneForHistory : getSellerAccttAcct);
        kTrans2b.setReceiverName(receiverAcctName);
        kTrans2b.setSenderName(senderName);
        kTrans2b.setSentAmount(rqC.getFinalCHarges());
        kTrans2b.setTheNarration("Fx Peer-Peer Transfer");
        kTrans2b.setCurrencyCode(off.get(0).getCurrencySell().toString());
        finWealthPaymentTransactionRepo.save(kTrans2b);
        // publishCanonicalHistory(kTrans2b, sellerPhoneForHistory, buyerPhoneForHistory, sellerPhoneForHistory, senderName, receiverAcctName);
        Order ord = new Order();
        ord.setOfferId(off.get(0).getId());
        ord.setSellerUserId(off.get(0).getSellerUserId());
        ord.setBuyerUserId(Long.valueOf(buyerId));
        ord.setCurrencySell(off.get(0).getCurrencySell());
        ord.setCurrencyReceive(off.get(0).getCurrencyReceive());
        ord.setSellAmount(amount);
        ord.setReceiveAmount(receive);
        ord.setRate(off.get(0).getRate());
        ord.setLockExpiresAt(Instant.now().plusSeconds(lockTtlSeconds));
        ord.setStatus(com.finacial.wealth.api.fxpeer.exchange.common.OrderStatus.RELEASED);
        return orders.save(ord);
    }
}
