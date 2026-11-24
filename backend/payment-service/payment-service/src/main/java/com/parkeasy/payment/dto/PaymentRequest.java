package com.parkeasy.payment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record PaymentRequest(
        @NotNull Long reservationId,
        @NotBlank @Pattern(regexp = "CARD|WALLET",
                message = "method doit être CARD ou WALLET") String method,
        boolean simulateFailure) {
}