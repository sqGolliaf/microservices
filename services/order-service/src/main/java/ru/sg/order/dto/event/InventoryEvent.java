package ru.sg.order.dto.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

import java.time.Instant;

@Builder
public record InventoryEvent(

        @JsonProperty("event_id") String eventId,

        @JsonProperty("saga_id") String sagaId,

        @JsonProperty("order_id") Long orderId,

        @JsonProperty("inventory_id") String inventoryId,

        @JsonProperty("quantity") int quantity,

        @JsonProperty("event_type") String eventType,

        @JsonProperty("status") String status,

        @JsonProperty("timestamp") Instant timestamp,

        @JsonProperty("source") String source) {

}
