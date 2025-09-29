/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.finacial.wealth.api.fxpeer.exchange.notify;

/**
 *
 * @author olufemioshin
 */
import org.xhtmlrenderer.pdf.ITextRenderer;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;

@Service
public class PdfService {

    public byte[] fromHtml(String html) {
        try {
            ITextRenderer renderer = new ITextRenderer();
            renderer.setDocumentFromString(html);
            renderer.layout();
            try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                renderer.createPDF(baos);
                return baos.toByteArray();
            }
        } catch (Exception e) {
            throw new RuntimeException("PDF generation failed", e);
        }
    }
}
