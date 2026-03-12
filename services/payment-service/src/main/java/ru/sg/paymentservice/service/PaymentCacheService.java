package ru.sg.paymentservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import ru.sg.paymentservice.entity.Payment;
import ru.sg.paymentservice.repository.PaymentRepository;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentCacheService {
    private final PaymentRepository paymentRepository;

    @Cacheable(value = "payments", key="#sagaId")
    public Payment getPayment(String sagaId) {
        return paymentRepository.findBySagaId(sagaId)
                .orElseThrow(() -> new RuntimeException("Payment not found for sagaId: " + sagaId));
    }

    @CachePut(value = "payments", key = "#payment.sagaId")
    public Payment putPayment(Payment payment) {
        return paymentRepository.save(payment);
    }

    @CacheEvict(value = "payments", key = "#sagaId")
    public void evictPayment(String sagaId) {}
}
