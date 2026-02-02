package com.finacial.wealth.api.profiling.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.finacial.wealth.api.profiling.client.model.AuthUserRequest;
import com.finacial.wealth.api.profiling.client.model.CreateUserRequest;
import com.finacial.wealth.api.profiling.client.model.WalletSystemResponse;
import com.finacial.wealth.api.profiling.client.model.WalletUserRequest;
import com.finacial.wealth.api.profiling.domain.AppConfig;
import com.finacial.wealth.api.profiling.limits.GetLedgerSummaryCallerReq;
import com.finacial.wealth.api.profiling.limits.LedgerSummaryRequest;
import com.finacial.wealth.api.profiling.models.GetAcctBalWallet;

import com.finacial.wealth.api.profiling.repo.AppConfigRepo;
import com.finacial.wealth.api.profiling.response.BaseResponse;
import com.finacial.wealth.api.profiling.utils.DecodedJWTToken;
import com.finacial.wealth.api.profiling.utils.StrongAES;
import com.finacial.wealth.api.profiling.utils.UttilityMethods;

import java.net.MalformedURLException;
import java.net.URI;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.function.Consumer;
import javax.annotation.PostConstruct;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import com.finacial.wealth.api.profiling.limits.LedgerSummaryResponse;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;

/**
 *
 * @author victorakinola
 */
@Service
@Slf4j
//@RequiredArgsConstructor
public class WalletSystemProxyService {

    private final UttilityMethods utilMethod;
    // @Autowired
    private final AppConfigRepo appConfigRepo;
    String appConfigValue = "finWealthCreatedSuccessfullyOnWalletService";

    @Value("${fin.wealth.otp.encrypt.key}")
    private String encryptionKey;
    // @Autowired
    private final WebClient webClient;

    @Value("${spring.profiles.active}")
    private String environment;

    private final RestTemplate restTemplate;

    @Autowired
    public WalletSystemProxyService(UttilityMethods utilMethod,
            WebClient webClient, AppConfigRepo appConfigRepo,
            RestTemplate restTemplate) {
        this.utilMethod = utilMethod;
        this.webClient = webClient;
        this.appConfigRepo = appConfigRepo;
        this.restTemplate = restTemplate;
    }

    private final String CREATE_AS_WALLET_USER = "/profilings/usermgt/create-user";
    private final String AUTHENTICATE_AS_WALLET_USER = "/session-manager/session/authenticate/user";
    private final String ADD_WALLET_NO_TO_WALLET_SYSTEM = "/generalledger/add-wallet-no";
    private final String QUERY_WALLET_NO_EXISTENCE = "/generalledger/check-if-wallet-exists";
    private final String GET_ACCT_BAL = "/generalledger/v2/get-account-info";
    private final String WALLET_DEBIT_WALLET = "/generalledger/v2/debit-wallet-account";
    private final String WALLET_CREDIT_WALLET = "/generalledger/v2/credit-wallet-account";
    private final String LEDGER_SUMMARY_PATH = "/generalledger/v2/summary";
    private static final String CHANNEL = "Api";

    Gson gson = new Gson();

    Consumer<HttpHeaders> headers;

    @PostConstruct
    public void init() throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {

        log.info("Invoking create-user API for ...");

        List<AppConfig> getConfigIs = appConfigRepo.findByConfigValue(appConfigValue);
        if (getConfigIs.size() <= 0) {

            WalletSystemResponse createUserApiResponse = createUser();

            log.info("createUser API Response  ::  {}", createUserApiResponse);
        } else {

            log.info("Fellow already created as a Client ...");

        }

    }

    private ResponseEntity<WalletSystemResponse> createUser(CreateUserRequest createUserRequest) throws MalformedURLException, Exception {
        headers = setDefaultHeaders();

        URI uri = UriComponentsBuilder.fromHttpUrl(utilMethod.getWALLET_SYSTEM_BASE_URL()
                .concat(CREATE_AS_WALLET_USER)).build().toUri();

        log.info("createUser API URI: {}", uri.toURL().toExternalForm());

        ResponseEntity<WalletSystemResponse> responseEntity = webClient.post()
                .uri(uri)
                .headers(headers)
                .bodyValue(createUserRequest)
                .retrieve()
                .toEntity(WalletSystemResponse.class)
                .block();

        return responseEntity;
    }

