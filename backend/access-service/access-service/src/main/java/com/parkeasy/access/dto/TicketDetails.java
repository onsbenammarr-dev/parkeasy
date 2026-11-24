package com.parkeasy.access.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TicketDetails(
        Long reservationId,
        String ticketStatus,
        String fullName,
        String email,
        String parkingName,
        String parkingAddress,
        String spotNumber,
        LocalDateTime startTime,
        LocalDateTime endTime,
        boolean paid,
        BigDecimal amount,
        LocalDateTime enteredAt,
        LocalDateTime exitedAt,
        long minutesToStart,
        long minutesRemaining,
        boolean canEnter,
        boolean canExit,
        String verdict) {
}