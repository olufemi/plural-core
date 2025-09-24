/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.finacial.wealth.api.sessionmanager.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 *
 * @author gol
 */
@Component
public class RabbitMQConsumer {

    // @Qualifier("withEureka")
    // @Autowired
    // private RestTemplate restTemplate;

    // @Value("${nxg.service.notification.name}")
    // private String notificationService;
    
    

    // @RabbitListener(queues = "${session.manager.rabbitmq.emailqueue}")
    // public void recievedEmailMessage(EmailRequestWithDisplayName emailRequest) throws Exception {
    //     restTemplate.postForObject(
	// 			"http://" + notificationService + "/notification-manager/email-with-dispaly-name", 
	// 			emailRequest, BaseResponse.class);

    // }
    
    
    // @RabbitListener(queues = "${session.manager.rabbitmq.smsqueue}")
    // public void recievedSmsMessage(SMSRequest smsRequest) throws Exception {
    //    restTemplate.postForObject("http://"+notificationService+"/notification-manager/sms", 
	// 			smsRequest, BaseResponse.class);

    // }
}
