package ru.sg.order.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.sg.order.entity.DLQEvent;
import ru.sg.order.repository.DLQEventRepository;
import ru.sg.order.service.DLQService;

import java.util.List;

@RestController
@RequestMapping("/dlq")
@RequiredArgsConstructor
@Slf4j
public class DLQController {

    private final DLQEventRepository dlqEventRepository;
    private final DLQService dlqService;

    @GetMapping("/unresolved")
    public ResponseEntity<List<DLQEvent>> getUnresolvedEvents() {
        List<DLQEvent> events = dlqEventRepository.findByIsResolvedFalseOrderByCreatedAtAsc();
        return ResponseEntity.ok(events);
    }

    @GetMapping("/{eventId}")
    public ResponseEntity<DLQEvent> getEvent(@PathVariable String eventId) {
        return dlqEventRepository.findById(eventId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{eventId}/retry")
    public ResponseEntity<String> retryEvent(@PathVariable String eventId) {
        try {
            dlqService.retryDLQEvent(eventId);
            return ResponseEntity.ok("Event retried successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/{eventId}/resolve")
    public ResponseEntity<String> resolveEvent(@PathVariable String eventId) {
        try {
            dlqService.resolveDLQEvent(eventId);
            return ResponseEntity.ok("Event resolved");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{eventId}")
    public ResponseEntity<String> deleteEvent(@PathVariable String eventId) {
        dlqEventRepository.deleteById(eventId);
        return ResponseEntity.ok("Event deleted");
    }

}
