package com.finacial.wealth.api.sessionmanager.utils;

import org.owasp.html.HtmlPolicyBuilder;
import org.owasp.html.PolicyFactory;

public class Constant {

    public static final String QUICKPAY = "quickpay";

    public static final String GLOBAL = "global";

    public static String sanitizeHTML(String untrustedHTML) {
        PolicyFactory policy = new HtmlPolicyBuilder()
                .allowAttributes("src").onElements("img")
                .allowAttributes("href").onElements("a")
                .allowStandardUrlProtocols()
                .allowElements(
                        "a", "img"
                ).toFactory();

        return policy.sanitize(untrustedHTML);
    }

}
