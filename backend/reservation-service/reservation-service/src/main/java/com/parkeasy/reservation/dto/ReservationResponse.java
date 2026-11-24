package com.parkeasy.reservation.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ReservationResponse(
        Long id,
        Long parkingId,
        Long spotId,
        LocalDateTime startTime,
        LocalDateTime endTime,
        BigDecimal totalPrice,
        String status) {
}