package com.finacial.wealth.api.sessionmanager.services;

import com.finacial.wealth.api.sessionmanager.entities.AddAccountDetails;
import com.finacial.wealth.api.sessionmanager.entities.DeviceDetails;
import com.finacial.wealth.api.sessionmanager.entities.PeerToPeerFxReferral;
import com.google.gson.Gson;
import com.finacial.wealth.api.sessionmanager.entities.RegWalletCheckLog;
import com.finacial.wealth.api.sessionmanager.entities.RegWalletInfo;
import com.finacial.wealth.api.sessionmanager.entities.SessionServiceLog;
import com.finacial.wealth.api.sessionmanager.entities.WalletIndivTransactionsDetails;
import com.finacial.wealth.api.sessionmanager.exceptions.CustomApplicationException;
import com.finacial.wealth.api.sessionmanager.repository.AuthenticationLogRepository;
import com.finacial.wealth.api.sessionmanager.repository.RegWalletCheckLogRepo;
import com.finacial.wealth.api.sessionmanager.request.AuthUserRequestCustomerUuid;
import com.finacial.wealth.api.sessionmanager.request.EmailRequestKulean;
import com.finacial.wealth.api.sessionmanager.request.UserDeviceRequest;
import com.finacial.wealth.api.sessionmanager.response.AuthApiResponse;
import com.finacial.wealth.api.sessionmanager.response.BaseResponse;
import com.finacial.wealth.api.sessionmanager.utils.DecodedToken;
import java.security.Key;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import java.util.concurrent.TimeUnit;

import javax.crypto.spec.SecretKeySpec;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.DatatypeConverter;
import javax.mail.internet.InternetAddress;

import org.apache.commons.lang.reflect.FieldUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import lombok.RequiredArgsConstructor;
import org.joda.time.DateTimeComparator;
import com.finacial.wealth.api.sessionmanager.proxy.UtilityProxy;
import com.finacial.wealth.api.sessionmanager.repository.AddAccountDetailsRepo;
import com.finacial.wealth.api.sessionmanager.repository.DeviceDetailsRepo;
import com.finacial.wealth.api.sessionmanager.repository.PeerToPeerFxReferralRepo;
import com.finacial.wealth.api.sessionmanager.repository.RegWalletInfoRepository;
import com.finacial.wealth.api.sessionmanager.repository.WalletIndivTransactionsDetailsRepo;
import com.finacial.wealth.api.sessionmanager.utils.GlobalMethods;

@Service
@RequiredArgsConstructor
public class SessionManagerClientUserService {

    private Logger logger = LoggerFactory.getLogger(SessionManagerClientUserService.class);

    private static final String TOKEN = "idToken";
    private static final String ISSUER = "FELLOWPAY";
    private static final String SUBJECT = "Authentication";
    private static final String LOGIN_SUCCESSFUL = "Login Successful";
    private static final int LOGIN_STATUS_CODE_1 = 200;
    private static final int LOGIN_STATUS_CODE_75 = 75;
    private static final int LOGIN_STATUS_CODE_105 = 105;

    private static final String AUTHENTICATION_SCHEME = "Bearer";

    private static final String HTTP_PROTOCOL = "http://";

    @Value("${fin.wealth.service.utility.name}")
    private String utilityService;

    @Value("${fin.wealth.jwt.secret-key}")
    private String secretKey;

    @Value("${fin.wealth.exempt.uuids}")
    private String uuids;

    @Value("${fin.wealth.jwt.expiration-period}")
    private long tokenExpiration;

    @Value("${fin.wealth.redis.enable.jwt.black-list}")
    private boolean isJwtBlackListitingEnabled;

    @Qualifier("withEureka")
    @Autowired
    private RestTemplate restTemplate;

    @Qualifier("withoutEureka")
    @Autowired
    private RestTemplate restTemplateWithoutEureka;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private final AuthenticationLogRepository authenticationLogRepository;

