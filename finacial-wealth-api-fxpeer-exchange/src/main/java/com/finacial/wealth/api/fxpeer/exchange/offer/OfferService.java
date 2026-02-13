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
import com.finacial.wealth.api.fxpeer.exchange.domain.AppConfig;
import com.finacial.wealth.api.fxpeer.exchange.domain.AppConfigRepo;
import com.finacial.wealth.api.fxpeer.exchange.domain.FinWealthPaymentTransaction;
import com.finacial.wealth.api.fxpeer.exchange.feign.ProfilingProxies;
import com.finacial.wealth.api.fxpeer.exchange.feign.TransactionServiceProxies;
import com.finacial.wealth.api.fxpeer.exchange.fx.p.p.wallet.WalletTransactionsDetails;
import com.finacial.wealth.api.fxpeer.exchange.fx.p.p.wallet.WalletTransactionsDetailsRepo;
import com.finacial.wealth.api.fxpeer.exchange.investment.service.TransactionHistoryClientLocalT;
import com.finacial.wealth.api.fxpeer.exchange.ledger.LedgerClient;

import com.finacial.wealth.api.fxpeer.exchange.model.AddAccountObj;
import com.finacial.wealth.api.fxpeer.exchange.model.ApiResponseModel;
import com.finacial.wealth.api.fxpeer.exchange.model.BaseResponse;
import com.finacial.wealth.api.fxpeer.exchange.model.CreditWalletCaller;
import com.finacial.wealth.api.fxpeer.exchange.model.DebitWalletCaller;
import com.finacial.wealth.api.fxpeer.exchange.model.ManageFeesConfigReq;
import com.finacial.wealth.api.fxpeer.exchange.model.WalletNo;
import com.finacial.wealth.api.fxpeer.exchange.util.GlobalMethods;
import com.finacial.wealth.api.fxpeer.exchange.util.UttilityMethods;
import com.google.gson.Gson;

import io.micrometer.common.lang.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@Service
public class OfferService {

    private final OfferRepository repo;
    private final LedgerClient ledger;
    private final UttilityMethods utilService;
    private final RegWalletInfoRepository regWalletInfoRepository;
    private final AddAccountDetailsRepo addAccountDetailsRepo;
    private final ProfilingProxies profilingProxies;
    private final TransactionServiceProxies transactionServiceProxies;
    private final AppConfigRepo appConfigRepo;
    @Value("${fx.trade.create.offer.min.amount.percent:0.05}")
    private String createOfferMinAmountPercent;
    private final WalletTransactionsDetailsRepo walletTransactionsDetailsRepo;

    @Value("${fx.enable.run.fx.trade.expired.listings.cron:0}")
    private String fxEnableRunFxTradeEpiredListingsCron;
    @Value("${fx.trade.expired.listings.cron}")
    private String fxTradeExpiredListingsCron;
    private final TransactionHistoryClientLocalT transactionHistoryClientLocalT;

