package com.finacial.wealth.api.fxpeer.exchange.investment.service;

import com.finacial.wealth.api.fxpeer.exchange.domain.RegWalletInfo;
import com.finacial.wealth.api.fxpeer.exchange.domain.RegWalletInfoRepository;
import com.finacial.wealth.api.fxpeer.exchange.investment.domain.InvestmentOrder;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.Instant;
import java.util.Currency;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
public class InvestmentRedemptionNotificationPublisher {

    private static final Logger log = LoggerFactory.getLogger(InvestmentRedemptionNotificationPublisher.class);
    private static final String EXCHANGE = "finwealth.notifications.exchange";
    private static final String ROUTING_KEY = "finwealth.notifications";
    private static final String TYPE_ID = "com.finacial.wealth.api.utility.models.NotificationEvent";

    private final RabbitTemplate rabbitTemplate;
    private final RegWalletInfoRepository regWalletInfoRepository;

    public InvestmentRedemptionNotificationPublisher(RabbitTemplate rabbitTemplate,
            RegWalletInfoRepository regWalletInfoRepository) {
        this.rabbitTemplate = rabbitTemplate;
        this.regWalletInfoRepository = regWalletInfoRepository;
    }

    public void redemptionRequested(InvestmentOrder order) {
        publish(order,
                "LIQUIDATION_REQUEST_CONFIRMATION",
                "Redemption request received",
                "Your redemption request of " + amount(order == null ? null : order.getAmount())
                + " has been received and is being processed.");
    }

    public void redemptionCompleted(InvestmentOrder order) {
        publish(order,
                "SUCCESSFUL_LIQUIDATION",
                "Redemption completed",
                "Your redemption of " + amount(order == null ? null : order.getNetAmount())
                + " has been completed and credited to your wallet.");
    }

    public void redemptionCancelled(InvestmentOrder order) {
        publish(order,
                "LIQUIDATION_CANCELLED",
                "Redemption request cancelled",
                "Your redemption request of " + amount(order == null ? null : order.getAmount())
                + " was not completed. The reserved investment amount has been released.");
    }

    private void publish(InvestmentOrder order, String process, String title, String message) {
        try {
            if (order == null || isBlank(order.getEmailAddress())) {
                return;
            }

            Optional<RegWalletInfo> walletInfo = regWalletInfoRepository.findByEmail(order.getEmailAddress());
            String pushToken = walletInfo.map(RegWalletInfo::getPushNotificationToken).orElse(null);
            String userId = walletInfo.map(RegWalletInfo::getWalletId).orElse(order.getWalletId());

            Map<String, Object> data = new HashMap<>();
            data.put("orderRef", order.getOrderRef());
            data.put("parentOrderRef", order.getParentOrderRef());
            data.put("amount", order.getAmount());
            data.put("netAmount", order.getNetAmount());
            data.put("status", order.getStatus() == null ? null : order.getStatus().name());
            data.put("eventType", "INVESTMENT_REDEMPTION");

            Map<String, Object> event = new HashMap<>();
            event.put("eventId", "investment-redemption-" + process + "-" + order.getOrderRef() + "-" + UUID.randomUUID());
            event.put("module", "INVESTMENT");
            event.put("process", process);
            event.put("userId", userId);
            event.put("email", order.getEmailAddress());
            event.put("pushToken", pushToken);
            event.put("title", title);
            event.put("message", message);
            event.put("data", data);
            event.put("createdAt", Instant.now().toString());

            rabbitTemplate.convertAndSend(EXCHANGE, ROUTING_KEY, event, msg -> {
                msg.getMessageProperties().setMessageId(String.valueOf(event.get("eventId")));
                msg.getMessageProperties().setCorrelationId(order.getOrderRef());
                msg.getMessageProperties().getHeaders().put("__TypeId__", TYPE_ID);
                return msg;
            });
            log.info("Investment redemption notification queued orderRef={} process={} email={}",
                    order.getOrderRef(), process, order.getEmailAddress());
        } catch (Exception ex) {
            log.warn("Investment redemption notification failed orderRef={} process={}",
                    order == null ? null : order.getOrderRef(), process, ex);
        }
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private static String amount(BigDecimal value) {
        BigDecimal safe = value == null ? BigDecimal.ZERO : value;
        NumberFormat format = NumberFormat.getCurrencyInstance(Locale.US);
        format.setCurrency(Currency.getInstance("NGN"));
        return format.format(safe);
    }
}
