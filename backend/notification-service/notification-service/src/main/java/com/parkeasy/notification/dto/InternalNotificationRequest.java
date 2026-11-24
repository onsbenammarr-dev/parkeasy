package com.parkeasy.notification.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record InternalNotificationRequest(
        @NotNull Long userId,
        @NotBlank @Size(max = 255) String message) {
}