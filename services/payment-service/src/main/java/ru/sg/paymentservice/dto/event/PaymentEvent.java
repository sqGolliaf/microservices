package ru.sg.paymentservice.dto.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.Instant;

@Builder
public record PaymentEvent(

        @JsonProperty("event_id") String eventId,

        @JsonProperty("saga_id") String sagaId,

        @JsonProperty("order_id") Long orderId,

        @JsonProperty("payment_id") String paymentId,

        @JsonProperty("order_status") String orderStatus,

        @JsonProperty("amount") BigDecimal amount,

        @JsonProperty("event_type") String eventType,

        @JsonProperty("status") String status,

        @JsonProperty("timestamp") Instant timestamp,

        @JsonProperty("source") String source) {
}
