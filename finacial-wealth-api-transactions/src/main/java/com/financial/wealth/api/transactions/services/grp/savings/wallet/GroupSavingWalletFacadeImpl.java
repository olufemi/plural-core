/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.financial.wealth.api.transactions.services.grp.savings.wallet;

/**
 *
 * @author olufemioshin
 */
import com.financial.wealth.api.transactions.domain.DeviceDetails;
import com.financial.wealth.api.transactions.domain.FinWealthPaymentTransaction;
import com.financial.wealth.api.transactions.domain.RegWalletInfo;
import com.financial.wealth.api.transactions.models.BaseResponse;
import com.financial.wealth.api.transactions.models.BatchPostingLegRequest;
import com.financial.wealth.api.transactions.models.BatchPostingRequest;
import com.financial.wealth.api.transactions.models.CreditWalletCaller;
import com.financial.wealth.api.transactions.models.DebitWalletCaller;
import com.financial.wealth.api.transactions.models.PushNotificationFireBase;
import com.financial.wealth.api.transactions.repo.DeviceDetailsRepo;
import com.financial.wealth.api.transactions.repo.FinWealthPaymentTransactionRepo;
import com.financial.wealth.api.transactions.repo.RegWalletInfoRepository;
import com.financial.wealth.api.transactions.services.TransactionHistoryClientLocalT;
import com.financial.wealth.api.transactions.services.grp.sav.fulfil.WalletFacade;
import com.financial.wealth.api.transactions.services.notify.MessageCenterService;
import static com.financial.wealth.api.transactions.tranfaar.services.WebhookKeyService.pushNotifyCreditWalletForWalletTransfer;
import com.financial.wealth.api.transactions.utils.GlobalMethods;
import com.financial.wealth.api.transactions.utils.StrongAES;
import com.financial.wealth.api.transactions.utils.UttilityMethods;
import com.google.gson.Gson;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import org.springframework.beans.factory.annotation.Value;

@Service
public class GroupSavingWalletFacadeImpl implements WalletFacade {

    @Value("${fin.wealth.otp.encrypt.key}")
    private String encryptionKey;

    // private final WalletRepository walletRepo;
    private final GroupSavingWalletTxnRepository txnRepo;
    private final RegWalletInfoRepository regWalletInfoRepository;
    private final UttilityMethods utilMeth;
    private final FinWealthPaymentTransactionRepo finWealthPaymentTransactionRepo;
    private final MessageCenterService messageCenterService;
    private final DeviceDetailsRepo deviceDetailsRepo;
    private static final String CCY = "CAD";
    private final TransactionHistoryClientLocalT transactionHistoryClientLocalT;

