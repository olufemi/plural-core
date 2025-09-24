package com.finacial.wealth.api.sessionmanager.entities;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

import lombok.Data;

@Entity
@Table(name="NOTIFICATION_CONFIG")
@Data
public class NotificationConfig implements Serializable {
	 /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private static final String SEQ_NAME = "FELLOWPAY_LT_SEQ";
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "pooled")
	@GenericGenerator(name = "pooled", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator", parameters = {
			@Parameter(name = "sequence_name", value = SEQ_NAME), @Parameter(name = "initial_value", value = "300"),
			@Parameter(name = "increment_size", value = "1"), @Parameter(name = "optimizer", value = "pooled") })

	@Column(name = "ID")
	private Long id;

	@Column(name = "type")
	private String type;

	@Column(name = "START_TIME")
	private String startTime;

	@Column(name = "END_TIME")
	private String endTime;

	@Column(name = "SMS_STATUS")
	private String smsStatus;

	@Column(name = "EMAIL_STATUS")
	private String emailStatus;
	
}
