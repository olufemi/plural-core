package com.finacial.wealth.api.fxpeer.exchange.inter.airtime.security;

import com.finacial.wealth.api.fxpeer.exchange.feign.TransactionServiceProxies;
import com.finacial.wealth.api.fxpeer.exchange.model.ApiResponseModel;
import com.finacial.wealth.api.fxpeer.exchange.model.BaseResponse;
import com.finacial.wealth.api.fxpeer.exchange.model.CreditWalletCaller;
import com.finacial.wealth.api.fxpeer.exchange.model.DebitWalletCaller;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.beans.factory.annotation.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class AirtimeRollbackService {

    private static final Logger log = LoggerFactory.getLogger(AirtimeRollbackService.class);

    private static final String STATUS_PENDING = "PENDING";
    private static final String STATUS_FAILED = "FAILED";
    private static final String STATUS_SUCCESS = "SUCCESS";
    private static final String ACTION_DEBIT = "DEBIT";
    private static final String ACTION_CREDIT = "CREDIT";
    private static final String LEG_REVERSE_GL_CREDIT = "reverseGLCredit";
    private static final String LEG_REVERSE_SELLER_CREDIT = "reverseSellerCredit";
    private static final String LEG_REVERSE_GL_DEBIT = "reverseGLDebit";
    private static final String LEG_REVERSE_BUYER_DEBIT = "reverseBuyerDebit";
    private static final List<String> ACTIVE_STATUSES = Arrays.asList(STATUS_PENDING, STATUS_FAILED, STATUS_SUCCESS);
    private static final List<String> RETRYABLE_STATUSES = Arrays.asList(STATUS_PENDING, STATUS_FAILED);

    private final AirtimeRollbackLogRepository rollbackLogRepository;
    private final TransactionServiceProxies transactionServiceProxies;

    @Value("${fx.airtime.rollback.retry.authorization:}")
    private String schedulerAuthorization;

    public AirtimeRollbackService(AirtimeRollbackLogRepository rollbackLogRepository,
            TransactionServiceProxies transactionServiceProxies) {
        this.rollbackLogRepository = rollbackLogRepository;
        this.transactionServiceProxies = transactionServiceProxies;
    }

    public Map<String, Object> executeRollback(PreDebitResult pre, ProcessTrnsactionReq rq, String auth, String providerError) {
        Map<String, Object> response = new HashMap<>();
        response.put("rollbackStarted", true);
        List<AirtimeRollbackLog> logs = upsertRollbackLegs(pre, rq, providerError);
        for (AirtimeRollbackLog logItem : logs) {
            BaseResponse res = executeRollbackLeg(logItem, auth);
            if (res != null) {
                response.put(logItem.getLegKey(), res.getStatusCode());
                if (res.getStatusCode() != 200 && res.getDescription() != null) {
                    response.put(logItem.getLegKey() + "_message", res.getDescription());
                }
            } else if (logItem.getLastError() != null) {
                response.put(logItem.getLegKey() + "_error", logItem.getLastError());
            }
        }
        response.put("processId", pre.getProcessId());
        response.put("status", deriveOverallStatus(rollbackLogRepository.findByProcessIdOrderByIdAsc(pre.getProcessId())));
        response.put("rollbackCompleted", true);
        return response;
    }

    @Scheduled(cron = "${fx.airtime.rollback.retry.cron:0 */2 * * * *}", zone = "${fx.timezone:Africa/Lagos}")
    @SchedulerLock(name = "AirtimeRollbackService.retryPendingRollbacks", lockAtMostFor = "10m", lockAtLeastFor = "30s")
    public void retryPendingRollbacks() {
        List<AirtimeRollbackLog> logs = rollbackLogRepository.findByStatusIn(RETRYABLE_STATUSES);
        for (AirtimeRollbackLog logItem : logs) {
            try {
                executeRollbackLeg(logItem, schedulerAuthorization == null ? "" : schedulerAuthorization);
            } catch (Exception ex) {
                log.error("Airtime rollback retry failed for processId={} legKey={}", logItem.getProcessId(), logItem.getLegKey(), ex);
            }
        }
    }

    public ApiResponseModel getSummary() {
        List<Map<String, Object>> grouped = buildGroupedCases(null);
        long success = grouped.stream().filter(m -> STATUS_SUCCESS.equals(m.get("status"))).count();
        long pending = grouped.stream().filter(m -> STATUS_PENDING.equals(m.get("status"))).count();
        long failed = grouped.stream().filter(m -> STATUS_FAILED.equals(m.get("status"))).count();

        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("totalCount", grouped.size());
        summary.put("successfulCount", success);
        summary.put("pendingCount", pending);
        summary.put("failedCount", failed);

        ApiResponseModel response = new ApiResponseModel();
        response.setStatusCode(200);
        response.setDescription("Airtime reversal summary fetched successfully");
        response.setData(summary);
        return response;
    }

    public ApiResponseModel getCases(String status) {
        List<Map<String, Object>> grouped = buildGroupedCases(status);
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("count", grouped.size());
        payload.put("items", grouped);

        ApiResponseModel response = new ApiResponseModel();
        response.setStatusCode(200);
        response.setDescription("Airtime reversal cases fetched successfully");
        response.setData(payload);
        return response;
    }

    private List<Map<String, Object>> buildGroupedCases(String statusFilter) {
        Collection<String> statusesToLoad = ACTIVE_STATUSES;
        if (statusFilter != null && !statusFilter.isBlank()) {
            statusesToLoad = Collections.singletonList(statusFilter.toUpperCase(Locale.ROOT));
        }

        List<AirtimeRollbackLog> logs = rollbackLogRepository.findByStatusIn(statusesToLoad);
        Map<String, List<AirtimeRollbackLog>> grouped = logs.stream()
                .collect(Collectors.groupingBy(AirtimeRollbackLog::getProcessId, LinkedHashMap::new, Collectors.toList()));

        List<Map<String, Object>> items = new ArrayList<>();
        for (Map.Entry<String, List<AirtimeRollbackLog>> entry : grouped.entrySet()) {
            String overallStatus = deriveOverallStatus(entry.getValue());
            if (statusFilter != null && !statusFilter.isBlank() && !overallStatus.equalsIgnoreCase(statusFilter)) {
                continue;
            }
            items.add(toCaseRecord(entry.getKey(), overallStatus, entry.getValue()));
        }

        items.sort((a, b) -> {
            Instant ai = (Instant) a.get("requestedAt");
            Instant bi = (Instant) b.get("requestedAt");
            if (ai == null && bi == null) {
                return 0;
            }
            if (ai == null) {
                return 1;
            }
            if (bi == null) {
                return -1;
            }
            return bi.compareTo(ai);
        });
        return items;
    }

    private Map<String, Object> toCaseRecord(String processId, String status, List<AirtimeRollbackLog> logs) {
        Map<String, Object> item = new LinkedHashMap<>();
        AirtimeRollbackLog first = logs.get(0);
        item.put("processId", processId);
        item.put("status", status);
        item.put("serviceType", first.getServiceType());
        item.put("operator", first.getOperatorCode());
        item.put("product", first.getProductCode());
        item.put("providerError", logs.stream().map(AirtimeRollbackLog::getProviderError).filter(Objects::nonNull).filter(s -> !s.isBlank()).findFirst().orElse(null));
        item.put("requestedAt", logs.stream().map(AirtimeRollbackLog::getRequestedAt).filter(Objects::nonNull).min(Instant::compareTo).orElse(null));
        item.put("completedAt", logs.stream().map(AirtimeRollbackLog::getCompletedAt).filter(Objects::nonNull).max(Instant::compareTo).orElse(null));
        item.put("retryCount", logs.stream().mapToInt(AirtimeRollbackLog::getRetryCount).max().orElse(0));
        item.put("lastError", logs.stream().map(AirtimeRollbackLog::getLastError).filter(Objects::nonNull).filter(s -> !s.isBlank()).findFirst().orElse(null));
        item.put("legs", logs.stream().map(this::toLegRecord).collect(Collectors.toList()));
        return item;
    }

    private Map<String, Object> toLegRecord(AirtimeRollbackLog logItem) {
        Map<String, Object> leg = new LinkedHashMap<>();
        leg.put("legKey", logItem.getLegKey());
        leg.put("transactionId", logItem.getRollbackTransactionId());
        leg.put("status", logItem.getStatus());
        leg.put("amount", logItem.getAmount());
        leg.put("retryCount", logItem.getRetryCount());
        leg.put("lastResponseCode", logItem.getLastResponseCode());
        leg.put("lastError", logItem.getLastError());
        leg.put("requestedAt", logItem.getRequestedAt());
        leg.put("completedAt", logItem.getCompletedAt());
        return leg;
    }

    private String deriveOverallStatus(List<AirtimeRollbackLog> logs) {
        LinkedHashSet<String> statuses = logs.stream()
                .map(AirtimeRollbackLog::getStatus)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        if (statuses.contains(STATUS_PENDING)) {
            return STATUS_PENDING;
        }
        if (statuses.contains(STATUS_FAILED)) {
            return STATUS_FAILED;
        }
        if (!statuses.isEmpty() && Collections.singleton(STATUS_SUCCESS).equals(statuses)) {
            return STATUS_SUCCESS;
        }
        return STATUS_PENDING;
    }

    private List<AirtimeRollbackLog> upsertRollbackLegs(PreDebitResult pre, ProcessTrnsactionReq rq, String providerError) {
        List<AirtimeRollbackLog> logs = new ArrayList<>();
        String serviceType = resolveServiceType(rq);

        if (pre.isLegGLCredited()) {
            logs.add(upsertLog(pre.getProcessId(), LEG_REVERSE_GL_CREDIT, ACTION_DEBIT, pre.getGglCode() + "_GL",
                    pre.getGglCode(), pre.getProcessId() + "-RB-GLCREDIT", pre.getGlAccountDecrypted(), pre.getReceiveAmount(),
                    pre.getReceiveAmount(), pre.getGglCode() + "_Deposit_RB", serviceType, rq, providerError));
        }
        if (pre.isLegSellerCredited()) {
            logs.add(upsertLog(pre.getProcessId(), LEG_REVERSE_SELLER_CREDIT, ACTION_DEBIT, "CUSTOMER",
                    "Seller", pre.getProcessId() + "-RB-SELLERCREDIT", pre.getSellerAccountNumber(), pre.getReceiveAmount(),
                    pre.getReceiveAmount(), "Seller_Deposit_RB", serviceType, rq, providerError));
        }
        if (pre.isLegGLDebited()) {
            logs.add(upsertLog(pre.getProcessId(), LEG_REVERSE_GL_DEBIT, ACTION_CREDIT, pre.getGglCode(),
                    pre.getGglCode(), pre.getProcessId() + "-RB-GLDEBIT", pre.getGlAccountDecrypted(), pre.getReceiveAmount(),
                    pre.getReceiveAmount(), pre.getGglCode() + "_Debit_RB", serviceType, rq, providerError));
        }
        if (pre.isLegBuyerDebited()) {
            logs.add(upsertLog(pre.getProcessId(), LEG_REVERSE_BUYER_DEBIT, ACTION_CREDIT, "CUSTOMER",
                    "Airtime_Buyer", pre.getProcessId() + "-RB-BUYERDEBIT", pre.getBuyerAccountNumber(), pre.getFinCharges(),
                    pre.getFinCharges(), "Buyer_Withdrawal_RB", serviceType, rq, providerError));
        }

        return logs;
    }

    private AirtimeRollbackLog upsertLog(String processId, String legKey, String actionType, String userType,
            String authValue, String rollbackTransactionId, String accountNumber, BigDecimal amount,
            BigDecimal finalCharges, String narration, String serviceType, ProcessTrnsactionReq rq, String providerError) {
        AirtimeRollbackLog logItem = rollbackLogRepository.findFirstByProcessIdAndLegKey(processId, legKey);
        Instant now = Instant.now();
        if (logItem == null) {
            logItem = new AirtimeRollbackLog();
            logItem.setProcessId(processId);
            logItem.setLegKey(legKey);
            logItem.setCreatedDate(now);
            logItem.setRequestedAt(now);
        }

        logItem.setActionType(actionType);
        logItem.setUserType(userType);
        logItem.setAuthValue(authValue);
        logItem.setRollbackTransactionId(rollbackTransactionId);
        logItem.setAccountNumber(accountNumber);
        logItem.setAmount(amount == null ? BigDecimal.ZERO : amount);
        logItem.setFinalCharges(finalCharges == null ? BigDecimal.ZERO : finalCharges);
        logItem.setNarration(narration);
        logItem.setServiceType(serviceType);
        logItem.setOperatorCode(rq == null ? null : rq.getOperator());
        logItem.setProductCode(rq == null ? null : rq.getProduct());
        logItem.setProviderError(providerError);
        if (!STATUS_SUCCESS.equals(logItem.getStatus())) {
            logItem.setStatus(STATUS_PENDING);
            logItem.setCompletedAt(null);
        }
        logItem.setLastModifiedDate(now);
        return rollbackLogRepository.save(logItem);
    }

    private BaseResponse executeRollbackLeg(AirtimeRollbackLog logItem, String auth) {
        BaseResponse response = null;
        try {
            if (ACTION_DEBIT.equals(logItem.getActionType())) {
                DebitWalletCaller rq = new DebitWalletCaller();
                rq.setAuth(logItem.getAuthValue());
                rq.setFees("0.00");
                rq.setFinalCHarges(toPlain(logItem.getFinalCharges()));
                rq.setNarration(logItem.getNarration());
                rq.setPhoneNumber(logItem.getAccountNumber());
                rq.setTransAmount(toPlain(logItem.getAmount()));
                rq.setTransactionId(logItem.getRollbackTransactionId());
                response = transactionServiceProxies.debitCustomerWithType(rq, logItem.getUserType(), auth);
            } else {
                CreditWalletCaller rq = new CreditWalletCaller();
                rq.setAuth(logItem.getAuthValue());
                rq.setFees("0.00");
                rq.setFinalCHarges(toPlain(logItem.getFinalCharges()));
                rq.setNarration(logItem.getNarration());
                rq.setPhoneNumber(logItem.getAccountNumber());
                rq.setTransAmount(toPlain(logItem.getAmount()));
                rq.setTransactionId(logItem.getRollbackTransactionId());
                response = transactionServiceProxies.creditCustomerWithType(rq, logItem.getUserType(), auth);
            }

            applyResult(logItem, response);
            return response;
        } catch (Exception ex) {
            logItem.setRetryCount(logItem.getRetryCount() + 1);
            logItem.setStatus(STATUS_FAILED);
            logItem.setLastError(ex.getMessage());
            logItem.setLastModifiedDate(Instant.now());
            rollbackLogRepository.save(logItem);
            log.error("Airtime rollback leg execution failed for processId={} legKey={}", logItem.getProcessId(), logItem.getLegKey(), ex);
            return response;
        }
    }

    private void applyResult(AirtimeRollbackLog logItem, BaseResponse response) {
        Instant now = Instant.now();
        if (response != null && response.getStatusCode() == 200) {
            logItem.setStatus(STATUS_SUCCESS);
            logItem.setCompletedAt(now);
            logItem.setLastResponseCode(response.getStatusCode());
            logItem.setLastError(null);
        } else {
            logItem.setRetryCount(logItem.getRetryCount() + 1);
            logItem.setStatus(STATUS_FAILED);
            logItem.setLastResponseCode(response == null ? null : response.getStatusCode());
            logItem.setLastError(response == null ? "Rollback returned null response" : response.getDescription());
        }
        logItem.setLastModifiedDate(now);
        rollbackLogRepository.save(logItem);
    }

    private String resolveServiceType(ProcessTrnsactionReq rq) {
        if (rq == null) {
            return "SOCHITEL_FULFILMENT";
        }
        if (rq.getProduct() != null && !rq.getProduct().isBlank()) {
            return rq.getProduct();
        }
        if (rq.getOperator() != null && !rq.getOperator().isBlank()) {
            return rq.getOperator();
        }
        return "SOCHITEL_FULFILMENT";
    }

    private String toPlain(BigDecimal amount) {
        return amount == null ? "0.00" : amount.toPlainString();
    }
}
