/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.finacial.wealth.api.utility.domains;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.Data;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

/**
 *
 * @author HRH
 */
@Entity
@Table(name = "SCH_FREQUENCY_DATA_CONFIG")
@Data
public class ScheduleFrequencyDataConfig implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final String SEQ_NAME = "SCH_FREQUENCY_DATA_CONFIG_SEQ";
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "pooled")
    @GenericGenerator(
            name = "pooled",
            strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
            parameters = {
                @Parameter(name = "sequence_name", value = SEQ_NAME),
                @Parameter(name = "initial_value", value = "300"),
                @Parameter(name = "increment_size", value = "1"),
                @Parameter(name = "optimizer", value = "pooled")
            }
    )
    @Column(name = "ID")
    private Long id;
    private String configuredFrequency;
    @Column(name = "FREQUENCY_VALUE")
    private String frequencyValue;
    
    
    

}
