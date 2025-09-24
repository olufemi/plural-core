/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.finacial.wealth.api.fxpeer.exchange.receipt;

/**
 *
 * @author olufemioshin
 */
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "receipts")
public class Receipt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private Long orderId;
    @Column(nullable = false)
    private Long buyerUserId;
    @Column(nullable = false)
    private Long sellerUserId;

    @Lob
    @Column(columnDefinition = "MEDIUMTEXT")
    private String html; // stored HTML

    private String pdfUrl; // optional link to PDF in object storage
    private boolean emailed;

    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    public Long getId() {
        return id;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public Long getBuyerUserId() {
        return buyerUserId;
    }

    public void setBuyerUserId(Long buyerUserId) {
        this.buyerUserId = buyerUserId;
    }

    public Long getSellerUserId() {
        return sellerUserId;
    }

    public void setSellerUserId(Long sellerUserId) {
        this.sellerUserId = sellerUserId;
    }

    public String getHtml() {
        return html;
    }

    public void setHtml(String html) {
        this.html = html;
    }

    public String getPdfUrl() {
        return pdfUrl;
    }

    public void setPdfUrl(String pdfUrl) {
        this.pdfUrl = pdfUrl;
    }

    public boolean isEmailed() {
        return emailed;
    }

    public void setEmailed(boolean emailed) {
        this.emailed = emailed;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
