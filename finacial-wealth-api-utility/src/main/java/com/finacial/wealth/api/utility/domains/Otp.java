
package com.finacial.wealth.api.utility.domains;

import java.io.Serializable;


import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Entity;
import javax.persistence.Id;

import javax.persistence.GenerationType;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@Table(name = "OTPNEW")
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Otp extends AbstractAuditingEntity  implements Serializable {

   private static final long serialVersionUID = 1L;

   private static final String SEQ_NAME = "AGB_OTPS_SEQ";
   @Id
   @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "pooled")
   @GenericGenerator(   name = "pooled",
           strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
           parameters = {
               @Parameter(name = "sequence_name", value = SEQ_NAME),
               @Parameter(name = "initial_value", value = "300"),
               @Parameter(name = "increment_size", value = "1"),
               @Parameter(name = "optimizer", value = "pooled")
           }
   )
   @Column(name = "ID")
   Long id;
   
   @Column(name = "USER_ID")
   private String userId;

   @Column(name = "PHONE_NUMBER")
   private String phoneNumber;
   
   @Column(name = "OTP")
   private String otp;
  

   @Column(name = "EXPIRY")
   private Long expiry;
   
   @Column(name = "IS_USED")
   private boolean isUsed;

   @Column(name = "SERVICE_NAME")
   private String serviceName;

   @Column(name = "REQUEST_ID")
   private String requestId;

   @Column(name = "ATTEMPTS")
   private Long attempts = 0L;

   @Column(name = "NEW_USER_ID")
   private String newUserId;

}