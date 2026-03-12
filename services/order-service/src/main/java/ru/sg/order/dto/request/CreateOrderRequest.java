package ru.sg.order.dto.request;

import jakarta.validation.constraints.*;
import lombok.Builder;
import ru.sg.order.validation.Price;

import java.math.BigDecimal;

@Builder
public record CreateOrderRequest(

        @NotBlank(message = "Name is required")
        @Size(min = 2, max = 255)
        String name,

        @Price
        BigDecimal price,

        @NotNull(message = "Quantity is required")
        @Min(1)
        @Max(100)
        Integer quantity
) {}