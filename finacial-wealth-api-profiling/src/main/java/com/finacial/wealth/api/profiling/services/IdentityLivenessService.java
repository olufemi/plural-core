package com.finacial.wealth.api.profiling.services;

import com.finacial.wealth.api.profiling.domain.AddAccountDetails;
import com.finacial.wealth.api.profiling.domain.RegWalletInfo;
import com.finacial.wealth.api.profiling.identity.IdentityFaceProxy;
import com.finacial.wealth.api.profiling.identity.IdentityLivenessApiResponse;
import com.finacial.wealth.api.profiling.identity.IdentityLivenessSessionRequest;
import com.finacial.wealth.api.profiling.identity.IdentityLivenessSessionResponse;
import com.finacial.wealth.api.profiling.identity.IdentityLivenessVerifyRequest;
import com.finacial.wealth.api.profiling.identity.IdentityLivenessVerifyResponse;
import com.finacial.wealth.api.profiling.repo.AddAccountDetailsRepo;
import com.finacial.wealth.api.profiling.repo.RegWalletInfoRepository;
import com.finacial.wealth.api.profiling.response.BaseResponse;
import feign.FeignException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class IdentityLivenessService {

    private final Logger logger = LoggerFactory.getLogger(IdentityLivenessService.class);
    private final IdentityFaceProxy identityFaceProxy;
    private final AddAccountDetailsRepo addAccountDetailsRepo;
    private final RegWalletInfoRepository regWalletInfoRepository;

    public IdentityLivenessService(IdentityFaceProxy identityFaceProxy,
            AddAccountDetailsRepo addAccountDetailsRepo,
            RegWalletInfoRepository regWalletInfoRepository) {
        this.identityFaceProxy = identityFaceProxy;
        this.addAccountDetailsRepo = addAccountDetailsRepo;
        this.regWalletInfoRepository = regWalletInfoRepository;
    }

    public BaseResponse createLivenessSession(IdentityLivenessSessionRequest request) {
        IdentityLivenessSessionRequest identityRequest = toIdentitySessionRequest(request);
        try {
            logger.info("Creating liveness session requestReference={} subjectReference={}",
                    identityRequest.getRequestReference(), identityRequest.getSubjectReference());
            IdentityLivenessApiResponse<IdentityLivenessSessionResponse> response =
                    identityFaceProxy.createLivenessSession(identityRequest);
            return toBaseResponse(response, "Liveness session created");
        } catch (FeignException ex) {
            logger.warn("Identity liveness session rejected requestReference={} status={} response={}",
                    identityRequest.getRequestReference(), ex.status(), ex.contentUTF8());
            return identityFailure("Unable to create liveness session", ex);
        } catch (Exception ex) {
            logger.warn("Unable to create liveness session requestReference={}", identityRequest.getRequestReference(), ex);
            return failed("Unable to create liveness session");
        }
    }

    public BaseResponse verifyLiveness(IdentityLivenessVerifyRequest request) {
        IdentityLivenessVerifyRequest identityRequest = toIdentityVerifyRequest(request);
        try {
            logger.info("Verifying liveness sessionReference={} requestReference={}",
                    identityRequest.getSessionReference(), identityRequest.getRequestReference());
            IdentityLivenessApiResponse<IdentityLivenessVerifyResponse> response =
                    identityFaceProxy.verifyLiveness(identityRequest);
            return toBaseResponse(response, "Liveness verification completed");
        } catch (FeignException ex) {
            logger.warn("Identity liveness verification rejected sessionReference={} requestReference={} status={} response={}",
                    identityRequest.getSessionReference(), identityRequest.getRequestReference(), ex.status(), ex.contentUTF8());
            return identityFailure("Unable to verify liveness", ex);
        } catch (Exception ex) {
            logger.warn("Unable to verify liveness sessionReference={} requestReference={}",
                    identityRequest.getSessionReference(), identityRequest.getRequestReference(), ex);
            return failed("Unable to verify liveness");
        }
    }

    private IdentityLivenessSessionRequest toIdentitySessionRequest(IdentityLivenessSessionRequest request) {
        IdentityLivenessSessionRequest identityRequest = new IdentityLivenessSessionRequest();
        identityRequest.setSubjectReference(resolveIdentitySubjectReference(request.getSubjectReference()));
        identityRequest.setRequestReference(request.getRequestReference());
        identityRequest.setConsentReference(request.getConsentReference());
        identityRequest.setChannel(request.getChannel());
        return identityRequest;
    }

    private IdentityLivenessVerifyRequest toIdentityVerifyRequest(IdentityLivenessVerifyRequest request) {
        IdentityLivenessVerifyRequest identityRequest = new IdentityLivenessVerifyRequest();
        identityRequest.setSessionReference(request.getSessionReference());
        identityRequest.setRequestReference(request.getRequestReference());
        identityRequest.setSubjectReference(resolveIdentitySubjectReference(request.getSubjectReference()));
        identityRequest.setBase64Image(request.getBase64Image());
        identityRequest.setImageReference(request.getImageReference());
        return identityRequest;
    }

    private String resolveIdentitySubjectReference(String subjectReference) {
        if (!StringUtils.hasText(subjectReference)) {
            return subjectReference;
        }

        String trimmedSubject = subjectReference.trim();
        if (!trimmedSubject.contains("@")) {
            return trimmedSubject;
        }

        String email = trimmedSubject.toLowerCase();
        Optional<String> ngnWalletReference = resolveNgnAccountWalletReference(email);
        if (ngnWalletReference.isPresent()) {
            return ngnWalletReference.get();
        }

        Optional<RegWalletInfo> walletInfo = regWalletInfoRepository.findByEmail(email);
        if (!walletInfo.isPresent()) {
            logger.warn("Unable to resolve liveness subjectReference to customer reference for email={}", trimmedSubject);
            return trimmedSubject;
        }

        RegWalletInfo wallet = walletInfo.get();
        return customerReference(wallet);
    }

    private Optional<String> resolveNgnAccountWalletReference(String email) {
        return addAccountDetailsRepo.findByCountryCodeByEmailAddress("NGN", email)
                .stream()
                .filter(account -> StringUtils.hasText(account.getWalletId()))
                .findFirst()
                .map(account -> prefixWalletReference(account.getWalletId()));
    }

    private String customerReference(RegWalletInfo wallet) {
        if (StringUtils.hasText(wallet.getWalletId())) {
            return prefixWalletReference(wallet.getWalletId());
        }
        if (StringUtils.hasText(wallet.getCustomerId())) {
            return prefixCustomerReference(wallet.getCustomerId());
        }
        if (wallet.getId() != null) {
            return prefixWalletReference(String.valueOf(wallet.getId()));
        }
        return wallet.getEmail();
    }

    private String prefixWalletReference(String value) {
        String trimmedValue = value.trim();
        if (trimmedValue.startsWith("WAL-") || trimmedValue.startsWith("WLT-")) {
            return trimmedValue;
        }
        return "WLT-" + trimmedValue;
    }

    private String prefixCustomerReference(String value) {
        String trimmedValue = value.trim();
        if (trimmedValue.startsWith("CUS-")) {
            return trimmedValue;
        }
        return "CUS-" + trimmedValue;
    }

    private BaseResponse toBaseResponse(IdentityLivenessApiResponse<?> identityResponse, String defaultMessage) {
        BaseResponse response = new BaseResponse();
        if (identityResponse == null) {
            response.setStatusCode(400);
            response.setDescription(defaultMessage);
            return response;
        }

        response.setStatusCode(identityResponse.isSuccess() ? 200 : 400);
        response.setDescription(identityResponse.getMessage() == null ? defaultMessage : identityResponse.getMessage());
        Map<String, Object> data = new HashMap<>();
        data.put("success", identityResponse.isSuccess());
        data.put("code", identityResponse.getCode());
        data.put("correlationId", identityResponse.getCorrelationId());
        data.put("timestamp", identityResponse.getTimestamp());
        data.put("result", identityResponse.getData());
        data.put("error", identityResponse.getError());
        response.setData(data);
        return response;
    }

    private BaseResponse failed(String message) {
        BaseResponse response = new BaseResponse();
        response.setStatusCode(400);
        response.setDescription(message);
        return response;
    }

    private BaseResponse identityFailure(String message, FeignException ex) {
        BaseResponse response = failed(message);
        Map<String, Object> data = new HashMap<>();
        data.put("identityStatus", ex.status());
        data.put("identityError", ex.contentUTF8());
        response.setData(data);
        return response;
    }
}
