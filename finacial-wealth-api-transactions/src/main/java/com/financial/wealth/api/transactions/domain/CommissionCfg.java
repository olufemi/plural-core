/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.financial.wealth.api.transactions.domain;

import java.io.Serializable;
import java.math.BigDecimal;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import lombok.Data;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

/**
 *
 * @author HRH
 */
@Entity
@Data
public class CommissionCfg implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final String SEQ_NAME = "COMMISSION_CFG_SEQ";
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
    private boolean enable;
    private String transType;
    private int amountMin;
    private int amountMax;
    private BigDecimal charges;
    private BigDecimal commPercent;
    private String hasPercent;
    private BigDecimal vatRate;
    private BigDecimal fee;

    @Override
    public String toString() {
        return "CommissionCfg{" + "id = " + id + ", transType = " + transType + ","
                + " amountMin = " + amountMin + ","
                + " amountMax = " + amountMax + ", "
                + " enable = " + enable + ", "
                + " vatRate = " + vatRate + ", "
                + " fee = " + fee + ", "
                + "charges = " + charges + ", commPercent = " + commPercent + '}';
    }

}
