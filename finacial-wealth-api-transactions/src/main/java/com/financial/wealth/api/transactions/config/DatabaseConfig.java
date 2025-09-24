package com.financial.wealth.api.transactions.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableJpaRepositories("com.accessbank")
@EnableTransactionManagement
public class DatabaseConfig {

}
