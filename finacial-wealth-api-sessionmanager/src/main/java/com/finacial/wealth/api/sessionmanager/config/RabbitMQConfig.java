/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.finacial.wealth.api.sessionmanager.config;

import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 *
 * @author gol
 */
@Configuration
public class RabbitMQConfig {

    @Bean
    public org.springframework.amqp.support.converter.MessageConverter jacksonConverter() {

        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(
            ConnectionFactory connectionFactory,
            org.springframework.amqp.support.converter.MessageConverter jacksonConverter) {

        RabbitTemplate template
                = new RabbitTemplate(connectionFactory);

        template.setMessageConverter(jacksonConverter);

        return template;
    }

}
