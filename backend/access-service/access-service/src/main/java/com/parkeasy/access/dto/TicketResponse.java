package com.parkeasy.access.dto;

import java.time.LocalDateTime;

public record TicketResponse(
        Long reservationId,
        String token,
        LocalDateTime validFrom,
        LocalDateTime expiration,
        String status,
        String qrImageBase64) {
}