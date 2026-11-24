package com.parkeasy.notification.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record WelcomeRequest(
        @NotNull Long userId,
        @NotBlank String email,
        @NotBlank String fullName) {
}