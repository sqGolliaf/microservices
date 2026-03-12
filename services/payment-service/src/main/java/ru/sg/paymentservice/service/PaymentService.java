package ru.sg.paymentservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.sg.paymentservice.dto.event.OrderEvent;
import ru.sg.paymentservice.entity.Payment;
import ru.sg.paymentservice.enums.OutboxType;
import ru.sg.paymentservice.enums.PaymentStatus;
import ru.sg.paymentservice.repository.PaymentRepository;

import java.math.BigDecimal;
import java.util.Objects;

@Service
@Slf4j
@RequiredArgsConstructor
public class PaymentService {

    private static final BigDecimal UNIT_PRICE = BigDecimal.valueOf(10);

    private final PaymentRepository paymentRepository;
    private final OutboxService outboxService;
    private final PaymentCacheService paymentCacheService;

    @Transactional
    public void confirmPayment(OrderEvent event) {
        Objects.requireNonNull(event.sagaId(), "sagaId cannot be null");

        PaymentStatus status = isValidPayment(event) ? PaymentStatus.SUCCESS : PaymentStatus.NOT_SUCCESS;

        Payment payment = Payment.builder()
                .sagaId(event.sagaId())
                .amount(event.price())
                .status(status)
                .build();

        Payment savedPayment = paymentRepository.save(payment);

        paymentCacheService.putPayment(savedPayment);

        OutboxType outboxType = (status == PaymentStatus.SUCCESS)
                ? OutboxType.PAYMENT_PROCESSED
                : OutboxType.PAYMENT_CANCELED;

        outboxService.createOutboxEvent(savedPayment, outboxType);

        log.info("Payment confirmed: paymentId={}, sagaId={}, status={}",
                savedPayment.getId(), savedPayment.getSagaId(), savedPayment.getStatus());
    }

    @Transactional
    public void cancelPayment(OrderEvent event) {
        Objects.requireNonNull(event.sagaId(), "sagaId cannot be null");

        Payment payment = paymentRepository.findBySagaId(event.sagaId())
                .orElseGet(() -> {
                    Payment newPayment = Payment.builder()
                            .sagaId(event.sagaId())
                            .amount(event.price())
                            .status(PaymentStatus.FAILED)
                            .build();
                    return paymentRepository.save(newPayment);
                });

        payment.setStatus(PaymentStatus.FAILED);
        Payment savedPayment = paymentRepository.save(payment);
        paymentCacheService.putPayment(savedPayment);

        outboxService.createOutboxEvent(savedPayment, OutboxType.PAYMENT_CANCELED);
        log.info("Payment canceled: paymentId={}, sagaId={}, amount={}",
                savedPayment.getId(), savedPayment.getSagaId(), event.price());
    }

    private boolean isValidPayment(OrderEvent event) {
        BigDecimal expectedAmount = UNIT_PRICE.multiply(BigDecimal.valueOf(event.quantity()));
        return event.price().compareTo(expectedAmount) > 0;
    }

    @Transactional
    public void failPayment(OrderEvent event, String reason) {
        Objects.requireNonNull(event.sagaId(), "sagaId cannot be null");

        Payment payment = paymentRepository.findBySagaId(event.sagaId())
                .orElseThrow(() -> new RuntimeException("Payment not found: " + event.sagaId()));

        payment.setStatus(PaymentStatus.ERROR);
        Payment savedPayment = paymentRepository.save(payment);
        paymentCacheService.putPayment(savedPayment);

        log.error("Payment failed: paymentId={}, sagaId={}, reason={}",
                savedPayment.getId(), savedPayment.getSagaId(), reason);
    }
}