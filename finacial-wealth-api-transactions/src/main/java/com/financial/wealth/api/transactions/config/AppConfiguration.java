package com.financial.wealth.api.transactions.config;

import org.apache.catalina.filters.RemoteIpFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfiguration {
	@Bean
    public RemoteIpFilter remoteIpFilter() {
        return new RemoteIpFilter();
    }
}
