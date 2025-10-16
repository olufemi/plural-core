/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.finacial.wealth.api.fxpeer.exchange.offer;

/**
 *
 * @author olufemioshin
 */
import java.time.Instant;

public final class OfferMapper {

    private OfferMapper() {
    }

    public static OfferListItemDto toListItem(Offer o) {
        Instant expiry = o.getExpiryAt();
        boolean expired = (expiry != null && Instant.now().isAfter(expiry));

        return new OfferListItemDto(
                o.getId(), // assuming AuditedBase has getId()
                o.getCurrencySell().name(),
                o.getCurrencyReceive().name(),
                o.getRate(),
                o.getQtyTotal(),
                o.getQtyAvailable(),
                o.getMinAmount(),
                o.getMaxAmount(),
                expiry,
                expired,
                o.getStatus().name(),
                o.isShowInTopDeals(),
                o.getPoweredBy(),
                o.getCorrelationId()
        );
    }
}
