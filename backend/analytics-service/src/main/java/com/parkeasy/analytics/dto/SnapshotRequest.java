package com.parkeasy.analytics.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record SnapshotRequest(
        @NotNull Long parkingId,
        @NotNull @DecimalMin("0.0") @DecimalMax("1.0") Double occupancyRate,
        LocalDateTime recordedAt) {
}