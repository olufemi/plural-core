package com.finacial.wealth.api.fxpeer.exchange.inter.airtime.val.msisdn;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class NumberNormalizeResponse {

    // -------- Top-level fields from Sochitel --------
    @JsonAlias("errno")
    private Integer errno;

    @JsonAlias("error")
    private String error;

    @JsonAlias("sysErr")
    private Integer sysErr;

    @JsonAlias("sysId")
    private String sysId;

    // The real payload is inside "response": { ... }
    @JsonProperty("response")
    private ResponseData response;

    // ========= Nested DTOs =========

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ResponseData {

        @JsonProperty("original")
        private String original;

        @JsonProperty("normalized")
        private String normalized;

        @JsonProperty("isFormallyValid")
        private Boolean formallyValid;

        @JsonProperty("isValid")
        private Boolean valid;

        // JSON: "country": { "id": "NG", "alt": ["NG"] }
        @JsonProperty("country")
        private Forma country;

        // JSON: "operator": { "id": "18", "confidence": 0, "alt": [1,18,19,20] }
        @JsonProperty("operator")
        private Operator operator;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Forma {
        // "NG", "GB", etc
        @JsonAlias({"id", "code"})
        private String id;

        // ["NG"], ["GB"], etc
        private List<String> alt;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Operator {
        private String id;
        private Double confidence;
        private List<Integer> alt;
    }

    // ========= Convenience helpers =========

    /**
     * Overall provider OK:
     *  - errno == 0  OR
     *  - sysErr == 0 OR
     *  - fall back to phone validity.
     */
    @JsonIgnore
    public boolean isOk() {
        if (errno != null)  return errno == 0;
        if (sysErr != null) return sysErr == 0;
        return isValid();
    }

    @JsonIgnore
    public String errorSummary() {
        if (errno != null && errno != 0) {
            return "errno=" + errno + (error != null ? (", error=" + error) : "");
        }
        if (sysErr != null && sysErr != 0) {
            return "sysErr=" + sysErr + (sysId != null ? (", sysId=" + sysId) : "");
        }
        return null;
    }

    // ===== Backwards-compatible helpers (so old code still compiles) =====

    // This is what your logic uses: n.isValid()
    @JsonIgnore
    public boolean isValid() {
        return response != null && Boolean.TRUE.equals(response.getValid());
    }

    // For existing calls like: n.getIsForma().getId()
    @JsonIgnore
    public Forma getIsForma() {
        return (response != null) ? response.getCountry() : null;
    }

    // If somewhere you did n.getOriginal()
    @JsonIgnore
    public String getOriginal() {
        return (response != null) ? response.getOriginal() : null;
    }

    // If somewhere you did n.getNormalized()
    @JsonIgnore
    public String getNormalized() {
        return (response != null) ? response.getNormalized() : null;
    }

    // Optional: top-level access to country and operator if you want
    @JsonIgnore
    public Forma getCountry() {
        return (response != null) ? response.getCountry() : null;
    }

    @JsonIgnore
    public Operator getOperator() {
        return (response != null) ? response.getOperator() : null;
    }
}
