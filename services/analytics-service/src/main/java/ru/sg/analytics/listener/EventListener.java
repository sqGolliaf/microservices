package ru.sg.analytics.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import ru.sg.analytics.event.AnalyticsEvent;
import ru.sg.analytics.processor.EventProcessor;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;

@Component
@RequiredArgsConstructor
@Slf4j
public class EventListener {

    private final EventProcessor eventProcessor;
    private final ObjectMapper objectMapper;

    @KafkaListener(
            topics = {
                    "order-events",
                    "user-events",
                    "payment-events",
                    "email-events"
            },
            groupId = "analytics-service-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleEvent(String message) {
        log.debug("Received event: {}", message);

        CompletableFuture.runAsync(() -> {
            try {
                AnalyticsEvent event = parseEvent(message);
                eventProcessor.processEventAsync(event)
                        .join();
            } catch (Exception e) {
                log.error("Error processing event", e);
            }
        }, Executors.newVirtualThreadPerTaskExecutor());
    }

    private AnalyticsEvent parseEvent(String json) throws Exception {
        JsonNode node = objectMapper.readTree(json);
        String eventType = node.get("eventType").asString();

        return switch (eventType) {
            case "ORDER_CREATED" -> objectMapper.readValue(json, AnalyticsEvent.OrderCreatedEvent.class);
            case "ORDER_CONFIRMED" -> objectMapper.readValue(json, AnalyticsEvent.OrderConfirmedEvent.class);
            case "USER_REGISTERED" -> objectMapper.readValue(json, AnalyticsEvent.UserRegisteredEvent.class);
            case "EMAIL_VERIFIED" -> objectMapper.readValue(json, AnalyticsEvent.EmailVerifiedEvent.class);
            case "PAYMENT_PROCESSED" -> objectMapper.readValue(json, AnalyticsEvent.PaymentProcessedEvent.class);
            default -> throw new IllegalArgumentException("Unknown event: " + eventType);
        };
    }
}
