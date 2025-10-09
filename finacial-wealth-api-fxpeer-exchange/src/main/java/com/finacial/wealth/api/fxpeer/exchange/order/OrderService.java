/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.finacial.wealth.api.fxpeer.exchange.order;

/**
 *
 * @author olufemioshin
 */
import com.finacial.wealth.api.fxpeer.exchange.common.BusinessException;
import com.finacial.wealth.api.fxpeer.exchange.common.NotFoundException;
import com.finacial.wealth.api.fxpeer.exchange.common.OfferStatus;
import com.finacial.wealth.api.fxpeer.exchange.domain.AddAccountDetails;
import com.finacial.wealth.api.fxpeer.exchange.domain.AddAccountDetailsRepo;
import com.finacial.wealth.api.fxpeer.exchange.domain.AppConfig;
import com.finacial.wealth.api.fxpeer.exchange.domain.AppConfigRepo;
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
import com.finacial.wealth.api.fxpeer.exchange.util.GlobalMethods;
import com.finacial.wealth.api.fxpeer.exchange.util.UttilityMethods;
import com.finacial.wealth.api.fxpeer.exchange.fx.p2.p.wallet.WalletIndivTransactionsDetails;
import com.finacial.wealth.api.fxpeer.exchange.fx.p2.p.wallet.WalletIndivTransactionsDetailsRepo;
import com.finacial.wealth.api.fxpeer.exchange.fx.p2.p.wallet.WalletTransactionsDetails;
import com.finacial.wealth.api.fxpeer.exchange.fx.p2.p.wallet.WalletTransactionsDetailsRepo;
import com.google.gson.Gson;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
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

    public OrderService(OrderRepository orders, OfferRepository offers,
            TransactionServiceProxies transactionServiceProxies,
            UttilityMethods utilService, RegWalletInfoRepository regWalletInfoRepository,
            AddAccountDetailsRepo addAccountDetailsRepo,
            ProfilingProxies profilingProxies, WalletTransactionsDetailsRepo walletTransactionsDetailsRepo,
            WalletIndivTransactionsDetailsRepo walletIndivTransactionsDetailsRepo,
            AppConfigRepo appConfigRepo) {
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
    }

    private ResponseEntity<ApiResponseModel> bad(ApiResponseModel res, String msg, int statusCode) {
        res.setStatusCode(statusCode);
        res.setDescription(msg);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(res);
    }

    public ResponseEntity<ApiResponseModel> createOrderCaller(BuyOfferNow rq, String auth) {
        final ApiResponseModel res = new ApiResponseModel();

        try {
            BigDecimal setAmount = new BigDecimal(rq.getAmount());

            String phoneNumber = utilService.getClaimFromJwt(auth, "phoneNumber"); // preferred if your JWT has sellerId
            Optional<RegWalletInfo> getRec = regWalletInfoRepository.findByPhoneNumber(phoneNumber);
            //validate pin
            BaseResponse bResPin = new BaseResponse();
            WalletNo wSend = new WalletNo();
            wSend.setPin(rq.getPin());

            wSend.setWalletId(getRec.get().getWalletId());
            bResPin = transactionServiceProxies.validatePin(wSend);
            if (bResPin.getStatusCode() != 200) {
                return bad(res, bResPin.getDescription(), bResPin.getStatusCode());
            }

            //check if cus has currency currency
            List<AddAccountDetails> getAdDe = addAccountDetailsRepo.findByWalletIdrData(getRec.get().getWalletId());

            BaseResponse bRes = new BaseResponse();
            List<Offer> off = offers.findByCorrelationIdData(rq.getOfferCorrelationId());
            if (off.size() <= 0) {
                return bad(res, "Invalid Offer CorrelationId!", 400);
            }
            if (!off.get(0).getStatus().equals(OfferStatus.LIVE)) {
                return bad(res, "Offer no longer available!", 400);
            }

            List<WalletTransactionsDetails> getTransDe = walletTransactionsDetailsRepo.findByCorrelationId(rq.getOfferCorrelationId());

            if (getTransDe.size() <= 0) {
                logger.info(" correlationId does not exist in WalletTransactionsDetails {}  ::::::::::::::::::::: ", rq.getOfferCorrelationId());
                return bad(res, "Invalid Offer CorrelationId!", 400);
            }

            if (setAmount.compareTo(getTransDe.get(0).getAvailableQuantity()) > 0) {
                System.out.println("Amount to buy is more than available quantity for sales!");
                return bad(res, "Amount to buy is more than available quantity for sales!", 400);
            }

            for (AddAccountDetails getWa : getAdDe) {

                logger.info(" off.get(0).getCurrencyReceive() {}  ::::::::::::::::::::: ", off.get(0).getCurrencyReceive());
                logger.info(" off.get(0).getCurrencySell() {}  ::::::::::::::::::::: ", off.get(0).getCurrencySell());

                String wallCurrencyCode = getWa.getCurrencyCode() == null ? "" : getWa.getCurrencyCode();

                if (!wallCurrencyCode.equals(off.get(0).getCurrencyReceive())) {
                    if (!"CA".equals(off.get(0).getCurrencyReceive())) {
                        //create account
                        AddAccountObj seObj = new AddAccountObj();
                        seObj.setCountry(getWa.getCountryName());
                        seObj.setCountryCode(getWa.getCountryCode());
                        seObj.setWalletId(getRec.get().getWalletId());
                        bRes = profilingProxies.addOtherAccount(seObj);
                        if (bRes.getStatusCode() != 200) {
                            return bad(res, STATUS_CODE_NIGERIA_ONBOARDING_FLOW_DESCRIPTION, STATUS_CODE_NIGERIA_ONBOARDING_FLOW_CODE);
                        }
                    }
                }
                if (!wallCurrencyCode.equals(off.get(0).getCurrencySell())) {
                    if (!"CA".equals(off.get(0).getCurrencyReceive())) {
                        //create account
                        AddAccountObj seObj = new AddAccountObj();
                        seObj.setCountry(getWa.getCountryName());
                        seObj.setCountryCode(getWa.getCountryCode());
                        seObj.setWalletId(getRec.get().getWalletId());
                        bRes = profilingProxies.addOtherAccount(seObj);
                        if (bRes.getStatusCode() != 200) {
                            return bad(res, STATUS_CODE_NIGERIA_ONBOARDING_FLOW_DESCRIPTION, STATUS_CODE_NIGERIA_ONBOARDING_FLOW_CODE);
                        }
                    }
                }

            }
            String walletId = getRec.get().getWalletId();
            // 0) Basic request checks
            if (rq == null) {
                return bad(res, "Empty request.", 400);
            }
            if (isBlank(rq.getAmount())) {
                return bad(res, "amount is required!", 400);
            }

            BaseResponse getRess = this.validateAmountInRange(rq.getAmount(), off.get(0).getMinAmount(), off.get(0).getMaxAmount());
            if (getRess.getStatusCode() != 200) {
                return bad(res, getRess.getDescription(), getRess.getStatusCode());
            }

            if (setAmount.compareTo(off.get(0).getQtyAvailable()) > 0) {
                String reDec = "Requested amount: " + setAmount + " is more than available quantity: " + off.get(0).getQtyAvailable();
                //bRes.setDescription("amount must be <= " + maxAmount);
                return bad(res, reDec, 400);

            }

            List<AddAccountDetails> getAdDeNew = addAccountDetailsRepo.findByWalletIdrData(walletId);
            String accountNumber = null;

            for (AddAccountDetails getWa : getAdDeNew) {
                if (getWa.getCurrencyCode().equals(off.get(0).getCurrencyReceive())) {
                    //validate account balance
                    accountNumber = getWa.getAccountNumber();
                    WalletInfoValiAcctBal wVal = new WalletInfoValiAcctBal();
                    wVal.setAccountNumber(accountNumber);
                    wVal.setRequestedAmount(setAmount);
                    wVal.setWalletId(getRec.get().getWalletId());
                    BaseResponse bResss = transactionServiceProxies.validateAccountBalnce(wVal);

                    if (bResss.getStatusCode() != 200) {
                        return bad(res, bResss.getDescription(), bResss.getStatusCode());
                    }
                }

            }

            String transactionId = String.valueOf(GlobalMethods.generateTransactionId());

            ManageFeesConfigReq mFeee = new ManageFeesConfigReq();
            mFeee.setAmount(rq.getAmount());
            mFeee.setTransType("buylisting");
            mFeee.setCurrencyCode(off.get(0).getCurrencyReceive().toString());

            BaseResponse mConfig = utilService.getFeesConfig(mFeee);

            if (mConfig.getStatusCode() != 200) {
                return bad(res, mConfig.getDescription(), mConfig.getStatusCode());
            }

            String pFeesString = (String) mConfig.getData()
                    .get("fees");

            BigDecimal receiveAmount = setAmount.multiply(off.get(0).getRate()).setScale(2, RoundingMode.HALF_UP);

            BigDecimal finCharges = receiveAmount.add(new BigDecimal(pFeesString));

            //debit the buyer
            DebitWalletCaller rqD = new DebitWalletCaller();
            rqD.setAuth("Buyer");
            rqD.setFees(pFeesString);
            rqD.setFinalCHarges(finCharges.toString());
            rqD.setNarration(off.get(0).getCurrencyReceive() + "_Withdrawal");
            rqD.setPhoneNumber(accountNumber);
            rqD.setTransAmount(receiveAmount.toString());
            rqD.setTransactionId(transactionId);
            BaseResponse debitBuyerAcct = transactionServiceProxies.debitCustomerWithType(rqD, "CUSTOMER");

            logger.info(" debitBuyerAcct BUYER  response ::::::::::::::::::::: ", debitBuyerAcct);

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
                debGLCredit.setTransactionId(transactionId);

                BaseResponse debitAcct_GL = transactionServiceProxies.debitCustomerWithType(debGLCredit, off.get(0).getCurrencyReceive().toString());
                logger.info(" debitAcct GL for buyerleg response ::::::::::::::::::::: ", debitAcct_GL);

                //CREDIT SELLER
                List<AddAccountDetails> getSellerAcct = addAccountDetailsRepo.findByWalletIdrData(walletId);
                String sellerAcctNumber = null;

                for (AddAccountDetails getWa : getSellerAcct) {
                    if (getWa.getCurrencyCode().equals(off.get(0).getCurrencyReceive())) {
                        sellerAcctNumber = getWa.getAccountNumber();
                    }
                }

                CreditWalletCaller rqC = new CreditWalletCaller();
                rqC.setAuth("Seller");
                rqC.setFees("00");
                rqC.setFinalCHarges(receiveAmount.toString());
                rqC.setNarration(off.get(0).getCurrencyReceive() + "_Deposit");
                rqC.setPhoneNumber(sellerAcctNumber);
                rqC.setTransAmount(receiveAmount.toString());
                rqC.setTransactionId(transactionId);

                BaseResponse creditSellerAcct = transactionServiceProxies.creditCustomerWithType(rqC, "CUSTOMER");

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
                    gLCredit.setTransAmount(rqC.getTransAmount());
                    gLCredit.setTransactionId(rqC.getTransactionId());

                    BaseResponse creditAcctNGN_GL = transactionServiceProxies.creditCustomerWithType(gLCredit, GGL_CODE + "_GL");

                    System.out.println(" CREDIT GL SLLER LEG Response from core ::::::::::::::::  %S  " + new Gson().toJson(creditAcctNGN_GL));

                }

                //augument WalletTransactionsDetails
                WalletTransactionsDetails getWalDeupdate = walletTransactionsDetailsRepo.findByCorrelationIdUpdated(rq.getOfferCorrelationId());
                BigDecimal availableQuantity = getWalDeupdate.getAvailableQuantity();

                getWalDeupdate.setLastModifiedDate(Instant.now());
                getWalDeupdate.setBuyerId(walletId);
                getWalDeupdate.setBuyerName(getRec.get().getFullName());

                getWalDeupdate.setAvailableQuantity(availableQuantity.subtract(setAmount));
                walletTransactionsDetailsRepo.save(getWalDeupdate);

                WalletIndivTransactionsDetails wallInd = new WalletIndivTransactionsDetails();
                wallInd.setAvailableQuantity(getTransDe.get(0).getAvailableQuantity());
                wallInd.setTotalQuantityCreated(getTransDe.get(0).getTotalQuantityCreated());
                wallInd.setBuyerAccount(accountNumber);
                wallInd.setBuyerId(getWalDeupdate.getBuyerId());
                wallInd.setBuyerName(getWalDeupdate.getBuyerName());
                wallInd.setCorrelationId(rq.getOfferCorrelationId());
                wallInd.setCreatedBy("System");
                wallInd.setCreatedDate(Instant.now());
                wallInd.setCurrencyToBuy(getWalDeupdate.getCurrencyToBuy());
                wallInd.setCurrencyToSell(getWalDeupdate.getCurrencyToSell());
                wallInd.setSellerName(getRec.get().getFullName());
                wallInd.setTransactionId(transactionId);
                wallInd.setSellerId(getWalDeupdate.getSellerId());
                wallInd.setReceiverAmount(receiveAmount);
                wallInd.setCorrelationId(rq.getOfferCorrelationId());

                walletIndivTransactionsDetailsRepo.save(wallInd);
                Order orddd = buyNow(off.get(0).getId(), setAmount, walletId, 0L, rq.getOfferCorrelationId(), String.valueOf(off.get(0).getSellerUserId()), "0.00", transactionId);
                /*
                 buyNow(long offerId, BigDecimal amount, long buyerId, long lockTtlSeconds,
            String correlId, String sellerId, String fees, String transactionId)
                 */

                // 4) Success
                res.setStatusCode(200);
                res.setDescription("Offer created.");
                res.setData(orddd); // or map to a lightweight DTO

            } else {
                return bad(res, "Transaction failed", debitBuyerAcct.getStatusCode());
            }

            return ResponseEntity.ok(res);

        } catch (BusinessException be) {
            return bad(res, be.getMessage(), 500);
        } catch (Exception ex) {
            ex.printStackTrace();
            return bad(res, "An error occurred, please try again.", 500);
        }
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
            bRes.setDescription("amount must be >= " + minAmount);

        }
        if (maxAmount != null && amount.compareTo(maxAmount) > 0) {
            bRes.setStatusCode(400);
            bRes.setDescription("amount must be <= " + maxAmount);

            // throw new IllegalArgumentException("amount must be <= " + maxAmount);
        }

        return bRes;
    }

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    @Transactional
    public Order buyNow(long offerId, BigDecimal amount, String buyerId, long lockTtlSeconds,
            String correlId, String sellerId, String fees, String transactionId)
            throws NoSuchAlgorithmException, NoSuchPaddingException,
            InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        //Offer off = offers.findById(offerId).orElseThrow(() -> new NotFoundException("Offer not found"));
        List<Offer> off = offers.findByCorrelationIdData(correlId);
        if (off.size() <= 0) {
            // return bad(res, "Invalid Offer CorrelationId!", 400);
            throw new BusinessException("Invalid Offer CorrelationId!");
        }

        List<AddAccountDetails> getBuyyAcct = addAccountDetailsRepo.findByWalletIdrData(String.valueOf(buyerId));

        List<AddAccountDetails> getSellerAcct = addAccountDetailsRepo.findByWalletIdrData(String.valueOf(sellerId));

        String getSellerAccttAcct = null;

        for (AddAccountDetails getWa : getSellerAcct) {
            if (getWa.getCurrencyCode().equals(off.get(0).getCurrencySell())) {
                getSellerAccttAcct = getWa.getAccountNumber();
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

        //debit the buyer
        DebitWalletCaller rqD = new DebitWalletCaller();
        rqD.setAuth("Seller");
        rqD.setFees(fees);
        rqD.setFinalCHarges(amount.toString());
        rqD.setNarration(off.get(0).getCurrencySell() + "_Withdrawal");
        rqD.setPhoneNumber(getSellerAccttAcct);
        rqD.setTransAmount(amount.toString());
        rqD.setTransactionId(transactionId);
        BaseResponse debitSellerAcct = transactionServiceProxies.debitCustomerWithType(rqD, "CUSTOMER");

        logger.info(" debitSellerAcct   response ::::::::::::::::::::: ", debitSellerAcct);

        if (debitSellerAcct.getStatusCode() == 200) {
            DebitWalletCaller debGLCredit = new DebitWalletCaller();
            debGLCredit.setAuth("Buyer");
            debGLCredit.setFees("0.00");
            debGLCredit.setFinalCHarges(rqD.getFinalCHarges());
            debGLCredit.setNarration(rqD.getNarration());
           // debGLCredit.setPhoneNumber(utilService.decryptData(utilService.getSETTING_KEY_WALLET_SYSTEM_SYSTEM_GG_CAD()));
            debGLCredit.setPhoneNumber(utilService.decryptData(GGL_ACCOUNT));
            debGLCredit.setTransAmount(rqD.getTransAmount());
            debGLCredit.setTransactionId(transactionId);

            BaseResponse debit_GL = transactionServiceProxies.debitCustomerWithType(debGLCredit, off.get(0).getCurrencySell().toString());
            logger.info(" debit GL for seller leg  response ::::::::::::::::::::: ", debit_GL);

            //CREDIT BUYER
            String getBuyyAcctAcct = null;

            for (AddAccountDetails getWa : getBuyyAcct) {
                if (getWa.getCurrencyCode().equals(off.get(0).getCurrencySell())) {
                    getBuyyAcctAcct = getWa.getAccountNumber();
                }
            }

            CreditWalletCaller rqC = new CreditWalletCaller();
            rqC.setAuth("Buyer");
            rqC.setFees("00");
            rqC.setFinalCHarges(amount.toString());
            rqC.setNarration(off.get(0).getCurrencySell() + "_Deposit");
            rqC.setPhoneNumber(getBuyyAcctAcct);
            rqC.setTransAmount(amount.toString());
            rqC.setTransactionId(transactionId);

            BaseResponse creditBuyerAcct = transactionServiceProxies.creditCustomerWithType(rqC, "CUSTOMER");

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
                GLCredit.setTransAmount(rqC.getTransAmount());
                GLCredit.setTransactionId(rqC.getTransactionId());

                BaseResponse creditAcct_GL = transactionServiceProxies.creditCustomerWithType(GLCredit, off.get(0).getCurrencySell().toString());

                System.out.println(" credit GL for buyer legCredit Response from core ::::::::::::::::  %S  " + new Gson().toJson(creditAcct_GL));

            }
        }

        Offer offUp = offers.findByCorrelationIdDataUpdate(correlId);

// Reserve available
        offUp.setQtyAvailable(off.get(0).getQtyAvailable().subtract(amount));
        offers.save(offUp);

// Compute receive = amount * rate
        BigDecimal receive = amount.multiply(off.get(0).getRate()).setScale(2, RoundingMode.HALF_UP);

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
