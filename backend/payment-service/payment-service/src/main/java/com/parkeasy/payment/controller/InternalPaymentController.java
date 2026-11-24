package com.parkeasy.payment.controller;

import com.parkeasy.payment.entity.PaymentStatus;
import com.parkeasy.payment.repository.PaymentRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/internal/payments")
public class InternalPaymentController {

    public record PaymentSummary(BigDecimal totalRevenue, long successfulPayments, long failedPayments) {
    }

    private final PaymentRepository repository;
    private final String internalKey;

    public InternalPaymentController(PaymentRepository repository,
                                     @Value("${internal.api-key}") String internalKey) {
        this.repository = repository;
        this.internalKey = internalKey;
    }

    @GetMapping("/summary")
    public PaymentSummary summary(@RequestHeader(value = "X-Internal-Key", required = false) String key) {
        if (key == null || !key.equals(internalKey)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Clé interne invalide");
        }
        return new PaymentSummary(
                repository.sumAmountByStatus(PaymentStatus.SUCCESS),
                repository.countByStatus(PaymentStatus.SUCCESS),
                repository.countByStatus(PaymentStatus.FAILED));
    }
}