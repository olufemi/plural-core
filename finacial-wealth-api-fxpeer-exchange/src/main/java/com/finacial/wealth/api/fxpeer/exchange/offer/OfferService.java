/*
 * FXPeer Exchange - OfferService (rewritten)
 */
package com.finacial.wealth.api.fxpeer.exchange.offer;

import com.finacial.wealth.api.fxpeer.exchange.domain.RegWalletInfo;
import com.finacial.wealth.api.fxpeer.exchange.domain.RegWalletInfoRepository;
import com.finacial.wealth.api.fxpeer.exchange.common.BusinessException;
import com.finacial.wealth.api.fxpeer.exchange.common.CurrencyCode;
import com.finacial.wealth.api.fxpeer.exchange.common.NotFoundException;
import com.finacial.wealth.api.fxpeer.exchange.common.OfferStatus;
import com.finacial.wealth.api.fxpeer.exchange.domain.AddAccountDetails;
import com.finacial.wealth.api.fxpeer.exchange.domain.AddAccountDetailsRepo;
import com.finacial.wealth.api.fxpeer.exchange.feign.ProfilingProxies;
import com.finacial.wealth.api.fxpeer.exchange.feign.TransactionServiceProxies;
import com.finacial.wealth.api.fxpeer.exchange.ledger.LedgerClient;

import com.finacial.wealth.api.fxpeer.exchange.model.AddAccountObj;
import com.finacial.wealth.api.fxpeer.exchange.model.ApiResponseModel;
import com.finacial.wealth.api.fxpeer.exchange.model.BaseResponse;
import com.finacial.wealth.api.fxpeer.exchange.model.ManageFeesConfigReq;
import com.finacial.wealth.api.fxpeer.exchange.model.WalletNo;
import com.finacial.wealth.api.fxpeer.exchange.util.GlobalMethods;
import com.finacial.wealth.api.fxpeer.exchange.util.UttilityMethods;

import io.micrometer.common.lang.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Service
public class OfferService {

    private final OfferRepository repo;
    private final LedgerClient ledger;
    private final UttilityMethods utilService;
    private final RegWalletInfoRepository regWalletInfoRepository;
    private final AddAccountDetailsRepo addAccountDetailsRepo;
    private final ProfilingProxies profilingProxies;
    private final TransactionServiceProxies transactionServiceProxies;

