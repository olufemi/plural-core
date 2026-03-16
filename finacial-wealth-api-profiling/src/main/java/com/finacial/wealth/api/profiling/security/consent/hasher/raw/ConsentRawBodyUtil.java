/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.finacial.wealth.api.profiling.security.consent.hasher.raw;

/**
 *
 * @author olufemioshin
 */
import javax.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;

public final class ConsentRawBodyUtil {

    private ConsentRawBodyUtil() {
    }

    public static String getRawRequestBody(HttpServletRequest request) {
        if (!(request instanceof CachedBodyHttpServletRequest)) {
            throw new IllegalStateException("Request not wrapped with CachedBodyHttpServletRequest");
        }

        CachedBodyHttpServletRequest wrapper = (CachedBodyHttpServletRequest) request;
        return new String(wrapper.getCachedBody(), StandardCharsets.UTF_8);
    }
}
