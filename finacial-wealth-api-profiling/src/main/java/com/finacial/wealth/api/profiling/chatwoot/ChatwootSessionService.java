package com.finacial.wealth.api.profiling.chatwoot;

import com.finacial.wealth.api.profiling.domain.RegWalletInfo;
import com.finacial.wealth.api.profiling.repo.RegWalletInfoRepository;
import com.finacial.wealth.api.profiling.response.BaseResponse;
import com.finacial.wealth.api.profiling.utils.DecodedJWTToken;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class ChatwootSessionService {

    private static final String HMAC_SHA256 = "HmacSHA256";

    private final RegWalletInfoRepository regWalletInfoRepository;

    @Value("${fin.wealth.chatwoot.enabled:false}")
    private boolean enabled;

    @Value("${fin.wealth.chatwoot.base-url:}")
    private String baseUrl;

    @Value("${fin.wealth.chatwoot.website-token:}")
    private String websiteToken;

    @Value("${fin.wealth.chatwoot.hmac-secret:}")
    private String hmacSecret;

    @Value("${fin.wealth.chatwoot.locale:en}")
    private String locale;

    @Value("${fin.wealth.chatwoot.color-scheme:auto}")
    private String colorScheme;

    public ChatwootSessionService(RegWalletInfoRepository regWalletInfoRepository) {
        this.regWalletInfoRepository = regWalletInfoRepository;
    }

    public BaseResponse getSession(String authorization) {
        BaseResponse response = new BaseResponse();
        try {
            if (!enabled) {
                response.setStatusCode(503);
                response.setDescription("Live chat is not enabled");
                return response;
            }
            if (!StringUtils.hasText(baseUrl) || !StringUtils.hasText(websiteToken) || !StringUtils.hasText(hmacSecret)) {
                response.setStatusCode(500);
                response.setDescription("Live chat is not configured");
                return response;
            }

            DecodedJWTToken decoded = DecodedJWTToken.getDecoded(authorization);
            String emailAddress = normalize(decoded.emailAddress);
            if (!StringUtils.hasText(emailAddress)) {
                response.setStatusCode(401);
                response.setDescription("Invalid customer session");
                return response;
            }

            RegWalletInfo customer = regWalletInfoRepository.findByEmail(emailAddress).orElse(null);

            Map<String, Object> user = new HashMap<String, Object>();
            user.put("identifier", emailAddress);
            user.put("email", emailAddress);
            user.put("name", resolveFullName(customer, decoded, emailAddress));
            user.put("identifier_hash", hmacSha256Hex(hmacSecret, emailAddress));

            Map<String, Object> payload = new HashMap<String, Object>();
            payload.put("baseUrl", baseUrl);
            payload.put("websiteToken", websiteToken);
            payload.put("locale", locale);
            payload.put("colorScheme", colorScheme);
            payload.put("user", user);
            payload.put("customAttributes", new HashMap<String, Object>());

            response.setStatusCode(200);
            response.setDescription("Live chat session generated successfully");
            response.setData(payload);
            return response;
        } catch (Exception ex) {
            response.setStatusCode(500);
            response.setDescription("Unable to generate live chat session");
            return response;
        }
    }

    private String hmacSha256Hex(String secret, String message) throws Exception {
        Mac mac = Mac.getInstance(HMAC_SHA256);
        mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), HMAC_SHA256));
        byte[] digest = mac.doFinal(message.getBytes(StandardCharsets.UTF_8));
        StringBuilder hex = new StringBuilder(digest.length * 2);
        for (byte b : digest) {
            hex.append(String.format("%02x", b & 0xff));
        }
        return hex.toString();
    }

    private String resolveFullName(RegWalletInfo customer, DecodedJWTToken decoded, String emailAddress) {
        if (customer != null && StringUtils.hasText(customer.getFullName())) {
            return customer.getFullName();
        }
        String firstName = customer != null ? customer.getFirstName() : decoded.firstName;
        String lastName = customer != null ? customer.getLastName() : null;
        String fullName = ((firstName == null ? "" : firstName) + " " + (lastName == null ? "" : lastName)).trim();
        return StringUtils.hasText(fullName) ? fullName : emailAddress;
    }

    private String normalize(String value) {
        return value == null ? null : value.trim().toLowerCase();
    }
}
