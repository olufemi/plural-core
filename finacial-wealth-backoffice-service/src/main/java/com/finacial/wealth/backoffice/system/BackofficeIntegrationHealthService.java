package com.finacial.wealth.backoffice.system;

import com.finacial.wealth.backoffice.auth.repo.BoAdminUserRepository;
import com.finacial.wealth.backoffice.integrations.fxpeer.FxPeerExchangeClient;
import com.finacial.wealth.backoffice.integrations.profiling.ProfilingClient;
import com.finacial.wealth.backoffice.integrations.transactions.TransactionsClient;
import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BackofficeIntegrationHealthService {

    private final BoAdminUserRepository adminUserRepository;
    private final ProfilingClient profilingClient;
    private final FxPeerExchangeClient fxPeerExchangeClient;
    private final TransactionsClient transactionsClient;

    public Map<String, Object> health() {
        List<Map<String, Object>> dependencies = List.of(
                probe("BACKOFFICE_DB", () -> Map.of("adminUsers", adminUserRepository.count())),
                probe("PROFILING_SERVICE", () -> Map.of("customersProbe", profilingClient.getAllCustomers(0, 1, "id,desc") != null)),
                probe("FXPEER_EXCHANGE_SERVICE", () -> Map.of("productsProbe", fxPeerExchangeClient.getInvestmentProducts() != null)),
                probe("TRANSACTIONS_SERVICE", () -> Map.of("reversalProbe", transactionsClient.getReversalSummary() != null))
        );
        boolean allUp = dependencies.stream().allMatch(dep -> "UP".equals(dep.get("status")));
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("status", allUp ? "UP" : "DEGRADED");
        response.put("checkedAt", Instant.now());
        response.put("dependencies", dependencies);
        return response;
    }

    private Map<String, Object> probe(String name, Supplier<Map<String, Object>> supplier) {
        Instant start = Instant.now();
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("name", name);
        try {
            result.put("details", supplier.get());
            result.put("status", "UP");
        } catch (RuntimeException ex) {
            result.put("status", "DOWN");
            result.put("error", ex.getClass().getSimpleName());
            result.put("message", "Dependency probe failed.");
        }
        result.put("durationMs", Duration.between(start, Instant.now()).toMillis());
        return result;
    }
}
