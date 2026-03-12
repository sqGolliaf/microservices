package ru.sg.paymentservice.dto.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.Instant;

@Builder
public record OrderEvent(

        @JsonProperty("event_id") String eventId,

        @JsonProperty("saga_id") String sagaId,

        @JsonProperty("order_id") Long orderId,

        @JsonProperty("event_type") String eventType,

        @JsonProperty("order_status") String orderStatus,

        @JsonProperty("name") String name,

        @JsonProperty("quantity") int quantity,

        @JsonProperty("price") BigDecimal price,

        @JsonProperty("timestamp") Instant timestamp,

        @JsonProperty("source") String source

) {
}