    private final RegWalletCheckLogRepo regWalletCheckLogRepo;
    private final DeviceDetailsRepo deviceDetailsRepo;
    private final RegWalletInfoRepository regWalletInfoRepository;
    private final PeerToPeerFxReferralRepo peerToPeerFxReferralRepo;
    private final WalletIndivTransactionsDetailsRepo walletIndivTransactionsDetailsRepo;
    private final AddAccountDetailsRepo addAccountDetailsRepo;

    @Autowired
    private final UtilityProxy utilityServiceFeignService;

    static String left3(String s) {
        if (s == null) {
            return null;
        }
        return s.length() <= 3 ? s : s.substring(0, 3);
    }

    private String generateReferal(String firstName) {
        String getForst3Char = left3(firstName);
        String refrerCode = getForst3Char.toUpperCase() + String.valueOf(GlobalMethods.generateOTP());
        return refrerCode;

    }

    public ResponseEntity<BaseResponse> authenticateWalletUserUuid(AuthUserRequestCustomerUuid rq, HttpServletRequest request, String channel) {
        BaseResponse baseResponse = new BaseResponse();
        UserDeviceRequest userDeviceRequest = new UserDeviceRequest();
        userDeviceRequest.setUserId(rq.getEmailAddress());
        userDeviceRequest.setUuid("");

        SessionServiceLog log = new SessionServiceLog();
        String uuid;
        String walletId;
        String loginIP = getClientIpAddr(request);
        log.setCreatedDate(Instant.now());
        log.setLogIP(loginIP);

        try {

            logger.info(String.format("rq.getPushNotificationToken()>>>>>> +++++++++++++ =>%s", rq.getPushNotificationToken()));

            BaseResponse response = utilityServiceFeignService.authenticateWalletUserUuid(rq, channel);

            logger.info(String.format("util auth response >>>>>> +++++++++++++ =>%s", response));

            if (response.getStatusCode() == LOGIN_STATUS_CODE_1) {

                String uniqueIdentificationNo = (String) response.getData().get("uniqueIdentificationNo");
                String mobile = (String) response.getData().get("mobile");
                String email = (String) response.getData().get("email");
                String accountNumber = (String) response.getData().get("accountNumber");

                String firstName = (String) response.getData().get("firstName");
                String lastName = (String) response.getData().get("lastName");
                String phoneNumberVerification = (String) response.getData().get("phoneNumberVerification");
                String emailAddressVerification = (String) response.getData().get("emailAddressVerification");
                String pinCreated = (String) response.getData().get("pinCreated");
                uuid = (String) response.getData().get("uuid");
                walletId = (String) response.getData().get("walletId");
                String referralCode = (String) response.getData().get("referralCode");
                String referralCodeLink = (String) response.getData().get("referralCodeLink");
                String merchantId = (String) response.getData().get("merchantId");
                String merchantLink = (String) response.getData().get("merchantLink");
                String virtualWalletNo = (String) response.getData().get("virtualWalletNo");
                String bvn = (String) response.getData().get("bvn");

                System.out.println("virtualWalletNo from util >>>>>>>>>>>>>>>>>> ::::::::::::::::::::: " + virtualWalletNo);

                userDeviceRequest.setCustomerId(uniqueIdentificationNo);
                userDeviceRequest.setPhoneNumber(mobile);

                AuthApiResponse res = new AuthApiResponse();

                res.setFirstName(firstName);
                res.setLastName(lastName);
                res.setEmailAddress(email);
                res.setUniqueIdentificationNo(uniqueIdentificationNo);
                res.setMobile(mobile);
                res.setEmailAddressVerification(emailAddressVerification);
                res.setPhoneNumberVerification(phoneNumberVerification);
                res.setPhoneNumber(mobile);
                res.setCustomerAccountNo(accountNumber);
                res.setUserDeviceId(rq.getUserDeviceId());
                res.setBrowserType(rq.getBrowserType());
                res.setDeviceType(rq.getDeviceType());
                res.setOsType(rq.getOsType());
                res.setChannel(channel);
                res.setUuid(uuid);
                res.setReferralCode(referralCode);
                res.setReferralCodeLink(referralCodeLink);
                res.setMerchantId(merchantId);
                res.setMerchantLink(merchantLink);
                res.setVirtualWalletNo(virtualWalletNo);
                res.setBvn(bvn);
                res.setWalletId(walletId);
                // BaseResponse baseResponse2 = new BaseResponse();
                baseResponse.setStatusCode(200);
                baseResponse.addData("pinCreated", pinCreated);

                System.out.println(" >>>>>>>>>>>>>>>>>> ::::::::::::::::::::: " + res.getVirtualWalletNo());

                // baseResponse2 = utilityServiceFeignService.checkIfDeviceBelongsToUser(userDeviceRequest,channel);
                if (baseResponse.getStatusCode() == HttpServletResponse.SC_OK) {
                    issueToken(baseResponse, rq.getEmailAddress(), res, loginIP, res.getUuid(), rq.getJoinTransactionId());
                    log.setUserId(rq.getEmailAddress().toLowerCase().trim());
                    log.setUuId(uuid);
                    log.setPhoneNumber(res.getUniqueIdentificationNo());
                    log.setCreatedDate(Instant.now());
                    log.setMethod("Authentication-Wallet-User");
                    log.setCustomerType("Wallet");
                    log.setChannel(channel);

                    List<RegWalletInfo> cumLog = regWalletInfoRepository.findByEmailList(rq.getEmailAddress());

                    List<DeviceDetails> getDe = deviceDetailsRepo.findAllByWalletId(cumLog.get(0).getWalletId());
                    logger.info(String.format("rq.getAppType() +++++++++++++ =>%s", rq.getAppType()));

                    String getType = null;
                    rq.setAppType(rq.getAppType() == null ? "" : rq.getAppType());

                    if (!rq.getAppType().toUpperCase().equals("ANDROID") || !rq.getAppType().toUpperCase().equals("IOS") || rq.getAppType().equals("")) {
                        getType = null;
                    } else {
                        getType = rq.getAppType().toUpperCase();
                    }

                    if (getDe.size() <= 0) {
                        DeviceDetails deLog = new DeviceDetails();
                        deLog.setCreatedDate(Instant.now());
                        deLog.setCreatedBy("System");

                        deLog.setPlatform(DeviceDetails.Platform.from(getType));
                        deLog.setToken(rq.getPushNotificationToken());
                        deLog.setUuid(rq.getUuid());
                        deLog.setWalletId(cumLog.get(0).getWalletId());
                        deviceDetailsRepo.save(deLog);
                    } else {

                        DeviceDetails getDeUp = deviceDetailsRepo.findAllByWalletIdUpdate(cumLog.get(0).getWalletId());
                        getDeUp.setLastModifiedDate(Instant.now());
                        getDeUp.setCreatedBy("System");
                        getDeUp.setPlatform(DeviceDetails.Platform.from(getType));
                        getDeUp.setToken(rq.getPushNotificationToken());
                        getDeUp.setUuid(rq.getUuid());
                        getDeUp.setLastModifiedBy("System");
                        deviceDetailsRepo.save(getDeUp);

                    }

                } else {
                    baseResponse.setStatusCode(response.getStatusCode());
                    baseResponse.setDescription(response.getDescription());
                }
                // }
            } else {
                baseResponse.setStatusCode(response.getStatusCode());
                baseResponse.setDescription(response.getDescription());
            }

        } catch (Exception e) {
            // log.setExceptions(("Exception Occurred " + e.getMessage()).substring(0, 100));
            authenticationLogRepository.save(log);
            e.printStackTrace();
            // logger.error(e.getMessage());

            // propagate exception to return proper message to the user
            throw new CustomApplicationException(HttpStatus.UNAUTHORIZED, "Authentication failed. Please try again");
        }

        log.setApiResponse(baseResponse.getDescription());

        authenticationLogRepository.save(log);
        //logger.info(String.format("session manager response >>>>>> +++++++++++++ =>%s", baseResponse));

        return new ResponseEntity<>(baseResponse, HttpStatus.OK);

    }

