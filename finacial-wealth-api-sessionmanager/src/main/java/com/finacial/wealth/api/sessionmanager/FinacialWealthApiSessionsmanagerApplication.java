package com.finacial.wealth.api.sessionmanager;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jms.annotation.EnableJms;

@ServletComponentScan
@SpringBootApplication
@EnableEurekaClient
@EnableFeignClients
@EnableJms
@EnableJpaRepositories
public class FinacialWealthApiSessionsmanagerApplication {

	public static void main(String[] args) {
		SpringApplication.run(FinacialWealthApiSessionsmanagerApplication.class, args);
	}

}
