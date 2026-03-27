package ru.sg.analytics.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import ru.sg.analytics.event.AnalyticsEvent;
import ru.sg.analytics.model.EventStatistic;
import ru.sg.analytics.repository.EventRepository;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AnalyticsService {

    private final EventRepository eventRepository;

    private final Map<String, EventStatistic> statisticsCache = new ConcurrentHashMap<>();

    private final List<String> recentEvents = new CopyOnWriteArrayList<>();

    @Async("virtualThreadExecutor")
    public String processOrderCreated(AnalyticsEvent.OrderCreatedEvent event) {
        try {
            String key = "order_" + event.orderId();

            EventStatistic stat = EventStatistic.builder()
                    .eventType(event.eventType())
                    .sourceService(event.sourceService())
                    .eventData(Map.of(
                            "orderId", event.orderId(),
                            "quantity", String.valueOf(event.quantity()),
                            "price", String.valueOf(event.price())
                    ))
                    .createdAt(Instant.now())
                    .build();

            eventRepository.save(stat);
            statisticsCache.put(key, stat);
            recentEvents.add(key);

            log.info("Order created: orderId={}, price={}", event.orderId(), event.price());

            return key;
        } catch (Exception e) {
            log.error("X Error processing order event", e);
            throw new RuntimeException("Processing failed", e);
        }
    }

    @Async("virtualThreadExecutor")
    public String processOrderConfirmed(AnalyticsEvent.OrderConfirmedEvent event) {
        return "Order confirmed: " + event.orderId();
    }

    @Async("virtualThreadExecutor")
    public String processUserRegistered(AnalyticsEvent.UserRegisteredEvent event) {
        return "User registered: " + event.email();
    }

    @Async("virtualThreadExecutor")
    public String processEmailVerified(AnalyticsEvent.EmailVerifiedEvent event) {
        return "Email verified: " + event.email();
    }

    @Async("virtualThreadExecutor")
    public String processPaymentProcessed(AnalyticsEvent.PaymentProcessedEvent event) {
        return "Payment processed: " + event.amount();
    }

    public Map<String, Long> getStatisticsByEventType(int hoursBack) {
        Instant since = Instant.now().minus(hoursBack, ChronoUnit.HOURS);

        return eventRepository.findEventsSince(since)
                .parallelStream()
                .collect(
                        Collectors.groupingByConcurrent(
                                EventStatistic::getEventType,
                                Collectors.counting()
                        )
                );
    }

    public List<Map.Entry<String, Long>> getTopEvents(int limit) {
        return statisticsCache.values()
                .parallelStream()
                .collect(
                        Collectors.groupingByConcurrent(
                                EventStatistic::getEventType,
                                Collectors.counting()
                        )
                )
                .entrySet()
                .parallelStream()
                .sorted((e1, e2) -> Long.compare(e2.getValue(), e1.getValue()))
                .limit(limit)
                .toList();
    }

    public Map<String, Object> getSummary() {
        Instant now = Instant.now();
        Instant dayAgo = now.minus(1, ChronoUnit.DAYS);

        Map<String, Double> recentStats = eventRepository.findEventsSince(dayAgo)
                .stream()
                .filter(e -> e.getCreatedAt().isAfter(dayAgo))
                .collect(
                        Collectors.groupingBy(
                                EventStatistic::getEventType,
                                Collectors.averagingInt(e -> 1)
                        )
                );

        return Map.ofEntries(
                Map.entry("totalEvents", eventRepository.count()),
                Map.entry("last24Events", recentStats.size()),
                Map.entry("cacheSize", statisticsCache.size()),
                Map.entry("lastUpdate", now)
        );
    }
}
