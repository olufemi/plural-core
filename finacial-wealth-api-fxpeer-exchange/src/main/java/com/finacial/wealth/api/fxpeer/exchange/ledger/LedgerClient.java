/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.finacial.wealth.api.fxpeer.exchange.ledger;

/**
 *
 * @author olufemioshin
 */
import com.finacial.wealth.api.fxpeer.exchange.ledger.model.CreditRq;
import com.finacial.wealth.api.fxpeer.exchange.ledger.model.DebitRq;
import com.finacial.wealth.api.fxpeer.exchange.ledger.model.TxnResult;
import com.finacial.wealth.api.fxpeer.exchange.ledger.model.WalletInfo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;


@FeignClient(name = "ledger", url = "${ledger.base-url}")
public interface LedgerClient {


@PostMapping("/wallets/{userId}/{ccy}/ensure")
void ensureWallet(@PathVariable("userId") long userId, @PathVariable("ccy") String ccy);


@GetMapping("/wallets/{userId}")
WalletInfo getWallet(@PathVariable("userId") long userId, @RequestParam("ccy") String ccy);


@PostMapping("/ledger/debit")
TxnResult debit(@RequestHeader("Idempotency-Key") String idemKey, @RequestBody DebitRq rq);


@PostMapping("/ledger/credit")
TxnResult credit(@RequestHeader("Idempotency-Key") String idemKey, @RequestBody CreditRq rq);
}