    public ResponseEntity<WalletSystemResponse> authenticateUser(AuthUserRequest authUserRequest) throws MalformedURLException {
        headers = setDefaultHeaders();
        // WebClient webClient = null;

        URI uri = UriComponentsBuilder.fromHttpUrl(utilMethod.getWALLET_SYSTEM_BASE_URL()
                .concat(AUTHENTICATE_AS_WALLET_USER)).build().toUri();

        log.info("authenticateUser API URI: {}", uri.toURL().toExternalForm());

        ResponseEntity<WalletSystemResponse> responseEntity = webClient.post()
                .uri(uri)
                .headers(headers)
                .bodyValue(authUserRequest)
                .retrieve()
                .toEntity(WalletSystemResponse.class)
                .block();

        return responseEntity;
    }

    public ResponseEntity<WalletSystemResponse> addWalletNo(WalletUserRequest walletUserRequest) throws MalformedURLException {
        log.info("addWalletNo :: WalletUserRequest payload: {}", walletUserRequest);
        headers = setDefaultHeaders();
        // WebClient webClient = null;

        URI uri = UriComponentsBuilder.fromHttpUrl(utilMethod.getWALLET_SYSTEM_BASE_URL()
                .concat(ADD_WALLET_NO_TO_WALLET_SYSTEM)).build().toUri();

        log.info("addWalletNo API URI: {}", uri.toURL().toExternalForm());

        String token = walletUserRequest.getToken();

        ResponseEntity<WalletSystemResponse> responseEntity = webClient.post()
                .uri(uri)
                .headers(headers)
                .headers(h -> h.setBearerAuth(token))
                .bodyValue(walletUserRequest)
                .retrieve()
                .toEntity(WalletSystemResponse.class)
                .block();

        return responseEntity;
    }

    public ResponseEntity<WalletSystemResponse> checkIfWalletNoExists(WalletUserRequest walletUserRequest) throws MalformedURLException {
        log.info("checkIfWalletNoExists :: WalletUserRequest payload: {}", walletUserRequest);
        headers = setDefaultHeaders();
        //WebClient webClient = null;

        URI uri = UriComponentsBuilder.fromHttpUrl(utilMethod.getWALLET_SYSTEM_BASE_URL()
                .concat(QUERY_WALLET_NO_EXISTENCE)).build().toUri();

        log.info("checkIfWalletNoExists API URI: {}", uri.toURL().toExternalForm());

        String token = walletUserRequest.getToken();

        ResponseEntity<WalletSystemResponse> responseEntity = webClient.post()
                .uri(uri)
                .headers(headers)
                .headers(h -> h.setBearerAuth(token))
                .bodyValue(walletUserRequest)
                .retrieve()
                .toEntity(WalletSystemResponse.class)
                .block();

        return responseEntity;
    }

    private WalletSystemResponse createUser() throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        WalletSystemResponse wsResponse = new WalletSystemResponse(500, "Error creating FellowPay User on the Wallet-System!");
        CreateUserRequest createUserRequest = new CreateUserRequest();
        createUserRequest.setProductName(utilMethod.getWALLET_SYSTEM_PRODUCTNAME());
        createUserRequest.setPassword(decryptData(utilMethod.getWALLET_SYSTEM_PASSWORD()));
        createUserRequest.setConfPassword(decryptData(utilMethod.getWALLET_SYSTEM_PASSWORD()));
        createUserRequest.setEmailAddress(utilMethod.getWALLET_SYSTEM_EMAIL());
        createUserRequest.setClearanceId(decryptData(utilMethod.getWALLET_SYSTEM_CLEARANCEID()));

        log.info("createUserRequest payload: {}", createUserRequest);

