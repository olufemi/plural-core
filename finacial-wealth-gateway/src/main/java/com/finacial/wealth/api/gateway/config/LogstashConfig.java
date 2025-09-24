package com.finacial.wealth.api.gateway.config;


import ch.qos.logback.classic.AsyncAppender;
import ch.qos.logback.classic.LoggerContext;
import net.logstash.logback.appender.LogstashTcpSocketAppender;
import net.logstash.logback.encoder.LogstashEncoder;
import net.logstash.logback.stacktrace.ShortenedThrowableConverter;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.net.InetSocketAddress;

@Component
public class LogstashConfig {

    private static final Logger logger = LoggerFactory.getLogger(LogstashConfig.class);

    @Value("${logstash.host:localhost}")
    String logstashHost;

    @Value("${logstash.port:0}")
    Integer logstashPort;

    @Value("${server.port}")
    String serverPort;

    @Value("${spring.application.name}")
    String applicationName;
  
    @PostConstruct
    public void initLogstashAppender() {
        setAccessLogstashAppender(applicationName, serverPort, logstashHost, logstashPort);
    }

    private static void setAccessLogstashAppender(String applicationName, String serverPort, String logstashHost, Integer logstashPort) {

        if(StringUtils.isEmpty(logstashHost) || logstashPort <= 0) return;

        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();

        LogstashTcpSocketAppender logstashAppender = new LogstashTcpSocketAppender();
        logstashAppender.setName(applicationName.toLowerCase());
        logstashAppender.setContext(context);
        logstashAppender.addDestinations(new InetSocketAddress(logstashHost, logstashPort));

        String customFields = "{\"serviceName\":\"" + applicationName + "\",\"servicePort\":\"" + serverPort + "\"}";
        LogstashEncoder logstashEncoder = new LogstashEncoder();
        logstashEncoder.setCustomFields(customFields);

        ShortenedThrowableConverter throwableConverter = new ShortenedThrowableConverter();
        throwableConverter.setRootCauseFirst(true);
        logstashEncoder.setThrowableConverter(throwableConverter);

        logstashAppender.setEncoder(logstashEncoder);
        logstashAppender.start();

        // Wrap the appender in an Async appender for performance
        AsyncAppender asyncLogstashAppender = new AsyncAppender();
        asyncLogstashAppender.setContext(context);
        asyncLogstashAppender.setName(applicationName.toLowerCase());
        asyncLogstashAppender.addAppender(logstashAppender);
        asyncLogstashAppender.setDiscardingThreshold(0); //keep all logs even when the queue is almost full
        asyncLogstashAppender.start();

        //add the appender to the loggerName supplied
        context.getLogger(Logger.ROOT_LOGGER_NAME).addAppender(asyncLogstashAppender);


    }
  
}
