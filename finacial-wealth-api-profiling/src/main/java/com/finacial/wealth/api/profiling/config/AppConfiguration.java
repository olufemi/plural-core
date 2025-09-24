package com.finacial.wealth.api.profiling.config;

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
