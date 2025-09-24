package com.finacial.wealth.api.gateway.helpers;

import com.netflix.zuul.context.RequestContext;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.util.StreamUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Enumeration;

public class RequestHelper {

    private static final Logger logger = LoggerFactory.getLogger(RequestHelper.class);
    private static final String REQUEST_BODY = "REQUEST_BODY";

    public static String readBody(RequestContext context) {

        String body = "";
        InputStream in = null;

        if(context.get(REQUEST_BODY) == null) { //read the body afresh

            try {

                in = (InputStream) context.get("requestEntity");
                if (in == null) {
                    in = context.getRequest().getInputStream();
                }
                body = StreamUtils.copyToString(in, Charset.forName("UTF-8"));
                context.set("requestEntity", new ByteArrayInputStream(body.getBytes("UTF-8")));
                context.set(REQUEST_BODY, StringUtils.defaultString(body));
            }
            catch (IOException e) {
            }
            finally {
                if(in != null) {
                    try {
                        in.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        else { //return the cached value
            body = String.valueOf(context.get(REQUEST_BODY));
        }

        return body;

    }

    public static String readHeaders(RequestContext context) {
        String headers = "";
        HttpServletRequest request = context.getRequest();
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            headers += headerName + ":" + request.getHeader(headerName) + ",";
        }

        //remove last comma
        headers = headers.substring(0, headers.length() - 1);

        return headers;
    }


}
