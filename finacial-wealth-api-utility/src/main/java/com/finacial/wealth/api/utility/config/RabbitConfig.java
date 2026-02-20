/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.finacial.wealth.api.utility.config;

/**
 *
 * @author olufemioshin
 */
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.amqp.support.converter.SimpleMessageConverter;

@Configuration
public class RabbitConfig {

    // ===================== EMAIL =====================
    public static final String EMAIL_EXCHANGE = "finwealth.email.exchange";
    public static final String EMAIL_QUEUE = "finwealth.email.queue";
    public static final String EMAIL_ROUTING_KEY = "finwealth.email";

    public static final String EMAIL_DLX = "finwealth.email.dlx";
    public static final String EMAIL_DLQ = "finwealth.email.dlq";
    public static final String EMAIL_DLQ_ROUTING_KEY = "finwealth.email.dlq";

    // ===================== TXN HISTORY (EXISTING QUEUE) =====================
    public static final String TXN_EXCHANGE = "finwealth.exchange";
    public static final String TXN_QUEUE = "finwealth.txn.history.queue";
    public static final String TXN_ROUTING_KEY = "finwealth.txn.history";

    public static final String QUEUE = TXN_QUEUE;
    public static final String EXCHANGE = TXN_EXCHANGE;
    public static final String ROUTING_KEY = TXN_ROUTING_KEY;

    // NOTE:
    // Do NOT declare DLX args for TXN_QUEUE here because the broker already has this queue
    // created WITHOUT DLX args. Adding DLX args causes PRECONDITION_FAILED 406.
    // ===================== NOTIFICATIONS =====================
    public static final String NOTIF_EXCHANGE = "finwealth.notifications.exchange";
    public static final String NOTIF_QUEUE = "finwealth.notifications.queue";
    public static final String NOTIF_ROUTING_KEY = "finwealth.notifications";

    public static final String NOTIF_DLX = "finwealth.notifications.dlx";
    public static final String NOTIF_DLQ = "finwealth.notifications.dlq";
    public static final String NOTIF_DLQ_ROUTING_KEY = "finwealth.notifications.dlq";

    // ===================== EMAIL BEANS =====================
    @Bean
    public DirectExchange emailExchange() {
        return new DirectExchange(EMAIL_EXCHANGE);
    }

    @Bean
    public Queue emailQueue() {
        return QueueBuilder.durable(EMAIL_QUEUE)
                .withArgument("x-dead-letter-exchange", EMAIL_DLX)
                .withArgument("x-dead-letter-routing-key", EMAIL_DLQ_ROUTING_KEY)
                .build();
    }

    @Bean
    public Binding emailBinding() {
        return BindingBuilder.bind(emailQueue())
                .to(emailExchange())
                .with(EMAIL_ROUTING_KEY);
    }

    @Bean
    public DirectExchange emailDeadLetterExchange() {
        return new DirectExchange(EMAIL_DLX);
    }

    @Bean
    public Queue emailDeadLetterQueue() {
        return QueueBuilder.durable(EMAIL_DLQ).build();
    }

    @Bean
    public Binding emailDeadLetterBinding() {
        return BindingBuilder.bind(emailDeadLetterQueue())
                .to(emailDeadLetterExchange())
                .with(EMAIL_DLQ_ROUTING_KEY);
    }

    // ===================== TXN HISTORY BEANS =====================
    @Bean
    public DirectExchange txnExchange() {
        return new DirectExchange(TXN_EXCHANGE);
    }

    @Bean
    public Queue txnQueue() {
        // IMPORTANT: keep EXACT declaration (no DLX args) to avoid mismatch on broker
        return QueueBuilder.durable(TXN_QUEUE).build();
    }

    @Bean
    public Binding txnBinding() {
        return BindingBuilder.bind(txnQueue())
                .to(txnExchange())
                .with(TXN_ROUTING_KEY);
    }

    // ===================== NOTIFICATIONS BEANS =====================
    @Bean
    public DirectExchange notifExchange() {
        return new DirectExchange(NOTIF_EXCHANGE);
    }

    @Bean
    public Queue notifQueue() {
        return QueueBuilder.durable(NOTIF_QUEUE)
                .withArgument("x-dead-letter-exchange", NOTIF_DLX)
                .withArgument("x-dead-letter-routing-key", NOTIF_DLQ_ROUTING_KEY)
                .build();
    }

    @Bean
    public Binding notifBinding() {
        return BindingBuilder.bind(notifQueue())
                .to(notifExchange())
                .with(NOTIF_ROUTING_KEY);
    }

    @Bean
    public DirectExchange notifDeadLetterExchange() {
        return new DirectExchange(NOTIF_DLX);
    }

    @Bean
    public Queue notifDeadLetterQueue() {
        return QueueBuilder.durable(NOTIF_DLQ).build();
    }

    @Bean
    public Binding notifDeadLetterBinding() {
        return BindingBuilder.bind(notifDeadLetterQueue())
                .to(notifDeadLetterExchange())
                .with(NOTIF_DLQ_ROUTING_KEY);
    }

    // ===================== JSON CONVERTER / TEMPLATE / LISTENER FACTORY =====================
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
public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(ConnectionFactory connectionFactory) {

    SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
    factory.setConnectionFactory(connectionFactory);

    // For Option 2: we want raw bytes via Message.getBody()
    factory.setMessageConverter(new SimpleMessageConverter());

    // reject (no infinite requeue). DLQ if DLX configured.
    factory.setDefaultRequeueRejected(false);

    return factory;
}

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory,
            MessageConverter jacksonConverter) {

        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jacksonConverter);
        return template;
    }
}
