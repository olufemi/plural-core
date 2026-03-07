/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.finacial.wealth.api.fxpeer.exchange.config;

/**
 *
 * @author olufemioshin
 */
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;
import org.springframework.core.env.Environment;
import org.springframework.beans.factory.annotation.Autowired;

@Configuration
public class RabbitClientConfig {

    @Autowired
    private Environment env;

    @PostConstruct
    public void showRabbitCfg() {

        System.out.println("========== RabbitMQ CONFIG (FXPEER) ==========");
        System.out.println("rabbit.host=" + env.getProperty("spring.rabbitmq.host"));
        System.out.println("rabbit.port=" + env.getProperty("spring.rabbitmq.port"));
        System.out.println("rabbit.username=" + env.getProperty("spring.rabbitmq.username"));
        System.out.println("rabbit.virtual-host=" + env.getProperty("spring.rabbitmq.virtual-host"));
        System.out.println("===============================================");
    }

    @Bean
    public ObjectMapper rabbitObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return mapper;
    }

    @Bean
    public MessageConverter jacksonConverter(ObjectMapper rabbitObjectMapper) {
        return new Jackson2JsonMessageConverter(rabbitObjectMapper);
    }

    @Bean
    public RabbitTemplate rabbitTemplate(
            ConnectionFactory connectionFactory,
            MessageConverter jacksonConverter) {

        RabbitTemplate template = new RabbitTemplate(connectionFactory);

        template.setMessageConverter(jacksonConverter);

        // REQUIRED: detect routing failures
        template.setMandatory(true);

        // CONFIRM callback (exchange received message)
        template.setConfirmCallback((correlationData, ack, cause) -> {
            if (ack) {
                System.out.println("CONFIRMED: message delivered to exchange");
            } else {
                System.err.println("NOT CONFIRMED: " + cause);
            }
        });

        // RETURN callback (exchange could NOT route to queue)
        template.setReturnsCallback(returned -> {
            System.err.println("RETURNED (NO ROUTE):");
            System.err.println("Exchange: " + returned.getExchange());
            System.err.println("RoutingKey: " + returned.getRoutingKey());
            System.err.println("ReplyText: " + returned.getReplyText());
        });

        return template;
    }
}
