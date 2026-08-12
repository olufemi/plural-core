package com.financial.wealth.api.transactions.services;

import com.financial.wealth.api.transactions.domain.FinWealthPaymentTransaction;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class AdminNgnTransactionService {

    @PersistenceContext
    private EntityManager entityManager;

    public Map<String, Object> listNgnTransactions(
            String customer,
            String walletId,
            String transactionId,
            String type,
            String status,
            String q,
            String fromDate,
            String toDate,
            boolean includeLegacyNullCurrency,
            int page,
            int size) {

        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), 200);

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<FinWealthPaymentTransaction> cq = cb.createQuery(FinWealthPaymentTransaction.class);
        Root<FinWealthPaymentTransaction> root = cq.from(FinWealthPaymentTransaction.class);
        List<Predicate> predicates = buildPredicates(cb, root, customer, walletId, transactionId, type, status,
                q, fromDate, toDate, includeLegacyNullCurrency);
        List<Order> orders = new ArrayList<>();
        orders.add(cb.desc(root.get("createdDate")));
        orders.add(cb.desc(root.get("id")));
        cq.select(root).where(predicates.toArray(new Predicate[0])).orderBy(orders);

        TypedQuery<FinWealthPaymentTransaction> query = entityManager.createQuery(cq);
        query.setFirstResult(safePage * safeSize);
        query.setMaxResults(safeSize);
        List<FinWealthPaymentTransaction> rows = query.getResultList();

        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<FinWealthPaymentTransaction> countRoot = countQuery.from(FinWealthPaymentTransaction.class);
        List<Predicate> countPredicates = buildPredicates(cb, countRoot, customer, walletId, transactionId, type,
                status, q, fromDate, toDate, includeLegacyNullCurrency);
        countQuery.select(cb.count(countRoot)).where(countPredicates.toArray(new Predicate[0]));
        long totalElements = entityManager.createQuery(countQuery).getSingleResult();

        List<Map<String, Object>> content = new ArrayList<>();
        for (FinWealthPaymentTransaction tx : rows) {
            content.add(toRow(tx));
        }

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("content", content);
        data.put("page", safePage);
        data.put("size", safeSize);
        data.put("totalElements", totalElements);
        data.put("totalPages", (totalElements + safeSize - 1) / safeSize);
        data.put("currencyCode", "NGN");
        data.put("includeLegacyNullCurrency", includeLegacyNullCurrency);
        data.put("sourceService", "TRANSACTIONS");

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("statusCode", 200);
        response.put("description", "NGN transactions pulled successfully.");
        response.put("data", data);
        return response;
    }

    public Map<String, Object> getNgnTransactionDetails(String transactionId, boolean includeLegacyNullCurrency) {
        if (!hasText(transactionId)) {
            Map<String, Object> response = new LinkedHashMap<>();
            response.put("statusCode", 400);
            response.put("description", "transactionId is required.");
            response.put("data", new LinkedHashMap<>());
            return response;
        }

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<FinWealthPaymentTransaction> cq = cb.createQuery(FinWealthPaymentTransaction.class);
        Root<FinWealthPaymentTransaction> root = cq.from(FinWealthPaymentTransaction.class);

        List<Predicate> predicates = new ArrayList<>();
        Expression<String> currency = cb.upper(root.get("currencyCode").as(String.class));
        if (includeLegacyNullCurrency) {
            predicates.add(cb.or(cb.equal(currency, "NGN"), cb.isNull(root.get("currencyCode"))));
        } else {
            predicates.add(cb.equal(currency, "NGN"));
        }
        predicates.add(cb.equal(root.get("transactionId").as(String.class), transactionId.trim()));

        List<Order> orders = new ArrayList<>();
        orders.add(cb.desc(root.get("createdDate")));
        orders.add(cb.desc(root.get("id")));
        cq.select(root).where(predicates.toArray(new Predicate[0])).orderBy(orders);

        List<FinWealthPaymentTransaction> rows = entityManager.createQuery(cq).getResultList();
        List<Map<String, Object>> entries = new ArrayList<>();
        for (FinWealthPaymentTransaction tx : rows) {
            entries.add(toRow(tx));
        }

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("transactionId", transactionId.trim());
        data.put("sourceService", "TRANSACTIONS");
        data.put("currencyCode", "NGN");
        data.put("includeLegacyNullCurrency", includeLegacyNullCurrency);
        data.put("entryCount", entries.size());
        data.put("primary", entries.isEmpty() ? null : entries.get(0));
        data.put("entries", entries);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("statusCode", entries.isEmpty() ? 404 : 200);
        response.put("description", entries.isEmpty() ? "Transaction not found." : "NGN transaction details pulled successfully.");
        response.put("data", data);
        return response;
    }

    private List<Predicate> buildPredicates(
            CriteriaBuilder cb,
            Root<FinWealthPaymentTransaction> root,
            String customer,
            String walletId,
            String transactionId,
            String type,
            String status,
            String q,
            String fromDate,
            String toDate,
            boolean includeLegacyNullCurrency) {

        List<Predicate> predicates = new ArrayList<>();
        Expression<String> currency = cb.upper(root.get("currencyCode").as(String.class));
        if (includeLegacyNullCurrency) {
            predicates.add(cb.or(cb.equal(currency, "NGN"), cb.isNull(root.get("currencyCode"))));
        } else {
            predicates.add(cb.equal(currency, "NGN"));
        }

        if (hasText(walletId)) {
            predicates.add(cb.equal(root.get("walletNo").as(String.class), walletId.trim()));
        }
        if (hasText(transactionId)) {
            predicates.add(cb.equal(root.get("transactionId").as(String.class), transactionId.trim()));
        }
        if (hasText(type)) {
            String like = like(type);
            predicates.add(cb.or(
                    cb.like(lower(cb, root, "transactionType"), like),
                    cb.like(lower(cb, root, "paymentType"), like),
                    cb.like(lower(cb, root, "senderTransactionType"), like),
                    cb.like(lower(cb, root, "receiverTransactionType"), like),
                    cb.like(lower(cb, root, "requestType"), like)));
        }
        if (hasText(status)) {
            String like = like(status);
            predicates.add(cb.or(
                    cb.like(lower(cb, root, "reversals"), like),
                    cb.like(lower(cb, root, "requestType"), like),
                    cb.like(lower(cb, root, "paymentType"), like)));
        }
        if (hasText(customer)) {
            String like = like(customer);
            predicates.add(cb.or(
                    cb.like(lower(cb, root, "emailAddress"), like),
                    cb.like(lower(cb, root, "createdBy"), like),
                    cb.like(lower(cb, root, "walletNo"), like),
                    cb.like(lower(cb, root, "sender"), like),
                    cb.like(lower(cb, root, "receiver"), like),
                    cb.like(lower(cb, root, "senderName"), like),
                    cb.like(lower(cb, root, "receiverName"), like)));
        }
        if (hasText(q)) {
            String like = like(q);
            predicates.add(cb.or(
                    cb.like(lower(cb, root, "walletNo"), like),
                    cb.like(lower(cb, root, "sender"), like),
                    cb.like(lower(cb, root, "receiver"), like),
                    cb.like(lower(cb, root, "senderName"), like),
                    cb.like(lower(cb, root, "receiverName"), like),
                    cb.like(lower(cb, root, "transactionId"), like),
                    cb.like(lower(cb, root, "emailAddress"), like),
                    cb.like(lower(cb, root, "theNarration"), like),
                    cb.like(lower(cb, root, "receiverBankName"), like)));
        }
        LocalDate from = parseDate(fromDate);
        if (from != null) {
            predicates.add(cb.greaterThanOrEqualTo(root.<Instant>get("createdDate"),
                    from.atStartOfDay().toInstant(ZoneOffset.UTC)));
        }
        LocalDate to = parseDate(toDate);
        if (to != null) {
            predicates.add(cb.lessThan(root.<Instant>get("createdDate"),
                    to.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC)));
        }
        return predicates;
    }

    private Map<String, Object> toRow(FinWealthPaymentTransaction tx) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("id", tx.getId());
        row.put("sourceService", "TRANSACTIONS");
        row.put("walletNo", tx.getWalletNo());
        row.put("customerReference", firstText(tx.getEmailAddress(), tx.getCreatedBy(), tx.getWalletNo()));
        row.put("emailAddress", tx.getEmailAddress());
        row.put("transactionId", tx.getTransactionId());
        row.put("transactionType", tx.getTransactionType());
        row.put("paymentType", tx.getPaymentType());
        row.put("requestType", tx.getRequestType());
        row.put("status", firstText(tx.getReversals(), tx.getRequestType(), tx.getPaymentType()));
        row.put("amount", decimal(tx.getAmmount()));
        row.put("sentAmount", tx.getSentAmount());
        row.put("fees", decimal(tx.getFees()));
        row.put("sender", tx.getSender());
        row.put("senderName", tx.getSenderName());
        row.put("receiver", tx.getReceiver());
        row.put("receiverName", tx.getReceiverName());
        row.put("counterparty", firstText(tx.getReceiverName(), tx.getReceiver(), tx.getSenderName(), tx.getSender()));
        row.put("receiverBankName", tx.getReceiverBankName());
        row.put("receiverBankCode", tx.getReceiverBankCode());
        row.put("narration", tx.getTheNarration());
        row.put("currencyCode", firstText(tx.getCurrencyCode(), "NGN"));
        row.put("createdDate", tx.getCreatedDate());
        row.put("lastModifiedDate", tx.getLastModifiedDate());
        return row;
    }

    private Expression<String> lower(CriteriaBuilder cb, Root<FinWealthPaymentTransaction> root, String field) {
        return cb.lower(root.get(field).as(String.class));
    }

    private boolean hasText(String value) {
        return StringUtils.hasText(value);
    }

    private String like(String value) {
        return "%" + value.trim().toLowerCase(Locale.ROOT) + "%";
    }

    private LocalDate parseDate(String value) {
        if (!hasText(value)) {
            return null;
        }
        return LocalDate.parse(value.trim());
    }

    private BigDecimal decimal(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private String firstText(String... values) {
        if (values == null) {
            return null;
        }
        for (String value : values) {
            if (hasText(value)) {
                return value;
            }
        }
        return null;
    }
}
