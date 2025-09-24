/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.finacial.wealth.api.sessionmanager.services;



import org.springframework.stereotype.Service;

/**
 *
 * @author gol
 */
@Service
public class RabbitMQSender {
    
    // @Autowired
	// private AmqpTemplate rabbitTemplate;
	
	// @Value("${session.manager.rabbitmq.emailexchange}")
	// private String emailExchange;
	
	// @Value("${session.manager.rabbitmq.emailroutingkey}")
	// private String emailRoutingkey;
        
    // @Value("${session.manager.rabbitmq.smsexchange}")
	// private String smsExchange;
	
	// @Value("${session.manager.rabbitmq.smsroutingkey}")
	// private String smsRoutingkey;
        
	
	// public BaseResponse queueEmail(EmailRequestWithDisplayName emailRq) {
    //             BaseResponse rp= new BaseResponse();
	// 	rabbitTemplate.convertAndSend(emailExchange, emailRoutingkey, emailRq);
    //             rp.setStatusCode(200);
    //             rp.setDescription("Your request is being processed");
	//     return rp;
	// }
        
    //     public BaseResponse queueSms(SMSRequest smsRq) {
    //             BaseResponse rp= new BaseResponse();
	// 	rabbitTemplate.convertAndSend(smsExchange, smsRoutingkey, smsRq);
    //             rp.setStatusCode(200);
    //             rp.setDescription("Your request is being processed");
	//     return rp;
	// }
    
}