    public ResponseEntity<BaseResponse> appMinVersion() {
        BaseResponse baseResponse = new BaseResponse();

        //   baseResponse = utilityServiceFeignService.appMinVersion();
        return new ResponseEntity<>(baseResponse, HttpStatus.OK);
    }

    public ResponseEntity<BaseResponse> destroyJwt(String authorizationHeader) {
        BaseResponse baseResponse = new BaseResponse();
        if (isJwtBlackListitingEnabled) {
            String jwt = authorizationHeader.substring(AUTHENTICATION_SCHEME.length()).trim();
            String token = (String) redisTemplate.opsForValue().get(jwt);
            if (token == null) {
                DecodedToken tokenObject;
                try {
                    tokenObject = DecodedToken.getDecoded(authorizationHeader);
                    redisTemplate.opsForValue().set(jwt, tokenObject.userId);
                    redisTemplate.expire(jwt, (int) TimeUnit.MILLISECONDS.toSeconds(tokenObject.exp), TimeUnit.SECONDS);
                    baseResponse.setDescription("Token Successfully Invalidated");
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
        baseResponse.setStatusCode(HttpServletResponse.SC_OK);
        return new ResponseEntity<>(baseResponse, HttpStatus.OK);
    }

    private String issueToken(String userId, String uuid, AuthApiResponse response, BaseResponse baseResponse) {
        Date expire = generateTokenExpiration();
        String token = createJWT(response.getUniqueIdentificationNo(), response.getEmailAddressVerification(),
                userId.toLowerCase().trim(), ISSUER, SUBJECT, expire, response.getCustomerId(),
                uuid, response.getCustomerAccountNo(), response.getBvn(), response.getFirstName(),
                response.getLastName(), response.getEmailAddress(), response.getMobile(),
                baseResponse.getData().get("pinCreated").toString(), response.getPhoneNumber());
        return token;
    }

    private String createJWT(String uniqueIdentificationNo, String emailAddressVerification,
            String userId, String issuer,
            String subject, Date expire, String customerId, String uuid,
            String accountNo, String bvn,
            String firstName, String lastName, String email,
            String mobile, String pinCreated, String phoneNumber) {

        SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;
        long nowMillis = System.currentTimeMillis();
        Date now = new Date(nowMillis);

        byte[] apiKeySecretBytes = DatatypeConverter.parseBase64Binary(secretKey);
        Key signingKey = new SecretKeySpec(apiKeySecretBytes, signatureAlgorithm.getJcaName());
        Map<String, Object> claims = new HashMap<String, Object>();
        claims.put("userId", userId);
        claims.put("customerId", customerId);
        claims.put("accountNo", accountNo);
        claims.put("uuid", uuid);
        claims.put("bvn", bvn);
        claims.put("firstName", firstName);
        claims.put("lastName", lastName);
        claims.put("emailAddress", email);
        claims.put("mobile", mobile);
        claims.put("phoneNumber", phoneNumber);
        claims.put("pinCreated", pinCreated);
        claims.put("emailAddressVerification", emailAddressVerification);
        claims.put("uniqueIdentificationNo", uniqueIdentificationNo);
        claims.put("exp", expire.getTime());

        JwtBuilder builder = Jwts.builder()
                .setClaims(claims)
                .setId(userId)
                .setIssuedAt(now)
                .setSubject(subject)
                .setIssuer(issuer)
                .signWith(signingKey, signatureAlgorithm);

        builder.setExpiration(expire);

        return builder.compact();
    }

    private Date generateTokenExpiration() {
        LocalDateTime localDateTime = LocalDateTime.now().plusMinutes(tokenExpiration);
        Date date = Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
        return date;
    }

    private void issueToken(BaseResponse baseResponse, String userId, AuthApiResponse response,
            String remoteIp, String uuid, String joinTransactionId) {
        String token = issueToken(userId, uuid, response, baseResponse);
        baseResponse.addData(TOKEN, token);
        baseResponse.addData("firstName", response.getFirstName());
        baseResponse.addData("lasName", response.getLastName());
        baseResponse.addData("customerId", response.getCustomerId());
        baseResponse.addData("phoneNumber", response.getMobile());
        baseResponse.addData("mobile", response.getPhoneNumber());
        baseResponse.addData("emailAddress", response.getEmailAddress());
        baseResponse.addData("customerAccountNo", response.getCustomerAccountNo());
        baseResponse.addData("uniqueIdentificationNo", response.getUniqueIdentificationNo());
        baseResponse.addData("phoneNumberVerification", response.getPhoneNumberVerification());
        baseResponse.addData("emailAddressVerification", response.getEmailAddressVerification());
        baseResponse.addData("bvn", response.getBvn());
        baseResponse.addData("emailAddressVerification", response.getEmailAddressVerification());
        baseResponse.addData("referralCode", response.getReferralCode());
        baseResponse.addData("referralCodeLink", response.getReferralCodeLink());
        baseResponse.addData("merchantId", response.getMerchantId());
        baseResponse.addData("merchantLink", response.getMerchantLink());
        baseResponse.addData("virtualWalletNo", response.getVirtualWalletNo());
        baseResponse.addData("walletId", response.getWalletId());

        List<AddAccountDetails> getacct = addAccountDetailsRepo.findByEmailAddress(response.getEmailAddress());

        if (getacct.size() > 0) {

            String getRereeCode = generateReferal(response.getFirstName());
            String getRefralCode = generateReferal("System");
            String returnedRefreeCode;

            List<WalletIndivTransactionsDetails> getListWaa = walletIndivTransactionsDetailsRepo.findByAccountNumber(getacct.get(0).getEmailAddress());
            if (getListWaa.size() > 0) {
                List<PeerToPeerFxReferral> getRef = peerToPeerFxReferralRepo.findByEmailAddress(getacct.get(0).getEmailAddress());
                if (getRef.size() <= 0) {
                    //generate and save
                    PeerToPeerFxReferral refGen = new PeerToPeerFxReferral();
                    refGen.setCreatedDate(Instant.now());
                    refGen.setEmailAddress(response.getEmailAddress());
                    refGen.setReferee(response.getFirstName() + " " + response.getLastName());
                    refGen.setRefereeCode(getRereeCode);
                    refGen.setReferrer("System");
                    refGen.setReferralCode(getRefralCode);
                    peerToPeerFxReferralRepo.save(refGen);
                    returnedRefreeCode = getRereeCode;

                } else {
                    returnedRefreeCode = getRef.get(0).getRefereeCode();

                }

                baseResponse.addData("pToPFxReferralCode", returnedRefreeCode);
            }

        }

        baseResponse.setStatusCode(HttpServletResponse.SC_OK);
        baseResponse.setDescription(LOGIN_SUCCESSFUL);

        Date currentDate = new Date();
        DateTimeComparator dateTimeComparator = DateTimeComparator.getDateOnlyInstance();
        //here set all cummulative to zeros
        List<RegWalletCheckLog> cumLog = regWalletCheckLogRepo.findByPhoneNumberIdList(response.getPhoneNumber());
        if (cumLog.size() > 0) {
            System.out.println("cumLog.size() > 0" + "   >>>>>>>>>>>>>>>>>> ::::::::::::::::::::: ");
            System.out.println("cumLog.get(0).getLastModifiedDate()" + "   >>>>>>>>>>>>>>>>>> ::::::::::::::::::::: " + cumLog.get(0).getLastModifiedDate());
            System.out.println("dateTimeComparator.compare(currentDate, cumLog.get(0).getLastLoginDay()) > 0" + "   >>>>>>>>>>>>>>>>>> ::::::::::::::::::::: " + dateTimeComparator.compare(currentDate, cumLog.get(0).getLastLoginDay()));

            if (cumLog.get(0).getLastModifiedDate() == null
                    || dateTimeComparator.compare(currentDate, cumLog.get(0).getLastLoginDay()) > 0) {
                RegWalletCheckLog updateCumTbl = regWalletCheckLogRepo.findByPhoneNumberId(response.getPhoneNumber());

                updateCumTbl.setMilePaymentTransferCumm("0");
                updateCumTbl.setOneTimePaymentTransfercUMM("0");
                updateCumTbl.setWalletDepositCumm("0");
                updateCumTbl.setWalletTransferCumm("0");
                updateCumTbl.setWithdrawalcUMM("0");
                updateCumTbl.setLastModifiedDate(Instant.now());
                updateCumTbl.setLastLoginDay(new Date());

                regWalletCheckLogRepo.save(updateCumTbl);
            } else {
                RegWalletCheckLog updateCumTbl = regWalletCheckLogRepo.findByPhoneNumberId(response.getPhoneNumber());
                updateCumTbl.setLastLoginDay(new Date());
                regWalletCheckLogRepo.save(updateCumTbl);

            }
        }
        if (!response.getEmailAddress().isEmpty() && isValidEmailAddress(response.getEmailAddress())) {
            String name = response.getFirstName();
            EmailRequestKulean emailRe = new EmailRequestKulean();
            emailRe.setBody(generateLoginMsg(name, response.getOsType(), response.getDeviceType(), response.getChannel(),
                    response.getBrowserType(), response.getUserDeviceId()));
            emailRe.setSubject("FellowPay Login Notification");
            emailRe.setTo(response.getEmailAddress());
            // BaseResponse sendMail = utilityServiceFeignService.sendUserEmailAndSms(emailRe);
            // System.out.println("sendMail response:::::::: req" + "   >>>>>>>>>>>>>>>>>> ::::::::::::::::::::: " + new Gson().toJson(sendMail));
            System.out.println("sendMail response:::::::: req" + "   >>>>>>>>>>>>>>>>>> ::::::::::::::::::::: ");

        }
    }

    private String generateLoginMsg(String name, String osType, String deviceType, String channel, String browserType,
            String userDeviceId) {
        /* String otpMessage = "Dear " + name + ", you just logged in to Fellow Pay. "
                + "Kinldy call {+234-8038913480} now if you didn't initiate this request.";*/
        Date date = new Date();

        DateFormat dateFormat = new SimpleDateFormat("hh.mm aa");
        String timeWithAMPM = dateFormat.format(new Date());
        System.out.println("Current time in AM/PM: " + timeWithAMPM);

        SimpleDateFormat formatter = new SimpleDateFormat("MMMM dd, yyyy");
        String strDate = formatter.format(date);
        System.out.println("MMMM dd, yyyy: " + strDate);

        SimpleDateFormat sdf2 = new SimpleDateFormat("EEEE");
        String dayOftheWeek = sdf2.format(new Date());
        System.out.println("day Of the Week: " + dayOftheWeek);

        System.out.println("Channel ::::::::" + " ::::::::::::::::::::: " + channel);
        System.out.println("name ::::::::" + " ::::::::::::::::::::: " + name);
        System.out.println("osType ::::::::" + " ::::::::::::::::::::: " + osType);
        System.out.println("deviceType ::::::::" + " ::::::::::::::::::::: " + deviceType);
        String msg;
        switch (channel.trim()) {
            case "Mobile":

                System.out.println("Mobile  channel  ::::::::" + " ");

                msg = "<!DOCTYPE html>\n"
                        + "<html lang=\"en\">\n"
                        + "\n"
                        + "<head>\n"
                        + "    <meta charset=\"UTF-8\">\n"
                        + "    <meta http-equiv=\"X-UA-Compatible\" content=\"IE=edge\">\n"
                        + "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n"
                        + "    <link href=\"https://fonts.cdnfonts.com/css/product-sans\" rel=\"stylesheet\">\n"
                        + "    <title>Document</title>\n"
                        + "\n"
                        + "    <style>\n"
                        + "        .bodyContainer {\n"
                        + "            background: #F5FBFF;\n"
                        + "        }\n"
                        + "\n"
                        + "        .container {\n"
                        + "            color: #011F32;\n"
                        + "            padding: 45px 40px;\n"
                        + "            max-width: 650px;\n"
                        + "            margin: auto\n"
                        + "        }\n"
                        + "\n"
                        + "        h1,\n"
                        + "        h2,\n"
                        + "        h3,\n"
                        + "        p,\n"
                        + "        div {\n"
                        + "            font-family: 'Product Sans', sans-serif;\n"
                        + "        }\n"
                        + "\n"
                        + "        .line {\n"
                        + "            height: 1px;\n"
                        + "            background: #4894FE;\n"
                        + "            border: 0px\n"
                        + "        }\n"
                        + "\n"
                        + "        .footer {\n"
                        + "            color: #011F32\n"
                        + "        }\n"
                        + "    </style>\n"
                        + "\n"
                        + "</head>\n"
                        + "\n"
                        + "<body class=\"bodyContainer\">\n"
                        + "    <div class=\"container\">\n"
                        + "        <img src=\"https://firebasestorage.googleapis.com/v0/b/fellowpay.appspot.com/o/emailtemplate%2FfellowBrand%20(4).png?alt=media&token=9deeaa8f-7491-40a0-a5b9-e5457c3ce4e6\"\n"
                        + "            alt=\"fellowpaylogo\" />\n"
                        + "        <h2>Get started with Fellowpay</h2>\n"
                        + "        <img src=\"https://firebasestorage.googleapis.com/v0/b/fellowpay.appspot.com/o/emailtemplate%2Fimage%2015%20(3).png?alt=media&token=01c8ec96-623f-4d08-8418-5d9e0cad932e\"\n"
                        + "            alt=\"fellowpaylogo\" />\n"
                        + "        <div>\n"
                        + "            <p>Hello " + name + "</p>\n"
                        + "\n"
                        + "            <p>You logged into your FellowPay account from Device: " + deviceType + ","
                        + " " + osType + " at " + timeWithAMPM + " on " + dayOftheWeek + " " + strDate + "</p>\n"
                        + "            <p style=\"padding:15px 0px\">If this login did not originate from you, please let us know by sending an email\n"
                        + "                to support@fellowpay.com.\n"
                        + "                Alternatively, you can call  +234-9162924534 immediately, Thanks.</p>\n"
                        + "\n"
                        + "\n"
                        + "\n"
                        + "\n"
                        + "\n"
                        + "            <hr class=\"line\" />\n"
                        + "            <div class=\"footer\">\n"
                        + "                <p>If you run into issues while using fellowpay, can get our real-time chat support via WhatsApp or even\n"
                        + "                    write to us at <a target=\"_blank\" href=\"mailto:fellowpay.support@thefellowpay.com\">fellowpay.support@thefellowpay.com</a>\n"
                        + "                </p>\n"
                        + "\n"
                        + "                <p style=\"color:#69add8\">Fellowpay</p>\n"
                        + "            </div>\n"
                        + "\n"
                        + "        </div>\n"
                        + "    </div>\n"
                        + "</body>\n"
                        + "\n"
                        + "</html>";

                break;
            case "Web":

                System.out.println("Web  channel  ::::::::" + " ");

                msg = "<!DOCTYPE html>\n"
                        + "<html lang=\"en\">\n"
                        + "\n"
                        + "<head>\n"
                        + "    <meta charset=\"UTF-8\">\n"
                        + "    <meta http-equiv=\"X-UA-Compatible\" content=\"IE=edge\">\n"
                        + "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n"
                        + "    <link href=\"https://fonts.cdnfonts.com/css/product-sans\" rel=\"stylesheet\">\n"
                        + "    <title>Document</title>\n"
                        + "\n"
                        + "    <style>\n"
                        + "        .bodyContainer {\n"
                        + "            background: #F5FBFF;\n"
                        + "        }\n"
                        + "\n"
                        + "        .container {\n"
                        + "            color: #011F32;\n"
                        + "            padding: 45px 40px;\n"
                        + "            max-width: 650px;\n"
                        + "            margin: auto\n"
                        + "        }\n"
                        + "\n"
                        + "        h1,\n"
                        + "        h2,\n"
                        + "        h3,\n"
                        + "        p,\n"
                        + "        div {\n"
                        + "            font-family: 'Product Sans', sans-serif;\n"
                        + "        }\n"
                        + "\n"
                        + "        .line {\n"
                        + "            height: 1px;\n"
                        + "            background: #4894FE;\n"
                        + "            border: 0px\n"
                        + "        }\n"
                        + "\n"
                        + "        .footer {\n"
                        + "            color: #011F32\n"
                        + "        }\n"
                        + "    </style>\n"
                        + "\n"
                        + "</head>\n"
                        + "\n"
                        + "<body class=\"bodyContainer\">\n"
                        + "    <div class=\"container\">\n"
                        + "        <img src=\"https://firebasestorage.googleapis.com/v0/b/fellowpay.appspot.com/o/emailtemplate%2FfellowBrand%20(4).png?alt=media&token=9deeaa8f-7491-40a0-a5b9-e5457c3ce4e6\"\n"
                        + "            alt=\"fellowpaylogo\" />\n"
                        + "        <h2>Get started with Fellowpay</h2>\n"
                        + "        <img src=\"https://firebasestorage.googleapis.com/v0/b/fellowpay.appspot.com/o/emailtemplate%2Fimage%2015%20(3).png?alt=media&token=01c8ec96-623f-4d08-8418-5d9e0cad932e\"\n"
                        + "            alt=\"fellowpaylogo\" />\n"
                        + "        <div>\n"
                        + "            <p>Hello " + name + "</p>\n"
                        + "\n"
                        + "            <p>You logged into your FellowPay account from a Browser: " + browserType + ", "
                        + " " + osType + " at " + timeWithAMPM + " on " + dayOftheWeek + " " + strDate + "</p>\n"
                        + "            <p style=\"padding:15px 0px\">If this login did not originate from you, please let us know by sending an email\n"
                        + "                to support@fellowpay.com.\n"
                        + "                Alternatively, you can call +234-9162924534 immediately, Thanks.</p>\n"
                        + "\n"
                        + "\n"
                        + "\n"
                        + "\n"
                        + "\n"
                        + "            <hr class=\"line\" />\n"
                        + "            <div class=\"footer\">\n"
                        + "                <p>If you run into issues while using fellowpay, can get our real-time chat support via WhatsApp or even\n"
                        + "                    write to us at <a target=\"_blank\" href=\"mailto:fellowpay.support@thefellowpay.com\">fellowpay.support@thefellowpay.com</a>\n"
                        + "                </p>\n"
                        + "\n"
                        + "                <p style=\"color:#69add8\">Fellowpay</p>\n"
                        + "            </div>\n"
                        + "\n"
                        + "        </div>\n"
                        + "    </div>\n"
                        + "</body>\n"
                        + "\n"
                        + "</html>";
                break;
            default:
                System.out.println("Api  channel  ::::::::" + " ");
                msg = " Hello " + name + ",\n"
                        + "You logged into your FellowPay account from an Api: " + "" + ","
                        + "" + "" + " at " + timeWithAMPM + " on " + dayOftheWeek + " " + strDate + ". \n"
                        + "If this login did not originate from you, please let us know by sending an email "
                        + "to support@fellowpay.com. \n"
                        + "Alternatively, you can call +234-8038913480 immediately, Thanks.\n"
                        + "\n"
                        + "PS. If you did not initiate this request, kindly reply to this email \n"
                        + "or write to support@fellowpay.com or call us on +234-9162924534. \n"
                        + "There could have been an attempt to breach your account.";
            // return responseModel;
        }

        return msg;

    }

    public static boolean isValidEmailAddress(String email) {
        boolean result = true;
        try {
            InternetAddress emailAddr = new InternetAddress(email);
            emailAddr.validate();
        } catch (Exception ex) {
            result = false;
        }
        return result;
    }

    private String getClientIpAddr(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }

    private boolean checkIfDeviceIsExempted(String uuid) {
        /**
         * This was added to adapt customer experience center to exempt check
         * device
         */
        List<String> exemptUUIDs = Arrays.asList(uuids.split("\\s*,\\s*"));

        return exemptUUIDs.contains(uuid);

    }

}
