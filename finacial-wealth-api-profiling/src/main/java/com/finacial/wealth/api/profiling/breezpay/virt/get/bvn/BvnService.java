/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.finacial.wealth.api.profiling.breezpay.virt.get.bvn;

/**
 *
 * @author olufemioshin
 */
import com.finacial.wealth.api.profiling.breezpay.virt.get.bvn.GetSingleBvnResponse.BvnData;
import com.finacial.wealth.api.profiling.domain.AddFailedTransLog;
import com.finacial.wealth.api.profiling.domain.RegWalletInfo;
import com.finacial.wealth.api.profiling.proxies.BreezePayVirtAcctProxy;
import com.finacial.wealth.api.profiling.proxies.BreezePayVirtApiDevAcctProxy;
import com.finacial.wealth.api.profiling.proxies.UtilitiesProxy;
import com.finacial.wealth.api.profiling.repo.AddFailedTransLoggRepo;
import com.finacial.wealth.api.profiling.repo.RegWalletInfoRepository;
import com.finacial.wealth.api.profiling.response.BaseResponse;
import com.finacial.wealth.api.profiling.services.UniqueIdService;
import com.finacial.wealth.api.profiling.utils.DecodedJWTToken;
import com.finacial.wealth.api.profiling.utils.GlobalMethods;
import com.finacial.wealth.api.profiling.utils.UttilityMethods;
import java.io.UnsupportedEncodingException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;

@Service
@RequiredArgsConstructor
public class BvnService {

    @Value("${fin.wealth.breeze.pay.mer.auth}")
    private String auth;

    @Value("${fin.wealth.breeze.pay.mer.sub.key}")
    private String subKey;

    @Value("${fin.wealth.breeze.pay.appId}")
    private String appId;

    private final BreezePayVirtAcctProxy proxy;
    private final BvnLookupRepository repo;
    private final AddFailedTransLoggRepo addFailedTransLoggRepo;
    private final UttilityMethods uttilityMethods;
    private final BreezePayVirtApiDevAcctProxy breezePayVirtApiDevAcctProxy;
    @Value("${spring.profiles.active}")
    private String environment;
    private final RegWalletInfoRepository regWalletInfoRepo;
    private final UniqueIdService uniqueIds;

    // Optional: configurable freshness window (e.g., 30 days)
    private final Period freshness = Period.ofDays(30);
    private static final DateTimeFormatter DMY_MON = DateTimeFormatter.ofPattern("dd-MMM-yyyy", Locale.ENGLISH);

    @Transactional(readOnly = true)
    public BvnLookup getFromCache(String bvn) {
        return repo.findByBvn(bvn).orElse(null);
    }
    
    public static void assertValidBvn(String bvn) {
        if (bvn == null || bvn.trim().isEmpty()) {
            throw new IllegalArgumentException("BVN is required");
        }
        String v = bvn.trim();
        if (v.length() != 11) {
            throw new IllegalArgumentException("BVN must be exactly 11 digits");
        }
        for (int i = 0; i < v.length(); i++) {
            if (!Character.isDigit(v.charAt(i))) {
                throw new IllegalArgumentException("BVN must contain only digits");
            }
        }
    }

    public BaseResponse validateBvnCaller(String bvn, String auth) {
        BaseResponse responseModel = new BaseResponse();
        int statusCode = 500;
        String statusMessage = "An error occured,please try again";
        try {
            assertValidBvn(bvn); 
            statusCode = 400;
            BvnLookup getBlook = this.getOrFetchAndPersist(bvn, auth);

            if (!"00".equals(getBlook.getResponseCode())) {

                responseModel.setDescription("Unable to process request");
                responseModel.setStatusCode(statusCode);

                AddFailedTransLog pinActTransFailed = new AddFailedTransLog("activate-wallet",
                        "Unable to process request", "", "", "");
                addFailedTransLoggRepo.save(pinActTransFailed);
                return responseModel;

            }

            BaseResponse getOtpRes = uttilityMethods.initiateSendOtp(auth, getBlook.getPhoneNumber1(), getBlook.getFirstName() + " " + getBlook.getLastName(), "Request-To-Validate-Bvn-Request-On-Add-Account");
            if (getOtpRes.getStatusCode() != 200) {

                responseModel.setDescription(getOtpRes.getDescription());
                responseModel.setStatusCode(statusCode);

                AddFailedTransLog pinActTransFailed = new AddFailedTransLog("activate-wallet",
                        getOtpRes.getDescription(), "", "", "");
                addFailedTransLoggRepo.save(pinActTransFailed);
                return responseModel;

            }

            String otpReqId = (String) getOtpRes.getData()
                    .get("requestId");

            String otpProcessId = (String) getOtpRes.getData()
                    .get("processId");

            Map addExit = new HashMap();
            addExit.put("requestId", otpReqId);
            addExit.put("processId", otpProcessId);
            responseModel.setDescription(getOtpRes.getDescription());
            responseModel.setStatusCode(getOtpRes.getStatusCode());
            responseModel.setData(addExit);

            //send otp impl
        } catch (Exception ex) {
            responseModel.setDescription(statusMessage);
            responseModel.setStatusCode(statusCode);

            AddFailedTransLog pinActTransFailed = new AddFailedTransLog("activate-wallet",
                    statusMessage, "", "", "");
            addFailedTransLoggRepo.save(pinActTransFailed);
            ex.printStackTrace();
        }

        return responseModel;
    }

