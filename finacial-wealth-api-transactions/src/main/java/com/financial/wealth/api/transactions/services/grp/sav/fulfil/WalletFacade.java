/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.financial.wealth.api.transactions.services.grp.sav.fulfil;

/**
 *
 * @author olufemioshin
 */
import java.math.BigDecimal;

public interface WalletFacade {
    String debit(String walletId, BigDecimal amount, String idempotencyRef) throws Exception;
    String credit(String walletId, BigDecimal amount, String idempotencyRef) throws Exception;
}
