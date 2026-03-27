package ru.sg.analytics.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.sg.analytics.service.AnalyticsService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping("/summary")
    public ResponseEntity<Map<String, Object>> getSummary() {
        return ResponseEntity.ok(analyticsService.getSummary());
    }

    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Long>> getStatistics(
            @RequestParam(defaultValue = "24") int hoursBack) {
        return ResponseEntity.ok(
                analyticsService.getStatisticsByEventType(hoursBack)
        );
    }

    @GetMapping("/top-events")
    public ResponseEntity<List<Map.Entry<String, Long>>> getTopEvents(
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(analyticsService.getTopEvents(limit));
    }
}
