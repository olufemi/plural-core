package com.financial.wealth.api.transactions;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableFeignClients
@EnableEurekaClient
@EnableScheduling
@EnableJpaRepositories(basePackages = {
        "com.financial.wealth.api.transactions.repo",
        "com.financial.wealth.api.transactions.breezepay.payout",
        "com.financial.wealth.api.transactions.services.fx.p2.p2.wallet",
        "com.financial.wealth.api.transactions.services.grp.sav.fulfil",
        "com.financial.wealth.api.transactions.services.grp.savings.wallet"
})
@EntityScan(basePackages = "com.financial.wealth.api.transactions")
public class FinacialWealthApiTransactionsApplication {

	public static void main(String[] args) {
		SpringApplication.run(FinacialWealthApiTransactionsApplication.class, args);
	}

}
