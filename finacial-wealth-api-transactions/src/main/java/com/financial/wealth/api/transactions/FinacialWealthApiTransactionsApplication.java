package com.financial.wealth.api.transactions;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories
@EnableFeignClients
@EnableEurekaClient
public class FinacialWealthApiTransactionsApplication {

	public static void main(String[] args) {
		SpringApplication.run(FinacialWealthApiTransactionsApplication.class, args);
	}

}
