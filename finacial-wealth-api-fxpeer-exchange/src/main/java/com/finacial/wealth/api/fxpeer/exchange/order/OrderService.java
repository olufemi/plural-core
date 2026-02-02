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
import com.finacial.wealth.api.fxpeer.exchange.domain.PeerToPeerFxReferral;
import com.finacial.wealth.api.fxpeer.exchange.domain.RegWalletInfo;
import com.finacial.wealth.api.fxpeer.exchange.domain.RegWalletInfoRepository;
import com.finacial.wealth.api.fxpeer.exchange.feign.ProfilingProxies;
import com.finacial.wealth.api.fxpeer.exchange.feign.TransactionServiceProxies;
import com.finacial.wealth.api.fxpeer.exchange.model.AddAccountObj;
import com.finacial.wealth.api.fxpeer.exchange.model.ApiResponseModel;
import com.finacial.wealth.api.fxpeer.exchange.model.BaseResponse;
import com.finacial.wealth.api.fxpeer.exchange.model.CreditWalletCaller;
import com.finacial.wealth.api.fxpeer.exchange.model.DebitWalletCaller;
import com.finacial.wealth.api.fxpeer.exchange.model.ManageFeesConfigReq;
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

    public OrderService(OrderRepository orders, OfferRepository offers,
            TransactionServiceProxies transactionServiceProxies,
            UttilityMethods utilService, RegWalletInfoRepository regWalletInfoRepository,
            AddAccountDetailsRepo addAccountDetailsRepo,
            ProfilingProxies profilingProxies, WalletTransactionsDetailsRepo walletTransactionsDetailsRepo,
            WalletIndivTransactionsDetailsRepo walletIndivTransactionsDetailsRepo,
            AppConfigRepo appConfigRepo, PeerToPeerFxReferralRepo peerToPeerFxReferralRepo,
            ObjectMapper mapper) {
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
    }

    private ResponseEntity<ApiResponseModel> bad(ApiResponseModel res, String msg, int statusCode) {
        res.setStatusCode(statusCode);
        res.setDescription(msg);

        return new ResponseEntity<>(res, HttpStatus.OK);

        // return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(res);
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
            BaseResponse bResPin = new BaseResponse();
            WalletNo wSend = new WalletNo();
            wSend.setPin(rq.getPin());

            wSend.setWalletId(getRec.get().getWalletId());
            bResPin = transactionServiceProxies.validatePin(wSend, auth);
            if (bResPin.getStatusCode() != 200) {
                return bad(res, bResPin.getDescription(), bResPin.getStatusCode());
            }

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
                        AddAccountObj seObj = new AddAccountObj();
                        seObj.setCountry(getWa.getCountryName());
                        seObj.setCountryCode(getWa.getCountryCode());
                        seObj.setWalletId(getRec.get().getWalletId());
                        bRes = profilingProxies.addOtherAccount(seObj, auth);
                        if (bRes.getStatusCode() != 200) {
                            return bad(res, bRes.getDescription(), bRes.getStatusCode());
                        }
                        accountNumber = (String) bRes.getData()
                                .get("accountNumber");
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
                        AddAccountObj seObj = new AddAccountObj();
                        seObj.setCountry(getWa.getCountryName());
                        seObj.setCountryCode(getWa.getCountryCode());
                        seObj.setWalletId(getRec.get().getWalletId());
                        bRes = profilingProxies.addOtherAccount(seObj, auth);

                        if (bRes.getStatusCode() != 200) {
                            return bad(res, bRes.getDescription(), bRes.getStatusCode());
                        }
                        accountNumber = (String) bRes.getData()
                                .get("accountNumber");
                    }
                }

            }

            String sellerAcctNumber = null;

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

                } else {
                    for (AddAccountDetails getWa : getSellerAcct) {
                        if (getWa.getCurrencyCode().equals(off.get(0).getCurrencyReceive().toString())) {
                            sellerAcctNumber = getWa.getAccountNumber();
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

            //debit the buyer
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

            if (debitBuyerAcct.getStatusCode() == 200) {
                DebitWalletCaller debGLCredit = new DebitWalletCaller();
                debGLCredit.setAuth(off.get(0).getCurrencyReceive().toString());
                debGLCredit.setFees("0.00");
                debGLCredit.setFinalCHarges(receiveAmount.toString());
                debGLCredit.setNarration(rqD.getNarration());
                List<AppConfig> getAppConf = appConfigRepo.findByConfigName(off.get(0).getCurrencyReceive().toString());
                String GGL_ACCOUNT = null;
                String GGL_CODE = null;
                for (AppConfig getConfDe : getAppConf) {
                    if (getConfDe.getConfigName().equals(off.get(0).getCurrencyReceive().toString())) {
                        GGL_ACCOUNT = getConfDe.getConfigValue();
                        GGL_CODE = off.get(0).getCurrencyReceive().toString();
                    }
                }

                //debGLCredit.setPhoneNumber(utilService.decryptData(utilService.getSETTING_KEY_WALLET_SYSTEM_SYSTEM_GG_NIG()));
                debGLCredit.setPhoneNumber(utilService.decryptData(GGL_ACCOUNT));
                debGLCredit.setTransAmount(receiveAmount.toString());
                debGLCredit.setTransactionId(transactionId + "-" + GGL_CODE);

                System.out.println(" debitAcct_GL REQUEST ::::::::::::::::  %S  " + new Gson().toJson(debGLCredit));

                BaseResponse debitAcct_GLRes = transactionServiceProxies.debitCustomerWithType(debGLCredit, off.get(0).getCurrencyReceive().toString(), auth);
                System.out.println(" debitAcct_GLRes RESPONSE ::::::::::::::::  %S  " + new Gson().toJson(debitAcct_GLRes));

                //logger.info(" debitAcct GL for buyerleg response ::::::::::::::::::::: ", debitAcct_GL);
                //CREDIT SELLER
                CreditWalletCaller rqC = new CreditWalletCaller();
                rqC.setAuth("Seller");
                rqC.setFees("00");
                rqC.setFinalCHarges(receiveAmount.toString());
                rqC.setNarration(off.get(0).getCurrencyReceive() + "_Deposit");
                rqC.setPhoneNumber(sellerAcctNumber);
                rqC.setTransAmount(receiveAmount.toString());
                rqC.setTransactionId(transactionId + "-DEPOSIT");

                System.out.println(" CREDIT SELLER Credit REQUEST  ::::::::::::::::  %S  " + new Gson().toJson(rqC));

                BaseResponse creditSellerAcct = transactionServiceProxies.creditCustomerWithType(rqC, "CUSTOMER", auth);

                System.out.println(" CREDIT SELLER Credit Response from core ::::::::::::::::  %S  " + new Gson().toJson(creditSellerAcct));
                if (creditSellerAcct.getStatusCode() == 200) {

                    // Credit BAAS NGN_GL
                    CreditWalletCaller gLCredit = new CreditWalletCaller();
                    gLCredit.setAuth(off.get(0).getCurrencyReceive().toString());
                    gLCredit.setFees("0.00");
                    gLCredit.setFinalCHarges(receiveAmount.toString());
                    gLCredit.setNarration(off.get(0).getCurrencyReceive() + "_Deposit");
                    //gLCredit.setPhoneNumber(utilService.decryptData(utilService.getSETTING_KEY_WALLET_SYSTEM_SYSTEM_GG_NIG()));
                    gLCredit.setPhoneNumber(utilService.decryptData(GGL_ACCOUNT));
                    gLCredit.setTransAmount(receiveAmount.toString());
                    gLCredit.setTransactionId(rqC.getTransactionId() + "-" + off.get(0).getCurrencyReceive().toString() + "-DEPOSIT");

                    System.out.println(" CREDIT GL SLLER LEG REQUEST  ::::::::::::::::  %S  " + new Gson().toJson(gLCredit));

                    BaseResponse creditAcctNGN_GL = transactionServiceProxies.creditCustomerWithType(gLCredit, GGL_CODE + "_GL", auth);

                    System.out.println(" CREDIT GL SLLER LEG Response from core ::::::::::::::::  %S  " + new Gson().toJson(creditAcctNGN_GL));

                }

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
                Order orddd = buyNow(off.get(0).getId(), setAmount, walletId, 0L, rq.getOfferCorrelationId(),
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

            } else {
                return bad(res, "Transaction failed", debitBuyerAcct.getStatusCode());
            }

            return ResponseEntity.ok(res);

        } /*catch (BusinessException be) {
            be.printStackTrace();
            return bad(res, be.getMessage(), 500);
        } */ catch (Exception ex) {
            ex.printStackTrace();
            return bad(res, "An error occurred, please try again.", 500);
        }
    }

    @Transactional
    public Order buyNow(long offerId, BigDecimal amount, String buyerId, long lockTtlSeconds,
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
        rqC.setTransactionId(transactionId + "-DEPOSIT");

        System.out.println(" creditBuyerAcct REQ  ::::::::::::::::  %S  " + new Gson().toJson(rqC));

        BaseResponse creditBuyerAcct = transactionServiceProxies.creditCustomerWithType(rqC, "CUSTOMER", auth);

        System.out.println(" creditBuyerAcct Response from core ::::::::::::::::  %S  " + new Gson().toJson(creditBuyerAcct));
        if (creditBuyerAcct.getStatusCode() == 200) {

            // Credit BAAS NGN_GL
            CreditWalletCaller GLCredit = new CreditWalletCaller();
            GLCredit.setAuth("Receiver");
            GLCredit.setFees("0.00");
            GLCredit.setFinalCHarges(rqC.getFinalCHarges());
            GLCredit.setNarration(rqC.getNarration());
            //GLCredit.setPhoneNumber(utilService.decryptData(utilService.getSETTING_KEY_WALLET_SYSTEM_SYSTEM_GG_CAD()));
            GLCredit.setPhoneNumber(utilService.decryptData(GGL_ACCOUNT));
            GLCredit.setTransAmount(rqC.getFinalCHarges());
            GLCredit.setTransactionId(rqC.getTransactionId() + "-DEPOSIT-GL");

            System.out.println(" creditAcct_GL buyer legCredit REQ  ::::::::::::::::  %S  " + new Gson().toJson(GLCredit));

            BaseResponse creditAcct_GL = transactionServiceProxies.creditCustomerWithType(GLCredit, off.get(0).getCurrencySell().toString(), auth);

            System.out.println(" credit GL for buyer legCredit Response from core ::::::::::::::::  %S  " + new Gson().toJson(creditAcct_GL));

        }
        // }

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
