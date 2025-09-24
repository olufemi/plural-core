package com.finacial.wealth.api.gateway.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpResponse;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

public class GatewayClientHttpResponse implements ClientHttpResponse {

    private HttpStatus httpStatus;
    private byte [] message;
    private HttpHeaders httpHeaders;

    public GatewayClientHttpResponse(HttpStatus httpStatus, byte [] message) {
        this.httpStatus = httpStatus;
        this.message = message;
        httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
    }

    @Override
    public HttpStatus getStatusCode() throws IOException {
        return httpStatus;
    }

    @Override
    public int getRawStatusCode() throws IOException {
        return httpStatus.value();
    }

    @Override
    public String getStatusText() throws IOException {
        return httpStatus.getReasonPhrase();
    }

    @Override
    public void close() {
    }

    @Override
    public InputStream getBody() throws IOException {
        return new ByteArrayInputStream(message);
    }

    @Override
    public HttpHeaders getHeaders() {
        return httpHeaders;
    }
}