        try {
            ResponseEntity<WalletSystemResponse> resEntity = createUser(createUserRequest);
            if (resEntity != null && resEntity.hasBody()) {

                WalletSystemResponse userStatus = resEntity.getBody();
                log.info("userStatus :: {}", userStatus);
                if (userStatus.getStatusCode() == 200) {

                    AppConfig setApp = new AppConfig();
                    setApp.setConfigDescription("FinWealth created as client successfully on Wallet Service.");
                    setApp.setConfigName(appConfigValue);
                    setApp.setConfigValue(appConfigValue);
                    appConfigRepo.save(setApp);
                    //log user created successgfully
                }

                return resEntity.getBody();
            }
        } catch (MalformedURLException e) {
            log.error("Please check the create-user endpoint URI.", e);
            return wsResponse;
        } catch (Exception e) {
            log.error("Error invoking the create-user endpoint.", e);
            return wsResponse;
        }
        return wsResponse;
    }

    private Consumer<HttpHeaders> setDefaultHeaders() {
        HttpHeaders defaultHeaders = new HttpHeaders();
        defaultHeaders.add("cache-control", "no-cache");
        defaultHeaders.add("channel", "API");
        defaultHeaders.add("accept", "application/json");
        defaultHeaders.add("content-type", "application/json");
        LinkedMultiValueMap mvmap = new LinkedMultiValueMap<>(defaultHeaders);
        Consumer<HttpHeaders> consumer = it -> it.addAll(mvmap);
        return consumer;
    }

    private String decryptData(String data) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        if (data == null) {
            throw new IllegalArgumentException("Cannot decrypt: input is null");
        }
        String decryptData = StrongAES.decrypt(data, encryptionKey);
        return decryptData;

    }

    public BaseResponse getAccountBalance(GetAcctBalWallet rq, String token) {
        BaseResponse response = new BaseResponse();
        String statusMessage = "An error occurred, please try again";
        int statusCode = 500;
        try {
            String baseUrl = utilMethod.getWALLET_SYSTEM_BASE_URL();

            /*
             System.out.println( " CreateVirtualAcctProvidus reeq :::::::::::::::: ::::: %S " + new
             Gson().toJson(cProvReq));
             */
            String requestJson = "{\"productCode\":\"" + rq.getProductCode() + "\","
                    + "\"phoneNumber\":\"" + rq.getPhoneNumber() + "\"}";

            HttpHeaders headers2 = new HttpHeaders();
            headers2.setContentType(MediaType.APPLICATION_JSON);
            headers2.add("Authorization", "Bearer " + token);
            headers2.add("channel", "Api");

            String url = baseUrl + GET_ACCT_BAL;
            HttpEntity<String> entity = new HttpEntity<>(requestJson, headers2);

            RestTemplate withoutEurekarestTemplate = new RestTemplate();

            String walletInfoJson = withoutEurekarestTemplate.postForObject(url, entity, String.class);

            log.info("walletInfoJson :: {}", walletInfoJson);

            BaseResponse resFin = gson.fromJson(walletInfoJson, BaseResponse.class);
            /*
             System.out.println( " ProvidusCreateStaticAccountRes resFin :::::::::::::::: %S " + new
             Gson().toJson(resFin));
             */

            if (resFin.getStatusCode() == 200) {

                response.setData(resFin.getData());
                response.setStatusCode(200);
                response.setDescription(resFin.getDescription());
            } else {
                response.setStatusCode(HttpServletResponse.SC_BAD_REQUEST);
                response.setDescription(resFin.getDescription());
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            response.setStatusCode(statusCode);
            response.setDescription(statusMessage);
        }
        log.info("getWalletInfo response :: {}", response);
        return response;

    }

    public BaseResponse getAccountBalanceCaller(String auth) {
        BaseResponse responseModel = new BaseResponse();
        String statusMessage = "An error occurred, please try again";
        int statusCode = 500;

        try {
            //DecodedJWTToken getDecoded = DecodedJWTToken.getDecoded(auth);
            //  String phoneNumber = getDecoded.phoneNumber;
            statusCode = 400;

            DecodedJWTToken getDecoded = DecodedJWTToken.getDecoded(auth);

            AuthUserRequest authUserRequest = new AuthUserRequest();
            authUserRequest.setEmailAddress(utilMethod.getWALLET_SYSTEM_EMAIL());
            authUserRequest.setPassword(decryptData(utilMethod.getWALLET_SYSTEM_PASSWORD()));

            ResponseEntity<WalletSystemResponse> walletSystemResponse = authenticateUser(authUserRequest);
            String token = null;
            String productCode = null;

            if (walletSystemResponse != null && walletSystemResponse.hasBody()) {

                WalletSystemResponse userStatus = walletSystemResponse.getBody();
                log.info("userStatus :: {}", userStatus);
                if (userStatus.getStatusCode() != 200) {

                    responseModel.setDescription("Wallet Info:, " + userStatus.getDescription());
                    responseModel.setStatusCode(userStatus.getStatusCode());
                    return responseModel;
                }

                log.info("authenticateUser response ::::: {} ", userStatus);

                token = userStatus.getData().getIdToken();
                productCode = userStatus.getData().getProductCode();

            }

            GetAcctBalWallet addWa = new GetAcctBalWallet();
            addWa.setPhoneNumber(getDecoded.phoneNumber);
            addWa.setProductCode(productCode);
            BaseResponse walletInfo = getAccountBalance(addWa, token);
            if (walletInfo.getStatusCode() != 200) {
                responseModel.setStatusCode(walletInfo.getStatusCode());
                responseModel.setDescription(walletInfo.getDescription());
                return responseModel;
            }

            log.info("Wallet Info :: {}", walletInfo);
            return walletInfo;
        } catch (Exception e) {
            responseModel.setDescription(statusMessage);
            responseModel.setStatusCode(statusCode);
            return responseModel;
        }
    }

    public BaseResponse getAccountBalanceCallerPhoneNumber(String phoneNumber) {
        BaseResponse responseModel = new BaseResponse();
        String statusMessage = "An error occurred, please try again";
        int statusCode = 500;

        try {
            //DecodedJWTToken getDecoded = DecodedJWTToken.getDecoded(auth);
            //  String phoneNumber = getDecoded.phoneNumber;
            statusCode = 400;

            AuthUserRequest authUserRequest = new AuthUserRequest();
            authUserRequest.setEmailAddress(utilMethod.getWALLET_SYSTEM_EMAIL());
            authUserRequest.setPassword(decryptData(utilMethod.getWALLET_SYSTEM_PASSWORD()));

            ResponseEntity<WalletSystemResponse> walletSystemResponse = authenticateUser(authUserRequest);
            String token = null;
            String productCode = null;

            if (walletSystemResponse != null && walletSystemResponse.hasBody()) {

                WalletSystemResponse userStatus = walletSystemResponse.getBody();
                if (environment.equals("dev")) {
                    log.info("userStatus :: {}", userStatus);
                }
                if (userStatus.getStatusCode() != 200) {

                    responseModel.setDescription("Wallet Info:, " + userStatus.getDescription());
                    responseModel.setStatusCode(userStatus.getStatusCode());
                    return responseModel;
                }
                if (environment.equals("dev")) {
                    log.info("authenticateUser response ::::: {} ", userStatus);
                }

                token = userStatus.getData().getIdToken();
                productCode = userStatus.getData().getProductCode();

            }

            GetAcctBalWallet addWa = new GetAcctBalWallet();
            addWa.setPhoneNumber(phoneNumber);
            addWa.setProductCode(productCode);
            BaseResponse walletInfo = getAccountBalance(addWa, token);
            if (walletInfo.getStatusCode() != 200) {
                responseModel.setStatusCode(walletInfo.getStatusCode());
                responseModel.setDescription(walletInfo.getDescription());
                return responseModel;
            }
            if (environment.equals("dev")) {

                log.info("Wallet Info :: {}", walletInfo);
            }
            return walletInfo;
        } catch (Exception e) {
            responseModel.setDescription(statusMessage);
            responseModel.setStatusCode(statusCode);
            return responseModel;
        }
    }

    public LedgerSummaryResponse getLedgerSummaryCaller(LedgerSummaryRequest request) {
        try {
            // 1) Authenticate to wallet system
            AuthUserRequest authReq = new AuthUserRequest();
            authReq.setEmailAddress(utilMethod.getWALLET_SYSTEM_EMAIL());
            authReq.setPassword(decryptData(utilMethod.getWALLET_SYSTEM_PASSWORD()));

            ResponseEntity<WalletSystemResponse> authResp = authenticateUser(authReq);

            if (authResp == null || !authResp.hasBody() || authResp.getBody() == null) {
                return failure("Wallet system auth returned empty response", 502);
            }

            WalletSystemResponse authBody = authResp.getBody();
            log.info("Wallet system auth response: {}", authBody);

            if (authBody.getStatusCode() != 200 || authBody.getData() == null) {
                String msg = authBody.getDescription() != null ? authBody.getDescription() : "Wallet system auth failed";
                return failure(msg, 401);
            }

            String token = authBody.getData().getIdToken();
            String productCode = authBody.getData().getProductCode();

            if (token == null || token.trim().isEmpty()) {
                return failure("Wallet system auth did not return token", 400);
            }
            if (productCode == null || productCode.trim().isEmpty()) {
                return failure("Wallet system auth did not return productCode", 400);
            }

            // 2) Enrich request with productCode
            request.setProductCode(productCode);

            // 3) Call ledger summary (downstream)
            LedgerSummaryResponse ledgerResp = getLedgerSummary(request, token);

            if (ledgerResp == null) {
                return failure("Ledger summary returned empty response", 400);
            }

            return ledgerResp;

        } catch (Exception e) {
            log.error("getLedgerSummaryCaller failed. request={}", request, e);
            return failure("An error occurred, please try again", 500);
        }
    }

    public LedgerSummaryResponse getLedgerSummary(LedgerSummaryRequest request, String token) {
        final String baseUrl = utilMethod.getWALLET_SYSTEM_BASE_URL();
        final String url = baseUrl + LEDGER_SUMMARY_PATH;

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", token);   // important: standard header name
            headers.set("channel", CHANNEL);

            HttpEntity<LedgerSummaryRequest> entity = new HttpEntity<>(request, headers);

            ResponseEntity<LedgerSummaryResponse> resp
                    = restTemplate.exchange(url, HttpMethod.POST, entity, LedgerSummaryResponse.class);

            log.info("LedgerSummary status  : {}", resp.getStatusCode());
            log.info("LedgerSummary headers : {}", resp.getHeaders());
            log.info("LedgerSummary body    : {}", resp.getBody());
            ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();

            LedgerSummaryResponse body = resp.getBody();
            log.info("LedgerSummaryResponse JSON:\n{}",
                    mapper.writerWithDefaultPrettyPrinter()
                            .writeValueAsString(body));
            if (body == null) {
                return failure("Ledger service returned empty response", 400);
            }

            return body;

        } catch (HttpStatusCodeException ex) {
            // Downstream returned 4xx/5xx
            log.error("Ledger summary call failed. url={} status={} body={}",
                    url, ex.getStatusCode(), ex.getResponseBodyAsString(), ex);

            return failure("Ledger service error: " + ex.getStatusCode(), 502);

        } catch (ResourceAccessException ex) {
            // Connection timeout / DNS / refused
            log.error("Ledger summary call timeout/unreachable. url={}", url, ex);
            return failure("Ledger service unreachable", 504);

        } catch (Exception ex) {
            log.error("Ledger summary call unexpected failure. url={}", url, ex);
            return failure("An error occurred, please try again", 500);
        }
    }

    private LedgerSummaryResponse failure(String msg, int code) {
        LedgerSummaryResponse r = new LedgerSummaryResponse();
        r.setStatusCode(code);
        r.setDescription(msg);
        return r;
    }

}
