package com.finacial.wealth.api.fxpeer.exchange;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;


@SpringBootApplication
@EnableFeignClients
@EnableScheduling
public class FxPeerExchangeApplication {
public static void main(String[] args) {
SpringApplication.run(FxPeerExchangeApplication.class, args);
}
}
