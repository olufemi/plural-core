package com.financial.wealth.api.transactions.config;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;


@Configuration
@EnableEurekaClient
public class RestTemplateConfig {
	
	@Bean
	@LoadBalanced
	@Qualifier("withEureka")
	public RestTemplate restTemplate() {
		return new RestTemplate();
	}
	
	@Primary
	@Qualifier("withoutEureka")
	@Bean
	public RestTemplate restTemplateExternal() throws KeyStoreException, NoSuchAlgorithmException, KeyManagementException {
	   CloseableHttpClient httpClient = HttpClients.custom().setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE).build();
        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory(httpClient);
		factory.setConnectTimeout(120000);
		factory.setReadTimeout(120000);
        RestTemplate restTemplate = new RestTemplate(factory);
        return restTemplate;
	}
}