    private static final ZoneId LAGOS = ZoneId.of("Africa/Lagos");
    private static final DateTimeFormatter EXPIRY_DMY
            = DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.ENGLISH);

    public OfferService(OfferRepository repo,
            LedgerClient ledger,
            UttilityMethods utilService,
            RegWalletInfoRepository regWalletInfoRepository,
            AddAccountDetailsRepo addAccountDetailsRepo,
            ProfilingProxies profilingProxies,
            TransactionServiceProxies transactionServiceProxies) {
        this.repo = repo;
        this.ledger = ledger;
        this.utilService = utilService;
        this.regWalletInfoRepository = regWalletInfoRepository;
        this.addAccountDetailsRepo = addAccountDetailsRepo;
        this.profilingProxies = profilingProxies;
        this.transactionServiceProxies = transactionServiceProxies;
    }

    @Transactional(readOnly = true)
    public Offer getOffer(long offerId, long sellerId) {
        return repo.findByIdAndSellerUserId(offerId, sellerId)
                .orElseThrow(() -> new BusinessException("Offer not found or not owned by user"));
    }

    @Transactional(readOnly = true)
    public Page<Offer> getMyOffers(long sellerId, @Nullable OfferStatus status, Pageable pageable) {
        if (status != null) {
            return repo.findBySellerUserIdAndStatus(sellerId, status, pageable);
        }
        return repo.findBySellerUserId(sellerId, pageable);
    }

    public ResponseEntity<ApiResponseModel> getMyOffersCaller(String auth,
            Pageable pageable) {

        int statusCode = 500;

        ApiResponseModel resp = new ApiResponseModel();
        try {
            statusCode = 400;
            String phoneNumber = utilService.getClaimFromJwt(auth, "phoneNumber"); // preferred if your JWT has sellerId
            Optional<RegWalletInfo> getRec = regWalletInfoRepository.findByPhoneNumber(phoneNumber);
            long sellerId = Long.valueOf(getRec.get().getWalletId());
            OfferStatus status = null;
            Page<Offer> page = getMyOffers(sellerId, status, pageable);

            Page<OfferListItemDto> dtoPage = page.map(OfferMapper::toListItem);

            if (dtoPage.isEmpty()) {
                resp.setStatusCode(statusCode);
                resp.setDescription("No offers found.");
                return ResponseEntity.ok(resp);

            }
            resp.setData(Map.of(
                    "items", dtoPage.getContent(),
                    "page", dtoPage.getNumber(),
                    "size", dtoPage.getSize(),
                    "totalPages", dtoPage.getTotalPages(),
                    "totalElements", dtoPage.getTotalElements(),
                    "hasNext", dtoPage.hasNext(),
                    "hasPrevious", dtoPage.hasPrevious()
            ));
            resp.setDescription("Offers fetched successfully.");
            resp.setStatusCode(200);
            return ResponseEntity.ok(resp);

        } catch (Exception e) {
            resp.setStatusCode(500);
            resp.setDescription("Failed to fetch offers: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(resp);
        }
    }

    @Transactional(readOnly = true)
    public Page<Offer> getAllOffersExceptLoggedInUser(long sellerId, @Nullable OfferStatus status, Pageable pageable) {

        return repo.findMarketExcludingSeller(sellerId, status, pageable);

    }

    @Transactional(readOnly = true)
    public Page<Offer> getAllOffers(@Nullable OfferStatus status, Pageable pageable) {

        return repo.findMarket(status, pageable);

    }

    public ResponseEntity<ApiResponseModel> getAllOffersExceptLoggedInUserCaller(String auth,
            Pageable pageable) {

        int statusCode = 500;

        ApiResponseModel resp = new ApiResponseModel();
        try {
            statusCode = 400;
            String phoneNumber = utilService.getClaimFromJwt(auth, "phoneNumber"); // preferred if your JWT has sellerId
            Optional<RegWalletInfo> getRec = regWalletInfoRepository.findByPhoneNumber(phoneNumber);
            long sellerId = Long.valueOf(getRec.get().getWalletId());
            OfferStatus status = null;
            Page<Offer> page = getAllOffersExceptLoggedInUser(sellerId, status.LIVE, pageable);

            Page<OfferListItemDto> dtoPage = page.map(OfferMapper::toListItem);

            if (dtoPage.isEmpty()) {
                resp.setStatusCode(statusCode);
                resp.setDescription("No offers found.");
                return ResponseEntity.ok(resp);

            }
            resp.setData(Map.of(
                    "items", dtoPage.getContent(),
                    "page", dtoPage.getNumber(),
                    "size", dtoPage.getSize(),
                    "totalPages", dtoPage.getTotalPages(),
                    "totalElements", dtoPage.getTotalElements(),
                    "hasNext", dtoPage.hasNext(),
                    "hasPrevious", dtoPage.hasPrevious()
            ));
            resp.setDescription("Offers fetched successfully.");
            resp.setStatusCode(200);
            return ResponseEntity.ok(resp);

        } catch (Exception e) {
            resp.setStatusCode(500);
            resp.setDescription("Failed to fetch offers: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(resp);
        }
    }

    public ResponseEntity<ApiResponseModel> getAllOffersCaller(String auth,
            Pageable pageable) {

        int statusCode = 500;

        ApiResponseModel resp = new ApiResponseModel();
        try {
            statusCode = 400;
            String phoneNumber = utilService.getClaimFromJwt(auth, "phoneNumber"); // preferred if your JWT has sellerId
            //Optional<RegWalletInfo> getRec = regWalletInfoRepository.findByPhoneNumber(phoneNumber);

            OfferStatus status = null;
            Page<Offer> page = getAllOffers(status.LIVE, pageable);

            Page<OfferListItemDto> dtoPage = page.map(OfferMapper::toListItem);

            if (dtoPage.isEmpty()) {
                resp.setStatusCode(statusCode);
                resp.setDescription("No offers found.");
                return ResponseEntity.ok(resp);

            }
            resp.setData(Map.of(
                    "items", dtoPage.getContent(),
                    "page", dtoPage.getNumber(),
                    "size", dtoPage.getSize(),
                    "totalPages", dtoPage.getTotalPages(),
                    "totalElements", dtoPage.getTotalElements(),
                    "hasNext", dtoPage.hasNext(),
                    "hasPrevious", dtoPage.hasPrevious()
            ));
            resp.setDescription("Offers fetched successfully.");
            resp.setStatusCode(200);
            return ResponseEntity.ok(resp);

        } catch (Exception e) {
            resp.setStatusCode(500);
            resp.setDescription("Failed to fetch offers: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(resp);
        }
    }

    public ResponseEntity<ApiResponseModel> updateOfferCaller(UpdateOfferCallerReq rq, String auth) {
        final ApiResponseModel res = new ApiResponseModel();

        try {

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

            long logSellerId = Long.valueOf(getRec.get().getWalletId());

            List<Offer> offerDe = repo.findByCorrelationIdData(rq.getCorrelationId());
            if (offerDe.size() <= 0) {
                return bad(res, "Offer does not exist!", 400);
            }

            Offer updateOffer = updateRate(offerDe.get(0).getId(), new BigDecimal(rq.getNewRate()), logSellerId);
            res.setStatusCode(200);
            res.setDescription("Offer created.");
            res.setData(updateOffer); // or map to a lightweight DTO
            return ResponseEntity.ok(res);

        } catch (BusinessException be) {
            return bad(res, be.getMessage(), 500);
        } catch (Exception ex) {
            ex.printStackTrace();
            return bad(res, "An error occurred, please try again.", 500);
        }
    }

    public ResponseEntity<ApiResponseModel> createOfferCaller(CreateOfferCaller rq, String auth) {
        final ApiResponseModel res = new ApiResponseModel();

        try {

            String phoneNumber = utilService.getClaimFromJwt(auth, "phoneNumber"); // preferred if your JWT has sellerId
            Optional<RegWalletInfo> getRec = regWalletInfoRepository.findByPhoneNumber(phoneNumber);
            //validate pin
            BaseResponse bResPin = new BaseResponse();
            WalletNo wSend = new WalletNo();
            wSend.setPin(rq.getPin());

            // 0) Basic request checks
            if (rq == null) {
                return bad(res, "Empty request.", 400);
            }
            if (isBlank(rq.getCurrencySell()) || isBlank(rq.getCurrencyReceive())
                    || isBlank(rq.getRate()) || isBlank(rq.getQtyTotal()) || isBlank(rq.getExpiredAt())) {
                return bad(res, "currencySell, currencyReceive, rate, qtyTotal, expiredAt are required.", 400);
            }

            wSend.setWalletId(getRec.get().getWalletId());
            bResPin = transactionServiceProxies.validatePin(wSend);
            if (bResPin.getStatusCode() != 200) {
                return bad(res, bResPin.getDescription(), bResPin.getStatusCode());
            }
            if (rq.getCurrencyReceive().equals(rq.getCurrencySell())) {
                return bad(res, "Country code mismatch!", 400);
            }
            //check if cus has currency currency
            List<AddAccountDetails> getAdDe = addAccountDetailsRepo.findByWalletIdrData(getRec.get().getWalletId());
            BaseResponse bRes = new BaseResponse();
            for (AddAccountDetails getWa : getAdDe) {
                String wallCurrencyCode = getWa.getCurrencyCode() == null ? "" : getWa.getCurrencyCode();

                if (!wallCurrencyCode.equals(rq.getCurrencyReceive())) {
                    if (!"CA".equals(rq.getCurrencyReceive())) {
                        //create account
                        AddAccountObj seObj = new AddAccountObj();
                        seObj.setCountry(getWa.getCountryName());
                        seObj.setCountryCode(getWa.getCountryCode());
                        seObj.setWalletId(getRec.get().getWalletId());
                        bRes = profilingProxies.addOtherAccount(seObj);
                        if (bRes.getStatusCode() != 200) {
                            return bad(res, bRes.getDescription(), bRes.getStatusCode());
                        }
                    }
                }
                if (!wallCurrencyCode.equals(rq.getCurrencySell())) {
                    if (!"CA".equals(rq.getCurrencyReceive())) {
                        //create account
                        AddAccountObj seObj = new AddAccountObj();
                        seObj.setCountry(getWa.getCountryName());
                        seObj.setCountryCode(getWa.getCountryCode());
                        seObj.setWalletId(getRec.get().getWalletId());
                        bRes = profilingProxies.addOtherAccount(seObj);
                        if (bRes.getStatusCode() != 200) {
                            return bad(res, bRes.getDescription(), bRes.getStatusCode());
                        }
                    }
                }
            }
            String sellerId = getRec.get().getWalletId();

            // 1) Normalize inputs
            final CurrencyCode currencySell;
            final CurrencyCode currencyReceive;
            try {
                currencySell = CurrencyCode.valueOf(rq.getCurrencySell().trim().toUpperCase(Locale.ENGLISH));
                currencyReceive = CurrencyCode.valueOf(rq.getCurrencyReceive().trim().toUpperCase(Locale.ENGLISH));
            } catch (IllegalArgumentException iae) {
                return bad(res, "Unsupported currency code(s). Use valid currency code values, e.g., NGN, USD, EUR.", 400);
            }
            if (currencySell == currencyReceive) {
                return bad(res, "currencySell and currencyReceive must differ.", 400);
            }

            final BigDecimal rate;
            final BigDecimal qtyTotal;
            try {
                rate = new BigDecimal(rq.getRate().trim());
                qtyTotal = new BigDecimal(rq.getQtyTotal().trim());
            } catch (NumberFormatException nfe) {
                return bad(res, "rate and qtyTotal must be numeric.", 400);
            }
            if (rate.signum() <= 0) {
                return bad(res, "rate must be > 0.", 400);
            }
            if (qtyTotal.signum() <= 0) {
                return bad(res, "qtyTotal must be > 0.", 400);
            }

            // 2) Parse expiry "dd/MM/yyyy" → Instant (Africa/Lagos end-of-day)
            final Instant expiryAt;
            try {
                LocalDate d = LocalDate.parse(rq.getExpiredAt().trim(), EXPIRY_DMY);
                ZonedDateTime zdt = d.atTime(23, 59, 59).atZone(LAGOS);
                expiryAt = zdt.toInstant();
            } catch (DateTimeParseException dtpe) {
                return bad(res, "Invalid expiry date format dd/MM/yyyy!", 400);
            }
            if (expiryAt.isBefore(Instant.now())) {
                return bad(res, "Expiry time is in the past.", 400);
            }

            BigDecimal setMin = new BigDecimal(rq.getMinAmount());
            BigDecimal setMax = new BigDecimal(rq.getMaxAmount());

            if (this.isMinLeMax(setMin, setMax) == false) {
                return bad(res, "MinAmount must be ≤ MaxAmount.", 400);
            }
            ManageFeesConfigReq mFeee = new ManageFeesConfigReq();
            mFeee.setAmount(setMin.toString());
            mFeee.setTransType("createlisting");
            BaseResponse mConfig = utilService.getFeesConfig(mFeee);

            if (mConfig.getStatusCode() != 200) {
                return bad(res, mConfig.getDescription(), mConfig.getStatusCode());
            }

            // 3) Build domain request and delegate
            CreateOfferRq coreRq = new CreateOfferRq(currencySell, currencyReceive, rate, qtyTotal, expiryAt);
            Offer created = createOffer(coreRq, Long.valueOf(sellerId), setMin, setMax, rq.isShowInTopDeals(), getRec.get().getFirstName());

            // 4) Success
            res.setStatusCode(200);
            res.setDescription("Offer created.");
            res.setData(created); // or map to a lightweight DTO
            return ResponseEntity.ok(res);

        } catch (BusinessException be) {
            return bad(res, be.getMessage(), 500);
        } catch (Exception ex) {
            ex.printStackTrace();
            return bad(res, "An error occurred, please try again.", 500);
        }
    }

    @Transactional
    public Offer createOffer(CreateOfferRq rq, long sellerId, BigDecimal minAmt,
            BigDecimal maxAmt, boolean showInTopDeals, String creators) {
        BaseResponse bRes = new BaseResponse();
        //ledger.ensureWallet(sellerId, rq.getCurrencySell().name());
        // ledger.ensureWallet(sellerId, rq.getCurrencyReceive().name());
        String correlationId = String.valueOf(GlobalMethods.generateTransactionId());
        /*WalletInfo w = ledger.getWallet(sellerId, rq.getCurrencySell().name());
        if (w.available().compareTo(rq.getQtyTotal()) < 0) {
            throw new BusinessException("Insufficient balance to list qtyTotal");
        }*/
        List<AddAccountDetails> getAdDe = addAccountDetailsRepo.findByWalletIdrData(String.valueOf(sellerId));

        WalletInfoValAcct wSend = new WalletInfoValAcct();
        wSend.setAccountNumber(getAdDe.get(0).getAccountNumber());
        wSend.setCorrelationId(correlationId);
        wSend.setCurrencyToBuy(rq.getCurrencySell().toString());
        wSend.setCurrencyToSell(rq.getCurrencySell().toString());
        wSend.setTransactionAmmount(rq.getQtyTotal());
        wSend.setWalletId(String.valueOf(sellerId));

        bRes = transactionServiceProxies.createOffervalidateAccount(wSend);
        if (bRes.getStatusCode() != 200) {
            throw new BusinessException(bRes.getDescription());
        }

        Offer o = new Offer();
        o.setSellerUserId(sellerId);
        o.setCurrencySell(rq.getCurrencySell());
        o.setCurrencyReceive(rq.getCurrencyReceive());
        o.setRate(rq.getRate());
        o.setQtyTotal(rq.getQtyTotal());
        o.setQtyAvailable(rq.getQtyTotal());
        o.setStatus(OfferStatus.LIVE);
        o.setMaxAmount(maxAmt);
        o.setMinAmount(minAmt);
        o.setShowInTopDeals(showInTopDeals);
        o.setPoweredBy(creators);
        o.setCorrelationId(correlationId);
        return repo.save(o);
    }

    @Transactional
    public Offer updateRate(long offerId, BigDecimal newRate, long sellerId) {
        Offer o = repo.findById(offerId).orElseThrow(() -> new NotFoundException("Offer not found"));
        if (!o.getSellerUserId().equals(sellerId)) {
            throw new BusinessException("Forbidden");
        }
        if (o.getStatus() != OfferStatus.LIVE) {
            throw new BusinessException("Only LIVE offers can be updated");
        }
        o.setRate(newRate);
        return repo.save(o);
    }

    //@jakarta.validation.constraints.AssertTrue(message = "min must be ≤ max")
    public boolean isMinLeMax(BigDecimal min, BigDecimal max) {
        if (min == null || max == null) {
            return true; // @NotNull handles nulls
        }
        return min.compareTo(max) <= 0;
    }

    // ---- helpers ----
    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    private ResponseEntity<ApiResponseModel> bad(ApiResponseModel res, String msg, int statusCode) {
        res.setStatusCode(statusCode);
        res.setDescription(msg);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(res);
    }

    @Transactional
    public void cancel(long offerId, long sellerId) {
        Offer o = repo.findById(offerId).orElseThrow(() -> new NotFoundException("Offer not found"));
        if (!o.getSellerUserId().equals(sellerId)) {
            throw new BusinessException("Forbidden");
        }
        o.setStatus(OfferStatus.CANCELLED);
        repo.save(o);
    }
}
