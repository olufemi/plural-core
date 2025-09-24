package com.finacial.wealth.api.utility.services;

import com.finacial.wealth.api.utility.client.model.AuthUserRequest;
import com.finacial.wealth.api.utility.client.model.CreateUserRequest;
import com.finacial.wealth.api.utility.client.model.WalletSystemResponse;
import com.finacial.wealth.api.utility.client.model.WalletUserRequest;
import com.finacial.wealth.api.utility.domains.AppConfig;
import com.finacial.wealth.api.utility.models.CreditWallet;
import com.finacial.wealth.api.utility.models.CreditWalletCaller;
import com.finacial.wealth.api.utility.models.DebitWallet;
import com.finacial.wealth.api.utility.models.DebitWalletCaller;
import com.finacial.wealth.api.utility.models.GetAcctBalWallet;
import com.finacial.wealth.api.utility.repository.AppConfigRepo;
import com.finacial.wealth.api.utility.response.BaseResponse;
import com.finacial.wealth.api.utility.utils.DecodedJWTToken;
import com.finacial.wealth.api.utility.utils.StrongAES;
import com.finacial.wealth.api.utility.utils.UttilityMethods;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import java.math.BigDecimal;

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

    @Autowired
    public WalletSystemProxyService(UttilityMethods utilMethod,
            WebClient webClient, AppConfigRepo appConfigRepo) {
        this.utilMethod = utilMethod;
        this.webClient = webClient;
        this.appConfigRepo = appConfigRepo;
    }

    private final String CREATE_AS_WALLET_USER = "/profilings/usermgt/create-user";
    private final String AUTHENTICATE_AS_WALLET_USER = "/session-manager/session/authenticate/user";
    private final String ADD_WALLET_NO_TO_WALLET_SYSTEM = "/generalledger/add-wallet-no";
    private final String QUERY_WALLET_NO_EXISTENCE = "/generalledger/check-if-wallet-exists";
    private final String GET_ACCT_BAL = "/generalledger/get-account-info";
    private final String WALLET_DEBIT_WALLET = "/generalledger/debit-wallet-account";
    private final String WALLET_CREDIT_WALLET = "/generalledger/credit-wallet-account";
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
                    setApp.setConfigDescription("Fellow created as client successfully on Wallet Service.");
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

            System.out.println(" CreateVirtualAcctProvidus reeq :::::::::::::::: ::::: %S " + requestJson);

            HttpHeaders headers2 = new HttpHeaders();
            headers2.setContentType(MediaType.APPLICATION_JSON);
            headers2.add("Authorization", "Bearer " + token);
            headers2.add("channel", "Api");

            String url = baseUrl + GET_ACCT_BAL;
            HttpEntity<String> entity = new HttpEntity<>(requestJson, headers2);

            RestTemplate withoutEurekarestTemplate = new RestTemplate();

            String walletInfoJson = withoutEurekarestTemplate.postForObject(url, entity, String.class);
            if (environment.equals("dev")) {
                log.info("walletInfoJson :: {}", walletInfoJson);
            }

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
            addWa.setPhoneNumber(getDecoded.phoneNumber);
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

    public BaseResponse debitWalletPayOut(DebitWallet rq, String token) {
        BaseResponse response = new BaseResponse();
        String statusMessage = "An error occured,please try again";
        int statusCode = 500;
        try {
            String url = utilMethod.getWALLET_SYSTEM_BASE_URL() + WALLET_DEBIT_WALLET;

            log.info("debitWallet token: {}", token);

            /*
             System.out.println( " CreateVirtualAcctProvidus reeq :::::::::::::::: ::::: %S " + new
             Gson().toJson(cProvReq));
             */
            String requestJson = "{\"fees\":\"" + rq.getFees() + "\","
                    + "\"transAmount\":\"" + rq.getTransAmount() + "\","
                    + "\"finalCHarges\":\"" + rq.getFinalCHarges() + "\","
                    + "\"transactionId\":\"" + rq.getTransactionId() + "\","
                    + "\"transType\":\"" + rq.getTransType() + "\","
                    + "\"narration\":\"" + rq.getNarration() + "\","
                    + "\"phoneNumber\":\"" + rq.getPhoneNumber() + "\","
                    + "\"itsPayOutTransaction\":\"" + rq.getItsPayOutTransaction() + "\","
                    + "\"productCode\":\"" + rq.getProductCode() + "\"}";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.add("Authorization", "Bearer " + token);
            headers.add("channel", "Api");

            HttpEntity<String> entity = new HttpEntity<>(requestJson, headers);

            RestTemplate withoutEurekarestTemplate = new RestTemplate();

            BaseResponse resFin = gson.fromJson(withoutEurekarestTemplate.postForObject(
                    url,
                    entity,
                    String.class), BaseResponse.class);

            if (environment.equals("dev")) {
                log.info("resFin response: {}", resFin);
            }
            /*
             System.out.println( " ProvidusCreateStaticAccountRes resFin :::::::::::::::: %S " + new
             Gson().toJson(resFin));
             */

            if (resFin.getStatusCode() == 200) {

                response.setData(resFin.getData());
                response.setStatusCode(200);
                response.setDescription(resFin.getDescription());
                return response;
            } else {
                response.setStatusCode(HttpServletResponse.SC_BAD_REQUEST);
                response.setDescription(resFin.getDescription());
            }
        } catch (JsonSyntaxException | RestClientException ex) {
            log.error("debitWallet Exception ::: " + ex.getMessage(), ex);
            response.setStatusCode(statusCode);
            response.setDescription(statusMessage);
        }

        return response;

    }

    public BaseResponse debitWalletPayOutCaller(DebitWalletCaller rq) throws MalformedURLException {
        BaseResponse responseModel = new BaseResponse();
        String statusMessage = "An error occurred, please try again";
        int statusCode = 500;
        try {
            statusCode = 400;
            //  String phoneNumber = getDecoded.phoneNumber;
            DecodedJWTToken getDecoded = DecodedJWTToken.getDecoded(rq.getAuth());

            if (!getDecoded.phoneNumber.equals(rq.getPhoneNumber())) {

                responseModel.setDescription("Wallet Info:, " + "invalid Client, please use correct token");
                responseModel.setStatusCode(statusCode);
                return responseModel;
            }

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
                    if (environment.equals("dev")) {
                        log.info("debitWalletPayOut wallet responseModel :: {}", responseModel);
                    }
                    return responseModel;
                }
                if (environment.equals("dev")) {
                    log.info("authenticateUser response ::::: {} ", userStatus);
                }

                token = userStatus.getData().getIdToken();
                productCode = userStatus.getData().getProductCode();

            }

            BigDecimal getFinalChrg = new BigDecimal(rq.getFees()).add(new BigDecimal(rq.getTransAmount()));
            BigDecimal entteredFinalChrg = new BigDecimal(rq.getFinalCHarges());

            if (getFinalChrg.compareTo(entteredFinalChrg) != 0) {
                System.out.println("Values are NOT equal");
                responseModel.setDescription("Invalid transaction, FinalCharges must equal total amount plus fees.");
                responseModel.setStatusCode(400);
                return responseModel;
            }

            DebitWallet rqqq = new DebitWallet();
            rqqq.setFees(rq.getFees());
            rqqq.setFinalCHarges(rq.getFinalCHarges());
            rqqq.setItsPayOutTransaction("1");
            rqqq.setNarration(rq.getNarration());
            rqqq.setPhoneNumber(rq.getPhoneNumber());
            rqqq.setProductCode(productCode);
            rqqq.setTransAmount(rq.getTransAmount());
            rqqq.setTransType("Withdrawal");
            rqqq.setTransactionId(rq.getTransactionId());

            BaseResponse creBase = debitWalletPayOut(rqqq, token);

            if (creBase.getStatusCode() == 200) {
                responseModel.setDescription(creBase.getDescription());
                responseModel.setStatusCode(200);
                if (environment.equals("dev")) {
                    log.info("debitWalletPayOutCaller wallet responseModel :: {}", responseModel);
                    return responseModel;
                }
            } else {

                responseModel.setDescription(creBase.getDescription());
                responseModel.setStatusCode(creBase.getStatusCode());
                if (environment.equals("dev")) {
                    log.info("debitWalletPayOutCaller wallet responseModel :: {}", responseModel);
                }
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            responseModel.setDescription(statusMessage);
            responseModel.setStatusCode(500);
        }

        //  log.info("Wallet Info :: {}", walletInfo);
        return responseModel;
    }

    public BaseResponse debitWalletPayOutCallerPhone(DebitWalletCaller rq) throws MalformedURLException {
        BaseResponse responseModel = new BaseResponse();
        String statusMessage = "An error occurred, please try again";
        int statusCode = 500;
        try {
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

            BigDecimal getFinalChrg = new BigDecimal(rq.getFees()).add(new BigDecimal(rq.getTransAmount()));
            BigDecimal entteredFinalChrg = new BigDecimal(rq.getFinalCHarges());

            if (getFinalChrg.compareTo(entteredFinalChrg) != 0) {
                System.out.println("Values are NOT equal");
                responseModel.setDescription("Invalid transaction, FinalCharges must equal total amount plus fees.");
                responseModel.setStatusCode(400);
                return responseModel;
            }

            DebitWallet rqqq = new DebitWallet();
            rqqq.setFees(rq.getFees());
            rqqq.setFinalCHarges(rq.getFinalCHarges());
            rqqq.setItsPayOutTransaction("1");
            rqqq.setNarration(rq.getNarration());
            rqqq.setPhoneNumber(rq.getPhoneNumber());
            rqqq.setProductCode(productCode);
            rqqq.setTransAmount(rq.getTransAmount());
            rqqq.setTransType("Withdrawal");
            rqqq.setTransactionId(rq.getTransactionId());

            BaseResponse creBase = debitWalletPayOut(rqqq, token);
            if (environment.equals("dev")) {
                log.info("creBase response :: {}", creBase);
            }

            if (creBase.getStatusCode() == 200) {
                responseModel.setDescription(creBase.getDescription());
                responseModel.setStatusCode(200);
                if (environment.equals("dev")) {
                    log.info("debitWalletPayOutCallerPhone wallet responseModel :: {}", responseModel);
                }
                return responseModel;
            } else {

                responseModel.setDescription(creBase.getDescription());
                responseModel.setStatusCode(creBase.getStatusCode());
            }
            if (environment.equals("dev")) {
                log.info("debitWalletPayOutCallerPhone wallet responseModel :: {}", responseModel);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            responseModel.setDescription(statusMessage);
            responseModel.setStatusCode(statusCode);
        }

        // log.info("Wallet Info :: {}", walletInfo);
        return responseModel;
    }

    public BaseResponse creditWalletPayIn(CreditWallet rq, String token) {
        BaseResponse response = new BaseResponse();
        String statusMessage = "An error occured,please try again";
        int statusCode = 500;
        try {
            String url = utilMethod.getWALLET_SYSTEM_BASE_URL() + WALLET_CREDIT_WALLET;

            log.info("debitWallet token: {}", token);

            /*
             System.out.println( " CreateVirtualAcctProvidus reeq :::::::::::::::: ::::: %S " + new
             Gson().toJson(cProvReq));
             */
            String requestJson = "{\"fees\":\"" + rq.getFees() + "\","
                    + "\"transAmount\":\"" + rq.getTransAmount() + "\","
                    + "\"finalCHarges\":\"" + rq.getFinalCHarges() + "\","
                    + "\"transactionId\":\"" + rq.getTransactionId() + "\","
                    + "\"transType\":\"" + rq.getTransType() + "\","
                    + "\"narration\":\"" + rq.getNarration() + "\","
                    + "\"phoneNumber\":\"" + rq.getPhoneNumber() + "\","
                    + "\"productCode\":\"" + rq.getProductCode() + "\"}";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.add("Authorization", "Bearer " + token);
            headers.add("channel", "Api");

            HttpEntity<String> entity = new HttpEntity<>(requestJson, headers);

            RestTemplate withoutEurekarestTemplate = new RestTemplate();

            BaseResponse resFin = gson.fromJson(withoutEurekarestTemplate.postForObject(
                    url,
                    entity,
                    String.class), BaseResponse.class);
            if (environment.equals("dev")) {
                log.info("resFin response: {}", resFin);
            }
            /*
             System.out.println( " ProvidusCreateStaticAccountRes resFin :::::::::::::::: %S " + new
             Gson().toJson(resFin));
             */

            if (resFin.getStatusCode() == 200) {

                response.setData(resFin.getData());
                response.setStatusCode(200);
                response.setDescription(resFin.getDescription());
                if (environment.equals("dev")) {
                    log.info("credit wallet responseModel :: {}", response);
                }
                return response;
            } else {
                response.setStatusCode(HttpServletResponse.SC_BAD_REQUEST);
                response.setDescription(resFin.getDescription());
            }
        } catch (JsonSyntaxException | RestClientException ex) {
            log.error("debitWallet Exception ::: " + ex.getMessage(), ex);
            response.setStatusCode(statusCode);
            response.setDescription(statusMessage);
        }

        return response;

    }

    public BaseResponse creditWalletCaller(CreditWalletCaller rq) throws MalformedURLException {
        BaseResponse responseModel = new BaseResponse();
        String statusMessage = "An error occurred, please try again";
        int statusCode = 500;
        try {
            statusCode = 400;
            //  String phoneNumber = getDecoded.phoneNumber;
            DecodedJWTToken getDecoded = DecodedJWTToken.getDecoded(rq.getAuth());

            if (!getDecoded.phoneNumber.equals(rq.getPhoneNumber())) {

                responseModel.setDescription("Wallet Info:, " + "invalid Client, please use correct token");
                responseModel.setStatusCode(statusCode);
                return responseModel;
            }

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
                    if (environment.equals("dev")) {
                        log.info("credit wallet calleer responseModel :: {}", responseModel);
                    }
                    return responseModel;
                }
                if (environment.equals("dev")) {
                    log.info("authenticateUser response ::::: {} ", userStatus);
                }

                token = userStatus.getData().getIdToken();
                productCode = userStatus.getData().getProductCode();

            }

            BigDecimal getFinalChrg = new BigDecimal(rq.getTransAmount()).subtract(new BigDecimal(rq.getFees()));
            BigDecimal entteredFinalChrg = new BigDecimal(rq.getFinalCHarges());

            if (getFinalChrg.compareTo(entteredFinalChrg) != 0) {
                System.out.println("Values are NOT equal");
                responseModel.setDescription("Invalid transaction, FinalCharges must equal total amount minus fees");
                responseModel.setStatusCode(400);
                return responseModel;
            }
            CreditWallet rqqq = new CreditWallet();
            rqqq.setFees(rq.getFees());
            rqqq.setFinalCHarges(rq.getFinalCHarges());

            rqqq.setNarration(rq.getNarration());
            rqqq.setPhoneNumber(rq.getPhoneNumber());
            rqqq.setProductCode(productCode);
            rqqq.setTransAmount(rq.getTransAmount());
            rqqq.setTransType("Deposit");
            rqqq.setTransactionId(rq.getTransactionId());

            BaseResponse creBase = creditWalletPayIn(rqqq, token);

            if (creBase.getStatusCode() == 200) {
                responseModel.setDescription(creBase.getDescription());
                responseModel.setStatusCode(200);
            } else {

                responseModel.setDescription(creBase.getDescription());
                responseModel.setStatusCode(creBase.getStatusCode());
            }
            if (environment.equals("dev")) {
                log.info("creditWalletCaller wallet responseModel :: {}", responseModel);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            responseModel.setDescription(statusMessage);
            responseModel.setStatusCode(500);
        }

        //  log.info("Wallet Info :: {}", walletInfo);
        return responseModel;
    }

    public BaseResponse creditWalletCallerPhn(CreditWalletCaller rq) throws MalformedURLException {
        BaseResponse responseModel = new BaseResponse();
        String statusMessage = "An error occurred, please try again";
        int statusCode = 500;
        try {
            statusCode = 400;
            //  String phoneNumber = getDecoded.phoneNumber;

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
                    if (environment.equals("dev")) {
                        log.info("credit wallet calleer phone responseModel :: {}", responseModel);
                    }
                    return responseModel;
                }
                if (environment.equals("dev")) {
                    log.info("authenticateUser response ::::: {} ", userStatus);
                }

                token = userStatus.getData().getIdToken();
                productCode = userStatus.getData().getProductCode();

            }

            //validate that the final charges = amount + fees
            BigDecimal getFinalChrg = new BigDecimal(rq.getTransAmount()).subtract(new BigDecimal(rq.getFees()));
            BigDecimal entteredFinalChrg = new BigDecimal(rq.getFinalCHarges());

            if (getFinalChrg.compareTo(entteredFinalChrg) != 0) {
                System.out.println("Values are NOT equal");
                responseModel.setDescription("Invalid transaction, FinalCharges must equal total amount minus fees");
                responseModel.setStatusCode(400);
                return responseModel;
            }

            CreditWallet rqqq = new CreditWallet();
            rqqq.setFees(rq.getFees());
            rqqq.setFinalCHarges(rq.getFinalCHarges());

            rqqq.setNarration(rq.getNarration());
            rqqq.setPhoneNumber(rq.getPhoneNumber());
            rqqq.setProductCode(productCode);
            rqqq.setTransAmount(rq.getTransAmount());
            rqqq.setTransType("Deposit");
            rqqq.setTransactionId(rq.getTransactionId());

            BaseResponse creBase = creditWalletPayIn(rqqq, token);

            if (creBase.getStatusCode() == 200) {
                responseModel.setDescription(creBase.getDescription());
                responseModel.setStatusCode(200);
            } else {

                responseModel.setDescription(creBase.getDescription());
                responseModel.setStatusCode(creBase.getStatusCode());
            }
            log.info("creditWalletCallerPhn wallet phone responseModel :: {}", responseModel);

        } catch (Exception ex) {
            ex.printStackTrace();
            responseModel.setDescription(statusMessage);
            responseModel.setStatusCode(statusCode);
        }

        //  log.info("Wallet Info :: {}", walletInfo);
        return responseModel;
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

}
