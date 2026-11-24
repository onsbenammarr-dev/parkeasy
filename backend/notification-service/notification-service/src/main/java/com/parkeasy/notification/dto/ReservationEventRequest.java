package com.parkeasy.notification.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record ReservationEventRequest(
        @NotBlank String type,
        String reason,
        @NotNull Long reservationId,
        @NotNull Long userId,
        @NotNull Long parkingId,
        @NotNull Long spotId,
        @NotBlank String startTime,
        @NotBlank String endTime,
        BigDecimal totalPrice) {
}