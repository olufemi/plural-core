package com.finacial.wealth.api.fxpeer.exchange.inter.airtime.val.msisdn;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class NumberNormalizeResponse {

    @JsonAlias("errno")  private Integer errno;
    @JsonAlias("error")  private String  error;
    @JsonAlias("sysErr") private Integer sysErr;
    @JsonAlias("sysId")  private String  sysId;

    private String original;
    private String normalized;

    @JsonAlias({"isValid","valid"})
    @JsonProperty("isValid")
    private boolean valid;

    @JsonAlias({"isForma","format","countryInfo"})
    @JsonProperty("isForma")
    private Forma isForma;

    @JsonAlias("country")              // <— add this
    private String country;            // <— add this

    private Operator operator;

    // ---------- Nested DTOs ----------
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Forma {
        // Country code (e.g., "NG", "GB")
        @JsonAlias({"id","code"})
        private String id;

        // Alternate country codes (e.g., ["GB"])
        private List<String> alt;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Operator {
        // Provider sometimes sends string ids; keep as String for safety
        private String id;

        // Confidence might be integer or decimal; Double is safe
        private Double confidence;

        // Alt list can vary; keeping Integer list is fine, adjust if you see strings
        private List<Integer> alt;
    }

    // ---------- Convenience helpers (optional) ----------
    public boolean isOk() {
        if (errno != null)  return errno == 0;
        if (sysErr != null) return sysErr == 0;
        return Boolean.TRUE.equals(valid); // fallback
    }

    public String errorSummary() {
        if (errno != null && errno != 0)  return "errno=" + errno + (error != null ? (", error=" + error) : "");
        if (sysErr != null && sysErr != 0) return "sysErr=" + sysErr + (sysId != null ? (", sysId=" + sysId) : "");
        return null;
    }
}