    private static final ZoneId LAGOS = ZoneId.of("Africa/Lagos");
    private static final DateTimeFormatter EXPIRY_DMY
            = DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.ENGLISH);

    public OfferService(OfferRepository repo,
            LedgerClient ledger,
            UttilityMethods utilService,
            RegWalletInfoRepository regWalletInfoRepository,
            AddAccountDetailsRepo addAccountDetailsRepo,
            ProfilingProxies profilingProxies,
            TransactionServiceProxies transactionServiceProxies,
            AppConfigRepo appConfigRepo, WalletTransactionsDetailsRepo walletTransactionsDetailsRepo,
            TransactionHistoryClientLocalT transactionHistoryClientLocalT) {
        this.repo = repo;
        this.ledger = ledger;
        this.utilService = utilService;
        this.regWalletInfoRepository = regWalletInfoRepository;
        this.addAccountDetailsRepo = addAccountDetailsRepo;
        this.profilingProxies = profilingProxies;
        this.transactionServiceProxies = transactionServiceProxies;
        this.appConfigRepo = appConfigRepo;
        this.walletTransactionsDetailsRepo = walletTransactionsDetailsRepo;
        this.transactionHistoryClientLocalT = transactionHistoryClientLocalT;
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

    @Transactional(readOnly = true)
    public ResponseEntity<ApiResponseModel> getMyOffersCaller(String auth, Pageable pageable) {
        ApiResponseModel resp = new ApiResponseModel();

        // -- auth / user lookup (unchanged but safe) --
        final String phoneNumber;
        try {
            if (auth == null || auth.isBlank()) {
                return bad(resp, "Missing Authorization header", 400);
            }
            phoneNumber = utilService.getClaimFromJwt(auth, "phoneNumber");
            if (phoneNumber == null || phoneNumber.isBlank()) {
                return bad(resp, "Invalid token: phoneNumber claim missing", 400);
            }
        } catch (Exception e) {
            return bad(resp, "Unable to parse token", 400);
        }

        RegWalletInfo wallet = regWalletInfoRepository.findByPhoneNumber(phoneNumber).orElse(null);
        if (wallet == null) {
            return bad(resp, "Customer not found for phone number", 400);
        }

        // derive seller IDs from both sources
        Long sellerIdA = null, sellerIdB = null;
        List<AddAccountDetails> accounts = addAccountDetailsRepo.findByEmailAddressrData(wallet.getEmail());
        if (accounts != null && !accounts.isEmpty() && accounts.get(0).getWalletId() != null) {
            try {
                sellerIdA = Long.valueOf(accounts.get(0).getWalletId());
            } catch (NumberFormatException ignored) {
            }
        }
        if (wallet.getWalletId() != null) {
            try {
                sellerIdB = Long.valueOf(wallet.getWalletId());
            } catch (NumberFormatException ignored) {
            }
        }

        // -- fetch offers (avoid double query if same ID) --
        OfferStatus status = null; // or set if you filter by status
        List<Offer> allOffers = new ArrayList<>();

        if (sellerIdA != null && sellerIdB != null && sellerIdA.equals(sellerIdB)) {
            allOffers.addAll(getMyOffers(sellerIdA, status, Pageable.unpaged()).getContent());
        } else {
            if (sellerIdA != null) {
                allOffers.addAll(getMyOffers(sellerIdA, status, Pageable.unpaged()).getContent());
            }
            if (sellerIdB != null) {
                allOffers.addAll(getMyOffers(sellerIdB, status, Pageable.unpaged()).getContent());
            }
        }

        // timezone for "today" and "end of today"
        final ZoneId zone = ZoneId.of("Africa/Lagos");
        final LocalDate today = LocalDate.now(zone);
        final Instant endOfToday = today.atTime(LocalTime.MAX).atZone(zone).toInstant();

        // -- map -> DTO, if expiry is today set to end-of-today, de-duplicate, sort --
        List<OfferListItemDto> mergedSorted = allOffers.stream()
                .map(OfferMapper::toListItem)
                .map(dto -> {
                    Instant exp = dto.expiryAt(); // or dto.getExpiryAt()
                    if (exp != null) {
                        LocalDate expDate = exp.atZone(zone).toLocalDate();
                        if (expDate.equals(today)) {
                            setDtoExpiry(dto, endOfToday); // << key change
                        }
                    }
                    return dto;
                })
                .collect(java.util.stream.Collectors.toMap(
                        OfferListItemDto::id,
                        java.util.function.Function.identity(),
                        (a, b) -> a,
                        java.util.LinkedHashMap::new))
                .values().stream()
                .sorted((a, b) -> {
                    Instant ea = a.expiryAt();
                    Instant eb = b.expiryAt();
                    int byExpiry = java.util.Comparator
                            .nullsLast(java.util.Comparator.<Instant>naturalOrder())
                            .reversed()
                            .compare(ea, eb);
                    if (byExpiry != 0) {
                        return byExpiry;
                    }

                    return java.util.Comparator.<Long>naturalOrder()
                            .reversed()
                            .compare(a.id(), b.id());
                })
                .toList();

        // -- page in-memory using incoming Pageable --
        Page<OfferListItemDto> page = pageList(mergedSorted, pageable);

        if (page.isEmpty()) {
            resp.setStatusCode(400);
            resp.setDescription("No offer found.");
            return ResponseEntity.ok(resp);
        }

        // -- build response --
        resp.setStatusCode(200);
        resp.setDescription("Offers fetched successfully.");
        resp.setData(Map.of(
                "items", page.getContent(),
                "page", page.getNumber(),
                "size", page.getSize(),
                "totalPages", page.getTotalPages(),
                "totalElements", page.getTotalElements(),
                "hasNext", page.hasNext(),
                "hasPrevious", page.hasPrevious()
        ));
        return ResponseEntity.ok(resp);
    }

    /**
     * Sets expiryAt on OfferListItemDto. Works if DTO has setExpiryAt(Instant),
     * otherwise tries direct field access.
     */
    private void setDtoExpiry(OfferListItemDto dto, Instant newExpiry) {
        try {
            // try setter first
            var m = dto.getClass().getMethod("setExpiryAt", Instant.class);
            m.invoke(dto, newExpiry);
            return;
        } catch (Exception ignored) {
        }

        try {
            // fallback: set field directly (for records / no setter)
            var f = dto.getClass().getDeclaredField("expiryAt");
            f.setAccessible(true);
            f.set(dto, newExpiry);
        } catch (Exception ignored) {
            // if neither works, we silently keep original expiry
        }
    }

    /**
     * In-memory pagination helper.
     */
    private static <T> Page<T> pageList(List<T> all, Pageable pageable) {
        if (pageable == null || pageable.isUnpaged()) {
            return new org.springframework.data.domain.PageImpl<>(all);
        }
        int page = pageable.getPageNumber();
        int size = pageable.getPageSize();
        int from = Math.min(page * size, all.size());
        int to = Math.min(from + size, all.size());
        List<T> slice = (from <= to) ? all.subList(from, to) : java.util.Collections.emptyList();
        return new org.springframework.data.domain.PageImpl<>(slice, pageable, all.size());
    }

    public ResponseEntity<ApiResponseModel> getMyOffersCallerOld(String auth,
            Pageable pageable) {

        int statusCode = 500;

        ApiResponseModel resp = new ApiResponseModel();
        try {
            statusCode = 400;
            String phoneNumber = utilService.getClaimFromJwt(auth, "phoneNumber"); // preferred if your JWT has sellerId
            Optional<RegWalletInfo> getRec = regWalletInfoRepository.findByPhoneNumber(phoneNumber);

            List<AddAccountDetails> getAdDe = addAccountDetailsRepo.findByEmailAddressrData(getRec.get().getEmail());

            long sellerId = Long.valueOf(getAdDe.get(0).getWalletId());
            long selleridTwo = Long.valueOf(getRec.get().getWalletId());
            OfferStatus status = null;
            Page<Offer> page = getMyOffers(sellerId, status, pageable);
            Page<Offer> page2 = getMyOffers(selleridTwo, status, pageable);

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

    static BigDecimal parsePercentFlexible(String p) {
        BigDecimal bd = new BigDecimal(p.trim());
        // If ≥ 1, assume “5” means 5% -> 0.05
        return (bd.compareTo(BigDecimal.ONE) >= 0) ? bd.movePointLeft(2) : bd;
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
            bResPin = transactionServiceProxies.validatePin(wSend, auth);
            if (bResPin.getStatusCode() != 200) {
                return bad(res, bResPin.getDescription(), bResPin.getStatusCode());
            }

            List<Offer> offerDe = repo.findByCorrelationIdData(rq.getCorrelationId());
            if (offerDe.size() <= 0) {
                return bad(res, "Offer does not exist!", 400);
            }

            if (!offerDe.get(0).getStatus().LIVE.equals(OfferStatus.LIVE)) {
                return bad(res, "Offer no longer Live!", 400);
            }

            long logSellerId = Long.valueOf(offerDe.get(0).getSellerUserId());

            /*BigDecimal setMin = new BigDecimal(rq.getMinAmount());
            BigDecimal setMax = new BigDecimal(rq.getMaxAmount());

            if (this.isMinLeMax(setMin, setMax) == false) {
                return bad(res, "MinAmount must be ≤ MaxAmount.", 400);
            }*/
            Offer updateOffer = updateRate(offerDe.get(0).getId(), new BigDecimal(rq.getNewRate()), logSellerId, rq.getCorrelationId());
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

    static boolean isBelowPercent(BigDecimal amount, BigDecimal base, BigDecimal percentFraction) {
        // percentFraction is 0.05 for 5%
        BigDecimal threshold = base.multiply(percentFraction);
        return amount.compareTo(threshold) < 0;
    }

    public ResponseEntity<ApiResponseModel> createOfferCaller(CreateOfferCaller rq, String auth) {
        final ApiResponseModel res = new ApiResponseModel();

        try {

            String phoneNumber = utilService.getClaimFromJwt(auth, "phoneNumber"); // preferred if your JWT has sellerId
            System.out.println("phoneNumber" + "  ::::::::::::::::::::: >>>>>>>>>>>>>>>>>>  " + phoneNumber);

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
            bResPin = transactionServiceProxies.validatePin(wSend, auth);
            if (bResPin.getStatusCode() != 200) {
                return bad(res, bResPin.getDescription(), bResPin.getStatusCode());
            }
            if (rq.getCurrencyReceive().equals(rq.getCurrencySell())) {
                return bad(res, "Country code mismatch!", 400);
            }
            //check if cus has currency currency
            List<AddAccountDetails> getAdDe = addAccountDetailsRepo.findByEmailAddressrData(getRec.get().getEmail());
            BaseResponse bRes = new BaseResponse();
            for (AddAccountDetails getWa : getAdDe) {
                String wallCurrencyCode = getWa.getCurrencyCode() == null ? "" : getWa.getCurrencyCode();

                if (!wallCurrencyCode.equals(rq.getCurrencyReceive())) {
                    if (!"CAD".equals(rq.getCurrencyReceive())) {
                        //create account
                        AddAccountObj seObj = new AddAccountObj();
                        seObj.setCountry(getWa.getCountryName());
                        seObj.setCountryCode(getWa.getCountryCode());
                        seObj.setWalletId(getRec.get().getWalletId());
                        bRes = profilingProxies.addOtherAccount(seObj, auth);
                        if (bRes.getStatusCode() != 200) {
                            return bad(res, bRes.getDescription(), bRes.getStatusCode());
                        }
                    }
                }
                if (!wallCurrencyCode.equals(rq.getCurrencySell())) {
                    if (!"CAD".equals(rq.getCurrencySell())) {
                        //create account
                        AddAccountObj seObj = new AddAccountObj();
                        seObj.setCountry(getWa.getCountryName());
                        seObj.setCountryCode(getWa.getCountryCode());
                        seObj.setWalletId(getRec.get().getWalletId());
                        bRes = profilingProxies.addOtherAccount(seObj, auth);
                        if (bRes.getStatusCode() != 200) {
                            return bad(res, bRes.getDescription(), bRes.getStatusCode());
                        }
                    }
                }
            }
            String sellerId = getAdDe.get(0).getWalletId();
            if ("CAD".equals(rq.getCurrencySell())) {
                sellerId = getRec.get().getWalletId();
            }
            System.out.println("sellerId" + "  ::::::::::::::::::::: >>>>>>>>>>>>>>>>>>  " + sellerId);

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
                rate = new BigDecimal(rq.getRate());
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
            //BigDecimal setMax = new BigDecimal(rq.getMaxAmount());
            BigDecimal setMax = BigDecimal.ZERO;
            /*if (this.isMinLeMax(setMin, setMax) == false) {
                return bad(res, "MinAmount must be ≤ MaxAmount.", 400);
            }*/
            // Calculate 5% of faceValue
// usage
            BigDecimal faceValue = new BigDecimal(rq.getQtyTotal());          // or whatever your base is
            BigDecimal minAmt = new BigDecimal(rq.getMinAmount());         // the user-supplied minimum
            BigDecimal pct = parsePercentFlexible(createOfferMinAmountPercent);
            BigDecimal percent = pct.multiply(BigDecimal.valueOf(100));

            boolean tooSmall = isBelowPercent(minAmt, faceValue, pct);
            if (tooSmall) {
                System.out.println("Minmum amount is less than " + percent + "% of Total Quantity");
                return bad(res, "Minmum amount is less than " + percent + "% of Total Quantity", 400);

            }

            /*  BigDecimal fivePercent = new BigDecimal(rq.getQtyTotal()).multiply(BigDecimal.valueOf(Integer.valueOf(createOfferMinAmountPercent)));
            BigDecimal decimal = new BigDecimal(createOfferMinAmountPercent);
            BigDecimal percent = decimal.multiply(BigDecimal.valueOf(100));
// Comparex
            if (setMin.compareTo(fivePercent) < 0) {

                System.out.println("Minmum amount is less than " + percent + "% of Total Quantity");
                return bad(res, "Minmum amount is less than " + percent + "% of Total Quantity", 400);
            } else {
                System.out.println("Minimum amount is at least " + percent + "% of Total Quantity");
            }*/
            ManageFeesConfigReq mFeee = new ManageFeesConfigReq();
            mFeee.setAmount(setMin.toString());
            mFeee.setTransType("createlisting");
            mFeee.setCurrencyCode(rq.getCurrencySell());
            BaseResponse mConfig = utilService.getFeesConfig(mFeee);

            if (mConfig.getStatusCode() != 200) {
                return bad(res, mConfig.getDescription(), mConfig.getStatusCode());
            }

            // 3) Build domain request and delegate
            CreateOfferRq coreRq = new CreateOfferRq(currencySell, currencyReceive, rate, qtyTotal, expiryAt);
            ApiResponseModel created = createOffer(coreRq, Long.valueOf(sellerId), setMin, setMax, rq.isShowInTopDeals(),
                    getRec.get().getFirstName(), getRec.get().getLastName(), auth, phoneNumber);

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
    public ApiResponseModel createOffer(CreateOfferRq rq, long sellerId, BigDecimal minAmt,
            BigDecimal maxAmt, boolean showInTopDeals, String creators,
            String creatorLastName, String auth, String cusCadAccountNo) {
        final ApiResponseModel res = new ApiResponseModel();

        try {
            BaseResponse bRes = new BaseResponse();
            //ledger.ensureWallet(sellerId, rq.getCurrencySell().name());
            // ledger.ensureWallet(sellerId, rq.getCurrencyReceive().name());
            String correlationId = String.valueOf(GlobalMethods.generateTransactionId());
            /*WalletInfo w = ledger.getWallet(sellerId, rq.getCurrencySell().name());
        if (w.available().compareTo(rq.getQtyTotal()) < 0) {
            throw new BusinessException("Insufficient balance to list qtyTotal");
        }*/
            System.out.println("sellerId" + "  ::::::::::::::::::::: >>>>>>>>>>>>>>>>>>  " + sellerId);
            System.out.println("cusCadAccountNo" + "  ::::::::::::::::::::: >>>>>>>>>>>>>>>>>>  " + cusCadAccountNo);

            List<AddAccountDetails> getAdDe = addAccountDetailsRepo.findByWalletIdrData1(String.valueOf(sellerId));

            String accountToDebit;
            String senderName;

            WalletInfoValAcct wSend = new WalletInfoValAcct();
            wSend.setCorrelationId(correlationId);
            wSend.setCurrencyToBuy(rq.getCurrencyReceive().toString());
            if ("CAD".equals(rq.getCurrencySell().toString())) {
                wSend.setAccountNumber(cusCadAccountNo);
                accountToDebit = wSend.getAccountNumber();
                senderName = creatorLastName;

            } else {
                wSend.setAccountNumber(getAdDe.get(0).getAccountNumber());
                accountToDebit = wSend.getAccountNumber();
                senderName = creatorLastName;

            }
            wSend.setCurrencyToSell(rq.getCurrencySell().toString());
            wSend.setTransactionAmmount(rq.getQtyTotal());
            wSend.setWalletId(String.valueOf(sellerId));

            bRes = transactionServiceProxies.createOffervalidateAccount(wSend, auth);
            System.out.println(" transactionServiceProxies.createOffervalidateAccount ::::::::::::::::  %S  " + new Gson().toJson(bRes));
            System.out.println("bRes.getStatusCode()" + "  ::::::::::::::::::::: >>>>>>>>>>>>>>>>>>  " + bRes.getStatusCode());

            if (bRes.getStatusCode() != 200) {
                res.setStatusCode(bRes.getStatusCode());
                res.setDescription(bRes.getDescription());
                return res;

            }

            //debit customer account
            //debit the buyer
            DebitWalletCaller rqD = new DebitWalletCaller();
            rqD.setAuth("Seller");
            rqD.setFees("0.00");
            rqD.setFinalCHarges(rq.getQtyTotal().add(new BigDecimal(rqD.getFees())).toString());
            rqD.setNarration(rq.getCurrencySell().toString() + "_Withdrawal");
            rqD.setPhoneNumber(accountToDebit);
            rqD.setTransAmount(rq.getQtyTotal().add(new BigDecimal(rqD.getFees())).toString());
            rqD.setTransactionId(correlationId);

            System.out.println(" debitBuyerAcct REQ ::::::::::::::::  %S  " + new Gson().toJson(rqD));

            BaseResponse debitBuyerAcct = transactionServiceProxies.debitCustomerWithType(rqD, "CUSTOMER", auth);

            System.out.println(" debitBuyerAcct RESPONSE ::::::::::::::::  %S  " + new Gson().toJson(debitBuyerAcct));

            if (debitBuyerAcct.getStatusCode() == 200) {

                FinWealthPaymentTransaction kTrans2b = new FinWealthPaymentTransaction();
                kTrans2b.setAmmount(new BigDecimal(rqD.getFinalCHarges()));
                kTrans2b.setCreatedDate(Instant.now().plusSeconds(1));
                kTrans2b.setFees(new BigDecimal(rqD.getFees()));
                kTrans2b.setPaymentType("FxPeer Transfer");
                kTrans2b.setReceiver("FxPeer");
                kTrans2b.setSender(accountToDebit);
                kTrans2b.setTransactionId(rqD.getTransactionId());
                kTrans2b.setSenderTransactionType("Withdrawal");
                kTrans2b.setReceiverTransactionType("Deposit");
                kTrans2b.setReceiverBankName("FxPeer");
                kTrans2b.setWalletNo(accountToDebit);
                kTrans2b.setReceiverName("FxPeer");
                kTrans2b.setSenderName(senderName);
                kTrans2b.setSentAmount(rqD.getFinalCHarges());
                kTrans2b.setTheNarration("Fx Peer-Peer listing.");
                kTrans2b.setCurrencyCode(rq.getCurrencySell().toString());

                transactionHistoryClientLocalT.publishFromTxn(kTrans2b);

                System.out.println("sellerId" + "  ::::::::::::::::::::: >>>>>>>>>>>>>>>>>>  " + sellerId);

                DebitWalletCaller debGLCredit = new DebitWalletCaller();
                debGLCredit.setAuth(rqD.getAuth());
                debGLCredit.setFees("0.00");
                debGLCredit.setFinalCHarges(rqD.getFinalCHarges());
                debGLCredit.setNarration(rqD.getNarration());
                List<AppConfig> getAppConf = appConfigRepo.findByConfigName(rq.getCurrencySell().toString());
                String GGL_ACCOUNT = null;
                String GGL_CODE = null;
                for (AppConfig getConfDe : getAppConf) {
                    if (getConfDe.getConfigName().equals(rq.getCurrencySell().toString())) {
                        GGL_ACCOUNT = getConfDe.getConfigValue();
                        GGL_CODE = rq.getCurrencySell().toString();
                    }
                }

                //debGLCredit.setPhoneNumber(utilService.decryptData(utilService.getSETTING_KEY_WALLET_SYSTEM_SYSTEM_GG_NIG()));
                debGLCredit.setPhoneNumber(utilService.decryptData(GGL_ACCOUNT));
                debGLCredit.setTransAmount(rqD.getTransAmount());
                debGLCredit.setTransactionId(correlationId + "-" + rq.getCurrencySell().toString());

                System.out.println(" debitAcct_GL REQUEST ::::::::::::::::  %S  " + new Gson().toJson(debGLCredit));

                BaseResponse debitAcct_GLRes = transactionServiceProxies.debitCustomerWithType(debGLCredit, rq.getCurrencySell().toString(), auth);
                System.out.println(" debitAcct_GLRes RESPONSE ::::::::::::::::  %S  " + new Gson().toJson(debitAcct_GLRes));
            } else {
                res.setStatusCode(bRes.getStatusCode());
                res.setDescription(bRes.getDescription());
                return res;
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
            o.setExpiryAt(rq.getExpiryAt());
            repo.save(o);

            res.setStatusCode(200);
            res.setDescription("Offer created.");
            res.setData(o);
            return res;

        } catch (Exception ex) {
            ex.printStackTrace();
            res.setStatusCode(500);
            res.setDescription("Something went wrong");

        }
        return res;
    }

    @Transactional(readOnly = false)
    public Offer updateRate(long offerId, BigDecimal newRate, long sellerId, String correlationId) {

        List<Offer> oC = repo.findByCorrelationIdData(correlationId);

        if (oC.size() <= 0) {
            throw new BusinessException("Offer not found");
        }

        if (oC.get(0).getSellerUserId() != sellerId) {

            throw new BusinessException("Invalid sellerId");
        }

        if (oC.get(0).getStatus() != OfferStatus.LIVE) {
            throw new BusinessException("Only LIVE offers can be updated");
        }

        Offer oCUp = repo.findByCorrelationIdDataUpdate(correlationId);
        oCUp.setUpdatedNow();
        oCUp.setRate(newRate);
        return repo.save(oCUp);


        /*Offer o = repo.findById(offerId).orElseThrow(() -> new NotFoundException("Offer not found"));
        if (!o.getSellerUserId().equals(sellerId)) {
            throw new BusinessException("Forbidden");
        }
        if (o.getStatus() != OfferStatus.LIVE) {
            throw new BusinessException("Only LIVE offers can be updated");
        }
        o.setRate(newRate);
        return repo.save(o);*/
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
        return new ResponseEntity<>(res, HttpStatus.OK);
        //return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(res);
    }

    /**
     * Runs every 5 minutes (Africa/Lagos). Tune to your needs.
     */
    //@Scheduled(cron = "0 */5 * * * *", zone = "Africa/Lagos")
    @Scheduled(cron = "${fx.trade.expired.listings.cron}")
    @Transactional
    public void expireDueOffers() throws NoSuchAlgorithmException,
            NoSuchPaddingException, InvalidKeyException,
            IllegalBlockSizeException, BadPaddingException {
        // If you mean "now", use Instant.now() (UTC-safe for Instant)
        final Instant now = Instant.now();
        System.out.println(" ****** Checking and Processing ExpiredDueOffers  >>>>>>>>>>>>>   *********** ");

        if (!fxEnableRunFxTradeEpiredListingsCron.equals("0")) {

            // If you truly mean "today" cut-off by calendar day in Lagos, use this instead:
            final ZoneId ng = ZoneId.of("Africa/Lagos");
            final Instant endOfTodayLagos = LocalDate.now(ng).plusDays(1).atStartOfDay(ng).toInstant();
            final Instant cutoff = endOfTodayLagos;
            final List<OfferStatus> openStatuses = Arrays.asList(OfferStatus.LIVE);

            Pageable page = PageRequest.of(0, 500);
            Page<Offer> batch;

            do {
                //  batch = repo.findByExpiryAtIsNotNullAndExpiryAtLessThanEqualAndStatusIn(now, openStatuses, page);
                batch = repo.findByExpiryAtIsNotNullAndExpiryAtLessThanEqualAndStatusIn(cutoff, openStatuses, page);

                for (Offer o : batch.getContent()) {
                    // Your per-offer business rules:
                    o.setStatus(OfferStatus.EXPIRED);
                    o.setQtyAvailable(BigDecimal.ZERO);
                    o.setUpdatedNow();
                    // o.setShowInTopDeals(false);
                    // notificationService.notifySellerExpired(o.getSellerUserId(), o.getId());
                    // publisher.publishEvent(new OfferExpiredEvent(o.getId()));
                    WalletTransactionsDetails getWalDeupdate = walletTransactionsDetailsRepo.findByCorrelationIdUpdated(o.getCorrelationId());
                    BigDecimal availableQuantity = getWalDeupdate.getAvailableQuantity();

                    //get currencyToSell
                    // get availableQuantity
                    //make reversal
                    CreditWalletCaller rqC = new CreditWalletCaller();

                    if (getWalDeupdate.getCurrencyToSell().equals("CAD")) {
                        Optional<RegWalletInfo> getRec = regWalletInfoRepository.findByEmail(getWalDeupdate.getEmailAddress());

                        rqC.setPhoneNumber(getRec.get().getPhoneNumber());
                        getWalDeupdate.setLastModifiedDate(Instant.now());
                        getWalDeupdate.setBuyerName(getRec.get().getFullName());

                    } else {
                        List<AddAccountDetails> getSellerAcct = addAccountDetailsRepo.findByEmailAddressrData(getWalDeupdate.getEmailAddress());
                        rqC.setPhoneNumber(getSellerAcct.get(0).getAccountNumber());
                        getWalDeupdate.setLastModifiedDate(Instant.now());
                        Optional<RegWalletInfo> getRec = regWalletInfoRepository.findByEmail(getWalDeupdate.getEmailAddress());

                        getWalDeupdate.setBuyerName(getRec.get().getFullName());

                    }

                    rqC.setAuth("Seller");
                    rqC.setFees("00");
                    rqC.setFinalCHarges(availableQuantity.toString());
                    rqC.setNarration(getWalDeupdate.getCurrencyToSell() + "_Reversal");
                    rqC.setTransAmount(availableQuantity.toString());
                    rqC.setTransactionId(o.getCorrelationId() + "CREDIT-REVERSAL");

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

                        List<AppConfig> getAppConf = appConfigRepo.findByConfigName(getWalDeupdate.getCurrencyToSell());

                        for (AppConfig getConfDe : getAppConf) {
                            if (getConfDe.getConfigName().equals(getWalDeupdate.getCurrencyToSell())) {
                                GGL_ACCOUNT = getConfDe.getConfigValue();

                            }
                        }
                        //GLCredit.setPhoneNumber(utilService.decryptData(utilService.getSETTING_KEY_WALLET_SYSTEM_SYSTEM_GG_CAD()));
                        GLCredit.setPhoneNumber(utilService.decryptData(GGL_ACCOUNT));
                        GLCredit.setTransAmount(rqC.getFinalCHarges());
                        GLCredit.setTransactionId(rqC.getTransactionId() + "-REVERSAL");

                        System.out.println(" creditAcct_GL seller for Reversal REQ  ::::::::::::::::  %S  " + new Gson().toJson(GLCredit));

                        BaseResponse creditAcct_GL = transactionServiceProxies.creditCustomerWithType(GLCredit, getWalDeupdate.getCurrencyToSell(), "");

                        System.out.println(" credit GL for seller for Reversal Response from core ::::::::::::::::  %S  " + new Gson().toJson(creditAcct_GL));

                    }

                    System.out.println(" On CANCEL SET availableQuantity to ZERO::::::::::::::::  %S  ");

                    getWalDeupdate.setStatus(OfferStatus.CANCELLED);

                    getWalDeupdate.setAvailableQuantity(BigDecimal.ZERO);
                    walletTransactionsDetailsRepo.save(getWalDeupdate);
                }

                // Persist this page before moving on
                repo.flush(); // (if JpaRepository)
                // Move to next page
                page = page.next();
            } while (!batch.isEmpty());
        }
    }

    public ResponseEntity<ApiResponseModel> cancelOfferCaller(CancelOfferCallerReq rq, String auth) {
        final ApiResponseModel res = new ApiResponseModel();

        try {

            String phoneNumber = utilService.getClaimFromJwt(auth, "phoneNumber"); // preferred if your JWT has sellerId
            Optional<RegWalletInfo> getRec = regWalletInfoRepository.findByPhoneNumber(phoneNumber);
            //validate pin
            BaseResponse bResPin = new BaseResponse();
            WalletNo wSend = new WalletNo();
            wSend.setPin(rq.getPin());

            wSend.setWalletId(getRec.get().getWalletId());
            bResPin = transactionServiceProxies.validatePin(wSend, auth);
            if (bResPin.getStatusCode() != 200) {
                return bad(res, bResPin.getDescription(), bResPin.getStatusCode());
            }

            List<Offer> offerDe = repo.findByCorrelationIdData(rq.getCorrelationId());
            if (offerDe.size() <= 0) {
                return bad(res, "Offer does not exist!", 400);
            }

            WalletTransactionsDetails getWalDeupdate = walletTransactionsDetailsRepo.findByCorrelationIdUpdated(rq.getCorrelationId());
            BigDecimal availableQuantity = getWalDeupdate.getAvailableQuantity();

            //get currencyToSell
            // get availableQuantity
            //make reversal
            CreditWalletCaller rqC = new CreditWalletCaller();

            if (getWalDeupdate.getCurrencyToSell().equals("CAD")) {
                rqC.setPhoneNumber(phoneNumber);

            } else {
                List<AddAccountDetails> getSellerAcct = addAccountDetailsRepo.findByEmailAddressrData(getWalDeupdate.getEmailAddress());
                rqC.setPhoneNumber(getSellerAcct.get(0).getAccountNumber());
            }

            rqC.setAuth("Seller");
            rqC.setFees("00");
            rqC.setFinalCHarges(availableQuantity.toString());
            rqC.setNarration(getWalDeupdate.getCurrencyToSell() + "_Reversal");
            rqC.setTransAmount(availableQuantity.toString());
            rqC.setTransactionId(rq.getCorrelationId() + "CREDIT-REVERSAL");

            System.out.println(" creditSellerAcct for Reversal REQ  ::::::::::::::::  %S  " + new Gson().toJson(rqC));

            BaseResponse creditSellerAcct = transactionServiceProxies.creditCustomerWithType(rqC, "CUSTOMER", auth);

            System.out.println(" creditSellerAcct Response from core ::::::::::::::::  %S  " + new Gson().toJson(creditSellerAcct));
            if (creditSellerAcct.getStatusCode() == 200) {

                // Credit BAAS NGN_GL
                CreditWalletCaller GLCredit = new CreditWalletCaller();
                GLCredit.setAuth("Receiver");
                GLCredit.setFees("0.00");
                GLCredit.setFinalCHarges(rqC.getFinalCHarges());
                GLCredit.setNarration(rqC.getNarration());
                String GGL_ACCOUNT = null;

                List<AppConfig> getAppConf = appConfigRepo.findByConfigName(getWalDeupdate.getCurrencyToSell());

                for (AppConfig getConfDe : getAppConf) {
                    if (getConfDe.getConfigName().equals(getWalDeupdate.getCurrencyToSell())) {
                        GGL_ACCOUNT = getConfDe.getConfigValue();

                    }
                }
                //GLCredit.setPhoneNumber(utilService.decryptData(utilService.getSETTING_KEY_WALLET_SYSTEM_SYSTEM_GG_CAD()));
                GLCredit.setPhoneNumber(utilService.decryptData(GGL_ACCOUNT));
                GLCredit.setTransAmount(rqC.getFinalCHarges());
                GLCredit.setTransactionId(rqC.getTransactionId() + "-REVERSAL");

                System.out.println(" creditAcct_GL seller for Reversal REQ  ::::::::::::::::  %S  " + new Gson().toJson(GLCredit));

                BaseResponse creditAcct_GL = transactionServiceProxies.creditCustomerWithType(GLCredit, getWalDeupdate.getCurrencyToSell(), auth);

                System.out.println(" credit GL for seller for Reversal Response from core ::::::::::::::::  %S  " + new Gson().toJson(creditAcct_GL));

                FinWealthPaymentTransaction kTrans2b = new FinWealthPaymentTransaction();
                kTrans2b.setAmmount(new BigDecimal(rqC.getFinalCHarges()));
                kTrans2b.setCreatedDate(Instant.now().plusSeconds(1));
                kTrans2b.setFees(new BigDecimal(rqC.getFees()));
                kTrans2b.setPaymentType("FxPeer Reversal Transfer");
                kTrans2b.setReceiver(rqC.getPhoneNumber());
                kTrans2b.setSender("FxPeer");
                kTrans2b.setTransactionId(rqC.getTransactionId());
                kTrans2b.setSenderTransactionType("Withdrawal");
                kTrans2b.setReceiverTransactionType("Deposit");
                kTrans2b.setReceiverBankName("FxPeer");
                kTrans2b.setWalletNo("FxPeer");
                kTrans2b.setReceiverName(getWalDeupdate.getSellerName());
                kTrans2b.setSenderName("FxPeer");
                kTrans2b.setSentAmount(rqC.getFinalCHarges());
                kTrans2b.setTheNarration("Fx Peer-Peer listing.");
                kTrans2b.setCurrencyCode(getWalDeupdate.getCurrencyToSell());

                // finWealthPaymentTransactionRepo.save(kTrans2b);
                transactionHistoryClientLocalT.publishFromTxn(kTrans2b);

            }

            getWalDeupdate.setLastModifiedDate(Instant.now());
            getWalDeupdate.setBuyerName(getRec.get().getFullName());

            System.out.println(" On CANCEL SET availableQuantity to ZERO::::::::::::::::  %S  ");

            getWalDeupdate.setStatus(OfferStatus.CANCELLED);

            getWalDeupdate.setAvailableQuantity(BigDecimal.ZERO);
            walletTransactionsDetailsRepo.save(getWalDeupdate);

            long logSellerId = Long.valueOf(offerDe.get(0).getSellerUserId());

            cancel(offerDe.get(0).getId(), logSellerId, rq.getCorrelationId());
            res.setStatusCode(200);
            res.setDescription("Offer canceled.");

            return ResponseEntity.ok(res);

        } catch (BusinessException be) {
            return bad(res, be.getMessage(), 500);
        } catch (Exception ex) {
            ex.printStackTrace();
            return bad(res, "An error occurred, please try again.", 500);
        }
    }

    @Transactional
    public void cancel(long offerId, long sellerId, String correlationId) {

        List<Offer> oC = repo.findByCorrelationIdData(correlationId);

        if (oC.size() <= 0) {
            throw new BusinessException("Offer not found");
        }

        if (oC.get(0).getSellerUserId() != sellerId) {

            throw new BusinessException("Invalid sellerId");
        }

        Offer oCUp = repo.findByCorrelationIdDataUpdate(correlationId);
        oCUp.setUpdatedNow();
        oCUp.setStatus(OfferStatus.CANCELLED);

        repo.save(oCUp);
    }
}
