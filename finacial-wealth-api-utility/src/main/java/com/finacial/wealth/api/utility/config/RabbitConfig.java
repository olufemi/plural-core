/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.finacial.wealth.api.utility.config;

/**
 *
 * @author olufemioshin
 */



import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    // Core exchange/queue/routing
    public static final String EXCHANGE = "finwealth.exchange";
    public static final String QUEUE = "finwealth.txn.history.queue";
    public static final String ROUTING_KEY = "finwealth.txn.history";

    // (Optional but recommended) DLQ setup so failed messages are not lost
    public static final String DLX = "finwealth.dlx";
    public static final String DLQ = "finwealth.txn.history.dlq";
    public static final String DLQ_ROUTING_KEY = "finwealth.txn.history.dlq";

    /**
     * Main queue with DLQ routing.
     * If listener fails (e.g., conversion error), message can be routed to DLQ (if rejected).
     */
    @Bean
    public Queue txnQueue() {
        return QueueBuilder.durable(QUEUE)
                .withArgument("x-dead-letter-exchange", DLX)
                .withArgument("x-dead-letter-routing-key", DLQ_ROUTING_KEY)
                .build();
    }

    @Bean
    public DirectExchange exchange() {
        return new DirectExchange(EXCHANGE);
    }

    @Bean
    public Binding binding() {
        return BindingBuilder
                .bind(txnQueue())
                .to(exchange())
                .with(ROUTING_KEY);
    }

    // DLQ exchange + queue + binding
    @Bean
    public DirectExchange deadLetterExchange() {
        return new DirectExchange(DLX);
    }

    @Bean
    public Queue deadLetterQueue() {
        return QueueBuilder.durable(DLQ).build();
    }

    @Bean
    public Binding deadLetterBinding() {
        return BindingBuilder
                .bind(deadLetterQueue())
                .to(deadLetterExchange())
                .with(DLQ_ROUTING_KEY);
    }

    /**
     * IMPORTANT FIX:
     * Register JavaTimeModule so java.time.Instant can be deserialized from ISO-8601 strings
     * like "2026-02-13T10:00:00Z".
     */
    @Bean
    public ObjectMapper rabbitObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return mapper;
    }

    /**
     * JSON message converter used by both consumers and publishers.
     */
    @Bean
    public MessageConverter jacksonConverter(ObjectMapper rabbitObjectMapper) {
        return new Jackson2JsonMessageConverter(rabbitObjectMapper);
    }

    /**
     * Listener container factory so @RabbitListener uses the same converter.
     */
    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory,
            MessageConverter jacksonConverter) {

        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(jacksonConverter);

        // Optional tuning (safe defaults)
        // factory.setConcurrentConsumers(1);
        // factory.setMaxConcurrentConsumers(3);

        return factory;
    }

    /**
     * RabbitTemplate for publishing messages with the same converter.
     */
    @Bean
    public RabbitTemplate rabbitTemplate(
            ConnectionFactory connectionFactory,
            MessageConverter jacksonConverter) {

        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jacksonConverter);
        return template;
    }
}



