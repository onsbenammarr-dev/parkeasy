package com.parkeasy.reservation.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record ReservationRequest(
        @NotNull Long parkingId,
        @NotNull Long spotId,
        @NotNull LocalDateTime startTime,
        @NotNull LocalDateTime endTime) {
}