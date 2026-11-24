package com.parkeasy.parking.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record SpotRequest(
        @NotNull Long parkingId,
        @NotBlank String spotNumber,
        @NotBlank @Pattern(regexp = "STANDARD|DISABLED|ELECTRIC",
                message = "type doit être STANDARD, DISABLED ou ELECTRIC") String type) {
}