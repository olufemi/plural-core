/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.finacial.wealth.api.fxpeer.exchange.receipt;

/**
 *
 * @author olufemioshin
 */
import com.finacial.wealth.api.fxpeer.exchange.notify.EmailService;
import com.finacial.wealth.api.fxpeer.exchange.notify.PdfService;
import com.finacial.wealth.api.fxpeer.exchange.order.Order;
import com.finacial.wealth.api.fxpeer.exchange.order.OrderRepository;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;


import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;

@Service
public class ReceiptDeliveryService {

    private final ReceiptService receipts;
    private final PdfService pdfs;
    private final EmailService email;
    private final OrderRepository orders;

    @Value("${receipt.pdf.dir:/tmp/receipts}")
    private String pdfDir;

    public ReceiptDeliveryService(ReceiptService receipts, PdfService pdfs, EmailService email, OrderRepository orders) {
        this.receipts = receipts;
        this.pdfs = pdfs;
        this.email = email;
        this.orders = orders;
    }

    /**
     * Ensures a PDF exists for the receipt; returns the absolute file path.
     */
    public String ensurePdf(Long orderId) {
        try {
            Receipt r = receipts.generateAndStore(orderId);
            File dir = new File(pdfDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            File pdf = new File(dir, "order-" + orderId + ".pdf");
            if (!pdf.exists()) {
                byte[] bytes = pdfs.fromHtml(r.getHtml());
                try (FileOutputStream fos = new FileOutputStream(pdf)) {
                    fos.write(bytes);
                }
            }
            r.setPdfUrl(pdf.getAbsolutePath());
            return pdf.getAbsolutePath();
        } catch (Exception e) {
            throw new RuntimeException("ensurePdf failed", e);
        }
    }

    /**
     * Sends emails with HTML body and PDF attachment. Provide resolved emails
     * from your user profile service.
     */
    public void emailBoth(Long orderId, String buyerEmail, String sellerEmail) {
        Order o = orders.findById(orderId).orElseThrow();
        Receipt r = receipts.generateAndStore(orderId);
        byte[] pdf = pdfs.fromHtml(r.getHtml());
        String subject = "FXPeer Receipt #" + orderId;
        String filename = "FXPeer-Receipt-" + orderId + ".pdf";
        if (buyerEmail != null && !buyerEmail.isBlank()) {
            email.sendWithPdf(buyerEmail, subject, r.getHtml(), pdf, filename);
        }
        if (sellerEmail != null && !sellerEmail.isBlank()) {
            email.sendWithPdf(sellerEmail, subject, r.getHtml(), pdf, filename);
        }
    }

    public byte[] readPdfBytes(Long orderId) {
        try {
            String path = ensurePdf(orderId);
            return Files.readAllBytes(new File(path).toPath());
        } catch (Exception e) {
            throw new RuntimeException("readPdfBytes failed", e);
        }
    }
}