    @Transactional
    public BvnLookup getOrFetchAndPersist(String bvn, String auth
    ) throws UnsupportedEncodingException {
        BvnLookup cached = repo.findByBvn(bvn).orElse(null);
        if (cached != null) {
            // Optional: re-fetch if stale
            if (isFresh(cached)) {
                return cached;
            }
        }

        String ref = String.valueOf(GlobalMethods.generateTransactionId());

        ValidateSingleBvnReq bReq = new ValidateSingleBvnReq();
        bReq.setAppId(appId);
        bReq.setAppReference(ref);
        bReq.setBvn(bvn);

        // GetSingleBvnResponse resp = proxy.getSingleBvn(bvn, auth, subKey);
        if (!environment.equals("dev")) {
            GetSingleBvnResponse resp = breezePayVirtApiDevAcctProxy.VerifySingleBVN(bReq, auth, subKey);
            if (resp == null || resp.getData() == null) {
                // return cached even if stale (or null)
                return cached;
            }
            BvnLookup entity = (cached != null) ? cached : new BvnLookup();
            mapIntoEntity(entity, resp.getData());
            OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
            if (entity.getCreatedAt() == null) {
                entity.setCreatedAt(now);
            }
            entity.setLastCheckedAt(now);

            return repo.save(entity);
        }

        //GetSingleBvnResponse resp = breezePayVirtApiDevAcctProxy.VerifySingleBVN(bReq, auth, subKey);
        GetSingleBvnResponse resp = new GetSingleBvnResponse();
        DecodedJWTToken getDecoded = DecodedJWTToken.getDecoded(auth);
        String emailAddress = getDecoded.emailAddress;

        Optional<RegWalletInfo> getReg = regWalletInfoRepo.findByEmail(emailAddress);
        resp.setMessage("Success");
        resp.setStatus("success");
        BvnData vData = new BvnData();
        vData.setBvn(bvn);
        vData.setPhoneNumber1(uniqueIds.nextUniquePhoneNumber());
        vData.setEmail(emailAddress);
        vData.setFirstName(getReg.get().getFirstName());
        vData.setMiddleName(getReg.get().getMiddleName());
        vData.setLastName(getReg.get().getLastName());
        vData.setResponseCode("00");
        vData.setPhoneNumber(vData.getPhoneNumber());
        
        resp.setData(vData);

        if (resp == null || resp.getData() == null) {
            // return cached even if stale (or null)
            return cached;
        }

        BvnLookup entity = (cached != null) ? cached : new BvnLookup();
        mapIntoEntity(entity, resp.getData());
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        if (entity.getCreatedAt() == null) {
            entity.setCreatedAt(now);
        }
        entity.setLastCheckedAt(now);

        return repo.save(entity);
    }

    private boolean isFresh(BvnLookup e) {
        if (e.getLastCheckedAt() == null) {
            return false;
        }
        return e.getLastCheckedAt().isAfter(OffsetDateTime.now(ZoneOffset.UTC).minus(freshness));
    }

    private void mapIntoEntity(BvnLookup e, GetSingleBvnResponse.BvnData d) {
        e.setBvn(d.getBvn());
        e.setNameOnCard(d.getNameOnCard());
        e.setFirstName(d.getFirstName());
        e.setMiddleName(d.getMiddleName());
        e.setLastName(d.getLastName());
        e.setDateOfBirth(parseDate(d.getDateOfBirth()));
        e.setRegistrationDate(parseDate(d.getRegistrationDate()));
        e.setPhoneNumber1(d.getPhoneNumber1());
        e.setPhoneNumber2(d.getPhoneNumber2());
        e.setEnrollmentBank(d.getEnrollmentBank());
        e.setEnrollmentBranch(d.getEnrollmentBranch());
        e.setEmail(d.getEmail());
        e.setGender(d.getGender());
        e.setLevelOfAccount(d.getLevelOfAccount());
        e.setLgaOfOrigin(d.getLgaOfOrigin());
        e.setLgaOfResidence(d.getLgaOfResidence());
        e.setMaritalStatus(d.getMaritalStatus());
        e.setNin(d.getNin());
        e.setNationality(d.getNationality());
        e.setResidentialAddress(d.getResidentialAddress());
        e.setStateOfOrigin(d.getStateOfOrigin());
        e.setStateOfResidence(d.getStateOfResidence());
        e.setTitle(d.getTitle());
        e.setWatchListed(d.getWatchListed());
        e.setBase64Image(d.getBase64Image());
        e.setResponseCode(d.getResponseCode());
    }

    private LocalDate parseDate(String s) {
        if (s == null || s.trim().isEmpty()) {
            return null;
        }
        return LocalDate.parse(s.trim(), DMY_MON); // "22-Oct-1970"
    }
}
