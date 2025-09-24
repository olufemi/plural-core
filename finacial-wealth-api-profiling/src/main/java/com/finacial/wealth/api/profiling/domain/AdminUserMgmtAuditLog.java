/*
 To change this license header, choose License Headers in Project Properties.
 To change this template file, choose Tools | Templates
 and open the template in the editor.
 */
package com.finacial.wealth.api.profiling.domain;

import java.io.Serializable;
import java.time.LocalDateTime;
import javax.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.springframework.data.annotation.CreatedBy;

/**

 @author victorakinola
 */
@Entity
@Data
@Table(indexes = {
    @Index(name = "AUDITUIDX", columnList = "user"),
    @Index(name = "AUDITSCIDX", columnList = "statusCode"),
    @Index(name = "AUDITDATEIDX", columnList = "createdOn"),
    @Index(name = "AUDITUDATEIDX", columnList = "user, createdOn"),})
public class AdminUserMgmtAuditLog implements Serializable {

    private static final long serialVersionUID = 21567584765900L;
    private static final String SEQUENCE_NAME = "ADMINAUDITLOG_SEQ_GEN";
    private static final String GENERATOR_NAME = "ADMINAUDITLOG_GENRT";

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = GENERATOR_NAME)
    @GenericGenerator(
            name = GENERATOR_NAME,
            strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
            parameters = {
                @Parameter(name = "sequence_name", value = SEQUENCE_NAME),
                @Parameter(name = "initial_value", value = "1"),
                @Parameter(name = "increment_size", value = "50"),
                @Parameter(name = "optimizer", value = "pooled")
            }
    )
    private Long logId;
    @CreatedBy
    private String user;
    @CreationTimestamp
    private LocalDateTime createdOn;
    private String description;
    private Integer statusCode;
    private String sourceAddress;
    private String requestPayLoad;
    //@Column(columnDefinition = "varchar2(1000)")
    @Lob
    @Column(name = "response_payload", length = 1000000)
    private String responsePayload;
}
