package com.finacial.wealth.backoffice.dashboard;

import com.finacial.wealth.backoffice.approval.entity.ApprovalStatus;
import com.finacial.wealth.backoffice.approval.repo.BoApprovalRequestRepository;
import com.finacial.wealth.backoffice.auth.entity.BoAdminUser;
import com.finacial.wealth.backoffice.auth.repo.BoAdminUserRepository;
import com.finacial.wealth.backoffice.integrations.fxpeer.FxPeerExchangeClient;
import com.finacial.wealth.backoffice.integrations.profiling.ProfilingClient;
import com.finacial.wealth.backoffice.integrations.transactions.TransactionsClient;
import com.finacial.wealth.backoffice.model.ApiResponse;
import com.finacial.wealth.backoffice.model.RegWalletInfoBackofficeResponse;
import com.finacial.wealth.backoffice.notification.repo.BoBackofficeNotificationRepository;
import com.finacial.wealth.backoffice.system.BackofficeIntegrationHealthService;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BackofficeDashboardService {

    private static final List<ApprovalStatus> OPEN_APPROVAL_STATUSES = List.of(
            ApprovalStatus.PENDING,
            ApprovalStatus.IN_REMEDIATION,
            ApprovalStatus.RESUBMITTED
    );

    private final BoApprovalRequestRepository approvalRequestRepository;
    private final BoBackofficeNotificationRepository notificationRepository;
    private final BoAdminUserRepository adminUserRepository;
    private final ProfilingClient profilingClient;
    private final FxPeerExchangeClient fxPeerExchangeClient;
    private final TransactionsClient transactionsClient;
    private final BackofficeIntegrationHealthService integrationHealthService;

    @Value("${bo.downstream.internal-token:}")
    private String downstreamInternalToken;

    public Map<String, Object> dashboard(Long adminUserId, String range, LocalDate fromDate, LocalDate toDate) {
        Instant generatedAt = Instant.now();

        Map<String, Object> approvals = section("approvals",
                () -> Map.of("pendingCount", approvalRequestRepository.countByStatusIn(OPEN_APPROVAL_STATUSES)));
        Map<String, Object> notifications = section("notifications",
                () -> Map.of("unreadCount", unreadCount(adminUserId)));
        Map<String, Object> adminUsers = section("adminUsers",
                () -> Map.of("activeAdminCount", adminUserRepository.countByStatus(BoAdminUser.Status.ACTIVE)));
        Map<String, Object> customers = section("customers", this::customerSummary);
        Map<String, Object> investment = section("investment",
                () -> fxPeerExchangeClient.getAdminPerformance(null, fromDate, toDate));
        Map<String, Object> ngnBalances = section("ngnBalances", this::ngnBalanceSummary);
        Map<String, Object> health = section("integrationHealth", integrationHealthService::health);

        Map<String, Object> sections = new LinkedHashMap<>();
        sections.put("approvals", approvals);
        sections.put("notifications", notifications);
        sections.put("adminUsers", adminUsers);
        sections.put("customers", customers);
        sections.put("investment", investment);
        sections.put("ngnBalances", ngnBalances);
        sections.put("integrationHealth", health);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("range", range);
        data.put("fromDate", fromDate);
        data.put("toDate", toDate);
        data.put("summaryCards", summaryCards(approvals, notifications, adminUsers, customers, ngnBalances));
        data.put("sections", sections);
        data.put("quickLinks", quickLinks());

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("status", 200);
        response.put("description", "Backoffice dashboard retrieved successfully");
        response.put("generatedAt", generatedAt);
        response.put("data", data);
        return response;
    }

    private long unreadCount(Long adminUserId) {
        if (adminUserId == null) {
            return 0;
        }
        return notificationRepository.countByRecipientAdminIdAndReadAtIsNull(adminUserId);
    }

    private Map<String, Object> customerSummary() {
        ApiResponse<Page<RegWalletInfoBackofficeResponse>> response = profilingClient.getAllCustomers(0, 1, "id,desc");
        Page<RegWalletInfoBackofficeResponse> page = response == null ? null : response.getData();
        return Map.of("customerCount", page == null ? 0 : page.getTotalElements());
    }

    private Map<String, Object> ngnBalanceSummary() {
        if (downstreamInternalToken == null || downstreamInternalToken.trim().isEmpty()) {
            return Map.of(
                    "status", "UNCONFIGURED",
                    "message", "bo.downstream.internal-token is not configured"
            );
        }
        return transactionsClient.getNgnCumulativeAccountBalanceSummary(downstreamInternalToken, null, null, null, "API");
    }

    private List<Map<String, Object>> summaryCards(
            Map<String, Object> approvals,
            Map<String, Object> notifications,
            Map<String, Object> adminUsers,
            Map<String, Object> customers,
            Map<String, Object> ngnBalances) {
        return List.of(
                card("pendingApprovals", "Pending approvals", valueAt(approvals, "pendingCount", 0), "count", "/approvals", approvals),
                card("unreadNotifications", "Unread notifications", valueAt(notifications, "unreadCount", 0), "count", "/notifications", notifications),
                card("activeAdmins", "Active admins", valueAt(adminUsers, "activeAdminCount", 0), "count", "/settings/admin-users", adminUsers),
                card("customers", "Customers", valueAt(customers, "customerCount", 0), "count", "/customers", customers),
                card("ngnCumulativeBalance", "NGN cumulative balance", extractNgnBalance(ngnBalances), "amount", "/operations/transactions", ngnBalances)
        );
    }

    private Map<String, Object> card(String key, String label, Object value, String valueType, String deepLink,
            Map<String, Object> source) {
        Map<String, Object> card = new LinkedHashMap<>();
        card.put("key", key);
        card.put("label", label);
        card.put("value", value);
        card.put("valueType", valueType);
        card.put("deepLink", deepLink);
        card.put("status", source.getOrDefault("status", "AVAILABLE"));
        return card;
    }

    private Object valueAt(Map<String, Object> section, String key, Object fallback) {
        Object data = section.get("data");
        if (data instanceof Map<?, ?> map && map.containsKey(key)) {
            return map.get(key);
        }
        return fallback;
    }

    private Object extractNgnBalance(Map<String, Object> section) {
        Object data = section.get("data");
        if (!(data instanceof Map<?, ?> map)) {
            return null;
        }
        Object nestedData = map.get("data");
        if (nestedData instanceof Map<?, ?> nestedMap) {
            return firstPresent(nestedMap, "cumulativeBalance", "totalBalance", "balance", "amount");
        }
        return firstPresent(map, "cumulativeBalance", "totalBalance", "balance", "amount");
    }

    private Object firstPresent(Map<?, ?> map, String... keys) {
        for (String key : keys) {
            if (map.containsKey(key)) {
                return map.get(key);
            }
        }
        return null;
    }

    private List<Map<String, String>> quickLinks() {
        return List.of(
                Map.of("label", "Approvals", "path", "/approvals"),
                Map.of("label", "Investment Products", "path", "/investments/products"),
                Map.of("label", "Investment Orders", "path", "/investments/orders"),
                Map.of("label", "NGN Transactions", "path", "/operations/transactions"),
                Map.of("label", "System Health", "path", "/system/integration-health")
        );
    }

    private Map<String, Object> section(String name, Supplier<Map<String, Object>> supplier) {
        Instant start = Instant.now();
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("name", name);
        try {
            result.put("data", supplier.get());
            result.put("status", "AVAILABLE");
        } catch (RuntimeException ex) {
            result.put("status", "UNAVAILABLE");
            result.put("error", ex.getClass().getSimpleName());
            result.put("message", "Dashboard section is temporarily unavailable.");
        }
        result.put("durationMs", Duration.between(start, Instant.now()).toMillis());
        return result;
    }
}
