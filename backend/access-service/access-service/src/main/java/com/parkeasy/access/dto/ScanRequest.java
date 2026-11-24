package com.parkeasy.access.dto;

import com.parkeasy.access.entity.EventType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ScanRequest(
        @NotBlank String token,
        @NotNull EventType eventType) {
}