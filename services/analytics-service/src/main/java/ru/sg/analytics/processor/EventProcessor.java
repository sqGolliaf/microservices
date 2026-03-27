package ru.sg.analytics.processor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.sg.analytics.event.AnalyticsEvent;
import ru.sg.analytics.service.AnalyticsService;

import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventProcessor {

    private final AnalyticsService analyticsService;

    public CompletableFuture<Void> processEventAsync(AnalyticsEvent event) {
        return CompletableFuture.runAsync(() -> {
            log.info("Processing event: {} from {}", event.eventType(), event.sourceService());

            // Switch expression (Java 12+) + Pattern matching (Java 16+)
            var result = switch (event) {
                // Pattern matching на конкретные типы
                case AnalyticsEvent.OrderCreatedEvent orderEvent -> analyticsService.processOrderCreated(orderEvent);

                case AnalyticsEvent.OrderConfirmedEvent orderConfirmed ->
                        analyticsService.processOrderConfirmed(orderConfirmed);

                case AnalyticsEvent.UserRegisteredEvent userEvent -> analyticsService.processUserRegistered(userEvent);

                case AnalyticsEvent.EmailVerifiedEvent emailEvent -> analyticsService.processEmailVerified(emailEvent);

                case AnalyticsEvent.PaymentProcessedEvent paymentEvent ->
                        analyticsService.processPaymentProcessed(paymentEvent);

                default -> throw new IllegalArgumentException("Unknown event: " + event);
            };

            log.info("Event processed: {}", event.eventType());
        });
    }
}
