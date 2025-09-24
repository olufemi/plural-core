/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.finacial.wealth.api.fxpeer.exchange.receipt;

/**
 *
 * @author olufemioshin
 */
import com.finacial.wealth.api.fxpeer.exchange.order.Order;
import com.finacial.wealth.api.fxpeer.exchange.order.OrderRepository;
import java.time.format.DateTimeFormatter;
import org.springframework.stereotype.Service;

@Service
public class ReceiptService {

    private final OrderRepository orders;
    private final ReceiptRepository receipts;

    public ReceiptService(OrderRepository orders, ReceiptRepository receipts) {
        this.orders = orders;
        this.receipts = receipts;
    }

    /**
     * Generate HTML receipt for an order and store it; returns the Receipt
     * entity.
     */
    public Receipt generateAndStore(Long orderId) {
        return receipts.findByOrderId(orderId).orElseGet(() -> {
            Order o = orders.findById(orderId).orElseThrow();
            String html = buildHtml(o);
            Receipt r = new Receipt();
            r.setOrderId(o.getId());
            r.setBuyerUserId(o.getBuyerUserId());
            r.setSellerUserId(o.getSellerUserId());
            r.setHtml(html);
// TODO: generate PDF → setPdfUrl(); send email → setEmailed(true)
            return receipts.save(r);
        });
    }

    public String getBuyerHtml(Long orderId) {
        return receipts.findByOrderId(orderId).orElseGet(() -> generateAndStore(orderId)).getHtml();
    }

    public String getSellerHtml(Long orderId) {
        return receipts.findByOrderId(orderId).orElseGet(() -> generateAndStore(orderId)).getHtml();
    }

    private String buildHtml(Order o) {
        var fmt = DateTimeFormatter.ISO_INSTANT;
        return """
<html><head><meta charset='utf-8'><title>FXPeer Receipt</title>
<style>body{font-family:Arial,Helvetica,sans-serif;color:#111} .kv{margin:4px 0}</style>
</head><body>
<h2>FXPeer Receipt</h2>
<div class='kv'><b>Order ID:</b> %d</div>
<div class='kv'><b>Seller ID:</b> %d</div>
<div class='kv'><b>Buyer ID:</b> %d</div>
<div class='kv'><b>Sell:</b> %s %s</div>
<div class='kv'><b>Receive:</b> %s %s</div>
<div class='kv'><b>Rate:</b> %s</div>
<div class='kv'><b>Fees (buyer/seller):</b> %s / %s</div>
<div class='kv'><b>Status:</b> %s</div>
<div class='kv'><b>Created:</b> %s</div>
</body></html>
""".formatted(
                o.getId(), o.getSellerUserId(), o.getBuyerUserId(),
                o.getCurrencySell(), o.getSellAmount(),
                o.getCurrencyReceive(), o.getReceiveAmount(),
                o.getRate(), o.getFeesBuyer(), o.getFeesSeller(), o.getStatus(),
                fmt.format(o.getCreatedAt())
        );
    }
}
