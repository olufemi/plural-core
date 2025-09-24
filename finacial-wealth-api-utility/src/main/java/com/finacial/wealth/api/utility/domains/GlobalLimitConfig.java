/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.finacial.wealth.api.utility.domains;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

/**
 *
 * @author HRH
 */
@Entity
public class GlobalLimitConfig extends BaseTransactionLimitConfig implements Serializable {

    private static final String SEQ_NAME = "GLOBAL_LIMIT_CONFIG_SEQ";
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "pooled")
    @GenericGenerator(
            name = "pooled",
            strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
            parameters = {
                @Parameter(name = "sequence_name", value = SEQ_NAME),
                @Parameter(name = "initial_value", value = "4"),
                @Parameter(name = "increment_size", value = "1"),
                @Parameter(name = "optimizer", value = "pooled")
            }
    )
    @JsonIgnore
    @Column(name = "ID")
    private Long id;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

}
