package com.parkeasy.parking.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record ParkingRequest(
        @NotBlank String name,
        @NotBlank String address,
        @NotNull Double latitude,
        @NotNull Double longitude,
        @NotNull @DecimalMin("0.0") BigDecimal hourlyRate) {
}