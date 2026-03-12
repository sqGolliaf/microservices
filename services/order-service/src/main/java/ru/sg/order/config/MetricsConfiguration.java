package ru.sg.order.config;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class MetricsConfiguration {

    private final MeterRegistry meterRegistry;

    public void registerCustomMetrics() {
        meterRegistry.counter("orders.created.total").increment();
        meterRegistry.counter("payments.success.total").increment();
        meterRegistry.counter("payments.failed.total").increment();
        Timer.Sample sample = Timer.start(meterRegistry);

        sample.stop(Timer.builder("order.processing.time")
                .description("Time taken to process order")
                .register(meterRegistry));
    }
}
