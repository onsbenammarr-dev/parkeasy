package com.parkeasy.parking.dto;

import com.parkeasy.parking.entity.SpotStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record SensorEventRequest(
        @NotBlank String sensorCode,
        @NotNull Long parkingSpotId,
        @NotNull SpotStatus status,
        LocalDateTime timestamp) {
}