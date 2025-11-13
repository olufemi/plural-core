package com.finacial.wealth.api.fxpeer.exchange.inter.airtime.security;

import static com.finacial.wealth.api.fxpeer.exchange.inter.airtime.security.SochitelSignedClient.loadPkcs8PrivateKeyFromPem;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.finacial.wealth.api.fxpeer.exchange.domain.AddAccountDetails;
import com.finacial.wealth.api.fxpeer.exchange.domain.AddAccountDetailsRepo;
import com.finacial.wealth.api.fxpeer.exchange.domain.AppConfig;
import com.finacial.wealth.api.fxpeer.exchange.domain.AppConfigRepo;
import com.finacial.wealth.api.fxpeer.exchange.domain.FinWealthPaymentTransaction;
import com.finacial.wealth.api.fxpeer.exchange.domain.RegWalletInfo;
import com.finacial.wealth.api.fxpeer.exchange.domain.RegWalletInfoRepository;
import com.finacial.wealth.api.fxpeer.exchange.feign.ProfilingProxies;
import com.finacial.wealth.api.fxpeer.exchange.feign.TransactionServiceProxies;
import com.finacial.wealth.api.fxpeer.exchange.fx.p.p.wallet.FinWealthPaymentTransactionRepo;
import com.finacial.wealth.api.fxpeer.exchange.inter.airtime.getpack.OperatorEntry;
import com.finacial.wealth.api.fxpeer.exchange.inter.airtime.getpack.SochitelProductsResponse;
import com.finacial.wealth.api.fxpeer.exchange.inter.airtime.transaction.TopupPurchaseResponse;
import com.finacial.wealth.api.fxpeer.exchange.inter.airtime.val.msisdn.NumberNormalizeResponse;
import com.finacial.wealth.api.fxpeer.exchange.model.AddAccountObj;
import com.finacial.wealth.api.fxpeer.exchange.model.ApiResponseModel;
import com.finacial.wealth.api.fxpeer.exchange.model.BaseResponse;
import com.finacial.wealth.api.fxpeer.exchange.model.CreditWalletCaller;
import com.finacial.wealth.api.fxpeer.exchange.model.DebitWalletCaller;
import com.finacial.wealth.api.fxpeer.exchange.model.GetProducts;
import com.finacial.wealth.api.fxpeer.exchange.model.ManageFeesConfigReq;

import com.finacial.wealth.api.fxpeer.exchange.model.ValidateCountryCode;
import com.finacial.wealth.api.fxpeer.exchange.order.WalletInfoValiAcctBal;
import com.finacial.wealth.api.fxpeer.exchange.util.GlobalMethods;
import com.finacial.wealth.api.fxpeer.exchange.util.UttilityMethods;
import jakarta.annotation.PostConstruct;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;

@Service
public class ProcSochitelServices {

    private static final Logger log = LoggerFactory.getLogger(ProcSochitelServices.class);