    public GroupSavingWalletFacadeImpl(
            //WalletRepository walletRepo, 
            GroupSavingWalletTxnRepository txnRepo,
            RegWalletInfoRepository regWalletInfoRepository,
            UttilityMethods utilMeth,
            FinWealthPaymentTransactionRepo finWealthPaymentTransactionRepo,
            MessageCenterService messageCenterService,
            DeviceDetailsRepo deviceDetailsRepo,
          TransactionHistoryClientLocalT transactionHistoryClientLocalT) {
        //this.walletRepo = walletRepo;
        this.txnRepo = txnRepo;
        this.regWalletInfoRepository = regWalletInfoRepository;
        this.utilMeth = utilMeth;
        this.finWealthPaymentTransactionRepo = finWealthPaymentTransactionRepo;
        this.messageCenterService = messageCenterService;
        this.deviceDetailsRepo = deviceDetailsRepo;
        this.transactionHistoryClientLocalT = transactionHistoryClientLocalT;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String debit(String walletId, BigDecimal amount, String idempotencyRef) throws Exception {
        validateInputs(walletId, amount, idempotencyRef);

        // 1) Fast path for idempotency
        Optional<GroupSavingWalletTransaction> existing
                = txnRepo.findByWalletIdAndIdempotencyRefAndType(walletId, idempotencyRef, GroupSavingWalletTxnType.DEBIT);
        if (existing.isPresent()) {
            return resolveIdempotentOutcome(existing.get());
        }

        // 2) Create PENDING transaction (unique key enforces idempotency under concurrency)
        GroupSavingWalletTransaction txn = new GroupSavingWalletTransaction();
        txn.setId(UUID.randomUUID().toString());
        txn.setWalletId(walletId);
        txn.setType(GroupSavingWalletTxnType.DEBIT);
        txn.setAmount(amount);
        txn.setStatus(GroupSavingWalletTxnStatus.PENDING);
        txn.setIdempotencyRef(idempotencyRef);

        try {
            txnRepo.saveAndFlush(txn);
        } catch (DataIntegrityViolationException dup) {
            // Another thread created it concurrently
            GroupSavingWalletTransaction concurrent = txnRepo
                    .findByWalletIdAndIdempotencyRefAndType(walletId, idempotencyRef, GroupSavingWalletTxnType.DEBIT)
                    .orElseThrow(() -> dup); // should never happen
            return resolveIdempotentOutcome(concurrent);
        }

        /*// 3) Lock wallet row and apply business logic
        Wallet wallet = walletRepo.findByIdForUpdate(walletId)
                .orElseThrow(() -> new IllegalArgumentException("Wallet not found: " + walletId));

        if (wallet.getBalance().compareTo(amount) < 0) {
            txn.setStatus(WalletTxnStatus.FAILED);
            txn.setErrorMessage("Insufficient funds");
            txnRepo.save(txn);
            throw new InsufficientFundsException("Insufficient funds");
        }

        wallet.setBalance(wallet.getBalance().subtract(amount));
        walletRepo.save(wallet);*/
        //here call wallet-service
        List<RegWalletInfo> getReg = regWalletInfoRepository.findByWalletIdList(walletId);

        if (getReg.size() <= 0) {
            txn.setStatus(GroupSavingWalletTxnStatus.FAILED);
            txn.setErrorMessage("Insufficient funds");
            txnRepo.save(txn);
            return "Wallet not found: " + walletId;
        }

        String reff = String.valueOf(GlobalMethods.generateTransactionId());

        // TODO: validate amounts, ids, etc., then persist or enqueue
        // 6) Build response
        DebitWalletCaller rqC = new DebitWalletCaller();
        rqC.setAuth("Receiver");
        rqC.setFees("0.00");
        rqC.setFinalCHarges(amount.toString());
        rqC.setNarration("Withdrawal");
        rqC.setPhoneNumber(getReg.get(0).getPhoneNumber());
        rqC.setTransAmount(amount.toString());
        rqC.setTransactionId(reff + "-CUSTOMER_DR");
        System.out.println("Credit Request TO core rqC ::::::::::::::::  %S  " + new Gson().toJson(rqC));

        BatchPostingRequest batchRq = new BatchPostingRequest();
        batchRq.setGroupRef(reff);
        // productCode resolved from Smart-Core auth token in batchPost
        BatchPostingLegRequest customerLeg = new BatchPostingLegRequest();
        customerLeg.setDirection("DEBIT");
        customerLeg.setRequestRef(reff + "-CUSTOMER_DR");
        customerLeg.setUserType("CUSTOMER");
        customerLeg.setAuth("Receiver");
        customerLeg.setFees("0.00");
        customerLeg.setFinalCHarges(amount.toString());
        customerLeg.setNarration("Withdrawal");
        customerLeg.setPhoneNumber(getReg.get(0).getPhoneNumber());
        customerLeg.setTransAmount(amount.toString());
        customerLeg.setTransactionId(reff + "-CUSTOMER_DR");
        BatchPostingLegRequest cadGlLeg = new BatchPostingLegRequest();
        cadGlLeg.setDirection("DEBIT");
        cadGlLeg.setRequestRef(reff + "-CAD_GL_DR");
        cadGlLeg.setUserType("CAD_GL");
        cadGlLeg.setAuth("Receiver");
        cadGlLeg.setFees("0.00");
        cadGlLeg.setFinalCHarges(amount.toString());
        cadGlLeg.setNarration("CAD_Withdrawal");
        cadGlLeg.setPhoneNumber(decryptData(utilMeth.getSETTING_KEY_WALLET_SYSTEM_SYSTEM_GG_CAD()));
        cadGlLeg.setTransAmount(amount.toString());
        cadGlLeg.setTransactionId(reff+"-CAD_GL_DR");
        batchRq.setLegs(Arrays.asList(customerLeg, cadGlLeg));

        BaseResponse debitAcct = utilMeth.batchPost(batchRq, null);

        System.out.println("Debit Response from core debitAcct ::::::::::::::::  %S  " + new Gson().toJson(debitAcct));

        //BaseResponse creditAcct = genLedgerProxy.creditOneTime(rqq);
        if (debitAcct.getStatusCode() == 200) {

            FinWealthPaymentTransaction kTrans2b = new FinWealthPaymentTransaction();
            kTrans2b.setAmmount(amount);
            kTrans2b.setCreatedDate(Instant.now().plusSeconds(1));
            kTrans2b.setFees(new BigDecimal(rqC.getFees()));
            kTrans2b.setPaymentType("Withdrawal from Account");
            kTrans2b.setReceiver(rqC.getPhoneNumber());
            kTrans2b.setSender(rqC.getPhoneNumber());
            kTrans2b.setTransactionId(reff + "-CUSTOMER_CR");
            kTrans2b.setSenderTransactionType("");
            kTrans2b.setReceiverTransactionType("Withdrawal");

            List<RegWalletInfo> getReceiverName = regWalletInfoRepository.findByPhoneNumberData(rqC.getPhoneNumber());

            kTrans2b.setWalletNo(rqC.getPhoneNumber());
            kTrans2b.setReceiverName(getReceiverName.get(0).getFullName());
            kTrans2b.setSenderName(getReceiverName.get(0).getFullName());
            kTrans2b.setSentAmount(amount.toString());
            kTrans2b.setTheNarration("Group savings debit.");
            kTrans2b.setCurrencyCode(CCY);
            
            transactionHistoryClientLocalT.publishFromTxn(kTrans2b);

            //finWealthPaymentTransactionRepo.save(kTrans2b);

            PushNotificationFireBase puFireSender = new PushNotificationFireBase();
            puFireSender.setBody(pushNotifyDebitWalletForWalletTransferGroupSaving(amount, "", "" + " " + ""));
            List<DeviceDetails> getDepuFireSender = deviceDetailsRepo.findAllByWalletId(walletId);

            puFireSender.setTitle("Withdrawal-From-wallet");
            if (getDepuFireSender.size() > 0) {
                String getToken = getDepuFireSender.get(0).getToken() == null ? "" : getDepuFireSender.get(0).getToken();

                if (getToken != "") {
                    puFireSender.setDeviceToken(getDepuFireSender.get(0).getToken());
                    Map<String, String> data = new HashMap<String, String>();
                    data.put("type", "ALERT");            // sample custom data
                    if (puFireSender.getData() != null) {
                        data.putAll(puFireSender.getData());
                    }

                    messageCenterService.createAndPushToUser(getReceiverName.get(0).getWalletId(), puFireSender.getTitle(),
                            puFireSender.getBody(),
                            data, null, "");

                }
            }

        }
        txn.setWalletPocRef(reff);
        txn.setStatus(GroupSavingWalletTxnStatus.SUCCESS);
        txnRepo.save(txn);

        return txn.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String credit(String walletId, BigDecimal amount, String idempotencyRef) throws Exception {
        validateInputs(walletId, amount, idempotencyRef);

        Optional<GroupSavingWalletTransaction> existing
                = txnRepo.findByWalletIdAndIdempotencyRefAndType(walletId, idempotencyRef, GroupSavingWalletTxnType.CREDIT);
        if (existing.isPresent()) {
            return resolveIdempotentOutcome(existing.get());
        }

        String reff = String.valueOf(GlobalMethods.generateTransactionId());

        GroupSavingWalletTransaction txn = new GroupSavingWalletTransaction();
        txn.setId(UUID.randomUUID().toString());
        txn.setWalletId(walletId);
        txn.setType(GroupSavingWalletTxnType.CREDIT);
        txn.setAmount(amount);
        txn.setStatus(GroupSavingWalletTxnStatus.PENDING);
        txn.setIdempotencyRef(idempotencyRef);

        try {
            txnRepo.saveAndFlush(txn);
        } catch (DataIntegrityViolationException dup) {
            GroupSavingWalletTransaction concurrent = txnRepo
                    .findByWalletIdAndIdempotencyRefAndType(walletId, idempotencyRef, GroupSavingWalletTxnType.CREDIT)
                    .orElseThrow(() -> dup);
            return resolveIdempotentOutcome(concurrent);
        }

        // TODO: validate amounts, ids, etc., then persist or enqueue
        // 6) Build response
        List<RegWalletInfo> getReg = regWalletInfoRepository.findByWalletIdList(walletId);

        if (getReg.size() <= 0) {
            txn.setStatus(GroupSavingWalletTxnStatus.FAILED);
            txn.setErrorMessage("Insufficient funds");
            txnRepo.save(txn);
            return "Wallet not found: " + walletId;
        }
        CreditWalletCaller rqC = new CreditWalletCaller();
        rqC.setAuth("Receiver");
        rqC.setFees("0.00");
        rqC.setFinalCHarges(amount.toString());
        rqC.setNarration("Deposit");
        rqC.setPhoneNumber(getReg.get(0).getPhoneNumber());
        rqC.setTransAmount(amount.toString());
        rqC.setTransactionId(reff + (amount.signum() >= 0 ? "-CUSTOMER" : "-CUSTOMER"));
        System.out.println("Credit Request TO core rqC ::::::::::::::::  %S  " + new Gson().toJson(rqC));

        BatchPostingRequest batchRq = new BatchPostingRequest();
        batchRq.setGroupRef(reff);
        BatchPostingLegRequest customerLeg = new BatchPostingLegRequest();
        customerLeg.setDirection("CREDIT");
        customerLeg.setRequestRef(reff + "-CUSTOMER_CR");
        customerLeg.setUserType("CUSTOMER");
        customerLeg.setAuth("Receiver");
        customerLeg.setFees("0.00");
        customerLeg.setFinalCHarges(amount.toString());
        customerLeg.setNarration("Deposit");
        customerLeg.setPhoneNumber(getReg.get(0).getPhoneNumber());
        customerLeg.setTransAmount(amount.toString());
        customerLeg.setTransactionId(reff + "-CUSTOMER_CR");
        BatchPostingLegRequest cadGlLeg = new BatchPostingLegRequest();
        cadGlLeg.setDirection("CREDIT");
        cadGlLeg.setUserType("CAD_GL");
        cadGlLeg.setAuth("Receiver");
        cadGlLeg.setFees("0.00");
        cadGlLeg.setFinalCHarges(amount.toString());
        cadGlLeg.setNarration("CAD_Deposit");
        cadGlLeg.setPhoneNumber(decryptData(utilMeth.getSETTING_KEY_WALLET_SYSTEM_SYSTEM_GG_CAD()));
        cadGlLeg.setTransAmount(amount.toString());
        cadGlLeg.setTransactionId(reff+"-CAD_GL_CR");
        batchRq.setLegs(Arrays.asList(customerLeg, cadGlLeg));

        BaseResponse creditAcct = utilMeth.batchPost(batchRq, null);

        System.out.println("Credit Response from core creditAcct ::::::::::::::::  %S  " + new Gson().toJson(creditAcct));

        //BaseResponse creditAcct = genLedgerProxy.creditOneTime(rqq);
        if (creditAcct.getStatusCode() == 200) {

            FinWealthPaymentTransaction kTrans2b = new FinWealthPaymentTransaction();
            kTrans2b.setAmmount(amount);
            kTrans2b.setCreatedDate(Instant.now().plusSeconds(1));
            kTrans2b.setFees(new BigDecimal(rqC.getFees()));
            kTrans2b.setPaymentType("Deposit to Account");
            kTrans2b.setReceiver(rqC.getPhoneNumber());
            kTrans2b.setSender(rqC.getPhoneNumber());
            kTrans2b.setTransactionId(reff + "-CUSTOMER_CR");
            kTrans2b.setSenderTransactionType("");
            kTrans2b.setReceiverTransactionType("Deposit");

            List<RegWalletInfo> getReceiverName = regWalletInfoRepository.findByPhoneNumberData(rqC.getPhoneNumber());

            kTrans2b.setWalletNo(rqC.getPhoneNumber());
            kTrans2b.setReceiverName(getReceiverName.get(0).getFullName());
            kTrans2b.setSenderName(getReceiverName.get(0).getFullName());
            kTrans2b.setSentAmount(amount.toString());
            kTrans2b.setTheNarration("Group savings settlement.");
            kTrans2b.setCurrencyCode(CCY);
            
            transactionHistoryClientLocalT.publishFromTxn(kTrans2b);

            //finWealthPaymentTransactionRepo.save(kTrans2b);

            PushNotificationFireBase puFireSender = new PushNotificationFireBase();
            puFireSender.setBody(pushNotifyCreditWalletForWalletTransferGroupSavings(amount,
                    "", "" + " " + ""
            ));
            List<DeviceDetails> getDepuFireSender = deviceDetailsRepo.findAllByWalletId(walletId);

            puFireSender.setTitle("Deposit-To-wallet");
            if (getDepuFireSender.size() > 0) {
                String getToken = getDepuFireSender.get(0).getToken() == null ? "" : getDepuFireSender.get(0).getToken();

                if (getToken != "") {
                    puFireSender.setDeviceToken(getDepuFireSender.get(0).getToken());
                    Map<String, String> data = new HashMap<String, String>();
                    data.put("type", "ALERT");            // sample custom data
                    if (puFireSender.getData() != null) {
                        data.putAll(puFireSender.getData());
                    }

                    messageCenterService.createAndPushToUser(getReceiverName.get(0).getWalletId(), puFireSender.getTitle(),
                            puFireSender.getBody(),
                            data, null, "");

                }
            }

        }

        /*Wallet wallet = walletRepo.findByIdForUpdate(walletId)
                .orElseThrow(() -> new IllegalArgumentException("Wallet not found: " + walletId));

        wallet.setBalance(wallet.getBalance().add(amount));
        walletRepo.save(wallet);*/
        //here call wallet-service
        txn.setStatus(GroupSavingWalletTxnStatus.SUCCESS);
        txnRepo.save(txn);

        return txn.getId();
    }

    public static String pushNotifyCreditWalletForWalletTransferGroupSavings(BigDecimal amount, String recName, String senderName) {
        String sMSMessage = "Dear " + "Customer" + ", "
                + " your Wallet has been credited with " + "N" + amount + ", for your Group Saving"
                + " Thanks for using Plural.";
        return sMSMessage;
    }

    public static String pushNotifyDebitWalletForWalletTransferGroupSaving(BigDecimal amount, String recName, String senderName) {
        String sMSMessage = "Dear " + "Customer" + ", "
                + " your Wallet has been debited with " + "N" + amount + ", for your Group Saving"
                + " Thanks for using Plural.";
        return sMSMessage;
    }

    private String decryptData(String data) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {

        String decryptData = StrongAES.decrypt(data, encryptionKey);

        // log.info("decryptData ::::: {} ", decryptData);
        return decryptData;

    }

    private String resolveIdempotentOutcome(GroupSavingWalletTransaction tx) throws Exception {
        switch (tx.getStatus()) {
            case SUCCESS:
                return tx.getId();
            case FAILED:
                throw new IllegalStateException(
                        tx.getErrorMessage() != null ? tx.getErrorMessage() : "Previous attempt failed");
            case PENDING:
                throw new GroupSavingsPendingTransactionException("Transaction is still processing, retry later.");
            default:
                throw new IllegalStateException("Unknown status: " + tx.getStatus());
        }
    }

    private void validateInputs(String walletId, BigDecimal amount, String idempotencyRef) {
        if (walletId == null || walletId.trim().isEmpty()) {
            throw new IllegalArgumentException("walletId is required");
        }
        if (idempotencyRef == null || idempotencyRef.trim().isEmpty()) {
            throw new IllegalArgumentException("idempotencyRef is required");
        }
        if (amount == null || amount.signum() <= 0) {
            throw new IllegalArgumentException("amount must be > 0");
        }
    }
}
