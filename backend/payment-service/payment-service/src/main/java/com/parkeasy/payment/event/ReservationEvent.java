package com.parkeasy.payment.event;

import java.math.BigDecimal;

public record ReservationEvent(
        String eventId,
        String type,
        Long reservationId,
        Long userId,
        BigDecimal amount,
        String occurredAt) {
}