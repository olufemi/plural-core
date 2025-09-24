package com.finacial.wealth.api.utility.services;

import com.finacial.wealth.api.utility.domains.Otp;
import com.finacial.wealth.api.utility.domains.RegWalletInfo;
import com.finacial.wealth.api.utility.models.EmailRequest;
import com.finacial.wealth.api.utility.models.OtpRequest;
import com.finacial.wealth.api.utility.models.OtpValidateRequest;
import com.finacial.wealth.api.utility.models.ReqRequestId;
import com.finacial.wealth.api.utility.repository.OtpRepository;
import com.finacial.wealth.api.utility.repository.RegWalletInfoRepository;
import com.finacial.wealth.api.utility.response.BaseResponse;
import com.finacial.wealth.api.utility.utils.StrongAES;
import com.finacial.wealth.api.utility.utils.UttilityMethods;
import com.google.gson.Gson;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Optional;

import java.util.UUID;
import javax.servlet.http.HttpServletResponse;
import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;

import java.security.SecureRandom;
import java.util.List;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class OtpService {

    private static final Logger LOG = LoggerFactory.getLogger(OtpService.class);
    private static final String createdBy = "SYSTEM";
    private static final String OTP_SUCCESSFULLY_SENT = "Otp Sent SuccessFully";
    private static final String OTP_SUCCESSFULLY_CREATED = "Otp Created SuccessFully";
    private static final String OTP_VALID = "Otp Valid";
    private static final String OTP_INVALID = "Otp Invalid";
    private static final String OTP_INCOREECT = "Otp Entered Is Invalid";
    private static final String OTP_INCOREECT_RETRY_EXCEEDED = "Otp Retry Exceeded";
    private static final String INVALID_REQUEST_ID = "Invalid request id";
    private static final String ERROR_OCCURED = "An error occured. Pls try agian later";
    private static final String OTP_STATUS_CODE = "otpStatusCode";
    private static final String REQUEST_ID_INVALID = "This regisration session has expired. Pls start again or call our contact centre on {Contact-Centre-No} to assist you further.";

    /* @Autowired
    AsyncService asyncService;*/
    @Value("${fin.wealth.otp.expiry-period-in-minutes}")
    private String otpExpiry;

    @Value("${fin.wealth.otp.encrypt.key}")
    private String encryptionKey;

    @Value("${fin.wealth.otp.retry}")
    private String otpRetry;

    @Value("${spring.profiles.active}")
    private String environment;

    private final OtpRepository otpRepository;

    @Autowired
    UttilityMethods uttilityMethods;

    private final RegWalletInfoRepository regWalletInfoRepo;

    public BaseResponse createAndSendOtp(OtpRequest request) {
        BaseResponse baseResponse = new BaseResponse();
        try {

            // create Otp
            int otpValue;
            Otp otp;
            boolean isOtpCreatedFromExternalService = false; //
            if (request.getOtp() == null) {
                //  otpValue = !environment.equals("local") || !environment.equals("dev") ? generateOtp() : 123456;
                otpValue = 123456;
                if (!environment.equals("local")) {
                    if (!environment.equals("dev")) {

                        otpValue = generateOtp();
                    }
                }

            } else {
                isOtpCreatedFromExternalService = true;
                //otpValue = !environment.equals("local") || !environment.equals("dev") ? Integer.valueOf(request.getOtp()) : 123456;
                otpValue = 123456;
                if (!environment.equals("local")) {
                    if (!environment.equals("dev")) {

                        otpValue = generateOtp();
                    }
                }
            }
            //   System.out.println("CreateAndSendOtpOtp Sent To User ----- " + otpValue);
            //String encyrptedOtp = StrongAES.encyrpt(String.valueOf(otpValue), encryptionKey);
            //  System.out.println("createAndSendOtp otp encripted ----- " + encyrptedOtp);
            // request.setOtp(Integer.toString(otpValue));
            request.setOtp(String.valueOf(otpValue));

            //     System.out.println("createAndSendOtp request.getOtp() TO SAVE ----- " + request.getOtp());
            if (request.getResend() != null && request.getResend().equals("1")) {
                otp = updateOtp(request);
            } else {
                otp = create(request);
            }

            List<RegWalletInfo> getRecordDevice = regWalletInfoRepo.findByEmail(request.getEmailAddress());
            String name = null;
            if (getRecordDevice.size() > 0) {

                name = getRecordDevice.get(0).getFirstName();
            }

            //    System.out.println("createAndSendOtp otp :::::::: " + "::::: " + new Gson().toJson(otp));
            // Send Otp
            // Send Otp
            String emailAddress = request.getEmailAddress();
            System.out.println("email otp req emailAddress:::::::: " + "::::: " + emailAddress);
            // System.out.println("createAndSendOtp otpValue:::::::: " + "::::: " + request.getOtp());
            //  String mailBody = String.format(generateOtpMsg(otpValue, name));
            String mailBody = generateOtpMsg(otpValue, name);

            //System.out.println("generateOtpMsg  ::: mailBody :::::::: " + "::::: " + emailAddress);
            String receiverEmail = emailAddress;
            EmailRequest req = new EmailRequest();
            req.setBody(mailBody);
            req.setSubject("Kulean Pay Wallet");
            req.setTo(receiverEmail);
            //  System.out.println("email otp req :::::::: " + "::::: " + new Gson().toJson(req));
            if (!isOtpCreatedFromExternalService) {
                if (!environment.equals("local")) {
                    if (!environment.equals("dev")) {
                        System.out.println(":::::::: createAndSendOtp" + "::::: " + request.getAppDeviceSig());
                        String newSig = request.getAppDeviceSig() == null ? "" : request.getAppDeviceSig();

                        // smsService.sendSMS(request.getPhoneNumber(), String.valueOf(otpValue), newSig);
                        // System.out.println(" smsService.sendSMS  :::  :::::::: " + "::::: " + emailAddress);
                        //  sendGridMailSender.sendEmail(req);
                    }

                }
            }

            baseResponse.addData("requestId", otp.getRequestId());
            baseResponse.addData("phoneNumber", request.getPhoneNumber());
            baseResponse.setDescription(OTP_SUCCESSFULLY_SENT);
            baseResponse.setStatusCode(HttpServletResponse.SC_OK);
            return baseResponse;
        } catch (RestClientException exception) {
            baseResponse.setDescription(ERROR_OCCURED);
            baseResponse.setStatusCode(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return baseResponse;
        } catch (Exception exception) {
            System.out.println(exception.getMessage());
            baseResponse.setDescription(exception.getMessage());
            baseResponse.setStatusCode(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return baseResponse;
        }
    }

    public BaseResponse createAndSendOtpSMSOnly(OtpRequest request) {
        BaseResponse baseResponse = new BaseResponse();
        try {

            // create Otp
            int otpValue;
            Otp otp;
            boolean isOtpCreatedFromExternalService = false; //
            if (request.getOtp() == null) {
                // otpValue = !environment.equals("local") || !environment.equals("dev") ? generateOtp() : 123456;

                otpValue = 123456;
                if (!environment.equals("local")) {
                    if (!environment.equals("dev")) {

                        otpValue = generateOtp();
                    }
                }
            } else {
                isOtpCreatedFromExternalService = true;
                // otpValue = !environment.equals("local") || !environment.equals("dev") ? Integer.valueOf(request.getOtp()) : 123456;

                otpValue = 123456;
                if (!environment.equals("local")) {
                    if (!environment.equals("dev")) {

                        otpValue = generateOtp();
                    }
                }
            }
            //   System.out.println("CreateAndSendOtpOtp Sent To User ----- " + otpValue);
            //String encyrptedOtp = StrongAES.encyrpt(String.valueOf(otpValue), encryptionKey);
            //  System.out.println("createAndSendOtp otp encripted ----- " + encyrptedOtp);
            // request.setOtp(Integer.toString(otpValue));
            request.setOtp(String.valueOf(otpValue));

            //     System.out.println("createAndSendOtp request.getOtp() TO SAVE ----- " + request.getOtp());
            if (request.getResend() != null && request.getResend().equals("1")) {
                otp = updateOtp(request);
            } else {
                otp = create(request);
            }

            //    System.out.println("createAndSendOtp otp :::::::: " + "::::: " + new Gson().toJson(otp));
            // Send Otp
            // Send Otp
            //  System.out.println("email otp req :::::::: " + "::::: " + new Gson().toJson(req));
            if (!isOtpCreatedFromExternalService) {
                if (!environment.equals("local")) {
                    if (!environment.equals("dev")) {
                        System.out.println(":::::::: createAndSendOtpSMSOnly" + "::::: " + request.getAppDeviceSig());

                        String newSig = request.getAppDeviceSig() == null ? "" : request.getAppDeviceSig();

                        //smsService.sendSMS(request.getPhoneNumber(), String.valueOf(otpValue), newSig);
                    }
                }

            }

            baseResponse.addData("requestId", otp.getRequestId());
            baseResponse.addData("phoneNumber", request.getPhoneNumber());
            baseResponse.setDescription(OTP_SUCCESSFULLY_SENT);
            baseResponse.setStatusCode(HttpServletResponse.SC_OK);
            return baseResponse;
        } catch (RestClientException exception) {
            baseResponse.setDescription(ERROR_OCCURED);
            baseResponse.setStatusCode(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return baseResponse;
        } catch (Exception exception) {
            System.out.println(exception.getMessage());
            baseResponse.setDescription(exception.getMessage());
            baseResponse.setStatusCode(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return baseResponse;
        }
    }

    public BaseResponse createAndSendOtpEmail(OtpRequest request) {
        BaseResponse baseResponse = new BaseResponse();
        try {

            // create Otp
            int otpValue;
            Otp otp;
            boolean isOtpCreatedFromExternalService = false; //
            if (request.getOtp() == null) {
                //otpValue = !environment.equals("local") || !environment.equals("dev") ? generateOtp() : 123456;
                otpValue = 123456;
                if (!environment.equals("local")) {
                    if (!environment.equals("dev")) {

                        otpValue = generateOtp();
                    }
                }
            } else {
                isOtpCreatedFromExternalService = true;
                // otpValue = !environment.equals("local") || !environment.equals("dev") ? Integer.valueOf(request.getOtp()) : 123456;
                otpValue = 123456;
                if (!environment.equals("local")) {
                    if (!environment.equals("dev")) {

                        otpValue = generateOtp();
                    }
                }
            }

            List<RegWalletInfo> getRecordDevice = regWalletInfoRepo.findByEmail(request.getEmailAddress());
            String name = null;
            if (getRecordDevice.size() > 0) {

                name = getRecordDevice.get(0).getFirstName();
            }
            //   System.out.println("CreateAndSendOtpOtp Sent To User ----- " + otpValue);
            //String encyrptedOtp = StrongAES.encyrpt(String.valueOf(otpValue), encryptionKey);
            //  System.out.println("createAndSendOtp otp encripted ----- " + encyrptedOtp);
            // request.setOtp(Integer.toString(otpValue));
            request.setOtp(String.valueOf(otpValue));

            //     System.out.println("createAndSendOtp request.getOtp() TO SAVE ----- " + request.getOtp());
            if (request.getResend() != null && request.getResend().equals("1")) {
                otp = updateOtp(request);
            } else {
                otp = create(request);
            }

            String emailAddress = request.getEmailAddress();
            String mailBody = generateOtpMsg(otpValue, name);
            String receiverEmail = emailAddress;
            EmailRequest req = new EmailRequest();
            req.setBody(mailBody);
            req.setSubject("Kulean Pay Wallet");
            req.setTo(receiverEmail);

            if (!isOtpCreatedFromExternalService) {
                if (!environment.equals("local")) {
                    if (!environment.equals("dev")) {
                        //  sendGridMailSender.sendEmail(req);

                    }
                }
            }

            baseResponse.addData("requestId", otp.getRequestId());
            baseResponse.addData("emailAddress", request.getEmailAddress());
            baseResponse.setDescription(OTP_SUCCESSFULLY_SENT);
            baseResponse.setStatusCode(HttpServletResponse.SC_OK);
            return baseResponse;
        } catch (RestClientException exception) {
            baseResponse.setDescription(ERROR_OCCURED);
            baseResponse.setStatusCode(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return baseResponse;
        } catch (Exception exception) {
            System.out.println(exception.getMessage());
            baseResponse.setDescription(exception.getMessage());
            baseResponse.setStatusCode(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return baseResponse;
        }
    }

    public BaseResponse validateOtp(OtpValidateRequest request) {
        BaseResponse baseResponse = new BaseResponse();
         System.out.println("validateOtp :::::  " + new Gson().toJson(request));
        try {
            // System.out.println("validateOtp request.getOtp() ----- " + request.getOtp());
            //  System.out.println("validate otp from user, KEY :::::::: " + "::::: " + encryptionKey);
            String encyrptedOtp = StrongAES.encyrpt(String.valueOf(request.getOtp()), encryptionKey);
            //   System.out.println("validateOtp otp request encripted ----- " + encyrptedOtp);
            List<Otp> otp1;
            otp1 = otpRepository.findByReqId(request.getRequestId());
            if (otp1.size() <= 0) {

                baseResponse.setStatusCode(HttpServletResponse.SC_BAD_REQUEST);
                baseResponse.setDescription(INVALID_REQUEST_ID);
                return baseResponse;
            }

            Optional<Otp> otp;
            otp = otpRepository.findByRequestId(request.getRequestId());
            // System.out.println("validateOtp encripted from db :::::: otp.get().getOtp() ----- " + otp.get().getOtp());
            // System.out.println("validateOtp deencripted from db :::::: otp.get().getOtp() ----- " + StrongAES.decrypt(String.valueOf(otp.get().getOtp()), encryptionKey));
            // System.out.println("otpReqId" + "  ::::::::::::::::::::: >>>>>>>>>>>>>>>>>>  " + request.getRequestId());

            long nowMillis = System.currentTimeMillis();
            if (otp.isPresent() && otp.get().getOtp().equals(encyrptedOtp) && !otp.get().isUsed()
                    && otp.get().getExpiry() >= nowMillis) {
                otp.get().setUsed(true);
                otp.get().setLastModifiedDate(Instant.now());
                otp.get().setAttempts(otp.get().getAttempts() + 1);
                otp.get().setLastModifiedBy(otp.get().getUserId());
                otpRepository.save(otp.get());
                baseResponse.setDescription(OTP_VALID);
                baseResponse.addData("requestId", otp.get().getRequestId());
                baseResponse.addData("userId", otp.get().getUserId());
                baseResponse.addData("newUserId", otp.get().getNewUserId());
                baseResponse.setStatusCode(HttpServletResponse.SC_OK);
                return baseResponse;
            } else {
                if (otp.isPresent() && !otp.get().isUsed() && otp.get().getExpiry() >= nowMillis
                        && Integer.valueOf(otpRetry) > otp.get().getAttempts()) {
                    long increaseAttempt = otp.get().getAttempts() + 1;
                    otp.get().setLastModifiedDate(Instant.now());
                    otp.get().setAttempts(increaseAttempt);
                    otp.get().setLastModifiedBy(otp.get().getUserId());
                    otpRepository.save(otp.get());
                    baseResponse.addData("attempts", increaseAttempt);
                    baseResponse.setDescription(OTP_INCOREECT);
                } else {
                    if (otp.isPresent() && !otp.get().isUsed() && otp.get().getExpiry() >= nowMillis
                            && otp.get().getAttempts() == Long.valueOf(otpRetry)) {
                        baseResponse.setDescription(OTP_INCOREECT_RETRY_EXCEEDED);
                        baseResponse.addData(OTP_STATUS_CODE, "99");
                    } else {
                        baseResponse.setDescription(OTP_INVALID);
                        baseResponse.addData(OTP_STATUS_CODE, "99");
                    }
                }
                baseResponse.setStatusCode(HttpServletResponse.SC_BAD_REQUEST);
                return baseResponse;
            }
        } catch (Exception e) {
            e.printStackTrace();
            baseResponse.setDescription(HttpStatus.INTERNAL_SERVER_ERROR.toString());
            baseResponse.setStatusCode(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return baseResponse;
        }
    }

    public BaseResponse createOtp(OtpRequest request) {
        BaseResponse baseResponse = new BaseResponse();
        Otp otp = create(request);
        baseResponse.addData("requestId", otp.getRequestId());
        baseResponse.setDescription(OTP_SUCCESSFULLY_CREATED);
        baseResponse.setStatusCode(HttpServletResponse.SC_OK);
        return baseResponse;
    }

    private Otp create(OtpRequest request) {
        Otp otp = new Otp();
        LocalDateTime expireMinutes = LocalDateTime.now().plusMinutes(Long.valueOf(otpExpiry));
        long expiry = Timestamp.valueOf(expireMinutes).getTime();
        try {
            //String otpPass = !environment.equals("local") ? request.getOtp() : "123456";
            //   System.out.println("save oto to db, raw OTP :::::::: " + "::::: " + otpPass);

            //  System.out.println("save oto to db, KEY :::::::: " + "::::: " + encryptionKey);
            // String encyrptedOtp = StrongAES.encyrpt(String.valueOf(otpPass), encryptionKey);
            //   System.out.println("save oto to db, otp :::::::: " + "::::: " + encyrptedOtp);
            String encyrptedOtp = StrongAES.encyrpt(request.getOtp(), encryptionKey);

            String requestId = generateRequestId(request.getServiceName());
            otp.setExpiry(expiry);
            otp.setCreatedBy(createdBy);
            otp.setOtp(encyrptedOtp);
            otp.setCreatedDate(Instant.now());
            otp.setPhoneNumber(request.getPhoneNumber());
            otp.setUserId(request.getUserId());
            otp.setRequestId(requestId);
            otp.setServiceName(request.getServiceName());
            if (request.getNewUserId() != null) {
                otp.setNewUserId(request.getNewUserId());
            }
            otpRepository.save(otp);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return otp;
    }

    private Otp updateOtp(OtpRequest request) throws Exception {
        Optional<Otp> otp = otpRepository.findByRequestId(request.getRequestId());

        System.out.println(request.getRequestId());

        if (otp.isPresent() && !otp.get().isUsed()) {
            LocalDateTime expireMinutes = LocalDateTime.now().plusMinutes(Long.valueOf(otpExpiry));
            long expiry = Timestamp.valueOf(expireMinutes).getTime();
            try {
                //String otpPass = !environment.equals("local") ? request.getOtp() : "123456";
                //String encyrptedOtp = StrongAES.encyrpt(String.valueOf(otpPass), encryptionKey);
                String encyrptedOtp = StrongAES.encyrpt(request.getOtp(), encryptionKey);
                otp.get().setOtp(encyrptedOtp);
                otp.get().setExpiry(expiry);
                otp.get().setLastModifiedDate(Instant.now());

                otpRepository.save(otp.get());
            } catch (Exception e) {
                e.printStackTrace();
            }
            return otp.get();
        } else {
            throw new Exception(REQUEST_ID_INVALID);
        }
    }

    public BaseResponse getOtpByRequestId(ReqRequestId rq) {
        BaseResponse baseResponse = new BaseResponse();
        Optional<Otp> otp = otpRepository.findByRequestId(rq.getRequestId());
        long nowMillis = System.currentTimeMillis();
        if (otp.isPresent() && !otp.get().isUsed() && otp.get().getExpiry() >= nowMillis) {
            baseResponse.addData("userId", otp.get().getUserId());
            baseResponse.setStatusCode(HttpServletResponse.SC_OK);
        } else {
            baseResponse.setStatusCode(HttpServletResponse.SC_BAD_REQUEST);
            baseResponse.setDescription(REQUEST_ID_INVALID);
        }
        return baseResponse;
    }

    public BaseResponse getOtpByRequestIdExist(ReqRequestId rq) {
        BaseResponse baseResponse = new BaseResponse();
        List<Otp> otp = otpRepository.findByReqId(rq.getRequestId());
        System.out.println(":::::::: util  getOtpByRequestIdExist" + "::::: " + rq.getRequestId());

        if (otp.size() <= 0) {
            baseResponse.setStatusCode(HttpServletResponse.SC_BAD_REQUEST);
            baseResponse.setDescription(REQUEST_ID_INVALID);
            return baseResponse;
        }
        // long nowMillis = System.currentTimeMillis();
        if (!otp.get(0).isUsed()) {
            if (otp.get(0).getUserId().equals("") || otp.get(0).getUserId() == null) {
                baseResponse.addData("userId", otp.get(0).getPhoneNumber());
            } else {
                baseResponse.addData("userId", otp.get(0).getUserId());
            }
            baseResponse.setStatusCode(HttpServletResponse.SC_OK);
        } else {
            baseResponse.setStatusCode(HttpServletResponse.SC_BAD_REQUEST);
            baseResponse.setDescription(REQUEST_ID_INVALID);
        }
        return baseResponse;
    }

    private int generateOtp() {
        SecureRandom r = new SecureRandom();
        Integer randomString = Math.abs(new SecureRandom().nextInt(99999));
        int otp = 100000 + r.nextInt(randomString);
        return otp;
        /*Random r = new Random(System.currentTimeMillis());
		int otp = 100000 + r.nextInt(900000);
		return otp;*/
    }

    private String generateOtpMsg(int otp, String name
    ) {

        if (name == null) {
            name = "Customer";
        }

        String msg = "<!DOCTYPE html>\n"
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
                + "        .container{\n"
                + "            color: #011F32;\n"
                + "            padding: 45px 40px;\n"
                + "            max-width: 650px;\n"
                + "            margin: auto\n"
                + "        }\n"
                + "    \n"
                + "        h1,\n"
                + "        h2,\n"
                + "        h3,\n"
                + "        p,\n"
                + "        div {\n"
                + "            font-family: 'Product Sans', sans-serif;\n"
                + "        }\n"
                + "    \n"
                + "        .line {\n"
                + "            height: 1px;\n"
                + "            background: #A5DBFD;\n"
                + "            border: 0px\n"
                + "        }\n"
                + "    \n"
                + "        .footer {\n"
                + "            color:#011F32\n"
                + "        }\n"
                + "    </style>\n"
                + "\n"
                + "\n"
                + "</head>\n"
                + "\n"
                + "<body class=\"bodyContainer\">\n"
                + "    <div class=\"container\">\n"
                + "    <img src=\"https://firebasestorage.googleapis.com/v0/b/kuleanpay.appspot.com/o/emailtemplate%2FkuleanBrand%20(4).png?alt=media&token=9deeaa8f-7491-40a0-a5b9-e5457c3ce4e6\" alt=\"kuleanpaylogo\" />\n"
                + "    <h2>Get started with Kuleanpay</h2>\n"
                + "    <img src=\"https://firebasestorage.googleapis.com/v0/b/kuleanpay.appspot.com/o/emailtemplate%2Fimage%2015%20(2).png?alt=media&token=fa10ff3d-f92d-49ef-b4cf-92ac6b6419c9\" alt=\"shield\" />\n"
                + "    <div>\n"
                + "        <p>Hello " + name + "</p>\n"
                + "\n"
                + "        <p>DO NOT DISCLOSE</p>\n"
                + "        <p style=\"padding:15px 0px\">Use this OTP to complete your transaction\n"
                + "        </p>\n"
                + "       \n"
                + "        <h2>\n"
                + "            " + otp + "\n"
                + "        </h2>\n"
                + "\n"
                + "\n"
                + "\n"
                + "\n"
                + "        <hr class=\"line\" />\n"
                + "        <div class=\"footer\">\n"
                + "            <p>If you run into issues while using kuleanpay, can get our real-time chat support via WhatsApp or even\n"
                + "                write to us at <a target=\"_blank\" href=\"mailto:kuleanpay.support@theefifthlab.com\">kuleanpay.support@theefifthlab.com</a>\n"
                + "            </p>\n"
                + "\n"
                + "            <p style=\"color:#69add8\">Kuleanpay</p>\n"
                + "        </div>\n"
                + "\n"
                + "    </div>\n"
                + "</div>\n"
                + "</body>\n"
                + "\n"
                + "</html>";

        String otpMessage = "Do not share with anyone including Kulean Pay Support staff. " + "Your KuleanPay code: "
                + String.valueOf(otp) + "." + " Call {+234-8038913480} now if you didn't initiate this request.";
        return msg;
    }

    public static String generateRequestId(String serviceName) {
        String servicePrevix = serviceName.substring(0, 2).toUpperCase();
        return servicePrevix + UUID.randomUUID().toString().replaceAll("-", "").substring(0, 10).toUpperCase();
    }
}
