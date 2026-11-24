package com.parkeasy.payment.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PaymentResponse(
        Long id,
        Long reservationId,
        BigDecimal amount,
        String method,
        String status,
        LocalDateTime paymentDate,
        String invoiceNumber) {
}