package ru.sg.analytics.event;

import java.time.Instant;

public sealed interface AnalyticsEvent {
    Instant timestamp();
    String eventType();
    String sourceService();

    record OrderCreatedEvent(
            String orderId,
            String keycloakId,
            String productName,
            int quantity,
            double price,
            Instant timestamp,
            String sourceService
    ) implements AnalyticsEvent {

        @Override
        public String eventType() {
            return "ORDER_CREATED";
        }
    }

    record OrderConfirmedEvent(
            String orderId,
            Instant timestamp,
            String sourceService
    ) implements AnalyticsEvent {
        @
                Override
        public String eventType() {
            return "ORDER_CONFIRMED";
        }
    }

    record UserRegisteredEvent(
            String keycloakId,
            String email,
            String username,
            Instant timestamp,
            String sourceService
    ) implements AnalyticsEvent {

        @Override
        public String eventType() {
            return "USER_REGISTERED";
        }
    }

    record EmailVerifiedEvent(
            String keycloakId,
            String email,
            Instant timestamp,
            String sourceService
    ) implements AnalyticsEvent {

        @Override
        public String eventType() {
            return "EMAIL_VERIFIED";
        }
    }

    record PaymentProcessedEvent(
            String paymentId,
            String orderId,
            double amount,
            String status,
            Instant timestamp,
            String sourceService
    ) implements AnalyticsEvent {

        @Override
        public String eventType() {
            return "PAYMENT_PROCESSED";
        }
    }
}