    private final ObjectMapper mapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true)
            .configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);

    @Value("${sochitel.base.url}")
    private String serviceSochiBaseUrl;

    @Value("${sochitel.key.id}")
    private String keyId;

    @Value("${sochitel.pull.pem.file:false}")
    private String pullPemFileRaw;

    @Value("${sochitel.pem-path:file:/root/secrets/sochitel/sochitel.pem}")
    private String pemPath;

    private final ProfilingProxies profilingProxies;
    private final ResourceLoader resourceLoader;

    private Resource pemFile;

    @Value("${spring.profiles.active}")
    private String environment;

    @Value("${sochitel.service.sample.get.operators}")
    private String mockedOperators;

    @Value("${sochitel.products.mock.enabled:false}")
    private boolean mockEnabled;

    @Value("${sochitel.products.check.transaction.status.enabled:false}")
    private boolean checkTransactionStatusenabled;

    private final RegWalletInfoRepository regWalletInfoRepository;
    private final UttilityMethods utilService;
    private final FinWealthPaymentTransactionRepo finWealthPaymentTransactionRepo;
    private final TransactionServiceProxies transactionServiceProxies;
    private final AddAccountDetailsRepo addAccountDetailsRepo;
    private final AppConfigRepo appConfigRepo;

    public ProcSochitelServices(
            ProfilingProxies profilingProxies,
            ResourceLoader resourceLoader,
            UttilityMethods utilService,
            FinWealthPaymentTransactionRepo finWealthPaymentTransactionRepo,
            TransactionServiceProxies transactionServiceProxies,
            AddAccountDetailsRepo addAccountDetailsRepo,
            RegWalletInfoRepository regWalletInfoRepository,
            AppConfigRepo appConfigRepo) {

        this.profilingProxies = profilingProxies;
        this.resourceLoader = resourceLoader;
        this.utilService = utilService;
        this.finWealthPaymentTransactionRepo = finWealthPaymentTransactionRepo;
        this.transactionServiceProxies = transactionServiceProxies;
        this.addAccountDetailsRepo = addAccountDetailsRepo;
        this.regWalletInfoRepository = regWalletInfoRepository;
        this.appConfigRepo = appConfigRepo;
    }

    // ---------- Utility ----------
    private static boolean looksLikeHtml(String body) {
        if (body == null) {
            return false;
        }
        String t = body.trim();
        return t.startsWith("<!DOCTYPE") || t.startsWith("<html");
    }

    private static String sha256(byte[] bytes) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] d = md.digest(bytes);
        StringBuilder sb = new StringBuilder();
        for (byte b : d) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    private Resource resolveResource(String path) {
        if (path.startsWith("classpath:") || path.startsWith("file:") || path.matches("^[a-zA-Z]+:.*")) {
            return resourceLoader.getResource(path);
        }
        if (path.startsWith("/")) {
            return resourceLoader.getResource("file:" + path);
        }
        return resourceLoader.getResource("classpath:" + path);
    }

    private String readResourceAsString(String path) throws IOException {
        Resource resource = resolveResource(path);
        if (!resource.exists()) {
            throw new FileNotFoundException("Resource not found: " + path);
        }
        try (InputStream in = resource.getInputStream()) {
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    @PostConstruct
    public void verifyPemReadable() throws Exception {
        boolean shouldCheck = "1".equals(pullPemFileRaw) || Boolean.parseBoolean(pullPemFileRaw);
        if (!shouldCheck) {
            return;
        }

        this.pemFile = resourceLoader.getResource(pemPath);
        if (!pemFile.exists()) {
            throw new IllegalStateException("PEM not found: " + safeDesc(pemFile));
        }

        byte[] data;
        try (InputStream in = pemFile.getInputStream()) {
            data = in.readAllBytes();
        }
        if (data.length == 0) {
            throw new IllegalStateException("PEM empty: " + safeDesc(pemFile));
        }

        log.info("Sochitel PEM loaded from {} (bytes={}, sha256={})", safeDesc(pemFile), data.length, sha256(data));
    }

    private String safeDesc(Resource r) {
        try {
            return r.getURI().toString();
        } catch (Exception ignored) {
            return r.getDescription();
        }
    }

    public String loadPem() throws IOException {
        if (this.pemFile == null) {
            this.pemFile = resourceLoader.getResource(pemPath);
        }
        try (InputStream in = pemFile.getInputStream()) {
            return StreamUtils.copyToString(in, StandardCharsets.UTF_8);
        }
    }

    private static String safe(String s) {
        return s == null ? "" : s;
    }

    private ResponseEntity<ApiResponseModel> bad(ApiResponseModel res, String msg, int statusCode) {
        res.setStatusCode(statusCode);
        res.setDescription(msg);
        return new ResponseEntity<>(res, HttpStatus.OK);
    }

    // ---------- Operators ----------
    public ResponseEntity<ApiResponseModel> getProdocts(GetProducts rq, String auth) {
        ApiResponseModel resp = new ApiResponseModel();
        try {
            if (rq == null || rq.getCurrencyCode() == null || rq.getCurrencyCode().trim().isEmpty()) {
                resp.setStatusCode(400);
                resp.setDescription("currencyCode is required");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(resp);
            }
            final String currency = rq.getCurrencyCode().trim().toUpperCase(Locale.ROOT);

            ValidateCountryCode valReq = new ValidateCountryCode();
            valReq.setCurrencyCode(currency);
            ApiResponseModel valCode = profilingProxies.validateCountryCode(valReq, auth);
            int vStatus = (valCode == null ? -1 : valCode.getStatusCode());
            log.info("[validateCountryCode] status={} desc={}", vStatus, (valCode == null ? "null" : valCode.getDescription()));
            if (valCode == null || vStatus != 200) {
                resp.setStatusCode(400);
                resp.setDescription(valCode != null && valCode.getDescription() != null ? valCode.getDescription() : "validateCountryCode failed");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(resp);
            }

            String providerBody;
            if (!mockEnabled) {
                String pem = loadPem();
                if (pem == null || pem.trim().isEmpty()) {
                    resp.setStatusCode(500);
                    resp.setDescription("Provider signing key is not configured");
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(resp);
                }
                PrivateKey key = loadPkcs8PrivateKeyFromPem(pem);

                if (serviceSochiBaseUrl == null || serviceSochiBaseUrl.isEmpty()) {
                    resp.setStatusCode(500);
                    resp.setDescription("Provider base URL not configured");
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(resp);
                }

                String normalizedBase = serviceSochiBaseUrl.replaceAll("/+$", "");
                URI base = URI.create(normalizedBase);
                String basePath = base.getPath() == null ? "" : base.getPath();
                final String operatorsPath = basePath.contains("/api") ? "operators" : "/api/operators";

                log.info("[sochitel] base={} path={}", base, operatorsPath);

                SochitelSignedClient client = new SochitelSignedClient(base, keyId, key);
                HttpResponse<String> providerResp = client.get(operatorsPath);
                int providerHttp = providerResp.statusCode();
                providerBody = providerResp.body();
                log.info("[sochitel/operators] http={}", providerHttp);
                log.info("[sochitel/providers/raw] http={} bodyPreview={}",
                        providerHttp,
                        providerBody != null && providerBody.length() > 500 ? providerBody.substring(0, 500) + "..." : providerBody);

                if (providerHttp < 200 || providerHttp >= 300) {
                    resp.setStatusCode(502);
                    resp.setDescription("Provider HTTP error: " + providerHttp);
                    resp.setOther(providerBody);
                    return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(resp);
                }
            } else {
                providerBody = readResourceAsString(mockedOperators);
            }

            if (looksLikeHtml(providerBody)) {
                resp.setStatusCode(502);
                resp.setDescription("Provider returned HTML instead of JSON");
                resp.setOther(providerBody.length() > 400 ? providerBody.substring(0, 400) + "…" : providerBody);
                return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(resp);
            }

            SochitelProductsResponse products;
            try {
                products = mapper.readValue(providerBody, SochitelProductsResponse.class);
            } catch (Exception parseEx) {
                resp.setStatusCode(502);
                resp.setDescription("Provider JSON parse error");
                resp.setOther(providerBody != null && providerBody.length() > 500 ? providerBody.substring(0, 500) + "…" : providerBody);
                return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(resp);
            }

            boolean ok = false;
            String errSummary = null;
            if (products != null) {
                Integer errno = products.getErrno();
                Integer sysErr = products.getSysErr();
                if (errno != null) {
                    ok = (errno == 0);
                    if (!ok) {
                        errSummary = "errno=" + errno + (products.getError() != null ? (", error=" + products.getError()) : "");
                    }
                } else if (sysErr != null) {
                    ok = (sysErr == 0);
                    if (!ok) {
                        errSummary = "sysErr=" + sysErr + (products.getSysId() != null ? (", sysId=" + products.getSysId()) : "");
                    }
                } else {
                    ok = products.getOperators() != null && !products.getOperators().isEmpty();
                }
            }

            if (!ok) {
                resp.setStatusCode(502);
                resp.setDescription("Provider error" + (errSummary != null ? (": " + errSummary) : ""));
                resp.setOther(providerBody);
                return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(resp);
            }

            List<OperatorEntry> all = Optional.ofNullable(products.getOperators()).orElse(Collections.emptyList());
            log.info("[sochitel/operators] count={} sampleCurrency={}", all.size(), all.isEmpty() ? "n/a" : all.get(0).getCurrency());
            List<OperatorEntry> filtered = all.stream()
                    .filter(op -> currency.equalsIgnoreCase(safe(op.getCurrency())))
                    .collect(Collectors.toList());

            if (filtered.isEmpty()) {
                resp.setStatusCode(404);
                resp.setDescription("No operators found for currency " + currency);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(resp);
            }

            resp.setStatusCode(200);
            resp.setDescription("Successful");
            resp.setData(filtered);
            return ResponseEntity.ok(resp);

        } catch (Exception ex) {
            log.error("getProdocts error", ex);
            resp.setStatusCode(500);
            resp.setDescription("Internal error: " + ex.getClass().getSimpleName() + " - " + ex.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(resp);
        }
    }

    // ---------- MSISDN ----------
    public BaseResponse validatePhoneNumber(ValidatePhoneNumber rq, String auth) {
        BaseResponse resp = new BaseResponse();
        try {
            if (rq == null
                    || rq.getCurrencyCode() == null || rq.getCurrencyCode().trim().isEmpty()
                    || rq.getPhoneNumber() == null || rq.getPhoneNumber().trim().isEmpty()) {
                resp.setStatusCode(400);
                resp.setDescription("currencyCode and phoneNumber are required");
                return resp;
            }
            final String currency = rq.getCurrencyCode().trim().toUpperCase(Locale.ROOT);
            final String msisdn = rq.getPhoneNumber().trim();

            ValidateCountryCode rqq = new ValidateCountryCode();
            rqq.setCurrencyCode(currency);
            ApiResponseModel valCode = profilingProxies.validateCountryCode(rqq, auth);
            if (valCode == null || valCode.getStatusCode() != 200) {
                resp.setStatusCode(400);
                resp.setDescription(valCode != null && valCode.getDescription() != null ? valCode.getDescription() : "validateCountryCode failed");
                return resp;
            }

            String pem = loadPem();
            if (pem == null || pem.trim().isEmpty()) {
                resp.setStatusCode(500);
                resp.setDescription("Provider signing key is not configured");
                return resp;
            }
            PrivateKey key = loadPkcs8PrivateKeyFromPem(pem);

            if (serviceSochiBaseUrl == null || serviceSochiBaseUrl.isEmpty()) {
                resp.setStatusCode(500);
                resp.setDescription("Provider base URL not configured");
                return resp;
            }
            final URI base = URI.create(serviceSochiBaseUrl.replaceAll("/+$", ""));
            final String basePath = base.getPath() == null ? "" : base.getPath();
            final String msisdnPath = basePath.contains("/api") ? "msisdn/" + msisdn : "/api/msisdn/" + msisdn;

            SochitelSignedClient client = new SochitelSignedClient(base, keyId, key);

            HttpResponse<String> getResp = client.get(msisdnPath);
            final int http = getResp.statusCode();
            final String body = getResp.body();

            String masked = msisdn.length() <= 2 ? "**" : "*".repeat(msisdn.length() - 2) + msisdn.substring(msisdn.length() - 2);
            log.info("[sochitel/msisdn] http={} path=.../msisdn/{}", http, masked);
            log.info("[sochitel/msisdn] bodyPreview={}", body == null ? "null" : (body.length() > 500 ? body.substring(0, 500) + "..." : body));

            if (http < 200 || http >= 300) {
                resp.setStatusCode(502);
                resp.setDescription("Provider HTTP error: " + http);
                resp.addData("other", body);
                return resp;
            }

            if (looksLikeHtml(body)) {
                resp.setStatusCode(502);
                resp.setDescription("Provider returned HTML instead of JSON");
                resp.addData("other", body.length() > 400 ? body.substring(0, 400) + "…" : body);
                return resp;
            }

            NumberNormalizeResponse n = mapper.readValue(body, NumberNormalizeResponse.class);

            boolean ok;
            String errSummary = null;
            Integer errno = n.getErrno();
            Integer sysErr = n.getSysErr();
            if (errno != null) {
                ok = (errno == 0);
                if (!ok) {
                    errSummary = "errno=" + errno + (n.getError() != null ? (", error=" + n.getError()) : "");
                }
            } else if (sysErr != null) {
                ok = (sysErr == 0);
                if (!ok) {
                    errSummary = "sysErr=" + sysErr + (n.getSysId() != null ? (", sysId=" + n.getSysId()) : "");
                }
            } else {
                ok = Boolean.TRUE.equals(n.getValid());
            }

            if (!ok) {
                resp.setStatusCode(502);
                resp.setDescription("Provider error" + (errSummary != null ? (": " + errSummary) : ""));
                resp.addData("other", body);
                return resp;
            }

            resp.setStatusCode(200);
            resp.setDescription("Successful");
            resp.addData("isValid", n.getValid());
            if (n.getIsForma() != null && n.getIsForma().getId() != null) {
                resp.addData("country", n.getIsForma().getId());
            } else if (n.getCountry() != null) {
                resp.addData("country", n.getCountry());
            }
            if (n.getNormalized() != null) {
                resp.addData("normalized", n.getNormalized());
            }
            if (n.getOriginal() != null) {
                resp.addData("original", n.getOriginal());
            }

            return resp;

        } catch (Exception ex) {
            log.error("validatePhoneNumber error", ex);
            resp.setStatusCode(500);
            resp.setDescription("Something went wrong, please retry in a moment.");
            return resp;
        }
    }

    // ---------- Transaction (purchase) ----------
    public ApiResponseModel processTrnsaction(ProcessTrnsactionReq rq, String auth) throws IOException {
        ApiResponseModel resp = new ApiResponseModel();
        int statusCode = 500;
        String description = "Something went wrong, please retry in a moment.";

        final String processId = String.valueOf(GlobalMethods.generateTransactionId());

        try {
            final String phoneNumber = utilService.getClaimFromJwt(auth, "phoneNumber");
            final String email = utilService.getClaimFromJwt(auth, "emailAddress");

            if (rq == null || rq.getCurrencyCode() == null || rq.getCurrencyCode().trim().isEmpty()) {
                resp.setStatusCode(400);
                resp.setDescription("currencyCode is required");
                return resp;
            }
            final String currency = rq.getCurrencyCode().trim().toUpperCase(Locale.ROOT);

            if (rq.getOperator() == null || rq.getOperator().trim().isEmpty()
                    || rq.getProduct() == null || rq.getProduct().trim().isEmpty()
                    || rq.getRecipient() == null || rq.getRecipient().trim().isEmpty()
                    || rq.getAmount() == null || rq.getAmount().trim().isEmpty()) {
                resp.setStatusCode(400);
                resp.setDescription("operator, product, recipient and amount are required");
                return resp;
            }

            ValidateCountryCode rqq = new ValidateCountryCode();
            rqq.setCurrencyCode(currency);
            ApiResponseModel valCode = profilingProxies.validateCountryCode(rqq, auth);
            if (valCode == null || valCode.getStatusCode() != 200) {
                resp.setStatusCode(400);
                resp.setDescription(valCode != null && valCode.getDescription() != null ? valCode.getDescription() : "validateCountryCode failed");
                return resp;
            }

            ManageFeesConfigReq mFeee = new ManageFeesConfigReq();
            mFeee.setAmount(rq.getAmount());
            mFeee.setTransType("buyairtime");
            mFeee.setCurrencyCode(rq.getCurrencyCode());

            BaseResponse mConfig = utilService.getFeesConfig(mFeee);
            if (mConfig.getStatusCode() != 200) {
                resp.setStatusCode(mConfig.getStatusCode());
                resp.setDescription(mConfig.getDescription());
                return resp;
            }

            // Pre-debit & internal legs
            PreDebitResult pre = preDebitAndSettleAirtime(rq, auth, email, phoneNumber, mConfig, processId);
            if (!pre.isSuccess()) {
                resp.setStatusCode(pre.getError().getStatusCode());
                resp.setDescription(pre.getError().getDescription());
                return resp;
            }

            // Prepare provider client
            String pem = loadPem();
            if (pem == null || pem.trim().isEmpty()) {
                resp.setStatusCode(500);
                resp.setDescription("Provider signing key is not configured");
                return resp;
            }
            PrivateKey key = loadPkcs8PrivateKeyFromPem(pem);

            if (serviceSochiBaseUrl == null || serviceSochiBaseUrl.isEmpty()) {
                resp.setStatusCode(500);
                resp.setDescription("Provider base URL not configured");
                return resp;
            }
            final URI base = URI.create(serviceSochiBaseUrl.replaceAll("/+$", ""));
            final String basePath = base.getPath() == null ? "" : base.getPath();
            final String createTxnPath = basePath.contains("/api") ? "transaction" : "/api/transaction";
            final String fetchByUserRefPathPrefix = basePath.contains("/api") ? "transaction/user/" : "/api/transaction/user/";

            SochitelSignedClient client = new SochitelSignedClient(base, keyId, key);

            // Provider request JSON
            ObjectMapper mapperReq = new ObjectMapper();
            ObjectNode root = mapperReq.createObjectNode()
                    .put("operator", rq.getOperator().trim())
                    .put("product", rq.getProduct().trim())
                    .put("recipient", rq.getRecipient().trim())
                    .put("amount", rq.getAmount().trim())
                    .put("currency", currency)
                    .put("reference", processId);

            String json = mapperReq.writeValueAsString(root);

            // POST /transaction
            HttpResponse<String> postResp = client.postJson(createTxnPath, json);
            int http = postResp.statusCode();
            String body = postResp.body();

            String rcpt = root.path("recipient").asText("");
            String masked = rcpt.isEmpty() ? "n/a" : (rcpt.length() <= 2 ? "**" : "*".repeat(rcpt.length() - 2) + rcpt.substring(rcpt.length() - 2));
            log.info("[sochitel/transaction] http={} ref={} recipient={}", http, root.path("reference").asText("n/a"), masked);
            log.info("[sochitel/transaction] bodyPreview={}", body == null ? "null" : (body.length() > 600 ? body.substring(0, 600) + "..." : body));
            TopupPurchaseResponse t = mapper.readValue(body, TopupPurchaseResponse.class);
            ApiResponseModel getStatus = null;
            getStatus.setStatusCode(200);
            if (checkTransactionStatusenabled==true) {
                getStatus = this.getTransactionStatus("artx", t.getOperator().getReference());
                log.error("Check transaction status after failed ::::::::::::::::::: ", getStatus);
            }

            if (http < 200 || http >= 300) {
                resp.setStatusCode(502);
                resp.setDescription("Provider HTTP error: " + http);
                resp.setOther(body);
                try {
                    if (getStatus.getStatusCode() != 200) {
                        Map<String, Object> rb = rollbackPreDebitExact(pre, auth);

                        Map<String, Object> payload = new java.util.LinkedHashMap<>();
                        payload.put("rollback", rb);

                        resp.setData(payload);  // OK: single-arg, type Object
                    }
                } catch (Exception rbEx) {
                    log.error("Rollback failed", rbEx);
                    // resp.addData("rollbackError", rbEx.getClass().getSimpleName());
                }
                return resp;
            }

            if (looksLikeHtml(body)) {
                resp.setStatusCode(502);
                resp.setDescription("Provider returned HTML instead of JSON");
                resp.setOther(body.length() > 500 ? body.substring(0, 500) + "…" : body);
                try {
                    if (getStatus.getStatusCode() != 200) {
                        Map<String, Object> rb = rollbackPreDebitExact(pre, auth);

                        Map<String, Object> payload = new java.util.LinkedHashMap<>();
                        payload.put("rollback", rb);

                        resp.setData(payload);  // OK: single-arg, type Object
                    }
                } catch (Exception rbEx) {
                    log.error("Rollback failed", rbEx);
                    // resp.addData("rollbackError", rbEx.getClass().getSimpleName());
                }
                return resp;
            }

            boolean ok;
            String errSummary = null;
            Integer errno = t.getErrno();
            Integer sysErr = t.getSysErr();
            if (errno != null) {
                ok = (errno == 0);
                if (!ok) {
                    errSummary = "errno=" + errno + (t.getError() != null ? (", error=" + t.getError()) : "");
                }
            } else if (sysErr != null) {
                ok = (sysErr == 0);
                if (!ok) {
                    errSummary = "sysErr=" + sysErr + (t.getSysId() != null ? (", sysId=" + t.getSysId()) : "");
                }
            } else {
                ok = t.getId() != null && t.getReference() != null;
            }

            if (!ok) {
                try {
                    if (t.getReference() != null) {
                        String checkPath = fetchByUserRefPathPrefix + t.getReference();
                        HttpResponse<String> getCheck = client.get(checkPath);
                        log.info("[sochitel/transaction/check] http={} ref={}", getCheck.statusCode(), t.getReference());
                        if (getCheck.statusCode() == 200 && !looksLikeHtml(getCheck.body())) {
                            TopupPurchaseResponse t2 = mapper.readValue(getCheck.body(), TopupPurchaseResponse.class);
                            Integer e2 = t2.getErrno(), s2 = t2.getSysErr();
                            boolean ok2 = (e2 != null && e2 == 0) || (s2 != null && s2 == 0);
                            if (ok2) {
                                t = t2;
                                ok = true;
                            }
                        }
                    }
                } catch (Exception ignored) {
                    // Best effort; fall through to rollback
                }
            }

            if (!ok) {
                resp.setStatusCode(502);
                resp.setDescription("Provider error" + (errSummary != null ? (": " + errSummary) : ""));
                resp.setOther(body);
                try {
                    if (getStatus.getStatusCode() != 200) {
                        Map<String, Object> rb = rollbackPreDebitExact(pre, auth);

                        Map<String, Object> payload = new java.util.LinkedHashMap<>();
                        payload.put("rollback", rb);

                        resp.setData(payload);  // OK: single-arg, type Object
                    }
                } catch (Exception rbEx) {
                    log.error("Rollback failed", rbEx);
                    // resp.addData("rollbackError", rbEx.getClass().getSimpleName());
                }
                return resp;
            }

            // Success – response body
            resp.setStatusCode(200);
            resp.setDescription("Successful");
            Map<String, Object> data = new HashMap<>();
            if (t.getId() != null) {
                data.put("id", t.getId());
            }
            if (t.getReference() != null) {
                data.put("reference", t.getReference());
            }
            data.put("status", t.getStatus());
            // data.put("balance", t.getBalance());
            // data.put("pin", t.getPin());          // NOTE: sensitive – ensure not logged elsewhere
            data.put("amount", t.getAmount());
            data.put("operator", t.getOperator());
            resp.setData(data);

            // Transaction history (best effort; don’t fail flow)
            try {
                BigDecimal fees = pre.getFees() != null ? pre.getFees() : BigDecimal.ZERO;
                String paymentType = "Airtime Purchase";
                String senderDisplayName = phoneNumber != null ? phoneNumber : "Customer";
                String receiverDisplayName = "Airtime Provider";
                String narration = rq.getCurrencyCode() + " Airtime Purchase (" + rq.getOperator() + "/" + rq.getProduct() + ")";

                recordTxnHistory(
                        pre.getProcessId(),
                        paymentType,
                        pre.getBuyerAccountNumber(),
                        pre.getSellerAccountNumber(),
                        senderDisplayName,
                        receiverDisplayName,
                        new BigDecimal(rq.getAmount()),
                        fees,
                        narration
                );
            } catch (Exception histEx) {
                log.warn("Failed to record transaction history for processId={}", pre.getProcessId(), histEx);
            }

            return resp;

        } catch (Exception ex) {
            log.error("processTrnsaction error", ex);
            resp.setStatusCode(statusCode);
            resp.setDescription(description);
            return resp;
        }
    }

    // ---------- Helpers (internal) ----------
    private void recordTxnHistory(
            String processId,
            String paymentType,
            String senderPhone,
            String receiverPhone,
            String senderName,
            String receiverName,
            BigDecimal amount,
            BigDecimal fees,
            String narration
    ) {
        FinWealthPaymentTransaction tx = new FinWealthPaymentTransaction();
        tx.setTransactionId(processId);
        tx.setPaymentType(paymentType);
        tx.setSender(senderPhone);
        tx.setReceiver(receiverPhone);
        tx.setSenderName(senderName);
        tx.setReceiverName(receiverName);
        tx.setWalletNo(senderPhone);
        tx.setAmmount(amount);
        tx.setFees(fees != null ? fees : BigDecimal.ZERO);
        tx.setSentAmount(amount != null ? amount.toPlainString() : "0");
        tx.setTheNarration(narration);
        tx.setSenderTransactionType("Withdrawal");
        tx.setReceiverTransactionType("Deposit");
        tx.setCreatedDate(Instant.now());

        finWealthPaymentTransactionRepo.save(tx);
    }

    private PreDebitResult preDebitAndSettleAirtime(
            ProcessTrnsactionReq rq,
            String auth,
            String email,
            String phoneNumber,
            BaseResponse feeConfig,
            String processId
    ) {
        PreDebitResult out = new PreDebitResult();
        out.setSuccess(false);

        try {
            if (feeConfig == null || feeConfig.getStatusCode() != 200 || feeConfig.getData() == null) {
                BaseResponse err = new BaseResponse(502, "Invalid fee configuration");
                if (feeConfig != null && feeConfig.getDescription() != null) {
                    err.setDescription(feeConfig.getDescription());
                }
                out.setError(err);
                return out;
            }

            List<AddAccountDetails> acctList = addAccountDetailsRepo.findByEmailAddressrData(email);
            Optional<RegWalletInfo> regOpt = regWalletInfoRepository.findByPhoneNumber(phoneNumber);
            if (regOpt.isEmpty()) {
                out.setError(new BaseResponse(404, "Wallet not found for user"));
                return out;
            }
            RegWalletInfo reg = regOpt.get();

            if (acctList == null || acctList.isEmpty()) {
                out.setError(new BaseResponse(404, "No linked accounts for user"));
                return out;
            }

            String feesStr = String.valueOf(feeConfig.getData().get("fees"));
            BigDecimal fees = new BigDecimal(feesStr);
            BigDecimal receiveAmount = new BigDecimal(rq.getAmount().trim());
            BigDecimal finCharges = receiveAmount.add(fees);

            // Resolve debit account (preserve override to phoneNumber)
            String accountNumber = null;
            for (AddAccountDetails acc : acctList) {
                if (!"CAD".equalsIgnoreCase(rq.getCurrencyCode())) {
                    AddAccountObj newAcc = new AddAccountObj();
                    newAcc.setCountry(acc.getCountryName());
                    newAcc.setCountryCode(acc.getCountryCode());
                    newAcc.setWalletId(reg.getWalletId());
                    BaseResponse addAccRes = profilingProxies.addOtherAccount(newAcc, auth);
                    if (addAccRes.getStatusCode() != 200) {
                        out.setError(new BaseResponse(addAccRes.getStatusCode(), addAccRes.getDescription()));
                        return out;
                    }
                    accountNumber = (String) addAccRes.getData().get("accountNumber");
                } else {
                    accountNumber = acc.getAccountNumber();
                }
                accountNumber = phoneNumber; // override
                break;
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
            debitBuyer.setAuth("Airtime_Buyer");
            debitBuyer.setFees(feesStr);
            debitBuyer.setFinalCHarges(finCharges.toString());
            debitBuyer.setNarration(rq.getCurrencyCode() + "_Withdrawal");
            debitBuyer.setPhoneNumber(accountNumber);
            debitBuyer.setTransAmount(finCharges.toString());
            debitBuyer.setTransactionId(processId);

            BaseResponse debitBuyerRes = transactionServiceProxies.debitCustomerWithType(debitBuyer, "CUSTOMER", auth);
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
            String sellerAcctNumber = AIRTIME_GGL_ACCOUNT;

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
            if (debitGLRes.getStatusCode() != 200) {
                out.setError(new BaseResponse(debitGLRes.getStatusCode(), debitGLRes.getDescription()));
                return out;
            }
            out.setLegGLDebited(true);

            // Credit Seller
            CreditWalletCaller creditSeller = new CreditWalletCaller();
            creditSeller.setAuth("Seller");
            creditSeller.setFees("00");
            creditSeller.setFinalCHarges(receiveAmount.toString());
            creditSeller.setNarration(rq.getCurrencyCode() + "_Deposit");
            creditSeller.setPhoneNumber(sellerAcctNumber);
            creditSeller.setTransAmount(receiveAmount.toString());
            creditSeller.setTransactionId(processId);

            BaseResponse creditSellerRes = transactionServiceProxies.creditCustomerWithType(creditSeller, "CUSTOMER", auth);
            if (creditSellerRes.getStatusCode() != 200) {
                out.setError(new BaseResponse(creditSellerRes.getStatusCode(), creditSellerRes.getDescription()));
                return out;
            }
            out.setLegSellerCredited(true);

            // Credit GL (seller leg)
            CreditWalletCaller glCredit = new CreditWalletCaller();
            glCredit.setAuth(rq.getCurrencyCode());
            glCredit.setFees("0.00");
            glCredit.setFinalCHarges(receiveAmount.toString());
            glCredit.setNarration(rq.getCurrencyCode() + "_Deposit");
            glCredit.setPhoneNumber(decryptedGL);
            glCredit.setTransAmount(receiveAmount.toString());
            glCredit.setTransactionId(creditSeller.getTransactionId());

            BaseResponse creditGLRes = transactionServiceProxies.creditCustomerWithType(glCredit, GGL_CODE + "_GL", auth);
            if (creditGLRes.getStatusCode() != 200) {
                out.setError(new BaseResponse(creditGLRes.getStatusCode(), creditGLRes.getDescription()));
                return out;
            }
            out.setLegGLCredited(true);

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

            return out;

        } catch (Exception e) {
            out.setError(new BaseResponse(500, "Pre-debit/settlement failed: " + e.getClass().getSimpleName()));
            return out;
        }
    }

    private Map<String, Object> rollbackPreDebitExact(PreDebitResult pre, String auth) {
        Map<String, Object> rep = new HashMap<>();
        rep.put("rollbackStarted", true);

        // 1) If GL credited (seller leg) → DEBIT GL
        if (pre.isLegGLCredited()) {
            try {
                DebitWalletCaller r = new DebitWalletCaller();
                r.setAuth(pre.getGglCode());
                r.setFees("0.00");
                r.setFinalCHarges(pre.getReceiveAmount().toString());
                r.setNarration(pre.getGglCode() + "_Deposit_RB");
                r.setPhoneNumber(pre.getGlAccountDecrypted());
                r.setTransAmount(pre.getReceiveAmount().toString());
                r.setTransactionId(pre.getProcessId() + "-RB-GLCREDIT");
                BaseResponse res = transactionServiceProxies.debitCustomerWithType(r, pre.getGglCode() + "_GL", auth);
                rep.put("reverseGLCredit", res.getStatusCode());
            } catch (Exception e) {
                rep.put("reverseGLCredit_error", e.getClass().getSimpleName());
            }
        }

        // 2) If seller credited → DEBIT seller
        if (pre.isLegSellerCredited()) {
            try {
                DebitWalletCaller r = new DebitWalletCaller();
                r.setAuth("Seller");
                r.setFees("0.00");
                r.setFinalCHarges(pre.getReceiveAmount().toString());
                r.setNarration("Seller_Deposit_RB");
                r.setPhoneNumber(pre.getSellerAccountNumber());
                r.setTransAmount(pre.getReceiveAmount().toString());
                r.setTransactionId(pre.getProcessId() + "-RB-SELLERCREDIT");
                BaseResponse res = transactionServiceProxies.debitCustomerWithType(r, "CUSTOMER", auth);
                rep.put("reverseSellerCredit", res.getStatusCode());
            } catch (Exception e) {
                rep.put("reverseSellerCredit_error", e.getClass().getSimpleName());
            }
        }

        // 3) If GL debited (buyer leg) → CREDIT GL
        if (pre.isLegGLDebited()) {
            try {
                CreditWalletCaller r = new CreditWalletCaller();
                r.setAuth(pre.getGglCode());
                r.setFees("0.00");
                r.setFinalCHarges(pre.getReceiveAmount().toString());
                r.setNarration(pre.getGglCode() + "_Debit_RB");
                r.setPhoneNumber(pre.getGlAccountDecrypted());
                r.setTransAmount(pre.getReceiveAmount().toString());
                r.setTransactionId(pre.getProcessId() + "-RB-GLDEBIT");
                BaseResponse res = transactionServiceProxies.creditCustomerWithType(r, pre.getGglCode(), auth);
                rep.put("reverseGLDebit", res.getStatusCode());
            } catch (Exception e) {
                rep.put("reverseGLDebit_error", e.getClass().getSimpleName());
            }
        }

        // 4) If buyer debited → CREDIT buyer
        if (pre.isLegBuyerDebited()) {
            try {
                CreditWalletCaller r = new CreditWalletCaller();
                r.setAuth("Airtime_Buyer");
                r.setFees("0.00");
                r.setFinalCHarges(pre.getFinCharges().toString());
                r.setNarration("Buyer_Withdrawal_RB");
                r.setPhoneNumber(pre.getBuyerAccountNumber());
                r.setTransAmount(pre.getFinCharges().toString());
                r.setTransactionId(pre.getProcessId() + "-RB-BUYERDEBIT");
                BaseResponse res = transactionServiceProxies.creditCustomerWithType(r, "CUSTOMER", auth);
                rep.put("reverseBuyerDebit", res.getStatusCode());
            } catch (Exception e) {
                rep.put("reverseBuyerDebit_error", e.getClass().getSimpleName());
            }
        }

        rep.put("rollbackCompleted", true);
        return rep;
    }

    public ApiResponseModel getTransactionStatus(String refType, String refId) {
        ApiResponseModel resp = new ApiResponseModel();

        try {
            // (0) Basic input validation
            if (refType == null || refType.trim().isEmpty()
                    || refId == null || refId.trim().isEmpty()) {
                resp.setStatusCode(400);
                resp.setDescription("referenceType and referenceId are required");
                return resp;
            }

            String type = refType.trim().toLowerCase(Locale.ROOT);
            String id = refId.trim();

            // Only allow "artx" or "user"
            if (!"artx".equals(type) && !"user".equals(type)) {
                resp.setStatusCode(400);
                resp.setDescription("Invalid transaction reference type (must be 'artx' or 'user')");
                return resp;
            }

            // If ARTX type → must be numeric (per spec)
            if ("artx".equals(type) && !id.matches("\\d+")) {
                resp.setStatusCode(400);
                resp.setDescription("Invalid ARTX transaction ID (must be numeric)");
                return resp;
            }

            // (1) Build signing client
            String pem = loadPem();
            if (pem == null || pem.trim().isEmpty()) {
                resp.setStatusCode(500);
                resp.setDescription("Provider signing key is not configured");
                return resp;
            }
            PrivateKey key = loadPkcs8PrivateKeyFromPem(pem);

            if (serviceSochiBaseUrl == null || serviceSochiBaseUrl.isEmpty()) {
                resp.setStatusCode(500);
                resp.setDescription("Provider base URL not configured");
                return resp;
            }

            final URI base = URI.create(serviceSochiBaseUrl.replaceAll("/+$", ""));
            final String basePath = base.getPath() == null ? "" : base.getPath();
            final String txPrefix = basePath.contains("/api") ? "transaction" : "/api/transaction";

            // /transaction/artx/{id} or /transaction/user/{ref}
            final String path = txPrefix + "/" + type + "/" + id;

            SochitelSignedClient client = new SochitelSignedClient(base, keyId, key);

            // (2) Call provider
            HttpResponse<String> getResp = client.get(path);
            int http = getResp.statusCode();
            String body = getResp.body();

            // Mask refId a bit in logs
            String idMask = id.length() <= 3 ? "***" : "***" + id.substring(id.length() - 3);
            log.info("[sochitel/transaction] http={} path=.../transaction/{}/{}", http, type, idMask);
            log.info("[sochitel/transaction] bodyPreview={}",
                    body == null ? "null" : (body.length() > 600 ? body.substring(0, 600) + "..." : body));

            // (3) HTTP status handling
            if (http == 404) {
                // Follow spec: 404 Not Found
                resp.setStatusCode(404);
                resp.setDescription("Transaction not found");
                resp.setOther(body);
                return resp;
            }

            if (http == 400) {
                // Try to parse errno 103 / 104 etc
                if (!looksLikeHtml(body)) {
                    try {
                        TopupPurchaseResponse tErr = mapper
                                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                                .readValue(body, TopupPurchaseResponse.class);
                        Integer errno = tErr.getErrno();
                        String errorMsg = tErr.getError();
                        resp.setStatusCode(400);
                        if (errno != null) {
                            resp.setDescription("Provider error: errno=" + errno
                                    + (errorMsg != null ? (", error=" + errorMsg) : ""));
                        } else {
                            resp.setDescription("Provider 400 Bad Request");
                        }
                        resp.setOther(body);
                        return resp;
                    } catch (Exception ignore) {
                        // fall-through
                    }
                }
                resp.setStatusCode(400);
                resp.setDescription("Provider 400 Bad Request");
                resp.setOther(body);
                return resp;
            }

            if (http < 200 || http >= 300) {
                resp.setStatusCode(502);
                resp.setDescription("Provider HTTP error: " + http);
                resp.setOther(body);
                return resp;
            }

            // (4) HTML guard
            if (looksLikeHtml(body)) {
                resp.setStatusCode(502);
                resp.setDescription("Provider returned HTML instead of JSON");
                resp.setOther(body.length() > 500 ? body.substring(0, 500) + "…" : body);
                return resp;
            }

            // (5) Parse JSON into TopupPurchaseResponse
            TopupPurchaseResponse t = mapper
                    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                    .readValue(body, TopupPurchaseResponse.class);

            boolean ok;
            String errSummary = null;
            Integer errno = t.getErrno();
            Integer sysErr = t.getSysErr();   // make sure TopupPurchaseResponse has these (Integer sysErr; String sysId)

            if (errno != null) {
                ok = (errno == 0);
                if (!ok) {
                    errSummary = "errno=" + errno + (t.getError() != null ? (", error=" + t.getError()) : "");
                }
            } else if (sysErr != null) {
                ok = (sysErr == 0);
                if (!ok) {
                    errSummary = "sysErr=" + sysErr + (t.getSysId() != null ? (", sysId=" + t.getSysId()) : "");
                }
            } else {
                // Fallback: presence of id/reference is treated as success-ish
                ok = t.getId() != null && t.getReference() != null;
            }

            if (!ok) {
                resp.setStatusCode(502);
                resp.setDescription("Provider error" + (errSummary != null ? (": " + errSummary) : ""));
                resp.setOther(body);
                return resp;
            }

            // (6) Success – just return the full provider object as data
            resp.setStatusCode(200);
            resp.setDescription("Successful");
            resp.setData(t);
            return resp;

        } catch (Exception ex) {
            log.error("getTransactionStatus error", ex);
            resp.setStatusCode(500);
            resp.setDescription("Something went wrong, please retry in a moment.");
            return resp;
        }
    }

}
