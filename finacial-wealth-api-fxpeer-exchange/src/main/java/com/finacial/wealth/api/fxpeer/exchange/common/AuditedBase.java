/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.finacial.wealth.api.fxpeer.exchange.common;

/**
 *
 * @author olufemioshin
 */
import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Version;
import java.time.Instant;


@MappedSuperclass
public abstract class AuditedBase {
@Id
@GeneratedValue(strategy = GenerationType.IDENTITY)
private Long id;


@Version
private long version;


@Column(nullable = false, updatable = false)
private Instant createdAt = Instant.now();


@Column(nullable = false)
private Instant updatedAt = Instant.now();


public Long getId() { return id; }
public long getVersion() { return version; }
public Instant getCreatedAt() { return createdAt; }
public Instant getUpdatedAt() { return updatedAt; }
public void setUpdatedNow() { this.updatedAt = Instant.now(); }
}
