package com.finacial.wealth.api.profiling.services;

import com.finacial.wealth.api.profiling.identity.IdentityFaceProxy;
import com.finacial.wealth.api.profiling.identity.IdentityLivenessApiResponse;
import com.finacial.wealth.api.profiling.identity.IdentityLivenessSessionRequest;
import com.finacial.wealth.api.profiling.identity.IdentityLivenessSessionResponse;
import com.finacial.wealth.api.profiling.identity.IdentityLivenessVerifyRequest;
import com.finacial.wealth.api.profiling.identity.IdentityLivenessVerifyResponse;
import com.finacial.wealth.api.profiling.response.BaseResponse;
import feign.FeignException;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class IdentityLivenessService {

    private final Logger logger = LoggerFactory.getLogger(IdentityLivenessService.class);
    private final IdentityFaceProxy identityFaceProxy;

    public IdentityLivenessService(IdentityFaceProxy identityFaceProxy) {
        this.identityFaceProxy = identityFaceProxy;
    }

    public BaseResponse createLivenessSession(IdentityLivenessSessionRequest request) {
        try {
            logger.info("Creating liveness session requestReference={}", request.getRequestReference());
            IdentityLivenessApiResponse<IdentityLivenessSessionResponse> response =
                    identityFaceProxy.createLivenessSession(request);
            return toBaseResponse(response, "Liveness session created");
        } catch (FeignException ex) {
            logger.warn("Identity liveness session rejected requestReference={} status={} response={}",
                    request.getRequestReference(), ex.status(), ex.contentUTF8());
            return identityFailure("Unable to create liveness session", ex);
        } catch (Exception ex) {
            logger.warn("Unable to create liveness session requestReference={}", request.getRequestReference(), ex);
            return failed("Unable to create liveness session");
        }
    }

    public BaseResponse verifyLiveness(IdentityLivenessVerifyRequest request) {
        try {
            logger.info("Verifying liveness sessionReference={} requestReference={}",
                    request.getSessionReference(), request.getRequestReference());
            IdentityLivenessApiResponse<IdentityLivenessVerifyResponse> response =
                    identityFaceProxy.verifyLiveness(request);
            return toBaseResponse(response, "Liveness verification completed");
        } catch (FeignException ex) {
            logger.warn("Identity liveness verification rejected sessionReference={} requestReference={} status={} response={}",
                    request.getSessionReference(), request.getRequestReference(), ex.status(), ex.contentUTF8());
            return identityFailure("Unable to verify liveness", ex);
        } catch (Exception ex) {
            logger.warn("Unable to verify liveness sessionReference={} requestReference={}",
                    request.getSessionReference(), request.getRequestReference(), ex);
            return failed("Unable to verify liveness");
        }
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
